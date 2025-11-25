pipeline{
    agent any
    environment {
        DOCKER_CREDENTIALS = 'docker-hub'
        GITHUB_CREDENTIALS = 'git-hub'
        IMAGE_REPOSITORY = 'zephyr0109/cicdtest'
    }

    stages {
        stage( 'Checkout') {
            steps {
                git credentialsId : "${GITHUB_CREDENTIALS}", url: "https://github.com/zephyr0109/citest.git"

            }
        }

        stage("Build"){
            steps {
                sh 'mvn clean package -DskipTests=false'
            }
        }

        stages("Test") {
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