# Kiosk Persistence and Recovery

## ADDED Requirements

### Requirement: Durable configuration and command state

The implementation MUST provide the behavior described by this requirement, expose failures explicitly, and preserve the existing application behavior outside this change's scope.

#### Scenario: Configuration is saved

- GIVEN a configuration passes validation
- WHEN it is accepted for application
- THEN its version and required payload/metadata MUST be persisted durably
- AND the applied version MUST be updated only after required policy operations succeed.

#### Scenario: Command is received

- GIVEN a valid command with a unique ID is received
- WHEN it is accepted for processing
- THEN its ID, validated type, expiry, and processing state MUST be persisted before non-idempotent side effects.

#### Scenario: Command is delivered twice

- GIVEN a command ID already has a completed result
- WHEN the same command is received again
- THEN the command MUST NOT be executed again
- AND the prior result MUST be reused where appropriate.

#### Scenario: Process terminates mid-command

- GIVEN a command is in progress
- WHEN the process terminates
- THEN startup recovery MUST inspect its persisted state
- AND retry only if the operation's retry/idempotency policy permits it.

### Requirement: Result delivery and retries

The implementation MUST provide the behavior described by this requirement, expose failures explicitly, and preserve the existing application behavior outside this change's scope.

#### Scenario: Backend is unavailable

- GIVEN a command result is persisted
- WHEN the backend cannot accept the result
- THEN the result MUST remain queued for later delivery.

#### Scenario: Backend becomes available

- GIVEN pending results exist
- WHEN connectivity and work constraints allow synchronization
- THEN the client MUST retry delivery using bounded backoff
- AND mark a result delivered only after an acknowledged response.

#### Scenario: Retry budget is exhausted

- GIVEN repeated delivery attempts fail
- WHEN the retry policy reaches its configured limit
- THEN the item MUST remain diagnosable and follow a defined deferred/manual recovery policy rather than retrying in a tight loop.

### Requirement: Storage security and migrations

The implementation MUST provide the behavior described by this requirement, expose failures explicitly, and preserve the existing application behavior outside this change's scope.

#### Scenario: Database migration runs

- GIVEN an existing database schema version
- WHEN the application upgrades to a newer schema
- THEN migrations MUST preserve required command and configuration state or fail safely with diagnostics.

#### Scenario: Sensitive data is stored

- GIVEN the application persists management state
- WHEN database writes occur
- THEN authentication secrets and private keys MUST NOT be stored as plaintext in SQLite.

## Implementation constraints

- Minimum Android API level: 26.
- Use Kotlin and preserve the existing Clean Architecture/MVI/Compose conventions.
- Do not introduce third-party EMM as a runtime dependency.
- Never claim successful provisioning, policy application, command execution, or recovery without verifying the relevant outcome.
