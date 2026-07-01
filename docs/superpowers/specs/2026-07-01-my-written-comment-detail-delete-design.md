# 내가 쓴 댓글 상세 이동 및 삭제 API 설계

## 작업 범위

이번 작업에서는 마이페이지의 내가 쓴 댓글 상세 이동 API와 내가 쓴 댓글 삭제 API만 구현한다.

- 상세 이동: `GET /api/users/me/comments/{commentId}`
- 삭제: `DELETE /api/users/me/comments/{commentId}`
- 인증: 기존 `/api/users/me/**` 보안 규칙과 동일하게 인증된 `USER` 또는 `ADMIN`
- 요청 값: path variable `commentId`, query parameter `type`
- 상세 이동 성공 응답 데이터: `targetType`, `targetId`, `commentId`
- 삭제 성공 응답 데이터: `message`
- 최종 API 명세서 기준 상태코드
  - 상세 이동: `200`, `401`, `404`
  - 삭제: `200`, `401`, `403`, `404`

아래 작업은 이번 범위에서 제외한다.

- 정보 공유 댓글 작성/수정/삭제 API 구현
- 활동 사진 댓글 작성/수정/삭제 API 구현
- 정보 공유, 활동 사진 도메인의 실제 댓글 엔티티/테이블 신규 구현
- 댓글 목록 조회 API의 응답 구조 변경
- 인증 principal을 `loginId`에서 `userId`로 바꾸는 전역 인증 구조 변경

## 전제

- 최종 API 명세서를 최우선 기준으로 삼는다.
- PRD와 `mypage_front`는 기능 의도와 프론트 이동 흐름을 이해하기 위한 참고 자료로만 사용한다.
- 현재 mypage API는 응답 data 구조와 라우팅 타입 값을 이미 통일했다.
- 성공 공통 응답 메시지는 현재 코드 기준인 `요청에 성공했습니다.`를 유지한다.
- 댓글 상세 이동의 `targetType`은 프론트 라우팅에 바로 사용할 URL segment 문자열이다.
- 지원하는 댓글 라우팅 타입은 아래 두 가지다.
  - `info-posts`
  - `photo-posts`
- 기존 호환 입력으로 `INFO_POST`, `PHOTO_ALBUM`, `PHOTO_COMMENT`이 들어와도 `MyPageRouteType.normalize()`를 통해 라우팅 타입으로 정규화한다.
- 현재 로컬 코드에는 정보 공유/활동 사진 댓글 도메인 구현체가 없으므로 mypage는 포트 인터페이스에 의존한다.
- 공통 응답 envelope 구조를 유지한다.

```json
{
  "success": true,
  "status": 200,
  "message": "요청에 성공했습니다.",
  "data": {}
}
```

## 검토한 접근 방식

### 추천안: 기존 `MyWrittenCommentReader` 포트 확장

기존 댓글 목록 조회가 사용하는 `MyWrittenCommentReader`에 상세 이동과 삭제 메서드를 추가한다. `MyPageService`는 현재 사용자를 확인하고 `type`을 `MyPageRouteType.normalize()`로 정규화한 뒤, 실제 댓글 존재 여부, 작성자 권한, 원본 대상 매핑, 삭제 수행은 포트 구현체에 위임한다.

이 방식은 게시글 목록/상세/삭제 API가 이미 사용하는 구조와 가장 일관적이다. 다른 도메인 구현이 병합될 때 연결할 계약도 명확하고, 이번 작업에서 선행 도메인 기능을 임의 구현하지 않는다.

### 대안: 별도 target/deleter 포트 생성

`MyWrittenCommentTargetReader`, `MyWrittenCommentDeleter`처럼 상세 이동과 삭제 책임을 분리할 수 있다. 하지만 현재 코드에서는 게시글도 하나의 reader 포트에 목록, 상세 이동, 삭제 계약을 두고 있다. 댓글만 더 세분화하면 파일과 빈 설정이 늘고 mypage 내부 패턴이 흔들리므로 채택하지 않는다.

### 대안: mypage에서 댓글 도메인 엔티티와 삭제 로직 구현

정보 공유 댓글과 활동 사진 댓글 엔티티를 mypage에서 직접 만들어 조회/삭제를 완성할 수 있다. 하지만 해당 도메인 API는 다른 파트의 선행 기능이며, 사용자가 선행 의존 기능은 먼저 제안하고 바로 구현하지 말라고 했으므로 채택하지 않는다.

## API 동작

### 댓글 상세 이동 성공: `200`

승인된 인증 사용자가 `GET /api/users/me/comments/{commentId}?type={type}`을 호출하면 다음처럼 처리한다.

- 토큰의 `loginId`로 현재 사용자를 조회한다.
- 요청 `type`을 `MyPageRouteType.normalize()`로 라우팅 타입으로 정규화한다.
- 현재 사용자의 `userId`, `commentId`, 정규화된 `type`을 댓글 포트에 전달한다.
- 포트는 사용자가 접근할 수 있는 본인 댓글이면 원본 상세 페이지 이동 정보를 반환한다.

예상 응답:

```json
{
  "success": true,
  "status": 200,
  "message": "요청에 성공했습니다.",
  "data": {
    "targetType": "info-posts",
    "targetId": 12,
    "commentId": 101
  }
}
```

`PHOTO_ALBUM` 또는 `photo-posts` 댓글이면 `targetType`은 `photo-posts`다.

### 댓글 삭제 성공: `200`

승인된 인증 사용자가 본인이 작성한 댓글을 삭제하면 `200`과 메시지를 반환한다.

예상 응답:

```json
{
  "success": true,
  "status": 200,
  "message": "요청에 성공했습니다.",
  "data": {
    "message": "작성한 댓글이 삭제되었습니다."
  }
}
```

### 인증 실패: `401`

토큰이 없거나, 토큰이 유효하지 않거나, 승인되지 않은 사용자 상태인 경우에는 기존 Spring Security/JWT 처리 경로를 그대로 사용한다.

예상 응답:

```json
{
  "success": false,
  "status": 401,
  "message": "인증이 필요합니다.",
  "data": null
}
```

### 삭제 권한 없음: `403`

댓글은 존재하지만 현재 사용자가 작성자가 아니면 삭제 API는 `403`을 반환한다. `ADMIN`의 타인 댓글 삭제 허용 여부는 정보 공유/활동 사진 댓글 도메인의 작성자 또는 관리자 삭제 정책에 맞춰 포트 구현체가 판단한다.

예상 응답:

```json
{
  "success": false,
  "status": 403,
  "message": "삭제 권한이 없습니다.",
  "data": null
}
```

### 리소스 없음: `404`

요청한 `commentId`와 `type`에 해당하는 이동/삭제 대상 댓글이 없으면 `404`를 반환한다. 지원하지 않는 `type`도 이동 또는 삭제 가능한 대상이 없으므로 같은 `404`로 처리한다.

예상 응답:

```json
{
  "success": false,
  "status": 404,
  "message": "작성한 댓글을 찾을 수 없습니다.",
  "data": null
}
```

## 코드 설계

- `MyPageController`
  - `@GetMapping("/me/comments/{commentId}")`를 추가한다.
  - `@DeleteMapping("/me/comments/{commentId}")`를 추가한다.
  - `commentId` path variable과 required `type` query parameter를 받는다.
  - Swagger 응답은 명세서 상태코드를 모두 명시한다.
- `MyPageService`
  - `getMyCommentTarget(String loginId, Long commentId, String type)`를 추가한다.
  - `deleteMyComment(String loginId, Long commentId, String type)`를 추가한다.
  - 기존 메서드들과 동일하게 login ID로 사용자를 조회한다.
  - 포트 호출 전 `MyPageRouteType.normalize(type)`를 적용한다.
- `MyWrittenCommentReader`
  - 기존 목록 조회 메서드는 유지한다.
  - 상세 이동용 `readTarget(Long userId, Long commentId, String type)` 메서드를 추가한다.
  - 삭제용 `delete(Long userId, Long commentId, String type)` 메서드를 추가한다.
- `EmptyMyWrittenCommentReader`
  - 목록은 기존처럼 빈 목록을 반환한다.
  - 상세 이동과 삭제는 현재 연결된 도메인 구현체가 없으므로 `404` 예외를 던진다.
- DTO
  - `MyWrittenCommentTargetResponse`: `targetType`, `targetId`, `commentId`
  - `MyWrittenCommentDeleteResponse`: `message`
  - `MyWrittenCommentTargetApiResponse`: Swagger 성공 응답 wrapper
  - `MyWrittenCommentDeleteApiResponse`: Swagger 성공 응답 wrapper

기존 `MyWrittenPostTargetResponse`와 `MyWrittenPostDeleteResponse`를 댓글 API에 재사용하지 않는다. Swagger Editor에서 댓글 API의 data 구조와 예시가 댓글 용어로 명확히 보이도록 별도 DTO를 둔다.

## 테스트 설계

기존 `MyPageControllerTest` 방식을 그대로 따른다. 테스트는 반드시 먼저 작성하고, 기능 미구현으로 실패하는 것을 확인한 뒤 최소 구현으로 통과시킨다.

검증할 항목:

- 댓글 상세 이동 성공 시 `200` 공통 envelope와 `targetType`, `targetId`, `commentId`를 반환한다.
- `INFO_POST` 입력은 포트에 `info-posts`로 전달되고 응답도 `info-posts`로 반환된다.
- `PHOTO_ALBUM` 입력은 포트에 `photo-posts`로 전달되고 응답도 `photo-posts`로 반환된다.
- 이미 라우팅 타입인 `info-posts`, `photo-posts` 입력은 그대로 전달된다.
- 댓글 상세 이동 토큰 없음은 `401` 공통 envelope를 반환한다.
- 댓글 상세 이동 대상 없음은 `404` 공통 envelope와 `작성한 댓글을 찾을 수 없습니다.` 메시지를 반환한다.
- 댓글 삭제 성공 시 `200` 공통 envelope와 `작성한 댓글이 삭제되었습니다.` 메시지를 반환한다.
- 댓글 삭제 요청은 user ID, comment ID, 정규화된 type을 포트에 그대로 전달한다.
- 댓글 삭제 토큰 없음은 `401` 공통 envelope를 반환한다.
- 댓글 삭제 권한 없음은 `403` 공통 envelope와 `삭제 권한이 없습니다.` 메시지를 반환한다.
- 댓글 삭제 대상 없음은 `404` 공통 envelope와 `작성한 댓글을 찾을 수 없습니다.` 메시지를 반환한다.

## OpenAPI

구현 후 `docs/openapi.json`에 아래가 보이도록 반영한다.

- `/api/users/me/comments/{commentId}` path
- `get` operation
  - required query parameter `type`
  - `200`, `401`, `404` response
  - `data.targetType`, `data.targetId`, `data.commentId` schema
- `delete` operation
  - required query parameter `type`
  - `200`, `401`, `403`, `404` response
  - `data.message` schema

`docs/openapi.json`은 수정 시 들여쓰기와 줄바꿈을 유지한다.

## 성공 기준

- 테스트를 production code보다 먼저 작성하고, 기능 미구현으로 인해 기대한 방식으로 실패하는 것을 확인한다.
- 구현 후 새 댓글 상세 이동/삭제 테스트가 통과한다.
- 기존 마이페이지 테스트가 계속 통과한다.
- 모든 응답 envelope는 `ApiResponse<T>` 구조를 따른다.
- Swagger 응답 schema에서 성공 `data` 객체와 명세서에 정의된 모든 상태코드가 명확히 보인다.
- 다른 담당자가 구현하는 댓글 도메인 엔티티나 API를 이번 작업에서 새로 만들지 않는다.
