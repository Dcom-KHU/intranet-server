# 회원 탈퇴 API 설계

## 작업 범위

이번 작업에서는 마이페이지의 회원 탈퇴 API만 구현한다.

- 엔드포인트: `PATCH /api/users/me/withdraw`
- 인증: 기존 `/api/users/me/**` 보안 규칙과 동일하게 인증된 `USER` 또는 `ADMIN`
- 요청 값: 없음
- 성공 응답 데이터: `userId`, `status`, `withdrawnAt`
- 상태코드: `200`, `401`

아래 작업은 이번 범위에서 제외한다.

- 로그아웃 API 또는 refresh token 무효화 구현
- 탈퇴 회원의 2년 후 물리 삭제 배치 구현
- 회원 재가입 정책 구현
- 로그인, 회원가입, 관리자 회원 관리 API 변경
- 전역 인증 구조 변경

## 전제

- 최종 API 명세서를 최우선 기준으로 삼는다.
- PRD는 기능 의도 확인용으로 사용한다.
- 기존 `UserStatus` 값은 `PENDING`, `APPROVED`, `WITHDRAWN`만 사용한다.
- 기존 회원이 탈퇴하면 `User.status`를 `WITHDRAWN`으로 변경한다.
- 명세서와 PRD에 탈퇴일시가 있으므로 `User`에 `withdrawnAt` nullable 컬럼을 추가한다.
- 프론트 회원 탈퇴 화면은 비밀번호 재입력 없이 탈퇴 버튼만 제공하므로 request body 없이 처리한다.
- 이 설계는 최종 API 명세서에 있던 request body `password`와 상태코드 `400`을 제거하는 방향의 명세 변경을 전제로 한다.
- 기존 보안 필터가 `APPROVED` 사용자만 인증 처리하므로, 탈퇴 후 같은 토큰으로 `/api/users/me/**`에 접근하면 인증 실패 `401`이 된다.
- 공통 응답 envelope 구조를 유지한다.

```json
{
  "success": true,
  "status": 200,
  "message": "요청이 성공적으로 처리되었습니다.",
  "data": {}
}
```

## 검토한 접근 방식

### 추천안: 원클릭 탈퇴와 `User.withdraw()` 도메인 메서드 추가

`User` 엔티티에 `withdrawnAt` 필드를 추가하고, `withdraw(LocalDateTime withdrawnAt)` 메서드에서 `status = WITHDRAWN`과 탈퇴일시 저장을 함께 처리한다. `MyPageService`는 인증된 현재 사용자를 조회한 뒤 이 메서드만 호출한다.

이 방식은 기존 `updateProfile`, `changeEmail`, `changePassword`처럼 사용자 상태 변경을 엔티티 메서드로 표현하는 패턴과 일치한다. 탈퇴 상태 변경 규칙도 한 곳에 모이며, 이후 관리자 API나 정리 배치가 같은 상태값과 일시를 기준으로 판단하기 쉽다.

### 대안: 비밀번호 재확인 유지

최종 API 명세서의 기존 형태처럼 request body `password`를 받고 현재 비밀번호 불일치 시 `400`을 반환할 수 있다. 하지만 제공된 프론트 화면은 비밀번호 입력 없이 탈퇴 버튼만 제공하므로 화면 흐름과 맞지 않는다. 이번 작업에서는 채택하지 않는다.

### 대안: Service에서 상태와 탈퇴일시를 직접 변경

구현량은 조금 적지만 `User`의 상태 변경 규칙이 서비스로 흩어진다. 기존 코드 스타일과 덜 맞고, 같은 상태 변경을 다른 기능에서 재사용하기 어렵다. 이번 작업에서는 채택하지 않는다.

### 대안: `withdrawnAt` 저장 없이 응답 시점만 반환

엔티티 컬럼 추가를 피할 수 있지만, PRD의 탈퇴일시와 향후 2년 후 삭제 정책을 DB에 남길 수 없다. 최종 API 명세서의 `withdrawnAt` 의미도 약해지므로 채택하지 않는다.

## API 동작

### 성공: `200`

승인된 인증 사용자가 탈퇴를 요청하면 회원 상태를 `WITHDRAWN`으로 변경하고 탈퇴일시를 저장한다.

예상 응답 구조:

```json
{
  "success": true,
  "status": 200,
  "message": "요청이 성공적으로 처리되었습니다.",
  "data": {
    "userId": 1,
    "status": "WITHDRAWN",
    "withdrawnAt": "2026-07-01T10:30:00"
  }
}
```

### 인증 실패: `401`

토큰이 없거나, 토큰이 유효하지 않거나, 사용자 상태가 `APPROVED`가 아니면 기존 Spring Security/JWT 처리 경로를 그대로 사용한다.

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

- `User`
  - nullable `withdrawnAt` 필드를 추가한다.
  - `withdraw(LocalDateTime withdrawnAt)` 메서드를 추가한다.
  - getter `getWithdrawnAt()`을 추가한다.
- `MyPageService`
  - `withdraw(String loginId)`를 추가한다.
  - 기존 mypage 메서드들과 동일하게 login ID로 현재 사용자를 조회한다.
  - `user.withdraw(LocalDateTime.now())`를 호출한다.
- `MyPageController`
  - `@PatchMapping("/me/withdraw")`를 추가한다.
  - `Authentication`만 받는다.
  - Swagger 응답은 `200`, `401`을 명시한다.
- DTO
  - `MemberWithdrawResponse`: `userId`, `status`, `withdrawnAt`
  - `MemberWithdrawApiResponse`: Swagger 성공 응답 wrapper

## 테스트 설계

기존 `MyPageControllerTest` 방식을 그대로 따른다. 테스트는 먼저 작성하고 실패를 확인한 뒤 구현한다.

검증할 항목:

- 인증된 사용자가 탈퇴하면 `200` 공통 envelope와 `userId`, `status`, `withdrawnAt` 반환
- 탈퇴 성공 후 DB의 사용자 상태가 `WITHDRAWN`이고 `withdrawnAt`이 저장됨
- 탈퇴 성공 후 같은 토큰으로 `/api/users/me`에 접근하면 `401`
- 토큰이 없으면 `401` 공통 envelope
- 이미 `WITHDRAWN` 상태인 사용자의 토큰은 인증 필터에서 거부되어 `401`

## OpenAPI

구현 후 `docs/openapi.json`에 아래가 보이도록 반영한다.

- `/api/users/me/withdraw` path의 `patch` operation
- request body 없음
- `200`, `401` response
- 성공 응답의 `data.userId`, `data.status`, `data.withdrawnAt` schema
