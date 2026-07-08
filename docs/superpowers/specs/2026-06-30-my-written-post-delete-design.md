# 내가 쓴 글 삭제 API 설계

## 작업 범위

이번 작업에서는 마이페이지의 내가 쓴 글 삭제 API만 구현한다.

- 엔드포인트: `DELETE /api/users/me/posts/{postId}`
- 인증: 기존 `/api/users/me/**` 보안 규칙과 동일하게 인증된 `USER` 또는 `ADMIN`
- 요청 값: path variable `postId`, query parameter `type`
- 성공 응답 데이터: `message`
- 최종 API 명세서 기준 상태코드: `200`, `401`, `403`, `404`

아래 작업은 이번 범위에서 제외한다.

- 정보 공유, 족보, 활동 사진, 공지사항 도메인의 실제 엔티티/테이블 신규 구현
- 각 도메인의 직접 삭제 API 구현
- 인증 principal을 `loginId`에서 `userId`로 바꾸는 전역 인증 구조 변경

## 전제

- 최종 API 명세서를 최우선 기준으로 삼는다.
- PRD는 기능 의도 확인용으로만 사용한다.
- 기존 mypage API와 동일하게 `Authentication#getName()`은 `loginId`를 반환한다.
- Service는 `loginId`로 현재 사용자를 조회한 뒤 내부 도메인 포트에는 `userId`를 전달한다.
- `type`은 프론트 이동과 백엔드 URL segment가 일치하도록 아래 문자열을 사용한다.
  - `info-posts`
  - `archives`
  - `photo-posts`
  - `notices`
- `notices`는 ADMIN 사용자가 본인이 작성한 공지를 삭제하는 경우에만 허용하는 계약으로 둔다.
- 현재 로컬 코드에는 각 게시판 도메인 구현체가 없으므로 mypage는 조회/삭제 포트 인터페이스에 의존한다.
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

### 추천안: 기존 `MyWrittenPostReader` 포트 확장

기존 목록 조회와 상세 이동 API가 사용하는 `MyWrittenPostReader`에 삭제 메서드를 추가한다. `MyPageService`는 현재 사용자를 확인하고, 실제 대상 존재 여부, 작성자 권한, 삭제 수행은 포트 구현체에 위임한다.

이 방식은 기존 mypage 구조와 가장 일관적이고, 다른 도메인 구현이 병합될 때 연결할 계약이 명확하다. 이번 작업에서 다른 도메인을 선행 구현하지 않는다는 제한사항도 지킨다.

### 대안: 별도 `MyWrittenPostDeleter` 포트 생성

삭제 책임만 분리할 수 있다. 하지만 현재 프로젝트는 이미 내가 쓴 글 목록과 상세 이동을 하나의 포트에서 다루고 있고, 삭제 계약도 같은 외부 도메인 경계에 속한다. 파일과 빈 설정만 늘어나므로 이번 범위에서는 채택하지 않는다.

### 대안: mypage에서 실제 게시판 엔티티와 삭제 로직 구현

삭제가 즉시 동작할 수 있지만, 정보 공유/족보/활동 사진/공지사항 도메인을 mypage에서 임의로 구현하게 된다. 선행 의존 기능을 바로 구현하지 말라는 조건과 맞지 않으므로 채택하지 않는다.

## API 동작

### 성공: `200`

승인된 인증 사용자가 본인이 작성한 글을 삭제하면 `200`과 메시지를 반환한다.

예상 응답 구조:

```json
{
  "success": true,
  "status": 200,
  "message": "요청이 성공적으로 처리되었습니다.",
  "data": {
    "message": "작성한 글이 삭제되었습니다."
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

### 권한 없음: `403`

대상 글은 존재하지만 현재 사용자가 작성자가 아니거나, `notices`에 대해 ADMIN 본인 작성 조건을 만족하지 못하면 `403`을 반환한다.

예상 응답 구조:

```json
{
  "success": false,
  "status": 403,
  "message": "삭제 권한이 없습니다.",
  "data": null
}
```

### 리소스 없음: `404`

요청한 `postId`와 `type`에 해당하는 대상이 없으면 `404`를 반환한다. 지원하지 않는 `type`도 삭제 가능한 대상이 없으므로 같은 `404`로 처리한다.

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
  - `@DeleteMapping("/me/posts/{postId}")`를 추가한다.
  - `postId` path variable과 required `type` query parameter를 받는다.
  - Swagger 응답은 `200`, `401`, `403`, `404`를 명시한다.
- `MyPageService`
  - `deleteMyPost(String loginId, Long postId, String type)`를 추가한다.
  - 기존 메서드들과 동일하게 login ID로 사용자를 조회한다.
  - 삭제 포트에는 user ID, post ID, type을 전달한다.
- `MyWrittenPostReader`
  - 기존 목록/상세 이동 메서드는 유지한다.
  - 삭제용 `delete(Long userId, Long postId, String type)` 메서드를 추가한다.
- `EmptyMyWrittenPostReader`
  - 현재 연결된 도메인 구현체가 없으므로 삭제는 `404` 예외를 던진다.
- DTO
  - `MyWrittenPostDeleteResponse`: `message`
  - `MyWrittenPostDeleteApiResponse`: Swagger 성공 응답 wrapper
  - `ForbiddenApiResponse`: Swagger 403 실패 응답 wrapper

## 테스트 설계

기존 `MyPageControllerTest` 방식을 그대로 따른다. 테스트는 먼저 작성하고 실패를 확인한 뒤 구현한다.

검증할 항목:

- `info-posts` 삭제 성공 시 `message` 반환
- `archives` 삭제 요청이 user ID, post ID, type을 포트에 그대로 전달함
- `photo-posts` 삭제 요청이 user ID, post ID, type을 포트에 그대로 전달함
- `notices` 삭제 요청은 ADMIN 사용자도 접근 가능하며 type을 그대로 전달함
- 토큰이 없으면 `401` 공통 envelope
- 포트가 권한 없음으로 판단하면 `403` 공통 envelope
- 포트가 대상 없음으로 판단하면 `404` 공통 envelope

## OpenAPI

구현 후 `docs/openapi.json`에 아래가 보이도록 반영한다.

- `/api/users/me/posts/{postId}` path의 `delete` operation
- required query parameter `type`
- `200`, `401`, `403`, `404` response
- `data.message` schema
