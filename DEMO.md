# Sanad — Demo & Run Guide

Everything you need to run, demo, and understand the full DevOps stack.

---

## 🔑 Login Credentials

| Service | URL | Username | Password |
|---|---|---|---|
| **Jenkins** | http://localhost:8090 | `admin` | `WQHHc0wQLJaw4QxyKiaW` |
| **App — Officer** | http://localhost:8085/officer | `officer` | `Officer@fCfWDT0k` |
| **Tomcat Manager** | http://localhost:8888/manager/html | `deployer` | `nM4NdqR0IMsBkHArlaE9` |
| **Selenium (noVNC)** | http://localhost:7900 | _(no user)_ | `secret` |
| **Docker Registry** | http://localhost:5000 | _(no auth)_ | _(no auth)_ |

> Demo national ID for officer review: `2345 6789 0124`  
> Citizen pages (`/apply`, `/track`) require **no login**.

---

## 🚀 Step 1 — Start Everything (First Time or After Reboot)

Run this one command from the project root. It generates secrets, builds the Jenkins image, and spins up the entire CI stack:

```bash
scripts/ci-up.sh
```

This starts 4 containers:
- `ccrp-jenkins` → Jenkins at **:8090**
- `ccrp-registry` → Private Docker registry at **:5000**
- `ccrp-tomcat` → Tomcat 11 at **:8888**
- `ccrp-selenium` → Selenium Chrome Grid at **:4444** + noVNC at **:7900**

> **Note**: The production container (`ccrp-prod` at `:8085`) is **created by the Jenkins pipeline**, not by this script.

To verify everything is up:
```bash
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
```

---

## 🌐 Step 2 — Open the Web App

### Option A — Docker Production Container (created by pipeline)
```
http://localhost:8085
```
- Apply for a certificate: http://localhost:8085/apply
- Track a request: http://localhost:8085/track
- Officer login: http://localhost:8085/officer → `officer` / `Officer@fCfWDT0k`

### Option B — Tomcat Deployment (deployed by pipeline)
```
http://localhost:8888/certportal-production
```

### Option C — Run locally without Docker
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
# Opens at http://localhost:8080
```

---

## 🏃 Step 3 — Run the App Quickly (without CI)

If you just want to see the app running, build and start it directly:

```bash
# 1. Build the WAR (skip tests for speed)
./mvnw -DskipTests package

# 2. Run it standalone
java -jar target/citizen-certificate-portal-*.war --spring.profiles.active=dev
# Open http://localhost:8080
```

---

## ⚙️ Step 4 — Jenkins: Trigger a Full Pipeline Run

1. Open **http://localhost:8090**
2. Login: `admin` / `WQHHc0wQLJaw4QxyKiaW`
3. Click **`ccrp-pipeline`** job
4. Click **▶ Build with Parameters** (left sidebar)
5. Leave defaults → click **Build**

### What happens (8 stages):

```
Checkout → Build & Unit Tests → Package WAR → Build Docker Image
→ Deploy Staging Container → Selenium E2E (Quality Gate)
→ Publish Image to Registry → Deploy (Docker container + Tomcat in parallel)
```

> Watch the stages turn green one by one. If Selenium fails → nothing gets deployed (quality gate).

---

## 🎭 Step 5 — Watch Selenium Tests Live

While a pipeline build is running in Step 4:

1. Open **http://localhost:7900**
2. Password: `secret`
3. You will see a real Chrome browser running the 8 E2E journeys live

You can also open the Selenium Grid dashboard: **http://localhost:4444/ui**

### Run Selenium tests manually (against the running app):
```bash
./mvnw verify -Pe2e
# Runs 8 browser journeys against http://localhost:8080
```

---

## 🐳 Step 6 — Docker Lifecycle Demo

This script walks through the **full container lifecycle** — build, tag, run, stop, restart, remove — and logs every command:

```bash
# Prerequisite: WAR must be built first
./mvnw -DskipTests package

# Run the full Docker demo
scripts/docker-lifecycle-demo.sh
```

### What it demonstrates:
| Step | Command | What it shows |
|---|---|---|
| 0 | `docker version` | Client & server info |
| 1 | `docker build` | Multi-stage layered image build |
| 2 | `docker tag` | Tagging: version, git hash, latest |
| 3 | `docker image inspect` | Non-root user, exposed ports, labels |
| 4 | `docker run` | Port mapping, named volume, env vars, restart policy |
| 5 | `curl /actuator/health` | Health check + live request submission |
| 6 | `docker logs / exec / stats` | Observability inside container |
| 7 | `docker stop / start / restart` | Graceful SIGTERM shutdown |
| 8 | `docker rm` + recreate | Data persists in volume across container deletion |
| 9 | `docker rmi` | Cleanup |

---

## 📦 Step 7 — Private Docker Registry

Check what images and tags are stored in the local registry:

```bash
# List all tags for the app image
curl http://localhost:5000/v2/sanad-ccrp/tags/list | jq .

# List all repositories in the registry
curl http://localhost:5000/v2/_catalog | jq .
```

Expected output — three tags per successful build:
- `1.2.0-b17` → version + build number
- `git-a3f9c12` → exact Git commit
- `latest` → always points to the newest

---

## 🧪 Step 8 — Run Tests Manually

```bash
# Unit + integration tests only (65 tests)
./mvnw verify

# Unit + integration + Selenium E2E (8 browser journeys)
./mvnw verify -Pe2e

# Check coverage report (opens after running verify)
open target/site/jacoco/index.html
```

---

## 🎭 Step 9 — Ansible (Configuration Management)

Spin up a clean Ubuntu 24.04 target node and deploy the app onto it:

```bash
# 1. Create a clean test node (Docker container acting as a server)
scripts/ansible-node.sh --recreate

# 2. Deploy version 1.2.0 to the node
scripts/ansible.sh site.yml -e app_version=1.2.0

# 3. One-command rollback to the previous version
scripts/ansible.sh rollback.yml
```

---

## 🛑 Stop Everything

```bash
# Stop and remove CI stack containers (Jenkins, Registry, Tomcat, Selenium)
docker compose -f ci/docker-compose.yml --env-file ci/.env down

# Also remove the production container
docker rm -f ccrp-prod
```

---

## 🌍 All URLs at a Glance

| URL | What |
|---|---|
| http://localhost:8085 | ✅ App — Docker production container |
| http://localhost:8085/apply | Citizen: apply for certificate |
| http://localhost:8085/track | Citizen: track with ID |
| http://localhost:8085/officer | Officer login |
| http://localhost:8888/certportal-production | ✅ App — Tomcat deployment |
| http://localhost:8090 | ⚙️ Jenkins CI/CD |
| http://localhost:5000/v2/_catalog | 📦 Docker Registry |
| http://localhost:4444/ui | 🎭 Selenium Grid dashboard |
| http://localhost:7900 | 👁️ Live browser (noVNC) |

---

## 📁 Key Files

| File | Purpose |
|---|---|
| [`Jenkinsfile`](Jenkinsfile) | Complete 8-stage pipeline definition |
| [`Dockerfile`](Dockerfile) | Multi-stage layered image (non-root, healthcheck) |
| [`ci/docker-compose.yml`](ci/docker-compose.yml) | Jenkins + Registry + Tomcat + Selenium stack |
| [`scripts/ci-up.sh`](scripts/ci-up.sh) | One-command CI stack launcher |
| [`scripts/docker-lifecycle-demo.sh`](scripts/docker-lifecycle-demo.sh) | Full Docker demo walkthrough |
| [`infra/ansible/site.yml`](infra/ansible/site.yml) | Main Ansible deployment playbook |
| [`infra/ansible/rollback.yml`](infra/ansible/rollback.yml) | Ansible rollback playbook |
| [`ci/.env`](ci/.env) | All secrets (auto-generated by `ci-up.sh`) |
