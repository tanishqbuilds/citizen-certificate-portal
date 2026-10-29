# PPT Evidence Guide — DevOps Mini-Project: GetDocs

> [!IMPORTANT]
> This is a task-wise inventory of **every screenshot, log file, and key code file** in your repository that you can use as proof in your PPT. Since Jenkins/Docker/Ansible are not running live, these screenshots and logs **are your evidence**.

**Live Website:** https://getdocs.onrender.com

---

## Task 1 — Problem Definition, Real-World Need & MVP Scope
**Doc:** [`01-problem-definition.md`](file:///d:/CODE/citizen-certificate-portal/docs/01-problem-definition.md)

**No screenshots needed** — this is a documentation task. For PPT, summarize:
- Problem statement (manual certificate issuance)
- 3 personas: Citizen, Officer, DevOps engineer
- 9 pain points (P1–P9)
- 15-item frozen MVP scope (CCR-1 to CCR-15)
- SMART objectives table

---

## Task 2 — Agile Planning & DevOps Workflow
**Doc:** [`02-agile-plan.md`](file:///d:/CODE/citizen-certificate-portal/docs/02-agile-plan.md)

**No screenshots needed** — documentation task. For PPT:
- Scrum/Kanban hybrid workflow
- 15 user stories with Given-When-Then acceptance criteria
- 4-sprint roadmap table
- Definition of Ready / Definition of Done
- CI/CD workflow description (Mermaid diagram in the doc)

---

## Task 3 — SRS & System Architecture
**Doc:** [`03-srs-architecture.md`](file:///d:/CODE/citizen-certificate-portal/docs/03-srs-architecture.md)

**Key code to show:**
| File | What to highlight |
|---|---|
| [`pom.xml`](file:///d:/CODE/citizen-certificate-portal/pom.xml) | Spring Boot 4.1, Java 21, WAR packaging, all dependencies |
| [`application.yml`](file:///d:/CODE/citizen-certificate-portal/src/main/resources/application.yml) | H2 file DB, Flyway, server config |
| [`V1__baseline.sql`](file:///d:/CODE/citizen-certificate-portal/src/main/resources/db/migration/V1__baseline.sql) | Flyway migration creating tables |
| [`CertificateRequest.java`](file:///d:/CODE/citizen-certificate-portal/src/main/java/in/ac/muquestionpapers/ccrp/model/CertificateRequest.java) | JPA entity model |
| [`SecurityConfig.java`](file:///d:/CODE/citizen-certificate-portal/src/main/java/in/ac/muquestionpapers/ccrp/config/SecurityConfig.java) | Spring Security setup |

**For PPT:** Show the architecture diagram from the markdown (Mermaid ER diagram, sequence diagrams).

---

## Task 4 — Git, GitHub & Collaboration (Phases 4–6)
**Doc:** [`04-git-and-collaboration.md`](file:///d:/CODE/citizen-certificate-portal/docs/04-git-and-collaboration.md)

### Screenshots (14 total — Citizen + Officer UI proof):

#### Citizen Journey (5 screenshots):
| # | Screenshot | Shows |
|---|---|---|
| 1 | [`01-apply-form.png`](file:///d:/CODE/citizen-certificate-portal/docs/evidence/phase-05-feature-branch/01-apply-form.png) | Empty certificate application form |
| 2 | [`02-apply-validation-errors.png`](file:///d:/CODE/citizen-certificate-portal/docs/evidence/phase-05-feature-branch/02-apply-validation-errors.png) | Validation errors on submit |
| 3 | [`03-apply-filled.png`](file:///d:/CODE/citizen-certificate-portal/docs/evidence/phase-05-feature-branch/03-apply-filled.png) | Filled form with document upload |
| 4 | [`04-acknowledgement.png`](file:///d:/CODE/citizen-certificate-portal/docs/evidence/phase-05-feature-branch/04-acknowledgement.png) | Tracking ID generated after submit |
| 5 | [`05-track-result-dark.png`](file:///d:/CODE/citizen-certificate-portal/docs/evidence/phase-05-feature-branch/05-track-result-dark.png) | Status tracking result (dark mode) |

#### Officer Journey (9 screenshots):
| # | Screenshot | Shows |
|---|---|---|
| 6 | [`10-officer-login.png`](file:///d:/CODE/citizen-certificate-portal/docs/evidence/phase-06-mvp/10-officer-login.png) | Officer login page |
| 7 | [`11-officer-login-error.png`](file:///d:/CODE/citizen-certificate-portal/docs/evidence/phase-06-mvp/11-officer-login-error.png) | Login error handling |
| 8 | [`12-officer-queue.png`](file:///d:/CODE/citizen-certificate-portal/docs/evidence/phase-06-mvp/12-officer-queue.png) | Review queue with KPI cards |
| 9 | [`13-request-detail-submitted.png`](file:///d:/CODE/citizen-certificate-portal/docs/evidence/phase-06-mvp/13-request-detail-submitted.png) | Application detail view |
| 10 | [`14-reject-requires-reason.png`](file:///d:/CODE/citizen-certificate-portal/docs/evidence/phase-06-mvp/14-reject-requires-reason.png) | Rejection requires reason guardrail |
| 11 | [`15-request-approved.png`](file:///d:/CODE/citizen-certificate-portal/docs/evidence/phase-06-mvp/15-request-approved.png) | Approved with certificate generated |
| 12 | [`16-certificate.png`](file:///d:/CODE/citizen-certificate-portal/docs/evidence/phase-06-mvp/16-certificate.png) | Digital certificate with SHA-256 |
| 13 | [`17-citizen-approved.png`](file:///d:/CODE/citizen-certificate-portal/docs/evidence/phase-06-mvp/17-citizen-approved.png) | Citizen sees approval |
| 14 | [`18-citizen-rejected.png`](file:///d:/CODE/citizen-certificate-portal/docs/evidence/phase-06-mvp/18-citizen-rejected.png) | Citizen sees rejection + reason |

#### Git Evidence (text files):
| File | Shows |
|---|---|
| [`git-log-graph.txt`](file:///d:/CODE/citizen-certificate-portal/docs/evidence/phase-06-mvp/git-log-graph.txt) | Branch/merge graph with feature branches |
| [`merge-conflict.log`](file:///d:/CODE/citizen-certificate-portal/docs/evidence/phase-06-mvp/merge-conflict.log) | Merge conflict resolution proof |

#### Key code:
| File | What to highlight |
|---|---|
| [`ApplyController.java`](file:///d:/CODE/citizen-certificate-portal/src/main/java/in/ac/muquestionpapers/ccrp/controller/ApplyController.java) | Form handling |
| [`OfficerController.java`](file:///d:/CODE/citizen-certificate-portal/src/main/java/in/ac/muquestionpapers/ccrp/controller/OfficerController.java) | Queue, approve, reject |
| [`CertificateRequestService.java`](file:///d:/CODE/citizen-certificate-portal/src/main/java/in/ac/muquestionpapers/ccrp/service/CertificateRequestService.java) | Business logic |

**Git info:** 42 branches total, `v1.0.0` tag for MVP release.

---

## Task 5 — Jenkins CI & Tomcat Deployment (Phases 7–8)
**Doc:** [`05-jenkins-ci-and-tomcat.md`](file:///d:/CODE/citizen-certificate-portal/docs/05-jenkins-ci-and-tomcat.md)

### Screenshots (11 total):

#### Jenkins CI (Phase 7) — 4 key screenshots:
| # | Screenshot | Shows |
|---|---|---|
| 1 | [`01-jenkins-dashboard.png`](file:///d:/CODE/citizen-certificate-portal/docs/evidence/phase-07-jenkins-ci/01-jenkins-dashboard.png) | Jenkins CaC dashboard |
| 2 | [`02-ci-job.png`](file:///d:/CODE/citizen-certificate-portal/docs/evidence/phase-07-jenkins-ci/02-ci-job.png) | CI job configuration |
| 3 | [`03-build-2-status.png`](file:///d:/CODE/citizen-certificate-portal/docs/evidence/phase-07-jenkins-ci/03-build-2-status.png) | Green build with SCM tracking |
| 4 | [`05-test-results.png`](file:///d:/CODE/citizen-certificate-portal/docs/evidence/phase-07-jenkins-ci/05-test-results.png) | **65 tests passing** |

#### Extra Jenkins CI screenshots (bonus):
| # | Screenshot | Shows |
|---|---|---|
| 5 | [`04-build-2-console.png`](file:///d:/CODE/citizen-certificate-portal/docs/evidence/phase-07-jenkins-ci/04-build-2-console.png) | Console output |
| 6 | [`06-scm-polling-log.png`](file:///d:/CODE/citizen-certificate-portal/docs/evidence/phase-07-jenkins-ci/06-scm-polling-log.png) | SCM polling |
| 7 | [`07-build-1-failed-test.png`](file:///d:/CODE/citizen-certificate-portal/docs/evidence/phase-07-jenkins-ci/07-build-1-failed-test.png) | Failed build (shows red→green story) |

#### Pipeline & Tomcat (Phase 8) — 7 key screenshots:
| # | Screenshot | Shows |
|---|---|---|
| 8 | [`01-pipeline-stage-view.png`](file:///d:/CODE/citizen-certificate-portal/docs/evidence/phase-08-pipeline-tomcat/01-pipeline-stage-view.png) | **Multi-stage pipeline view** |
| 9 | [`02-build-3-summary.png`](file:///d:/CODE/citizen-certificate-portal/docs/evidence/phase-08-pipeline-tomcat/02-build-3-summary.png) | Build summary with artifacts |
| 10 | [`06-coverage-report.png`](file:///d:/CODE/citizen-certificate-portal/docs/evidence/phase-08-pipeline-tomcat/06-coverage-report.png) | **JaCoCo 90.5% coverage** |
| 11 | [`08-tomcat-staging-home.png`](file:///d:/CODE/citizen-certificate-portal/docs/evidence/phase-08-pipeline-tomcat/08-tomcat-staging-home.png) | **App on Tomcat staging** |
| 12 | [`09-tomcat-production-apply.png`](file:///d:/CODE/citizen-certificate-portal/docs/evidence/phase-08-pipeline-tomcat/09-tomcat-production-apply.png) | **App on Tomcat production** |

#### Extra Pipeline screenshots (bonus):
| Screenshot | Shows |
|---|---|
| [`02b-pipeline-graph-build-3.png`](file:///d:/CODE/citizen-certificate-portal/docs/evidence/phase-08-pipeline-tomcat/02b-pipeline-graph-build-3.png) | Pipeline graph |
| [`03-build-with-parameters.png`](file:///d:/CODE/citizen-certificate-portal/docs/evidence/phase-08-pipeline-tomcat/03-build-with-parameters.png) | Parameterized build |
| [`07-build-1-failed-graph.png`](file:///d:/CODE/citizen-certificate-portal/docs/evidence/phase-08-pipeline-tomcat/07-build-1-failed-graph.png) | Failed pipeline |

#### Key code:
| File | What to highlight |
|---|---|
| [`Jenkinsfile`](file:///d:/CODE/citizen-certificate-portal/Jenkinsfile) | 9-stage declarative pipeline (147 lines) |
| [`ci/jenkins.yaml`](file:///d:/CODE/citizen-certificate-portal/ci/jenkins.yaml) | Jenkins Configuration-as-Code |
| [`ci/docker-compose.yml`](file:///d:/CODE/citizen-certificate-portal/ci/docker-compose.yml) | Full CI stack (Jenkins + Tomcat + Registry + Selenium Grid) |
| [`ci/Dockerfile.jenkins`](file:///d:/CODE/citizen-certificate-portal/ci/Dockerfile.jenkins) | Custom Jenkins image |

#### Console logs (for showing in PPT as text):
| File | What it proves |
|---|---|
| [`build-2-success-console.log`](file:///d:/CODE/citizen-certificate-portal/docs/evidence/phase-07-jenkins-ci/build-2-success-console.log) | Maven build success, 65 tests |
| [`build-3-console.log`](file:///d:/CODE/citizen-certificate-portal/docs/evidence/phase-08-pipeline-tomcat/build-3-console.log) | Pipeline execution log |

---

## Task 6 — Selenium Testing (Phase 9)
**Doc:** [`06-selenium-test-plan.md`](file:///d:/CODE/citizen-certificate-portal/docs/06-selenium-test-plan.md)

### Screenshots (6 total):
| # | Screenshot | Shows |
|---|---|---|
| 1 | [`local-failsafe-html-report.png`](file:///d:/CODE/citizen-certificate-portal/docs/evidence/phase-09-selenium/local-failsafe-html-report.png) | **Failsafe report: all tests green** |
| 2 | [`CitizenApplicationJourney_citizenAppliesAndSeesSubmittedStatus_20261011-210842.png`](file:///d:/CODE/citizen-certificate-portal/docs/evidence/phase-09-selenium/passed-journeys/CitizenApplicationJourney_citizenAppliesAndSeesSubmittedStatus_20261011-210842.png) | CJ-1: Citizen applies |
| 3 | [`OfficerApprovalJourney_approvalIssuesACertificate_20261011-210920.png`](file:///d:/CODE/citizen-certificate-portal/docs/evidence/phase-09-selenium/passed-journeys/OfficerApprovalJourney_approvalIssuesACertificate_20261011-210920.png) | CJ-4: Officer approves |
| 4 | [`OfficerRejectionJourney_rejectionReasonReachesTheCitizen_20261011-210939.png`](file:///d:/CODE/citizen-certificate-portal/docs/evidence/phase-09-selenium/passed-journeys/OfficerRejectionJourney_rejectionReasonReachesTheCitizen_20261011-210939.png) | CJ-5: Officer rejects |
| 5 | [`StatusTrackingJourney_trackingRequiresBothFields_20261011-211003.png`](file:///d:/CODE/citizen-certificate-portal/docs/evidence/phase-09-selenium/passed-journeys/StatusTrackingJourney_trackingRequiresBothFields_20261011-211003.png) | CJ-6: Tracking validation |

#### Key code:
| File | What to highlight |
|---|---|
| [`CitizenApplicationJourney.java`](file:///d:/CODE/citizen-certificate-portal/src/test/java/in/ac/muquestionpapers/ccrp/e2e/CitizenApplicationJourney.java) | E2E test using Page Object Model |
| [`OfficerApprovalJourney.java`](file:///d:/CODE/citizen-certificate-portal/src/test/java/in/ac/muquestionpapers/ccrp/e2e/OfficerApprovalJourney.java) | Officer approval E2E |
| [`ApplyPage.java`](file:///d:/CODE/citizen-certificate-portal/src/test/java/in/ac/muquestionpapers/ccrp/e2e/pages/ApplyPage.java) | Page Object Model example |
| [`docker-compose.yml`](file:///d:/CODE/citizen-certificate-portal/docker-compose.yml) | Selenium Grid (Hub + Chrome + Firefox) |

#### Console log:
| File | What it proves |
|---|---|
| [`local-run.log`](file:///d:/CODE/citizen-certificate-portal/docs/evidence/phase-09-selenium/local-run.log) | 6 tests run, 0 failures |

---

## Task 7 — Continuous Testing & Quality Gate (Phase 10)
**Doc:** [`07-continuous-testing-quality-gate.md`](file:///d:/CODE/citizen-certificate-portal/docs/07-continuous-testing-quality-gate.md)

### Screenshots (5 key + 2 bonus):
| # | Screenshot | Shows |
|---|---|---|
| 1 | [`01-build-7-failed-stages.png`](file:///d:/CODE/citizen-certificate-portal/docs/evidence/phase-10-quality-gate/01-build-7-failed-stages.png) | **Build #7 HALTED — quality gate blocked prod deploy** |
| 2 | [`02-build-7-test-report.png`](file:///d:/CODE/citizen-certificate-portal/docs/evidence/phase-10-quality-gate/02-build-7-test-report.png) | Test failure report (injected defect found) |
| 3 | [`04-build-7-artifacts.png`](file:///d:/CODE/citizen-certificate-portal/docs/evidence/phase-10-quality-gate/04-build-7-artifacts.png) | Failure screenshots automatically archived |
| 4 | [`05-build-8-rerun-stages.png`](file:///d:/CODE/citizen-certificate-portal/docs/evidence/phase-10-quality-gate/05-build-8-rerun-stages.png) | **Build #8 GREEN — fix deployed to production** |
| 5 | [`06-pipeline-history-and-trend.png`](file:///d:/CODE/citizen-certificate-portal/docs/evidence/phase-10-quality-gate/06-pipeline-history-and-trend.png) | **Build history & test trend graph** |

#### Bonus:
| Screenshot | Shows |
|---|---|
| [`03-build-7-selenium-html-report.png`](file:///d:/CODE/citizen-certificate-portal/docs/evidence/phase-10-quality-gate/03-build-7-selenium-html-report.png) | Selenium HTML report (failed) |
| [`07-build-8-selenium-html-report.png`](file:///d:/CODE/citizen-certificate-portal/docs/evidence/phase-10-quality-gate/07-build-8-selenium-html-report.png) | Selenium HTML report (passed) |

#### Failure screenshots (automatically captured by Jenkins):
| File | Shows |
|---|---|
| [`jenkins-archived-failure-screenshots/CitizenA...png`](file:///d:/CODE/citizen-certificate-portal/docs/evidence/phase-10-quality-gate/jenkins-archived-failure-screenshots) | Browser state at failure time |
| [`jenkins-archived-failure-screenshots/OfficerA...png`](file:///d:/CODE/citizen-certificate-portal/docs/evidence/phase-10-quality-gate/jenkins-archived-failure-screenshots) | Browser state at failure time |

#### Console logs:
| File | What it proves |
|---|---|
| [`build-7-quality-gate-failed.log`](file:///d:/CODE/citizen-certificate-portal/docs/evidence/phase-10-quality-gate/build-7-quality-gate-failed.log) | Gate caught the defect |
| [`build-8-rerun-success.log`](file:///d:/CODE/citizen-certificate-portal/docs/evidence/phase-10-quality-gate/build-8-rerun-success.log) | Fix passed and deployed |
| [`production-before-defect.txt`](file:///d:/CODE/citizen-certificate-portal/docs/evidence/phase-10-quality-gate/production-before-defect.txt) | Production unchanged after failed build |
| [`production-after-fix-and-github-statuses.txt`](file:///d:/CODE/citizen-certificate-portal/docs/evidence/phase-10-quality-gate/production-after-fix-and-github-statuses.txt) | Production updated after fix |

---

## Task 8 — Docker & Continuous Deployment (Phases 11–12)
**Doc:** [`08-docker-and-continuous-deployment.md`](file:///d:/CODE/citizen-certificate-portal/docs/08-docker-and-continuous-deployment.md)

### Screenshots (3):
| # | Screenshot | Shows |
|---|---|---|
| 1 | [`01-build-13-stages.png`](file:///d:/CODE/citizen-certificate-portal/docs/evidence/phase-12-container-cd/01-build-13-stages.png) | **Docker build + registry push in Jenkins** |
| 2 | [`02-pipeline-stage-view.png`](file:///d:/CODE/citizen-certificate-portal/docs/evidence/phase-12-container-cd/02-pipeline-stage-view.png) | **End-to-end container pipeline** |
| 3 | [`03-production-container-8085.png`](file:///d:/CODE/citizen-certificate-portal/docs/evidence/phase-12-container-cd/03-production-container-8085.png) | **Live container on port 8085** |

#### Key code:
| File | What to highlight |
|---|---|
| [`Dockerfile`](file:///d:/CODE/citizen-certificate-portal/Dockerfile) | Multi-stage build, layer extraction, non-root user, health check (34 lines) |
| [`scripts/deploy-container.sh`](file:///d:/CODE/citizen-certificate-portal/scripts/deploy-container.sh) | Container deploy script |
| [`scripts/docker-lifecycle-demo.sh`](file:///d:/CODE/citizen-certificate-portal/scripts/docker-lifecycle-demo.sh) | Docker lifecycle commands |

#### Console logs:
| File | What it proves |
|---|---|
| [`docker-lifecycle.log`](file:///d:/CODE/citizen-certificate-portal/docs/evidence/phase-11-docker/docker-lifecycle.log) | Docker build, run, inspect, logs, stop, rm |
| [`build-12-commit-to-container.log`](file:///d:/CODE/citizen-certificate-portal/docs/evidence/phase-12-container-cd/build-12-commit-to-container.log) | Commit-to-container pipeline |
| [`build-13-replaces-running-container.log`](file:///d:/CODE/citizen-certificate-portal/docs/evidence/phase-12-container-cd/build-13-replaces-running-container.log) | Rolling container replacement |
| [`registry-and-production-container.txt`](file:///d:/CODE/citizen-certificate-portal/docs/evidence/phase-12-container-cd/registry-and-production-container.txt) | Registry catalog + running container |

---

## Task 9 — Ansible Configuration Management (Phases 13–14)
**Doc:** [`09-configuration-management.md`](file:///d:/CODE/citizen-certificate-portal/docs/09-configuration-management.md)

### No screenshots, but 12 console/text logs that prove everything:

#### Phase 13 — First Ansible Run:
| File | What it proves |
|---|---|
| [`site-first-run.log`](file:///d:/CODE/citizen-certificate-portal/docs/evidence/phase-13-ansible/site-first-run.log) | Full provisioning: install JRE, create user, deploy JAR, systemd, health check |

#### Phase 14 — Reliability Proof (11 files):
| # | File | What it proves |
|---|---|---|
| 1 | [`01-clean-node.log`](file:///d:/CODE/citizen-certificate-portal/docs/evidence/phase-14-reliability/01-clean-node.log) | Clean node before provisioning |
| 2 | [`02-provision-1.0.0.log`](file:///d:/CODE/citizen-certificate-portal/docs/evidence/phase-14-reliability/02-provision-1.0.0.log) | First provision (many `changed`) |
| 3 | [`03-idempotency-rerun-1.0.0.log`](file:///d:/CODE/citizen-certificate-portal/docs/evidence/phase-14-reliability/03-idempotency-rerun-1.0.0.log) | **Idempotency: `changed=0`** |
| 4 | [`04-upgrade-to-1.1.0.log`](file:///d:/CODE/citizen-certificate-portal/docs/evidence/phase-14-reliability/04-upgrade-to-1.1.0.log) | Version upgrade |
| 5 | [`05-idempotency-rerun-1.1.0.log`](file:///d:/CODE/citizen-certificate-portal/docs/evidence/phase-14-reliability/05-idempotency-rerun-1.1.0.log) | Idempotency after upgrade |
| 6 | [`06-healthcheck.log`](file:///d:/CODE/citizen-certificate-portal/docs/evidence/phase-14-reliability/06-healthcheck.log) | Health check playbook |
| 7 | [`07-data-before-rollback.txt`](file:///d:/CODE/citizen-certificate-portal/docs/evidence/phase-14-reliability/07-data-before-rollback.txt) | State before rollback |
| 8 | [`08-rollback.log`](file:///d:/CODE/citizen-certificate-portal/docs/evidence/phase-14-reliability/08-rollback.log) | **1-command rollback** |
| 9 | [`09-after-rollback.txt`](file:///d:/CODE/citizen-certificate-portal/docs/evidence/phase-14-reliability/09-after-rollback.txt) | Back to v1.0.0 |
| 10 | [`10-recovery-after-crash.log`](file:///d:/CODE/citizen-certificate-portal/docs/evidence/phase-14-reliability/10-recovery-after-crash.log) | **JVM crash → auto-recovery in ~17s** |
| 11 | [`12-container-auto-rollback.log`](file:///d:/CODE/citizen-certificate-portal/docs/evidence/phase-14-reliability/12-container-auto-rollback.log) | Container auto-rollback on unhealthy image |

#### Key code:
| File | What to highlight |
|---|---|
| [`ansible/site.yml`](file:///d:/CODE/citizen-certificate-portal/ansible/site.yml) | Main playbook |
| [`ansible/rollback.yml`](file:///d:/CODE/citizen-certificate-portal/ansible/rollback.yml) | Rollback playbook |
| [`ansible/healthcheck.yml`](file:///d:/CODE/citizen-certificate-portal/ansible/healthcheck.yml) | Health check playbook |
| [`ansible/roles/ccrp/tasks/main.yml`](file:///d:/CODE/citizen-certificate-portal/ansible/roles/ccrp/tasks/main.yml) | All provisioning tasks |
| [`ansible/roles/ccrp/templates/ccrp.service.j2`](file:///d:/CODE/citizen-certificate-portal/ansible/roles/ccrp/templates/ccrp.service.j2) | Systemd service template |
| [`ansible/inventory.ini`](file:///d:/CODE/citizen-certificate-portal/ansible/inventory.ini) | Inventory |

---

## Task 10 — Final Report & Roadmap (Phase 15)
**Doc:** [`10-final-report.md`](file:///d:/CODE/citizen-certificate-portal/docs/10-final-report.md)

#### Console logs:
| File | What it proves |
|---|---|
| [`ansible-deploy-1.2.0.log`](file:///d:/CODE/citizen-certificate-portal/docs/evidence/phase-15-final/ansible-deploy-1.2.0.log) | Final release deployment |
| [`final-environments.txt`](file:///d:/CODE/citizen-certificate-portal/docs/evidence/phase-15-final/final-environments.txt) | **All 3 environments running: Docker :8085, Tomcat :8888, Ansible :8095** |

---

## Quick Reference: Total Evidence Count

| Task | Screenshots | Logs/Text Files | Key Code Files |
|---|---|---|---|
| Task 1 | 0 | 0 | 0 |
| Task 2 | 0 | 0 | 0 |
| Task 3 | 0 | 0 | 5 |
| Task 4 | **14** | 2 | 3 |
| Task 5 | **11 + 7 bonus** | 2 | 4 |
| Task 6 | **6** | 1 | 4 |
| Task 7 | **5 + 2 bonus** | 4 | 0 |
| Task 8 | **3** | 4 | 3 |
| Task 9 | 0 | **12** | 6 |
| Task 10 | 0 | 2 | 0 |
| **TOTAL** | **39+ screenshots** | **27 logs** | **25 code files** |

---

## Git Info for PPT
- **42 branches** (feature/*, release/*, docs/*)
- **3 tags:** `v1.0.0`, `v1.1.0`, `v1.2.0`
- **Live:** https://getdocs.onrender.com
