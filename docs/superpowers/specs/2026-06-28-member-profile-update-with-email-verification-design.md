# 회원정보 수정 및 이메일 변경 인증 API 설계

## 배경

이번 작업은 mypage 파트 중 회원정보 수정 기능을 구현한다. `최종_API_명세서.pdf`를 최우선 기준으로 삼고, `D COM 인트라넷 개편 프로젝트 개요.pdf`와 `mypage_front` 이미지는 기능 의도와 프론트엔드 데이터 요구를 이해하기 위한 보조 자료로만 사용한다.

API 명세서상 회원정보 수정 API는 `emailChangeToken`을 요청값으로 받는다. 이 토큰은 별도 이메일 변경 인증 API에서 발급되므로, 회원정보 수정 API를 안전하게 구현하려면 다음 순서가 필요하다.

1. 이메일 변경 인증 메일 발송 API
2. 이메일 변경 인증 확인 API
3. 회원정보 수정 API

실제 SMTP 또는 외부 메일 발송 서비스는 아직 정해지지 않았다. 따라서 이번 작업은 인증 코드 생성, 저장, 검증, 이메일 변경 토큰 발급, 회원정보 수정까지 서버 내부 흐름을 완성하고, 실제 이메일 발송은 추후 연결할 수 있도록 설계만 남긴다.

## 범위

구현 대상 API는 세 개다.

- `POST /api/users/me/email/verification/send`
- `POST /api/users/me/email/verification/verify`
- `PATCH /api/users/me/settings`

포함한다:

- 이메일 변경 인증 요청 저장 엔티티와 Repository
- 인증 코드 생성과 만료시간 저장
- 인증 코드 검증과 이메일 변경 토큰 발급
- 회원정보 수정 시 이름, 전화번호, 검증된 이메일 변경
- 공통 응답 envelope
- 명세 상태코드 `200`, `400`, `401`, `409`, `410` 응답 구조
- 이메일 변경 인증 메일 발송 API의 `429` 재요청 제한 응답
- Swagger에서 성공/실패 response data 구조가 보이도록 하는 DTO
- 추후 SMTP/메일 서비스 연결 방법 문서화

포함하지 않는다:

- 실제 이메일 발송
- SMTP, SendGrid, AWS SES 같은 외부 메일 서비스 설정
- 로그인, 회원가입, 비밀번호 변경, 회원탈퇴, 내가 쓴 글 API
- 이메일 도메인 제한
- 횟수 기반 또는 시간대 기반의 복잡한 재발송 제한 정책
- 오래된 인증 요청 정리 배치

## API 계약

모든 응답은 다음 공통 구조를 따른다.

```json
{
  "success": true,
  "status": 200,
  "message": "요청이 성공적으로 처리되었습니다.",
  "data": {}
}
```

실패 응답도 같은 envelope를 사용하며, `data`는 `null`이다.

### 이메일 변경 인증 메일 발송

Endpoint:

```http
POST /api/users/me/email/verification/send
Authorization: Bearer <access-token>
Content-Type: application/json
```

Request:

```json
{
  "newEmail": "newhong@khu.ac.kr"
}
```

성공 응답:

```json
{
  "success": true,
  "status": 200,
  "message": "요청이 성공적으로 처리되었습니다.",
  "data": {
    "message": "이메일 변경 인증 코드가 생성되었습니다.",
    "expiresIn": 300
  }
}
```

상태코드:

- `200`: 인증 코드 생성 성공
- `400`: `newEmail` 누락, 빈 값, 이메일 형식 오류
- `401`: 인증 실패
- `409`: 이미 다른 사용자가 사용 중인 이메일
- `429`: 같은 사용자와 같은 이메일에 대해 아직 만료되지 않은 미사용 인증 요청이 이미 있음

이번 구현에서는 실제 메일 발송이 없으므로 테스트에서 저장된 인증 코드를 Repository로 조회해 검증 API에 전달한다.

### 이메일 변경 인증 확인

Endpoint:

```http
POST /api/users/me/email/verification/verify
Authorization: Bearer <access-token>
Content-Type: application/json
```

Request:

```json
{
  "newEmail": "newhong@khu.ac.kr",
  "verificationCode": "123456"
}
```

성공 응답:

```json
{
  "success": true,
  "status": 200,
  "message": "요청이 성공적으로 처리되었습니다.",
  "data": {
    "emailChangeToken": "generated-email-change-token",
    "message": "이메일 변경 인증이 완료되었습니다.",
    "verifiedEmail": "newhong@khu.ac.kr"
  }
}
```

상태코드:

- `200`: 인증 확인 성공
- `400`: 요청값 누락, 코드 불일치, 인증 요청 없음
- `401`: 인증 실패
- `410`: 인증 코드 만료

검증 성공 시 같은 인증 요청에 이메일 변경 토큰을 저장한다. 이 토큰은 회원정보 수정 API에서 이메일 변경을 최종 적용할 때 사용한다.

### 회원정보 수정

Endpoint:

```http
PATCH /api/users/me/settings
Authorization: Bearer <access-token>
Content-Type: application/json
```

Request:

```json
{
  "name": "홍길동",
  "phoneNumber": "010-9999-8888",
  "emailChangeToken": "generated-email-change-token"
}
```

`emailChangeToken`은 선택값이다. 이름과 전화번호만 수정할 때는 생략할 수 있다.

성공 응답:

```json
{
  "success": true,
  "status": 200,
  "message": "요청이 성공적으로 처리되었습니다.",
  "data": {
    "name": "홍길동",
    "studentId": "2026123456",
    "email": "newhong@khu.ac.kr",
    "phoneNumber": "010-9999-8888",
    "message": "회원정보가 수정되었습니다."
  }
}
```

상태코드:

- `200`: 회원정보 수정 성공
- `400`: 이름 또는 전화번호 값 오류, 이메일 변경 토큰 불일치
- `401`: 인증 실패
- `409`: 이메일 변경 토큰의 이메일이 이미 다른 사용자에게 사용됨
- `410`: 이메일 변경 토큰 만료

`loginId`와 `studentId`는 수정하지 않는다. `studentId`는 응답에만 포함한다.

## 데이터 모델

기존 `User` 엔티티는 유지하되, 회원정보 수정에 필요한 메서드만 추가한다.

- `updateProfile(String name, String phoneNumber)`
- `changeEmail(String email)`

새 `EmailVerification` 엔티티를 추가한다.

- `id`: 내부 PK
- `loginId`: 인증 요청을 만든 사용자 로그인 ID
- `email`: 인증 대상 새 이메일
- `verificationCode`: 6자리 인증 코드
- `emailChangeToken`: 인증 성공 후 발급되는 이메일 변경 토큰
- `expiresAt`: 인증 코드와 이메일 변경 토큰 만료시각
- `verified`: 인증 코드 검증 성공 여부
- `used`: 회원정보 수정에 사용 완료 여부
- `createdAt`: 생성시각

`EmailVerificationRepository`는 다음 조회가 필요하다.

- `findTopByLoginIdAndEmailOrderByCreatedAtDesc`
- `findByEmailChangeToken`

이메일 중복 검사는 `UserRepository.existsByEmail`을 추가해 처리한다.

## 인증과 권한

세 API 모두 `USER` 권한이 필요하다. 기존 구현처럼 `ADMIN`도 접근할 수 있다.

`SecurityConfig`는 다음 경로를 인증 대상으로 확장한다.

- `/api/users/me`
- `/api/users/me/settings`
- `/api/users/me/email/verification/send`
- `/api/users/me/email/verification/verify`

JWT 인증 방식과 `CustomAuthenticationEntryPoint`의 `401` 응답 구조는 기존 구현을 유지한다.

## 예외 처리

현재 `401`은 `CustomAuthenticationEntryPoint`가 공통 응답으로 처리한다. 새 API는 `400`, `409`, `410`도 명세에 맞는 공통 envelope로 내려야 하므로, mypage에서 사용할 전역 예외 처리 구조를 추가한다.

추가할 예외 처리:

- `MethodArgumentNotValidException`: `400`, `"요청값이 올바르지 않습니다."`
- `MyPageApiException`: 지정된 HTTP 상태와 메시지

`MyPageApiException`은 다음 상황에 사용한다.

- 인증 요청 없음: `400`
- 인증 코드 불일치: `400`
- 이메일 변경 토큰 불일치: `400`
- 이메일 중복: `409`
- 인증 코드 또는 이메일 변경 토큰 만료: `410`
- 아직 만료되지 않은 같은 이메일 인증 요청 재생성: `429`

## 컴포넌트

`common`:

- `ApiResponse<T>`: 기존 공통 응답 envelope를 그대로 사용한다.
- `GlobalExceptionHandler`: validation과 mypage 도메인 예외를 공통 envelope로 변환한다.

`user`:

- `User`: 이름, 전화번호, 이메일 변경 메서드 추가.
- `UserRepository`: `existsByEmail` 추가.

`mypage`:

- `MyPageController`: 세 API endpoint 추가.
- `MyPageService`: 회원정보 수정 orchestration.
- `EmailVerificationService`: 인증 코드 생성, 인증 코드 검증, 이메일 변경 토큰 검증.
- `EmailVerification`: 이메일 변경 인증 엔티티.
- `EmailVerificationRepository`: 인증 요청 조회.
- request/response DTO: API별 요청값과 응답 data를 명확히 표현한다.
- Swagger response wrapper DTO: `data` 구조가 Swagger Editor에서 보이도록 API별 성공 응답과 실패 응답을 분리한다.

## 테스트 전략

모든 구현은 TDD 순서로 진행한다.

### 이메일 변경 인증 메일 발송 API

먼저 실패 테스트를 작성한다.

- 인증된 사용자가 새 이메일을 요청하면 `200`과 `message`, `expiresIn`을 반환한다.
- 요청 후 Repository에 인증 코드와 만료시간이 저장된다.
- `newEmail`이 빈 값이면 `400` 공통 응답을 반환한다.
- 이미 사용 중인 이메일이면 `409` 공통 응답을 반환한다.
- 같은 사용자와 같은 이메일에 대해 아직 만료되지 않은 미사용 인증 요청이 있으면 `429` 공통 응답을 반환한다.
- 토큰이 없으면 `401` 공통 응답을 반환한다.

### 이메일 변경 인증 확인 API

먼저 실패 테스트를 작성한다.

- 저장된 인증 코드가 일치하면 `200`과 `emailChangeToken`, `message`, `verifiedEmail`을 반환한다.
- 인증 코드가 틀리면 `400` 공통 응답을 반환한다.
- 인증 요청이 없으면 `400` 공통 응답을 반환한다.
- 인증 코드가 만료되면 `410` 공통 응답을 반환한다.
- 토큰이 없으면 `401` 공통 응답을 반환한다.

### 회원정보 수정 API

먼저 실패 테스트를 작성한다.

- 이름과 전화번호만 수정하면 `200`과 수정된 data를 반환하고 이메일은 유지한다.
- 검증된 `emailChangeToken`을 함께 보내면 이메일까지 변경한다.
- 잘못된 `emailChangeToken`이면 `400` 공통 응답을 반환한다.
- 만료된 `emailChangeToken`이면 `410` 공통 응답을 반환한다.
- 토큰의 이메일이 이미 다른 사용자에게 사용 중이면 `409` 공통 응답을 반환한다.
- `studentId`와 `loginId`는 요청에 포함되어도 변경되지 않는다.
- 토큰이 없으면 `401` 공통 응답을 반환한다.

테스트는 `MockMvc` 기반 통합 테스트로 작성한다. 실제 JWT 필터, H2 DB, Repository를 사용해 API 계약을 검증한다.

## 추후 SMTP/메일 서비스 연결 가이드

현재 구현은 실제 이메일 발송 없이 인증 코드 생성과 저장까지만 수행한다. SMTP 또는 외부 메일 서비스가 정해지면 아래 순서로 연결한다.

### 1. 메일 발송 인터페이스 추가

메일 서비스와 도메인 로직을 분리하기 위해 인터페이스를 먼저 둔다.

```java
public interface EmailSender {
    void sendEmailChangeVerificationCode(String to, String verificationCode, long expiresIn);
}
```

`EmailVerificationService`는 구체적인 SMTP 구현체를 알지 않고 `EmailSender`만 의존한다.

### 2. 운영 메일 구현체 추가

선택한 서비스에 따라 구현체를 만든다.

- SMTP: `SmtpEmailSender`
- SendGrid: `SendGridEmailSender`
- AWS SES: `SesEmailSender`

구현체는 인증 코드, 만료시간, 서비스 이름이 포함된 메일 본문을 만든 뒤 발송한다.

### 3. 설정값은 외부 secret으로 관리

다음 값은 git에 커밋하지 않는다.

- SMTP host
- SMTP port
- SMTP username
- SMTP password
- API key
- 발신자 이메일

로컬 개발은 `.env`나 개인 `application-local.yml`을 사용하고, 운영 환경은 배포 환경의 secret manager 또는 환경변수를 사용한다.

### 4. 트랜잭션 정책 결정

메일 발송을 연결할 때는 저장과 발송 실패의 정책을 정해야 한다.

권장 기본 정책:

1. 인증 요청을 DB에 저장한다.
2. 메일 발송을 시도한다.
3. 발송 실패 시 `500` 또는 별도 운영 정책에 맞는 오류를 반환한다.

더 엄격하게 하려면 발송 실패 시 저장된 인증 요청을 무효 처리하거나 삭제한다. 다만 외부 메일 서비스는 네트워크 실패가 있을 수 있으므로, 운영에서는 재시도 정책이나 outbox 패턴을 별도로 검토한다.

### 5. 테스트 전략

운영 메일 구현체를 직접 호출하는 테스트는 통합 테스트로 분리한다. 일반 API 테스트에서는 fake sender를 사용한다.

```java
public class FakeEmailSender implements EmailSender {
    private String lastTo;
    private String lastCode;

    @Override
    public void sendEmailChangeVerificationCode(String to, String verificationCode, long expiresIn) {
        this.lastTo = to;
        this.lastCode = verificationCode;
    }
}
```

API 테스트는 다음만 검증한다.

- send API 호출 시 `EmailSender`가 호출된다.
- 수신 이메일이 `newEmail`과 같다.
- 저장된 인증 코드와 발송된 인증 코드가 같다.
- 메일 발송 실패 시 정해진 오류 응답이 내려간다.

## 성공 기준

- 세 API가 명세 endpoint, request, response data, 상태코드를 따른다.
- 모든 성공/실패 응답은 공통 envelope를 따른다.
- send API는 명세의 `429` 상태를 같은 사용자/이메일의 활성 인증 요청 중복 생성 방지로 처리한다.
- Swagger에서 각 API의 `data` 구조가 명확히 보인다.
- 실제 이메일 발송 없이도 인증 코드 생성, 검증, 이메일 변경 토큰 적용 흐름이 테스트로 검증된다.
- `loginId`와 `studentId`는 수정되지 않는다.
- 명세에 없는 mypage 기능은 구현하지 않는다.
- 모든 테스트는 실패를 먼저 확인한 뒤 최소 구현으로 통과시킨다.
