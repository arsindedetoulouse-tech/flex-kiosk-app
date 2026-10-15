# Design: Kiosk Persistence and Recovery

## Storage

Use SQLite through the project's established database access layer. If no database abstraction exists, introduce repositories and DAOs without exposing SQL details to domain use cases.

Suggested tables:
- `kiosk_config`: config version, payload, validation status, applied timestamp.
- `management_command`: unique command ID, type, expiry, payload, status, attempt count, timestamps.
- `command_result`: command ID, result status, safe result metadata, delivery status.
- `sync_state`: last successful sync, last error category, next eligible retry time.

Use unique constraints on command IDs and transactions for state transitions. Define retention limits and migration strategy.

## Command state machine

`RECEIVED -> VALIDATED -> IN_PROGRESS -> SUCCEEDED | FAILED | REJECTED`

Delivery state is separate: `PENDING_DELIVERY -> DELIVERED`. A completed command whose result has not been delivered must not be re-executed.

After process death, `IN_PROGRESS` operations require per-command recovery semantics. Do not blindly replay operations whose outcome cannot be determined.

## Retries

Use bounded exponential backoff with jitter where appropriate. Use WorkManager for deferrable persistent sync, not a permanently running Activity coroutine. A foreground service is not introduced unless a documented platform requirement justifies it.

## Security

Do not persist bearer tokens, private keys, or reusable secrets in plaintext. Use Android Keystore-backed protection where applicable. Minimize command payload and result retention.
