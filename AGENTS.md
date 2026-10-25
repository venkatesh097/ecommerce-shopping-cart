# AGENTS.md - Agent & Contributor Guidelines

This repository contains the backend service for the **E-Commerce Shopping Cart** college project. All autonomous agents and human contributors must adhere to the conventions and rules outlined in this document.

---

## 1. Project Overview & Scope
- **Domain**: E-Commerce Shopping Cart Backend REST API.
- **Base Package**: `com.example.ecommerce`
- **Target Java Version**: Java 17 / 21 compatibility (compiled with `--release 17`).
- **Framework**: Spring Boot 3.3.x with Spring Data JPA, Jakarta Validation, and PostgreSQL.

---

## 2. Architecture & Layering Rules
Code must strictly follow the standard layered package structure:
- `controller`: Exclusively contains `@RestController` classes handling HTTP requests, response status codes, and input validation. No raw SQL or complex business logic.
- `service`: Contains service interfaces and implementations (`@Service`). All business logic, transaction boundaries (`@Transactional`), and entity-to-DTO conversions reside here.
- `entity`: JPA entities (`@Entity`) representing database tables.
- `repository`: Spring Data JPA repositories (`JpaRepository`) providing data access operations.
- `dto`: Request and response POJOs/records (`@Valid` target classes, response bodies). Entities must not be leaked directly across public API boundaries.
- `exception`: Custom domain exceptions, error response models, and global exception handlers (`@RestControllerAdvice`).
- `config`: Spring `@Configuration` beans for security, CORS, Jackson, and integration settings.

---

## 3. Security & Credentials
- **NEVER hard-code credentials, passwords, API tokens, or secrets** in source code or committed property files.
- All environment-sensitive configurations (database connection, credentials, ports) must use Spring environment variable placeholders:
  ```properties
  spring.datasource.url=${DB_URL:...}
  spring.datasource.password=${DB_PASSWORD:...}
  ```
- Local testing should use in-memory databases (e.g. H2 in test profile) or non-sensitive local defaults.

---

## 4. Testing Guidelines
- Automated test suites must be maintained under `src/test/java`.
- Tests must pass cleanly via Maven without requiring a live, external PostgreSQL database running during basic builds.
- Tests should run using `mvn test` or `./mvnw test`.

---

## 5. Task & Status Tracking
- Whenever working on a task, you **must** update `PROJECT_STATUS.md`.
- `PROJECT_STATUS.md` tracks:
  - Task ID
  - Owner / Assignee
  - Current Status (`PENDING`, `IN_PROGRESS`, `COMPLETED`, `BLOCKED`)
  - Files Changed
  - Actual Test / Build Results
  - Blockers / Challenges
  - Next Steps

---

## 6. Implementation Guardrails
- Work only on the designated scope of your assigned task.
- Do not introduce unrelated frontend assets, external CI scripts, or unapproved dependencies unless explicitly instructed.
