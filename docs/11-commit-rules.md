# 커밋 규칙

## 목적

커밋 메시지는 평가자가 변경 이력을 빠르게 이해할 수 있도록 명확하고 일관되게 작성합니다. 기능, 문서, 테스트, 설정 변경을 구분하면 포트폴리오 리뷰 시 어떤 의도로 변경했는지 설명하기 쉽습니다.

## 기본 형식

가능하면 Conventional Commits 스타일을 사용합니다.

```text
type(scope): summary
```

예시:

```text
feat(rest): add real REST simulator execution
fix(batch): handle forceFail=false correctly
docs: translate portfolio documentation into Korean for evaluator review
test(monitoring): cover dashboard protocol summaries
```

## Type Guide

| Type | 의미 |
| --- | --- |
| `feat` | 새로운 기능 |
| `fix` | 버그 수정 |
| `docs` | 문서 변경 |
| `test` | 테스트 추가 또는 수정 |
| `refactor` | 동작 변경 없는 구조 개선 |
| `perf` | 성능 개선 |
| `chore` | 빌드, 설정, 정리 작업 |

## 작성 원칙

- 한 커밋에는 하나의 의도를 담는다.
- 이미 적용된 Flyway migration을 수정하지 않는다.
- 실제 secret이나 local password를 포함하지 않는다.
- 문서 변경과 동작 변경은 가능하면 분리한다.
- 커밋 전 `.\gradlew.bat test` 또는 영향 범위에 맞는 검증을 수행한다.
- 제출 전 `git diff`로 local-only 설정이 포함되지 않았는지 확인한다.

## Phase 단위 커밋 예시

```text
feat(interfacehub): add common execution history and retry flow
feat(soap): add local SOAP simulator and executor
feat(batch): add Spring Batch run history and scheduler support
perf(monitoring): aggregate protocol dashboard counts with grouped queries
docs: translate portfolio documentation into Korean for evaluator review
```
