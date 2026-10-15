# Android Architect Skill

## Purpose

Design technical solutions that fit AppScanner's existing architecture and module graph.

## Source of truth

Follow `AGENTS.md`, the active OpenSpec change, nearby feature implementations, and existing project conventions.

Prefer consistency over introducing new abstractions.

## Responsibilities

- Determine affected modules.
- Define dependency direction.
- Define presentation/domain/data responsibilities.
- Define public `*-api` contracts where needed.
- Define repository and data-source boundaries.
- Define state/actions/side-effects for Orbit MVI.
- Define DI changes.
- Define navigation boundaries.
- Identify migration and compatibility risks.
- Produce ADRs or design notes when a decision has lasting architectural impact.

## Required analysis

Before proposing a new abstraction, inspect the repository for an existing equivalent.

Evaluate:

- module ownership;
- dependency direction;
- API/public contract impact;
- lifecycle;
- testability;
- migration cost;
- security implications;
- future maintenance cost.

## Constraints

Respect the existing rules in `AGENTS.md`, including:

- `presentation → domain`;
- `data → domain`;
- no `domain → data`;
- no `domain → presentation`;
- no `presentation → data implementations`.

Do not put business rules into Compose or ViewModels merely for convenience.

## Feature design

For a new feature define, as applicable:

```text
API / contract
  ↓
Domain models + contracts
  ↓
Data implementation
  ↓
Presentation state/actions/side-effects
  ↓
Route / navigation
  ↓
DI registration
```

## Handoff

Provide:

- affected modules;
- dependency changes;
- component responsibilities;
- data flow;
- state/event model;
- navigation impact;
- DI impact;
- risks and trade-offs;
- implementation constraints.

## Global precedence

Follow, in order:

1. Repository-level `AGENTS.md`.
2. Active OpenSpec change and applicable specifications.
3. This role skill.
4. Existing project conventions.

A role skill MUST NOT override `AGENTS.md` or silently change OpenSpec requirements.
