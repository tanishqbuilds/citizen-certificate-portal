# Sanad — Citizen Certificate Request Portal

Citizens apply online for Birth, Income, Domicile and Marriage certificates, upload one document and track the
request with a tracking ID. Officers review it and approve (issuing a verifiable certificate) or reject it with a reason.
Built as a complete DevOps project: Git flow → Jenkins → Selenium quality gate → Docker → Ansible.

**Latest release: [v1.2.0](https://github.com/tanishqbuilds/citizen-certificate-portal/releases/tag/v1.2.0)** · [CHANGELOG](CHANGELOG.md)

## Stack

| Layer | Technology |
|---|---|
| App | Java 21, Spring Boot 4.1 (MVC, Security, Data JPA, Validation, Actuator), Thymeleaf, H2 + Flyway |
| UI | Hand-written CSS: grainy gradients, liquid-glass surfaces, light/dark theme |
| Build | Maven wrapper → executable WAR (runs standalone, on Tomcat 11, or in Docker) |
| Tests | JUnit 5 + MockMvc (65), Selenium WebDriver 4 journeys (8) |
| CI/CD | Jenkins (configuration as code), Jenkinsfile, Tomcat 11, Docker + private registry |
| Config mgmt | Ansible (roles, vault, rollback) |

## Quick start

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev     # http://localhost:8080
# officer sign-in (local only): officer / Officer@123 · demo national ID: 2345 6789 0124

./mvnw verify          # unit + integration tests
./mvnw verify -Pe2e    # + Selenium journeys in headless Chrome
```

## DevOps toolchain

```bash
scripts/ci-up.sh                       # Jenkins :8090, registry :5000, Tomcat :8888, Selenium Grid :4444
scripts/docker-lifecycle-demo.sh       # Docker build/run/stop/restart/rm walkthrough
scripts/ansible-node.sh --recreate     # clean Ubuntu 24.04 node
scripts/ansible.sh site.yml -e app_version=1.2.0
scripts/ansible.sh rollback.yml
```

Pipeline: `Build & test → WAR → image → staging container → Selenium gate → push → production container ‖ Tomcat`.

## Documentation

| # | Document | Phase |
|---|---|---|
| 01 | [Problem definition & MVP scope](docs/01-problem-definition.md) | 1 |
| 02 | [Agile plan, backlog, DoD, DevOps lifecycle](docs/02-agile-plan.md) | 2 |
| 03 | [SRS, architecture, data model, API](docs/03-srs-architecture.md) | 3 |
| 04 | [Git, GitHub, PRs, merge conflict, releases](docs/04-git-and-collaboration.md) | 4–6 |
| 05 | [Jenkins CI, Jenkinsfile, Tomcat](docs/05-jenkins-ci-and-tomcat.md) | 7–8 |
| 06 | [Selenium test plan](docs/06-selenium-test-plan.md) | 9 |
| 07 | [Continuous testing & quality gate](docs/07-continuous-testing-quality-gate.md) | 10 |
| 08 | [Docker & continuous deployment](docs/08-docker-and-continuous-deployment.md) | 11–12 |
| 09 | [Configuration management (Ansible)](docs/09-configuration-management.md) | 13–14 |
| 10 | [Final report: E2E run, troubleshooting, limitations, roadmap](docs/10-final-report.md) | 15 |

Evidence (logs and screenshots) for every phase is in [`docs/evidence/`](docs/evidence/).

## Repository layout

```
src/                Spring Boot application and tests (unit, MockMvc, Selenium e2e)
ci/                 Jenkins image, configuration as code, Job DSL, docker-compose stack
infra/tomcat/       Tomcat 11 deployment target
infra/ansible/      Inventory, roles, playbooks, vault, test node
scripts/            ci-up, deploy-tomcat, deploy-container, smoke-test, ansible helpers
Dockerfile          Layered, non-root runtime image
Jenkinsfile         Delivery pipeline
docs/               Documentation and evidence
```

Branching, commits and PRs: [CONTRIBUTING.md](CONTRIBUTING.md). Academic project; certificates issued here have no legal validity.
