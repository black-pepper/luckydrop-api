# Prompt Draft

## 1. Task Type
- Development

## 1-1. Prompt File Name
- 작업 번호: `09`
- 단계: `draft`
- 유형: `dev`
- 작업 슬러그: `draw-result-admin-list-pagination-filter`
- 최종 파일명: `09-draft-dev-draw-result-admin-list-pagination-filter.md`

## 2. Goal
- 관리자용 추첨 결과 목록 조회 API에 페이징과 동적 필터를 추가해 누적된 데이터를 운영자가 효율적으로 탐색할 수 있게 한다.
- 첫 QueryDSL 커스텀 레포지토리 도입이므로, 이후 다른 도메인이 따라쓸 수 있는 구조를 함께 정립한다.

## 3. Background
- 현재 `GET /api/manage/draw-results`는 `contentCode` 단일 파라미터로 전체 목록을 `List<>` 형태로 반환한다.
- 응답 DTO `ManagerDrawResultResponse`는 `drawResultId`, `invitationCode`, `invitationCodeName`, `rewardName`, `drawNo`, `drawnAt`, `delivered` 필드를 가진다.
- 데이터가 누적되며 응답이 커져 운영자가 원하는 행을 찾기 어려운 상태다.
- 최근 커밋 `ddee615 add querydsl`로 QueryDSL 5.1.0(jakarta) 의존성과 `QuerydslConfig`(JPAQueryFactory 빈)이 이미 추가되어 있다.
- 현재까지 `*RepositoryCustom`/`*RepositoryImpl` 패턴을 사용하는 코드는 없다. 이번 작업이 첫 적용 사례가 된다.
- 관련 주요 파일:
  - `src/main/java/com/luckydrop/api/controller/ManagerDrawResultController.java`
  - `src/main/java/com/luckydrop/api/service/ManagerDrawResultService.java`
  - `src/main/java/com/luckydrop/api/domain/drawresult/repository/DrawResultRepository.java`
  - `src/main/java/com/luckydrop/api/domain/drawresult/entity/DrawResult.java`
  - `src/main/java/com/luckydrop/api/domain/drawresult/dto/ManagerDrawResultResponse.java`
  - `src/main/java/com/luckydrop/api/config/QuerydslConfig.java`
- 사용자 결정 사항(확인 완료):
  - 필터 4종: `drawnAt` 범위(from~to), `delivered`, `invitationCode`(정확 일치), `rewardName`(LIKE).
  - 정렬: `drawnAt DESC` 고정. 정렬 파라미터는 노출하지 않는다.
  - 페이징 응답: Spring `Page<T>` (totalElements, totalPages 포함).
- `drawnAt` 입력 형식과 경계:
  - 포맷: ISO-8601 OffsetDateTime. 예: `2026-04-29T00:00:00+09:00`, `2026-04-29T00:00:00Z`.
  - 경계: from/to 모두 inclusive. 즉 `drawnAt >= drawnAtFrom`, `drawnAt <= drawnAtTo`.
- 기존 테스트는 모두 Mockito 기반 서비스 목 테스트(`ManagerDrawResultServiceTest`)이므로, 이번 변경의 핵심 리스크인 QueryDSL 동적 where, count 쿼리 분리, fetch join, OffsetDateTime 쿼리 바인딩은 현재 테스트 구조로 검증이 불가능하다. 리포지토리 통합 테스트가 별도로 필요하다.

## 4. Scope
### Include
- `DrawResultRepositoryCustom` 인터페이스와 `DrawResultRepositoryImpl`(QueryDSL 사용) 추가
- `DrawResultRepository`가 `DrawResultRepositoryCustom`을 상속하도록 수정
- 검색 조건 DTO `ManagerDrawResultSearchCondition` 추가 (4종 필터)
- `ManagerDrawResultService.getManagerDrawResults` 시그니처 변경: 검색 조건과 `Pageable`을 받아 `Page<ManagerDrawResultResponse>`를 반환
- `ManagerDrawResultController` 변경: 검색 조건 파라미터, `@PageableDefault(size = 20)` 수신, 응답 타입 `Page<ManagerDrawResultResponse>`로 변경
- 본문 조회 시 `invitationCode`, `reward`에 대한 fetch join을 유지해 N+1을 방지하고, count 쿼리는 fetch join 없이 별도 작성
- Swagger 문서화: 검색 조건 DTO 필드별 `@Schema(description, example)` 부착, 컨트롤러에서 `@ParameterObject` 사용으로 쿼리 파라미터 펼쳐 노출
- 검색 조건 DTO는 `@ModelAttribute`로 쿼리스트링 바인딩
- **리포지토리 통합 테스트 필수**: `DrawResultRepositoryImpl` 대상으로 DB 동작을 검증
  - 필터 단독/조합 동작 (`drawnAt` 범위, `delivered`, `invitationCode`, `rewardName`)
  - `drawnAt` 경계 inclusive 동작(경계값과 정확히 같은 시각의 데이터가 포함되는지)
  - 페이지 경계: `Page#totalElements`, `totalPages`, 마지막 페이지 잘림 없는 처리
  - fetch join으로 인한 N+1 미발생(SQL 카운트 또는 Hibernate Statistics 활용)
  - `OffsetDateTime` 파라미터가 DB 컬럼(`timestamptz`)과 정확히 비교되어 의도한 행만 필터링되는지
- **컨트롤러 웹 테스트 필수**(MockMvc 등): HTTP 입력 → DTO 바인딩 계층을 검증
  - `drawnAtFrom`/`drawnAtTo`에 ISO-8601 쿼리스트링(`2026-04-29T00:00:00+09:00`, `Z` 포함)이 `OffsetDateTime`으로 정상 바인딩되는지
  - `delivered`/`invitationCode`/`rewardName`이 `@ModelAttribute`로 정상 바인딩되어 서비스에 전달되는지
  - 잘못된 형식의 `drawnAt` 입력에 대한 응답(400 등) 동작
  - `page`/`size` 파라미터로 `Pageable`이 구성되고 응답 구조가 `Page<>` 형태로 직렬화되는지
- 서비스 단위 테스트는 시그니처 변경에 따른 기존 케이스 보정 수준으로 유지(목 테스트로는 QueryDSL/바인딩 검증 불가능함을 인지)

### Exclude
- 응답 DTO `ManagerDrawResultResponse` 필드 변경
- 정렬 파라미터(sort) 외부 노출
- 추첨 실행 로직 변경
- 다른 도메인 레포지토리의 QueryDSL 전환
- 스키마 변경
- 인증/소유권 정책 변경(기존 `getOwnedActiveContent` 가드 그대로 사용)

## 5. Constraints
- 모든 파일은 UTF-8로 저장한다.
- 기존 패키지 구조, 계층 구조, 네이밍 패턴을 최대한 유지한다.
- 변경은 필요한 최소 범위로 수행한다.
- 외부 API 파라미터로 `contentId` 대신 `contentCode`를 사용하는 규칙을 유지한다.
- 본문 쿼리에 `invitationCode`, `reward` fetch join을 적용해 응답 매핑 시 추가 select가 발생하지 않게 한다.
- count 쿼리에는 fetch join을 넣지 않는다.
- `BooleanExpression` 또는 `BooleanBuilder`를 이용한 동적 where는 null/blank 입력 시 자연스럽게 무시되도록 작성한다.
- `contentCode`는 검색 조건 DTO가 아니라 별도 인자로 받아 항상 적용한다(소유권 경계).
- 정렬은 리포지토리 내부에서 `drawnAt DESC`로 고정하고, 클라이언트가 보낸 Pageable의 sort는 사용하지 않는다. 이 사실을 Swagger 설명에 명시한다.
- QueryDSL 버전 등 의존성 버전은 임의로 변경하지 않는다.
- `drawnAt` 입력은 ISO-8601 OffsetDateTime 형식만 허용하고, from/to 모두 inclusive로 처리한다.
- 검색 조건 DTO는 `@ModelAttribute`로 바인딩하고, 컨트롤러에는 `@ParameterObject`(springdoc-openapi)를 사용해 Swagger에서 쿼리 파라미터가 펼쳐 노출되도록 한다.
- 검색 조건 DTO의 모든 필드에는 `@Schema` 또는 동급의 Swagger 어노테이션으로 설명과 예시를 부착한다.
- 리포지토리 통합 테스트를 반드시 추가하고, 단위 목 테스트만으로 마무리하지 않는다.
- 테스트는 정상 흐름, 필터 단독/조합, 페이징 경계, 권한 없는 접근, `OffsetDateTime` 경계값을 우선 검증한다.

## 6. Naming Plan
- 새로 만드는 파일명:
  - `DrawResultRepositoryCustom.java`
  - `DrawResultRepositoryImpl.java`
  - `ManagerDrawResultSearchCondition.java`
- 새로 만드는 클래스/인터페이스명:
  - `DrawResultRepositoryCustom`
  - `DrawResultRepositoryImpl`
  - `ManagerDrawResultSearchCondition`
- 새로 만드는 메서드/함수명:
  - `searchManagerDrawResults(String contentCode, ManagerDrawResultSearchCondition condition, Pageable pageable)`
  - 동적 조건 헬퍼: `drawnAtGoe`, `drawnAtLoe`, `deliveredEq`, `invitationCodeEq`, `rewardNameContains`
- 새로 만드는 테스트명(예시):
  - 서비스 단위 테스트:
    - 본인 소유 콘텐츠의 추첨 결과를 페이지 단위로 조회할 수 있다
    - 타인 소유 콘텐츠의 추첨 결과 조회는 차단된다
  - 리포지토리 통합 테스트(`DrawResultRepositoryImpl`, DB 동작 검증):
    - `delivered` 필터로 미전달 결과만 조회된다
    - `drawnAt` 범위 필터가 from만, to만, 둘 다 지정된 경우 모두 동작한다
    - `drawnAt` from/to 경계값과 같은 시각의 데이터가 결과에 포함된다(inclusive)
    - `invitationCode` 정확 일치 필터가 동작한다
    - `rewardName` LIKE 필터가 부분 일치로 동작한다
    - 페이지 크기와 번호에 따라 `Page#content`, `totalElements`, `totalPages`가 정확하다
    - 결과 매핑 시 `invitationCode`/`reward`가 fetch join으로 함께 로드되어 추가 select가 발생하지 않는다
  - 컨트롤러 웹 테스트(MockMvc 등, HTTP→DTO 바인딩 검증):
    - `drawnAtFrom`/`drawnAtTo`에 ISO-8601 쿼리스트링(예: `2026-04-29T00:00:00+09:00`, `Z`)이 `OffsetDateTime`으로 정상 바인딩된다
    - `delivered`/`invitationCode`/`rewardName`이 `@ModelAttribute`로 정상 바인딩되어 서비스에 전달된다
    - 잘못된 형식의 `drawnAt` 입력 시 의도한 응답(예: 400)을 반환한다
    - `page`/`size` 파라미터로 `Pageable`이 구성되고 응답이 `Page<>` 구조로 직렬화된다
- 용어 통일 기준:
  - 검색 조건 객체는 `SearchCondition` 접미사로 통일
  - 필터 파라미터명은 응답 DTO와 동일한 단어 사용 (`drawnAtFrom`, `drawnAtTo`, `delivered`, `invitationCode`, `rewardName`)
  - 지급 여부 용어는 기존 코드의 `delivered`를 그대로 사용

## 7. Deliverables
- QueryDSL 기반 `DrawResultRepositoryCustom`, `DrawResultRepositoryImpl` 코드
- 검색 조건 DTO `ManagerDrawResultSearchCondition`
- `ManagerDrawResultService.getManagerDrawResults` 변경된 메서드
- `ManagerDrawResultController` 쿼리 파라미터/응답 타입 변경
- 서비스 단위 테스트 보정 + 리포지토리 통합 테스트 신규 추가 + 컨트롤러 웹 테스트(MockMvc) 신규 추가
- Swagger UI에서 검색 조건/페이징 파라미터가 정상적으로 노출되는지 확인 결과
- 변경 내용, 영향 범위, 테스트 결과, 남은 리스크 정리

## 8. Checks Before Execution
- 본문 쿼리에 fetch join이 유지되어 응답 매핑 시 N+1이 발생하지 않는가
- count 쿼리는 fetch join 없이 별도로 작성되었는가
- `contentCode`가 검색 조건이 아닌 별도 인자로 항상 강제 적용되는가
- 정렬이 리포지토리 내부에서 고정되고, 외부 sort 입력이 무시되는 점이 Swagger에 드러나는가
- `drawnAt` from/to 경계가 inclusive로 명시되고 **리포지토리 통합 테스트**로 DB 필터링 동작이 검증되는가
- ISO-8601 쿼리스트링이 `OffsetDateTime` 필드로 바인딩되는지 **컨트롤러 웹 테스트**로 검증되는가(리포지토리 통합 테스트로 대체하지 않았는가)
- Swagger UI에서 검색 조건 필드가 펼쳐지고 각 필드에 설명/예시가 노출되는가(`@ParameterObject` + `@Schema`)
- 첫 QueryDSL 도입에 따른 Q클래스 생성과 빌드가 정상인가
- 리포지토리 통합 테스트가 추가되어 QueryDSL 동적 where, count 쿼리, fetch join, DB 필터링 동작이 실제로 검증되는가
- 컨트롤러 웹 테스트(MockMvc 등)가 추가되어 HTTP 쿼리스트링 → `@ModelAttribute` DTO 바인딩이 검증되는가
- 기존 `findAllByContentCodeOrderByDrawnAtDesc`를 호출하는 다른 코드가 깨지지 않도록 호환을 검토했는가
- 파일명만 보고도 순서, 단계, 목적, 작업 내용을 알 수 있는가

## 9. Final Prompt Draft

```md
파일명:
- `09-draft-dev-draw-result-admin-list-pagination-filter.md`

다음 공통 규칙을 따른다.
- `prompts/common-rules.md`
- `prompts/modes/development.md`

작업 목표:
- 관리자용 추첨 결과 목록 조회 API에 페이징과 동적 필터(drawnAt 범위, delivered, invitationCode, rewardName)를 추가한다.
- QueryDSL 기반 커스텀 레포지토리(`DrawResultRepositoryCustom` + `DrawResultRepositoryImpl`)를 도입하고, 응답을 `Page<ManagerDrawResultResponse>`로 변경한다.

배경:
- 현재 `/api/manage/draw-results`는 `contentCode` 단일 파라미터로 전체 목록을 `List<>`로 반환한다.
- 응답 DTO에는 drawResultId, invitationCode, invitationCodeName, rewardName, drawNo, drawnAt, delivered 필드가 있다.
- QueryDSL 5.1.0(jakarta)와 `QuerydslConfig`(JPAQueryFactory 빈)는 이미 설정되어 있다.
- 정렬은 `drawnAt DESC`로 고정하고, 페이징은 Spring `Page<T>`로 응답한다.
- `drawnAt` 입력은 ISO-8601 OffsetDateTime(예: `2026-04-29T00:00:00+09:00`, `2026-04-29T00:00:00Z`)이며 from/to 모두 inclusive(`drawnAt >= from`, `drawnAt <= to`)다.
- 기존 테스트는 모두 Mockito 기반 서비스 목 테스트라 QueryDSL 동작 검증과 HTTP 입력 바인딩 검증이 모두 빠져 있다. QueryDSL 동적 where/count/fetch join과 DB 필터링은 리포지토리 통합 테스트로, ISO-8601 쿼리스트링 → `OffsetDateTime` 바인딩과 `@ModelAttribute` 동작은 컨트롤러 웹 테스트(MockMvc 등)로 각각 분리해 검증해야 한다.

포함 범위:
- `DrawResultRepositoryCustom` 인터페이스와 `DrawResultRepositoryImpl`(QueryDSL) 추가
- `DrawResultRepository`가 `DrawResultRepositoryCustom`을 상속하도록 수정
- 검색 조건 DTO `ManagerDrawResultSearchCondition` 추가 (drawnAtFrom, drawnAtTo, delivered, invitationCode, rewardName)
- `ManagerDrawResultService.getManagerDrawResults`가 검색 조건과 Pageable을 받아 `Page<ManagerDrawResultResponse>`를 반환하도록 변경
- `ManagerDrawResultController`에 검색 조건 파라미터, `@PageableDefault(size = 20)` 추가, `@ParameterObject`(springdoc)로 Swagger에 쿼리 파라미터를 펼쳐 노출
- 검색 조건 DTO 모든 필드에 `@Schema(description, example)` 부착, `@ModelAttribute` 기반 쿼리스트링 바인딩
- 본문 쿼리에 `invitationCode`, `reward` fetch join 유지, count 쿼리는 fetch join 없이 별도 작성
- 리포지토리 통합 테스트 신규 추가(DB 동작 검증): 필터 단독/조합, `drawnAt` 경계 inclusive, 페이지 경계, fetch join N+1 미발생, DB 컬럼과의 `OffsetDateTime` 비교 정확성
- 컨트롤러 웹 테스트 신규 추가(HTTP→DTO 바인딩 검증, MockMvc 등): ISO-8601 쿼리스트링 → `OffsetDateTime` 바인딩, `delivered`/`invitationCode`/`rewardName` `@ModelAttribute` 바인딩, 잘못된 형식 입력 처리, `Pageable` 구성과 `Page<>` 응답 직렬화
- 서비스 단위 테스트는 시그니처 변경에 맞춰 보정(목 테스트로는 QueryDSL/바인딩 검증 불가)

제외 범위:
- 응답 DTO 필드 변경
- 정렬 파라미터 외부 노출
- 추첨 실행 로직 변경
- 다른 도메인의 QueryDSL 전환
- 스키마 변경
- 인증/소유권 정책 변경

제약:
- 모든 파일은 UTF-8로 저장한다.
- 기존 구조와 네이밍을 최대한 유지한다.
- 외부 API에는 `contentId` 대신 `contentCode`를 사용한다.
- 본문 쿼리에 fetch join을 유지하고, count 쿼리에는 넣지 않는다.
- 동적 where는 null/blank 입력 시 무시되도록 작성한다.
- `contentCode`는 검색 조건 DTO가 아닌 별도 인자로 항상 적용한다.
- 정렬은 리포지토리 내부에서 고정하고, 외부 sort 입력은 사용하지 않는다.
- 의존성 버전은 임의로 변경하지 않는다.
- `drawnAt` 입력은 ISO-8601 OffsetDateTime(예: `2026-04-29T00:00:00+09:00`)만 허용하고 from/to 모두 inclusive로 처리한다.
- 검색 조건 DTO는 `@ModelAttribute`로 바인딩하고, 컨트롤러는 `@ParameterObject`(springdoc)를 사용해 Swagger에 쿼리 파라미터를 펼쳐 노출한다.
- 검색 조건 DTO 모든 필드에 `@Schema(description, example)`를 부착한다.
- 리포지토리 통합 테스트와 컨트롤러 웹 테스트를 각각 반드시 추가한다. 통합 테스트는 DB 동작을, 웹 테스트는 HTTP→DTO 바인딩을 검증한다. 둘 중 하나로 다른 계층을 대체하지 않는다.

명명 규칙:
- 검색 조건 객체는 `SearchCondition` 접미사를 사용한다.
- 필터 파라미터명은 응답 DTO와 동일한 단어를 사용한다.
- 지급 여부 용어는 기존 `delivered`를 유지한다.

완료 조건:
- 관리자가 `contentCode` 기준으로 본인 소유 콘텐츠의 추첨 결과를 페이지 단위로 조회할 수 있다.
- 4종 필터가 단독 또는 조합으로 동작한다.
- 응답이 `Page<ManagerDrawResultResponse>` 구조이며 totalElements/totalPages가 정확하다.
- 본문 쿼리 1회 + count 쿼리 1회로 N+1 없이 동작한다.
- `drawnAt` from/to 경계가 inclusive로 동작함이 리포지토리 통합 테스트로 검증된다.
- ISO-8601 쿼리스트링이 `OffsetDateTime`으로 정상 바인딩됨이 컨트롤러 웹 테스트로 검증된다.
- Swagger UI에서 검색 조건 필드가 펼쳐지고 각 필드 설명/예시가 노출된다.
- 리포지토리 통합 테스트가 QueryDSL 동적 where, count 쿼리, fetch join, DB 필터링 동작을 검증한다.
- 컨트롤러 웹 테스트(MockMvc 등)가 HTTP 쿼리스트링 → `@ModelAttribute` DTO 바인딩 동작을 검증한다.
- 변경 코드에 대한 테스트 또는 검증 결과가 함께 정리된다.
```
