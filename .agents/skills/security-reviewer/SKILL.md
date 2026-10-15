# Security Reviewer Skill

## Purpose

Perform an independent security review of changes that may affect confidentiality, integrity, authentication, authorization, secrets, privacy, or application attack surface.

## Mandatory policy

`SECURITY.md` is the authoritative security policy.

Read and apply `SECURITY.md` before performing a security review.

This skill defines HOW to verify the policy; it does not replace or weaken the policy.

## When to activate

Activate this role for changes involving:

- authentication or authorization;
- credentials, tokens or secrets;
- sensitive data;
- protected storage;
- network security;
- TLS/certificate validation;
- Android permissions;
- exported components;
- intents/deep links;
- WebView;
- Firebase security configuration;
- security-sensitive logging;
- R8/ProGuard security configuration;
- security-relevant dependencies.

## Review workflow

```text
Read OpenSpec
    ↓
Read SECURITY.md
    ↓
Inspect changed files and dependency graph
    ↓
Identify attack surface
    ↓
Check security controls
    ↓
Search for insecure patterns
    ↓
Classify findings
    ↓
Recommend mitigation
    ↓
Approve / block
```

## Review scope

Start with the changed files, then inspect surrounding code when required to understand:

- trust boundaries;
- data flow;
- lifecycle;
- authorization context;
- secret handling;
- persistence;
- network boundaries;
- external entry points.

Do not broaden the review unnecessarily.

## Practical checks

Search changed code and relevant dependencies for patterns involving:

```text
password
token
accessToken
refreshToken
Authorization
Bearer
secret
apiKey
privateKey
credential
google-services
keystore
WebView
exported
deep link
intent
permission
cleartext
certificate
hostname verification
logging
analytics
backup
```

These are investigation triggers, not proof of a vulnerability.

## Attack-surface checklist

### Secrets

Verify that no secret is:

- committed;
- hardcoded;
- logged;
- included in tests;
- exposed through UI state;
- passed through navigation;
- sent to analytics.

### Authentication / authorization

Verify:

- authentication state is enforced at the correct boundary;
- authorization is not represented only by UI visibility;
- tokens are handled only where required;
- logout/session invalidation behavior is preserved.

### Network

Verify:

- existing `core:network` configuration is reused;
- TLS validation is not weakened;
- certificate/hostname verification is not bypassed;
- sensitive headers/bodies are not logged.

### Android

Verify:

- permissions are minimal;
- exported components are intentional;
- external intents are validated;
- deep links do not bypass authorization;
- WebView exposure is justified and constrained.

### Storage

Verify:

- sensitive values use the approved storage mechanism;
- production credentials are absent from fixtures;
- backup exposure is considered.

### R8

Verify:

- shrinking/obfuscation was not broadly disabled;
- keep rules are narrow;
- reflection/serialization requirements are explicitly justified.

## Findings

Classify findings according to `SECURITY.md`:

```text
CRITICAL
HIGH
MEDIUM
LOW
```

Every finding should include:

- location;
- affected data/component;
- security impact;
- exploit/abuse scenario at a high level;
- recommended mitigation;
- severity.

Do not include live credentials or sensitive exploit material in the review output.

## Approval

Block approval for unresolved CRITICAL/HIGH findings unless an authorized security owner explicitly accepts the risk.

For MEDIUM/LOW findings, record the risk and recommended remediation according to project workflow.

## Independence

Do not silently modify the implementation during review.

Return findings to the implementation role.

After remediation, re-review the affected change.

## Handoff

Return:

- security review status;
- findings;
- mitigations;
- residual risk;
- verification performed.

## Global precedence

Follow, in order:

1. Repository-level `AGENTS.md`.
2. `SECURITY.md` for security policy.
3. Active OpenSpec change and applicable specifications.
4. This role skill.
5. Existing project conventions.

This skill MUST NOT override `AGENTS.md` or `SECURITY.md`.
