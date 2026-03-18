pipeline {
    agent any

    environment {
        IMAGE_NAME   = 'demo-backend'
        IMAGE_TAG    = "${env.BUILD_NUMBER}"
        REGISTRY     = credentials('docker-registry-url')   // set in Jenkins credentials
        DOCKER_CREDS = credentials('docker-registry-creds') // username/password binding
    }

    tools {
        maven 'Maven-3.9'   // configured in Jenkins → Global Tool Configuration
        jdk   'JDK-17'
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
                echo "Branch: ${env.BRANCH_NAME} | Build: ${env.BUILD_NUMBER}"
            }
        }

        stage('Build') {
            steps {
                dir('demo-backend') {
                    sh 'mvn clean compile -q'
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
                    sh 'mvn package -DskipTests -q'
                    archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
                }
            }
        }

        stage('Docker Build') {
            steps {
                dir('demo-backend') {
                    sh """
                        docker build \
                            -f Dockerfile \
                            -t ${IMAGE_NAME}:${IMAGE_TAG} \
                            -t ${IMAGE_NAME}:latest \
                            .
                    """
                }
            }
        }

        stage('Docker Push') {
            when {
                anyOf {
                    branch 'main'
                    branch 'develop'
                }
            }
            steps {
                sh """
                    echo ${DOCKER_CREDS_PSW} | docker login ${REGISTRY} -u ${DOCKER_CREDS_USR} --password-stdin
                    docker tag ${IMAGE_NAME}:${IMAGE_TAG} ${REGISTRY}/${IMAGE_NAME}:${IMAGE_TAG}
                    docker tag ${IMAGE_NAME}:latest     ${REGISTRY}/${IMAGE_NAME}:latest
                    docker push ${REGISTRY}/${IMAGE_NAME}:${IMAGE_TAG}
                    docker push ${REGISTRY}/${IMAGE_NAME}:latest
                """
            }
        }

        stage('Deploy') {
            when { branch 'main' }
            steps {
                echo "Deploying ${IMAGE_NAME}:${IMAGE_TAG} ..."
                // Option A – docker-compose (single server)
                sh """
                    docker-compose -f docker-compose.yml up -d --no-deps backend
                """
                // Option B – kubectl (uncomment for Kubernetes)
                // sh """
                //     kubectl set image deployment/backend backend=${REGISTRY}/${IMAGE_NAME}:${IMAGE_TAG}
                //     kubectl rollout status deployment/backend
                // """
            }
        }
    }

    post {
        success {
            echo "Backend pipeline SUCCESS — image: ${IMAGE_NAME}:${IMAGE_TAG}"
        }
        failure {
            echo "Backend pipeline FAILED on branch ${env.BRANCH_NAME}"
        }
        always {
            sh 'docker image prune -f --filter "until=24h"'
            cleanWs()
        }
    }
}
