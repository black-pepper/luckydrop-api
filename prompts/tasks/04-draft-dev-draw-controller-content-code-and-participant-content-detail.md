# Prompt Draft

## 1. Task Type
- Development

## 1-1. Prompt File Name
- 작업 번호: `04`
- 단계: `draft`
- 유형: `dev`
- 작업 슬러그: `draw-controller-content-code-and-participant-content-detail`
- 최종 파일명: `04-draft-dev-draw-controller-content-code-and-participant-content-detail.md`

## 2. Goal
- `DrawController`의 추첨 실행 API가 기존 invitation code 단일 입력이 아니라 `contents.code`와 `invitation_codes.code`를 함께 입력받도록 변경한다.
- 참가자가 `contents.code`로 콘텐츠 상세 정보를 조회할 수 있는 API를 추가한다.

## 3. Background
- 현재 `DrawController`의 `/api/draw/execute`는 `DrawRequest.code` 하나만 받아 추첨을 수행한다.
- 현재 추첨 로직은 `DrawCodeRepository.findByCodeWithLock()` 기준으로 invitation code만 조회한 뒤 해당 코드에 연결된 콘텐츠로 보상과 결과를 처리한다.
- 현재 콘텐츠 상세 조회는 관리자 전용 `AdminContentController`의 `/api/admin/contents/{contentCode}`만 존재하며, 참가자용 공개 조회 API는 없다.
- 관련 주요 파일:
  - `src/main/java/com/luckydrop/api/controller/DrawController.java`
  - `src/main/java/com/luckydrop/api/domain/drawresult/dto/DrawRequest.java`
  - `src/main/java/com/luckydrop/api/service/DrawService.java`
  - `src/main/java/com/luckydrop/api/controller/AdminContentController.java`
  - `src/main/java/com/luckydrop/api/service/AdminContentService.java`
  - `src/main/java/com/luckydrop/api/domain/content/repository/ContentRepository.java`
- 추정:
  - 참가자용 콘텐츠 상세 응답은 관리자용 상세 응답과 완전히 같을 필요는 없고, 공개 가능한 필드만 노출하는 별도 DTO가 더 적절할 수 있다.
  - 추첨 실행 시 전달받은 `contentCode`와 `invitationCode`가 서로 매핑되는지 검증이 필요하다.

## 4. Scope
### Include
- `/api/draw/execute` 요청 스펙을 변경해 `contentCode`와 `invitationCode`를 함께 받도록 수정
- `DrawService` 및 관련 조회 로직을 수정해 invitation code와 content code의 조합을 검증
- 잘못된 조합 또는 존재하지 않는 코드에 대한 예외 처리 검토 및 반영
- 참가자용 콘텐츠 상세 조회 API 추가
- 참가자용 콘텐츠 상세 조회에 필요한 Service, DTO, Repository 조회 로직 추가 또는 재사용
- 필요 시 해당 변경을 검증하는 테스트 코드 추가 또는 수정

### Exclude
- 관리자용 콘텐츠 관리 API의 동작 변경
- DB 스키마 변경
- 인증/인가 정책의 신규 도입 또는 변경
- 추첨 결과 조회, 보상 조회, 코드 검증 API의 요청/응답 스펙 변경
- 프론트엔드 또는 외부 클라이언트 코드 수정

## 5. Constraints
- 모든 파일은 UTF-8로 저장한다.
- 기존 패키지 구조, 계층 구조, 네이밍 패턴을 최대한 유지한다.
- 변경은 필요한 최소 범위로 수행한다.
- 기존 동작에 영향이 있으면 영향 범위와 리스크를 명확히 정리한다.
- 버전, 의존성, 스키마는 임의로 변경하지 않는다.
- 테스트가 필요한 변경이면 정상 흐름, 실패 흐름, 코드 불일치 같은 경계 케이스를 우선 검증한다.

## 6. Naming Plan
- 새로 만드는 파일명:
  - 필요 시 참가자용 콘텐츠 상세 응답 DTO 파일
  - 필요 시 참가자용 콘텐츠 조회 컨트롤러 또는 서비스 파일
- 새로 만드는 클래스/인터페이스명:
  - 예시: `ParticipantContentDetailResponse`
  - 예시: `ContentController` 또는 기존 `DrawController` 내 참가자 조회 API 추가 여부를 먼저 확인
- 새로 만드는 메서드/함수명:
  - 예시: `getContentDetail`
  - 예시: `draw`
  - 예시: `getActiveContent`
  - 예시: `validateContentAndInvitationCode`
- 새로 만드는 테스트명:
  - content code와 invitation code가 모두 유효할 때 추첨이 수행된다
  - invitation code가 다른 content에 속하면 예외가 발생한다
  - content code로 참가자용 콘텐츠 상세 조회가 가능하다
- 용어 통일 기준:
  - `contentCode`는 `contents.code`
  - `invitationCode`는 `invitation_codes.code`
  - `code` 같은 단일 이름은 의미가 모호하므로 새 요청/응답/메서드에서는 가능한 한 구체화한다

## 7. Deliverables
- draw 실행 요청 DTO 및 관련 컨트롤러/서비스 코드 수정
- 참가자용 콘텐츠 상세 조회 API 구현
- 필요한 Repository 조회 또는 검증 로직 반영
- 테스트 코드 추가 또는 수정
- 변경 내용, 영향 범위, 테스트 결과, 남은 리스크를 정리한 보고

## 8. Checks Before Execution
- 목표가 현재 API 변경 사항과 신규 API 추가 요구를 모두 포함하는가
- `contentCode`와 `invitationCode`의 책임이 명확히 분리되어 있는가
- 잘못된 코드 조합 검증이 범위에 포함되어 있는가
- 참가자용 상세 조회에서 공개하면 안 되는 필드가 없는지 검토 대상이 명시되어 있는가
- 기존 관리자 API와 혼동되지 않는 이름과 경로를 사용할 수 있는가
- 파일명만 보고도 순서, 단계, 목적, 작업 내용을 알 수 있는가

## 9. Final Prompt Draft

```md
파일명:
- `04-draft-dev-draw-controller-content-code-and-participant-content-detail.md`

다음 공통 규칙을 따른다.
- `prompts/common-rules.md`
- `prompts/modes/development.md`

작업 목표:
- `DrawController`의 추첨 실행 API를 수정해 기존 단일 code 입력 대신 `contentCode`와 `invitationCode`를 함께 받도록 변경한다.
- 참가자용 콘텐츠 상세 조회 API를 추가해 `contents.code`로 콘텐츠 정보를 조회할 수 있게 한다.

배경:
- 현재 `/api/draw/execute`는 `DrawRequest.code` 하나만 받아 invitation code 기준으로 추첨을 수행한다.
- 현재 콘텐츠 상세 조회는 관리자 전용 `/api/admin/contents/{contentCode}`만 존재한다.
- 이번 작업에서는 참가자 흐름에서 content와 invitation code를 분리해 다루도록 API를 명확히 바꿔야 한다.

포함 범위:
- `DrawRequest` 및 draw 실행 API 요청 스펙 변경
- `DrawService`의 조회 및 검증 로직 수정
- `contentCode`와 `invitationCode`의 매핑 검증 추가
- 참가자용 콘텐츠 상세 조회 API, DTO, Service 로직 추가 또는 기존 로직 재사용
- 필요 시 관련 테스트 코드 추가 또는 수정

제외 범위:
- 관리자용 콘텐츠 CRUD API 변경
- DB 스키마 변경
- 의존성, 버전, 설정 변경
- 추첨 결과 조회/보상 조회/코드 검증 API의 별도 스펙 변경

제약:
- 모든 파일은 UTF-8로 저장한다.
- 기존 구조와 네이밍을 최대한 유지한다.
- 변경은 최소 범위로 수행한다.
- 새 이름은 `contentCode`, `invitationCode`처럼 의미가 드러나게 작성한다.
- 테스트가 필요하면 정상 흐름, 실패 흐름, 코드 불일치 케이스를 우선 검증한다.

명명 규칙:
- 기존 패키지와 계층 구조를 따른다.
- 참가자용 콘텐츠 상세 응답은 관리자용 응답과 분리 필요 시 별도 DTO로 만든다.
- 단일 `code` 대신 의미가 분명한 이름을 우선 사용한다.

완료 조건:
- draw 실행 API가 `contentCode`와 `invitationCode`를 함께 받아 정상 동작한다.
- 서로 맞지 않는 `contentCode`와 `invitationCode` 조합에서 적절한 예외가 발생한다.
- 참가자용 콘텐츠 상세 조회 API가 `contents.code` 기준으로 정상 응답한다.
- 변경 코드에 필요한 테스트 또는 검증 결과가 함께 정리된다.
```
