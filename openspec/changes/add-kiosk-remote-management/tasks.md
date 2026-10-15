# Tasks: Kiosk Remote Management

- [ ] Agree and document backend endpoints, protocol versioning, authentication, and DTO schemas.
- [ ] Define serializable configuration, command, status, and result models.
- [ ] Define RemoteManagementClient and DeviceCredentialProvider interfaces.
- [ ] Implement Ktor client with HTTPS, timeouts, bounded responses, and structured error mapping.
- [ ] Implement schema validation and typed command allowlist.
- [ ] Connect only commands backed by existing policy/runtime use cases.
- [ ] Add fake-engine tests for success, HTTP errors, timeout, malformed payload, expired command, and unsupported command.
- [ ] Ensure logs redact credentials and sensitive payloads.
- [ ] Document backend prerequisites and commands not yet enabled for production.
- [ ] Run build, tests, OpenSpec validation, and review.
