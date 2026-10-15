# Android Kiosk OpenSpec Roadmap

## Change order

1. `add-custom-dpc` — prerequisite; authored separately.
2. `add-kiosk-policy-management`
3. `add-lock-task-runtime`
4. `add-kiosk-home-launch`
5. `add-kiosk-remote-management`
6. `add-kiosk-persistence-and-recovery`
7. `add-kiosk-maintenance-and-hardening`

## Notes

- Each change has its own `proposal.md`, `design.md`, `tasks.md`, and delta spec.
- The delta spec is placed under `specs/<change-name>/spec.md` in this bundle. Before using it, align the capability path with the project's existing OpenSpec conventions. If the capability should be `android-kiosk`, rename the nested directory consistently.
- Apply changes in order and update downstream proposals/specs if implementation reveals a changed interface.
- Do not enable non-idempotent remote commands in production until durable deduplication from `add-kiosk-persistence-and-recovery` is complete.
- Backend endpoint paths, authentication protocol, and provisioning deployment method require agreement with the project/backend owners.
- The custom DPC can share an APK with the UI initially, but administrative policy logic should remain isolated from Compose and business logic.
