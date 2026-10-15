# Kiosk Remote Management

## ADDED Requirements

### Requirement: Authenticated transport

The implementation MUST provide the behavior described by this requirement, expose failures explicitly, and preserve the existing application behavior outside this change's scope.

#### Scenario: Authenticated request succeeds

- GIVEN device credentials are available and valid
- WHEN the client calls a management endpoint
- THEN it MUST use the agreed authenticated HTTPS protocol
- AND validate response status and schema before passing data to domain logic.

#### Scenario: Authentication fails

- GIVEN credentials are invalid or unavailable
- WHEN a management request is attempted
- THEN the request MUST fail explicitly
- AND no returned command may be executed.

#### Scenario: Server or network fails

- GIVEN the endpoint is unreachable or returns an error
- WHEN the client performs a request
- THEN it MUST return a typed failure
- AND MUST apply bounded timeouts without blocking the UI thread.

### Requirement: Configuration and status contracts

The implementation MUST provide the behavior described by this requirement, expose failures explicitly, and preserve the existing application behavior outside this change's scope.

#### Scenario: Valid configuration is received

- GIVEN an authenticated response contains a supported configuration schema
- WHEN deserialization and validation succeed
- THEN the typed configuration MUST be returned to the configuration use case.

#### Scenario: Malformed response is received

- GIVEN a response is malformed or uses an unsupported schema version
- WHEN parsing or validation occurs
- THEN the response MUST be rejected and reported as a protocol error.

#### Scenario: Status is reported

- GIVEN a device status snapshot is available
- WHEN the client submits it
- THEN the request MUST include the agreed device ID, app version, applied config version, observed kiosk state, and relevant health/error fields.

### Requirement: Command validation

The implementation MUST provide the behavior described by this requirement, expose failures explicitly, and preserve the existing application behavior outside this change's scope.

#### Scenario: Valid command is dispatched

- GIVEN a command response is authenticated and valid
- AND the command type is supported
- WHEN command validation completes
- THEN the command processor MUST dispatch only the corresponding typed operation.

#### Scenario: Expired or unsupported command

- GIVEN a command is expired, malformed, or unsupported
- WHEN validation runs
- THEN the command MUST be rejected before side effects.

#### Scenario: Dangerous command is requested

- GIVEN a payload requests arbitrary shell execution, arbitrary code, or an operation outside the allowlist
- WHEN it is validated
- THEN it MUST be rejected.

## Implementation constraints

- Minimum Android API level: 26.
- Use Kotlin and preserve the existing Clean Architecture/MVI/Compose conventions.
- Do not introduce third-party EMM as a runtime dependency.
- Never claim successful provisioning, policy application, command execution, or recovery without verifying the relevant outcome.
