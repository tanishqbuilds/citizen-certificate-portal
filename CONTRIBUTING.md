# Contributing

This repository follows a lightweight **Git Flow** model with protected long-lived branches,
short-lived topic branches and [Conventional Commits](https://www.conventionalcommits.org/).

## Branches

| Branch | Purpose | Created from | Merges into | Protection |
|---|---|---|---|---|
| `main` | Production-ready history. Every commit on `main` is a tagged release or a hotfix. | — | — | No direct pushes. PR + 1 review + green Jenkins build. |
| `develop` | Integration branch for the next release. | `main` | `release/*` → `main` | No direct pushes. PR + green Jenkins build. |
| `feature/CCR-<n>-<slug>` | New user-facing capability for backlog item `CCR-<n>`. | `develop` | `develop` | — |
| `fix/CCR-<n>-<slug>` | Non-urgent defect fix. | `develop` | `develop` | — |
| `ci/<slug>` | Pipeline, Docker, Ansible or tooling changes. | `develop` | `develop` | — |
| `docs/<slug>` | Documentation only. | `develop` | `develop` | — |
| `release/<major>.<minor>.<patch>` | Release hardening: version bump, changelog, last fixes. | `develop` | `main` **and** `develop` | — |
| `hotfix/<major>.<minor>.<patch>` | Urgent production fix. | `main` | `main` **and** `develop` | — |

### Naming rules

- Lowercase, words separated by `-`, no spaces or underscores: `feature/CCR-7-status-timeline`.
- The backlog key (`CCR-<n>`) is mandatory for `feature/` and `fix/` branches so that the branch,
  the commits and the pull request all trace back to a story in [`docs/agile/backlog.csv`](docs/agile/backlog.csv).
- Delete topic branches after they are merged.

## Commit messages

```
<type>(<optional scope>): <imperative summary, ≤ 72 chars>

<optional body: what and why, wrapped at 72 chars>

Refs: CCR-<n>
```

Allowed types: `feat`, `fix`, `docs`, `test`, `refactor`, `perf`, `build`, `ci`, `chore`, `style`, `revert`.

Examples:

```
feat(apply): validate national ID with the Verhoeff checksum
fix(review): require a reason before a request can be rejected
ci(jenkins): stop the pipeline when the Selenium quality gate fails
```

## Pull requests

1. Rebase or merge `develop` into your branch and make sure `./mvnw verify` passes.
2. Open a PR against `develop` (or `main` for `release/*` and `hotfix/*`). Fill in the template.
3. At least one approving review is required. Address every review comment with a commit or a reply.
4. Merge with **"Create a merge commit"** (`--no-ff`) so feature boundaries stay visible in `git log --graph`.

## Versioning and tags

[Semantic Versioning](https://semver.org/). Releases are annotated tags on `main`: `git tag -a v1.0.0 -m "…"`.
The Maven project version, the Docker image tag and the version shown in the portal footer are kept in sync.

## Local checks before pushing

```bash
./mvnw verify          # compile, unit + integration tests, package
./mvnw verify -Pe2e    # also boots the app and runs the Selenium journeys in headless Chrome
```
