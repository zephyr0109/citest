pipeline{
    agent any
    environment {
        DOCKER_CREDENTIALS = 'docker-hub'
        GITHUB_CREDENTIALS = 'git-hub'
        IMAGE_REPOSITORY = 'zephyr0109/cicdtest'
    }

    // CI Pipeline
    stages {
        stage( 'Checkout') {
            steps {
                git credentialsId : "${GITHUB_CREDENTIALS}",
                 url: "https://github.com/zephyr0109/citest.git"

            }
        }

        stage("Build"){
            steps {
                sh 'mvn clean package -DskipTests'
            }
        }

        stage("Test") {
            steps {
                sh "mvn test"
            }
        }

        stage("Build Docker Image"){
            steps {
                sh "docker build -t ${IMAGE_REPOSITORY}:latest ."\
            }
        }

        stage("Push Docker Image"){
            steps {
                withCredentials([usernamePassword(credentialsId : "$DOCKER_CREDENTIALS",
                passwordVariable: "DOCKER_PASS",
                usernameVariable: "DOCKER_USER")]){
                    sh 'echo $DOCKER_PASS | docker login -u $DOCKER_USER --password-stdin'
                    sh "docker push ${IMAGE_REPOSITORY}:latest"
                }
            }
        }
        // CD Pipeline
        stage("Deploy to Local") {
            steps{
                echo "Deploying container"

                sh "docker stop hello-ci-web || true"
                sh "docker rm hello-ci-web || true"

                withCredentials([usernamePassword(credentialsId : "$DOCKER_CREDENTIALS",
                                passwordVariable: "DOCKER_PASS",
                                usernameVariable: "DOCKER_USER")]) {
                    sh 'echo $DOCKER_PASS | docker login -u $DOCKER_USER --password-stdin'
                    sh "docker pull ${IMAGE_REPOSITORY}:latest"
                }
                sh """
                    docker run -d \
                    --name hello-ci-web \
                    -p 8080:8080 \
                    ${IMAGE_REPOSITORY}:latest
                """

            }
        }
        stage("Health check"){
            steps{
              sh """
                echo 'Waiting for app to start'
                sleep 5
                curl -f http://localhost:8080/
             """
            }
        }
    }

    post {
        always {
            echo "pipeline finished"

        }
        success {
            echo "Build and push successful"

        }
        failure {
            echo "Build or test failed"
        }
    }
}
