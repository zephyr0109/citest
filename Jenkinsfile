@Library('jenkins-shared-library-test') _

pipeline{
    agent any
    environment {
        GITHUB_TOKEN = credentials('github-token-api')
        DOCKER_CREDENTIALS = 'docker-hub'
        GITHUB_REPO = "zephyr0109/citest"
        MAVEN_CACHE = "/Users/junyul.im/.m2"

        IMAGE_TAG = "${env.BRANCH_NAME}-${env.BUILD_NUMBER}"
        IMAGE_REPO = "${env.BRANCH_NAME == 'master' ? 'zephyr0109/cicdtest' : 'zephyr0109/cicdtest-dev'}"
        CONTAINER_NAME = "${env.BRANCH_NAME == 'master' ? 'hello-ci-prod' : 'hello-ci-dev'}"
        CONTAINER_PORT = "${env.BRANCH_NAME == 'master' ? '8080' : '8081'}"

    }
    stages {
        stage("Checkout") {
            steps {
                checkout scm
                script {
                    echo "BRANCH_NAME=${env.BRANCH_NAME}"
                    echo "CHANGE_ID=${env.CHANGE_ID}"
                    echo "IS_PR: ${isChangeRequest()}"
                }
            }
        }
        stage("Build") {
            steps {
                sh "mvn -DskipTests -Dmaven.repo.local=${env.MAVEN_CACHE} clean package "
            }
        }
        stage("Test(Parallel)") {
            parallel{
                stage("repository test") {
                    steps {
                        sh 'mvn -Dtest="com.example.hello.repository.*Test" test'
                    }
                }
                stage("unit test") {
                    steps{
                        sh 'mvn -Dtest="com.example.hello.unit.*Test" test'
                    }
                }
            }
        }

           stage("Set Image Tag") {
                    steps {
                        script {
                            GIT_NUM = sh(
                                script : "git rev-parse --short HEAD",
                                returnStdout : true
                            ).trim()
                            env.IMAGE_TAG = "${env.BRANCH_NAME}-${GIT_NUM}"
                            echo "IMAGE_TAG: ${IMAGE_TAG}"
                        }
                    }
                }

        stage("Docker build & push") {
            // PR이 아닐 경우
            when {
                not { changeRequest() }
            }
            steps {
                script {
                    dockerBuild(image : IMAGE_REPO, tag : IMAGE_TAG)
                }
            }
        }

        stage("Deploy"){
            when{
                allOf{
                    not { changeRequest() }
                    anyOf {
                        branch 'develop'
                        branch 'master'
                    }
                }
            }
            steps {
                script {

                    deployApp(
                        containerName : CONTAINER_NAME
                        imageRepo : IMAGE_REPO
                        containerPort : CONTAINER_PORT
                    )
                }
            }
        }

        stage("Health Check"){
            when {not { changeRequest()}}
            steps{
                sh """
                    echo "Waiting for app on ${CONTAINER_PORT}"
                    sleep 8
                    curl -f http://localhost:${CONTAINER_PORT}/ || (echo 'smoke failed' && exit 1)
                """
            }
        }
    }


    post {
        success {

            echo "SUCCESS: ${env.BRANCH_NAME} (CHANGE_ID=${env.CHANGE_ID})"

        }
        failure {
            echo "FAILED: ${env.BRANCH_NAME} (CHANGE_ID=${env.CHANGE_ID})"
        }
        always {
            junit '**/target/surefire-reports/*.xml'

            echo "GITHUB_TOKEN length: ${GITHUB_TOKEN.length()}"
            script {
                if (isChangeRequest()) {
                    def status = currentBuild.currentResult
                    def message = ""

                    if (status == "SUCCESS") {
                        message = """
                        ✔ **CI Passed**
                        - Build: SUCCESS
                        - Tests: SUCCESS
                        - Jenkins Build #: ${env.BUILD_NUMBER}
                        """
                    } else {
                        message = """
                        ❌ **CI Failed**
                        - Stage failure: ${status}
                        - Check Jenkins console logs
                        """
                    }

                    postCommentToPR(message)
                }
            }
        }

    }
}

// helper: changeRequest() 가 제대로 동작하지 않을 때 사용할 수 있는 함수
def isChangeRequest() {
  return env.CHANGE_ID != null && env.CHANGE_ID != ''
}

def postCommentToPR(text) {
    def pr = env.CHANGE_ID
    def apiUrl = "https://api.github.com/repos/${env.GITHUB_REPO}/issues/${pr}/comments"
    withCredentials([string(credentialsId: 'github-token-api', variable:"TOKEN")]){
        sh """
            curl -s -H "Authorization: token ${TOKEN}" \
                 -H "Content-Type: application/json" \
                 -d '{"body": "${text.replace("\n","\\n")}"}' \
                 ${apiUrl}
            """
    }

}