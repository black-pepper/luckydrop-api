# LuckyDrop API

LuckyDrop 백엔드 API 저장소입니다.

## 기술 스택

- Java 21
- Spring Boot 4
- Spring Web MVC
- Spring Security + OAuth2 Resource Server
- Spring Data JPA / Hibernate
- QueryDSL
- PostgreSQL
- Supabase Auth
- Cloudtype

## 로컬 개발

테스트 실행:

```bash
./gradlew test
```

애플리케이션 실행:

```bash
./gradlew bootRun
```

애플리케이션은 `local`, `develop`, `prod`, `test` 등의 Spring profile을 사용합니다.
DB와 Supabase 관련 설정은 환경변수로 주입합니다.

## 프롬프트 문서 사용법

프롬프트 문서의 원본은 아래 Notion 데이터베이스입니다.

- [프롬프트 문서](https://www.notion.so/36a41a6b4b058086a35bfbaca0dd0bc9)

Codex에게 작업을 요청할 때는 로컬 파일 경로 대신 Notion의 문서 이름이나 호출 이름을 사용합니다.

자주 사용하는 문서:

- `common-rules`
- `backend-project-context`
- `backend-api-context`
- `development-mode`
- `review-mode`
- `verification-mode`
- `prompt-draft-template`

요청 예시:

```text
Notion 프롬프트 문서에서 common-rules, backend-project-context,
backend-api-context, development-mode를 참고해서 작업해줘.
```

백엔드 API나 도메인 규칙이 변경되면 관련 Notion 프롬프트 문서를 갱신하고 `검수일`을 업데이트합니다.
오래된 문서는 로컬 파일로 다시 만들지 않고, Notion에서 `Needs Update` 또는 `Archived` 상태로 관리합니다.
