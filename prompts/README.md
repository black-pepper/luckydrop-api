# Prompt Guide

이 폴더는 AI 작업용 공통 규칙, 목적별 프롬프트, 프롬프트 초안 템플릿을 관리한다.

## Project Summary
- 기술 스택: Java 21, Spring Boot 4, Spring MVC, JPA, PostgreSQL
- 프로젝트 성격: 추첨 이벤트 백엔드 API
- 핵심 API: `/api/draw/verify`, `/api/draw/execute`, `/api/draw/results`, `/api/draw/rewards`
- 핵심 도메인: `Content`, `DrawCode`, `Reward`, `DrawResult`, `User`
- 핵심 흐름: 코드 검증 -> 남은 횟수 확인 -> 보상 조회 -> 가중치 추첨 -> 재고 차감 -> 결과 저장
- 주요 규칙: 비관적 락 사용, 중복 보상 허용 여부 반영, 재고 소진 보상 제외
- 테스트 상태: 기본 애플리케이션 로딩 테스트만 존재

## Recommended Flow
1. 새 작업 번호를 정하고 `templates/prompt-draft-template.md`를 기준으로 초안을 만든다.
2. 초안에 작업 목표, 범위, 제약, 검토 포인트를 채운다.
3. 사용자가 초안을 검수한다.
4. 검수 완료 후 같은 번호로 단계만 바꿔 파일을 만든다.
5. 검수된 초안에 목적별 문서를 함께 붙여 실제 실행한다.

## Naming Convention
- 작업 파일명 형식: `<nn>-<stage>-<type>-<task>.md`
- `nn`: 작업 순서 두 자리 숫자
- `stage`: `draft`, `approved`, `run`
- `type`: `dev`, `verify`, `review`
- `task`: 작업 내용을 짧게 설명하는 kebab-case

예시:
- `01-draft-dev-user-email-dup-check.md`
- `01-approved-dev-user-email-dup-check.md`
- `02-draft-verify-draw-api-regression.md`
- `03-draft-review-reward-service-change.md`

## Files
- `common-rules.md`: 모든 작업에 공통 적용되는 규칙
- `templates/prompt-draft-template.md`: 프롬프트 초안 작성용 공통 템플릿
- `modes/development.md`: 개발 작업용 프롬프트
- `modes/verification.md`: 검증 작업용 프롬프트
- `modes/review.md`: 리뷰 작업용 프롬프트

## Suggested Workspace Layout
```text
prompts/
  common-rules.md
  README.md
  templates/
    prompt-draft-template.md
  modes/
    development.md
    verification.md
    review.md
  tasks/
    01-draft-dev-user-email-dup-check.md
    01-approved-dev-user-email-dup-check.md
    01-run-dev-user-email-dup-check.md
```

## Suggested Usage

프롬프트 초안 생성 요청:

```md
다음 문서를 기준으로 이번 작업용 프롬프트 초안을 작성해줘.

- `prompts/common-rules.md`
- `prompts/templates/prompt-draft-template.md`
- `prompts/modes/development.md`

작업 목표:
- 관리자용 추첨 결과 조회 API 추가

제약:
- 기존 서비스 구조 유지
- 파일 변경은 최소화
- 테스트 코드도 함께 고려
```

실행 요청:

```md
다음 문서를 기준으로 작업을 수행해줘.

- `prompts/common-rules.md`
- `prompts/modes/development.md`

확정 프롬프트:
- 관리자용 추첨 결과 조회 API를 추가한다.
- 테스트 코드까지 함께 작성한다.
- 기존 패턴을 유지하고 변경 이유를 간단히 설명한다.
```
