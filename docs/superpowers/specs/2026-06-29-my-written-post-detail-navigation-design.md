# 내가 쓴 글 상세 이동 API 설계

## 작업 범위

이번 작업에서는 마이페이지의 내가 쓴 글 상세 이동 API만 구현한다.

- 엔드포인트: `GET /api/users/me/posts/{postId}`
- 인증: 기존 `/api/users/me/**` 보안 규칙과 동일하게 인증된 `USER` 또는 `ADMIN`
- 요청 값: path variable `postId`, query parameter `type`
- 성공 응답 데이터: `targetType`, `targetId`
- 최종 API 명세서 기준 상태코드: `200`, `401`, `404`

아래 API는 이번 작업 범위에서 제외한다.

- 내가 쓴 글 삭제 API: `DELETE /api/users/me/posts/{postId}`
- 정보 공유, 족보, 활동 사진, 공지사항 도메인의 상세 조회 API 구현
- 각 도메인 게시글 엔티티나 테이블 신규 설계

## 전제

- 최종 API 명세서를 최우선 기준으로 삼는다.
- PRD는 상세 이동의 의도를 확인하는 참고 자료로만 사용한다.
- `targetType`은 내부 enum 이름이 아니라 프론트엔드가 상세 페이지 이동에 그대로 사용할 백엔드 URL segment 문자열이다.
- Notice는 관리자만 작성 가능하지만, `ADMIN` 사용자가 본인이 작성한 공지를 마이페이지에서 열 수 있어야 한다.
- 현재 로컬 코드에는 정보 공유, 족보, 활동 사진, 공지사항 도메인 구현체가 없으므로 mypage는 조회 포트 인터페이스에 의존한다.
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

### 추천안: 기존 mypage 조회 포트 확장

기존 `MyWrittenPostReader`에 상세 이동용 메서드를 추가한다. `MyPageService`는 인증된 사용자를 확인하고, 실제 `postId`, `type`, 작성자 검증, target 매핑은 reader 구현체에 위임한다.

이 방식은 기존 내가 쓴 글 목록 조회 설계와 같은 경계에 머무르며, 다른 도메인 구현이 병합될 때 연결할 위치가 명확하다. 이번 작업에서 다른 도메인을 임의로 구현하지 않는다는 제한사항도 지킬 수 있다.

### 대안: 별도 `MyWrittenPostTargetReader` 인터페이스 생성

목록 조회와 상세 이동 조회를 별도 포트로 분리할 수 있다. 역할은 더 세분화되지만, 현재 mypage 기능에서 같은 외부 도메인 데이터를 다루는 작은 계약이므로 파일과 빈 설정이 불필요하게 늘어난다.

### 대안: mypage에서 대상 URL을 검증 없이 바로 반환

`type`과 `postId`를 그대로 `targetType`, `targetId`로 반환하면 구현은 가장 작다. 하지만 명세서의 `404`를 처리할 수 없고, 본인이 쓴 글인지 확인할 수 없다. 따라서 채택하지 않는다.

## API 동작

### 성공: `200`

승인된 인증 사용자가 `GET /api/users/me/posts/{postId}?type={type}`을 호출하면 다음처럼 처리한다.

- 토큰의 `loginId`로 현재 사용자를 조회한다.
- 현재 사용자의 `userId`, `postId`, `type`을 조회 포트에 전달한다.
- 조회 포트는 사용자가 접근할 수 있는 본인 작성 대상이면 `targetType`, `targetId`를 반환한다.
- `targetType`은 요청 `type`과 같은 URL segment 문자열이다.

지원하는 `type`과 `targetType`은 아래 네 가지다.

- `info-posts`
- `archives`
- `photo-posts`
- `notices`

예상 응답 구조:

```json
{
  "success": true,
  "status": 200,
  "message": "요청이 성공적으로 처리되었습니다.",
  "data": {
    "targetType": "archives",
    "targetId": 11
  }
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

### 리소스 없음: `404`

요청한 `postId`와 `type`에 해당하는 본인 작성 대상이 없으면 `404`를 반환한다. 지원하지 않는 `type`도 이동 가능한 대상이 없으므로 같은 `404`로 처리한다.

예상 응답 구조:

```json
{
  "success": false,
  "status": 404,
  "message": "작성한 글을 찾을 수 없습니다.",
  "data": null
}
```

## 코드 설계

- `MyPageController`
  - `@GetMapping("/me/posts/{postId}")`를 추가한다.
  - `postId` path variable과 required `type` query parameter를 받는다.
  - Swagger 응답은 `200`, `401`, `404`를 명시한다.
- `MyPageService`
  - `getMyPostTarget(String loginId, Long postId, String type)`를 추가한다.
  - 기존 메서드들과 동일하게 login ID로 사용자를 조회한다.
  - 조회 포트에는 user ID, post ID, type을 전달한다.
- `MyWrittenPostReader`
  - 기존 목록 조회 메서드는 유지한다.
  - 상세 이동용 `readTarget(Long userId, Long postId, String type)` 메서드를 추가한다.
- `EmptyMyWrittenPostReader`
  - 목록은 기존처럼 빈 목록을 반환한다.
  - 상세 이동은 현재 연결된 도메인 구현체가 없으므로 `404` 예외를 던진다.
- DTO
  - `MyWrittenPostTargetResponse`: `targetType`, `targetId`
  - `MyWrittenPostTargetApiResponse`: Swagger 성공 응답 wrapper
  - `NotFoundApiResponse`: Swagger 실패 응답 wrapper

## 테스트 설계

기존 `MyPageControllerTest` 방식을 그대로 따른다. 테스트는 먼저 작성하고 실패를 확인한 뒤 구현한다.

검증할 항목:

- `info-posts` 상세 이동 성공 시 `targetType: "info-posts"`, `targetId` 반환
- `archives` 상세 이동 성공 시 `targetType: "archives"`, `targetId` 반환
- `photo-posts` 상세 이동 성공 시 `targetType: "photo-posts"`, `targetId` 반환
- `notices` 상세 이동 성공 시 `targetType: "notices"`, `targetId` 반환
- reader에 user ID, post ID, type이 그대로 전달됨
- 토큰이 없으면 `401` 공통 envelope
- 대상이 없으면 `404` 공통 envelope

## OpenAPI

구현 후 `docs/openapi.json`에 아래가 보이도록 반영한다.

- `/api/users/me/posts/{postId}` path
- required query parameter `type`
- `200`, `401`, `404` response
- `data.targetType`, `data.targetId` schema
