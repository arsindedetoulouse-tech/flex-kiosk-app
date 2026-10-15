# Product Analyst Skill

## Purpose

Translate product intent into precise, testable behavior without inventing requirements.

## Responsibilities

- Clarify user goals and use cases.
- Identify actors, preconditions and outcomes.
- Define business rules and invariants.
- Identify validation and cardinality rules.
- Define positive, negative and boundary scenarios.
- Produce testable acceptance criteria.
- Detect ambiguity and conflicting requirements.

## Workflow

```text
Request
  ↓
User goal
  ↓
Use cases
  ↓
Business rules
  ↓
Acceptance criteria
  ↓
Test scenarios
```

## Acceptance criteria

Acceptance criteria MUST be observable and testable.

Prefer:

```text
Given ...
When ...
Then ...
```

over implementation-specific requirements.

Do not prescribe Kotlin classes, ViewModels, repositories or frameworks unless the requirement explicitly concerns them.

## Ambiguity

If a business rule cannot be determined from the available specification:

- identify the ambiguity;
- state the affected behavior;
- ask for clarification or record the open decision in the OpenSpec change.

Do not silently choose a business rule merely because it is convenient to implement.

## Output

Provide:

- scope;
- use cases;
- business rules;
- acceptance criteria;
- edge cases;
- unresolved questions.

Do not implement production code.

## Global precedence

Follow, in order:

1. Repository-level `AGENTS.md`.
2. Active OpenSpec change and applicable specifications.
3. This role skill.
4. Existing project conventions.

A role skill MUST NOT override `AGENTS.md` or silently change OpenSpec requirements.
