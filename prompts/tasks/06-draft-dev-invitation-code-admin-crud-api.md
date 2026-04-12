# 06 Draft Dev Invitation Code Admin CRUD API

파일명:
- 06-draft-dev-invitation-code-admin-crud-api.md

다음 공통 규칙을 따른다.
- `prompts/common-rules.md`
- `prompts/modes/development.md`

작업 목표:
- 관리자용 초대 코드(DrawCode) CRUD API를 추가한다.
- 관리자 권한 확인을 위해 토큰 기반으로 현재 사용자를 식별하고, 해당 코드가 속한 컨텐츠(Content)의 소유권(User)을 검증한다.

배경:
- DB 테이블 `invitation_codes`는 엔티티 `DrawCode`와 매핑되어 있다.
- 관리자는 본인이 소유한 컨텐츠의 초대 코드만 생성, 조회, 수정, 삭제할 수 있어야 한다.
- 초대 코드는 특정 컨텐츠 내에서 유일해야 하며(`unique(content_id, code)`), 허용 횟수와 만료일 설정을 포함한다.

포함 범위:
- `AdminDrawCodeController`: `/api/admin/draw-codes` 엔드포인트 구현
  - `GET /api/admin/draw-codes?contentId={contentId}`: 특정 컨텐츠의 모든 초대 코드 목록 조회
  - `GET /api/admin/draw-codes/{drawCodeId}`: 특정 초대 코드 상세 조회
  - `POST /api/admin/draw-codes`: 새 초대 코드 생성 (body에 `contentId`, `code`, `allowedDrawCount`, `expiresAt` 등 포함)
  - `PUT /api/admin/draw-codes/{drawCodeId}`: 초대 코드 정보 수정 (이름, 허용 횟수, 만료일, 활성화 여부 등)
  - `DELETE /api/admin/draw-codes/{drawCodeId}`: 초대 코드 삭제 (또는 `active=false` 처리)
- `DrawCodeService` (또는 `DrawCodeAdminService` 분리): CRUD 및 소유권 검증 로직 구현
- `DrawCodeCreateRequest`, `DrawCodeUpdateRequest`, `DrawCodeResponse` DTO 추가
- `DrawCodeRepository`: `findByContentId` 등 관리자 조회용 메서드 추가

제외 범위:
- 초대 코드 이외의 도메인 CRUD
- 추첨 참여 로직 수정

제약:
- 모든 파일은 UTF-8로 저장한다.
- 기존 계층 구조와 엔티티 네이밍(`DrawCode`)을 따른다.
- `/api/admin/contents`를 참고하여 일관된 URL 구조와 보안 처리를 유지한다.
- 소유권 검증 실패 시 적절한 예외(403 Forbidden)를 발생시킨다.
- 모든 응답은 `ApiResponse` 공통 형식을 사용한다.

명명 규칙:
- 관리자용 컨트롤러: `AdminDrawCodeController`
- 요청/응답 DTO: `DrawCodeCreateRequest`, `DrawCodeUpdateRequest`, `DrawCodeResponse`
- 서비스 메서드: `getDrawCodesByContent`, `createDrawCode`, `updateDrawCode`, `deleteDrawCode`

완료 조건:
- 본인 소유 컨텐츠의 초대 코드에 대해서만 CRUD가 정상 동작함을 확인한다.
- 타인 소유 컨텐츠의 코드 접근 시 차단됨을 확인한다.
- 중복된 코드 생성 시도 시 적절한 에러 처리가 되는지 확인한다.
- API 응답이 `ApiResponse` 형식을 따르는지 확인한다.
