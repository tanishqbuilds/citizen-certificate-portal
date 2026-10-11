// Sanad — Citizen Certificate Request Portal
// Delivery pipeline (pipeline as code). Job definition: ci/jenkins/casc/jobs.groovy → ccrp-pipeline.

pipeline {
    agent any

    options {
        timestamps()
        ansiColor('xterm')
        timeout(time: 30, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '30', artifactNumToKeepStr: '10'))
        disableConcurrentBuilds()
        skipDefaultCheckout(true)
    }

    parameters {
        choice(name: 'TARGET_ENV', choices: ['staging', 'production'],
               description: 'Tomcat environment. Deployed to /certportal-<env> with its own settings and data directory.')
        booleanParam(name: 'DEPLOY', defaultValue: true,
                     description: 'Deploy to Tomcat after a green build.')
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
                    env.APP_VERSION = sh(returnStdout: true,
                            script: './mvnw -q $MAVEN_ARGS help:evaluate -Dexpression=project.version -DforceStdout').trim()
                    currentBuild.displayName = "#${env.BUILD_NUMBER} · ${env.APP_VERSION} · ${env.GIT_SHORT}"
                    currentBuild.description = "TARGET_ENV=${params.TARGET_ENV}"
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

        stage('Deploy to Tomcat') {
            when { expression { params.DEPLOY } }
            steps {
                withCredentials([
                        usernamePassword(credentialsId: 'tomcat-deployer', usernameVariable: 'TOMCAT_USER', passwordVariable: 'TOMCAT_PASSWORD'),
                        string(credentialsId: 'officer-password', variable: 'OFFICER_PASSWORD')]) {
                    sh 'scripts/deploy-tomcat.sh target/ccrp.war "$TARGET_ENV"'
                }
            }
        }

        stage('Smoke test') {
            when { expression { params.DEPLOY } }
            steps {
                sh 'scripts/smoke-test.sh "$TOMCAT_URL/certportal-$TARGET_ENV" "$APP_VERSION" "$TARGET_ENV"'
            }
        }
    }

    post {
        success {
            githubStatus('success', params.DEPLOY ? "Deployed ${env.APP_VERSION} to Tomcat ${params.TARGET_ENV}" : "Build ${env.APP_VERSION} passed")
        }
        unsuccessful {
            githubStatus('failure', "Build #${env.BUILD_NUMBER} failed")
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
