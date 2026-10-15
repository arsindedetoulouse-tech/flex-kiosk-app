# Changelog

All notable changes to this project are documented in this file.

## [Unreleased]

### Refactoring: AGENTS.md compliance, Orbit MVI, string resources, Context removal

- Migrated `Details`, `Home` and `AppList` features to Orbit MVI (`OrbitContainerHost`, Orbit 12 `org.orbitmvi.orbit`), with `State` / `Action` / `SideEffect` contracts.
- All feature Routes now observe side effects via `collectSideEffect` instead of plumbing navigation callbacks through screens.
- Removed `Context` injection from presentation: details context usage wrapped in `AppDetailsProvider` (`AppDetailsProviderImpl` in `core:scanner`).
- Extracted all user-visible strings into per-feature resources (`values` / `values-ru` / `values-uz`), including the Settings screen; no hardcoded UI strings remain in feature source.
- Added feature API modules: `feature:details-api`, `feature:home-api`, `feature:app-list-api` with public contracts and destination/route definitions.
- Added central navigation module `app:navigation` (`AppDestinations` sealed class + `NavController` extension functions).
- Centralized Koin wiring in `AppScannerApplication`; moved `AppCatalogRepository` registration to the app layer (`appRepositoryModule`).
- Added JUnit 5 tests (`orbit-test`, mockk, Kover) for all migrated ViewModels and navigation routes; per-ViewModel line coverage >= 80%.