# Code Reviewer Skill

## Purpose

Independently evaluate a change against requirements, architecture, tests, security, and maintainability.

## Review order

Review in this order:

```text
OpenSpec scope
    ↓
Acceptance criteria
    ↓
Architecture
    ↓
Implementation
    ↓
Tests
    ↓
Security
    ↓
Maintainability
```

## Review checklist

### Scope

- Is the implementation limited to the requested change?
- Is there unrelated refactoring?
- Are generated/local files untouched?

### Requirements

- Are acceptance criteria satisfied?
- Are failure and boundary cases handled?

### Architecture

- Are module boundaries preserved?
- Is dependency direction valid?
- Are domain rules in the correct layer?
- Are DTOs/entities/Ktor types prevented from leaking?

### Compose / MVI

- Is state ownership correct?
- Are Screen and Route responsibilities separated?
- Are one-time effects represented correctly?
- Is navigation outside domain/data?

### DI

- Is Koin wiring correct?
- Are parameters legitimate?
- Are runtime UI callbacks kept out of DI?

### Tests

- Was TDD followed?
- Is there a meaningful test for each new behavior?
- Are tests deterministic?
- Are assertions strong enough?
- Are edge/failure cases covered?

### Security

- No credentials or secrets?
- No sensitive logging?
- No unnecessary permissions?
- No weakened security controls?

## Findings

Classify findings:

```text
BLOCKER   — must fix before approval
MAJOR     — should fix before approval
MINOR     — non-blocking improvement
INFO      — observation
```

A finding must include:

- location;
- problem;
- why it matters;
- recommended direction.

Do not silently modify the code while performing review.

## Approval

Approve only when:

- requirements are satisfied;
- no blocker/major findings remain;
- required tests pass;
- security concerns are resolved;
- the implementation remains within OpenSpec scope.

## Handoff

Return:

- approval status;
- findings;
- verification evidence;
- residual risks.

## Global precedence

Follow, in order:

1. Repository-level `AGENTS.md`.
2. Active OpenSpec change and applicable specifications.
3. This role skill.
4. Existing project conventions.

A role skill MUST NOT override `AGENTS.md` or silently change OpenSpec requirements.
