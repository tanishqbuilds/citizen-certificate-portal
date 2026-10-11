# Changelog

All notable changes to this project are documented here.
The format follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/) and the project uses [Semantic Versioning](https://semver.org/).

## [1.2.0] — 2026-10-11 · Containers and configuration management

### Added
- Layered, non-root Docker image; container lifecycle script (#33)
- Pipeline builds versioned images, gates them with Selenium on an ephemeral staging container, pushes to a private registry and deploys a fresh production container with automatic rollback (#34)
- Ansible roles and playbooks (`site.yml`, `rollback.yml`, `healthcheck.yml`) for a clean Ubuntu 24.04 node; checksum-verified releases, ansible-vault secrets (#35)

### Fixed
- Tomcat health check and persistence of deployed contexts (#37)

## [1.1.0] — 2026-10-11 · CI and quality gate

### Added
- **Jenkins as code** (`ci/`): Dockerised Jenkins 2.580.1 LTS with Configuration-as-Code, pinned plugins and Job DSL (#21)
- `ccrp-ci-build`: SCM-polled Maven job with JUnit results and an archived, fingerprinted WAR (#21)
- **Jenkinsfile**: checkout → build & test → package → deploy → smoke test, with the `TARGET_ENV` parameter (#23, #24)
- **Tomcat 11 deployment** through the Manager text API; per-environment settings as context parameters (#23)
- **Selenium WebDriver suite**: 8 journey tests, Page Object Model, CSV test data, failure screenshots, HTML report (#26)
- **Quality gate**: journeys run against staging through a Selenium Grid; failures stop promotion to production (#27)
- Commit statuses on GitHub from Jenkins

### Fixed
- External Tomcat failed with `NoClassDefFoundError: …/WebServer` (Maven nearest-wins moved `spring-boot-web-server` to `lib-provided`) (#23)
- Declaration label did not toggle its checkbox (#26)
- Officer login test depended on the build machine's environment (#22)
- "Track status" on the acknowledgement page opened the empty lookup form, a deliberate defect caught by the gate (#28 → #29)

## [1.0.0] — 2026-10-11 · MVP release

### Added
- **Citizen journey**: certificate application for Birth, Income, Domicile and Marriage with one supporting document (#16)
- **Validation**: Verhoeff-checked 12-digit national ID (stored as mask + SHA-256 only), Indian mobile and PIN rules, type-specific rules (income amount, spouse name, adult applicant) (#16)
- **Documents**: PDF/PNG/JPEG detected by magic bytes, 2 MB limit, extension must match content, SHA-256 fingerprint, random storage names (#16)
- **Tracking**: non-sequential tracking IDs (`CCR-<year>-<6>`), enumeration-safe lookup by ID + email, progress stepper and timeline (#16)
- **JSON API** `/api/v1`: certificate types, request status, public certificate verification (#17)
- **Officer workspace**: form login, KPI cards, filterable/searchable queue, request detail with inline document (#18)
- **Decisions**: start review, approve (issues `SND-<code>-<year>-<id>` + verification hash), reject with mandatory reason (#18)
- **Certificates**: printable certificate page, visible to the verified citizen or an officer (#18)
- **Audit trail** of every status change with actor, remarks and time (#16, #18)
- Liquid-glass / grainy-gradient design system with light, dark and system themes (v0.1.0)

### Fixed
- Pages failed with "Cannot create a session after the response has been committed": CSRF tokens are now resolved eagerly (#16)
- Uploaded documents were left on disk when saving the request failed (#16)
- Age rules were evaluated in the JVM zone (UTC) instead of IST (#16)

### Security
- `/officer/**` requires `ROLE_OFFICER`; BCrypt; session fixation protection; CSRF on all forms
- Officer password comes from `APP_OFFICER_PASSWORD`; start-up fails if it is shorter than 8 characters

## [0.1.0] — 2026-09-11 · Foundation
### Added
- Repository governance (README, CONTRIBUTING with branch policy, issue/PR templates, CODEOWNERS)
- Problem definition, agile plan, SRS and architecture documents
- Spring Boot 4.1 skeleton with Maven wrapper, executable WAR, build/git info at `/actuator/info`

[1.2.0]: https://github.com/tanishqbuilds/citizen-certificate-portal/compare/v1.1.0...v1.2.0
[1.1.0]: https://github.com/tanishqbuilds/citizen-certificate-portal/compare/v1.0.0...v1.1.0
[1.0.0]: https://github.com/tanishqbuilds/citizen-certificate-portal/compare/v0.1.0...v1.0.0
[0.1.0]: https://github.com/tanishqbuilds/citizen-certificate-portal/releases/tag/v0.1.0
