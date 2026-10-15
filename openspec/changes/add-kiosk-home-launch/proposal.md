# Kiosk Home Launch and Recovery

## Why

The application needs to launch into the existing Home Screen after device startup and recover its kiosk runtime after process recreation. This change integrates the current Home activity with the policy and Lock Task runtime layers without duplicating Home Screen business logic.

## Goals

- Use the configured HOME activity as the kiosk entry point.
- Restore kiosk runtime after startup and Activity/process recreation.
- Keep Home Screen usable when the network is unavailable.
- Expose startup and recovery state for diagnostics.
- Test boot-to-home and lifecycle recovery on a provisioned device.

## Proposed approach

This change follows the preceding kiosk changes and MUST reuse their public interfaces rather than duplicating device-policy, runtime, or remote-management logic.

- Keep Android framework operations behind testable adapters where practical.
- Preserve the existing Home Screen and project architecture.
- Use Kotlin, Coroutines/Flow, Koin, Ktor, `kotlinx.serialization`, and SQLite only where required by this change.
- Treat platform limitations and unsupported device behavior as explicit outcomes.
- Do not report an operation as successful until its success criteria have been verified.

## Scope

### Included

- HOME/DEFAULT manifest intent filters and integration with persistent HOME policy.
- Thin Activity lifecycle integration with KioskRuntimeCoordinator.
- Reloading locally available configuration and re-evaluating provisioning/policy/runtime state.
- Startup and process-recreation tests.

### Excluded

- Applying policies beyond invoking the existing policy manager.
- Remote command execution or backend implementation.
- Using an unrestricted background service to keep the process alive.
- Changing the Home Screen's business features.

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
- Specific outcome: Use the configured HOME activity as the kiosk entry point.
- Specific outcome: Restore kiosk runtime after startup and Activity/process recreation.
- Specific outcome: Keep Home Screen usable when the network is unavailable.

## Rollout

1. Inspect existing project interfaces and completed OpenSpec changes.
2. Implement the smallest vertical slice in the task order.
3. Add tests alongside each behavior.
4. Verify on a provisioned test device where platform behavior is involved.
5. Review scope, update tasks, validate OpenSpec, and archive only after acceptance.
