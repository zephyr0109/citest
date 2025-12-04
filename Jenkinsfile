pipeline{
    agent any
    environment {
        DOCKER_CREDENTIALS = 'docker-hub'
        IMAGE_TAG = "${env.BRANCH_NAME}-${env.BUILD_NUMBER}"
        IMAGE_REPO = "${env.BRANCH_NAME == 'master' ? 'zephyr0109/cicdtest' : 'zephyr0109/cicdtest-dev'}"
        GITHUB_REPO = "zephyr0109/citest"
        CONTAINER_NAME = "${env.BRANCH_NAME == 'master' ? 'hello-ci-prod' : 'hello-ci-dev'}"
        CONTAINER_PORT = "${env.BRANCH_NAME == 'master' ? '8080' : '8081'}"
        GITHUB_TOKEN = credentials('git-hub')
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
                sh "mvn -DskipTests -B clean package "
            }
        }
        stage("Test") {
            steps{
                sh "mvn test"
            }
        }
        stage("Docker build & push") {
            // PR이 아닐 경우
            when {
                not { changeRequest() }
            }
            steps {
                script {
                    sh """
                        docker build -t ${IMAGE_REPO}:${IMAGE_TAG} .
                        docker tag ${IMAGE_REPO}:${IMAGE_TAG} ${IMAGE_REPO}:latest
                    """
                    withCredentials([
                                        usernamePassword(credentialsId : "${DOCKER_CREDENTIALS}",
                                         usernameVariable : "DOCKER_USER",
                                          passwordVariable : "DOCKER_PASS")
                                    ]) {
                                        sh """
                                            echo \$DOCKER_PASS | docker login -u \$DOCKER_USER --password-stdin
                                            docker push ${IMAGE_REPO}:${IMAGE_TAG}
                                            docker push ${IMAGE_REPO}:latest
                                        """
                                    }
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
                    sh """
                        docker stop ${CONTAINER_NAME} || true
                        docker rm ${CONTAINER_NAME} || true
                        docker pull ${IMAGE_REPO}:latest
                        docker run -d --name ${CONTAINER_NAME} \
                            -p ${CONTAINER_PORT}:8080\
                            ${IMAGE_REPO}:latest
                    """
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
    withCredentials([string(credentialsId: 'git-hub', variable:"TOKEN")]){
        sh """
            curl -s -H "Authorization: token ${TOKEN}" \
                 -H "Content-Type: application/json" \
                 -d '{"body": "${text.replace("\n","\\n")}"}' \
                 ${apiUrl}
            """
    }

}