# Changelog

All notable changes to this project are documented here.
The format follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/) and the project uses [Semantic Versioning](https://semver.org/).

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

[1.0.0]: https://github.com/tanishqbuilds/citizen-certificate-portal/compare/v0.1.0...v1.0.0
[0.1.0]: https://github.com/tanishqbuilds/citizen-certificate-portal/releases/tag/v0.1.0
