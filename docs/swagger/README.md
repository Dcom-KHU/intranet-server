# Swagger 전달 안내

프론트엔드 전달용 Swagger 산출물입니다.

## 현재 전달 파일

- `docs/swagger/openapi.json`

이 파일은 백엔드 서버가 실행 중일 때 Springdoc이 자동 생성하는 `/v3/api-docs` 응답을 저장한 OpenAPI JSON입니다.

## 자동 생성 구조

이 프로젝트는 `build.gradle`에 Springdoc Swagger 의존성이 들어있습니다.

```groovy
implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.7.0'
```

Springdoc은 아래 코드를 읽어서 Swagger 문서를 자동 생성합니다.

- `@RestController`, `@GetMapping` 같은 Controller 정보
- `@Operation`, `@Tag` 같은 API 설명
- `@Schema`가 붙은 response DTO 필드 정보

즉, Swagger 문서를 손으로 직접 작성하는 것이 아니라 백엔드 코드의 Controller/DTO 구조를 기준으로 자동 생성합니다.

## 로컬에서 Swagger UI 확인하기

프로젝트 루트에서 서버를 실행합니다.

```bash
JAVA_HOME=/opt/homebrew/opt/openjdk@21 ./gradlew bootRun
```

브라우저에서 Swagger UI를 엽니다.

```text
http://localhost:8080/swagger-ui/index.html
```

OpenAPI JSON을 직접 확인하려면 아래 주소를 엽니다.

```text
http://localhost:8080/v3/api-docs
```

## openapi.json 갱신 방법

백엔드 API 코드나 DTO가 바뀌면 서버를 다시 실행한 뒤 아래 명령어로 JSON 파일을 갱신합니다.

```bash
curl -s http://localhost:8080/v3/api-docs -o docs/swagger/openapi.json
```

갱신 후 변경사항을 확인합니다.

```bash
git diff -- docs/swagger/openapi.json
```

## 프론트엔드 전달 방법

프론트엔드에는 아래 둘 중 하나를 전달하면 됩니다.

1. 배포된 백엔드 서버의 Swagger UI 주소
2. 저장된 `docs/swagger/openapi.json` 파일

아직 백엔드가 배포되지 않았거나 로컬 기능 확인만 필요한 단계라면 `docs/swagger/openapi.json` 파일을 전달하는 방식이 가장 단순합니다.

## Home API

프론트에서 연결할 엔드포인트:

```http
GET /api/home
```

응답 최상위 필드:

- `success`
- `status`
- `message`
- `data`

성공 응답의 홈 화면 데이터는 `data` 안에 들어갑니다.

```json
{
  "success": true,
  "status": 200,
  "message": "요청에 성공했습니다.",
  "data": {
    "recentNotices": [],
    "recentArchives": [],
    "recentInfoPosts": [],
    "recentPhotoAlbums": []
  }
}
```

명세에 포함된 응답 상태:

- `200`: 요청 성공
- `401`: 인증 필요

응답에서 제외한 필드:

- `userId`
- `role`
- `mainMenu`
- `recentAnnouncements`

`recentArchives` 항목은 `semester`를 포함하지 않고, `author` 객체와 `yyyy.MM.dd` 형식의 `date`를 제공합니다.
