# Flex Kiosk App

Android kiosk application for managed Android devices. It ships its own user-facing features and, in
addition, uses a custom Device Policy Controller (DPC): once provisioned as Device Owner on a fully
managed device, it owns Lock Task Mode, the persistent HOME activity, kiosk policies and remote
management.

The project is currently at its foundation stage: a working navigation shell, one Home feature, and
a Device Policy Controller foundation. Kiosk functionality is delivered through OpenSpec changes,
applied in order; `add-custom-dpc` is done and archived.

## Technology

| Concern | Choice |
|---|---|
| Language | Kotlin, JVM target 21 |
| Android | compileSdk/targetSdk per AGP, `minSdk = 24` |
| UI | Jetpack Compose + Material 3 |
| State | Orbit MVI 12 (`State` / `SideEffect`, intents) |
| Navigation | Navigation 3 (`androidx.navigation3`) |
| DI | Koin 4 |
| Network | Ktor 3 (OkHttp) + Kotlin Serialization |
| Storage | DataStore Preferences |
| Build | Gradle Kotlin DSL + convention plugins in `build-logic` |
| Tests | JUnit 5, MockK, Orbit test, Kover |

Supported locales: English (default), Russian (`ru-RU`), Uzbek (`uz-Latn-UZ`).

## Getting started

Requirements: JDK 21, Android SDK with the platform matching `compileSdk` (see
`build-logic/src/main/kotlin/android.application.gradle.kts`). Point the build at your SDK with a
`local.properties` file containing `sdk.dir=/path/to/Android/sdk`.

```bash
./gradlew :app:assembleDebug          # build the debug APK
./gradlew test                        # run all unit tests
./gradlew lint                        # run Android lint
./gradlew :app:installDebug           # install on a connected device
```

Add a new feature module `feature/<name>` plus `feature/<name>-api` using the existing
`android.library`, `android.library.compose`, `android.orbit` and `android.koin` convention
plugins, then register it in `settings.gradle.kts` and add its `*Section` to `MainActivity`.

## Module layout

```text
app/            application entry point, DI wiring, navigation entry provider
app/navigation/ start route and top-level route declarations
core/
  designsystem/ theme, typography, spacing, shape, colors
  navigation/   AppRoute model, Navigator, NavigationState, LocalNavigator
  datastore/    DataStore setup
  network/      shared Ktor HttpClient configuration
  devicepolicy/ Device Policy Controller foundation (planned)
common/
  domain/       business models and repository contracts
  data/         repository implementations and data sources
  presentation/ shared presentation base types
  testing/      shared test utilities
feature/
  <name>/       feature implementation
  <name>-api/   public contracts, route definition, API models
openspec/       spec-driven change definitions (see below)
build-logic/    Gradle convention plugins
```

Layers are strictly separated: `presentation -> domain` and `data -> domain`. `domain` never
depends on Compose, navigation, Ktor or persistence. Network DTOs and persistence entities are
mapped to domain models at the data boundary.

Architecture rules, DI rules and the development workflow are specified in `AGENTS.md`. Security
requirements are specified in `SECURITY.md` and take precedence over everything else.

## Device Owner provisioning

Provisioning is provided by the archived `add-custom-dpc` change. Development provisioning is
performed over ADB on a resettable test device; the full procedure, its prerequisites and its
limitations are in `openspec/changes/archive/2026-10-16-add-custom-dpc/provisioning.md`.

Two properties of a provisioned device are worth knowing before working on it: the package becomes
*protected*, so it can be neither force-stopped nor uninstalled from the shell, and the state can
only be released by a factory reset. Use `./gradlew :app:installDebug` rather than Android Studio's
run action on a provisioned device.

## Roadmap

Work is planned as OpenSpec changes and applied in this order:

| # | Change | Outcome |
|---|---|---|
| 1 | `add-custom-dpc` — **done** | Device admin receiver, Device Owner detection, ADB provisioning, status card |
| 2 | `add-kiosk-policy-management` | Policy model, allowlist, persistent HOME, config versioning |
| 3 | `add-lock-task-runtime` | Lock Task Mode entry, state tracking, recovery, error handling |
| 4 | `add-kiosk-home-launch` | Home Screen as kiosk UI, boot start, process recovery |
| 5 | `add-kiosk-remote-management` | Device registration, auth, config fetch, typed commands, reports |
| 6 | `add-kiosk-persistence-and-recovery` | Config storage, command queue, idempotency, sync recovery, history |
| 7 | `add-kiosk-maintenance-and-hardening` | Maintenance mode, decommissioning, policy updates, audit, security tests |

Each change directory holds `proposal.md`, `design.md`, `tasks.md` and a delta spec under `specs/`.
Archiving a change moves it to `openspec/changes/archive/` and promotes its delta spec to
`openspec/specs/`, which is where the lasting requirements live.

Do not enable non-idempotent remote commands in production until the durable deduplication from
change 6 exists. Backend endpoint paths, the authentication protocol and the production provisioning
method require agreement with the backend owners.

Known debt and open decisions are recorded in `TECHNICAL_DEBT.md`.