// GetDocs — Citizen Certificate Request Portal
// Delivery pipeline (pipeline as code). Job definition: ci/jenkins/casc/jobs.groovy → ccrp-pipeline.
//
//   Checkout → Build & unit tests → Package WAR → Build image → Deploy staging container
//            → Selenium E2E (quality gate) → Publish image → Deploy ┬ production container (:8085)
//                                                                     └ Tomcat /certportal-<TARGET_ENV>
//
// Build once, deploy many: the WAR that passed the unit tests is the one inside the image, and the image
// that passed the browser journeys is the one pushed and deployed. A failing journey stops everything after it.

pipeline {
    agent any

    options {
        timestamps()
        ansiColor('xterm')
        timeout(time: 45, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '30', artifactNumToKeepStr: '10'))
        disableConcurrentBuilds()
        skipDefaultCheckout(true)
    }

    parameters {
        choice(name: 'TARGET_ENV', choices: ['production', 'staging'],
               description: 'Tomcat context that receives the WAR after the quality gate (/certportal-<env>).')
        booleanParam(name: 'DEPLOY', defaultValue: true,
                     description: 'Publish and deploy after a green build and quality gate.')
    }

    environment {
        TOMCAT_URL = 'http://ccrp-tomcat:8080'
        MAVEN_ARGS = '-B -ntp'
        IMAGE_REPO = "${env.REGISTRY ?: 'localhost:5000'}/getdocs-ccrp"
        PROD_CONTAINER = 'ccrp-prod'
        PROD_PORT = '8085'
    }

    stages {
        stage('Checkout') {
            steps {
                script {
                    def scmVars = checkout scm
                    env.GIT_COMMIT = scmVars.GIT_COMMIT
                    env.GIT_SHORT = env.GIT_COMMIT.take(7)
                    // Parameters declared here only exist after the job's first run; never rely on them being set.
                    env.TARGET_ENV = params.TARGET_ENV ?: 'production'
                    env.DEPLOY = (params.DEPLOY == null || params.DEPLOY) ? 'true' : 'false'
                    env.APP_VERSION = sh(returnStdout: true,
                            script: './mvnw -q $MAVEN_ARGS help:evaluate -Dexpression=project.version -DforceStdout').trim()
                    env.IMAGE_TAG = "${env.APP_VERSION}-b${env.BUILD_NUMBER}"
                    env.IMAGE = "${env.IMAGE_REPO}:${env.IMAGE_TAG}"
                    env.STAGING_CONTAINER = "ccrp-staging-${env.BUILD_NUMBER}"
                    currentBuild.displayName = "#${env.BUILD_NUMBER} · ${env.IMAGE_TAG} · ${env.GIT_SHORT}"
                    currentBuild.description = "image ${env.IMAGE_TAG} · Tomcat ${env.TARGET_ENV}"
                }
                githubStatus('pending', "Build #${env.BUILD_NUMBER} running")
            }
        }

        stage('Build & unit tests') {
            steps {
                sh './mvnw $MAVEN_ARGS clean verify'
            }
            post {
                always {
                    junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml'
                    recordCoverage tools: [[parser: 'JACOCO', pattern: 'target/site/jacoco/jacoco.xml']],
                                   sourceCodeRetention: 'NEVER', skipPublishingChecks: true
                }
            }
        }

        stage('Package WAR') {
            steps {
                sh '''
                    cp target/citizen-certificate-portal-*.war target/ccrp.war
                    sha256sum target/citizen-certificate-portal-*.war | tee target/SHA256SUMS
                '''
                archiveArtifacts artifacts: 'target/citizen-certificate-portal-*.war, target/SHA256SUMS', fingerprint: true
            }
        }

        stage('Build image') {
            steps {
                sh '''
                    docker build \
                      --build-arg VERSION="$APP_VERSION" \
                      --build-arg REVISION="$GIT_SHORT" \
                      --build-arg CREATED="$(date -u +%Y-%m-%dT%H:%M:%SZ)" \
                      --label "ccrp.build=$BUILD_TAG" \
                      -t "$IMAGE" -t "$IMAGE_REPO:git-$GIT_SHORT" .
                    docker image inspect "$IMAGE" --format 'image {{.Id}} · {{.Size}} bytes · user {{.Config.User}}'
                '''
            }
        }

        stage('Deploy staging container') {
            steps {
                withCredentials([string(credentialsId: 'officer-password', variable: 'OFFICER_PASSWORD')]) {
                    sh '''
                        docker rm -f "$STAGING_CONTAINER" >/dev/null 2>&1 || true
                        docker run -d --name "$STAGING_CONTAINER" --network "$DOCKER_NETWORK" \
                          -e APP_ENVIRONMENT=staging -e APP_OFFICER_PASSWORD="$OFFICER_PASSWORD" \
                          --label ccrp.ephemeral=true "$IMAGE"
                    '''
                }
                sh '''
                    for i in $(seq 1 60); do
                      s=$(docker inspect --format '{{.State.Health.Status}}' "$STAGING_CONTAINER")
                      [ "$s" = healthy ] && break
                      [ "$s" = unhealthy ] && { docker logs --tail 60 "$STAGING_CONTAINER"; exit 1; }
                      sleep 2
                    done
                    echo "staging container: $s"
                    scripts/smoke-test.sh "http://$STAGING_CONTAINER:8080" "$APP_VERSION" staging
                '''
            }
        }

        stage('Selenium E2E (quality gate)') {
            steps {
                withCredentials([string(credentialsId: 'officer-password', variable: 'E2E_OFFICER_PASSWORD')]) {
                    // Only the browser journeys run here; unit and integration tests already passed above.
                    sh '''./mvnw $MAVEN_ARGS verify -Pe2e \
                            -De2e.external=true \
                            -De2e.baseUrl="http://$STAGING_CONTAINER:8080" \
                            -De2e.remoteUrl="$SELENIUM_URL" \
                            -Dtest=NoUnitTestsInThisStage -Dsurefire.failIfNoSpecifiedTests=false \
                            -Djacoco.skip=true'''
                }
            }
            post {
                always {
                    junit allowEmptyResults: true, testResults: 'target/failsafe-reports/TEST-*.xml'
                    publishHTML(target: [reportName: 'Selenium E2E report', reportDir: 'target/reports',
                                         reportFiles: 'failsafe.html', keepAll: true, alwaysLinkToLastBuild: true,
                                         allowMissing: true])
                    archiveArtifacts artifacts: 'target/selenium-screenshots/**', allowEmptyArchive: true
                }
                failure {
                    echo 'Quality gate FAILED: the image is not published and nothing is deployed.'
                }
            }
        }

        stage('Publish image') {
            when { environment name: 'DEPLOY', value: 'true' }
            steps {
                sh '''
                    docker tag "$IMAGE" "$IMAGE_REPO:latest"
                    docker push -q "$IMAGE"
                    docker push -q "$IMAGE_REPO:git-$GIT_SHORT"
                    docker push -q "$IMAGE_REPO:latest"
                    echo "registry tags:"; curl -fsS http://ccrp-registry:5000/v2/getdocs-ccrp/tags/list | jq -c .
                '''
            }
        }

        stage('Deploy') {
            when { environment name: 'DEPLOY', value: 'true' }
            parallel {
                stage('Production container') {
                    steps {
                        withCredentials([string(credentialsId: 'officer-password', variable: 'OFFICER_PASSWORD')]) {
                            sh 'scripts/deploy-container.sh "$IMAGE" "$PROD_CONTAINER" "$PROD_PORT"'
                        }
                        sh 'scripts/smoke-test.sh "http://$PROD_CONTAINER:8080" "$APP_VERSION" production'
                    }
                }
                stage('Tomcat') {
                    steps {
                        withCredentials([
                                usernamePassword(credentialsId: 'tomcat-deployer', usernameVariable: 'TOMCAT_USER', passwordVariable: 'TOMCAT_PASSWORD'),
                                string(credentialsId: 'officer-password', variable: 'OFFICER_PASSWORD')]) {
                            sh 'scripts/deploy-tomcat.sh target/ccrp.war "$TARGET_ENV"'
                        }
                        sh 'scripts/smoke-test.sh "$TOMCAT_URL/certportal-$TARGET_ENV" "$APP_VERSION" "$TARGET_ENV"'
                    }
                }
            }
        }
    }

    post {
        success {
            githubStatus('success', "Quality gate passed · ${env.IMAGE_TAG} deployed")
        }
        unsuccessful {
            githubStatus('failure', "Build #${env.BUILD_NUMBER} failed: not deployed")
        }
        always {
            // Staging is disposable; local build images are pruned (the registry keeps every version).
            sh '''
                docker rm -f "$STAGING_CONTAINER" >/dev/null 2>&1 || true
                docker images "$IMAGE_REPO" --format '{{.CreatedAt}}\t{{.Repository}}:{{.Tag}}' | sort -r \
                  | grep -v ':latest$' | awk -F'\t' 'NR>6 {print $2}' | xargs -r docker rmi >/dev/null 2>&1 || true
            '''
        }
        cleanup {
            cleanWs(deleteDirs: true, notFailBuild: true)
        }
    }
}

/** Publishes a commit status to GitHub when a token is configured (credential 'github-token' ≠ "none"). */
def githubStatus(String state, String description) {
    if (!env.GIT_COMMIT) { return }
    withCredentials([string(credentialsId: 'github-token', variable: 'GH_TOKEN')]) {
        withEnv(["GH_STATE=${state}", "GH_DESC=${description}"]) {
            sh '''
                set +x
                [ "$GH_TOKEN" = "none" ] && exit 0
                slug=$(echo "$CCRP_REPO_URL" | sed -E 's#.*github\\.com[:/]([^/]+/[^/.]+)(\\.git)?$#\\1#')
                body=$(jq -n --arg s "$GH_STATE" --arg d "$GH_DESC" --arg u "$BUILD_URL" \
                       '{state: $s, description: ($d | .[0:140]), context: "jenkins/ccrp-pipeline", target_url: $u}')
                curl -fsS -o /dev/null -X POST \
                     -H "Authorization: Bearer $GH_TOKEN" -H "Accept: application/vnd.github+json" \
                     "https://api.github.com/repos/$slug/statuses/$GIT_COMMIT" -d "$body" \
                  || echo "warning: GitHub commit status not published"
            '''
        }
    }
}
