# Kiosk Policy Management

## ADDED Requirements

### Requirement: Policy model and validation

The implementation MUST provide the behavior described by this requirement, expose failures explicitly, and preserve the existing application behavior outside this change's scope.

#### Scenario: A valid policy is applied

- GIVEN the application is Device Owner
- AND a valid policy passes validation
- WHEN the policy manager applies it
- THEN it MUST apply only the declared supported policy fields
- AND report each operation's outcome
- AND mark the policy version applied only when all required operations succeed.

#### Scenario: Invalid policy is rejected

- GIVEN a policy contains an invalid package name, unsupported option, or contradictory setting
- WHEN validation runs
- THEN the policy MUST be rejected before device mutations
- AND the previous successfully applied policy MUST remain identifiable.

### Requirement: Lock Task allowlist and HOME configuration

The implementation MUST provide the behavior described by this requirement, expose failures explicitly, and preserve the existing application behavior outside this change's scope.

#### Scenario: Application is allowlisted

- GIVEN the device is provisioned as Device Owner
- WHEN a valid policy is applied
- THEN the configured kiosk packages MUST be passed to DevicePolicyManager.setLockTaskPackages
- AND the result MUST be verified or reported as an operation result.

#### Scenario: Persistent HOME is configured

- GIVEN a valid HOME component belongs to an installed and approved package
- WHEN the policy is applied
- THEN the device-policy layer MUST configure the persistent preferred HOME activity using supported APIs
- AND report failure if the component cannot be configured.

#### Scenario: Package is not installed

- GIVEN a policy references a required package that is not installed
- WHEN validation or application detects the missing package
- THEN the policy MUST NOT be reported fully applied
- AND the missing package MUST be reported explicitly.

### Requirement: Policy reconciliation and compatibility

The implementation MUST provide the behavior described by this requirement, expose failures explicitly, and preserve the existing application behavior outside this change's scope.

#### Scenario: Policy is already applied

- GIVEN the effective device policy matches the desired policy
- WHEN reconciliation runs
- THEN it MUST avoid unnecessary mutations where the platform exposes sufficient state to compare.

#### Scenario: API capability is unavailable

- GIVEN a requested policy field is not supported on the current Android API or device
- WHEN the policy is validated
- THEN the implementation MUST follow the field's declared required/optional behavior
- AND MUST NOT silently claim the unsupported field was applied.

#### Scenario: Partial application fails

- GIVEN one required operation fails
- WHEN policy reconciliation finishes
- THEN the policy result MUST be partial or failed rather than successful
- AND failed operations MUST be available for diagnostics.

### Requirement: Testability

The implementation MUST provide the behavior described by this requirement, expose failures explicitly, and preserve the existing application behavior outside this change's scope.

#### Scenario: Policy orchestration is unit tested

- GIVEN a fake policy gateway
- WHEN valid, invalid, partially failing, and already-applied policies are reconciled
- THEN tests MUST verify the exact operations, ordering constraints, and reported outcomes.

## Implementation constraints

- Minimum Android API level: 26.
- Use Kotlin and preserve the existing Clean Architecture/MVI/Compose conventions.
- Do not introduce third-party EMM as a runtime dependency.
- Never claim successful provisioning, policy application, command execution, or recovery without verifying the relevant outcome.
