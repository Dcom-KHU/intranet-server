# 내가 쓴 글 상세 이동 API Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 최종 API 명세서 기준으로 `GET /api/users/me/posts/{postId}` 내가 쓴 글 상세 이동 API를 구현한다.

**Architecture:** 기존 mypage 목록 조회와 같은 `MyWrittenPostReader` 포트를 확장한다. Controller와 Service는 인증 사용자 확인과 공통 응답 생성을 담당하고, 실제 도메인별 작성자 검증과 target 매핑은 reader 구현체가 담당한다.

**Tech Stack:** Java 21, Spring Boot 3.5.15, Spring MVC, Spring Security, Spring Data JPA, H2, JUnit 5, MockMvc, springdoc-openapi.

---

## 참조 문서

- 설계 문서: `docs/superpowers/specs/2026-06-29-my-written-post-detail-navigation-design.md`
- 최종 API 명세서 기준: `GET /api/users/me/posts/{postId}`, query `type`, 응답 `targetType`, `targetId`, 상태코드 `200`, `401`, `404`
- 지원 target type: `info-posts`, `archives`, `photo-posts`, `notices`

## 파일 구조

- Modify: `src/test/java/com/dcom/intranet/mypage/MyPageControllerTest.java`
  - 상세 이동 API의 성공, 인증 실패, 404 테스트를 추가한다.
  - 테스트용 `MyWrittenPostReader`에 상세 이동 응답과 호출 기록을 추가한다.
- Modify: `src/main/java/com/dcom/intranet/mypage/MyWrittenPostReader.java`
  - 상세 이동용 `readTarget` 메서드를 추가한다.
- Modify: `src/main/java/com/dcom/intranet/mypage/EmptyMyWrittenPostReader.java`
  - 상세 이동 기본 구현으로 404를 반환한다.
- Create: `src/main/java/com/dcom/intranet/mypage/dto/MyWrittenPostTargetResponse.java`
  - 성공 응답 `data` 구조를 정의한다.
- Create: `src/main/java/com/dcom/intranet/mypage/dto/MyWrittenPostTargetApiResponse.java`
  - Swagger에서 성공 응답 envelope와 `data` 구조가 보이도록 wrapper schema를 정의한다.
- Create: `src/main/java/com/dcom/intranet/mypage/dto/NotFoundApiResponse.java`
  - Swagger에서 404 공통 실패 응답 구조가 보이도록 wrapper schema를 정의한다.
- Modify: `src/main/java/com/dcom/intranet/mypage/MyPageService.java`
  - 사용자 조회 후 `MyWrittenPostReader`에 `userId`, `postId`, `type`을 전달한다.
- Modify: `src/main/java/com/dcom/intranet/mypage/MyPageController.java`
  - `GET /api/users/me/posts/{postId}` endpoint와 Swagger response annotation을 추가한다.
- Modify: `docs/openapi.json`
  - 상세 이동 API path와 schema를 반영한다.

## Task 1: RED - 상세 이동 API 테스트 추가

- [ ] `MyPageControllerTest`에 `MyWrittenPostTargetResponse` import를 추가한다.
- [ ] 테스트용 `TestMyWrittenPostReader`에 target response, not-found flag, last post ID 필드를 추가한다.
- [ ] `info-posts`, `archives`, `photo-posts`, `notices` 성공 테스트를 추가한다.
- [ ] 토큰 없음 `401` 테스트를 추가한다.
- [ ] 대상 없음 `404` 테스트를 추가한다.
- [ ] `./gradlew test --tests com.dcom.intranet.mypage.MyPageControllerTest`를 실행해 컴파일 또는 테스트 실패를 확인한다.

## Task 2: GREEN - 최소 구현 추가

- [ ] `MyWrittenPostTargetResponse` record를 추가한다.
- [ ] `MyWrittenPostTargetApiResponse` Swagger wrapper를 추가한다.
- [ ] `NotFoundApiResponse` Swagger wrapper를 추가한다.
- [ ] `MyWrittenPostReader`에 `readTarget(Long userId, Long postId, String type)`를 추가한다.
- [ ] `EmptyMyWrittenPostReader`의 `readTarget`은 `MyPageApiException(HttpStatus.NOT_FOUND, "작성한 글을 찾을 수 없습니다.")`를 던진다.
- [ ] `MyPageService.getMyPostTarget`을 추가한다.
- [ ] `MyPageController.getMyPostTarget`을 추가한다.
- [ ] `./gradlew test --tests com.dcom.intranet.mypage.MyPageControllerTest`를 실행해 통과를 확인한다.

## Task 3: OpenAPI 반영

- [ ] `docs/openapi.json`에 `/api/users/me/posts/{postId}` path를 추가한다.
- [ ] `MyWrittenPostTargetApiResponse`, `MyWrittenPostTargetResponse`, `NotFoundApiResponse` schema를 추가한다.
- [ ] JSON 들여쓰기와 줄바꿈을 유지한다.
- [ ] `./gradlew test --tests com.dcom.intranet.mypage.MyPageControllerTest`를 다시 실행한다.

## Task 4: Review

- [ ] 변경 diff를 읽고 범위가 상세 이동 API에 한정되는지 확인한다.
- [ ] 모든 명세 상태코드 `200`, `401`, `404`가 테스트와 OpenAPI에 반영됐는지 확인한다.
- [ ] code-review 단계에서 발견한 중요 이슈가 있으면 TDD 흐름으로 수정한다.
