# Tasks: Lock Task Runtime

- [ ] Define LockTaskState and runtime state transitions.
- [ ] Create a testable LockTaskGateway interface.
- [ ] Implement Android state observation using ActivityManager.
- [ ] Implement Activity-bound entry request using startLockTask().
- [ ] Add precondition checks for Device Owner and package allowlist.
- [ ] Integrate coordinator with Activity lifecycle without putting logic in Compose.
- [ ] Add tests for locked, pinned, unavailable, duplicate entry, and failure paths.
- [ ] Verify behavior on a provisioned physical device across supported Android versions.
- [ ] Document device-specific behavior and troubleshooting.
- [ ] Run build, tests, OpenSpec validation, and review.
