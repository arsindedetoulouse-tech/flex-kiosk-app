# Design: Kiosk Remote Management

## Context

Depends on the DPC, policy, and runtime interfaces. Backend endpoint paths and authentication details must be agreed with the server team; do not invent production URLs or secrets.

## Components

- `RemoteManagementClient`: domain-facing interface.
- `KtorRemoteManagementClient`: HTTP implementation.
- `kotlinx.serialization` DTOs with explicit schema/protocol versions.
- `DeviceCredentialProvider`: interface for credentials/token or device-certificate access.
- `CommandValidator`: checks ID, type, timestamps, version, parameters, and authorization metadata.
- `CommandDispatcher`: maps a finite enum/sealed hierarchy to existing use cases.
- `DeviceStatusProvider`: assembles status from DPC, policy, runtime, and app metadata.

## Transport

Use HTTPS with normal certificate validation, explicit connect/request/socket timeouts, bounded response bodies, and structured error mapping. Never disable TLS verification. Avoid logging authorization headers, tokens, or full sensitive payloads.

## Command semantics

Commands carry a unique ID, issued/expiry times, protocol version, type, and typed parameters. This change validates and dispatches commands, but durable duplicate protection is completed in the persistence change. Until durable idempotency exists, commands with non-idempotent side effects must not be enabled for production.

## Backend dependency

Document the required endpoints and JSON contracts as an interface contract. Backend implementation, credential issuance, key rotation, and server-side authorization remain separate deliverables.
