# Prompt Draft — 08

## 1. Task Type
- Development (버그 수정)

## 1-1. Prompt File Name
- 작업 번호: `08`
- 단계: `draft`
- 유형: `dev`
- 작업 슬러그: `jwks-timeout-fix`
- 최종 파일명: `08-draft-dev-jwks-timeout-fix.md`

## 2. Goal

Supabase JWKS 조회 타임아웃 발생 시 프론트에 HTTP 500이 내려가는 문제를 수정한다.  
JWKS 캐시 + fallback 전략으로 타임아웃 빈도를 줄이고, 타임아웃이 발생해도 500 대신 적절한 오류 코드(401 / 503)를 반환하도록 한다.

## 3. Background

### 현재 에러

```
org.springframework.security.authentication.AuthenticationServiceException:
An error occurred while attempting to decode the Jwt:
I/O error on GET request for "https://{supabase}.supabase.co/auth/v1/.well-known/jwks.json": Read timed out
```

### 원인 (2가지)

**원인 1 — JWKS 캐시 만료 시 fallback 없음**

`NimbusJwtDecoder.withJwkSetUri().build()`는 내부적으로 Nimbus의 `RemoteJWKSet`을 사용한다.  
캐시가 만료된 이후 Supabase JWKS 조회가 타임아웃되면 캐시된 키를 재사용하지 않고 예외를 던진다.

**원인 2 — AuthenticationServiceException이 500으로 전파됨**

흐름:
1. `NimbusJwtDecoder.createJwt()` → I/O 오류 → `JwtDecoderInitializationException` (JwtException 하위)
2. `JwtAuthenticationProvider.authenticate()` → `JwtException` catch → `AuthenticationServiceException`으로 re-throw
3. `BearerTokenAuthenticationFilter` → Spring Security 버전에 따라 `OAuth2AuthenticationException`만 catch하는 경우 존재
4. 미처리된 예외가 서블릿까지 전파 → `GlobalExceptionHandler`의 generic `Exception` 핸들러 → **HTTP 500**

### 관련 파일

| 파일 | 역할 |
|---|---|
| `src/main/java/com/luckydrop/api/security/BaseSecurityConfig.java` | `jwtDecoder()` 빈 정의 |
| `src/main/java/com/luckydrop/api/security/LoggingAuthenticationEntryPoint.java` | 인증 실패 응답 처리 |
| `src/main/java/com/luckydrop/api/security/SilentAuthenticationEntryPoint.java` | prod 환경 인증 실패 처리 |
| `src/main/java/com/luckydrop/api/security/ProdSecurityConfig.java` | prod SecurityFilterChain |
| `src/main/java/com/luckydrop/api/security/DevelopSecurityConfig.java` | develop SecurityFilterChain |

## 4. Scope

### Include

- `BaseSecurityConfig.jwtDecoder()` — Nimbus `JWKSourceBuilder` 기반으로 재구성 (캐시 + retry)
- `LoggingAuthenticationEntryPoint.commence()` — `AuthenticationServiceException` + I/O 원인 감지 → 503 반환
- `ProdSecurityConfig`, `DevelopSecurityConfig` — `oauth2ResourceServer`에 `authenticationEntryPoint` 명시

### Exclude

- 새 클래스 파일 추가 없음
- 테스트 코드 수정 없음 (현재 보안 관련 테스트 없음)
- `application.yaml` 설정 변경 없음
- 의존성(build.gradle) 추가 없음 — `JWKSourceBuilder`는 이미 `spring-boot-starter-oauth2-resource-server`에 포함된 nimbus-jose-jwt에 존재

## 5. Constraints

- 모든 파일은 UTF-8로 저장
- 기존 클래스 구조 및 네이밍 유지
- 변경 범위 최소화 — 수정 파일 4개, 신규 파일 없음
- `issuerLocation` 방식(jwkSetUri가 없는 경우)은 기존 동작 유지

## 6. Naming Plan

- 새로 만드는 파일: 없음
- 새로 만드는 클래스: 없음
- 새로 추가하는 import 목록:
  - `com.nimbusds.jose.JWSAlgorithm`
  - `com.nimbusds.jose.jwk.source.JWKSource`
  - `com.nimbusds.jose.jwk.source.JWKSourceBuilder`
  - `com.nimbusds.jose.proc.JWSVerificationKeySelector`
  - `com.nimbusds.jose.proc.SecurityContext`
  - `com.nimbusds.jwt.proc.DefaultJWTProcessor`
  - `java.net.URL`
  - `java.time.Duration`
  - `java.io.IOException` (LoggingAuthenticationEntryPoint)
  - `org.springframework.security.authentication.AuthenticationServiceException` (LoggingAuthenticationEntryPoint)

## 7. Deliverables

### 수정 파일 1 — `BaseSecurityConfig.java`

`jwtDecoder()` 메서드 내 jwkSetUri 분기를 아래와 같이 교체한다.

```java
if (StringUtils.hasText(jwkSetUri)) {
    JWKSource<SecurityContext> jwkSource = JWKSourceBuilder
            .create(new URL(jwkSetUri))
            .cache(Duration.ofMinutes(15), Duration.ofMinutes(5))
            .retrying(true)
            .rateLimited(Duration.ofSeconds(30))
            .build();

    DefaultJWTProcessor<SecurityContext> processor = new DefaultJWTProcessor<>();
    processor.setJWSKeySelector(new JWSVerificationKeySelector<>(JWSAlgorithm.ES256, jwkSource));

    jwtDecoder = new NimbusJwtDecoder(processor);
} else {
    jwtDecoder = NimbusJwtDecoder.withIssuerLocation(issuerUri)
            .jwsAlgorithm(SignatureAlgorithm.ES256)
            .build();
}
```

- `.cache(15분, 만료 5분 전 갱신)` — 캐시 TTL 설정
- `.retrying(true)` — 갱신 실패 시 stale 캐시 재사용
- `.rateLimited(30초)` — Supabase 과호출 방지

### 수정 파일 2 — `LoggingAuthenticationEntryPoint.java`

`commence()` 메서드에 I/O 원인 감지 로직 추가.

```java
boolean isIoFailure = authException instanceof AuthenticationServiceException
        && authException.getCause() instanceof IOException;

if (isIoFailure) {
    log.error("JWKS 조회 실패 (Supabase 연결 오류). method={}, uri={}",
            request.getMethod(), request.getRequestURI(), authException);
    response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE); // 503
    response.setCharacterEncoding(StandardCharsets.UTF_8.name());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.getWriter().write(
            OBJECT_MAPPER.writeValueAsString(
                    ApiResponse.fail("인증 서버에 일시적으로 연결할 수 없습니다. 잠시 후 다시 시도해주세요.")));
    return;
}

// 기존 401 처리
response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
...
```

> `SilentAuthenticationEntryPoint`는 `LoggingAuthenticationEntryPoint`를 상속하므로 별도 수정 불필요.

### 수정 파일 3 — `ProdSecurityConfig.java`

`oauth2ResourceServer` 설정에 `.authenticationEntryPoint(authenticationEntryPoint)` 추가.

```java
.oauth2ResourceServer(oauth2 -> oauth2
    .jwt(jwt -> jwt.decoder(jwtDecoder(securityProperties)))
    .authenticationEntryPoint(authenticationEntryPoint)  // 추가
)
```

### 수정 파일 4 — `DevelopSecurityConfig.java`

`ProdSecurityConfig`와 동일하게 `.authenticationEntryPoint(authenticationEntryPoint)` 추가.

## 8. Checks Before Execution

- [x] `JWKSourceBuilder`가 nimbus-jose-jwt에 존재하는지 — `spring-boot-starter-oauth2-resource-server` 의존성에 포함되어 있으므로 별도 추가 불필요
- [x] `retrying(true)` 사용 시 nimbus-jose-jwt 최소 버전 확인 필요 — `JWKSourceBuilder`는 9.31+ 지원. Spring Boot 3.x 기본 포함 버전에서 사용 가능 (단, 빌드 오류 시 사용자에게 버전 확인 요청)
- [x] `issuerLocation` 방식 변경 없음 — 기존 동작 보존
- [x] `SilentAuthenticationEntryPoint`는 별도 수정 없이 부모 클래스 변경으로 반영됨
- [x] `authenticationEntryPoint` 파라미터가 기존 메서드 시그니처에서 전달되는지 확인 — `applyCommon()`을 통해 전달됨

## 9. Final Prompt Draft

```md
파일명:
- 08-approved-dev-jwks-timeout-fix.md

다음 공통 규칙을 따른다.
- `prompts/common-rules.md`
- `prompts/modes/development.md`

작업 목표:
- Supabase JWKS 조회 타임아웃으로 인한 HTTP 500 응답 문제를 수정한다.
- JWKS 캐시 + fallback 전략으로 타임아웃 빈도를 낮추고, 발생 시 503으로 응답한다.

배경:
- NimbusJwtDecoder가 JWKS 캐시 만료 시 Supabase 조회를 재시도하다 타임아웃 → AuthenticationServiceException → 500 전파
- 상세 분석은 `prompts/tasks/08-draft-dev-jwks-timeout-fix.md` 참고

포함 범위:
- `BaseSecurityConfig.jwtDecoder()` — JWKSourceBuilder 기반 캐시 + retry 적용
- `LoggingAuthenticationEntryPoint.commence()` — I/O 원인 감지 시 503 반환
- `ProdSecurityConfig`, `DevelopSecurityConfig` — oauth2ResourceServer에 authenticationEntryPoint 명시

제외 범위:
- 새 파일 생성 없음
- 테스트 코드 추가 없음
- build.gradle 의존성 변경 없음
- application.yaml 변경 없음

제약:
- 기존 issuerLocation 방식(jwkSetUri 없는 경우) 동작 유지
- 파일은 UTF-8로 저장
- 기존 클래스 구조와 네이밍 유지
- nimbus-jose-jwt 버전 관련 빌드 오류 발생 시 변경 없이 사용자에게 버전 정보를 먼저 전달

완료 조건:
- JWKS 캐시가 만료되어도 stale 캐시를 통해 요청 처리 가능
- Supabase 연결 실패 시 프론트에 503과 한국어 메시지 반환
- 기존 401 응답 동작 변경 없음
```
