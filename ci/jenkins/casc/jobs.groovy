// Job DSL — every Jenkins job for Sanad is defined here, nothing is clicked together in the UI.
def repo = System.getenv('CCRP_REPO_URL') ?: 'https://github.com/tanishqbuilds/citizen-certificate-portal.git'

// Phase 7: classic CI job. Polls develop, runs the Maven build with tests, archives the WAR.
job('ccrp-ci-build') {
    displayName('CCRP · CI build (Maven, freestyle)')
    description('''Phase 7 — Continuous Integration.
Polls the develop branch every 2 minutes; on a new commit runs ./mvnw clean verify,
publishes JUnit results and archives the WAR with a fingerprint.''')
    logRotator { numToKeep(30); artifactNumToKeep(10) }
    label('maven')
    scm {
        git {
            remote { url(repo) }
            branch('*/develop')
            extensions { cleanBeforeCheckout() }
        }
    }
    triggers { scm('H/2 * * * *') }
    wrappers {
        timestamps()
        timeout { absolute(20) }
    }
    steps {
        shell('''set -eu
java -version
./mvnw -B -ntp -v
./mvnw -B -ntp clean verify''')
    }
    publishers {
        archiveJunit('target/surefire-reports/*.xml')
        archiveArtifacts {
            pattern('target/*.war')
            fingerprint(true)
            onlyIfSuccessful(true)
        }
    }
}
