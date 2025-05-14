pipeline {
    agent any

    stages {
        stage('Checkout') {
            steps {
                cleanWs() // Clean the workspace
                checkout scm // Checkout source code
            }
        }

        stage('Build') {
            steps {
                bat 'mvn clean package' // Build the project
            }
        }

        stage('Test') {
            steps {
                bat 'mvn test' // Run tests
            }
        }

        stage('Deploy') {
            steps {
                // Your deployment steps
                echo 'Deploying...'
            }
        }
    }

    post {
        always {
            archiveArtifacts artifacts: 'target/*.jar', allowEmptyArchive: true
            junit 'target/surefire-reports/*.xml'
        }
    }
}