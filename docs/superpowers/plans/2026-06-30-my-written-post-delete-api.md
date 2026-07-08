# 내가 쓴 글 삭제 API Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 최종 API 명세서 기준으로 `DELETE /api/users/me/posts/{postId}` 내가 쓴 글 삭제 API를 구현한다.

**Architecture:** 기존 mypage 내가 쓴 글 목록/상세 이동과 같은 `MyWrittenPostReader` 포트를 확장한다. Controller와 Service는 인증 사용자 확인과 공통 응답 생성을 담당하고, 실제 도메인별 존재 여부, 작성자 권한, 삭제 수행은 reader 구현체가 담당한다.

**Tech Stack:** Java 21, Spring Boot 3.5.15, Spring MVC, Spring Security, Spring Data JPA, H2, JUnit 5, MockMvc, springdoc-openapi.

---

## 참조 문서

- 설계 문서: `docs/superpowers/specs/2026-06-30-my-written-post-delete-design.md`
- 최종 API 명세서 기준: `DELETE /api/users/me/posts/{postId}`, query `type`, 응답 `message`, 상태코드 `200`, `401`, `403`, `404`
- 지원 type: `info-posts`, `archives`, `photo-posts`, `notices`

## 파일 구조

- Modify: `src/test/java/com/dcom/intranet/mypage/MyPageControllerTest.java`
  - 삭제 API의 성공, 인증 실패, 권한 없음, 대상 없음 테스트를 추가한다.
  - 테스트용 `MyWrittenPostReader`에 삭제 응답과 호출 기록을 추가한다.
- Modify: `src/main/java/com/dcom/intranet/mypage/MyWrittenPostReader.java`
  - 삭제용 `delete` 메서드를 추가한다.
- Modify: `src/main/java/com/dcom/intranet/mypage/EmptyMyWrittenPostReader.java`
  - 삭제 기본 구현으로 404를 반환한다.
- Create: `src/main/java/com/dcom/intranet/mypage/dto/MyWrittenPostDeleteResponse.java`
  - 성공 응답 `data` 구조를 정의한다.
- Create: `src/main/java/com/dcom/intranet/mypage/dto/MyWrittenPostDeleteApiResponse.java`
  - Swagger에서 성공 응답 envelope와 `data` 구조가 보이도록 wrapper schema를 정의한다.
- Create: `src/main/java/com/dcom/intranet/mypage/dto/ForbiddenApiResponse.java`
  - Swagger에서 403 공통 실패 응답 구조가 보이도록 wrapper schema를 정의한다.
- Modify: `src/main/java/com/dcom/intranet/mypage/MyPageService.java`
  - 사용자 조회 후 `MyWrittenPostReader`에 `userId`, `postId`, `type`을 전달한다.
- Modify: `src/main/java/com/dcom/intranet/mypage/MyPageController.java`
  - `DELETE /api/users/me/posts/{postId}` endpoint와 Swagger response annotation을 추가한다.
- Modify: `docs/openapi.json`
  - 삭제 API operation과 schema를 반영한다.

## Task 1: RED - 삭제 API 테스트 추가

- [ ] `MyPageControllerTest`에 `MyWrittenPostDeleteResponse` import를 추가한다.
- [ ] static import에 `MockMvcRequestBuilders.delete`를 추가한다.
- [ ] 테스트용 `TestMyWrittenPostReader`에 delete response, forbidden flag, not-found flag를 추가한다.
- [ ] `info-posts` 삭제 성공 테스트를 추가한다.
- [ ] `archives`, `photo-posts`, `notices` type 전달 테스트를 추가한다.
- [ ] 토큰 없음 `401` 테스트를 추가한다.
- [ ] 권한 없음 `403` 테스트를 추가한다.
- [ ] 대상 없음 `404` 테스트를 추가한다.
- [ ] `./gradlew test --rerun-tasks --tests com.dcom.intranet.mypage.MyPageControllerTest`를 실행해 컴파일 또는 테스트 실패를 확인한다.

## Task 2: GREEN - 최소 구현 추가

- [ ] `MyWrittenPostDeleteResponse` record를 추가한다.
- [ ] `MyWrittenPostDeleteApiResponse` Swagger wrapper를 추가한다.
- [ ] `ForbiddenApiResponse` Swagger wrapper를 추가한다.
- [ ] `MyWrittenPostReader`에 `delete(Long userId, Long postId, String type)`를 추가한다.
- [ ] `EmptyMyWrittenPostReader`의 `delete`는 `MyPageApiException(HttpStatus.NOT_FOUND, "작성한 글을 찾을 수 없습니다.")`를 던진다.
- [ ] `MyPageService.deleteMyPost`를 추가한다.
- [ ] `MyPageController.deleteMyPost`를 추가한다.
- [ ] `./gradlew test --rerun-tasks --tests com.dcom.intranet.mypage.MyPageControllerTest`를 실행해 통과를 확인한다.

## Task 3: OpenAPI 반영

- [ ] `docs/openapi.json`의 `/api/users/me/posts/{postId}` path에 `delete` operation을 추가한다.
- [ ] `MyWrittenPostDeleteApiResponse`, `MyWrittenPostDeleteResponse`, `ForbiddenApiResponse` schema를 추가한다.
- [ ] JSON 들여쓰기와 줄바꿈을 유지한다.
- [ ] `JSON.parse(require("fs").readFileSync("docs/openapi.json", "utf8"))`로 JSON 문법을 확인한다.
- [ ] `./gradlew test --rerun-tasks`를 실행한다.

## Task 4: Review

- [ ] 변경 diff를 읽고 범위가 삭제 API에 한정되는지 확인한다.
- [ ] 모든 명세 상태코드 `200`, `401`, `403`, `404`가 테스트와 OpenAPI에 반영됐는지 확인한다.
- [ ] code-review 단계에서 발견한 중요 이슈가 있으면 TDD 흐름으로 수정한다.
