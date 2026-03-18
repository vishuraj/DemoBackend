pipeline {
    agent any

    environment {
        AWS_REGION = 'eu-north-1'
        ACCOUNT_ID = '766209049494'
        IMAGE_NAME = 'demo-test'
        IMAGE_TAG  = "${env.BUILD_NUMBER}"
        IMAGE_URI  = "${ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com/${IMAGE_NAME}"
    }

    tools {
        maven 'Maven-3.9'
        jdk   'JDK-17'
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
                dir('demo-backend') {
                    sh 'mvn clean compile'
                }
            }
        }

        stage('Test') {
            steps {
                dir('demo-backend') {
                    sh 'mvn test'
                }
            }
            post {
                always {
                    junit 'demo-backend/target/surefire-reports/*.xml'
                }
            }
        }

        stage('Package') {
            steps {
                dir('demo-backend') {
                    sh 'mvn package -DskipTests'
                }
            }
        }

        stage('Docker Build') {
            steps {
                dir('demo-backend') {
                    sh """
                        docker build -t ${IMAGE_NAME}:${IMAGE_TAG} .
                    """
                }
            }
        }

        stage('Login to ECR') {
            steps {
                withCredentials([[
                    $class: 'AmazonWebServicesCredentialsBinding',
                    credentialsId: 'aws-creds'   // 🔥 Jenkins AWS credentials ID
                ]]) {
                    sh """
                        aws ecr get-login-password --region ${AWS_REGION} \
                        | docker login --username AWS --password-stdin ${ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com
                    """
                }
            }
        }

        stage('Tag Image') {
            steps {
                sh """
                    docker tag ${IMAGE_NAME}:${IMAGE_TAG} ${IMAGE_URI}:${IMAGE_TAG}
                    docker tag ${IMAGE_NAME}:${IMAGE_TAG} ${IMAGE_URI}:latest
                """
            }
        }

        stage('Push to ECR') {
            steps {
                sh """
                    docker push ${IMAGE_URI}:${IMAGE_TAG}
                    docker push ${IMAGE_URI}:latest
                """
            }
        }

        stage('Deploy (EC2)') {
            when { branch 'main' }
            steps {
                sh """
                    docker stop backend || true
                    docker rm backend || true

                    docker run -d -p 8080:8080 \
                    --name backend \
                    ${IMAGE_URI}:latest
                """
            }
        }
    }

    post {
        success {
            echo "SUCCESS: ${IMAGE_URI}:${IMAGE_TAG}"
        }
        failure {
            echo "FAILED on ${env.BRANCH_NAME}"
        }
        always {
            sh 'docker system prune -f'
            cleanWs()
        }
    }
}