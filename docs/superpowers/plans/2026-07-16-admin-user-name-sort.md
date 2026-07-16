# Admin User Name Sort Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Support deterministic Korean name ascending order for `GET /api/admin/users` through `sort=name,asc` while preserving all existing response and sort behavior.

**Architecture:** Keep Spring's existing `Pageable` query contract. Normalize only requests containing `name ASC` inside `AdminService` by appending `id ASC`, then pass the normalized pageable through both the unfiltered and keyword-search repository paths. Document the accepted sort examples in the controller and checked-in OpenAPI snapshot.

**Tech Stack:** Java 21, Spring Boot 3.5.15, Spring Data JPA, springdoc-openapi 2.7.0, JUnit 5, Mockito, AssertJ, Gradle

## Global Constraints

- The public name-sort request is exactly `sort=name,asc`.
- Preserve the response type `CommonResponse<AdminUserListResponse>` and every existing response field.
- Preserve the default `createdAt DESC`, student-number sort, and recent-login sort behavior.
- Apply `id ASC` only as a deterministic tie-breaker for `name ASC` when an `id` order is not already present.
- Do not change the database schema, collation, frontend, or unrelated admin endpoints.

---

### Task 1: Specify name-sort normalization in service tests

**Files:**
- Create: `src/test/java/com/dcom/intranet/admin/service/AdminServiceTest.java`

**Interfaces:**
- Consumes: `AdminService.getUserList(String keyword, Pageable pageable)` and existing `UserRepository` list methods.
- Produces: executable requirements that `name ASC` becomes `name ASC, id ASC` in both repository branches while non-name sorts retain their original `Pageable`.

- [ ] **Step 1: Create the focused test fixture**

Create `AdminServiceTest` with Mockito mocks for all seven `AdminService` constructor dependencies and construct the service directly:

```java
private final UserRepository userRepository = mock(UserRepository.class);
private final NoticeRepository noticeRepository = mock(NoticeRepository.class);
private final PhotoPostRepository photoPostRepository = mock(PhotoPostRepository.class);
private final ArchiveRepository archiveRepository = mock(ArchiveRepository.class);
private final InfoPostRepository infoPostRepository = mock(InfoPostRepository.class);
private final EmailService emailService = mock(EmailService.class);

private final AdminService adminService = new AdminService(
        userRepository,
        noticeRepository,
        photoPostRepository,
        archiveRepository,
        infoPostRepository,
        emailService
);
```

- [ ] **Step 2: Write the failing no-keyword name-sort test**

```java
@Test
@DisplayName("Name ascending sort adds id ascending tie-breaker")
void nameAscendingSortAddsIdAscendingTieBreaker() {
    Pageable requested = PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "name"));
    when(userRepository.findAll(any(Pageable.class)))
            .thenAnswer(invocation -> Page.empty(invocation.getArgument(0)));

    adminService.getUserList(null, requested);

    ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
    verify(userRepository).findAll(captor.capture());
    assertThat(captor.getValue().getSort().stream().toList()).containsExactly(
            Sort.Order.asc("name"),
            Sort.Order.asc("id")
    );
}
```

- [ ] **Step 3: Write the failing keyword name-sort test**

```java
@Test
@DisplayName("Keyword search applies the same stable name sort")
void keywordSearchAppliesTheSameStableNameSort() {
    Pageable requested = PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "name"));
    when(userRepository.findByNameContainingOrLoginIdContainingOrStudentIdContaining(
            eq("김"), eq("김"), eq("김"), any(Pageable.class)
    )).thenAnswer(invocation -> Page.empty(invocation.getArgument(3)));

    adminService.getUserList("김", requested);

    ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
    verify(userRepository).findByNameContainingOrLoginIdContainingOrStudentIdContaining(
            eq("김"), eq("김"), eq("김"), captor.capture()
    );
    assertThat(captor.getValue().getSort().stream().toList()).containsExactly(
            Sort.Order.asc("name"),
            Sort.Order.asc("id")
    );
}
```

- [ ] **Step 4: Write the existing-sort regression test**

```java
@Test
@DisplayName("Non-name sort is passed to repository unchanged")
void nonNameSortIsPassedToRepositoryUnchanged() {
    Pageable requested = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "lastLoginAt"));
    when(userRepository.findAll(any(Pageable.class)))
            .thenAnswer(invocation -> Page.empty(invocation.getArgument(0)));

    adminService.getUserList(null, requested);

    ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
    verify(userRepository).findAll(captor.capture());
    assertThat(captor.getValue()).isSameAs(requested);
}
```

- [ ] **Step 5: Run the focused tests and verify the intended RED state**

Run:

```bash
./gradlew test --tests com.dcom.intranet.admin.service.AdminServiceTest
```

Expected: the two name-sort tests fail because the captured sort contains only `name ASC`; the non-name regression test passes.

- [ ] **Step 6: Commit the executable requirements**

```bash
git add src/test/java/com/dcom/intranet/admin/service/AdminServiceTest.java
git commit -m "test: specify admin user name sorting"
```

### Task 2: Implement stable name sorting and document the API contract

**Files:**
- Modify: `src/main/java/com/dcom/intranet/admin/service/AdminService.java:88`
- Modify: `src/main/java/com/dcom/intranet/admin/controller/AdminController.java:53`
- Modify: `docs/swagger/openapi.json:4221`

**Interfaces:**
- Consumes: Spring `Pageable`, `PageRequest`, and `Sort`.
- Produces: private `Pageable stabilizeNameSort(Pageable pageable)` used by `getUserList`; documented request examples for `studentId`, `lastLoginAt`, and `name`.

- [ ] **Step 1: Normalize the pageable before either repository path**

Add imports for `PageRequest` and `Sort`, normalize at the top of `getUserList`, and use the normalized value in both branches:

```java
@Transactional(readOnly = true)
public AdminUserListResponse getUserList(String keyword, Pageable pageable) {
    Pageable stablePageable = stabilizeNameSort(pageable);
    Page<User> users = (keyword == null || keyword.isBlank())
            ? userRepository.findAll(stablePageable)
            : userRepository.findByNameContainingOrLoginIdContainingOrStudentIdContaining(
                    keyword, keyword, keyword, stablePageable
            );
```

- [ ] **Step 2: Add the minimal stable-sort helper**

Add this private method near the other service helpers:

```java
private Pageable stabilizeNameSort(Pageable pageable) {
    Sort sort = pageable.getSort();
    Sort.Order nameOrder = sort.getOrderFor("name");

    if (nameOrder == null || !nameOrder.isAscending() || sort.getOrderFor("id") != null) {
        return pageable;
    }

    return PageRequest.of(
            pageable.getPageNumber(),
            pageable.getPageSize(),
            sort.and(Sort.by(Sort.Direction.ASC, "id"))
    );
}
```

- [ ] **Step 3: Run the focused tests and verify GREEN**

Run:

```bash
./gradlew test --tests com.dcom.intranet.admin.service.AdminServiceTest
```

Expected: all three tests pass with zero failures.

- [ ] **Step 4: Update the runtime Swagger description**

Replace the single-line operation description with:

```java
@Operation(
        summary = "회원 목록 조회",
        description = """
                전체 회원 목록을 조회합니다.

                정렬 예시:
                - 학번 오름차순: `sort=studentId,asc`
                - 최근 접속일 내림차순: `sort=lastLoginAt,desc`
                - 이름 가나다순: `sort=name,asc`
                """
)
```

- [ ] **Step 5: Update the checked-in OpenAPI snapshot**

Change only `/api/admin/users` GET `description` in `docs/swagger/openapi.json` to the newline-escaped equivalent:

```json
"description": "전체 회원 목록을 조회합니다.\n\n정렬 예시:\n- 학번 오름차순: `sort=studentId,asc`\n- 최근 접속일 내림차순: `sort=lastLoginAt,desc`\n- 이름 가나다순: `sort=name,asc`\n"
```

- [ ] **Step 6: Validate source formatting and the JSON snapshot**

Run:

```bash
git diff --check
python3 -m json.tool docs/swagger/openapi.json >/dev/null
```

Expected: both commands exit with status 0 and print no errors.

- [ ] **Step 7: Commit implementation and API documentation**

```bash
git add src/main/java/com/dcom/intranet/admin/service/AdminService.java src/main/java/com/dcom/intranet/admin/controller/AdminController.java docs/swagger/openapi.json
git commit -m "feat: add admin user name sorting"
```

### Task 3: Verify the complete change

**Files:**
- Verify: all files changed by Tasks 1 and 2

**Interfaces:**
- Consumes: completed feature and project test suite.
- Produces: fresh verification evidence for the focused behavior, all regressions, formatting, and the final diff.

- [ ] **Step 1: Run the full test suite**

Run:

```bash
./gradlew test
```

Expected: Gradle exits with `BUILD SUCCESSFUL` and reports zero failed tests.

- [ ] **Step 2: Re-run static artifact checks**

Run:

```bash
git diff --check HEAD~2..HEAD
python3 -m json.tool docs/swagger/openapi.json >/dev/null
```

Expected: both commands exit with status 0.

- [ ] **Step 3: Review repository state and commit history**

Run:

```bash
git status --short
git log -3 --oneline
```

Expected: no uncommitted files from this task; history contains the design, test, and implementation commits.
