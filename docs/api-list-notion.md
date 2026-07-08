| 영역 | URL | Method | 설명 | 인증 필요 |
| --- | --- | --- | --- | --- |
| auth | /api/auth/signup | POST | 회원가입 요청 | No |
| auth | /api/auth/check-login-id | GET | 아이디 중복 확인 | No |
| auth | /api/auth/login | POST | 로그인 | No |
| auth | /api/auth/me | GET | 로그인 상태 확인 | Yes |
| auth | /api/auth/email/send | POST | 이메일 인증코드 발송 | No |
| auth | /api/auth/email/verify | POST | 이메일 인증코드 확인 | No |
| auth | /api/auth/refresh | POST | 토큰 재발급 | No |
| auth | /api/auth/logout | POST | 로그아웃 | No |
| auth | /api/auth/password/reset/send | POST | 임시 비밀번호 발송 | No |
| auth | /api/auth/password | POST | 비밀번호 재설정 | Yes |
| home | /api/home | GET | 메인 대시보드 조회 | Yes |
| admin | /api/admin/me | GET | 관리자 콘솔 접근 확인 | Yes |
| admin | /api/admin/dashboard | GET | 관리자 대시보드 조회 | Yes |
| admin | /api/admin/users | GET | 회원 목록 조회 | Yes |
| admin | /api/admin/users/pending | GET | 가입 승인 대상 조회 | Yes |
| admin | /api/admin/users/{userId} | GET | 회원 상세 조회 | Yes |
| admin | /api/admin/users/{userId}/approve | PATCH | 가입 승인 | Yes |
| admin | /api/admin/users/{userId}/reject | PATCH | 가입 거절 | Yes |
| admin | /api/admin/users/{userId}/transfer-admin | PATCH | 관리자 권한 이양 | Yes |
| notice | /api/notice | GET | 공지사항 목록 조회 | Yes |
| notice | /api/notice/{noticeId} | GET | 공지사항 상세 조회 | Yes |
| notice | /api/notice | POST | 공지사항 작성 | Yes |
| notice | /api/notice/{noticeId} | PUT | 공지사항 수정 | Yes |
| notice | /api/notice/{noticeId} | DELETE | 공지사항 삭제 | Yes |
| archive | /api/archives | GET | 족보 목록 조회 | Yes |
| archive | /api/archives/search | GET | 족보 검색 | Yes |
| archive | /api/archives/{archiveId} | GET | 족보 상세 조회 | Yes |
| archive | /api/archives | POST | 족보 등록 | Yes |
| archive | /api/archives/{archiveId}/records/{recordId} | PUT | 족보 수정 | Yes |
| archive | /api/archives/{archiveId}/records/{recordId} | DELETE | 족보 삭제 | Yes |
| archive | /api/archives/{archiveId}/records/{recordId}/files/{fileId}/download | GET | 족보 파일 다운로드 | Yes |
| info | /api/info-posts | GET | 정보공유 게시글 목록 조회 | Yes |
| info | /api/info-posts/{postId} | GET | 정보공유 게시글 상세 조회 | Yes |
| info | /api/info-posts | POST | 정보공유 게시글 작성 | Yes |
| info | /api/info-posts/{postId} | PUT | 정보공유 게시글 수정 | Yes |
| info | /api/info-posts/{postId} | DELETE | 정보공유 게시글 삭제 | Yes |
| info | /api/info-posts/{postId}/comments | GET | 정보공유 댓글 목록 조회 | Yes |
| info | /api/info-posts/{postId}/comments | POST | 정보공유 댓글 작성 | Yes |
| info | /api/info-posts/{postId}/comments/{commentId} | PUT | 정보공유 댓글 수정 | Yes |
| info | /api/info-posts/{postId}/comments/{commentId} | DELETE | 정보공유 댓글 삭제 | Yes |
| photo | /api/photo-posts | GET | 사진첩 목록 조회 | Yes |
| photo | /api/photo-posts/{albumId} | GET | 사진첩 상세 조회 | Yes |
| photo | /api/photo-posts | POST | 사진첩 등록 | Yes |
| photo | /api/photo-posts/{albumId} | PUT | 사진첩 수정 | Yes |
| photo | /api/photo-posts/{albumId} | DELETE | 사진첩 삭제 | Yes |
| photo | /api/photo-posts/{albumId}/comments | POST | 댓글 작성 | Yes |
| photo | /api/photo-posts/{albumId}/comments/{commentId} | PUT | 댓글 수정 | Yes |
| photo | /api/photo-posts/{albumId}/comments/{commentId} | DELETE | 댓글 삭제 | Yes |
| users | /api/users/me | GET | 회원정보 조회 | Yes |
| users | /api/users/me/posts | GET | 내가 쓴 글 목록 조회 | Yes |
| users | /api/users/me/posts/{postId} | GET | 내가 쓴 글 상세 이동 | Yes |
| users | /api/users/me/posts/{postId} | DELETE | 내가 쓴 글 삭제 | Yes |
| users | /api/users/me/comments | GET | 내가 쓴 댓글 목록 조회 | Yes |
| users | /api/users/me/comments/{commentId} | GET | 내가 쓴 댓글 상세 이동 | Yes |
| users | /api/users/me/comments/{commentId} | DELETE | 내가 쓴 댓글 삭제 | Yes |
| users | /api/users/me/email/verification/send | POST | 이메일 변경 인증 메일 발송 | Yes |
| users | /api/users/me/email/verification/verify | POST | 이메일 변경 인증 확인 | Yes |
| users | /api/users/me/settings | PATCH | 회원정보 수정 | Yes |
| users | /api/users/me/password | PATCH | 비밀번호 변경 | Yes |
| users | /api/users/me/withdraw | PATCH | 회원 탈퇴 | Yes |
