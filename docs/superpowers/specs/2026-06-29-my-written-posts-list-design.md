# 내가 쓴 글 목록 조회 API 설계

## 작업 범위

이번 작업에서는 마이페이지의 내가 쓴 글 목록 조회 API만 구현한다.

- 엔드포인트: `GET /api/users/me/posts`
- 인증: 기존 `/api/users/me/**` 보안 규칙과 동일하게 인증된 `USER` 또는 `ADMIN`
- 요청 쿼리: `page`, `size`, `type`
- 성공 응답 데이터: `postList`, `pageInfo`
- 최종 API 명세서 기준 상태코드: `200`, `401`

아래 API는 이번 작업 범위에서 제외한다.

- 내가 쓴 글 상세 이동 API: `GET /api/users/me/posts/{postId}`
- 내가 쓴 글 삭제 API: `DELETE /api/users/me/posts/{postId}`
- 정보 공유, 족보, 활동 사진 도메인의 작성/수정/삭제 API
- 정보 공유, 족보, 활동 사진 도메인의 테이블/엔티티 신규 설계

## 전제

- 최종 API 명세서를 최우선 기준으로 삼는다.
- PRD와 `mypage_front`의 내가 쓴 글 목록 화면은 기능 의도를 이해하는 참고 자료로만 사용한다.
- 정보 공유, 족보, 활동 사진 도메인은 다른 담당자가 구현 중인 파트이므로 이번 작업에서는 이미 구현되어 있다고 가정한다.
- 같은 Spring 서버 안에서 다른 도메인 데이터를 조합해야 하므로, mypage API가 다른 HTTP API를 직접 호출하지 않는다.
- 현재 로컬 코드에는 다른 도메인 구현체가 없으므로, mypage 쪽은 조회 포트 인터페이스에 의존한다. 실제 도메인 구현이 들어오면 그 인터페이스 구현체만 연결한다.
- 공통 응답 envelope 구조는 유지한다.

```json
{
  "success": true,
  "status": 200,
  "message": "요청이 성공적으로 처리되었습니다.",
  "data": {}
}
```

## 검토한 접근 방식

### 추천안: mypage 조회 포트에 의존하는 조합 API

`MyPageService`는 인증된 사용자만 확인하고, 실제 내가 쓴 글 목록 조회는 `MyWrittenPostReader` 같은 인터페이스에 위임한다. 이 인터페이스는 `userId`, `type`, `page`, `size`를 받아 명세서 응답에 맞는 목록과 페이지 정보를 반환한다.

이 방식은 다른 도메인 구현을 mypage가 직접 만들지 않으면서도, 정보 공유/족보/활동 사진 코드가 병합될 때 연결 지점이 명확하다.

### 대안: 다른 도메인 API를 서버 내부에서 HTTP 호출

명세서에 있는 정보 공유, 족보, 활동 사진 API를 서버 내부 HTTP 클라이언트로 호출해서 결과를 조합할 수 있다.

하지만 같은 애플리케이션 내부 기능끼리 HTTP를 다시 호출하면 인증, 트랜잭션, 테스트가 불필요하게 복잡해진다. 현재 프로젝트 구조에도 내부 HTTP 클라이언트 패턴이 없으므로 이번 작업에서는 채택하지 않는다.

### 대안: mypage에서 다른 도메인 엔티티를 새로 만든다

`InformationPost`, `JokboRecord`, `AlbumComment` 같은 엔티티를 이번 작업에서 추가해 실제 조회를 완성할 수 있다.

하지만 정보 공유, 족보, 활동 사진 도메인은 다른 담당자의 작업 범위다. 이번 작업에서 새로 만들면 선행 도메인을 임의로 구현하는 것이므로 채택하지 않는다.

## API 동작

### 성공: `200`

승인된 인증 사용자가 `GET /api/users/me/posts`를 호출하면 다음처럼 처리한다.

- 토큰의 `loginId`로 현재 사용자를 조회한다.
- 현재 사용자의 `userId`를 조회 포트에 전달한다.
- `page`, `size`, `type` query parameter를 조회 조건으로 사용한다.
- 응답 `data`는 `postList`와 `pageInfo`만 포함한다.

예상 응답 구조:

```json
{
  "success": true,
  "status": 200,
  "message": "요청이 성공적으로 처리되었습니다.",
  "data": {
    "postList": [
      {
        "postId": 1,
        "title": "오픈소스SW개발방법및도구",
        "type": "ARCHIVE",
        "createdAt": "2026-05-25T10:30:00"
      }
    ],
    "pageInfo": {
      "page": 0,
      "size": 10,
      "totalPages": 1,
      "totalElements": 1
    }
  }
}
```

### 빈 목록: `200`

작성한 글이 없으면 `postList`는 빈 배열로 반환한다. 이는 리소스 없음이 아니라 정상 조회 결과이므로 `404`를 사용하지 않는다.

```json
{
  "success": true,
  "status": 200,
  "message": "요청이 성공적으로 처리되었습니다.",
  "data": {
    "postList": [],
    "pageInfo": {
      "page": 0,
      "size": 10,
      "totalPages": 0,
      "totalElements": 0
    }
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

## Query Parameter

`page`와 `size`는 명세서에 포함된 조회 파라미터다. 요청에서 생략되면 서버 기본값을 사용한다.

- 기본 `page`: `0`
- 기본 `size`: `10`

`type`은 명세서에 포함되어 있지만 구체적인 enum 값은 별도 표가 없다. PRD의 대상 콘텐츠를 기준으로 아래 세 값만 사용한다.

- `INFO_POST`: 정보 공유 게시글
- `ARCHIVE`: 족보 글
- `PHOTO_COMMENT`: 활동 사진 댓글

`type`이 없으면 전체 유형을 조회한다. 명세서에 `400` 상태코드가 없으므로, 이번 작업에서는 잘못된 `type`에 대해 실패 응답을 추가하지 않는다. 알 수 없는 `type`은 정상 조회 결과가 없는 것으로 보고 `200`과 빈 `postList`를 반환한다.

## 코드 설계

기존 마이페이지 패턴을 따르면서 필요한 최소 변경만 추가한다.

- `MyPageController`
  - `@GetMapping("/me/posts")`를 추가한다.
  - `page`, `size`, `type`을 query parameter로 받는다.
  - Swagger 응답은 `200`, `401`만 명시한다.
- `MyPageService`
  - `getMyPosts(String loginId, int page, int size, String type)`를 추가한다.
  - 기존 메서드들과 동일하게 login ID로 사용자를 조회한다.
  - 사용자가 없으면 `ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증이 필요합니다.")`를 던진다.
  - 조회 포트에는 user ID와 query parameter를 전달한다.
- `MyWrittenPostReader`
  - 다른 도메인 구현체가 연결될 인터페이스다.
  - 반환 타입은 mypage 응답 DTO로 둔다.
  - 실제 정보 공유/족보/사진 도메인 로직은 이 인터페이스 구현체에서 담당한다.
- `EmptyMyWrittenPostReader`
  - 현재 로컬 코드에서 애플리케이션이 컴파일되고 실행될 수 있도록 하는 기본 구현체다.
  - 다른 도메인 구현이 연결되기 전까지 빈 목록을 반환한다.
  - 다른 도메인이 들어오면 이 구현체를 실제 구현체로 교체하거나 우선순위에서 밀어낸다.
- DTO
  - `MyWrittenPostListResponse`: `postList`, `pageInfo`
  - `MyWrittenPostResponse`: `postId`, `title`, `type`, `createdAt`
  - `PageInfoResponse`: `page`, `size`, `totalPages`, `totalElements`
  - `MyWrittenPostListApiResponse`: Swagger 성공 응답 wrapper

새로운 범용 pagination abstraction은 만들지 않는다. 현재 프로젝트에 공통 pagination 응답 타입이 없으므로 이번 API에 필요한 DTO만 둔다.

## 테스트 설계

기존 `MyPageControllerTest`의 방식을 그대로 따른다. 즉, `@SpringBootTest`, `MockMvc`, JWT 토큰, repository assertion을 사용한다.

테스트는 반드시 먼저 작성하고 아래 순서로 진행한다.

1. 인증된 사용자가 호출하면 `200` 공통 envelope와 `postList`, `pageInfo`가 반환된다.
2. `type=INFO_POST` 요청 시 query parameter가 조회 포트에 전달된다.
3. `type` 없이 요청하면 전체 조회로 처리된다.
4. 작성한 글이 없으면 `200`과 빈 `postList`가 반환된다.
5. 토큰이 없으면 `401` 공통 envelope와 `인증이 필요합니다.` 메시지가 반환된다.
6. `PENDING` 사용자 토큰이면 기존 보안 필터에 의해 `401` 공통 envelope가 반환된다.

현재 로컬에는 실제 다른 도메인 구현체가 없으므로, 테스트에서는 mypage 조회 포트의 테스트용 구현체를 사용해 목록 데이터를 제공한다. 이 테스트는 컨트롤러, 인증, 공통 응답 구조, query parameter 전달, DTO 구조를 검증한다.

## Swagger/OpenAPI

컨트롤러 어노테이션은 Swagger Editor에서 아래 구조가 명확히 보이도록 작성한다.

- `200` 응답: `success`, `status`, `message`, `data.postList`, `data.pageInfo`
- `401` 실패 응답: nullable `data`

구현 후에는 기존 마이페이지 API 작업에서 사용한 방식과 동일하게 `docs/openapi.json`을 재생성하거나 갱신한다. `openapi.json`은 들여쓰기와 줄바꿈을 유지한다.

## 제외 범위

- 내가 쓴 글 상세 이동 API
- 내가 쓴 글 삭제 API
- 댓글 단 글 조회 API
- 정보 공유, 족보, 활동 사진 도메인의 실제 게시글 저장/조회 구현
- 댓글 수, 본문 요약, 교수명, 첨부파일, 작성자 이름 등 명세서에 없는 추가 응답 필드
- 명세서에 없는 `400`, `403`, `404` 응답 추가

## 성공 기준

- 테스트를 production code보다 먼저 작성하고, 기능 미구현으로 인해 기대한 방식으로 실패하는 것을 확인한다.
- 구현 후 내가 쓴 글 목록 조회 테스트가 통과한다.
- 기존 마이페이지 테스트가 계속 통과한다.
- 모든 응답 envelope는 `ApiResponse<T>` 구조를 따른다.
- Swagger 응답 schema에서 성공 `data` 객체와 명세서에 정의된 모든 상태코드가 명확히 보인다.
- 다른 담당자가 구현하는 도메인 엔티티나 API를 이번 작업에서 새로 만들지 않는다.
