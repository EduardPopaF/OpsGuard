# OpsGuard

> Enterprise incident and operations management platform for coordinating, tracking, resolving, and analyzing operational and technical incidents.

## Status

**Active development — pre-release**

OpsGuard is currently being built as a production-oriented platform with a strong focus on reliability, security, auditability, maintainability, and operational visibility.

The repository is under active development. APIs, architecture, and implementation details may evolve before the first stable release.

## Overview

OpsGuard provides organizations with a centralized system for managing the complete lifecycle of operational and technical incidents.

The platform is designed to answer critical operational questions clearly:

- What happened?
- Which service is affected?
- How severe is the incident?
- Who currently owns the response?
- When was the incident acknowledged?
- What actions have been taken?
- Are SLA targets at risk or already breached?
- How was the incident resolved?
- What can be learned to prevent recurrence?

Rather than relying on fragmented communication across chat messages, spreadsheets, email threads, and undocumented decisions, OpsGuard creates a structured and auditable operational record.

## Core Capabilities

Planned core capabilities include:

- Incident creation and lifecycle management
- Severity classification
- Team and engineer assignment
- Role-based access control
- Multi-tenant organization isolation
- Service ownership
- SLA policies and SLA tracking
- Escalation workflows
- Incident timelines
- Comments and operational collaboration
- Immutable audit history
- Notifications
- Post-incident reviews
- Operational analytics
- MTTA and MTTR reporting
- SLA compliance reporting
- Secure REST APIs
- Health and readiness monitoring

## Incident Lifecycle

The incident lifecycle is modeled as an explicit state machine rather than unrestricted status editing.

The exact transition rules, authorization requirements, invariants, and side effects are maintained as part of the engineering specification and architecture documentation.

## Architecture

OpsGuard is being designed initially as a **modular monolith** with explicit domain boundaries.

This provides:

- transactional consistency where required;
- lower operational complexity during the initial product stages;
- clear ownership between application modules;
- the ability to extract independently deployable services later when justified by real scaling or organizational requirements.

The architecture is intentionally designed to avoid premature distributed-system complexity while preserving future evolution paths.

## Technology Direction

The planned technology stack includes:

### Backend

- Java 21 LTS
- Spring Boot
- Spring Security
- PostgreSQL
- Database migrations
- OpenAPI documentation
- Automated unit and integration testing

### Frontend

- TypeScript
- React
- Next.js
- Automated frontend testing

### Infrastructure

- Docker
- Docker Compose
- CI/CD
- Structured application logging
- Metrics and health monitoring
- Environment-specific configuration

Technology choices may be refined through Architecture Decision Records as implementation progresses.

## Security Principles

OpsGuard is being developed with security as an architectural requirement rather than a later feature.

Core principles include:

- deny by default;
- centralized authorization;
- strict tenant isolation;
- least privilege;
- validated input;
- secure secret management;
- auditable privileged operations;
- no credentials committed to source control;
- immutable security-relevant history where required.

## Engineering Principles

The project follows several engineering rules:

1. Correctness before convenience.
2. Explicit domain invariants.
3. Business logic separated from transport and persistence concerns.
4. Database changes performed through versioned migrations.
5. Security boundaries enforced server-side.
6. Important state changes are auditable.
7. Features require appropriate automated tests.
8. Infrastructure must be reproducible.
9. Documentation evolves together with implementation.
10. Complexity must be justified by an actual requirement.

## Repository Structure

OpsGuard/
├── .github/            # CI/CD workflows and repository automation
├── backend/            # Backend application
├── frontend/           # Web application
├── infrastructure/     # Local and deployment infrastructure
├── docs/
│   ├── adr/            # Architecture Decision Records
│   ├── api/            # API documentation
│   ├── architecture/   # System architecture documentation
│   ├── database/       # Data model and database documentation
│   ├── images/         # Documentation assets and product screenshots
│   ├── operations/     # Deployment and operational documentation
│   ├── security/       # Security model and threat-related documentation
│   └── testing/        # Testing strategy and quality documentation
├── .editorconfig
├── .gitattributes
├── .gitignore
├── CONTRIBUTING.md
├── SECURITY.md
└── README.md

## Documentation

Engineering documentation is maintained inside the `docs/` directory and will evolve together with the implementation.

It will include:

- system architecture;
- domain model;
- incident state machine;
- database design;
- authorization model;
- tenant isolation strategy;
- SLA behavior;
- API contracts;
- Architecture Decision Records;
- testing strategy;
- deployment procedures;
- operational runbooks.

Documentation is treated as part of the product rather than as an afterthought.

## Development

Local development instructions will be added as the backend, frontend, database, and infrastructure modules are introduced.

The target is a reproducible local development environment that can be started without undocumented machine-specific configuration.

Development infrastructure will be containerized where appropriate to reduce environment differences between contributors and deployment targets.

## Testing

The testing strategy will cover multiple layers where appropriate, including:

- unit tests;
- integration tests;
- authorization tests;
- tenant-isolation tests;
- database integration tests;
- API tests;
- frontend tests;
- end-to-end tests for critical workflows.

Critical business rules such as incident state transitions, SLA calculations, authorization decisions, audit generation, and organization isolation require explicit automated coverage.

Passing the happy path alone is not considered sufficient validation.

## Reliability and Operations

OpsGuard is intended to provide operational visibility into both the incidents it manages and the health of the platform itself.

Operational capabilities will include, where appropriate:

- application health checks;
- readiness checks;
- structured logs;
- metrics;
- error visibility;
- database health monitoring;
- deployment verification;
- backup and recovery procedures.

Operational behavior and recovery procedures will be documented under `docs/operations/`.

## Security

Security-sensitive behavior is enforced by the backend and must not depend solely on frontend restrictions.

Particular attention is given to:

- authentication;
- authorization;
- organization boundaries;
- privileged operations;
- secret management;
- input validation;
- auditability;
- secure configuration.

The detailed security model and relevant threat analysis will be maintained under `docs/security/`.

Security vulnerability reporting guidance is maintained in `SECURITY.md`.

## Architecture Decisions

Material architectural decisions are documented using Architecture Decision Records (ADRs).

ADRs are stored under `docs/adr/`.

Each accepted decision should document the context, decision, consequences, and relevant alternatives where appropriate.

This allows future contributors to understand not only how OpsGuard is built, but why important architectural decisions were made.

## Product Direction

OpsGuard is intended to evolve beyond a demonstration application.

The project is being engineered so that it can mature into a deployable incident and operations management product suitable for real organizations.

Future capabilities may include:

- external monitoring integrations;
- automated incident creation;
- configurable escalation policies;
- email and collaboration-platform notifications;
- advanced operational analytics;
- service dependency modeling;
- configurable organization policies;
- incident automation;
- historical incident intelligence;
- AI-assisted incident summarization;
- AI-assisted post-incident analysis.

Future capabilities are not considered implemented until they exist, are tested, and are documented accordingly.

## Project Goals

The engineering goals of OpsGuard are to:

- solve a real operational problem;
- maintain strong security and tenant isolation;
- preserve complete accountability for important actions;
- provide reliable incident lifecycle management;
- provide measurable SLA performance;
- remain maintainable as functionality grows;
- support reproducible development and deployment;
- maintain useful automated test coverage;
- provide clear technical and operational documentation;
- preserve a credible path from initial product to commercial deployment.

## Current Development Stage

OpsGuard is currently in its foundational development stage.

Repository structure, engineering standards, architecture, security boundaries, domain rules, infrastructure, and testing strategy are being established before substantial feature implementation begins.

This status will be updated as the project reaches meaningful implementation milestones.

## Screenshots and Demo

Product screenshots, architecture visuals, and demonstration material will be added after the relevant functionality exists and the user interface reaches a representative state.

Documentation assets are stored under `docs/images/`.

No placeholder screenshots or misleading demonstrations are included.

## Contributing

Contribution standards and engineering workflow are documented in `CONTRIBUTING.md`.

Contributions should preserve the project's security boundaries, architectural principles, testing requirements, and documentation standards.

## License

No public license has been granted at this stage.

All rights reserved unless a license is explicitly added to this repository.