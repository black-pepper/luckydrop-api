# Prompt Draft Template

이 문서는 "실행용 프롬프트"를 만들기 전, 사용자가 먼저 검수할 수 있는 초안 템플릿이다.

## 1. Task Type
- Development
- Verification
- Review

## 1-1. Prompt File Name
- 작업 번호:
- 단계: `draft`
- 유형: `dev | verify | review`
- 작업 슬러그:
- 최종 파일명: `<nn>-<stage>-<type>-<task>.md`
- 예시: `01-draft-dev-draw-result-admin-api.md`

## 2. Goal
- 이번 작업에서 달성하려는 목표를 한두 문장으로 적는다.

## 3. Background
- 관련 기능 또는 버그 맥락
- 참고해야 할 파일, API, 도메인
- 이미 알려진 제약 사항

## 4. Scope
### Include
- 이번 작업에 반드시 포함할 범위

### Exclude
- 이번 작업에서 제외할 범위

## 5. Constraints
- 모든 파일은 UTF-8로 저장
- 기존 구조와 네이밍 유지
- 변경 범위 최소화
- 필요 시 테스트 또는 검증 포함

## 6. Naming Plan
- 새로 만드는 파일명:
- 새로 만드는 클래스/인터페이스명:
- 새로 만드는 메서드/함수명:
- 새로 만드는 테스트명:
- 용어 통일 기준:

## 7. Deliverables
- AI가 최종적으로 만들어야 할 결과물
- 예: 코드 수정, 테스트 코드 추가, 위험 요소 정리, 검증 결과 보고

## 8. Checks Before Execution
- 목표가 충분히 구체적인가
- 제외 범위가 명확한가
- 위험한 변경 여부가 드러나는가
- 성공 기준이 명확한가
- 이름이 기존 프로젝트 규칙과 충돌하지 않는가
- 같은 개념에 다른 용어를 섞어 쓰지 않는가
- 파일명만 보고도 순서, 단계, 목적, 작업 내용이 보이는가

## 9. Final Prompt Draft
아래 형식으로 최종 실행용 프롬프트 초안을 작성한다.

```md
파일명:
- <nn>-<stage>-<type>-<task>.md

다음 공통 규칙을 따른다.
- `prompts/common-rules.md`
- `prompts/modes/<mode>.md`

작업 목표:
- ...

배경:
- ...

포함 범위:
- ...

제외 범위:
- ...

제약:
- ...

명명 규칙:
- ...

완료 조건:
- ...
```
