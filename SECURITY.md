# Security Policy

Security is a core engineering requirement of OpsGuard.

OpsGuard manages operational incidents, organization data, user identities, permissions, audit history, and potentially sensitive infrastructure-related information. Security controls are therefore treated as part of the product architecture rather than as optional enhancements.

This document defines the baseline security principles for the project.

## Security Principles

OpsGuard follows these principles:

1. Deny access by default.
2. Authenticate every protected request.
3. Authorize every protected operation server-side.
4. Enforce organization isolation server-side.
5. Apply least-privilege access.
6. Validate all untrusted input.
7. Never trust client-side authorization.
8. Never commit credentials or secrets.
9. Preserve security-relevant audit history.
10. Minimize exposure of sensitive information.
11. Use secure defaults.
12. Treat dependencies and infrastructure as part of the security boundary.

## Authentication

Protected OpsGuard functionality requires an authenticated identity.

Authentication determines who the caller is.

Authentication alone does not grant permission to perform an operation.

The backend must reject protected requests that do not contain valid authentication credentials.

Authentication failures should not expose unnecessary implementation details.

## Authorization

Authorization determines whether an authenticated user may perform a specific operation.

Authorization must be enforced by the backend.

Frontend controls may improve user experience but must never be treated as a security boundary.

A user must not gain access to an operation merely by:

- modifying frontend code;
- changing a request payload;
- calling an API endpoint directly;
- guessing a resource identifier;
- manipulating route parameters;
- changing client-side state.

Authorization decisions may consider:

- authenticated user;
- organization membership;
- account status;
- role;
- team membership;
- resource ownership;
- requested operation;
- current resource state.

When access is not explicitly permitted, it should be denied.

## Role-Based Access Control

OpsGuard uses role-based access control as part of its authorization model.

Initial platform roles include:

- `ADMIN`
- `MANAGER`
- `ENGINEER`
- `VIEWER`

Roles provide permission boundaries but do not override organization isolation or domain invariants.

An administrator is not permitted to bypass fundamental security or data-integrity rules merely because the account has an administrative role.

Authorization logic should be centralized and testable rather than scattered across controllers and UI components.

## Multi-Tenant Isolation

OpsGuard is designed as a multi-tenant platform.

Each tenant is represented by an organization.

Organization boundaries are security boundaries.

A user belonging to one organization must not be able to access resources belonging to another organization unless an explicit future cross-organization capability is intentionally designed and authorized.

Tenant-owned resources include, where applicable:

- users;
- teams;
- services;
- incidents;
- assignments;
- comments;
- timeline events;
- SLA policies;
- SLA instances;
- notifications;
- audit events;
- post-incident reviews.

Tenant isolation must be enforced server-side.

The backend must not trust an organization identifier supplied by the client without validating it against the authenticated identity and requested resource.

Cross-organization access attempts must be rejected.

Tenant isolation requires automated tests.

## Resource Access

Possession or knowledge of a resource identifier does not grant access to that resource.

Every protected resource lookup must account for the authenticated user's organization and permissions.

Endpoints must be designed to prevent insecure direct object reference behavior.

Where practical, resource queries should include tenant ownership constraints directly rather than loading arbitrary resources first and relying exclusively on later checks.

## Input Validation

All external input is untrusted.

This includes:

- request bodies;
- query parameters;
- path parameters;
- HTTP headers;
- uploaded data;
- integration payloads;
- webhook payloads;
- imported data;
- user-generated text.

Input should be validated for:

- required values;
- allowed values;
- expected format;
- size limits;
- type constraints;
- domain constraints.

Validation must occur server-side even when equivalent frontend validation exists.

## Domain Security

Security includes protecting business invariants.

Users must not be able to bypass the incident lifecycle by directly modifying status values.

Sensitive domain operations must use explicit application behavior.

Examples include:

- incident acknowledgement;
- assignment;
- severity changes;
- incident transitions;
- resolution;
- reopening;
- closure;
- SLA changes;
- escalation;
- privileged configuration changes.

The backend must validate both permission and domain state before executing these operations.

## Secrets Management

Secrets must never be committed to the repository.

Secrets include:

- passwords;
- API keys;
- database credentials;
- access tokens;
- refresh tokens;
- signing keys;
- private keys;
- cloud credentials;
- webhook secrets;
- encryption keys;
- production environment values.

Secrets should be provided through approved environment configuration or a dedicated secret-management system.

Files containing local secrets must be excluded from version control.

Example configuration files may be committed only when they contain safe placeholder values.

If a secret is accidentally committed, it must be considered compromised and rotated.

Deleting the secret from the latest commit alone is not sufficient.

## Password Security

If OpsGuard directly manages passwords, passwords must never be stored in plaintext.

Password handling must use an established password-hashing algorithm and appropriate framework security mechanisms.

Passwords must never appear in:

- logs;
- audit events;
- API responses;
- exception messages;
- analytics;
- source code.

Authentication design should prefer established security components over custom cryptographic implementations.

## Token Security

Authentication and authorization tokens must be treated as secrets.

Tokens must not be written to application logs.

Token validation must verify all security-relevant properties required by the chosen authentication mechanism.

Expired, malformed, invalid, or otherwise unacceptable tokens must be rejected.

Token configuration must differ appropriately between local development and production environments.

## Transport Security

Production traffic carrying authenticated or sensitive information must use encrypted transport.

Production deployment should use HTTPS.

Plain HTTP may be used for controlled local development where appropriate.

Secure transport termination and proxy configuration must be documented as deployment architecture evolves.

## Database Security

Database access must follow least-privilege principles.

Application database credentials should have only the permissions required by the application.

Database access must not be exposed directly to untrusted clients.

Important integrity rules should be enforced with database constraints where appropriate.

Database migrations must be version-controlled and reviewable.

Production database credentials must never be committed to source control.

## SQL Injection Prevention

Database access should use parameterized queries, ORM mechanisms, or other safe database APIs.

Untrusted input must never be concatenated directly into executable SQL.

Dynamic query behavior must be designed carefully and tested where user-controlled values affect filtering, sorting, or query construction.

## Audit Security

Audit events provide security and accountability evidence.

Security-relevant actions should generate appropriate audit records.

Examples include:

- authentication-sensitive administrative actions;
- incident creation;
- assignment changes;
- severity changes;
- status transitions;
- acknowledgement;
- resolution;
- reopening;
- closure;
- role changes;
- relevant organization configuration changes.

Ordinary application users must not be able to silently rewrite audit history.

Audit records should identify relevant information such as:

- actor;
- organization;
- action;
- resource type;
- resource identifier;
- timestamp;
- relevant previous state;
- relevant resulting state.

Audit data must not contain passwords, authentication tokens, or other secrets.

## Logging Security

Application logs must be useful for investigation without becoming a source of sensitive-data leakage.

Never intentionally log:

- passwords;
- access tokens;
- refresh tokens;
- API secrets;
- private keys;
- authentication credentials.

Sensitive user or operational data should be logged only when necessary and appropriate.

Structured logs should use identifiers and operational context rather than unnecessary sensitive content.

## Error Handling

External error responses must not expose unnecessary internal details.

Production responses should avoid exposing:

- stack traces;
- database credentials;
- SQL internals;
- filesystem paths;
- secret configuration;
- internal tokens;
- infrastructure credentials.

Detailed diagnostic information may be recorded securely in server-side logs when appropriate.

Expected security failures should return controlled responses.

## Data Exposure

API responses should return only the information required by the operation.

Do not expose entire database entities merely because they are convenient to serialize.

Sensitive fields should be excluded intentionally.

Data-transfer objects or equivalent explicit response models should be used where appropriate.

## Mass Assignment

Request payloads must not be mapped blindly onto security-sensitive domain entities.

Clients must not be able to modify protected fields simply by adding unexpected properties to a request.

Protected fields may include:

- organization identifiers;
- roles;
- ownership fields;
- audit metadata;
- SLA completion fields;
- system timestamps;
- privileged status fields.

Writable fields should be explicitly defined by the application.

## Cross-Origin Security

Cross-origin resource sharing must use an explicit configuration.

Production environments should not use unrestricted origins for authenticated APIs unless a concrete requirement justifies that behavior.

Allowed origins should be environment-specific.

## CSRF

The required CSRF protection strategy depends on the final authentication architecture.

If authentication relies on browser cookies, appropriate CSRF protections must be implemented.

If the architecture uses another authentication transport, the threat model must be documented and protections selected accordingly.

CSRF protection must not be disabled merely to simplify development.

## Browser Security

The production web application should use appropriate browser security controls.

Relevant controls may include:

- secure cookies where cookies are used;
- `HttpOnly` cookies where appropriate;
- `SameSite` policy;
- Content Security Policy;
- frame restrictions;
- MIME-type protections;
- referrer policy;
- secure transport enforcement.

Exact policies will be defined together with the production architecture.

## Dependency Security

Third-party dependencies are part of the security surface.

Dependencies should be:

- intentionally selected;
- actively maintained where practical;
- version-controlled;
- monitored for known vulnerabilities;
- updated deliberately.

Unnecessary dependencies should be avoided.

Security findings in dependencies should be evaluated according to actual exposure and impact rather than ignored or updated blindly.

## Container Security

Containers do not eliminate application security requirements.

Container images should:

- use trusted base images;
- minimize unnecessary packages;
- avoid embedded secrets;
- use controlled versions;
- be rebuilt when relevant security updates are required.

Production containers should avoid unnecessary privileges.

Container configuration should be version-controlled.

## Infrastructure Security

Infrastructure configuration is part of the security model.

Production infrastructure should consider:

- network exposure;
- firewall rules;
- database exposure;
- secret storage;
- TLS;
- service permissions;
- container privileges;
- persistent storage;
- backups;
- monitoring;
- patching;
- recovery procedures.

Services should not be exposed publicly unless required.

## CI/CD Security

CI/CD workflows must not expose credentials in repository files or build logs.

Secrets used by automated workflows should use the secret-storage mechanisms provided by the CI/CD platform.

Automated workflows should receive only the permissions they require.

Changes to deployment workflows should be reviewed as security-sensitive changes.

## File Upload Security

If file uploads are introduced, they must be treated as untrusted input.

The implementation must consider:

- file-size limits;
- allowed file types;
- content validation;
- storage isolation;
- filename handling;
- malware risk;
- authorization;
- tenant isolation;
- retention;
- download authorization.

Uploaded files must not become executable merely because they were accepted by the application.

## Webhooks and Integrations

If inbound integrations or webhooks are introduced, requests must be authenticated or cryptographically verified where supported.

Webhook processing should consider:

- signature verification;
- replay protection where applicable;
- idempotency;
- payload validation;
- tenant identification;
- rate limiting;
- auditability.

Integration secrets must be handled as credentials.

## Rate Limiting

Security-sensitive and abuse-prone endpoints should use appropriate rate limiting when the application architecture requires it.

Potential targets include:

- authentication;
- password recovery;
- invitation flows;
- public integrations;
- webhooks;
- resource-intensive operations.

Rate-limiting behavior should be observable and configurable where appropriate.

## Concurrency and Security

Concurrent requests must not allow security or domain rules to be bypassed.

Sensitive operations should consider race conditions.

Examples include:

- simultaneous assignment;
- simultaneous acknowledgement;
- conflicting state transitions;
- duplicate resolution requests;
- role changes;
- SLA updates.

Appropriate transactional or concurrency-control mechanisms must be used where required.

## Security Testing

Security requirements require automated verification.

Tests should cover relevant cases such as:

- unauthenticated access;
- unauthorized access;
- cross-organization access;
- privilege escalation attempts;
- invalid state transitions;
- protected-field modification;
- malformed input;
- tenant-bound resource enumeration;
- security-sensitive domain operations.

Both allowed and denied behavior should be tested.

## Security Headers

Production HTTP security headers should be configured deliberately.

The final configuration will depend on the frontend, authentication, proxy, and deployment architecture.

Security headers should be tested rather than assumed to be present.

## Data Retention and Deletion

Data-retention and deletion behavior must be explicitly designed.

Historical operational and audit records may have different retention requirements from ordinary application data.

Records must not be physically deleted merely for convenience when deletion would destroy required accountability history.

Where user or organization deletion is supported, the system must define how historical references, audit requirements, and applicable legal obligations are handled.

## Backups

Production data requires an explicit backup strategy.

The strategy should define:

- what is backed up;
- backup frequency;
- retention;
- storage security;
- restoration procedure;
- restoration testing.

A backup that has never been tested for restoration must not be assumed to provide reliable recovery.

## Security Incident Response

Security incidents affecting OpsGuard itself should be handled as security-sensitive operational events.

The response process should include, where appropriate:

- identification;
- containment;
- investigation;
- remediation;
- credential rotation;
- recovery;
- impact assessment;
- documentation;
- preventive actions.

Security events should preserve evidence required for investigation.

## Vulnerability Reporting

Security vulnerabilities should not be published with exploitable details before they can be assessed and remediated.

For the current development stage, vulnerabilities should be reported privately to the project owner or maintainers.

A dedicated security contact and disclosure process may be introduced before public production use.

## Production Readiness

Before a production release, security validation should include the controls relevant to the implemented architecture.

This includes reviewing:

- authentication;
- authorization;
- tenant isolation;
- secret handling;
- transport security;
- database access;
- audit behavior;
- logging;
- error handling;
- dependencies;
- infrastructure exposure;
- backups;
- deployment permissions.

Production readiness is not established merely because the application runs successfully.

## Security Documentation

Security decisions that materially affect architecture should be documented.

Relevant documentation may be stored under:

`docs/security/`

Major architectural security decisions may also require an Architecture Decision Record under:

`docs/adr/`

Documentation must reflect implemented behavior accurately and distinguish implemented controls from planned controls.

## Final Principle

OpsGuard treats security as a system property.

No individual frontend control, framework feature, authentication mechanism, container, or infrastructure service is sufficient by itself.

Security depends on maintaining consistent controls across identity, authorization, tenant isolation, domain behavior, data access, infrastructure, deployment, observability, and operational procedures.