# 비밀번호 변경 API 설계

## 작업 범위

이번 작업에서는 마이페이지의 비밀번호 변경 API만 구현한다.

- 엔드포인트: `PATCH /api/users/me/password`
- 인증: 기존 `/api/users/me/**` 보안 규칙과 동일하게 인증된 `USER` 또는 `ADMIN`
- 요청 본문: `currentPassword`, `newPassword`
- 성공 응답 데이터: `{ "message": "비밀번호가 변경되었습니다." }`
- 최종 API 명세서 기준 상태코드: `200`, `400`, `401`

이미 완료된 아래 마이페이지 API는 이번 작업 범위에서 제외한다.

- `GET /api/users/me`
- `POST /api/users/me/email/verification/send`
- `POST /api/users/me/email/verification/verify`
- `PATCH /api/users/me/settings`

회원탈퇴, 내가 쓴 글/댓글 목록 등 이후 마이페이지 API도 이번 작업 범위에서 제외한다.

## 전제

- 최종 API 명세서를 최우선 기준으로 삼는다.
- PRD와 `mypage_front`의 비밀번호 변경 화면은 기능 의도를 이해하는 참고 자료로만 사용한다.
- 새 비밀번호 확인은 API 명세서에 “프론트에서” 처리한다고 되어 있으므로 서버 요청 필드에 추가하지 않는다.
- 서버 검증은 명세서에 필요한 수준으로 제한한다: 필수 요청값 검증, 현재 비밀번호 확인, 인증 확인.
- 비밀번호 해시는 기존 Spring Security `PasswordEncoder` 빈을 사용한다.
- 기존 공통 응답 envelope 구조는 유지한다.

```json
{
  "success": true,
  "status": 200,
  "message": "요청이 성공적으로 처리되었습니다.",
  "data": {}
}
```

## 검토한 접근 방식

### 추천안: API 명세서 기준의 최소 서버 검증

서버는 `currentPassword`와 `newPassword`를 받는다. 빈 값이면 `400`을 반환하고, `PasswordEncoder.matches`로 현재 비밀번호를 확인한다. 확인에 성공하면 `PasswordEncoder.encode`로 새 비밀번호를 해시해 저장하고, 메시지만 담은 응답을 반환한다.

이 방식은 최종 API 명세서에 없는 비밀번호 복잡도 정책을 임의로 추가하지 않으면서 필요한 동작을 충족한다.

### 대안: 프론트 화면의 비밀번호 복잡도 문구까지 서버에서 검증

비밀번호 변경 화면에는 “8자리 이상의 영문 소문자, 숫자 조합” 문구가 있다. 이를 서버에서도 검증하면 현재 UI 문구와는 더 일관될 수 있다.

다만 최종 API 명세서에 정의되지 않은 동작을 추가하는 것이므로 이번 작업에서는 채택하지 않는다.

### 대안: `newPasswordConfirm` 요청 필드 추가

PRD와 화면에는 새 비밀번호 확인 입력이 있다. 하지만 최종 API 명세서의 요청값은 `currentPassword`, `newPassword`뿐이고, 새 비밀번호 확인은 프론트에서 처리한다고 적혀 있다.

따라서 이번 작업에서는 `newPasswordConfirm`을 API 요청 필드로 추가하지 않는다.

## API 동작

### 성공: `200`

승인된 인증 사용자가 올바른 현재 비밀번호와 비어 있지 않은 새 비밀번호를 보내면 다음처럼 처리한다.

- 저장된 비밀번호를 `newPassword`의 BCrypt 해시로 교체한다.
- 기존 공통 성공 응답 envelope를 사용한다.
- 응답 `data` 객체에는 메시지만 포함한다.
- 원문 새 비밀번호는 응답에 절대 포함하지 않는다.

예상 응답 구조:

```json
{
  "success": true,
  "status": 200,
  "message": "요청이 성공적으로 처리되었습니다.",
  "data": {
    "message": "비밀번호가 변경되었습니다."
  }
}
```

### 유효성 검증 실패: `400`

아래 경우 기존 bad request 공통 응답 envelope를 반환한다.

- `currentPassword`가 비어 있거나 누락된 경우
- `newPassword`가 비어 있거나 누락된 경우
- `currentPassword`가 저장된 비밀번호 해시와 일치하지 않는 경우

빈 필드 검증은 기존 `MethodArgumentNotValidException` 처리 경로와 메시지를 사용한다.

```json
{
  "success": false,
  "status": 400,
  "message": "요청값이 올바르지 않습니다.",
  "data": null
}
```

현재 비밀번호가 틀린 경우에는 `MyPageApiException`을 사용하고 아래 메시지를 반환한다.

```json
{
  "success": false,
  "status": 400,
  "message": "현재 비밀번호가 올바르지 않습니다.",
  "data": null
}
```

### 인증 실패: `401`

토큰이 없거나, 토큰이 유효하지 않거나, 승인되지 않은 사용자 상태인 경우에는 기존 Spring Security/JWT 처리 경로를 그대로 사용한다.

예상 응답 구조:

```json
{
  "success": false,
  "status": 401,
  "message": "인증이 필요합니다.",
  "data": null
}
```

## 코드 설계

기존 마이페이지 패턴을 따르면서 필요한 최소 변경만 추가한다.

- `src/main/java/com/dcom/intranet/mypage/dto/` 아래에 `PasswordChangeRequest`를 추가한다.
  - record 필드: `currentPassword`, `newPassword`
  - 검증: 두 필드 모두 `@NotBlank`
  - Swagger schema 예시만 추가
- `PasswordChangeResponse`를 추가한다.
  - record 필드: `message`
  - 정적 팩토리는 필수는 아니며 직접 생성해도 충분하다.
- `PasswordChangeApiResponse`를 추가한다.
  - `MyProfileUpdateApiResponse`와 같은 wrapper 스타일을 따른다.
  - `data` 타입은 `PasswordChangeResponse`로 둔다.
- `MyPageService`를 확장한다.
  - `PasswordEncoder`를 주입한다.
  - `changePassword(String loginId, PasswordChangeRequest request)`를 추가한다.
  - 기존 메서드들과 동일하게 login ID로 사용자를 조회한다.
  - 사용자가 없으면 `ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증이 필요합니다.")`를 던진다.
  - `PasswordEncoder.matches`가 실패하면 `MyPageApiException(HttpStatus.BAD_REQUEST, "현재 비밀번호가 올바르지 않습니다.")`를 던진다.
  - 새 비밀번호를 encode한 뒤 저장한다.
- `User`를 확장한다.
  - 도메인 메서드 `changePassword(String encodedPassword)` 하나만 추가한다.
  - 새 비밀번호 관련 데이터는 외부로 노출하지 않는다.
- `MyPageController`를 확장한다.
  - `@PatchMapping("/me/password")`를 추가한다.
  - 반환 타입은 `ResponseEntity<ApiResponse<PasswordChangeResponse>>`로 둔다.
  - Swagger 응답은 `200`, `400`, `401`을 모두 명시한다.

새 서비스나 추가 추상화는 만들지 않는다.

## 테스트 설계

기존 `MyPageControllerTest`의 방식을 그대로 따른다. 즉, `@SpringBootTest`, `MockMvc`, JWT 토큰, repository assertion을 사용한다.

테스트는 반드시 먼저 작성하고 아래 순서로 진행한다.

1. 비밀번호 변경 성공 시 `200`, 공통 성공 envelope, 메시지 데이터가 반환된다.
2. 비밀번호 변경 성공 후 저장된 비밀번호는 새 비밀번호와 매칭되고 기존 비밀번호와는 매칭되지 않는다.
3. 현재 비밀번호가 틀리면 `400` 공통 envelope와 `현재 비밀번호가 올바르지 않습니다.` 메시지가 반환된다.
4. `currentPassword`가 비어 있으면 `400` 공통 envelope와 `요청값이 올바르지 않습니다.` 메시지가 반환된다.
5. `newPassword`가 비어 있으면 `400` 공통 envelope와 `요청값이 올바르지 않습니다.` 메시지가 반환된다.
6. 토큰이 없으면 `401` 공통 envelope와 `인증이 필요합니다.` 메시지가 반환된다.

기존 `saveUser` 테스트 helper는 기본 비밀번호를 BCrypt로 encode해 저장하도록 조정한다. 그래야 기존 테스트는 계속 동작하면서 비밀번호 변경 테스트는 실제 비밀번호 검증을 사용할 수 있다.

## Swagger/OpenAPI

컨트롤러 어노테이션은 Swagger Editor에서 아래 구조가 명확히 보이도록 작성한다.

- `200` 응답: `success`, `status`, `message`, `data.message`
- `400` 실패 응답: nullable `data`
- `401` 실패 응답: nullable `data`

구현 후에는 기존 마이페이지 API 작업에서 사용한 방식과 동일하게 `docs/openapi.json`을 재생성하거나 갱신한다.

## 제외 범위

- 이메일을 통한 비밀번호 재설정
- 비밀번호 확인 요청 필드
- non-blank 검증을 넘어서는 비밀번호 복잡도 정책
- 비밀번호 변경 후 강제 로그아웃 또는 토큰 폐기
- 비밀번호 변경 알림 메일
- 비밀번호 변경 외의 다른 마이페이지 API

## 성공 기준

- 비밀번호 변경 테스트를 production code보다 먼저 작성하고, 기능 미구현으로 인해 기대한 방식으로 실패하는 것을 확인한다.
- 구현 후 비밀번호 변경 테스트가 통과한다.
- 기존 마이페이지 테스트가 계속 통과한다.
- 모든 응답 envelope는 `ApiResponse<T>` 구조를 따른다.
- Swagger 응답 schema에서 성공 `data` 객체와 명세서에 정의된 모든 상태코드가 명확히 보인다.
