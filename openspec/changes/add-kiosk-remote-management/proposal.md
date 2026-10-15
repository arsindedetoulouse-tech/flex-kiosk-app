# Kiosk Remote Management

## Why

The managed device needs a secure channel to receive approved configuration and commands and report its state. This change adds the Android-side remote-management adapter while keeping backend implementation and policy execution behind explicit contracts.

## Goals

- Define typed API contracts for configuration, commands, status, and results.
- Implement a Ktor client with TLS, bounded timeouts, structured error handling, and authentication hooks.
- Validate command identity, expiry, type, schema, and preconditions before execution.
- Support status reporting and synchronization triggers.
- Keep command execution behind an allowlisted command processor.

## Proposed approach

This change follows the preceding kiosk changes and MUST reuse their public interfaces rather than duplicating device-policy, runtime, or remote-management logic.

- Keep Android framework operations behind testable adapters where practical.
- Preserve the existing Home Screen and project architecture.
- Use Kotlin, Coroutines/Flow, Koin, Ktor, `kotlinx.serialization`, and SQLite only where required by this change.
- Treat platform limitations and unsupported device behavior as explicit outcomes.
- Do not report an operation as successful until its success criteria have been verified.

## Scope

### Included

- RemoteManagementClient interface and Ktor implementation.
- kotlinx.serialization DTOs and protocol versioning.
- Authentication-provider interface, request/response validation, timeouts, and error mapping.
- Typed command dispatcher for operations whose local capabilities already exist.
- Contract tests using a fake HTTP engine.

### Excluded

- Implementing the backend or management console.
- Arbitrary shell commands or remote code execution.
- Installing arbitrary APKs or rebooting the whole device from an ordinary app.
- Durable command queue and full retry persistence, which belongs to the persistence/recovery change.

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
- Specific outcome: Define typed API contracts for configuration, commands, status, and results.
- Specific outcome: Implement a Ktor client with TLS, bounded timeouts, structured error handling, and authentication hooks.
- Specific outcome: Validate command identity, expiry, type, schema, and preconditions before execution.

## Rollout

1. Inspect existing project interfaces and completed OpenSpec changes.
2. Implement the smallest vertical slice in the task order.
3. Add tests alongside each behavior.
4. Verify on a provisioned test device where platform behavior is involved.
5. Review scope, update tasks, validate OpenSpec, and archive only after acceptance.
