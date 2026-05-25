# LuckDrop API

LuckDrop은 다양한 추첨 이벤트를 생성하고 관리할 수 있는 플랫폼의 백엔드 API 애플리케이션입니다. 참여자는 이벤트 코드와 초대 코드를 통해 추첨에 참여하고, 주최자는 인증된 API를 통해 이벤트, 경품, 초대 코드, 당첨 결과를 체계적으로 관리할 수 있습니다.

## 주요 기능

### 참여자 API (Participant)
- **초대 코드 검증**: 이벤트 코드(`contentCode`)와 초대 코드(`invitationCode`)의 유효성, 사용 가능 여부, 추첨 가능 상태 확인.
- **추첨 실행**: 경품 재고와 가중치 기반으로 추첨을 수행하고 당첨 결과 저장.
- **이벤트 상세 조회**: 참여자가 접근 가능한 이벤트 정보와 추첨 상태 조회.
- **경품 목록 조회**: 이벤트별 경품 목록과 잔여 수량 조회.
- **추첨 내역 조회**: 참여자의 초대 코드 기준 추첨 결과 히스토리 조회.

### 주최자 API (Manager)
- **콘텐츠 관리**: 추첨 이벤트 생성, 조회, 수정, 삭제 및 이벤트 기간/상태 관리.
- **경품 관리**: 이벤트별 경품 등록, 수정, 삭제, 일괄 생성/수정, 가중치(확률) 및 재고 관리.
- **초대 코드 관리**: 초대 코드 단건/일괄 생성, 조회, 수정, 삭제 및 사용 횟수 제한 관리.
- **당첨 결과 관리**: 이벤트별 당첨 내역 검색과 경품 지급(배송) 상태 업데이트.
- **사용자 관리**: 현재 로그인한 사용자 정보 조회/수정 및 회원 탈퇴 처리.
- **문의 접수**: 외부 문의 요청 저장.

### 인증/운영
- **Supabase Auth 연동**: JWT Resource Server 기반 주최자 API 보호.
- **프로필별 보안 설정**: `local`, `develop`, `prod`, `test` 프로필에 따라 CORS, Swagger, 공개 경로 설정 분리.
- **공통 응답/예외 처리**: 표준 API 응답 포맷과 전역 예외 처리 제공.
- **상태 확인**: Spring Boot Actuator 기반 `health`, `info` 엔드포인트 제공.

## 기술 스택

- **Language**: [Java](https://www.oracle.com/java/) 21
- **Framework / Runtime**: [Spring Boot](https://spring.io/projects/spring-boot) 4, Spring Web MVC
- **Security**: Spring Security, OAuth2 Resource Server, [Supabase Auth](https://supabase.com/auth)
- **Persistence**: Spring Data JPA, Hibernate, [QueryDSL](https://querydsl.com/)
- **Database**: [PostgreSQL](https://www.postgresql.org/), H2(Test)
- **Validation**: Spring Validation
- **API Docs**: [springdoc-openapi](https://springdoc.org/) 3
- **Monitoring**: Spring Boot Actuator
- **Build Tool**: [Gradle](https://gradle.org/)
- **Deployment**: Cloudtype
- **Testing**: JUnit Platform, Spring Boot Test, H2

## 시작하기

### 사전 준비 사항
- Java 21
- PostgreSQL
- Supabase 프로젝트 및 인증 설정

### 설치 및 실행

1. **저장소 클론**
   ```sh
   git clone https://github.com/black-pepper/luckydrop-api.git
   cd luckydrop-api
   ```

2. **환경 변수 설정**
   실행 프로필에 맞게 DB와 Supabase 관련 환경 변수를 설정합니다.

   - `DB_URL`: PostgreSQL JDBC URL
   - `DB_USERNAME`: PostgreSQL 사용자명
   - `DB_PASSWORD`: PostgreSQL 비밀번호
   - `SUPABASE_ISSUER_URI`: Supabase JWT issuer URI
   - `SUPABASE_JWK_SET_URI`: Supabase JWK Set URI (미설정 시 `${SUPABASE_ISSUER_URI}/.well-known/jwks.json`)
   - `SUPABASE_JWT_AUDIENCE`: JWT audience (기본값: `authenticated`)
   - `SUPABASE_SERVICE_ROLE_KEY`: 서버 권한 작업에 사용하는 Supabase 비밀 키
   - `APP_CORS_ALLOWED_ORIGIN`: 허용할 프론트엔드 Origin

3. **테스트 실행**
   ```sh
   ./gradlew test
   ```

4. **애플리케이션 실행**
   ```sh
   ./gradlew bootRun --args='--spring.profiles.active=local'
   ```

   기본 서버 포트는 `8080`입니다.

### API 문서

`local`, `develop` 프로필에서는 Swagger UI와 OpenAPI 문서를 사용할 수 있습니다.

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

`prod` 프로필에서는 Swagger UI와 OpenAPI 문서가 비활성화됩니다.

## 프로젝트 구조

```text
src/
├── main/
│   ├── java/com/luckydrop/api/
│   │   ├── common/          # 공통 응답 포맷 및 예외 처리
│   │   ├── config/          # QueryDSL, Swagger 등 애플리케이션 설정
│   │   ├── controller/      # 참여자/주최자 API 컨트롤러
│   │   ├── domain/          # 도메인별 entity, dto, repository
│   │   │   ├── content/     # 추첨 이벤트 콘텐츠
│   │   │   ├── drawresult/  # 추첨 결과
│   │   │   ├── inquiry/     # 문의
│   │   │   ├── invitationcode/ # 초대 코드
│   │   │   ├── reward/      # 경품
│   │   │   └── user/        # 사용자
│   │   ├── infrastructure/  # 외부 서비스 연동 (Supabase 등)
│   │   ├── security/        # Spring Security 및 Supabase JWT 설정
│   │   └── service/         # 비즈니스 로직
│   └── resources/           # profile별 설정 및 schema.sql
└── test/                    # 서비스, 컨트롤러, Repository 테스트
```

## 주요 API 경로

- `GET /api/draw/verify`: 초대 코드 검증
- `POST /api/draw/execute`: 추첨 실행
- `GET /api/draw/contents/{contentCode}`: 참여자용 이벤트 상세 조회
- `GET /api/draw/results`: 참여자 추첨 내역 조회
- `GET /api/draw/rewards`: 참여자용 경품 목록 조회
- `/api/manage/contents`: 주최자 콘텐츠 관리
- `/api/manage/rewards`: 주최자 경품 관리
- `/api/manage/invitation-codes`: 주최자 초대 코드 관리
- `/api/manage/draw-results`: 주최자 당첨 결과 관리
- `/user`: 사용자 정보 조회/수정/탈퇴
- `POST /inquiries`: 문의 접수

## 스크립트

- `./gradlew bootRun`: 애플리케이션 실행
- `./gradlew test`: 테스트 실행
- `./gradlew build`: 테스트를 포함한 전체 빌드
- `./gradlew clean`: 빌드 산출물 삭제

## 라이선스

본 프로젝트의 라이선스는 해당 기관/개인의 정책에 따릅니다.
