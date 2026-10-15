# Design: Kiosk Home Launch and Recovery

## Context

Depends on DPC, policy management, and Lock Task runtime changes. The existing Home Screen remains the UI.

## Components

- Existing Home Activity, declared as `MAIN` + `HOME` + `DEFAULT` as appropriate.
- `KioskStartupCoordinator`, which initializes local state, checks provisioning, reconciles policy via existing interfaces, and starts runtime evaluation.
- Existing `KioskRuntimeCoordinator`.
- ViewModel exposes startup status without blocking UI rendering.

## Startup order

1. Render a safe initial Home UI.
2. Load locally available configuration/state.
3. Check Device Owner and policy readiness.
4. Reconcile through the policy layer if authorized.
5. Evaluate/enter Lock Task Mode through runtime.
6. Start or signal remote synchronization independently.

Do not rely exclusively on `BOOT_COMPLETED` or a long-running process. Persistent HOME resolution is the primary launch mechanism. Any optional receiver must comply with Android background-start restrictions and must not duplicate startup work.

## Recovery

All startup stages must be idempotent. Distinguish Home UI availability from kiosk security state. Network operations must not block startup.
