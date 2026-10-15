# Tasks: Kiosk Persistence and Recovery

- [ ] Define SQLite schema, DAOs, repositories, and migration strategy.
- [ ] Persist accepted configuration and distinguish desired from successfully applied version.
- [ ] Implement command/result state machine with unique command IDs and transactional updates.
- [ ] Persist result delivery state separately from command execution state.
- [ ] Implement bounded retry/backoff and eligible background synchronization.
- [ ] Implement recovery rules for commands left in progress after process death.
- [ ] Add tests for duplicate commands, process interruption, result upload failure, retries, and migrations.
- [ ] Verify no credentials or private keys are stored as plaintext.
- [ ] Document retention, retry limits, and recovery behavior.
- [ ] Run build, tests, OpenSpec validation, and review.
