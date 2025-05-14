pipeline {
    agent any

    stages {
        stage('Checkout') {
            steps {
                cleanWs()
                checkout scm
            }
        }

        stage('Build') {
            steps {
                bat """
                docker run -v %WORKSPACE%:/workspace maven:3.8.4-openjdk-21 /bin/sh -c "cd /workspace && mvn clean package"
                """
            }
        }

        stage('Test') {
            steps {
                bat """
                docker run -v %WORKSPACE%:/workspace maven:3.8.4-openjdk-21 /bin/sh -c "cd /workspace && mvn test"
                """
            }
        }

        stage('Deploy') {
            steps {
                withCredentials([sshUserPrivateKey(credentialsId: 'ec2-ssh-key', keyFileVariable: 'SSH_KEY', usernameVariable: 'SSH_USER')]) {
                    bat """
                    scp -i %SSH_KEY% target\\HonorsProject-0.0.1-SNAPSHOT.jar %SSH_USER%@44.203.66.17:/home/%SSH_USER%/
                    ssh -i %SSH_KEY% %SSH_USER%@44.203.66.17 "pkill -f 'java -jar' || true; nohup java -jar /home/%SSH_USER%/HonorsProject-0.0.1-SNAPSHOT.jar &"
                    """
                }
            }
        }
    }

    post {
        always {
            archiveArtifacts 'target/*.jar'
            junit 'target/surefire-reports/*.xml'
        }
    }
}