# Contributing to OpsGuard

Thank you for contributing to OpsGuard.

This document defines the baseline engineering workflow and contribution standards for the project.

OpsGuard is developed as a production-oriented system. Changes should prioritize correctness, security, maintainability, testability, auditability, and operational clarity over implementation speed alone.

## Development Principles

Contributions should follow these principles:

1. Keep changes focused and intentional.
2. Preserve domain invariants.
3. Enforce authorization and tenant isolation server-side.
4. Never commit credentials or secrets.
5. Add or update tests when behavior changes.
6. Use database migrations for schema changes.
7. Keep business logic independent from transport concerns where practical.
8. Prefer explicit behavior over hidden conventions.
9. Document architectural decisions that materially affect the system.
10. Avoid introducing infrastructure or abstraction without a concrete requirement.

## Branching

The default branch is:

`main`

The `main` branch should remain in a valid and reviewable state.

Feature development should use short-lived branches when collaboration or pull-request workflows are introduced.

Suggested naming conventions include:

- `feature/incident-lifecycle`
- `feature/sla-tracking`
- `fix/tenant-isolation`
- `refactor/authorization-policy`
- `docs/security-model`
- `test/incident-state-machine`

Direct production deployment from unreviewed local changes is not an accepted workflow.

## Commit Messages

Commit messages should describe the intent of the change clearly.

Prefer concise, meaningful messages such as:

- `feat: add incident acknowledgement workflow`
- `feat: add SLA policy model`
- `fix: enforce organization boundary on incident lookup`
- `test: add incident transition authorization tests`
- `docs: document tenant isolation strategy`
- `refactor: centralize incident authorization rules`
- `chore: configure local PostgreSQL container`

Avoid vague messages such as:

- `update`
- `changes`
- `fix stuff`
- `working`
- `test`
- `final`

A commit should represent a coherent change whenever practical.

## Change Scope

Keep changes focused enough to understand, test, and review.

Avoid mixing unrelated concerns in the same change.

For example, a single change should not unnecessarily combine:

- database schema redesign;
- frontend visual redesign;
- authentication changes;
- unrelated refactoring;
- infrastructure changes.

Large features should be decomposed into logical, independently verifiable steps.

## Domain Integrity

Business rules must be enforced by the backend.

The frontend must never be treated as the authoritative enforcement layer for domain invariants.

Examples of domain-sensitive behavior include:

- incident state transitions;
- severity changes;
- incident assignment;
- acknowledgement;
- resolution;
- reopening;
- closure;
- SLA calculations;
- escalation;
- organization ownership;
- audit-event generation.

Invalid state transitions must be rejected even when a client attempts to call the API directly.

## Authorization

Authorization must be explicit and enforced server-side.

A request is not authorized merely because the user is authenticated.

Authorization decisions must consider, where applicable:

- authenticated identity;
- organization membership;
- account status;
- role;
- team membership;
- resource ownership;
- requested action;
- current resource state.

The system follows a deny-by-default approach.

When permission is not explicitly granted, access should be denied.

## Tenant Isolation

OpsGuard is designed as a multi-tenant system.

Organization boundaries are security boundaries.

Resources belonging to one organization must not be accessible from another organization.

Tenant isolation must be enforced server-side and must not rely on identifiers supplied by the frontend alone.

Queries and commands involving tenant-owned resources must validate organization ownership.

Tenant-isolation behavior requires automated testing.

## Secrets and Credentials

Never commit secrets or credentials to the repository.

This includes:

- passwords;
- API keys;
- access tokens;
- refresh tokens;
- private keys;
- production credentials;
- database passwords;
- cloud credentials;
- signing secrets.

Local secrets must be provided through approved environment configuration.

Example environment files may document required variables but must contain placeholder values only.

If a secret is accidentally committed, removing it from the latest version of the file is not sufficient. The credential must be considered compromised and rotated.

## Database Changes

Database schema changes must be performed through versioned migrations.

Do not depend on undocumented manual database changes.

Migrations should be:

- deterministic;
- reviewable;
- reproducible;
- appropriately tested;
- compatible with the intended deployment strategy.

Database constraints should enforce important invariants where appropriate.

Schema changes must be documented when they materially affect the domain model or operational behavior.

## API Changes

API behavior should remain explicit and predictable.

API changes should consider:

- request validation;
- response contracts;
- authentication;
- authorization;
- tenant isolation;
- error behavior;
- idempotency where relevant;
- concurrency where relevant;
- audit requirements;
- backward compatibility where required.

Public or externally consumed API contracts must be documented.

OpenAPI documentation should evolve together with the implementation.

## Error Handling

Expected failures should produce controlled and meaningful errors.

Do not expose sensitive internal details to API consumers.

Unexpected failures should be logged with enough context for investigation while avoiding leakage of secrets or sensitive information.

Error handling should be centralized where practical.

Domain errors should remain distinguishable from infrastructure failures and validation failures.

## Logging

Application logging should be structured and operationally useful.

Logs should provide enough context to investigate failures without exposing secrets.

Relevant context may include:

- request or correlation identifier;
- organization identifier where safe;
- authenticated actor identifier where safe;
- resource identifier;
- operation;
- result;
- error category.

Passwords, authentication tokens, secrets, and other sensitive credentials must never be logged.

## Audit Events

Security-sensitive and operationally significant actions must produce appropriate audit events.

Examples include:

- incident creation;
- incident assignment;
- acknowledgement;
- status transitions;
- severity changes;
- resolution;
- reopening;
- closure;
- privileged administrative actions;
- relevant authorization-sensitive configuration changes.

Audit history must not be silently rewritten through ordinary application workflows.

Audit behavior is part of the business requirement and must be tested.

## Incident State Machine

Incident status is controlled by an explicit state machine.

Status values must not behave as unrestricted editable strings.

A transition may require:

- a specific current state;
- sufficient authorization;
- valid organization ownership;
- required incident data;
- assignment conditions;
- completion of associated actions;
- creation of timeline events;
- creation of audit events;
- SLA side effects.

Transition rules should be implemented centrally rather than duplicated across controllers or UI components.

## SLA Behavior

SLA behavior is part of the domain model.

Changes affecting SLA calculations must explicitly consider:

- acknowledgement deadlines;
- resolution deadlines;
- severity changes;
- completed SLA instances;
- breached SLA instances;
- incident reopening;
- historical policy preservation;
- escalation behavior.

Historical SLA results must not silently change because an SLA policy is edited later.

SLA logic requires automated tests.

## Concurrency

Operations that can be affected by concurrent requests must be designed intentionally.

Examples include:

- simultaneous incident assignment;
- simultaneous state transitions;
- acknowledgement;
- resolution;
- SLA updates;
- escalation;
- administrative configuration changes.

Where required, use appropriate database constraints, transactions, locking, versioning, or other concurrency controls.

Do not assume that requests will always arrive sequentially.

## Transactions

Operations involving multiple related state changes should use transactional boundaries where consistency requires them.

For example, an incident transition may involve:

- updating the incident;
- creating a timeline event;
- creating an audit event;
- updating SLA state;
- creating additional domain records.

The system should not intentionally leave these operations partially completed when atomic behavior is required.

## Testing

Behavior changes require appropriate automated testing.

Depending on the change, tests may include:

- unit tests;
- integration tests;
- database integration tests;
- API tests;
- authorization tests;
- tenant-isolation tests;
- state-machine tests;
- SLA tests;
- frontend tests;
- end-to-end tests.

Critical behavior requires both successful-path and failure-path coverage.

Tests should include relevant cases such as:

- valid input;
- invalid input;
- unauthorized access;
- cross-organization access attempts;
- invalid state transitions;
- missing resources;
- concurrency-sensitive behavior where relevant;
- infrastructure failure behavior where practical.

A feature is not considered complete merely because its happy path works manually.

## Security Testing

Changes affecting security boundaries require explicit validation.

Security-sensitive areas include:

- authentication;
- authorization;
- organization isolation;
- SLA calculations;
- audit events;
- database constraints;
- concurrency-sensitive operations.

Security tests should verify both allowed and denied behavior.

## Documentation

Documentation is part of the implementation.

Update the relevant documentation when changing:

- architecture;
- domain behavior;
- APIs;
- database structures;
- security assumptions;
- infrastructure;
- deployment procedures;
- operational behavior.

Significant architectural decisions should be recorded as Architecture Decision Records under:

`docs/adr/`

Documentation should explain the current implemented behavior and must not claim that planned functionality already exists.

## Pull Requests

When pull-request workflows are introduced, pull requests should explain:

- what changed;
- why the change is required;
- how it was tested;
- relevant risks;
- database or deployment implications;
- screenshots for meaningful UI changes where appropriate.

Changes should remain small enough to review effectively whenever practical.

## Code Review

Review should evaluate more than whether the code compiles.

Relevant review areas include:

- correctness;
- domain invariants;
- security;
- authorization;
- tenant isolation;
- data integrity;
- transaction boundaries;
- concurrency behavior;
- error handling;
- observability;
- test coverage;
- maintainability;
- documentation impact.

Review findings should be resolved before a change is considered ready for integration.

## Definition of Done

A change is considered complete only when the level of validation appropriate to that change has been completed.

Depending on the change, this includes:

- implementation is complete;
- compilation succeeds;
- formatting and static checks pass;
- relevant automated tests pass;
- invalid inputs are handled;
- authorization is enforced;
- tenant isolation is preserved;
- database migrations are included where required;
- audit behavior is implemented where required;
- errors are handled appropriately;
- relevant logs and operational visibility exist;
- documentation is updated;
- no credentials or secrets are introduced;
- deployment implications are understood.

Passing compilation alone is not a definition of done.

## Dependency Management

Dependencies should be introduced intentionally.

Before adding a dependency, consider:

- whether the functionality is genuinely required;
- whether the standard platform already provides an adequate solution;
- maintenance status;
- security history;
- license compatibility;
- operational impact;
- long-term maintenance cost.

Avoid adding dependencies for trivial functionality.

Dependency versions should be controlled and updated deliberately.

## Infrastructure Changes

Infrastructure should be reproducible and version-controlled where practical.

Infrastructure changes should consider:

- local development;
- CI/CD;
- environment configuration;
- security;
- secrets;
- networking;
- persistence;
- observability;
- backup and recovery;
- rollback behavior.

Manual infrastructure steps should be documented when they cannot reasonably be automated.

## Production Safety

Production-oriented changes must account for failure and recovery.

Before deployment, consider:

- database migration safety;
- backward compatibility;
- configuration requirements;
- secrets;
- health checks;
- monitoring;
- logs;
- rollback strategy;
- data integrity;
- backup requirements.

Deployment success must not be inferred solely from a process starting successfully.

## Engineering Decision Records

Important architectural decisions should be documented rather than preserved only in conversation or developer memory.

Architecture Decision Records should capture:

- context;
- decision;
- alternatives considered where relevant;
- consequences;
- status.

ADRs are stored under:

`docs/adr/`

## Documentation Accuracy

Documentation must distinguish between:

- implemented functionality;
- functionality currently under development;
- planned functionality.

Do not describe planned capabilities as if they are already operational.

This applies to:

- README documentation;
- architecture documentation;
- API documentation;
- screenshots;
- portfolio material;
- demonstrations.

## Final Principle

OpsGuard is intended to be engineered as a credible production-oriented system.

Implementation speed is valuable, but it does not override correctness, security, auditability, testability, maintainability, or operational reliability.

Every meaningful change should leave the system in a state that can be understood, tested, reviewed, and evolved safely.