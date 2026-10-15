# Kiosk Maintenance and Security Hardening

## Why

A kiosk needs a controlled way for authorized staff to diagnose, maintain, update, and decommission devices. Before production rollout, the application must also verify security assumptions and test end-to-end recovery across supported device configurations.

## Goals

- Define an authenticated maintenance workflow separate from ordinary kiosk navigation.
- Implement safe policy reconciliation and decommissioning procedures.
- Audit privileged operations and security-relevant failures.
- Verify app-update and recovery flows without claiming unsupported platform capabilities.
- Add an end-to-end acceptance checklist and device compatibility matrix.

## Proposed approach

This change follows the preceding kiosk changes and MUST reuse their public interfaces rather than duplicating device-policy, runtime, or remote-management logic.

- Keep Android framework operations behind testable adapters where practical.
- Preserve the existing Home Screen and project architecture.
- Use Kotlin, Coroutines/Flow, Koin, Ktor, `kotlinx.serialization`, and SQLite only where required by this change.
- Treat platform limitations and unsupported device behavior as explicit outcomes.
- Do not report an operation as successful until its success criteria have been verified.

## Scope

### Included

- Maintenance authorization boundary and state machine.
- Auditable maintenance, policy, command, and decommission events.
- Safe decommission orchestration for policies and backend credentials, within supported platform capabilities.
- Security review and end-to-end device acceptance tests.
- Documented supported Android versions/manufacturers and known limitations.

### Excluded

- Guaranteeing resistance to hardware-level compromise or every factory-reset path.
- Remote arbitrary shell execution or arbitrary code loading.
- An assumption that an ordinary app can reboot the whole device.
- Unreviewed automatic factory reset or destructive cleanup.

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
- Specific outcome: Define an authenticated maintenance workflow separate from ordinary kiosk navigation.
- Specific outcome: Implement safe policy reconciliation and decommissioning procedures.
- Specific outcome: Audit privileged operations and security-relevant failures.

## Rollout

1. Inspect existing project interfaces and completed OpenSpec changes.
2. Implement the smallest vertical slice in the task order.
3. Add tests alongside each behavior.
4. Verify on a provisioned test device where platform behavior is involved.
5. Review scope, update tasks, validate OpenSpec, and archive only after acceptance.
