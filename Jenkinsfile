pipeline{
    agent any
    environment {
        DOCKER_CREDENTIALS = 'docker-hub'
        IMAGE_TAG = "${env.BRANCH_NAME}-${env.BUILD_NUMBER}"
        IMAGE_REPO = "${env.BRANCH_NAME == 'master' ? 'zephyr0109/cicdtest' : 'zephyr0109/cicdtest-dev'}"
        CONTAINER_NAME = "${env.BRANCH_NAME == 'master' ? 'hello-ci-prod' : 'hello-ci-dev'}"
        CONTAINER_PORT = "${env.BRANCH_NAME == 'master' ? '8080' : '8081'}"
    }
    stages {
        stage("Checkout") {
            steps {
                checkout scm
            }
        }
        stage("Build") {
            steps {
                sh "mvn clean package -DskipTests"
            }
        }
        stage("Test") {
            steps{
                sh "mvn test"
            }
        }
        stage("Docker build") {
            steps {
                script {
                    sh """
                        docker build -t ${IMAGE_REPO}:${IMAGE_TAG} .
                        docker tag ${IMAGE_REPO}:${IMAGE_TAG} ${IMAGE_REPO}:latest
                    """
                }
            }
        }
        stage("Docker Login & Push"){
            steps {
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
        stage("Deploy"){
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
            steps{
                sh """
                    echo "Waiting for app on ${CONTAINER_PORT}"
                """
            }
        }



    }
}