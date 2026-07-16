# 관리자 회원 목록 이름 가나다순 정렬 설계

## 배경

관리자 회원 목록 조회 API `GET /api/admin/users`는 기존 명세에서 `page`, `size`, `keyword`, `sort` 요청 파라미터를 받고 학번순과 최근 접속일순 정렬을 지원한다. 이번 작업은 같은 API에 회원 이름 가나다순 정렬을 추가한다.

기준 자료는 다음과 같다.

- `최종_API_명세서.pdf`: 관리자 회원 목록 조회의 endpoint, 권한, 요청 파라미터, 응답 구조
- `BackEnd 전달 사항.rtf`: Swagger에 프론트가 사용할 요청과 응답 계약을 명확히 표시하고 기존 필드명을 일관되게 유지해야 한다는 협업 원칙
- `D COM 인트라넷 개편 프로젝트 개요.pdf`: `users.name`, `users.student_id`, `users.last_login_at` 데이터 모델

## 범위

포함한다.

- `sort=name,asc`를 이름 가나다순 정렬 계약으로 지원
- 검색어가 있거나 없어도 동일한 정렬 적용
- 동명이인의 페이지 순서가 바뀌지 않도록 `id ASC` 보조 정렬 적용
- Swagger 설명에 학번순, 최신 접속일순, 이름 가나다순의 요청 예시 명시
- 이름 가나다순 정렬 회귀 테스트

포함하지 않는다.

- 응답 JSON 필드 또는 공통 응답 형식 변경
- 기본 정렬(`createdAt DESC`) 변경
- 기존 학번순 및 최근 접속일순 동작 변경
- DB 스키마나 컬럼 collation 변경
- 관리자 화면 또는 프론트엔드 변경

## API 계약

요청 예시:

```http
GET /api/admin/users?page=0&size=20&sort=name,asc
```

검색과 함께 사용하는 예시:

```http
GET /api/admin/users?keyword=김&page=0&size=20&sort=name,asc
```

공식 정렬 예시는 다음과 같이 문서화한다.

- 학번 오름차순: `sort=studentId,asc`
- 최근 접속일 내림차순: `sort=lastLoginAt,desc`
- 이름 가나다순: `sort=name,asc`

응답은 기존 `CommonResponse<AdminUserListResponse>` 구조를 그대로 유지한다. `userList`의 각 항목과 `pageInfo` 필드는 변경하지 않는다.

## 정렬 처리

Controller는 기존처럼 Spring `Pageable`을 받는다. Service는 요청 정렬에 `name ASC`가 포함되어 있으면 `id ASC`를 마지막 정렬 조건으로 추가한 `Pageable`을 Repository에 전달한다.

실제 정렬 조건은 다음과 같다.

```text
ORDER BY name ASC, id ASC
```

`name`은 필수 컬럼이므로 null 처리 규칙은 필요하지 않다. 한글 완성형 이름은 운영 DB의 오름차순 문자열 정렬을 사용한다. 이번 범위에서는 운영 DB의 collation을 변경하지 않는다.

이름 정렬이 아닌 요청은 원래 `Pageable`을 그대로 전달한다. 따라서 기본 정렬, 학번순, 최신 접속일순 및 복수 정렬 요청의 기존 의미를 유지한다.

## 문서화

`AdminController.getUserList`의 OpenAPI 설명에 세 가지 공식 정렬 예시를 명시한다. 전달사항의 필드명 일관성 원칙에 따라 새로운 별칭 파라미터를 만들지 않고 엔티티와 현재 API에서 사용 중인 `name`, `studentId`, `lastLoginAt`을 그대로 사용한다.

## 테스트 전략

TDD로 다음 순서로 진행한다.

1. 이름 정렬 요청이 `name ASC, id ASC` 정렬로 Repository에 전달되는 실패 테스트를 먼저 작성한다.
2. 검색어가 있는 이름 정렬 요청에도 동일한 보조 정렬이 적용되는 실패 테스트를 작성한다.
3. 기존 정렬 요청은 변경되지 않는 테스트로 회귀를 방지한다.
4. 최소 구현 후 새 테스트와 전체 테스트를 실행한다.

테스트는 `AdminService` 단위 테스트로 작성하고 `UserRepository`에 전달되는 `Pageable`을 캡처해 정렬 조건을 검증한다. 목록 응답 매핑과 검색 분기는 실제 Service 코드를 통과시킨다.

## 성공 기준

- `GET /api/admin/users?sort=name,asc`가 회원을 이름 오름차순으로 조회한다.
- 동명이인은 `id ASC`로 안정적인 순서를 가진다.
- `keyword` 유무와 관계없이 이름 정렬이 적용된다.
- 기본 정렬과 기존 학번순·최근 접속일순은 변경되지 않는다.
- Swagger에서 이름 가나다순 요청값을 확인할 수 있다.
- 새 테스트와 기존 전체 테스트가 통과한다.
