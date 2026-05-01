# 파일명
- `02-draft-dev-supabase-current-user-mapping.md`

# 공통 규칙
- `prompts/common-rules.md`
- `prompts/modes/development.md`

# 작업 목표
- Supabase가 발급한 access token을 백엔드 요청의 Bearer token으로 받아, 토큰의 `sub` 값을 기준으로 `public.users.auth_id`와 매핑해 현재 사용자를 식별할 수 있는 구조를 추가한다.
- 인증 자체의 책임은 Supabase에 두고, 백엔드는 검증된 토큰에서 사용자 식별 정보만 꺼내 비즈니스 로직에서 재사용할 수 있도록 정리한다.
- 프론트엔드로부터 토큰을받아 사용자를 조회할 수 있는 '/user' API를 추가한다.

# 배경
- 이 프로젝트는 1인 개발 기준으로 인증 구현 비용을 최소화하고 비즈니스 로직에 집중하는 방향을 우선한다.
- 로그인과 토큰 발급은 Supabase Auth가 담당하고, 백엔드는 Supabase JWT를 받아 현재 사용자를 찾는 책임만 가진다.
- `public.users.auth_id`는 Supabase의 `auth.users.id`를 참조하는 외래키로 사용한다.
- 현재 프로젝트에는 Supabase JWT 검증을 위한 보안 설정과 `public.users.auth_id` 컬럼은 존재하지만, 토큰에서 현재 사용자를 조회해 공통적으로 사용하는 흐름은 아직 없다.

# 참고 파일
- `src/main/java/com/luckydrop/api/security/BaseSecurityConfig.java`
- `src/main/java/com/luckydrop/api/security/DevelopSecurityConfig.java`
- `src/main/java/com/luckydrop/api/security/ProdSecurityConfig.java`
- `src/main/java/com/luckydrop/api/domain/user/entity/User.java`
- `src/main/resources/schema.sql`

# 포함 범위
- `User`를 `auth_id`로 조회할 수 있는 저장소 메서드 추가
- SecurityContext 또는 인증 객체에서 Supabase JWT의 `sub` 값을 읽어 현재 사용자 `authId`를 구하는 공통 처리 추가
- `authId`로 `public.users`를 조회해 현재 사용자 정보를 반환하는 공통 서비스 또는 제공자 추가
- 비즈니스 로직에서 재사용 가능한 현재 사용자 DTO 또는 모델 추가
- 사용자를 찾을 수 없을 때의 예외 처리 정책 정리

# 제외 범위
- Supabase 로그인 플로우 자체 구현
- 이메일/비밀번호 로그인, OAuth 로그인, 회원가입 화면 구현
- 세션 저장 방식 변경
- Spring Security 대규모 재구성
- `public.users` 자동 생성 로직 추가
- 다른 도메인 API의 기능 확장

# 제약 사항
- 모든 파일은 UTF-8로 저장한다.
- 기존 프로젝트 구조와 네이밍 스타일을 우선 따른다.
- 변경 범위는 현재 사용자 식별 흐름에 필요한 최소 범위로 제한한다.
- 인증 책임을 백엔드로 이동시키지 말고, Supabase가 발급한 토큰을 바탕으로 사용자 매핑만 수행한다.
- `auth.users.id`와 `public.users.auth_id`의 연결을 전제로 하며, 비즈니스 로직에서는 최종적으로 우리 서비스의 `users.id`를 사용할 수 있어야 한다.
- 토큰의 사용자 식별자는 `sub` claim을 기준으로 처리한다.
- 토큰 검증은 기존 보안 설정을 존중하고, 중복된 인증 로직을 만들지 않는다.

# 명명 규칙
- 새로 추가하는 타입 이름은 현재 프로젝트의 패키지 구조와 역할이 드러나도록 작성한다.
- 현재 로그인 사용자를 나타내는 타입은 `CurrentUser`, `AuthUser`, `LoginUser` 중 현재 프로젝트 문맥에 가장 자연스러운 이름 하나로 통일한다.
- 저장소 메서드는 `findByAuthId`처럼 조회 기준이 드러나는 이름을 사용한다.
- 사용자 조회용 서비스 또는 제공자 이름은 역할이 분명하게 드러나도록 작성한다.

# 완료 조건
- Supabase access token의 `sub` 값을 기준으로 현재 사용자를 조회하는 공통 진입점이 존재한다.
- `public.users.auth_id`를 기준으로 현재 사용자를 찾을 수 있다.
- 사용자 조회 실패 시 일관된 예외 또는 응답 정책이 정리되어 있다.
- 이후 비즈니스 서비스에서 현재 사용자 식별 로직을 중복 구현하지 않아도 되는 구조가 마련되어 있다.
- 변경 내용과 영향 범위, 필요한 검증 방법이 함께 정리되어 있다.

# 구현 시 유의사항
- `public.users.auth_id`는 Supabase 사용자와 1:1 매핑이라는 점을 코드와 설계에서 분명히 드러낸다.
- 현재 단계에서는 사용자 자동 생성보다 조회와 식별 흐름 정리에 집중한다.
- 향후 `GET /me` 또는 사용자 기반 도메인 API에서 쉽게 재사용할 수 있는 방향으로 구조를 잡는다.
- 비즈니스 서비스가 SecurityContext를 직접 읽지 않도록 공통 계층으로 감싼다.

# 검증 관점
- 인증된 요청에서 토큰의 `sub`를 읽어 `public.users` 조회가 되는지 확인한다.
- `auth_id`에 해당하는 사용자가 없을 때 기대한 예외가 발생하는지 확인한다.
- 비인증 요청은 기존 보안 정책대로 차단되는지 확인한다.
- 현재 사용자 조회 로직이 특정 컨트롤러나 서비스에 중복되지 않는지 확인한다.
