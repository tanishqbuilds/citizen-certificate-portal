// Sanad — Citizen Certificate Request Portal
// Delivery pipeline (pipeline as code). Job definition: ci/jenkins/casc/jobs.groovy → ccrp-pipeline.
//
//   Checkout → Build & unit tests → Package → Deploy to staging → Selenium E2E (quality gate) → Deploy to production
//
// A failing Selenium journey stops the pipeline before anything is promoted.

pipeline {
    agent any

    options {
        timestamps()
        ansiColor('xterm')
        timeout(time: 40, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '30', artifactNumToKeepStr: '10'))
        disableConcurrentBuilds()
        skipDefaultCheckout(true)
    }

    parameters {
        choice(name: 'TARGET_ENV', choices: ['production', 'staging'],
               description: 'Environment promoted to after the Selenium quality gate passes (/certportal-<env>). Tests always run against staging.')
        booleanParam(name: 'DEPLOY', defaultValue: true,
                     description: 'Deploy after a green build and quality gate.')
    }

    environment {
        TOMCAT_URL = 'http://ccrp-tomcat:8080'
        MAVEN_ARGS = '-B -ntp'
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
                    currentBuild.displayName = "#${env.BUILD_NUMBER} · ${env.APP_VERSION} · ${env.GIT_SHORT}"
                    currentBuild.description = "TARGET_ENV=${env.TARGET_ENV}"
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

        stage('Package') {
            steps {
                sh '''
                    cp target/citizen-certificate-portal-*.war target/ccrp.war
                    sha256sum target/citizen-certificate-portal-*.war | tee target/SHA256SUMS
                '''
                archiveArtifacts artifacts: 'target/citizen-certificate-portal-*.war, target/SHA256SUMS', fingerprint: true
            }
        }

        stage('Deploy to staging') {
            steps {
                withCredentials([
                        usernamePassword(credentialsId: 'tomcat-deployer', usernameVariable: 'TOMCAT_USER', passwordVariable: 'TOMCAT_PASSWORD'),
                        string(credentialsId: 'officer-password', variable: 'OFFICER_PASSWORD')]) {
                    sh 'scripts/deploy-tomcat.sh target/ccrp.war staging'
                }
                sh 'scripts/smoke-test.sh "$TOMCAT_URL/certportal-staging" "$APP_VERSION" staging'
            }
        }

        stage('Selenium E2E (quality gate)') {
            steps {
                withCredentials([string(credentialsId: 'officer-password', variable: 'E2E_OFFICER_PASSWORD')]) {
                    // Only the browser journeys run here; unit and integration tests already passed above.
                    sh '''./mvnw $MAVEN_ARGS verify -Pe2e \
                            -De2e.external=true \
                            -De2e.baseUrl="$TOMCAT_URL/certportal-staging" \
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
                    echo 'Quality gate FAILED: the release is not promoted. See the Selenium report and failure screenshots.'
                }
            }
        }

        stage('Deploy to production') {
            when {
                allOf {
                    environment name: 'DEPLOY', value: 'true'
                    environment name: 'TARGET_ENV', value: 'production'
                }
            }
            steps {
                withCredentials([
                        usernamePassword(credentialsId: 'tomcat-deployer', usernameVariable: 'TOMCAT_USER', passwordVariable: 'TOMCAT_PASSWORD'),
                        string(credentialsId: 'officer-password', variable: 'OFFICER_PASSWORD')]) {
                    sh 'scripts/deploy-tomcat.sh target/ccrp.war production'
                }
                sh 'scripts/smoke-test.sh "$TOMCAT_URL/certportal-production" "$APP_VERSION" production'
            }
        }
    }

    post {
        success {
            githubStatus('success', "Quality gate passed · ${env.APP_VERSION} promoted to ${env.TARGET_ENV}")
        }
        unsuccessful {
            githubStatus('failure', "Build #${env.BUILD_NUMBER} failed: not promoted")
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
