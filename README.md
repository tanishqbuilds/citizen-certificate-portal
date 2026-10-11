# Sanad — Citizen Certificate Request Portal

Citizens apply online for government certificates, upload one supporting document and track
progress with a tracking ID. Officers review requests and approve or reject them.

> **v1.1.0**: MVP plus Jenkins CI/CD, Tomcat deployment and a Selenium quality gate. Docker and Ansible land in v1.2.0. See [`docs/`](docs/) and the [CHANGELOG](CHANGELOG.md).

## Features

- **Apply** for a Birth, Income, Domicile or Marriage certificate with one supporting document
- **Validation** at the door: Verhoeff-checked national ID, type-specific rules, documents verified by magic bytes
- **Track** a request with its tracking ID and email: progress stepper and activity timeline
- **Officer workspace**: sign-in, KPI cards, filterable queue, start review / approve / reject with reason
- **Certificates**: numbered, SHA-256 verification hash, printable
- **Audit trail** of every status change with actor and time
- **JSON API** (`/api/v1`): certificate types, request status, public certificate verification

## Repository layout

```
.github/          Issue templates, PR template, CODEOWNERS
docs/             Project documentation (problem definition → final report) and evidence
src/              Application source (Spring Boot)
```

## Contributing

Branching, commit and PR rules are in [CONTRIBUTING.md](CONTRIBUTING.md).
