# Lock Task Runtime

## ADDED Requirements

### Requirement: Runtime entry and state

The implementation MUST provide the behavior described by this requirement, expose failures explicitly, and preserve the existing application behavior outside this change's scope.

#### Scenario: Enter when permitted

- GIVEN the device is Device Owner and the package is allowlisted
- AND the kiosk Activity is resumed
- WHEN the runtime observes that full Lock Task Mode is not active
- THEN it MUST request entry through the Activity gateway
- AND verify the resulting state rather than treating the call itself as success.

#### Scenario: Do not enter when not permitted

- GIVEN the package is not allowlisted or provisioning is absent
- WHEN the runtime initializes
- THEN it MUST NOT report a fully locked kiosk
- AND MUST expose the missing precondition.

#### Scenario: Already locked

- GIVEN full Lock Task Mode is already active
- WHEN the Activity resumes again
- THEN the runtime MUST avoid redundant entry requests.

#### Scenario: Screen pinning is detected

- GIVEN Android reports a pinned state rather than fully locked Lock Task Mode
- WHEN runtime state is mapped
- THEN the state MUST NOT be represented as fully managed kiosk mode.

### Requirement: Failures and observability

The implementation MUST provide the behavior described by this requirement, expose failures explicitly, and preserve the existing application behavior outside this change's scope.

#### Scenario: Entry fails

- GIVEN the runtime requests Lock Task Mode
- WHEN the request throws or the observed state remains unlocked
- THEN the runtime MUST report failure and MUST NOT claim kiosk security is active.

#### Scenario: State is lost

- GIVEN the runtime previously observed full Lock Task Mode
- WHEN a later observation shows it is no longer active
- THEN the runtime MUST emit a state transition and evaluate whether a permitted re-entry attempt is appropriate.

### Requirement: Lifecycle and tests

The implementation MUST provide the behavior described by this requirement, expose failures explicitly, and preserve the existing application behavior outside this change's scope.

#### Scenario: Activity is recreated

- GIVEN the Activity is recreated
- WHEN the runtime reconnects to the lifecycle
- THEN it MUST query actual system state and avoid relying solely on in-memory state.

#### Scenario: Runtime is unit tested

- GIVEN fake Activity and state gateways
- WHEN preconditions, duplicate resume, entry failure, and state loss are simulated
- THEN tests MUST verify state transitions and entry requests.

## Implementation constraints

- Minimum Android API level: 26.
- Use Kotlin and preserve the existing Clean Architecture/MVI/Compose conventions.
- Do not introduce third-party EMM as a runtime dependency.
- Never claim successful provisioning, policy application, command execution, or recovery without verifying the relevant outcome.
