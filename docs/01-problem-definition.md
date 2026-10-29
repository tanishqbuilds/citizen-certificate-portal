# Problem Definition — GetDocs Citizen Certificate Request Portal (CCRP)

| Field | Value |
|---|---|
| Document ID | CCRP-DOC-01 |
| Title | Problem Definition and MVP Scope |
| Version | 1.0 |
| Status | Approved (scope frozen) |
| Owner | Tanishq |
| Related phase | Phase 1 — Problem definition |
| Last updated | 2026-10-11 |

## Table of contents

1. [Context and real-world need](#1-context-and-real-world-need)
2. [Problem statement](#2-problem-statement)
3. [Target users and personas](#3-target-users-and-personas)
4. [Pain points](#4-pain-points)
5. [Stakeholders](#5-stakeholders)
6. [Objectives (SMART)](#6-objectives-smart)
7. [Constraints](#7-constraints)
8. [Assumptions](#8-assumptions)
9. [Risks](#9-risks)
10. [Success criteria and KPIs](#10-success-criteria-and-kpis)
11. [MVP scope freeze](#11-mvp-scope-freeze)
12. [Out of scope](#12-out-of-scope)
13. [Glossary](#13-glossary)
14. [Sign-off](#14-sign-off)

---

## 1. Context and real-world need

In many Indian local government offices (tehsil, municipal ward, gram panchayat and similar offices), certificates such as **birth, income, domicile and marriage** certificates are still requested and issued through a largely paper- and counter-based process:

- The citizen collects a printed form, fills it by hand and attaches photocopies of supporting documents.
- The form is submitted at a counter during office hours, often after queuing.
- Applications are recorded in manual registers; the physical file moves between desks.
- The citizen has no reliable way to know the status of the application other than returning to the office or phoning.
- Supporting documents can be misplaced, and incomplete applications are often discovered only on a later visit.
- The opacity of the process creates room for informal intermediaries ("agents") who charge for navigating it.
- Issued paper certificates are difficult for third parties (banks, schools, employers) to verify.

Several states operate e-district and service-delivery portals that address parts of this problem. This project does not claim to replace them; it builds a **focused, self-contained reference implementation** of the core request–review–issue workflow, engineered with a complete DevOps toolchain, as the vehicle for the course.

> Note: All observations in this section are qualitative. Where numbers appear elsewhere in this document they are **targets or assumptions**, not measured statistics.

## 2. Problem statement

> Citizens who need a government certificate must make repeated in-person visits, cannot see where their application stands, and risk lost or incomplete paperwork; officers process applications from manual registers with no consolidated queue or audit trail. **GetDocs** provides a web portal where a citizen applies online with one validated supporting document, receives a tracking ID and follows the status, while an officer reviews a filtered queue, approves or rejects with a recorded reason, and the system issues a digitally verifiable certificate — with every status change audited and the whole application delivered through an automated, tested, containerised and reproducibly provisioned pipeline.

## 3. Target users and personas

### Persona 1 — Citizen (Applicant)

| Attribute | Detail |
|---|---|
| Name (illustrative) | Priya Sharma, 29, private-sector employee, tier-2 city |
| Digital literacy | Comfortable with smartphone and web forms; not technical |
| Goals | Apply for an income certificate without taking leave; know exactly what to upload; see progress; obtain a certificate she can share |
| Frustrations | Multiple visits for one certificate; told about missing documents only after waiting; no status visibility; being approached by middlemen |
| Success looks like | Submit once in a few minutes, get a tracking ID, check status online, print a verifiable certificate |

### Persona 2 — Reviewer / Officer

| Attribute | Detail |
|---|---|
| Name (illustrative) | R. Venkatesh, 46, revenue department officer |
| Digital literacy | Uses office software daily; prefers simple, predictable screens |
| Goals | See all pending applications in one queue; filter by type/status; approve or reject quickly with complete information; be protected by a clear record of decisions |
| Frustrations | Incomplete applications; physical files moving between desks; no single view of workload; disputes about who decided what and when |
| Success looks like | A queue with KPIs, one-click review actions, mandatory rejection reasons, automatic certificate numbering and an audit trail |

### Persona 3 — Administrator / DevOps engineer

| Attribute | Detail |
|---|---|
| Name (illustrative) | Tanishq, platform owner (the project developer) |
| Goals | Every commit built and tested automatically; a broken user journey blocks release; versioned, reproducible deployments; servers provisioned from code; fast rollback |
| Frustrations | "Works on my machine"; manual, undocumented server setup; deployments that cannot be reversed; regressions discovered by users |
| Success looks like | Jenkins pipeline green from commit to running container, Selenium quality gate, Ansible-provisioned node, health check and rollback within minutes |

## 4. Pain points

| # | Pain point | Who | Current impact (qualitative) | How GetDocs addresses it | Task |
|---|---|---|---|---|---|
| P1 | Repeated in-person visits | Citizen | Lost time and wages, travel cost | Online submission from any device | CCR-3 |
| P2 | Errors found late (invalid ID, missing fields) | Citizen, Officer | Rework, rejected applications | Strict server-side validation at submission | CCR-4 |
| P3 | Lost or unreadable supporting documents | Citizen, Officer | Re-submission, delays | Digital upload with type/size/magic-byte checks and SHA-256 | CCR-5 |
| P4 | No proof of submission | Citizen | Disputes, no reference | Tracking ID and acknowledgement | CCR-6 |
| P5 | No status visibility | Citizen | Phone calls, visits, reliance on middlemen | Status page with timeline | CCR-7 |
| P6 | No consolidated workload view | Officer | Missed or aged applications | Review queue with filters and KPIs | CCR-9 |
| P7 | Inconsistent decisions, unexplained rejections | Citizen, Officer | Grievances, re-applications | State machine and mandatory rejection reason | CCR-10 |
| P8 | Certificates hard to verify | Third parties | Fraud risk, manual verification | Verification page with SHA-256 hash | CCR-11 |
| P9 | No accountability record | Department, Auditor | Disputes about decisions | Immutable audit trail of status changes | CCR-12 |
| P10 | Manual, error-prone releases | DevOps | Downtime, regressions | Jenkins CI/CD, Selenium gate, Docker, Ansible, rollback | CCR-14, CCR-15 |

## 5. Stakeholders

| Stakeholder | Role | Interest | Influence | Involvement |
|---|---|---|---|---|
| Citizens / applicants | Primary end users | Fast, transparent, low-effort certificate issuance | Medium | Usability feedback, acceptance testing of citizen journey |
| Reviewers / officers | Primary end users | Efficient queue, clear decisions, protection via audit trail | High | Workflow validation, acceptance of officer journey |
| Department head (issuing authority) | Business owner (hypothetical) | Throughput, accountability, compliance | High | Approves workflow rules and certificate format |
| Third-party verifiers (banks, schools) | Secondary users | Quick authenticity check | Low | Use the verification page |
| Project owner — Tanishq | Product Owner, Scrum Master, Developer, DevOps | Deliver MVP and complete DevOps lifecycle | High | Full-time, all ceremonies |
| Faculty guide | Academic supervisor | Learning outcomes, phase deliverables, evaluation | High | Phase reviews, sign-off, final evaluation |
| Data protection / IT security function | Compliance (hypothetical) | Lawful processing of personal data (DPDP Act 2023) | Medium | Review of data handling and access control |

## 6. Objectives (SMART)

| ID | Objective | Measure | Target | Deadline |
|---|---|---|---|---|
| O1 | Deliver the citizen journey (apply, upload, tracking ID, status) | Stories CCR-3 to CCR-7 Done | 5/5 | End of Sprint 1 (2026-09-18) |
| O2 | Deliver the officer journey and release v1.0.0 | Stories CCR-8 to CCR-13 Done; Git tag `v1.0.0` | 6/6 + tag | End of Sprint 2 (2026-09-25) |
| O3 | Enforce quality with an automated E2E gate | Selenium suite covering all critical journeys runs in Jenkins and blocks on failure | 100% critical journeys covered | End of Sprint 3 (2026-10-02) |
| O4 | Automate build, containerisation and deployment | Commit triggers pipeline producing a versioned image in `localhost:5000` and a deployed container | Fully automated, no manual steps | End of Sprint 4 (2026-10-09) |
| O5 | Provision infrastructure as code | Ansible provisions a clean Ubuntu 24.04 node; second run reports `changed=0`; rollback restores previous release | Idempotent, rollback < 2 min | End of Sprint 4 (2026-10-09) |

## 7. Constraints

| Type | Constraint |
|---|---|
| Technical | Java 21, Spring Boot 4.1, Thymeleaf server-rendered UI, Spring Data JPA, Spring Security, Flyway, H2 file DB (PostgreSQL mode), Maven wrapper; packaged as executable WAR deployable standalone and on Apache Tomcat 11. |
| Technical | Toolchain fixed by the course: Git/GitHub, Jenkins, Selenium WebDriver 4 + JUnit 5, Docker, Ansible. All tooling must run on a single developer workstation (local registry at `localhost:5000`). |
| Time | Five 1-week sprints, 2026-09-07 to 2026-10-09, mapped to 15 course phases. |
| Budget | Zero spend: open-source software only, no paid cloud services or SaaS. |
| Regulatory / data privacy | Awareness of the Digital Personal Data Protection Act, 2023 (DPDP Act): collect only data needed for the certificate (purpose limitation, data minimisation), restrict access to authenticated officers, keep an audit trail. National ID values are synthetic in all test and demo data; no real Aadhaar numbers are used. The MVP is not certified for production use with real personal data. |
| Security | Officer pages require authentication; CSRF protection on forms; uploaded files validated by content (magic bytes), not extension; no secrets committed to Git. |
| Team | Solo developer performing Product Owner, Scrum Master, Developer and DevOps roles; limited peer review capacity. |

## 8. Assumptions

| ID | Assumption |
|---|---|
| A1 | A single issuing office with a single approval level is representative enough for the MVP. |
| A2 | One supporting document (PDF/PNG/JPEG, up to 2 MB) per application is sufficient. |
| A3 | Citizens do not need accounts; the tracking ID (plus registered details) is enough to look up status. |
| A4 | Officer accounts are pre-seeded; self-registration of officers is not required. |
| A5 | An H2 file database is acceptable for the MVP; the PostgreSQL-compatible mode keeps a future migration straightforward. |
| A6 | Verhoeff checksum validation is an adequate proxy for national ID format validity; no external identity verification is performed. |
| A7 | The developer workstation has enough resources (approx. 16 GB RAM) to run Jenkins, Selenium Grid, Docker registry and target containers concurrently. |
| A8 | Target timings in Section 10 are measured on that workstation and are indicative, not production SLAs. |

## 9. Risks

| ID | Risk | Likelihood | Impact | Mitigation |
|---|---|---|---|---|
| R1 | Spring Boot 4.1 / Tomcat 11 (Jakarta EE 11) incompatibilities with libraries or tutorials | Medium | High | Pin versions in the Maven BOM; spike in Sprint 0; prefer first-party Spring starters |
| R2 | Flaky Selenium tests block the pipeline | High | Medium | Page Object Model, explicit waits, stable `data-testid` selectors, screenshots on failure, Grid with pinned browser versions |
| R3 | Workstation resource exhaustion running the full toolchain | Medium | Medium | Resource limits on containers, stop idle services, headless browsers |
| R4 | Scope creep (payments, notifications, e-KYC) | Medium | High | Scope frozen in Section 11; changes require a new backlog item and explicit re-planning |
| R5 | Solo developer — single point of failure and limited review | High | Medium | Small PRs, PR template checklist, automated gates (tests, Selenium, build), documentation as part of DoD |
| R6 | Mishandling of personal data | Low | High | Synthetic data only, minimal fields, access control, audit trail, no real IDs in repo |
| R7 | Malicious file uploads | Medium | High | Allow-list by magic bytes, size limit, files stored outside web root, never executed or served inline as HTML |
| R8 | Failed deployment leaves environment broken | Medium | High | Health check after deploy, versioned images and releases, Ansible rollback playbook |
| R9 | Jenkins configuration drift or loss | Medium | Medium | Dockerised Jenkins with Configuration-as-Code and Jenkinsfile in the repository |

## 10. Success criteria and KPIs

All values below are **targets** for the MVP, measured on the reference workstation.

| Area | KPI | Target | How measured |
|---|---|---|---|
| Citizen experience | Time to complete a submission | < 3 minutes for a prepared applicant | Timed walkthrough; Selenium journey duration as proxy |
| Data quality | Server-side validation coverage of documented rules | 100% of rules in CCR-4 / CCR-5 | Unit and MockMvc tests mapped to each rule |
| Correctness | Invalid state transitions accepted | 0 | State-machine unit tests (CCR-10) |
| Accountability | Status changes with an audit entry | 100% | Integration test asserting audit rows per transition |
| Quality gate | Selenium E2E suite as release gate | Green on every release; red blocks image push | Jenkins stage result |
| Build health | Unit + integration tests passing on `main` | 100% | Jenkins test reports |
| Delivery speed | Commit to running container | < 15 minutes | Jenkins pipeline duration |
| Recovery | Rollback to previous release | < 2 minutes | Timed Ansible rollback run |
| Reproducibility | Ansible re-run on provisioned node | `changed=0` | Playbook recap |
| Availability check | Health endpoint after deploy | `UP` within 60 seconds | Actuator `/actuator/health` probe |
| Security | Critical vulnerabilities at build | 0 known critical | Dependency/image scan in pipeline |

## 11. MVP scope freeze

The following 15 tasks constitute the frozen MVP scope. Any addition requires a new backlog item and explicit re-planning by the Product Owner.

| Key | Title | Description | Phase mapping |
|---|---|---|---|
| CCR-1 | Repository & project skeleton | Maven wrapper, Spring Boot 4.1 / Java 21 project, WAR packaging, Flyway baseline, H2 file DB, README, `.gitignore`, PR template | 3, 4 |
| CCR-2 | Design system & UI shell (grainy gradients, liquid glass, theming) | Layout, typography, grainy gradient backgrounds, liquid-glass components, light/dark theme toggle | 3, 4 |
| CCR-3 | Certificate request submission form | Form for BIRTH, INCOME, DOMICILE, MARRIAGE with conditional fields (annual income, spouse name) | 5 |
| CCR-4 | Applicant data validation | Bean Validation and custom validators: name, DOB/age, email, Indian mobile, 12-digit ID with Verhoeff, PIN, purpose, conditional fields | 5 |
| CCR-5 | Supporting document upload & validation | One required file, PDF/PNG/JPEG by magic bytes, max 2 MB, SHA-256 recorded | 5 |
| CCR-6 | Tracking ID generation & acknowledgement | Unique `CCR-2026-XXXXXX` ID, acknowledgement page | 5 |
| CCR-7 | Status tracking with timeline | Lookup by tracking ID; timeline of SUBMITTED → UNDER_REVIEW → APPROVED/REJECTED | 5 |
| CCR-8 | Officer authentication | Spring Security form login, seeded officer accounts, protected `/officer/**` routes | 6 |
| CCR-9 | Officer review queue with filters & KPIs | Queue with filter by type/status/search, KPI cards per status | 6 |
| CCR-10 | Review actions — start review / approve / reject (state machine) | Enforced transitions; rejection requires reason; approval issues certificate number | 6 |
| CCR-11 | Certificate issuance & verification view | `SND-<TYPE-CODE>-2026-NNNNNN` number, printable certificate page with SHA-256 verification hash | 6 |
| CCR-12 | Audit trail of status changes | Append-only record of every transition with actor, timestamp, from/to status, note | 6 |
| CCR-13 | Health, info & JSON status API | Actuator health/info; JSON endpoint for application status | 6, 14 |
| CCR-14 | Selenium E2E suite for critical journeys | Selenium 4 + JUnit 5, Page Object Model, screenshots on failure, failsafe, Grid in CI, quality gate | 9, 10 |
| CCR-15 | Containerised CI/CD & provisioning (Jenkins → Docker → Ansible) | Jenkins CaC + Jenkinsfile, Tomcat deploy, multi-stage Docker image, registry push, Ansible provisioning, health check, rollback | 7, 8, 11, 12, 13, 14, 15 |

Phases 1, 2 and 15 (problem definition, Agile planning, final release and project demonstration) are covered by documentation deliverables rather than individual stories.

## 12. Out of scope

The following are explicitly excluded from the MVP:

- Payments and fee collection
- SMS and email notifications
- DigiLocker or Aadhaar e-KYC integration
- Multi-language (vernacular) user interface
- Multi-level approval hierarchy
- PostgreSQL high-availability deployment
- Kubernetes or other container orchestration
- OCR or automated content analysis of uploaded documents

## 13. Glossary

| Term | Meaning |
|---|---|
| CCRP | Citizen Certificate Request Portal (product name: GetDocs) |
| Tracking ID | Applicant-facing reference, format `CCR-2026-XXXXXX` |
| Certificate number | Issued on approval, format `SND-<TYPE-CODE>-2026-NNNNNN` |
| Verhoeff | Checksum algorithm used by 12-digit Aadhaar-style identifiers |
| DPDP Act 2023 | India's Digital Personal Data Protection Act, 2023 |
| Quality gate | Pipeline stage whose failure stops the release |

## 14. Sign-off

| Name | Role | Decision | Date |
|---|---|---|---|
| Tanishq | Project owner | Approved | 2026-09-07 |
| ______ | Faculty guide | Approved | 2026-09-07 |
