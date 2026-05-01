# 01 Draft Dev Supabase Security Profiles

파일명:
- `01-draft-dev-supabase-security-profiles.md`

다음 공통 규칙을 따른다.
- `prompts/common-rules.md`
- `prompts/modes/development.md`

작업 목표:
- `luckydrop-api`에 Spring Security를 추가한다.
- Supabase에서 발급한 JWT access token을 Authorization Bearer Token으로 받아 인증하도록 구성한다.
- 기존 `WebMvcConfigurer` 기반 CORS 설정은 제거하고 Spring Security 설정 내부에서 CORS를 관리한다.
- 보안 설정은 `local`, `develop`, `prod` 환경별로 분리한다.

배경:
- 현재 프로젝트에는 Spring Security가 아직 없다.
- 현재 CORS는 `src/main/java/com/luckydrop/api/config/CorsConfig.java`에서 관리하고 있다.
- 원하는 구조는 다음과 같다.
- `local` 프로필: 로컬 개발 편의를 위해 인증을 완화할 수 있는 단순 SecurityFilterChain 구성
- `develop` 프로필: Supabase JWT 검증 구조를 유지하되 개발 환경에 맞는 정책 적용
- `prod` 프로필: Supabase JWT 검증, stateless 세션, Security 내부 CORS, 공개 엔드포인트 최소 허용
- JWT 검증은 가능하면 `oauth2ResourceServer().jwt(...)`와 `NimbusJwtDecoder` 기반의 표준 방식으로 처리한다.
- 설정은 `application.yaml`과 프로필별 설정 파일로 분리할 수 있는 구조가 필요하다.

포함 범위:
- `spring-boot-starter-security` 및 필요한 OAuth2 resource server 의존성 추가
- `SecurityFilterChain` 기반 보안 설정 추가
- `local`, `develop`, `prod` 프로필별 보안 설정 분리
- Supabase JWT 검증을 위한 `JwtDecoder` 구성 검토 및 적용
- 필요 시 issuer, jwk-set-uri, audience 검증 전략 정리
- Security 내부 `CorsConfigurationSource` 구성
- 환경별 CORS 허용 origin 정책 정리
- 기존 `CorsConfig` 제거 또는 완전 대체
- 현재 API 중 공개 엔드포인트와 인증 필요 엔드포인트 분류
- 환경별 설정 파일 구조 정리

제외 범위:
- 자체 로그인 API 구현
- 세션 기반 인증 구현
- 권한(Role) 체계의 과도한 확장
- 프론트엔드 코드 변경

제약:
- 모든 파일은 UTF-8로 저장한다.
- 기존 프로젝트 구조와 네이밍을 최대한 유지한다.
- 환경 분리와 보안 구조는 명확히 하되, 현재 프로젝트에 불필요한 복잡성은 추가하지 않는다.
- 변경 범위는 필요한 최소 수준으로 제한한다.
- local/develop/prod의 차이를 코드와 설정에서 분명하게 드러낸다.

명명 규칙:
- 보안 설정 클래스는 프로필과 역할이 드러나게 작성한다.
- 예: `LocalSecurityConfig`, `DevelopSecurityConfig`, `SecurityConfig` 또는 프로젝트에 더 맞는 동등한 이름
- Supabase JWT 관련 클래스는 공급자와 역할이 드러나게 작성한다.
- 환경 설정 파일은 Spring profile 규칙에 맞춰 일관되게 유지한다.
- 프롬프트와 코드에서 `local`, `develop`, `prod` 용어를 혼용하지 않고 일관되게 사용한다.

구현 지침:
- `luckydrop-api`는 `local`, `develop`, `prod` 기준으로 보안 정책을 분리한다.
- `local`은 개발 편의를 위해 인증을 완화할 수 있지만, 그 정책이 코드상에서 명확히 드러나야 한다.
- `develop`과 `prod`는 Supabase JWT 검증 기반으로 맞추고, 필요 시 허용 엔드포인트만 예외로 둔다.
- 세션은 stateless 정책을 우선 적용한다.
- CSRF는 브라우저 폼 기반 앱이 아니라면 API 서버 기준으로 검토한다.
- CORS는 기존 `WebMvcConfigurer`를 유지하지 말고 Security 설정 내부에서 일원화한다.
- 환경별 CORS 기준은 명확히 분리한다.
- `local`은 로컬 프론트엔드 origin을 허용한다. 예: `http://localhost:<port>`
- `develop`은 개발/스테이징 프론트엔드 origin만 허용한다.
- `prod`는 운영 프론트엔드 origin만 허용한다.
- 허용 origin은 하드코딩을 최소화하고 프로퍼티 기반으로 분리할 수 있는 구조를 우선 검토한다.
- Swagger, actuator, preflight OPTIONS 요청 등 공개가 필요한 경로는 환경별로 검토한다.
- 단순 JWT 파싱 커스텀 필터보다 Spring Security OAuth2 Resource Server 표준 구성을 우선 검토한다.
- `JwtDecoder`에서 issuer와 audience 검증이 필요한지 확인하고, Supabase 구조에 맞는 최소 검증 전략을 선택한다.
- 이번 단계의 JWT 범위는 최소화한다.
- 이번 단계에서는 access token의 유효성 검증과 인증 객체 구성까지만 우선 처리한다.
- 사용자 엔티티 매핑, DB 조회 기반 인증 확장, 추가 claim 주입, 세부 role 인가 체계는 이번 범위에서 제외한다.
- 현재 단계에서 `SupabaseJwtAuthenticationConverter`가 꼭 필요하지 않다면 생략할 수 있다.
- 다만 이후 사용자 매핑이 필요해질 가능성은 고려해 확장 가능한 구조로 둔다.

완료 조건:
- Spring Security가 프로젝트에 추가되어 있다.
- `local`, `develop`, `prod` 기준의 보안 구조가 정리되어 있다.
- Supabase JWT bearer token을 검증할 수 있는 구조가 들어가 있다.
- JWT 처리는 토큰 유효성 검증과 인증 객체 구성 범위에 머무른다.
- 기존 CORS 설정은 Security 설정으로 대체되어 있다.
- 환경별 CORS 허용 origin 정책이 정리되어 있다.
- 공개 엔드포인트와 인증 필요 엔드포인트 정책이 정리되어 있다.
- 환경별 설정 파일 또는 프로필 설정이 반영되어 있다.
- 변경 내용, 영향 범위, 테스트 또는 검증 결과가 함께 정리되어 있다.
