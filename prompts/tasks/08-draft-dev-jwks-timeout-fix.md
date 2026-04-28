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

**원인 2 — 타임아웃 시 `commence()`가 호출되지만 I/O 원인 감지에 실패해 401을 반환**

흐름:
1. `NimbusJwtDecoder.createJwt()` → I/O 오류 → `JwtDecoderInitializationException` (`JwtException` 하위)
2. `JwtAuthenticationProvider.authenticate()` → `JwtException` catch → `AuthenticationServiceException`으로 re-throw
3. `BearerTokenAuthenticationFilter` (Spring Security 7.0.3) → `AuthenticationException` 전체 catch → `authenticationFailureHandler.onAuthenticationFailure(...)` 호출 → `authenticationEntryPoint.commence(...)` 위임
4. `LoggingAuthenticationEntryPoint.commence()` 진입은 되지만, I/O 오류 감지 로직이 없어 **단순 401**로만 응답

> 500의 실제 원인은 필터 우회가 아니라, `commence()` 내 조건 부재 또는 타임아웃 중 클라이언트 연결 끊김 등 다른 경로일 가능성이 높다. 핵심 수정은 JWKS 캐시로 타임아웃 자체를 줄이는 것이다.

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

### Exclude

- 새 클래스 파일 추가 없음
- 테스트 코드 수정 없음 (현재 보안 관련 테스트 없음)
- `application.yaml` 설정 변경 없음
- 의존성(build.gradle) 추가 없음 — `JWKSourceBuilder`는 이미 `spring-boot-starter-oauth2-resource-server`에 포함된 nimbus-jose-jwt에 존재

## 5. Constraints

- 모든 파일은 UTF-8로 저장
- 기존 클래스 구조 및 네이밍 유지
- 변경 범위 최소화 — 수정 파일 2개, 신규 파일 없음
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
  - (`java.time.Duration` 사용 안 함 — ms long 값 직접 사용)
  - `java.io.IOException` (LoggingAuthenticationEntryPoint)
  - `org.springframework.security.authentication.AuthenticationServiceException` (LoggingAuthenticationEntryPoint)
- 추가하는 메서드: `hasCause(Throwable, Class<?>)` — LoggingAuthenticationEntryPoint 내 private static 헬퍼

## 7. Deliverables

### 수정 파일 1 — `BaseSecurityConfig.java`

`jwtDecoder()` 메서드 내 jwkSetUri 분기를 아래와 같이 교체한다.

```java
if (StringUtils.hasText(jwkSetUri)) {
    JWKSource<SecurityContext> jwkSource = JWKSourceBuilder
            .create(new URL(jwkSetUri))
            .cache(15 * 60 * 1000L, 5 * 60 * 1000L)  // TTL 15분, 갱신 타임아웃 5분 (ms)
            .retrying(true)                             // 네트워크 오류 시 1회 재시도
            .outageTolerant(true)                       // outage 시 stale 캐시 반환
            .rateLimited(30 * 1000L)                    // 최소 30초 간격으로만 Supabase 호출 (ms)
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

- `.cache(long, long)` — TTL 15분, 갱신 타임아웃 5분 (ms 단위, `Duration` 아님)
- `.retrying(true)` — 네트워크 오류 시 **1회 재시도** (stale 캐시 반환 아님)
- `.outageTolerant(true)` — 재시도 포함 조회가 모두 실패할 때 **stale 캐시 반환** (타임아웃 핵심 fallback)
- `.rateLimited(long)` — ms 단위로 최소 호출 간격 설정 (Supabase 과호출 방지)

### 수정 파일 2 — `LoggingAuthenticationEntryPoint.java`

`commence()` 메서드에 I/O 원인 감지 로직 추가.

```java
boolean isIoFailure = authException instanceof AuthenticationServiceException
        && hasCause(authException, IOException.class);

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

실제 예외 체인이 여러 단계로 감싸지기 때문에 직접 `getCause()` 대신 cause chain 전체를 순회하는 헬퍼 메서드를 추가한다.

```java
// AuthenticationServiceException
//   └─ JwtDecoderInitializationException
//        └─ RemoteKeySourceException
//             └─ IOException  ← 실제 타임아웃 원인

private static boolean hasCause(Throwable t, Class<?> type) {
    for (Throwable c = t.getCause(); c != null; c = c.getCause()) {
        if (type.isInstance(c)) return true;
    }
    return false;
}
```

> `SilentAuthenticationEntryPoint`는 `LoggingAuthenticationEntryPoint`를 상속하므로 별도 수정 불필요.

## 8. Checks Before Execution

- [x] `JWKSourceBuilder`가 nimbus-jose-jwt에 존재하는지 — `spring-boot-starter-oauth2-resource-server` 의존성에 포함되어 있으므로 별도 추가 불필요
- [x] nimbus-jose-jwt **10.4** API 시그니처 확인 완료
  - `cache(long, long)` — ms 단위 (Duration 아님)
  - `rateLimited(long)` — ms 단위 (Duration 아님)
  - `retrying(boolean)` — 1회 재시도, stale 캐시 반환이 아님
  - `outageTolerant(boolean)` — outage 시 stale 캐시 반환 (타임아웃 fallback 핵심)
  - 추가 의존성 불필요
- [x] `issuerLocation` 방식 변경 없음 — 기존 동작 보존
- [x] `SilentAuthenticationEntryPoint`는 별도 수정 없이 부모 클래스 변경으로 반영됨
- [x] `ProdSecurityConfig`, `DevelopSecurityConfig`에 `authenticationEntryPoint` 이미 설정됨 — 수정 불필요 (코드 확인 완료)

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
- NimbusJwtDecoder가 JWKS 캐시 만료 시 Supabase 조회 타임아웃 → AuthenticationServiceException → commence() 호출되나 적절한 응답 처리 부재
- JWKSourceBuilder: cache/rateLimited는 ms long 값 사용, retrying은 1회 재시도, outageTolerant가 stale 캐시 fallback
- 상세 분석은 `prompts/tasks/08-draft-dev-jwks-timeout-fix.md` 참고

포함 범위:
- `BaseSecurityConfig.jwtDecoder()` — JWKSourceBuilder 기반 캐시 + retry 적용
- `LoggingAuthenticationEntryPoint.commence()` — I/O cause chain 감지 시 503 반환, hasCause() 헬퍼 추가

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
