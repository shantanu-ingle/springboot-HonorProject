pipeline {
    agent any

    stages {
        stage('Checkout') {
            steps {
                cleanWs() // Clean the workspace
                checkout scm // Checkout the source code
            }
        }

        stage('Build') {
            steps {
                bat 'mvn clean package' // Run Maven directly on Windows
            }
        }

        stage('Test') {
            steps {
                bat 'mvn test' // Run tests
            }
        }

        stage('Deploy') {
            steps {
                withCredentials([sshUserPrivateKey(
                    credentialsId: 'ec2-ssh-key',
                    keyFileVariable: 'SSH_KEY',
                    usernameVariable: 'SSH_USER'
                )]) {
                    bat """
                    scp -i %SSH_KEY% target\\*.jar %SSH_USER%@44.203.66.17:/home/%SSH_USER%/
                    ssh -i %SSH_KEY% %SSH_USER%@44.203.66.17 "nohup java -jar /home/%SSH_USER%/HonorsProject-0.0.1-SNAPSHOT.jar &"
                    """
                }
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