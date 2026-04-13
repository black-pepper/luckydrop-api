# 03 Draft Dev Admin Content API

파일명:
- `03-draft-dev-admin-content-api.md`

다음 공통 규칙을 따른다.
- `prompts/common-rules.md`
- `prompts/modes/development.md`

작업 목표:
- 관리자 페이지에서 사용할 콘텐츠 관리 API를 추가한다.
- 콘텐츠 생성, 단건 상세 조회, 사용자가 생성한 콘텐츠 목록 조회, 콘텐츠 수정, 콘텐츠 삭제 기능을 백엔드에 구현한다.
- 기존 `contents` 테이블과 `Content` 엔티티를 기준으로 API 구조를 정리하고, 현재 프로젝트의 응답 형식과 예외 처리 방식에 맞춘다.
- 외부 노출용 콘텐츠 식별자는 숫자 `id` 대신 별도 `code`를 사용하도록 구조를 정리한다.

배경:
- 현재 프로젝트에는 `public.contents` 테이블과 `Content` 엔티티가 이미 존재한다.
- `Content` 엔티티는 `id`, `createdAt`, `type`, `user`, `title`, `description`, `deletedAt` 필드를 가진다.
- 현재 공개 API는 추첨 관련 기능 위주이며, 관리자 페이지에서 콘텐츠를 직접 관리하는 API는 아직 없다.
- `contents.deleted_at` 컬럼이 있어 삭제는 소프트 삭제 방식이 더 자연스럽다.
- `contents.user_id`는 `users.id`를 참조하므로, 관리자 목록/상세 API에서는 콘텐츠 작성자 정보가 함께 필요할 수 있다.
- 향후 참여자는 URL 또는 공개 API를 통해 콘텐츠를 식별할 가능성이 높다.
- 숫자 증가형 `id`를 외부에 노출하면 다른 콘텐츠를 유추하거나 순차 조회하기 쉬워질 수 있다.
- 따라서 `contents`에 외부 식별용 `code` 컬럼을 추가하고, 외부 요청/응답과 URL에는 `code`를 사용하는 방향을 우선한다.
- 숫자 `id`는 내부 조인, 데이터 관리, 디버깅 용도로만 유지하는 것이 더 적합하다.

참고 파일:
- `src/main/java/com/luckydrop/api/domain/content/entity/Content.java`
- `src/main/resources/schema.sql`
- `src/main/java/com/luckydrop/api/common/response/ApiResponse.java`
- `src/main/java/com/luckydrop/api/common/exception/ErrorCode.java`
- `src/main/java/com/luckydrop/api/controller/DrawController.java`
- `src/main/java/com/luckydrop/api/service/CurrentUserService.java`

포함 범위:
- 관리자용 콘텐츠 API 엔드포인트 추가
- `contents` 테이블에 외부 식별용 `code` 컬럼 추가
- `Content` 엔티티에 `code` 필드 반영
- 콘텐츠 생성 요청/응답 DTO 추가
- 콘텐츠 상세 조회 응답 DTO 추가
- 사용자 생성 콘텐츠 목록 조회 응답 DTO 추가
- 콘텐츠 수정 요청/응답 DTO 추가
- 콘텐츠 삭제 처리 추가
- `Content` 조회/저장/삭제를 위한 Repository 추가 또는 확장
- 서비스 계층에서 콘텐츠 생성/조회/수정/삭제 흐름 구현
- 콘텐츠 생성 시 고유 `code`를 생성하고 저장하는 흐름 추가
- 삭제된 콘텐츠를 목록/상세 조회에서 어떻게 처리할지 정책 정리
- 필요 시 사용자 정보 일부를 포함한 목록/상세 응답 구조 정리
- 정상 흐름, 조회 실패, 삭제된 데이터 처리 등 기본 검증 또는 테스트 코드 추가 검토

제외 범위:
- 관리자 로그인 화면 구현
- 별도 관리자 권한(Role) 체계의 대규모 설계
- 프론트엔드 관리자 페이지 UI 구현
- 콘텐츠 외 다른 도메인(보상, 초대 코드, 추첨 결과)의 관리자 CRUD 확장
- 페이징, 정렬, 복합 검색 조건의 과도한 확장
- `contents` 스키마 자체를 크게 바꾸는 작업

제약:
- 모든 파일은 UTF-8로 저장한다.
- 기존 프로젝트 구조와 네이밍 스타일을 최대한 유지한다.
- 변경 범위는 콘텐츠 관리 API에 필요한 최소 수준으로 제한한다.
- 컨트롤러 응답은 기존 `ApiResponse` 형식에 맞춘다.
- 예외 처리는 기존 `ErrorCode`, 전역 예외 처리 방식과 충돌하지 않게 맞춘다.
- 콘텐츠 삭제는 현재 스키마를 고려해 `deletedAt` 기반 소프트 삭제를 우선 검토한다.
- 삭제된 콘텐츠를 다시 노출하지 않도록 조회 조건을 일관되게 적용한다.
- 관리자 API 경로는 현재 프로젝트 URL 구조와 충돌하지 않게 명확히 구분한다.
- 외부에 노출되는 콘텐츠 식별자는 `code`로 통일하고, 숫자 `id`는 외부 API 계약에 포함하지 않는 방향을 우선한다.
- `code`는 예측이 어렵고 유일해야 하며, 생성 규칙은 구현 복잡도를 과도하게 늘리지 않는 범위에서 정한다.

명명 규칙:
- 관리자용 컨트롤러는 역할이 드러나는 이름을 사용한다.
- 예: `AdminContentController`
- 서비스는 책임이 드러나는 이름을 사용한다.
- 예: `AdminContentService`
- 저장소는 도메인 기준으로 `ContentRepository`처럼 작성한다.
- DTO 이름은 용도를 드러나게 작성한다.
- 예: `AdminContentCreateRequest`, `AdminContentDetailResponse`, `AdminContentListResponse`, `AdminContentUpdateRequest`
- 프롬프트와 코드에서 `content`, `admin`, `deleted`, `code` 용어를 혼용하지 않고 일관되게 사용한다.

구현 지침:
- 관리자 API는 `/api/manage/contents` 계열 경로를 우선 검토한다.

- `POST /api/manage/contents`: 콘텐츠 생성
- `GET /api/manage/contents/{contentCode}`: 콘텐츠 상세 조회
- `GET /api/manage/contents`: 사용자 생성 콘텐츠 목록 전체 조회
- `PUT` 또는 `PATCH /api/manage/contents/{contentCode}`: 콘텐츠 수정
- `DELETE /api/manage/contents/{contentCode}`: 콘텐츠 삭제
- `Content.type`은 현재 스키마의 허용값(`DRAW`, `QUIZ`) 범위를 벗어나지 않도록 검증한다.
- 생성/수정 시 `title`, `description`, `type`, `userId` 중 어떤 값이 필수인지 현재 도메인 문맥에 맞게 명확히 정한다.
- 콘텐츠 외부 식별자는 생성 시 자동으로 부여되는 `code`를 사용한다.
- API 요청 경로, 응답 바디, 관리자 페이지 연동 값에는 `id` 대신 `code`를 사용한다.
- 숫자 `id`는 내부 DB 식별과 디버깅 용도로만 남기고, 외부 계약에는 포함하지 않는 방향을 우선한다.
- `code` 컬럼에는 유니크 제약 또는 동등한 무결성 보장이 필요하다.
- `ContentRepository`는 `id` 기반 내부 조회와 `code` 기반 외부 조회 책임을 구분해서 다룰 수 있어야 한다.
- 상세 조회와 목록 조회에서는 삭제된 콘텐츠를 기본적으로 제외할지, 상세 조회 시 404로 처리할지 정책을 코드와 응답에서 일관되게 맞춘다.
- 목록 조회는 “사용자가 생성한 콘텐츠 전체 보기” 요구를 충족하도록 작성자 기준 콘텐츠들을 반환할 수 있어야 한다.
- 필요하면 사용자 이름, 사용자 ID 등 관리자 화면에 필요한 최소 작성자 정보를 응답에 포함한다.
- JPA 조회 시 사용자 연관 객체 접근 때문에 N+1 문제가 생기지 않도록 필요한 조회 방식을 검토한다.
- 현재 프로젝트에 `ContentRepository`가 없으므로 새로 추가하는 방향을 우선 검토한다.
- 삭제 API는 실제 row 제거보다 `deletedAt` 갱신 방식이 더 적합한지 우선 판단하고, 그 정책을 구현과 문서에 분명히 드러낸다.
- 예외 상황은 최소한 존재하지 않는 콘텐츠, 이미 삭제된 콘텐츠, 잘못된 요청값을 구분해 다룬다.

완료 조건:
- `contents` 테이블에 외부 식별용 `code` 컬럼이 추가되어 있다.
- `Content` 엔티티에 `code` 필드가 반영되어 있다.
- 관리자 페이지에서 사용할 콘텐츠 생성 API가 추가되어 있다.
- 콘텐츠 상세 조회 API가 추가되어 있다.
- 사용자 생성 콘텐츠 목록 전체 조회 API가 추가되어 있다.
- 콘텐츠 수정 API가 추가되어 있다.
- 콘텐츠 삭제 API가 추가되어 있다.
- `Content` 저장/조회/수정/삭제를 담당하는 계층 구조가 현재 프로젝트 스타일에 맞게 정리되어 있다.
- 외부 요청/응답과 URL에서는 콘텐츠 식별자로 `code`를 사용한다.
- 숫자 `id`는 외부 API 계약에 노출하지 않고 내부 식별 용도로만 유지한다.
- 삭제 정책이 코드에서 일관되게 적용되어 있다.
- 응답 형식과 예외 처리 방식이 기존 프로젝트 스타일과 맞춰져 있다.
- 필요한 DTO, Repository, Service, Controller가 정리되어 있다.
- 가능하면 관련 테스트 또는 검증 결과가 함께 정리되어 있다.
