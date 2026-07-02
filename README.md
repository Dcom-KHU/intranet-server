# D.COM Intranet Server

D.COM 인트라넷 리뉴얼 프로젝트의 Spring Boot 백엔드 레포지토리입니다.

## Project Overview

D.COM 인트라넷 리뉴얼 프로젝트의 서버 애플리케이션입니다.

회원가입, 이메일 인증, 관리자 승인, 로그인/JWT 인증, 족보 아카이브, 정보 공유 게시판, 공지사항, 활동 사진 앨범, 마이페이지, 관리자 콘솔 기능을 제공합니다.

## Tech Stack

* Spring Boot
* MySQL
* JWT Authentication
* Local File Storage
* iwinv Cloud Server

## Related Repository

* Web: `Dcom-KHU/intranet-web`

## Branch Convention

* `main`: 안정 버전
* `develop`: feature 통합 및 개발
* `feature/*`: 기능 개발

## Branch Name

* `feature/auth`
* `feature/admin`
* `feature/home`
* `feature/mypage`
* `feature/archive`
* `feature/info-board`
* `feature/announcement`
* `feature/photo-album`


## Development Rule

1. `main` 브랜치에 직접 push하지 않습니다.
2. 모든 작업은 Issue 단위로 진행합니다.
3. 작업 시작 전 GitHub Projects에서 Issue 상태를 `In Progress`로 변경합니다.
4. 기능별 브랜치를 생성하여 작업합니다.
5. 작업 완료 후 Pull Request를 생성합니다.
6. Pull Request에는 관련 Issue 번호를 연결합니다.
7. 확인 후 `develop` 브랜치에 merge합니다.

## Commit Message Convention

* `feat`: 새로운 기능 추가
* `fix`: 버그 수정
* `docs`: 문서 수정
* `style`: 코드 포맷팅, 스타일 수정
* `refactor`: 코드 리팩토링
* `chore`: 설정, 빌드, 패키지 등 기타 작업

## Security Rule

아래 파일과 정보는 Git에 커밋하지 않습니다.

* `.env`
* `application-local.yml`
* `application-local.properties`
* DB 계정 정보
* JWT Secret
* 이메일 인증 관련 Secret
* 서버 접속 정보
* 운영 환경 설정 파일

## File Storage Policy

MVP 단계에서는 Cloudflare R2를 사용하지 않고, iwinv 클라우드 서버의 로컬 디스크에 파일을 저장합니다.

업로드 파일은 서버 내부의 지정된 업로드 디렉토리에 저장하며, DB에는 파일 자체가 아니라 파일 경로와 메타데이터를 저장합니다.

저장할 파일 메타데이터 예시는 다음과 같습니다.

* `original_file_name`
* `stored_file_name`
* `file_path`
* `content_type`
* `file_size`
* `uploaded_by`
* `created_at`

## Issue & Project Management

프로젝트 관리는 GitHub Projects의 `D.COM Intranet MVP` 보드에서 진행합니다.

Status는 다음 기준으로 관리합니다.

* `Backlog`: 해야 할 일 후보
* `Ready`: 작업 예정
* `In Progress`: 작업 중
* `Review`: PR 리뷰 또는 확인 중
* `Done`: 완료

## Notes

* 로그인 ID와 학번은 별도 필드로 관리합니다.
* 학번은 전체 학번 형식으로 저장합니다.
* 회원가입 직후 상태는 `EMAIL_UNVERIFIED`입니다.
* 이메일 인증 완료 후 상태는 `PENDING`입니다.
* 관리자 승인 후 상태는 `APPROVED`입니다.
