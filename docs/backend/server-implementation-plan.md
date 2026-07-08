# Server Implementation Plan

D.COM 인트라넷 MVP의 서버 배포 및 운영 계획서이다.

> 이 문서는 Notion의 서버 구현 계획, 실제 Ubuntu 배포 초안, MVP 배포 및 운영 계획서를 레포 문서용으로 정리한 것이다.  
> 실제 서버 접속 정보, DB 비밀번호, JWT Secret, SSH Key, 사용자 데이터는 포함하지 않는다.

## 1. 목적

이 문서는 D.COM 인트라넷 리뉴얼 MVP를 실제 서버에 배포하고, 동아리원이 사용할 수 있는 형태로 운영하기 위한 서버 구현 계획을 정리한다.

목표는 다음과 같다.

1. React 프론트엔드와 Spring Boot 백엔드를 실제 서버에서 실행한다.
2. 동아리원이 접속 가능한 MVP 환경을 만든다.
3. 배포 이후에도 피드백 반영, 재배포, 백업, 로그 확인이 가능한 운영 구조를 만든다.
4. DB, 파일 저장소, 서버 보안, 배포 절차를 한 문서에서 추적할 수 있게 한다.

## 2. 기존 인트라넷과 신규 인트라넷의 차이

기존 D.COM 인트라넷은 iwinv 웹호스팅 환경에서 운영되었다.

기존 구조는 다음과 같다.

```text
Laravel / PHP 기반 웹 애플리케이션
iwinv 웹호스팅 환경
```

웹호스팅은 PHP 실행 환경, DB, 웹서버 등이 미리 준비되어 있어 파일을 업로드하는 방식으로 비교적 쉽게 운영할 수 있다. 기존 인트라넷은 Laravel/PHP 기반이었기 때문에 웹호스팅 환경에 적합했다.

반면 신규 인트라넷은 프론트엔드와 백엔드가 분리된 구조이다.

```text
intranet-web    → React 프론트엔드
intranet-server → Spring Boot 백엔드 API 서버
```

React는 사용자 화면을 담당하고, Spring Boot는 로그인, 회원가입, 게시글, 댓글, 족보, 파일 업로드, 회원 관리, 관리자 기능 등 실제 데이터를 처리하는 API 서버 역할을 한다.

Spring Boot 서버는 PHP처럼 요청이 들어올 때마다 파일 단위로 실행되는 방식이 아니라, 서버 애플리케이션을 한 번 실행해둔 뒤 계속 요청을 기다리는 방식으로 동작한다.

```text
Spring Boot 서버 실행
→ API 요청 대기
→ 요청이 들어오면 처리
→ 다시 대기
```

따라서 Spring Boot 백엔드 서버를 안정적으로 운영하려면 서버 프로세스를 계속 실행할 수 있는 환경이 필요하다.

## 3. 배포 방식 선택

신규 인트라넷은 웹호스팅이 아니라 iwinv 가상서버(VPS)를 사용하여 배포한다.

가상서버를 사용하는 이유는 다음과 같다.

- Spring Boot 서버 프로세스를 계속 실행할 수 있다.
- Nginx 설정을 직접 구성할 수 있다.
- React 정적 파일과 Spring Boot API 서버를 하나의 도메인에서 운영할 수 있다.
- 운영 DB와 파일 저장소 경로를 직접 제어할 수 있다.
- systemd를 이용해 백엔드 서버를 서비스로 등록할 수 있다.
- 로그, 백업, 배포 절차를 직접 관리할 수 있다.

## 4. 서버 선택

MVP 배포에서는 iwinv Lite Zone의 Ubuntu 가상서버를 사용한다.

| 항목 | 선택 |
|---|---|
| 서비스 유형 | iwinv 가상서버 |
| Zone | KR1-Lite-Z01 |
| 하드웨어 | vgna_1_n |
| CPU | 1 vCPU |
| Memory | 1GB |
| Storage | NVMe 25GB |
| Traffic | 620GB |
| Network | 2.5Gbps |
| 운영체제 | Ubuntu 24.04 LTS 64bit |
| 네트워크 방식 | Direct IP |
| 서버 수량 | 1대 |
| 블록 스토리지 | MVP 단계에서는 추가하지 않음 |

현재 기존 인트라넷 사용량은 저장공간 약 5GB, 월 최고 트래픽 기준 약 17GB 수준으로 파악하고 있다. 신규 서버의 기본 저장공간 25GB와 월 트래픽 620GB는 MVP 초기 운영에는 충분할 것으로 판단한다.

다만 Lite Zone은 오토스케일과 네트워크 방화벽 기능이 제한적이므로, 서버 내부 방화벽과 운영 모니터링이 중요하다.

## 5. 전체 배포 구조

MVP 단계에서는 하나의 가상서버 안에서 프론트엔드, 백엔드, DB, 파일 저장소를 함께 운영한다.

```text
Ubuntu VPS
├── Nginx
│   ├── React 빌드 파일 서빙
│   └── /api 요청을 Spring Boot 서버로 프록시
│
├── intranet-web
│   └── React 빌드 결과물
│
├── intranet-server
│   └── Spring Boot Java 21 API 서버
│
├── MySQL 또는 MariaDB
│   └── dcom_intranet DB
│
├── 로컬 파일 저장소
│   └── 족보, 정보게시판 첨부파일, 공지 첨부파일, 활동 사진 저장
│
└── UFW
    └── 22, 80, 443만 외부 허용
```

사용자 요청 흐름은 다음과 같다.

```text
사용자
→ https://dev-intranet.dcom.club
→ Nginx
    ├── React 화면 제공
    └── /api 요청은 Spring Boot로 전달
→ Spring Boot
→ DB / 파일 저장소
```

## 6. 운영 단계

### 6.1 개발 서버 배포

목적은 팀원 내부 테스트이다.

확인 항목:

- 백엔드 API 정상 동작
- Swagger 확인
- 프론트와 API 연동
- 로그인, 회원가입, 게시글, 파일 업로드 등 핵심 흐름
- 테스트 데이터 사용

예상 주소:

```text
dev-intranet.dcom.club
```

### 6.2 베타 운영

목적은 일부 동아리원을 대상으로 실제 사용 피드백을 받는 것이다.

확인 항목:

- 실제 사용자 계정 생성
- 회원가입 → 이메일 인증 → 관리자 승인 → 로그인 흐름
- 족보 업로드/다운로드 사용성
- 정보 공유 게시판 사용성
- 활동 사진 앨범 사용성
- 관리자 기능
- 모바일 화면
- 사용자 피드백 수집

이 단계에서도 기존 인트라넷은 유지하고, 신규 인트라넷은 베타 서비스로 운영한다.

### 6.3 정식 운영 전환 검토

베타 운영 후 주요 버그와 사용성 문제가 해결되면 기존 인트라넷 대체 여부를 검토한다.

정식 운영 전환 전 확인할 항목:

- 핵심 기능 정상 동작
- 사용자 데이터 백업 정책
- 관리자 계정 관리 정책
- 파일 업로드 용량 관리
- 장애 발생 시 대응 방법
- 기존 인트라넷 데이터 이전 필요 여부

## 7. 서버 초기 설정

### 7.1 서버 접속

서버 생성 후 공인 IP를 확인하고 SSH Key 방식으로 접속한다.

```bash
chmod 400 키파일.pem
ssh -i 키파일.pem root@서버공인IP
```

초기 설정 이후에는 별도 운영 계정을 생성하여 사용하는 것을 권장한다.

```bash
adduser dcom
usermod -aG sudo dcom
```

### 7.2 기본 패키지 업데이트

```bash
apt update
apt upgrade -y
```

### 7.3 필수 프로그램 설치

```bash
apt install -y git curl unzip nginx ufw mysql-server openjdk-21-jdk
```

설치 항목의 역할은 다음과 같다.

| 프로그램 | 역할 |
|---|---|
| Git | GitHub 레포 clone 및 pull |
| Nginx | React 정적 파일 제공, `/api` 요청을 Spring Boot로 전달 |
| UFW | 서버 내부 방화벽 |
| MySQL | 운영 DB |
| Java 21 | Spring Boot 실행 |
| curl / unzip | 설치 및 운영 보조 도구 |

Java 설치 확인:

```bash
java -version
```

### 7.4 Node.js 설치

서버에서 React 프로젝트를 직접 빌드할 경우 Node.js를 설치한다.

```bash
curl -fsSL https://deb.nodesource.com/setup_22.x | bash -
apt install -y nodejs

node -v
npm -v
```

서버 RAM이 1GB라면 프론트 빌드와 백엔드 빌드를 같은 서버에서 반복 수행할 때 메모리 부족이 발생할 수 있다. 추후에는 로컬 또는 GitHub Actions에서 빌드한 결과물만 서버에 반영하는 방식도 검토한다.

### 7.5 Swap 설정

RAM 1GB 서버에서는 Gradle 빌드, React 빌드, MySQL, Spring Boot가 동시에 동작할 경우 메모리 부족이 발생할 수 있다. 따라서 1~2GB 정도의 swap을 생성하는 것을 권장한다.

```bash
fallocate -l 2G /swapfile
chmod 600 /swapfile
mkswap /swapfile
swapon /swapfile
echo '/swapfile none swap sw 0 0' >> /etc/fstab
free -h
```

## 8. 방화벽 및 보안 설정

Lite Zone은 네트워크 방화벽 기능이 제한적이므로 서버 내부 방화벽인 UFW 설정이 중요하다.

외부 공개 포트:

```text
22  → SSH
80  → HTTP
443 → HTTPS
```

외부 비공개 포트:

```text
8080 → Spring Boot 내부 포트
3306 → MySQL 포트
```

UFW 설정:

```bash
ufw allow OpenSSH
ufw allow 80
ufw allow 443
ufw enable
ufw status
```

주의사항:

- 8080 포트는 외부에 직접 열지 않는다.
- 3306 포트는 외부에 직접 열지 않는다.
- 사용자는 Nginx를 통해서만 서비스에 접근한다.
- Spring Boot는 내부 포트에서만 접근하도록 운영한다.
- MySQL은 같은 서버 내부에서만 접근한다.
- SSH Key 방식을 사용한다.
- SSH 비밀번호 로그인 비활성화를 검토한다.
- 운영 환경변수와 비밀번호는 GitHub에 커밋하지 않는다.
- 관리자 API 권한 검사를 반드시 수행한다.
- 파일 업로드 확장자와 용량 검증을 적용한다.

## 9. 프로젝트 디렉토리 구조

프로젝트는 `/srv/dcom` 아래에 배치한다.

```bash
mkdir -p /srv/dcom
cd /srv/dcom
```

GitHub 레포 clone:

```bash
git clone https://github.com/Dcom-KHU/intranet-server.git
git clone https://github.com/Dcom-KHU/intranet-web.git
```

업로드 파일은 별도 경로에 저장한다.

```bash
mkdir -p /var/dcom/uploads/jokbo
mkdir -p /var/dcom/uploads/information
mkdir -p /var/dcom/uploads/announcements
mkdir -p /var/dcom/uploads/albums
```

업로드 파일 경로:

```text
/var/dcom/uploads
├── jokbo
├── information
├── announcements
└── albums
```

## 10. DB 설정

운영용 DB를 생성한다.

```sql
CREATE DATABASE dcom_intranet CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE USER 'dcom_user'@'localhost' IDENTIFIED BY '강한_비밀번호';

GRANT ALL PRIVILEGES ON dcom_intranet.* TO 'dcom_user'@'localhost';

FLUSH PRIVILEGES;
EXIT;
```

운영 DB에서는 root 계정을 직접 사용하지 않고, Spring Boot 전용 DB 계정인 `dcom_user`를 사용한다.

`'dcom_user'@'localhost'`는 같은 서버 내부에서만 DB 접속을 허용한다는 의미이다. MVP 구조에서는 Spring Boot와 DB가 같은 VPS 안에서 실행되므로 `localhost` 접속으로 충분하다.

> DBMS 이름은 현재 문서에서 MySQL로 표현되어 있지만, 최종 DB를 MariaDB로 확정할 경우 `mysql-server` 대신 `mariadb-server`, JDBC URL 및 드라이버 설정도 MariaDB 기준으로 조정한다.

## 11. 백엔드 운영 설정

### 11.1 DB 드라이버 추가

MySQL 사용 시 `build.gradle`에 다음 의존성이 필요하다.

```gradle
runtimeOnly 'com.mysql:mysql-connector-j'
```

MariaDB 사용 시에는 다음 의존성을 사용한다.

```gradle
runtimeOnly 'org.mariadb.jdbc:mariadb-java-client'
```

최종 DBMS에 맞춰 둘 중 하나를 선택한다.

### 11.2 운영 설정 파일

운영 환경에서는 H2가 아니라 MySQL 또는 MariaDB를 사용한다. 운영 설정은 `application-prod.yml` 또는 환경변수 기반으로 분리한다.

MySQL 예시:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/dcom_intranet?serverTimezone=Asia/Seoul&characterEncoding=UTF-8
    username: dcom_user
    password: ${DB_PASSWORD}
    driver-class-name: com.mysql.cj.jdbc.Driver

  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        format_sql: false
    show-sql: false
    open-in-view: false

jwt:
  secret: ${JWT_SECRET}
  access-token-validity: 1800000
  refresh-token-validity: 1209600000

file:
  upload-dir: /var/dcom/uploads
```

MariaDB 예시:

```yaml
spring:
  datasource:
    url: jdbc:mariadb://localhost:3306/dcom_intranet
    username: dcom_user
    password: ${DB_PASSWORD}
    driver-class-name: org.mariadb.jdbc.Driver

  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        format_sql: false
    show-sql: false
    open-in-view: false

jwt:
  secret: ${JWT_SECRET}
  access-token-validity: 1800000
  refresh-token-validity: 1209600000

file:
  upload-dir: /var/dcom/uploads
```

주의사항:

- 실제 DB 비밀번호를 GitHub에 커밋하지 않는다.
- JWT Secret을 GitHub에 커밋하지 않는다.
- 운영 설정 예시는 `application-prod-example.yml`로 공유한다.
- 실제 운영 값은 서버 환경변수 또는 별도 env 파일로 관리한다.

### 11.3 환경변수 파일 관리

systemd 서비스 파일에 민감 정보를 직접 적기보다 별도 환경변수 파일로 관리하는 것을 권장한다.

```bash
mkdir -p /etc/dcom
nano /etc/dcom/intranet-server.env
```

예시:

```text
DB_PASSWORD=강한_DB_비밀번호
JWT_SECRET=충분히_긴_랜덤_문자열
```

권한 설정:

```bash
chmod 600 /etc/dcom/intranet-server.env
```

## 12. 백엔드 빌드 및 실행

```bash
cd /srv/dcom/intranet-server
chmod +x gradlew
./gradlew bootJar -x test
```

빌드 결과 확인:

```bash
ls build/libs
```

실행 테스트:

```bash
export DB_PASSWORD='정해둔_DB비밀번호'
export JWT_SECRET='JWT토큰'

java -jar build/libs/*.jar --spring.profiles.active=prod
```

다른 터미널에서 확인:

```bash
curl http://localhost:8080
```

정상 실행이 확인되면 `Ctrl + C`로 종료하고, systemd 서비스로 등록한다.

## 13. Spring Boot systemd 서비스 등록

```bash
nano /etc/systemd/system/intranet-server.service
```

예시:

```ini
[Unit]
Description=D.COM Intranet Server
After=network.target mysql.service

[Service]
User=dcom
WorkingDirectory=/srv/dcom/intranet-server
EnvironmentFile=/etc/dcom/intranet-server.env
ExecStart=/usr/bin/java -jar /srv/dcom/intranet-server/build/libs/intranet-server-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
```

적용:

```bash
systemctl daemon-reload
systemctl enable intranet-server
systemctl start intranet-server
systemctl status intranet-server
```

로그 확인:

```bash
journalctl -u intranet-server -f
```

## 14. 프론트엔드 빌드 및 배포

```bash
cd /srv/dcom/intranet-web
npm install
npm run build
```

빌드 결과 확인:

```bash
ls dist
```

Nginx가 서빙할 위치로 복사한다.

```bash
mkdir -p /var/www/intranet-web
rm -rf /var/www/intranet-web/*
cp -r dist/* /var/www/intranet-web/
```

프론트엔드에서 API 주소를 설정할 때는 Nginx 프록시 구조를 고려한다.

동일 도메인에서 `/api`로 호출하는 구조라면 프론트 `.env`는 다음처럼 둘 수 있다.

```text
VITE_API_BASE_URL=/api
```

## 15. Nginx 설정

Nginx 설정 파일을 생성한다.

```bash
nano /etc/nginx/sites-available/intranet
```

예시:

```nginx
server {
    listen 80;
    server_name dev-intranet.dcom.club;

    root /var/www/intranet-web;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }

    location /api/ {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

설정 활성화:

```bash
ln -s /etc/nginx/sites-available/intranet /etc/nginx/sites-enabled/intranet
nginx -t
systemctl reload nginx
```

주의사항:

- 백엔드 API는 `/api/...`로 시작하도록 통일한다.
- Nginx가 `/api/` 요청을 Spring Boot로 전달한다.
- 8080 포트는 외부에 직접 공개하지 않는다.
- 개발 단계에서 Swagger를 외부 확인해야 하는 경우 `/swagger-ui/index.html`, `/v3/api-docs` 프록시 여부를 확인한다.
- 운영 단계에서는 Swagger 접근 제한 또는 비활성화를 검토한다.

## 16. 도메인 연결

기존 인트라넷을 바로 대체하지 않고, MVP 테스트용 임시 도메인을 먼저 사용한다.

예시:

```text
dev-intranet.dcom.club
```

DNS 설정에서 A 레코드를 추가한다.

```text
유형: A
이름: dev-intranet
값: 새 VPS 공인 IP
TTL: 기본값
```

도메인 연결 후 Nginx 설정의 `server_name`도 동일하게 맞춘다.

```nginx
server_name dev-intranet.dcom.club;
```

## 17. HTTPS 적용

도메인 연결이 완료된 뒤 HTTPS를 적용한다.

```bash
apt install -y certbot python3-certbot-nginx
certbot --nginx -d dev-intranet.dcom.club
```

적용 후 접속 주소:

```text
https://dev-intranet.dcom.club
```

인증서 자동 갱신 확인:

```bash
certbot renew --dry-run
```

## 18. 반복 배포 절차

MVP 운영 중에는 사용자 피드백을 반영하여 계속 수정하고 재배포해야 한다.

### 18.1 백엔드 재배포

```bash
cd /srv/dcom/intranet-server
git pull origin main
./gradlew bootJar -x test
systemctl restart intranet-server
systemctl status intranet-server
journalctl -u intranet-server -n 100
```

확인할 것:

```text
- 빌드 성공 여부
- 서버 재시작 성공 여부
- API 정상 응답 여부
- 에러 로그 발생 여부
```

> 실제 운영 브랜치가 `main`이 아니라 `develop` 또는 별도 release 브랜치라면 `git pull origin <branch>`로 조정한다.

### 18.2 프론트엔드 재배포

```bash
cd /srv/dcom/intranet-web
git pull origin main
npm install
npm run build
rm -rf /var/www/intranet-web/*
cp -r dist/* /var/www/intranet-web/
systemctl reload nginx
```

확인할 것:

```text
- 빌드 성공 여부
- 화면 정상 표시 여부
- 새 UI 반영 여부
- API 호출 정상 여부
```

## 19. 롤백 계획

배포 후 치명적인 오류가 발생할 경우 이전 버전으로 되돌릴 수 있어야 한다.

### 19.1 Git 기준 롤백

이전 커밋으로 되돌린다.

```bash
git log --oneline
git checkout 이전커밋해시
```

백엔드의 경우 다시 빌드 후 재시작한다.

```bash
./gradlew bootJar -x test
systemctl restart intranet-server
```

프론트엔드의 경우 다시 빌드 후 Nginx 서빙 폴더에 복사한다.

```bash
npm install
npm run build
rm -rf /var/www/intranet-web/*
cp -r dist/* /var/www/intranet-web/
systemctl reload nginx
```

### 19.2 배포 전 백업

중요한 배포 전에는 기존 빌드 결과물과 DB 백업을 먼저 남긴다.

```bash
mkdir -p /srv/dcom/backups/releases
cp -r /var/www/intranet-web /srv/dcom/backups/releases/intranet-web_$(date +%F_%H%M)
```

## 20. 백업 정책

실제 사용자 피드백을 받는 MVP 단계부터는 데이터가 쌓인다. 따라서 DB와 업로드 파일 백업이 필요하다.

### 20.1 백업 대상

```text
- DB
- 업로드 파일 디렉토리
- Nginx 설정
- Spring Boot 운영 설정
```

### 20.2 백업 경로

```text
/srv/dcom/backups/db
/srv/dcom/backups/uploads
/srv/dcom/backups/config
```

### 20.3 DB 백업

```bash
mkdir -p /srv/dcom/backups/db
mysqldump -u dcom_user -p dcom_intranet > /srv/dcom/backups/db/dcom_$(date +%F).sql
```

### 20.4 업로드 파일 백업

```bash
mkdir -p /srv/dcom/backups/uploads
tar -czf /srv/dcom/backups/uploads/uploads_$(date +%F).tar.gz /var/dcom/uploads
```

### 20.5 설정 파일 백업

```bash
mkdir -p /srv/dcom/backups/config
cp /etc/nginx/sites-available/intranet /srv/dcom/backups/config/nginx_intranet_$(date +%F)
cp /etc/systemd/system/intranet-server.service /srv/dcom/backups/config/intranet-server.service_$(date +%F)
```

### 20.6 보관 정책

MVP 운영 기간에는 최소 최근 7일치 백업을 유지한다.

추후 운영 규모가 커지면 별도 외부 저장소 또는 클라우드 스토리지 백업을 검토한다.

## 21. 운영 로그 및 모니터링

### 21.1 Spring Boot 로그

```bash
journalctl -u intranet-server -f
journalctl -u intranet-server -n 100
```

### 21.2 Nginx 로그

```bash
tail -f /var/log/nginx/access.log
tail -f /var/log/nginx/error.log
```

### 21.3 서버 자원 확인

```bash
free -h
df -h
top
```

가능하면 `htop`도 설치한다.

```bash
apt install -y htop
htop
```

확인할 항목:

```text
- RAM 사용량
- 디스크 사용량
- CPU 사용량
- Spring Boot 프로세스 상태
- DB 프로세스 상태
- 업로드 파일 용량
```

## 22. 사용자 피드백 운영 계획

MVP 배포 이후에는 실제 사용자 피드백을 수집하고, 이를 GitHub Issue로 정리하여 개선한다.

### 22.1 피드백 수집 채널

```text
- Google Form
- 카카오톡 오픈채팅 또는 단체방
- Notion 피드백 페이지
- GitHub Issue는 내부 개발팀용으로만 사용
```

### 22.2 피드백 분류 기준

| 유형 | 설명 |
|---|---|
| Bug | 기능이 정상 동작하지 않음 |
| UX | 사용하기 불편함 |
| Feature | 새 기능 요청 |
| Content | 문구, 설명, 게시글 관련 문제 |
| Performance | 느림, 로딩 문제 |
| Security | 권한, 개인정보, 파일 접근 문제 |

### 22.3 우선순위 기준

| 우선순위 | 기준 |
|---|---|
| High | 로그인 불가, 데이터 손실, 파일 접근 오류, 관리자 권한 오류 |
| Medium | 주요 기능 사용 불편, 화면 오류, 일부 API 오류 |
| Low | 문구 수정, 디자인 개선, 편의 기능 요청 |

### 22.4 피드백 반영 흐름

```text
사용자 피드백 수집
→ 내용 정리
→ GitHub Issue 생성
→ 우선순위 설정
→ 담당자 배정
→ 개발
→ PR
→ 배포
→ 사용자에게 수정 완료 안내
```

## 23. 배포 전 QA 체크리스트

### 23.1 계정/인증

- [ ] 회원가입 가능
- [ ] 이메일 인증 가능
- [ ] 관리자 승인 대기 상태 정상 표시
- [ ] 관리자 승인 후 로그인 가능
- [ ] 거절된 회원 로그인 불가
- [ ] 탈퇴 회원 로그인 불가
- [ ] JWT 인증 정상 동작
- [ ] 일반 사용자와 관리자 권한 구분 정상 동작

### 23.2 사용자 기능

- [ ] 메인 대시보드 접속 가능
- [ ] 공지사항 목록/상세 조회 가능
- [ ] 정보 공유 게시글 작성 가능
- [ ] 정보 공유 게시글 수정/삭제 가능
- [ ] 댓글 작성/삭제 가능
- [ ] 족보 목록/검색 가능
- [ ] 족보 파일 업로드 가능
- [ ] 족보 파일 다운로드 가능
- [ ] 활동 사진 앨범 조회 가능
- [ ] 마이페이지 회원정보 조회 가능
- [ ] 마이페이지 회원정보 수정 가능
- [ ] 비밀번호 변경 가능
- [ ] 회원탈퇴 가능

### 23.3 관리자 기능

- [ ] 승인 대기 회원 조회 가능
- [ ] 회원 승인 가능
- [ ] 회원 거절 가능
- [ ] 전체 회원 조회 가능
- [ ] 공지사항 작성/수정/삭제 가능
- [ ] 활동 사진 앨범 등록/수정/삭제 가능
- [ ] 관리자 권한 이양 가능
- [ ] 일반 사용자가 관리자 API 접근 불가

### 23.4 파일 기능

- [ ] 파일 업로드 가능
- [ ] 파일 다운로드 가능
- [ ] 허용되지 않은 확장자 업로드 제한
- [ ] 파일 크기 제한 적용
- [ ] 삭제된 게시글의 파일 접근 정책 확인

### 23.5 화면/사용성

- [ ] PC 화면 정상
- [ ] 모바일 화면 확인
- [ ] Chrome 기준 정상 동작
- [ ] 주요 버튼 클릭 동작 확인
- [ ] 빈 데이터 화면 처리 확인
- [ ] 에러 메시지 표시 확인

### 23.6 운영

- [ ] HTTPS 적용
- [ ] 8080 외부 접근 차단
- [ ] 3306 외부 접근 차단
- [ ] Spring Boot 로그 확인 가능
- [ ] Nginx 로그 확인 가능
- [ ] DB 백업 가능
- [ ] 업로드 파일 백업 가능
- [ ] 배포 후 재시작 절차 확인

## 24. 운영 중 주의사항

```text
- 서버에서 직접 코드를 수정하지 않는다.
- 모든 수정은 로컬 개발 → GitHub PR → main 또는 배포 브랜치 반영 → 서버 배포 흐름으로 진행한다.
- 민감 정보는 GitHub에 커밋하지 않는다.
- 사용자 데이터가 쌓인 뒤에는 DB를 임의로 삭제하지 않는다.
- 파일 업로드 디렉토리를 임의로 삭제하지 않는다.
- 배포 전에는 가능하면 DB와 파일을 백업한다.
- 운영 중 장애가 발생하면 먼저 로그를 확인한다.
- 사용자 피드백은 개발 작업 단위로 정리해서 관리한다.
```

## 25. DB 문서와의 연결 지점

이 서버 구현 계획은 다음 DB 문서와 함께 참고한다.

```text
docs/db/mvp-erd-v2.0.md
docs/db/legacy-schema.md
docs/db/db-decisions.md
docs/db/entity-erd-check.md
docs/db/schema-draft.sql
```

DB 설계와 직접 연결되는 항목은 다음과 같다.

| 서버 구현 항목 | DB 설계 연결 |
|---|---|
| 운영 DB 생성 | `schema-draft.sql`, MariaDB DDL |
| 파일 업로드 경로 | `archive_files`, `info_post_files`, `photo_images`의 `object_key` |
| 파일 다운로드 API | `content_type`, `original_file_name`, `download_count` |
| 회원 승인 흐름 | `users.status` |
| 관리자 권한 | `users.role` |
| 탈퇴 회원 처리 | `users.status = WITHDRAWN` |
| 백업 정책 | DB 백업, 업로드 파일 백업 |
| Nginx `/api` 프록시 | API 경로 일관성 |

## 26. 최종 목표

이번 MVP 배포의 최종 목표는 완벽한 서비스를 한 번에 만드는 것이 아니라, 실제 동아리원이 사용할 수 있는 핵심 기능을 먼저 제공하고 사용자 피드백을 기반으로 점진적으로 개선하는 것이다.

성공 기준은 다음과 같다.

```text
- 사용자가 회원가입하고 로그인할 수 있다.
- 관리자가 회원을 승인할 수 있다.
- 사용자가 공지사항, 족보, 정보 공유 게시판, 활동 사진을 이용할 수 있다.
- 파일 업로드와 다운로드가 가능하다.
- 관리자가 핵심 콘텐츠를 관리할 수 있다.
- 배포 후 발생하는 피드백을 수집하고 개선할 수 있다.
- 데이터 백업과 로그 확인을 통해 최소한의 운영 대응이 가능하다.
```
