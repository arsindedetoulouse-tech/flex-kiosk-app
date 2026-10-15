# Kiosk Policy Management

## Why

The custom DPC foundation can identify Device Owner provisioning, but the application cannot yet define, apply, or verify the Android policies required by a kiosk. This change introduces a focused policy layer without activating Lock Task Mode or changing the Home Screen.

## Goals

- Introduce a typed, versioned desired kiosk policy model.
- Apply supported policies through an Android framework adapter owned by the DPC.
- Configure the Lock Task package allowlist and persistent preferred HOME activity.
- Support explicit Lock Task UI feature configuration on API 28+ with guarded compatibility behavior.
- Report desired, applied, unsupported, and failed policy states separately.
- Keep policy application idempotent and testable.

## Proposed approach

This change follows the preceding kiosk changes and MUST reuse their public interfaces rather than duplicating device-policy, runtime, or remote-management logic.

- Keep Android framework operations behind testable adapters where practical.
- Preserve the existing Home Screen and project architecture.
- Use Kotlin, Coroutines/Flow, Koin, Ktor, `kotlinx.serialization`, and SQLite only where required by this change.
- Treat platform limitations and unsupported device behavior as explicit outcomes.
- Do not report an operation as successful until its success criteria have been verified.

## Scope

### Included

- Policy domain model and validation.
- DevicePolicyManager adapter and policy orchestration.
- Allowlist configuration, persistent HOME configuration, and selected user restrictions.
- API-level capability checks and policy application results.
- Unit tests using a fake policy gateway and device-level verification instructions.

### Excluded

- Calling Activity.startLockTask() or implementing runtime lifecycle recovery.
- Remote policy delivery or backend integration.
- SQLite persistence of command queues.
- Maintenance UI, arbitrary app installation, or unrestricted shell execution.

## Dependencies

- `add-custom-dpc` MUST be completed first.
- `add-kiosk-policy-management` MUST be completed before runtime, startup, and remote policy commands depend on policy orchestration.
- Changes MUST be applied in the sequence defined by the project roadmap.
- Backend protocol details MUST be agreed with the backend owner before production integration.

## Risks and mitigations

| Risk | Mitigation |
|---|---|
| Android behavior differs by API level or manufacturer | Use capability checks and physical-device acceptance tests |
| Partial operations are mistaken for success | Model per-operation outcomes and observed state |
| Process/network failures interrupt work | Persist required state and use bounded, idempotent retries where in scope |
| Feature scope expands into later changes | Keep excluded functionality out of this change and expose only required interfaces |

## Acceptance criteria

- The application builds with the existing project configuration.
- Automated tests cover success, failure, and boundary conditions for this change.
- Existing Home Screen behavior remains compatible.
- Unsupported capabilities and partial failures are observable.
- Documentation and OpenSpec validation are complete.
- Specific outcome: Introduce a typed, versioned desired kiosk policy model.
- Specific outcome: Apply supported policies through an Android framework adapter owned by the DPC.
- Specific outcome: Configure the Lock Task package allowlist and persistent preferred HOME activity.

## Rollout

1. Inspect existing project interfaces and completed OpenSpec changes.
2. Implement the smallest vertical slice in the task order.
3. Add tests alongside each behavior.
4. Verify on a provisioned test device where platform behavior is involved.
5. Review scope, update tasks, validate OpenSpec, and archive only after acceptance.
