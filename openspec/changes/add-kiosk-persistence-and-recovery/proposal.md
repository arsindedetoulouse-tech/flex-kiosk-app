# Kiosk Persistence and Recovery

## Why

Remote synchronization and command execution must survive process termination, device restarts, and temporary network outages. This change adds durable state and retry semantics so commands are not repeated merely because results could not be uploaded.

## Goals

- Persist desired/applied configuration metadata and command processing state in SQLite.
- Implement durable pending-command and result queues.
- Prevent duplicate execution of completed commands.
- Support bounded retries and eventual result delivery.
- Keep secrets out of SQLite and make database migrations testable.

## Proposed approach

This change follows the preceding kiosk changes and MUST reuse their public interfaces rather than duplicating device-policy, runtime, or remote-management logic.

- Keep Android framework operations behind testable adapters where practical.
- Preserve the existing Home Screen and project architecture.
- Use Kotlin, Coroutines/Flow, Koin, Ktor, `kotlinx.serialization`, and SQLite only where required by this change.
- Treat platform limitations and unsupported device behavior as explicit outcomes.
- Do not report an operation as successful until its success criteria have been verified.

## Scope

### Included

- SQLite schema/DAO/repository for configuration metadata, commands, results, and sync state.
- Transactional command state transitions and unique command IDs.
- Retry policy and worker integration using the project's chosen background-work mechanism.
- Database migrations and tests for process interruption and duplicate delivery.

### Excluded

- Changing backend protocol semantics without agreement.
- Storing plaintext credentials or private keys.
- Arbitrary command replay or infinite retries.
- Device policy design unrelated to persistence.

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
- Specific outcome: Persist desired/applied configuration metadata and command processing state in SQLite.
- Specific outcome: Implement durable pending-command and result queues.
- Specific outcome: Prevent duplicate execution of completed commands.

## Rollout

1. Inspect existing project interfaces and completed OpenSpec changes.
2. Implement the smallest vertical slice in the task order.
3. Add tests alongside each behavior.
4. Verify on a provisioned test device where platform behavior is involved.
5. Review scope, update tasks, validate OpenSpec, and archive only after acceptance.
