# 회원정보 조회 API 설계

## 배경

이번 작업은 mypage 파트 전체가 아니라 첫 번째 기능인 회원정보 조회 API만 구현한다. 기준 문서는 `최종_API_명세서.pdf`이며, PRD 문서와 `mypage_front` 이미지는 보조 자료로만 사용한다.

사용자와 논의한 명세 변경에 따라 `GET /api/users/me` 응답에는 기존 `userId` 대신 `loginId`를 포함한다. `userId`는 서버 내부 PK와 연관관계 관리를 위해 유지하지만, 회원정보 조회 응답에는 노출하지 않는다.

## 범위

구현 대상은 `GET /api/users/me` 하나다.

포함한다:

- 공통 응답 포맷
- 사용자 엔티티와 Repository
- JWT 기반 인증 필터의 최소 구현
- 회원정보 조회 Controller, Service, Response DTO
- 상태코드 `200`, `401` 테스트
- Swagger에서 공통 응답 envelope와 `data` 구조가 보이도록 하는 스키마

포함하지 않는다:

- 회원가입, 로그인, 토큰 재발급 등 Auth API
- 회원정보 수정
- 이메일 변경 인증
- 비밀번호 변경
- 내가 쓴 글 조회
- 회원탈퇴
- 관리자 API

## API 계약

Endpoint:

```http
GET /api/users/me
Authorization: Bearer <access-token>
```

Auth:

- `USER`
- `ADMIN`도 `USER` 권한을 포함하므로 접근 가능

성공 응답:

```json
{
  "success": true,
  "status": 200,
  "message": "요청이 성공적으로 처리되었습니다.",
  "data": {
    "loginId": "hongil123",
    "email": "honghong@khu.ac.kr",
    "name": "홍길동",
    "phoneNumber": "010-1234-4567",
    "studentId": "2026123456"
  }
}
```

인증 실패 응답:

```json
{
  "success": false,
  "status": 401,
  "message": "인증이 필요합니다.",
  "data": null
}
```

## 데이터 모델

`User` 엔티티는 PRD의 ERD에 맞춰 다음 필드를 가진다.

- `id`: 내부 PK. API 응답에는 포함하지 않는다.
- `loginId`: 사용자가 보는 로그인 ID. 회원정보 조회 응답에 포함한다.
- `studentId`: 전체 학번.
- `password`: BCrypt 해시. 이번 API 응답에는 포함하지 않는다.
- `name`: 이름.
- `phoneNumber`: 전화번호. DB 컬럼명은 PRD의 `phone` 의도를 반영하되 Java 필드는 `phoneNumber`로 둔다.
- `email`: 이메일.
- `emailVerified`: 이메일 인증 여부.
- `status`: `PENDING`, `APPROVED`, `WITHDRAWN`.
- `role`: `USER`, `ADMIN`.
- `createdAt`: 가입 신청 일시.
- `lastLoginAt`: 최근 로그인 일시.

회원정보 조회는 인증된 토큰의 subject인 `loginId`로 사용자를 조회한다. 조회 대상 사용자가 없거나 더 이상 유효한 회원으로 볼 수 없는 상태라면 인증 컨텍스트를 만들 수 없게 처리해 `401` 응답으로 통일한다.

## 인증 설계

기존 `JwtTokenProvider`를 사용한다. Auth API는 구현하지 않으며, 테스트에서는 `JwtTokenProvider.createAccessToken(loginId, role)`로 토큰을 직접 만든다.

JWT 필터 책임:

- `Authorization` 헤더에서 Bearer 토큰 추출
- 토큰 누락 또는 검증 실패 시 인증 컨텍스트를 만들지 않음
- 유효한 토큰이면 `loginId`로 사용자를 조회
- 사용자의 `status`가 `APPROVED`이고 role이 `USER` 또는 `ADMIN`이면 인증 설정
- 그 외 상태는 인증 실패로 처리

Security 설정:

- `/api/users/me`는 인증 필요
- H2 console과 Swagger 관련 경로는 개발 편의를 위해 허용
- 아직 구현하지 않은 다른 API 경로는 기존처럼 허용 상태를 유지한다

## 컴포넌트

`common`:

- `ApiResponse<T>`: 모든 API 응답 envelope.
- 실패 응답도 `ApiResponse<Void>`를 사용한다. `data`는 `null`로 직렬화한다.

`user`:

- `User`: JPA 엔티티.
- `UserRepository`: `findByLoginId`.
- `UserRole`, `UserStatus`: enum.

`security`:

- `JwtAuthenticationFilter`: 요청마다 Bearer 토큰을 검증하고 SecurityContext 설정.
- `CustomAuthenticationEntryPoint`: 인증 실패 시 공통 응답 구조로 401 반환.
- `SecurityConfig`: 필터와 endpoint 권한 설정 연결.

`mypage`:

- `MyPageController`: `GET /api/users/me`.
- `MyPageService`: 현재 인증 사용자의 회원정보 조회.
- `MyProfileResponse`: `loginId`, `email`, `name`, `phoneNumber`, `studentId`.

## 테스트 전략

TDD 순서로 진행한다.

1. 인증된 `USER` 토큰으로 요청하면 200과 명세의 `data` 필드를 반환하는 실패 테스트를 먼저 작성한다.
2. 토큰이 없으면 401 공통 응답을 반환하는 실패 테스트를 작성한다.
3. 유효하지 않은 토큰이면 401 공통 응답을 반환하는 실패 테스트를 작성한다.
4. `ADMIN` 토큰도 접근 가능함을 확인하는 테스트를 작성한다.
5. Swagger 스키마는 구현 후 문서 어노테이션이 response envelope와 `data` DTO를 명확히 드러내는지 코드 기준으로 확인한다.

테스트는 `MockMvc` 기반 통합 테스트로 작성한다. H2 메모리 DB에 사용자를 저장하고, 실제 `JwtTokenProvider`로 발급한 토큰을 사용해 인증 필터까지 검증한다.

## 성공 기준

- `GET /api/users/me` 성공 응답의 `data`는 `loginId`, `email`, `name`, `phoneNumber`, `studentId`만 포함한다.
- 성공 응답 envelope는 `success: true`, `status: 200`, `message`, `data` 구조를 가진다.
- 토큰 없음과 잘못된 토큰은 모두 `success: false`, `status: 401`, `message`, `data: null` 구조를 가진다.
- 명세에 없는 mypage 기능이나 Auth API를 구현하지 않는다.
- 모든 새 테스트가 실패를 확인한 뒤 구현되고, 최종적으로 통과한다.
