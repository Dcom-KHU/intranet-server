# Password Change API Design

## Scope

Implement only the My Page password change API.

- Endpoint: `PATCH /api/users/me/password`
- Auth: authenticated `USER` or `ADMIN`, matching the existing `/api/users/me/**` security rule
- Request body: `currentPassword`, `newPassword`
- Success data: `{ "message": "비밀번호가 변경되었습니다." }`
- Status codes from the final API spec: `200`, `400`, `401`

Already completed My Page APIs stay out of scope:

- `GET /api/users/me`
- `POST /api/users/me/email/verification/send`
- `POST /api/users/me/email/verification/verify`
- `PATCH /api/users/me/settings`

Future My Page APIs, including withdrawal and post/comment lists, stay out of scope.

## Assumptions

- The final API spec is the source of truth.
- The PRD and `mypage_front` password-change screens explain intent, but do not add server fields beyond the final API spec.
- The frontend handles new-password confirmation because the API spec explicitly notes that confirmation is handled on the frontend.
- Server-side validation is limited to what is needed by the spec: required request fields, current password verification, and authentication.
- Password hashing uses the existing Spring Security `PasswordEncoder` bean.
- The existing common response envelope remains unchanged:

```json
{
  "success": true,
  "status": 200,
  "message": "요청이 성공적으로 처리되었습니다.",
  "data": {}
}
```

## Considered Approaches

### Recommended: Spec-minimal server validation

The server accepts `currentPassword` and `newPassword`, rejects blank values with `400`, verifies the current password with `PasswordEncoder.matches`, stores the new password with `PasswordEncoder.encode`, and returns a message-only response.

This matches the final API spec without adding password complexity rules that are not documented there.

### Alternative: Also enforce frontend password complexity

The password-change screen says the new password should combine lowercase English letters and numbers with at least 8 characters. Enforcing this on the backend would improve consistency with the current UI copy, but it would add behavior not defined by the final API spec.

This is not recommended for the current task.

### Alternative: Require `newPasswordConfirm` in the API

The PRD and screen include new-password confirmation. Adding the field would mirror the UI, but the final API spec says the request only contains `currentPassword` and `newPassword`, and notes that confirmation is handled by the frontend.

This is not recommended.

## API Behavior

### Success: `200`

When an approved authenticated user sends the correct current password and a non-blank new password:

- The stored password is replaced with a BCrypt hash of `newPassword`.
- The response uses the common success envelope.
- The response `data` object contains only a message.
- The raw new password is never returned.

Expected response shape:

```json
{
  "success": true,
  "status": 200,
  "message": "요청이 성공적으로 처리되었습니다.",
  "data": {
    "message": "비밀번호가 변경되었습니다."
  }
}
```

### Validation failure: `400`

Return the existing bad-request common envelope when:

- `currentPassword` is blank or missing.
- `newPassword` is blank or missing.
- `currentPassword` does not match the stored password hash.

Blank-field validation should use the existing `MethodArgumentNotValidException` path and message:

```json
{
  "success": false,
  "status": 400,
  "message": "요청값이 올바르지 않습니다.",
  "data": null
}
```

Wrong current password should use `MyPageApiException` with:

```json
{
  "success": false,
  "status": 400,
  "message": "현재 비밀번호가 올바르지 않습니다.",
  "data": null
}
```

### Authentication failure: `401`

Missing token, invalid token, and non-approved users continue to be handled by the existing Spring Security/JWT path.

Expected response shape:

```json
{
  "success": false,
  "status": 401,
  "message": "인증이 필요합니다.",
  "data": null
}
```

## Code Design

Add the smallest set of production changes that follows the existing My Page pattern.

- Add `PasswordChangeRequest` under `src/main/java/com/dcom/intranet/mypage/dto/`.
  - Record fields: `currentPassword`, `newPassword`
  - Validation: `@NotBlank` on both fields
  - Swagger schema examples only
- Add `PasswordChangeResponse`.
  - Record field: `message`
  - Static factory is optional; direct construction is enough.
- Add `PasswordChangeApiResponse`.
  - Same wrapper style as `MyProfileUpdateApiResponse`
  - `data` type is `PasswordChangeResponse`
- Extend `MyPageService`.
  - Inject `PasswordEncoder`.
  - Add `changePassword(String loginId, PasswordChangeRequest request)`.
  - Load the user by login ID as current methods do.
  - If the user is missing, throw `ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증이 필요합니다.")`.
  - If `PasswordEncoder.matches` fails, throw `MyPageApiException(HttpStatus.BAD_REQUEST, "현재 비밀번호가 올바르지 않습니다.")`.
  - Encode and store the new password.
- Extend `User`.
  - Add one domain method: `changePassword(String encodedPassword)`.
  - Do not expose new password data.
- Extend `MyPageController`.
  - Add `@PatchMapping("/me/password")`.
  - Return `ResponseEntity<ApiResponse<PasswordChangeResponse>>`.
  - Add Swagger responses for `200`, `400`, and `401`.

No new service or abstraction is needed.

## Testing Design

Use the existing `MyPageControllerTest` style with `@SpringBootTest`, `MockMvc`, JWT tokens, and repository assertions.

Test first, in this order:

1. Successful password change returns `200`, common success envelope, and message data.
2. Successful password change stores an encoded password that matches the new password and no longer matches the old password.
3. Wrong current password returns `400` common envelope with `현재 비밀번호가 올바르지 않습니다.`.
4. Blank `currentPassword` returns `400` common envelope with `요청값이 올바르지 않습니다.`.
5. Blank `newPassword` returns `400` common envelope with `요청값이 올바르지 않습니다.`.
6. Missing token returns `401` common envelope with `인증이 필요합니다.`.

The existing `saveUser` helper should be adjusted to store a BCrypt-encoded default password so existing tests continue to work while password-change tests exercise real password verification.

## Swagger/OpenAPI

The controller annotations must make Swagger Editor show:

- `200` response with `success`, `status`, `message`, and `data.message`
- `400` failure response with nullable `data`
- `401` failure response with nullable `data`

After implementation, regenerate or update `docs/openapi.json` using the same project workflow already used for the previous My Page APIs.

## Out Of Scope

- Password reset through email
- Password confirmation request field
- Password complexity policy beyond non-blank validation
- Forced logout or token revocation after password change
- Email notification after password change
- Any My Page APIs other than password change

## Success Criteria

- All password-change tests are written before production code and are observed failing for the expected missing-feature reason.
- The implementation passes the password-change tests.
- Existing My Page tests still pass.
- All response envelopes follow `ApiResponse<T>`.
- Swagger response schema clearly shows the success `data` object and all spec-defined status codes.
