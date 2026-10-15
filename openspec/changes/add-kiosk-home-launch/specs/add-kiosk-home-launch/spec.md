# Kiosk Home Launch and Recovery

## ADDED Requirements

### Requirement: HOME launch

The implementation MUST provide the behavior described by this requirement, expose failures explicitly, and preserve the existing application behavior outside this change's scope.

#### Scenario: HOME activity is selected

- GIVEN the DPC has configured the existing Home activity as persistent preferred HOME
- WHEN Android resolves the HOME intent after startup
- THEN the existing Home activity MUST be eligible to launch as the device home screen.

#### Scenario: Home starts without provisioning

- GIVEN the app is not provisioned as Device Owner
- WHEN the Home activity launches
- THEN existing Home behavior MUST remain available
- AND the app MUST NOT claim kiosk restrictions are active.

### Requirement: Startup and recovery

The implementation MUST provide the behavior described by this requirement, expose failures explicitly, and preserve the existing application behavior outside this change's scope.

#### Scenario: Device restarts

- GIVEN the device is provisioned and HOME policy is effective
- WHEN Android completes boot and resolves HOME
- THEN the kiosk activity MUST initialize its runtime
- AND re-evaluate provisioning, policy, and Lock Task state.

#### Scenario: Process is recreated

- GIVEN local kiosk configuration is available
- WHEN Android recreates the application process
- THEN the runtime MUST rebuild state from durable/configured sources and observed Android state
- AND MUST NOT depend only on previous in-memory state.

#### Scenario: Network unavailable at startup

- GIVEN the backend cannot be reached
- WHEN the Home activity launches
- THEN the local Home Screen MUST remain usable
- AND remote synchronization MUST NOT block the first frame or local interaction.

### Requirement: Lifecycle integration

The implementation MUST provide the behavior described by this requirement, expose failures explicitly, and preserve the existing application behavior outside this change's scope.

#### Scenario: Activity resumes repeatedly

- GIVEN the Home activity receives repeated lifecycle callbacks
- WHEN runtime initialization runs
- THEN it MUST be idempotent and MUST NOT create duplicate synchronization loops or redundant Lock Task requests.

#### Scenario: Startup failure

- GIVEN policy or runtime initialization fails
- WHEN the Home activity is displayed
- THEN the error MUST be surfaced to diagnostics
- AND the UI MUST not falsely report a secure kiosk state.

## Implementation constraints

- Minimum Android API level: 26.
- Use Kotlin and preserve the existing Clean Architecture/MVI/Compose conventions.
- Do not introduce third-party EMM as a runtime dependency.
- Never claim successful provisioning, policy application, command execution, or recovery without verifying the relevant outcome.
