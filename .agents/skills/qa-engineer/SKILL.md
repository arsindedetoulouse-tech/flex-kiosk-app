# QA Engineer Skill

## Purpose

Own test strategy, TDD discipline, deterministic verification, and regression protection.

## TDD is mandatory

All new features and non-trivial behavior changes MUST follow:

```text
Red → Green → Refactor
```

### Red

1. Translate an acceptance criterion into a focused test.
2. Run the test before implementing production behavior.
3. Verify that it fails for the expected reason.

A test that already passes does not demonstrate the required Red step.

### Green

Implement the minimum production behavior required to pass.

Do not weaken assertions or remove tests to make the build green.

### Refactor

Refactor only after tests are green.

Keep tests green during refactoring.

## Test scope

Cover, as applicable:

- ViewModel state transitions and side effects;
- use cases and business logic;
- repository/data-source behavior;
- DTO serialization/deserialization;
- mappers;
- Koin graph and parameterized resolution;
- validation;
- business cardinality;
- navigation routing;
- loading/success/empty/error states;
- coroutine and Flow behavior;
- failure handling;
- edge and boundary cases.

## Test quality

Tests MUST:

- verify observable behavior;
- be deterministic and isolated;
- use meaningful names;
- avoid private implementation details;
- cover success and failure;
- reuse `common:testing`;
- avoid unnecessary mocks.

Prefer lightweight fakes and fixtures where practical.

## Coroutine and Flow testing

Use deterministic synchronization and virtual time.

Do not use long `Thread.sleep()` or arbitrary `delay()` to prove that something does not happen.

Prefer:

- test dispatchers;
- virtual time;
- explicit collection;
- bounded assertions;
- deterministic event synchronization.

Cancellation must be tested where behavior depends on it.

## Test doubles

Use the simplest suitable double:

```text
Fake > Stub > Mock
```

Use mocks only when interaction verification is meaningful.

Do not mock implementation details merely to make a test easier.

## Regression testing

Before changing existing behavior:

1. Run relevant existing tests.
2. Add or update tests describing the new behavior.
3. Confirm old behavior that must remain unchanged.

Never modify a test solely because the implementation is difficult to satisfy.

## Commands

Typical commands:

```bash
./gradlew :app:assembleDebug
./gradlew testDebugUnitTest
./gradlew :feature:<feature>:testDebugUnitTest
./gradlew :feature:<feature>:testDebugUnitTest --tests "<TestClass>"
./gradlew lint
```

Run the smallest affected test first during TDD, then the affected module suite, then broader verification when practical.

## Test failure policy

A failing test is evidence, not an obstacle.

Do not:

- disable it;
- mark it flaky without evidence;
- add arbitrary sleeps;
- weaken assertions;
- delete it.

Investigate the production code, test setup, or requirement.

## QA handoff

Report:

- tests added;
- acceptance criteria covered;
- commands executed;
- pass/fail results;
- known limitations;
- remaining risks.

## Global precedence

Follow, in order:

1. Repository-level `AGENTS.md`.
2. Active OpenSpec change and applicable specifications.
3. This role skill.
4. Existing project conventions.

A role skill MUST NOT override `AGENTS.md` or silently change OpenSpec requirements.
