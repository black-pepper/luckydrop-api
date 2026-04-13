# 05 Draft Dev Reward Admin CRUD API

파일명
- `05-draft-dev-reward-admin-crud-api.md`

다음 공통 규칙을 따른다.
- `prompts/common-rules.md`
- `prompts/modes/development.md`

작업 목표:
- 관리자용 추첨 상품(Reward) CRUD API를 추가한다.
- 관리자 권한 확인을 위해 토큰 기반으로 현재 사용자를 식별하고, 해당 상품이 속한 컨텐츠(Content)의 소유권(User)을 검증한다.
- 외부 API 계약에서는 `contentId`를 사용하지 않고 `contentCode`를 사용한다.

배경:
- 이 프로젝트는 Supabase JWT를 사용하여 사용자를 인증한다.
- 현재 `CurrentUserService`를 통해 로그인한 사용자 정보를 가져올 수 있다.
- `Reward`는 `Content`에 속하며 `Content`는 `User`에 속한다.
- 관리자는 본인 소유 컨텐츠의 상품만 관리할 수 있어야 한다.
- `contentId`는 내부 식별자 또는 디버깅 용도로만 사용하고, API 요청/응답과 URL/쿼리 파라미터에는 `contentCode`를 사용한다.

포함 범위:
- `AdminRewardController`: `/api/manage/rewards` 엔드포인트 구현
  - `GET /api/manage/rewards?contentCode={contentCode}`: 특정 컨텐츠의 모든 상품 목록 조회
  - `GET /api/manage/rewards/{rewardId}`: 특정 상품 상세 조회
  - `POST /api/manage/rewards`: 새 상품 생성, body에 `contentCode` 포함
  - `PUT /api/manage/rewards/{rewardId}`: 상품 정보 수정
  - `DELETE /api/manage/rewards/{rewardId}`: 상품 삭제 또는 `active=false` 처리
- `RewardService` 또는 `RewardAdminService`: CRUD 로직 및 컨텐츠 소유권 검증 로직 추가
- `RewardCreateRequest`, `RewardUpdateRequest` DTO 추가 또는 수정
- `ContentRepository`: `contentCode`로 `Content` 조회
- `RewardRepository`: 관리자용 조회 메서드 추가
- `ErrorCode`: `FORBIDDEN_CONTENT_ACCESS` 등 소유권 관련 에러 코드 추가

제외 범위:
- 상품 이외의 CRUD
- 프론트엔드 작업

제약:
- 모든 파일은 UTF-8로 저장한다.
- 기존 계층 구조(Controller-Service-Repository)와 네이밍 규칙을 따른다.
- `/api/manage/contents`를 참고하여 유사한 URL 구조와 보안 처리를 유지한다.
- 소유권 검증 실패 시 명확한 예외(예: 403 Forbidden)를 반환한다.
- 모든 응답은 `ApiResponse` 공통 형식을 사용한다.
- 외부 API 스펙에는 `contentId`를 노출하지 않는다.

명명 규칙:
- 관리자용 컨트롤러: `AdminRewardController`
- 요청 DTO: `RewardCreateRequest`, `RewardUpdateRequest`
- 서비스 메서드: `getRewardsByContent`, `createReward`, `updateReward`, `deleteReward`
- 컨텐츠 식별자는 `contentCode`를 사용한다.

완료 조건:
- 본인 소유 컨텐츠의 상품에 대해서만 CRUD가 정상 동작한다.
- 타인 소유 컨텐츠의 상품 접근은 차단된다.
- API 요청과 응답에서 `contentId` 대신 `contentCode`를 사용한다.
- API 응답은 `ApiResponse` 공통 형식을 따른다.
- Swagger에 관리자 API가 정상적으로 노출된다.
