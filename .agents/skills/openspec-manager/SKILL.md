# OpenSpec Manager Skill

## Purpose

Own the lifecycle of an OpenSpec change and keep the implementation synchronized with the specification.

## Core rule

OpenSpec is the source of truth for non-trivial product changes.

The agent MUST NOT silently expand or redefine a change during implementation.

## Workflow

```text
Explore
  ↓
Propose
  ↓
Requirements
  ↓
Design
  ↓
Implementation
  ↓
Verification
  ↓
Archive
```

## Before implementation

1. Identify the active OpenSpec change.
2. Read the change requirements and affected specifications.
3. Determine the explicit scope.
4. Identify missing or ambiguous requirements.
5. Identify impacted modules and architecture.
6. Hand requirements to `product-analyst` when business behavior is unclear.
7. Hand technical impact to `android-architect` when architecture is affected.

Do not start implementation merely because a ticket title sounds sufficiently clear.

## Requirements changes

If implementation reveals that a requirement is incomplete, contradictory, or materially different from the requested behavior:

- stop and identify the discrepancy;
- update the OpenSpec change when authorized by the workflow;
- do not encode an undocumented product decision in production code.

## Scope control

Reject unrelated cleanup, speculative abstractions, broad refactoring, and opportunistic dependency upgrades unless they are explicitly part of the change.

## Verification

Before considering an OpenSpec change complete, verify:

- requirements are satisfied;
- acceptance criteria have evidence;
- implementation is within scope;
- tests exist for new behavior;
- TDD evidence is available where applicable;
- affected Gradle verification passes;
- review findings are resolved or explicitly accepted.

## Completion

Do not archive or mark a change complete while required implementation, tests, or review remain unresolved.

## Handoff

Output:

- change scope;
- affected specifications;
- acceptance criteria;
- implementation scope;
- unresolved questions;
- verification status.

## Global precedence

Follow, in order:

1. Repository-level `AGENTS.md`.
2. Active OpenSpec change and applicable specifications.
3. This role skill.
4. Existing project conventions.

A role skill MUST NOT override `AGENTS.md` or silently change OpenSpec requirements.
