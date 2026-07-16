# Copilot Instructions

This repository follows the DPC (Dans Plugins Community) conventions defined at
https://github.com/Dans-Plugins/dpc-conventions. Read those conventions before
making any changes.

## Technology Stack

- Language: Java 17
- Build tool: Maven
- Framework: Spring Boot 3
- Key libraries: kohsuke/github-api, Eclipse JGit
- Test framework: JUnit 5 (via Spring Boot Test)

## Project Structure

- `src/main/java/com/github/backup/` – Core application classes (backup service, GitHub service, CLI runner)
- `src/main/java/com/github/backup/web/` – Spring MVC REST controller and request/response models for web mode
- `src/main/resources/` – `application.properties`, `application-web.properties`, and static web assets
- `src/test/java/` – Unit tests mirroring the main source tree

## Coding Conventions

- Follow the existing package structure (`com.github.backup`) when adding new classes.
- Configuration is loaded from `application.properties`; use `@Value` or `@ConfigurationProperties` for new options.
- All new configuration options must be documented in `CONFIG.md`.
- User-facing documentation changes must be included in the same pull request as the related code change.

## Contribution Workflow

- Branch from `develop` for all changes.
- Open a pull request against `develop`, not `main`.
- Reference the related GitHub issue in every pull request description.
