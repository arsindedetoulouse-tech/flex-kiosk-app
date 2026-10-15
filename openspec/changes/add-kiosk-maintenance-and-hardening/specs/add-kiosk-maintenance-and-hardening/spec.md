# Kiosk Maintenance and Security Hardening

## ADDED Requirements

### Requirement: Authorized maintenance

The implementation MUST provide the behavior described by this requirement, expose failures explicitly, and preserve the existing application behavior outside this change's scope.

#### Scenario: Authorized administrator requests maintenance

- GIVEN the maintenance request is authenticated and authorized
- WHEN the maintenance workflow begins
- THEN the device MUST enter the explicitly defined maintenance state
- AND record the actor/device/action/result according to the audit policy.

#### Scenario: Unauthorized maintenance request

- GIVEN a request lacks required authorization
- WHEN it is evaluated
- THEN it MUST be rejected
- AND kiosk policies and credentials MUST remain unchanged.

#### Scenario: Maintenance operation fails

- GIVEN a maintenance operation begins
- WHEN a required step fails
- THEN the workflow MUST expose partial completion
- AND MUST NOT claim maintenance or cleanup completed successfully.

### Requirement: Decommissioning

The implementation MUST provide the behavior described by this requirement, expose failures explicitly, and preserve the existing application behavior outside this change's scope.

#### Scenario: Authorized decommissioning

- GIVEN a decommission operation is approved
- WHEN the procedure executes
- THEN it MUST revoke or invalidate backend credentials according to the protocol
- AND remove supported kiosk policies in a documented order
- AND report remaining device-owner or system restrictions that require a separate supported procedure.

#### Scenario: Decommissioning is interrupted

- GIVEN decommissioning has multiple steps
- WHEN the process terminates or a step fails
- THEN progress MUST be recoverable or diagnosable
- AND repeated execution MUST be safe where operations are idempotent.

### Requirement: Audit and security

The implementation MUST provide the behavior described by this requirement, expose failures explicitly, and preserve the existing application behavior outside this change's scope.

#### Scenario: Privileged action is executed

- GIVEN a privileged management action is attempted
- WHEN it succeeds or fails
- THEN a safe audit record MUST be produced with operation, timestamp, outcome, and correlation ID
- AND MUST NOT include credentials or sensitive payloads.

#### Scenario: Security boundary is tested

- GIVEN malformed, replayed, expired, unauthorized, and unsupported commands
- WHEN the end-to-end security tests run
- THEN no prohibited side effect MUST occur.

### Requirement: Production readiness

The implementation MUST provide the behavior described by this requirement, expose failures explicitly, and preserve the existing application behavior outside this change's scope.

#### Scenario: Supported device is evaluated

- GIVEN a device model and Android version are candidates for production
- WHEN the acceptance suite runs
- THEN provisioning, HOME launch, Lock Task, offline behavior, update, recovery, maintenance, and decommissioning MUST be checked or explicitly marked unsupported.

#### Scenario: Update is attempted

- GIVEN an approved update mechanism is configured
- WHEN an update is initiated
- THEN the application MUST verify the approved package/source and resulting version using the chosen platform-supported deployment mechanism
- AND report failure without claiming the update completed.

## Implementation constraints

- Minimum Android API level: 26.
- Use Kotlin and preserve the existing Clean Architecture/MVI/Compose conventions.
- Do not introduce third-party EMM as a runtime dependency.
- Never claim successful provisioning, policy application, command execution, or recovery without verifying the relevant outcome.
