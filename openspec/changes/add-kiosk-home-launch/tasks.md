# Tasks: Kiosk Home Launch and Recovery

- [ ] Inspect current Home Activity and preserve existing navigation/UI behavior.
- [ ] Ensure the intended HOME activity and intent filters are documented and correct.
- [ ] Implement KioskStartupCoordinator using existing DPC, policy, and runtime interfaces.
- [ ] Make initialization idempotent across repeated lifecycle callbacks.
- [ ] Ensure first-frame/Home interaction does not wait for network access.
- [ ] Expose startup, policy, and runtime state to the existing ViewModel/UI as appropriate.
- [ ] Add tests for unprovisioned startup, repeated resume, local/offline startup, and initialization failures.
- [ ] Verify boot-to-home and process recreation on a provisioned physical device.
- [ ] Run build, tests, OpenSpec validation, and review.
