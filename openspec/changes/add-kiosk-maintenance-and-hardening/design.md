# Design: Kiosk Maintenance and Security Hardening

## Maintenance boundary

Define a finite maintenance state machine and require a strong, project-approved authorization mechanism. Do not rely only on a hidden gesture or static PIN embedded in the APK. The UI is a trigger, not the security boundary; authorization must be checked by the domain/service layer.

## Audit

Record privileged operation type, device ID, timestamp, correlation/command ID, outcome, and safe error category. Redact tokens, credentials, private keys, and sensitive command payloads. Define retention and upload behavior.

## Decommissioning

Treat decommissioning as a multi-step, resumable workflow:
1. Authenticate and authorize the request.
2. Notify backend and revoke/disable device credentials according to the agreed protocol.
3. Stop accepting new remote commands.
4. Clear application-owned state according to retention policy.
5. Remove supported kiosk policy settings in a documented order.
6. Report incomplete steps and any platform-specific actions still required.

Do not claim that clearing Lock Task allowlist or HOME preference removes Device Owner status. Device Owner removal follows Android-supported provisioning/management procedures and must be verified separately.

## Updates and compatibility

Use the selected managed deployment/update mechanism. Do not implement arbitrary APK installation. Maintain a matrix of tested Android versions and device manufacturers. Document behavior that is not guaranteed by Android APIs.

## Acceptance

Run an end-to-end suite covering fresh provisioning, reboot, process death, network outage, replayed command, invalid authorization, policy mismatch, update failure, maintenance, and decommission interruption.
