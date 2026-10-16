# AGENTS.md

## Project overview

Flex Kiosk App is a multi-module Android application written in Kotlin. It targets managed Android
devices and, among its capabilities, includes a custom Device Policy Controller (DPC) used to
configure and run the device as a kiosk.

Main technologies:

- Kotlin/JVM 21
- Android SDK 37, minimum SDK 24
- Jetpack Compose
- Orbit MVI
- Koin
- Ktor
- Kotlin Serialization
- Navigation 3
- Gradle Kotlin DSL
- Gradle convention plugins in `build-logic`

The application supports Russian and Uzbek locales.

## Repository structure

- `app/` — application entry point and app-level DI wiring (`App.kt`, `MainActivity.kt`).
- `app/navigation/` — start route and top-level route declarations.
- `core/` — reusable technical modules:
  - `core:network`
  - `core:datastore`
  - `core:navigation`
  - `core:designsystem`
- `common/` — shared application layers:
  - `common:data`
  - `common:domain`
  - `common:presentation`
  - `common:testing`
- `feature/` — product features.
- `feature/*-api/` — public contracts and API models used to decouple features.
- `build-logic/` — Gradle convention plugins and shared Gradle configuration.
- `openspec/` — spec-driven change definitions: `proposal.md`, `design.md`, `tasks.md` and delta specs.
- `SECURITY.md` — security guidelines and policies.
- `AGENTS.md` — this contract.
- `CHANGELOG.md` — unreleased changes and recent implementation notes.

Note: `docs/` is ignored by `.gitignore` and holds local scratch material only. Do not rely on it
as a source of tracked project documentation.

## General rules

- Inspect the relevant module and its dependencies before changing code.
- Keep changes focused and avoid unrelated refactoring.
- Do not modify generated files, build outputs, IDE files or local configuration.
- Preserve existing package names and module boundaries.
- Prefer existing project abstractions and convention plugins over introducing new ones.
- Use the version catalog in `gradle/libs.versions.toml` for dependencies.
- Do not add dependencies directly to individual modules if an existing convention plugin already provides them.
- Keep public feature contracts in the appropriate `*-api` module.
- Do not introduce circular dependencies between modules.

## Architecture and code organization

Follow the existing architecture and code organization of the project.
Prefer consistency with the existing codebase over introducing new architectural abstractions.
Before adding a new pattern, base class, wrapper, use case, manager, provider or abstraction, check whether the project already has an established equivalent.
Do not perform unrelated architectural refactoring while implementing a focused task.
Prefer the minimum architectural change required to implement the requested behavior correctly.

### Layer dependency direction

The project follows the general dependency direction:

*presentation -> domain*
*data -> domain*

The following dependencies are not allowed:

*domain -> presentation*
*domain -> data*
*presentation -> data implementations*

`domain` must remain independent of Android UI, Compose, navigation, networking, persistence and concrete infrastructure implementations.

`presentation` must not directly depend on:

- network DTOs;
- database entities;
- Ktor-specific types;
- concrete repository implementations;
- data-source implementations.

`data` must not depend on `presentation`.

Cross-layer communication should use domain models and domain contracts.

### Layer responsibilities

Follow the existing layering:

```text
presentation/
domain/
data/
```

`presentation/` contains:

- Compose screens;
- Route composables;
- navigation;
- ViewModels;
- UI state;
- UI actions;
- UI side effects;
- presentation-specific models and formatting.

`domain/` contains:

- business models;
- repository contracts;
- reusable business rules;
- domain validation;
- domain services or interactors where justified by business complexity.

`data/` contains:

- repository implementations;
- remote and local data sources;
- network DTOs;
- persistence entities;
- mappers;
- infrastructure-specific implementations.

Network DTOs and persistence entities must not leak into `presentation` or `domain`.

Convert API and persistence models to domain models through explicit mapping.

Do not reuse domain models as UI state merely to avoid creating presentation models.

Presentation-specific concerns belong to presentation models, including:

- formatted values;
- selection state;
- expanded/collapsed state;
- UI-only flags;
- transient loading state;
- UI-specific labels or representations.

Persistence-specific state belongs to the data layer.

### Business logic

ViewModels may orchestrate application flows but should not contain reusable business rules.

Reusable business decisions, validation and invariants belong in the domain layer when they are sufficiently complex or reused by multiple callers.

Do not introduce a use case or interactor solely to wrap a single repository call if it adds no business logic or architectural value.

Prefer:

```text
ViewModel:
- accepts UI/system actions;
- coordinates repository/domain operations;
- updates presentation state;
- emits one-time side effects.

Domain:
- contains reusable business rules;
- performs business validation;
- defines business invariants;
- coordinates domain-level decisions when needed.
```

### Feature structure

Feature modules generally use:

- `Screen` for stateless or state-hoisted UI;
- `Route` for ViewModel collection, side-effect handling and navigation wiring;
- Orbit ViewModels for state, side effects and application-flow orchestration;
- separate navigation extension functions for destinations and navigation actions.

Keep feature implementation consistent with nearby features unless there is an explicit requirement to change the pattern.

### Route and Screen responsibilities

Route-level composables may:

- obtain the ViewModel;
- collect ViewModel state;
- collect Orbit side effects;
- convert side effects into navigation or UI operations;
- perform lifecycle-aware collection;
- pass immutable state and event callbacks to the Screen.

Route composables should contain minimal rendering logic.
Screen-level composables should remain as stateless and previewable as practical.
Prefer state hoisting.
Screen-level APIs should normally follow a structure similar to:

```kotlin
@Composable
fun FeatureScreen(
    state: FeatureState,
    onAction: (FeatureAction) -> Unit,
)
```
Do not pass ViewModels deeply into the composable tree.
Prefer:

```text
Route
  |
  +-- ViewModel
  |
  +-- Screen
        |
        +-- UI components
```
instead of resolving ViewModels or application dependencies from nested reusable UI components.

### Orbit state, actions and side effects

Orbit-based presentation code should normally separate:

- persistent UI state;
- actions/events accepted by the ViewModel;
- one-time side effects.

Use the existing project naming convention where one already exists.
Conceptually:

```text
FeatureState
FeatureAction
FeatureSideEffect
```
Persistent state belongs in `State`.
User interactions and system events belong in `Action`.
Navigation, snackbar/toast requests, external intents and similar one-time operations belong in `SideEffect`.
Do not represent one-time events as persistent state unless the UI genuinely needs the value to remain part of the rendered state.
Do not perform navigation or other external side effects directly as a consequence of normal Compose recomposition.
For example, avoid patterns such as:

```kotlin
if (state.isCompleted) {
    navController.navigate(...)
}
```
Use Orbit side effects or an appropriate Compose effect API instead.

### Navigation

Keep navigation callbacks and UI runtime parameters at the UI/navigation boundary.
Navigation code must not leak into domain or data layers.
Do not inject `NavController`, navigation lambdas or Compose-specific navigation objects into repositories, data sources or domain services.
Navigation arguments should contain stable identifiers or small serializable values.
Prefer passing identifiers and loading required data at the destination.
Avoid passing through navigation:

- ViewModels;
- repositories;
- callbacks;
- mutable state objects;
- large domain objects;
- infrastructure implementations.

Keep destination declarations and navigation actions in separate navigation extension functions where that pattern is already used.

## Dependency injection

Koin is used for:

- application services;
- repositories;
- data sources;
- ViewModels;
- infrastructure dependencies.

Keep module definitions close to the module they configure.
Register new Koin modules in the application Koin setup in:

```text
app/src/main/kotlin/com/arsinde/flexkioskapp/App.kt
```
Do not create global DI definitions for objects whose lifetime belongs to a Compose screen, navigation destination or another short-lived UI scope unless the existing project architecture explicitly requires it.

### Runtime UI dependencies

Objects depending on UI callbacks or navigation lambdas must normally be created at the UI/navigation boundary.
Avoid Koin definitions with runtime function parameters.
Do not use Koin to inject:

- navigation callbacks;
- `NavController`;
- Composable lambdas;
- Compose state;
- Activity-scoped UI objects into long-lived services.

Prefer explicit constructor creation at the Route/navigation boundary for these objects.

### Koin parameters

If runtime parameters are genuinely required, resolve them using explicit `parametersOf(...)`.
Parameterized Koin definitions should be reserved for values that are legitimate construction dependencies, such as:

- stable entity IDs;
- session IDs;
- feature configuration;
- other non-UI runtime values required by the ViewModel or service.

Do not use `parametersOf(...)` merely to move UI callbacks or navigation lambdas through the dependency graph.
For example, this is acceptable when consistent with the project:

```kotlin
viewModel { params ->
    DetailsViewModel(
        itemId = params.get(),
        repository = get(),
    )
}
```

Avoid definitions conceptually similar to:

```kotlin
factory { params ->
    SomeHandler(
        onBack = params.get(),
    )
}
```

Add a test covering every parameterized Koin resolution according to the existing project testing conventions.

### Android Context

Do not inject `Activity`, `Fragment`, `NavController` or other UI-scoped Android objects into repositories, domain services or long-lived dependencies.
Use application `Context` only when an Android platform API genuinely requires it.
Prefer dedicated abstractions around Android platform functionality instead of propagating `Context` across layers.
For example, prefer a dedicated abstraction such as:

```kotlin
interface VibrationController {
    fun vibrate()
}
```
over passing `Context` into unrelated ViewModels or repositories.

## Compose and UI

Reuse components and design tokens from `core:designsystem`.

Follow the existing:

- theme;
- typography;
- spacing;
- shape;
- color;
- component conventions.

Do not duplicate an existing design-system component locally unless there is a concrete feature-specific reason.
Keep composables previewable and as stateless as practical.
Keep state collection and navigation wiring in Route-level composables.
Prefer immutable UI state.
Avoid exposing mutable implementation details such as:

- `MutableStateFlow`;
- mutable collections;
- mutable repository state;
- writable state owned by another layer.

Prefer `val` properties in state models.

### State collection

Collect ViewModel `Flow` / `StateFlow` using lifecycle-aware Compose APIs such as:

```kotlin
collectAsStateWithLifecycle()
```
unless the project already provides another lifecycle-aware abstraction.
Do not create unnecessary independent collection scopes inside reusable composables.

### Compose side effects

Do not perform the following directly during normal composition:

- navigation;
- analytics reporting;
- network requests;
- file I/O;
- persistence operations;
- external intents;
- mutation of application-level state.

Use ViewModel actions, Orbit side effects or the appropriate Compose effect API depending on ownership of the behavior.

### Resources and localization

Put user-visible text in Android resources.
Avoid hardcoded user-facing strings in Kotlin code.
Add translations to all supported locales where applicable:

```text
values/
values-ru/
values-uz/
```

Do not move internal debug-only values, API constants or machine-readable identifiers into string resources unless required by the Android platform or existing project convention.

## Coroutines and concurrency

Use structured concurrency.
ViewModels should run UI-bound asynchronous operations through Orbit intents and/or the appropriate ViewModel-owned coroutine scope according to the existing feature pattern.
Repositories and data sources should expose `suspend` APIs or flows rather than creating unmanaged coroutine scopes.
Do not use `GlobalScope`.
Do not create long-lived custom `CoroutineScope` instances unless ownership, lifetime and cancellation behavior are explicit.
Do not perform blocking I/O or CPU-heavy work on the main thread.
Data sources are responsible for moving blocking APIs to an appropriate dispatcher when the underlying API is not already asynchronous.
Inject dispatchers when dispatcher substitution is required for testability or when that pattern already exists in the project.
Do not unnecessarily wrap already-suspending asynchronous APIs in additional dispatcher switches.

### Cancellation

Preserve coroutine cancellation.
Do not accidentally convert `CancellationException` into a generic application failure.
When catching broad exceptions around suspend code, rethrow cancellation where necessary:

```kotlin
catch (e: CancellationException) {
    throw e
}
```
Be especially careful with generic `catch`, `runCatching` and custom `Result` wrappers around suspend operations.

## Networking and API changes

Network configuration is centralized in `core:network`.
Use the existing Ktor configuration, authentication, logging and error-handling mechanisms.
Do not create feature-local `HttpClient` instances unless there is an explicit architectural requirement.
Keep:

- endpoint paths;
- API constants;
- request/response DTOs;
- Ktor-specific types;
- serialization-specific infrastructure

inside the network/data layer.
Do not expose Ktor-specific types outside the data/network layer, including:

- `HttpResponse`;
- `HttpStatusCode`;
- `ResponseException`;
- transport-specific exceptions;
- serialization implementation details.

### DTO and domain mapping

Network DTOs must not leak into domain or presentation code.

Map inbound data as:

```text
ResponseDto -> DomainModel
```
Map outbound data as appropriate:

```text
Domain/Input model -> RequestDto
```
Use explicit mappers where the project already uses mapper abstractions.
Do not rely on presentation code to interpret raw API DTOs.

### API changes

When changing a request or response:

1. Update the DTO.
2. Update serialization annotations if needed.
3. Update the mapper.
4. Update the repository and/or data source.
5. Update affected domain code.
6. Update affected presentation code.
7. Update serialization and mapper tests according to the existing testing section.

Do not silently ignore renamed, removed or semantically changed API fields.
When nullable or optional API fields change, verify the effect through all mappings up to the presentation state.

### Error handling

Preserve API error handling through the existing `ApiErrorResponse` and presentation error model.
Transport and infrastructure errors should be translated at the data boundary according to the project's existing error architecture.
Presentation code should not branch directly on:

- Ktor exceptions;
- raw HTTP status codes;
- serialization exceptions;
- raw network response objects;

unless the project explicitly establishes that behavior for the affected feature.
Prefer:

```text
Network/transport error
        ↓
data-layer error mapping
        ↓
domain/project error representation
        ↓
presentation error model
```
Do not introduce a parallel error wrapper if an existing project error abstraction already covers the use case.

## Security policy

`SECURITY.md` is the authoritative project security policy.

The agent MUST comply with `SECURITY.md` for all security-sensitive operations.

Security-sensitive changes MUST load and follow `.agents/skills/security-reviewer/SKILL.md`.

OpenSpec requirements, role skills, implementation convenience, or debugging needs MUST NOT weaken or override security requirements.

If a requested change conflicts with `SECURITY.md`, stop and report the conflict rather than silently weakening the security control.

## Testing policy

Unit tests are located in module-local `src/test`.

Use JUnit 5 where the module is configured with `useJUnitPlatform()`. Reuse test utilities and helpers from `common:testing` whenever applicable.

New features and non-trivial behavior changes MUST be developed using TDD:

```text
Requirement → Test → Red → Minimal implementation → Green → Refactor
```

Detailed test strategy and TDD procedures are defined in `.agents/skills/qa-engineer/SKILL.md`.

## Gradle conventions

Use the project convention plugins from build-logic.
Do not duplicate common Android, Compose, Koin or Orbit configuration in module build files.
Keep dependency declarations alphabetized consistently with the existing version catalog.
Use type-safe project accessors where already established.
Keep compiler target and Android SDK values aligned with the convention configuration.
Do not enable warnings-as-errors locally in committed project configuration unless explicitly requested.

## Change checklist

Before completing a change:

Check the affected module boundaries and public APIs.
Update tests for changed behavior.
Update localized resources when UI text changes.
Check Koin registration and application module wiring.
Run the narrowest relevant Gradle tests.
Run a debug build for changes affecting Android configuration or resources.
Review git diff and ensure no credentials or generated files are included.
Do not alter unrelated existing changes.

## Working tree safety

The working tree may contain user-owned untracked files, including CHANGELOG.md and files under tools/.
Preserve existing modifications and untracked files unless the task explicitly asks to change them.

## Agent roles

The project uses specialized roles. `AGENTS.md` defines the project-wide contract; role-specific procedures live in `.agents/skills/<role>/SKILL.md`.

### Available roles

| Role | Responsibility | Skill |
|---|---|---|
| `openspec-manager` | OpenSpec lifecycle, change scope, requirements/design synchronization and verification | `.agents/skills/openspec-manager/SKILL.md` |
| `product-analyst` | Requirements, use cases, business rules and acceptance criteria | `.agents/skills/product-analyst/SKILL.md` |
| `android-architect` | Architecture, module boundaries, technical design and ADRs | `.agents/skills/android-architect/SKILL.md` |
| `android-developer` | Kotlin/Android/Compose implementation and feature integration | `.agents/skills/android-developer/SKILL.md` |
| `qa-engineer` | TDD, test strategy, deterministic testing and verification | `.agents/skills/qa-engineer/SKILL.md` |
| `code-reviewer` | Independent review of requirements, architecture, implementation and tests | `.agents/skills/code-reviewer/SKILL.md` |
| `security-reviewer` | Security analysis and security-sensitive implementation review | `.agents/skills/security-reviewer/SKILL.md` |

### Role selection

Use the smallest set of roles required by the task.

Typical workflows:

```text
New feature:
openspec-manager
    → product-analyst
    → android-architect
    → android-developer
    → qa-engineer
    → code-reviewer

Bug fix:
openspec-manager
    → android-developer
    → qa-engineer
    → code-reviewer

Architecture change:
openspec-manager
    → android-architect
    → android-developer
    → qa-engineer
    → code-reviewer

Security-sensitive change:
openspec-manager
    → security-reviewer
    → android-developer
    → qa-engineer
    → code-reviewer

Requirements-only task:
openspec-manager
    → product-analyst

Code review:
code-reviewer
```

A role may be skipped only when its responsibility is genuinely not applicable.

### Role boundaries

- `openspec-manager` controls change/spec lifecycle; it does not silently redefine product behavior.
- `product-analyst` defines behavior and acceptance criteria; it does not implement production code.
- `android-architect` defines technical structure and constraints; it does not silently change requirements.
- `android-developer` implements approved behavior and follows TDD.
- `qa-engineer` verifies behavior and test quality; it must not weaken tests to make implementation pass.
- `code-reviewer` reports findings independently; it must not silently rewrite the implementation.
- `security-reviewer` identifies security risks and required mitigations; known unresolved security findings block approval where applicable.

### Role handoff

Each role produces explicit artifacts/findings for the next role.

```text
OpenSpec scope
    ↓
Requirements + acceptance criteria
    ↓
Architecture/design
    ↓
TDD implementation
    ↓
Verification evidence
    ↓
Independent review
```

Do not claim a handoff is complete without the corresponding evidence in the repository, OpenSpec change, test output, or review findings.

## OpenSpec → roles → TDD → review contract

For a non-trivial change, the Agent CLI SHOULD orchestrate the following lifecycle:

```text
1. OpenSpec change identified
2. Requirements understood
3. Architecture assessed
4. TDD implementation performed
5. Automated verification completed
6. Independent code review performed
7. Security review performed when applicable
8. OpenSpec verification completed
9. Change marked complete / archived
```

### Mandatory policy loading

Before modifying code, the agent MUST apply:

1. `AGENTS.md` — global project rules.
2. `SECURITY.md` — mandatory security policy; always applicable, with deeper review when security-sensitive.
3. The active OpenSpec change — scope and requirements.
4. The applicable role `SKILL.md` — role-specific procedure.

The agent MUST load the relevant skill before performing role-specific work.

### Policy precedence

Use the following precedence:

```text
AGENTS.md
    ↓
SECURITY.md
    ↓
OpenSpec change
    ↓
Role SKILL.md
    ↓
Local implementation conventions
```

OpenSpec defines WHAT must be built, but it MUST NOT authorize violating `AGENTS.md` or `SECURITY.md`.

A role skill MUST NOT override `AGENTS.md` or `SECURITY.md`.

### Evidence requirements

The agent MUST be able to state:

- which OpenSpec change it implemented;
- which acceptance criteria it addressed;
- which roles were used;
- which tests were written before implementation;
- which focused and broader verification commands were run;
- whether security review was required and what it found;
- whether code review found blockers or major issues.

Do not report a phase as complete based only on intention. Completion requires repository or command evidence where applicable.

### Security-sensitive changes

Treat changes involving any of the following as security-sensitive:

- authentication or authorization;
- credentials, tokens or secrets;
- sensitive data;
- storage of protected data;
- network security;
- TLS or certificate validation;
- Android permissions;
- exported components;
- intents or deep links;
- WebView;
- Firebase security configuration;
- security-sensitive logging;
- R8/ProGuard security configuration.

Security-sensitive changes MUST involve `security-reviewer` before final approval.

### No silent role switching

When the task crosses role boundaries, explicitly switch the working role and load its skill.

Do not use one role's authority to make decisions owned by another role.

### Review is independent

The implementation agent MUST NOT self-declare a change fully reviewed.

`code-reviewer` is an independent verification role and should inspect the resulting diff and test evidence.

For security-sensitive changes, `security-reviewer` independently evaluates security impact.

### Change scope

OpenSpec defines what is being changed.

`AGENTS.md` defines project-wide engineering rules.

`SECURITY.md` defines mandatory security constraints.

Role skills define how the work is performed.

The implementation MUST satisfy all applicable policies.
