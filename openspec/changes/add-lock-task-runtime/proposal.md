# Lock Task Runtime

## Why

Policy management can prepare a device for kiosk operation, but the application does not yet safely enter or observe Lock Task Mode. This change introduces a runtime coordinator that requests Lock Task Mode only when permitted and verifies the resulting system state.

## Goals

- Implement a lifecycle-aware Lock Task runtime coordinator.
- Distinguish fully locked mode from screen pinning and no-lock state.
- Verify observed state after requesting entry.
- Expose runtime state and failures to the application.
- Keep Activity lifecycle integration thin and testable.

## Proposed approach

This change follows the preceding kiosk changes and MUST reuse their public interfaces rather than duplicating device-policy, runtime, or remote-management logic.

- Keep Android framework operations behind testable adapters where practical.
- Preserve the existing Home Screen and project architecture.
- Use Kotlin, Coroutines/Flow, Koin, Ktor, `kotlinx.serialization`, and SQLite only where required by this change.
- Treat platform limitations and unsupported device behavior as explicit outcomes.
- Do not report an operation as successful until its success criteria have been verified.

## Scope

### Included

- Lock Task state model and gateway.
- Entry request when the approved kiosk Activity is resumed and the package is permitted.
- State verification, duplicate-entry prevention, and error reporting.
- Unit tests and physical-device verification.

### Excluded

- Changing device policies or allowlists.
- Selecting the persistent HOME activity.
- Remote commands, app relaunch strategy, or maintenance exit UX.
- Guaranteeing recovery from OS crashes or hardware-level interruption.

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
- Specific outcome: Implement a lifecycle-aware Lock Task runtime coordinator.
- Specific outcome: Distinguish fully locked mode from screen pinning and no-lock state.
- Specific outcome: Verify observed state after requesting entry.

## Rollout

1. Inspect existing project interfaces and completed OpenSpec changes.
2. Implement the smallest vertical slice in the task order.
3. Add tests alongside each behavior.
4. Verify on a provisioned test device where platform behavior is involved.
5. Review scope, update tasks, validate OpenSpec, and archive only after acceptance.
