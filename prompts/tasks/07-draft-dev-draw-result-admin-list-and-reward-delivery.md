# Prompt Draft

## 1. Task Type
- Development

## 1-1. Prompt File Name
- 작업 번호: `07`
- 단계: `draft`
- 유형: `dev`
- 작업 슬러그: `draw-result-admin-list-and-reward-delivery`
- 최종 파일명: `07-draft-dev-draw-result-admin-list-and-reward-delivery.md`

## 2. Goal
- 관리자용 추첨 결과 조회 API를 추가해 `contentCode` 기준으로 해당 콘텐츠의 추첨 결과 리스트를 조회할 수 있게 한다.
- 참가자용 추첨 결과 조회 흐름을 현재 코드 기준으로 점검하고, `contentCode`와 `invitationCode` 기준 리스트 조회가 일관되게 동작하도록 정리한다.
- `draw_results`에 상품 지급 여부 컬럼을 추가하고, 관리자용 지급 여부 수정 API를 추가한다.

## 3. Background
- 현재 참가자용 추첨 결과 조회 API `/api/draw/results`는 존재하며, `contentCode`와 `invitationCode`를 함께 받아 해당 참가자의 결과 이력을 조회한다.
- 현재 관리자용 추첨 결과 조회 API는 없다.
- 현재 `DrawResult` 엔티티와 `draw_results` 테이블에는 상품 지급 여부를 나타내는 컬럼이 없다.
- 관리자는 본인 소유 콘텐츠에 속한 추첨 결과만 조회하거나 지급 상태를 변경할 수 있어야 한다.
- 외부 API의 식별자는 내부 `contentId`가 아니라 `contentCode`를 사용해야 한다.
- 관련 주요 파일:
  - `src/main/java/com/luckydrop/api/controller/DrawController.java`
  - `src/main/java/com/luckydrop/api/service/ResultService.java`
  - `src/main/java/com/luckydrop/api/domain/drawresult/entity/DrawResult.java`
  - `src/main/java/com/luckydrop/api/domain/drawresult/repository/DrawResultRepository.java`
  - `src/main/java/com/luckydrop/api/controller/AdminContentController.java`
  - `src/main/java/com/luckydrop/api/service/CurrentUserService.java`
  - `src/main/resources/schema.sql`
- 추정:
  - 관리자용 결과 조회 응답에는 지급 여부, 초대 코드 정보, 보상명, 추첨 차수, 추첨 시각 등이 포함될 가능성이 높다.
  - 지급 여부 수정은 개별 추첨 결과 단위의 `PUT` 또는 `PATCH` API가 적절하다.

## 4. Scope
### Include
- 관리자용 추첨 결과 조회 API 추가
  - 예: `GET /api/manage/draw-results?contentCode={contentCode}`
- 관리자용 결과 조회를 위한 Controller, Service, DTO, Repository 로직 추가
- 현재 사용자 기준 콘텐츠 소유권 검증 추가 또는 기존 패턴 재사용
- 참가자용 추첨 결과 조회 API가 현재 요구사항과 일치하는지 점검하고, 필요 시 응답 필드 또는 조회 로직 최소 보완
- `draw_results` 테이블에 상품 지급 여부 컬럼 추가
- `DrawResult` 엔티티 및 응답 DTO에 지급 여부 반영
- 관리자용 상품 지급 여부 수정 API 추가
  - 예: `PUT /api/manage/draw-results/{drawResultId}/delivery`
- 지급 여부 수정을 위한 요청 DTO, 서비스 메서드, 예외 처리 추가
- 필요 시 Swagger 노출 및 검증용 테스트 코드 추가 또는 수정
- 스키마 변경이 반영되도록 `schema.sql` 수정

### Exclude
- 추첨 실행 로직 자체의 변경
- 보상(Reward) CRUD 스펙 변경
- 초대 코드 CRUD 스펙 변경
- 프론트엔드 또는 외부 클라이언트 코드 수정
- 대규모 리팩터링

## 5. Constraints
- 모든 파일은 UTF-8로 저장한다.
- 기존 패키지 구조, 계층 구조, 네이밍 패턴을 최대한 유지한다.
- 변경은 필요한 최소 범위로 수행한다.
- 로그인 필요한 관리자 API에서는 요청으로 `userId`를 받지 않고 `CurrentUserService`를 우선 사용한다.
- 외부 API 요청, 응답, 경로, 쿼리 파라미터에는 `contentId` 대신 `contentCode`를 사용한다.
- 스키마 변경은 이번 요구사항 범위인 `draw_results`의 지급 여부 컬럼 추가에 한정한다.
- 지급 여부 기본값, null 허용 여부, 응답 노출 방식은 기존 엔티티 스타일에 맞춰 일관되게 정한다.
- 테스트가 필요하면 정상 흐름, 권한 없는 접근, 존재하지 않는 결과, 지급 여부 변경 케이스를 우선 검증한다.

## 6. Naming Plan
- 새로 만드는 파일명:
  - 필요 시 관리자용 추첨 결과 응답 DTO 파일
  - 필요 시 지급 여부 수정 요청 DTO 파일
- 새로 만드는 클래스/인터페이스명:
  - 예시: `AdminDrawResultResponse`
  - 예시: `DrawResultDeliveryUpdateRequest`
  - 예시: `AdminDrawResultController`
- 새로 만드는 메서드/함수명:
  - 예시: `getAdminDrawResults`
  - 예시: `updateDeliveryStatus`
  - 예시: `getResultsByContent`
  - 예시: `getOwnedDrawResult`
- 새로 만드는 테스트명:
  - 본인 소유 콘텐츠의 추첨 결과 목록을 조회할 수 있다
  - 타인 소유 콘텐츠의 추첨 결과 조회는 차단된다
  - 지급 여부를 수정할 수 있다
  - 존재하지 않는 추첨 결과 수정 시 예외가 발생한다
- 용어 통일 기준:
  - 관리자 조회 기준 식별자는 `contentCode`
  - 참가자 조회 기준 식별자는 `contentCode`, `invitationCode`
  - 지급 여부 용어는 `delivery`, `delivered`, `deliveryStatus` 중 하나로 정하고 전 구간에서 일관되게 사용한다

## 7. Deliverables
- 관리자용 추첨 결과 조회 API
- 관리자용 지급 여부 수정 API
- 관련 DTO, Service, Repository, 예외 처리 코드
- `draw_results` 지급 여부 컬럼 및 엔티티 매핑 반영
- 참가자용 결과 조회 흐름 점검 결과와 필요한 최소 보완
- 테스트 코드 추가 또는 수정
- 변경 내용, 영향 범위, 테스트 결과, 남은 리스크 정리

## 8. Checks Before Execution
- 관리자용 조회와 참가자용 조회의 기준 식별자가 명확히 구분되어 있는가
- 관리자 API에 소유권 검증이 포함되어 있는가
- 지급 여부 컬럼 추가가 엔티티, DTO, API 응답까지 일관되게 반영되는가
- 참가자용 기존 `/api/draw/results`를 중복 구현하지 않고 현재 구조를 기준으로 다루는가
- `contentCode`를 외부 식별자로 사용하는 규칙을 지키는가
- 스키마 변경 범위가 과도하게 확장되지 않는가
- 파일명만 보고도 순서, 단계, 목적, 작업 내용을 알 수 있는가

## 9. Final Prompt Draft

```md
파일명:
- `07-draft-dev-draw-result-admin-list-and-reward-delivery.md`

다음 공통 규칙을 따른다.
- `prompts/common-rules.md`
- `prompts/modes/development.md`

작업 목표:
- 관리자용 추첨 결과 조회 API를 추가해 `contentCode` 기준으로 해당 콘텐츠의 결과 리스트를 조회할 수 있게 한다.
- 참가자용 추첨 결과 조회는 현재 구현된 `/api/draw/results`를 기준으로 요구사항에 맞게 점검하고, 필요 시 최소 범위로 보완한다.
- `draw_results`에 상품 지급 여부 컬럼을 추가하고, 관리자용 지급 여부 수정 API를 추가한다.

배경:
- 현재 참가자용 추첨 결과 조회 API `/api/draw/results`는 `contentCode`와 `invitationCode` 기준으로 동작한다.
- 현재 관리자용 추첨 결과 조회 API는 없다.
- 현재 `DrawResult` 및 `draw_results`에는 상품 지급 여부를 저장하는 필드가 없다.
- 관리자 기능은 토큰 기반 현재 사용자 확인 후 본인 소유 콘텐츠에 대해서만 허용되어야 한다.

포함 범위:
- 관리자용 추첨 결과 조회 API 추가
- 관리자용 결과 조회 DTO, Service, Repository 조회 로직 추가
- `CurrentUserService` 기반 콘텐츠 소유권 검증 추가 또는 기존 패턴 재사용
- 참가자용 `/api/draw/results`의 현재 동작 점검 및 필요 시 최소 보완
- `draw_results` 지급 여부 컬럼 추가와 `DrawResult` 엔티티 반영
- 관리자용 지급 여부 수정 API 및 요청 DTO 추가
- 필요 시 관련 테스트 코드 추가 또는 수정
- `schema.sql` 반영

제외 범위:
- 추첨 실행 로직 변경
- 보상 CRUD, 초대 코드 CRUD 스펙 변경
- 프론트엔드 수정
- 요구사항 범위를 벗어나는 리팩터링

제약:
- 모든 파일은 UTF-8로 저장한다.
- 기존 구조와 네이밍을 최대한 유지한다.
- 로그인 필요한 관리자 API에서는 요청으로 `userId`를 받지 않는다.
- 외부 API에는 `contentId` 대신 `contentCode`를 사용한다.
- 지급 여부 컬럼 추가 외의 스키마 변경은 최소화한다.
- 테스트가 필요하면 정상 조회, 권한 없는 접근, 존재하지 않는 결과, 지급 여부 변경 케이스를 우선 검증한다.

명명 규칙:
- 기존 패키지와 계층 구조를 따른다.
- 관리자용 결과 응답 DTO와 지급 여부 수정 요청 DTO는 역할이 드러나는 이름으로 작성한다.
- 지급 여부 관련 필드명은 하나의 용어로 통일한다.

완료 조건:
- 관리자가 본인 소유 콘텐츠의 추첨 결과 리스트를 `contentCode` 기준으로 조회할 수 있다.
- 참가자용 결과 조회가 현재 요구사항 기준으로 정상 동작한다.
- 관리자만 개별 추첨 결과의 상품 지급 여부를 수정할 수 있다.
- 지급 여부가 DB 스키마, 엔티티, 응답에 일관되게 반영된다.
- 변경 코드에 대한 테스트 또는 검증 결과가 함께 정리된다.
```
