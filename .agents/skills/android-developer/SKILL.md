# Android Developer Skill

## Purpose

Implement approved OpenSpec behavior within AppScanner's existing architecture.

## Preconditions

Before coding:

1. Read the active OpenSpec change.
2. Read relevant specifications/design.
3. Inspect the affected module and its dependencies.
4. Inspect nearby implementations.
5. Load `qa-engineer` rules for TDD.
6. Confirm the required acceptance criteria.

## Implementation rules

Follow `AGENTS.md` for:

- Kotlin/JVM 17;
- Android SDK 36 / min SDK 24;
- Compose;
- Orbit MVI;
- Koin;
- Ktor;
- Kotlin Serialization;
- Firebase;
- Gradle convention plugins;
- localization;
- security;
- module boundaries.

Do not introduce alternative frameworks without an explicit architectural decision.

## TDD

For each new behavior:

```text
Acceptance criterion
    ↓
Write focused test
    ↓
Run test → Red
    ↓
Minimum implementation
    ↓
Run test → Green
    ↓
Refactor
```

Do not implement the complete feature and add tests afterward.

## Compose

Prefer:

- stateless Screen composables;
- state hoisting;
- Route-level ViewModel/state collection;
- lifecycle-aware collection;
- existing `core:designsystem` components.

Do not put business rules, navigation or network operations into reusable UI components.

## Orbit MVI

Keep separate:

- persistent `State`;
- user/system `Action`;
- one-time `SideEffect`.

Navigation, snackbar/toast requests and external operations belong in side effects or appropriate effect APIs, not recomposition.

## Coroutines

Use structured concurrency.

Preserve cancellation.

Do not use `GlobalScope`, unmanaged long-lived scopes, or arbitrary delays.

## Networking and data

Keep Ktor and transport types in data/network layers.

Map:

```text
ResponseDto → DomainModel
Domain/Input → RequestDto
```

Do not expose DTOs, persistence entities or Ktor types to presentation/domain.

## DI

Follow `DEPENDENCY_INJECTION.md`.

Register feature dependencies in the existing Koin application setup.

Use parameters only for legitimate construction dependencies, not UI callbacks or `NavController`.

## Localization

User-visible strings belong in resources. Update Russian and Uzbek resources where applicable.

## Completion evidence

Before handoff:

- focused tests pass;
- affected module tests pass;
- debug build is run when relevant;
- lint is run when relevant;
- Koin wiring is verified;
- navigation is verified when changed;
- `git diff` contains no unrelated changes or secrets.

Report the commands run and their results.

## Global precedence

Follow, in order:

1. Repository-level `AGENTS.md`.
2. Active OpenSpec change and applicable specifications.
3. This role skill.
4. Existing project conventions.

A role skill MUST NOT override `AGENTS.md` or silently change OpenSpec requirements.
