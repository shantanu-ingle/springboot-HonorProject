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
                script {
                    docker.image('maven:3.8.4-openjdk-11').inside {
                        sh 'mvn clean package'
                    }
                }
            }
        }

        stage('Test') {
            steps {
                script {
                    docker.image('maven:3.8.4-openjdk-11').inside {
                        sh 'mvn test'
                    }
                }
            }
        }

        stage('Deploy') {
            steps {
                withCredentials([sshUserPrivateKey(credentialsId: 'ec2-ssh-key', keyFileVariable: 'SSH_KEY', usernameVariable: 'SSH_USER')]) {
                    bat """
                    scp -i %SSH_KEY% target\\*.jar %SSH_USER%@http://44.203.66.17/:/home/%SSH_USER%/
                    ssh -i %SSH_KEY% %SSH_USER%@http://44.203.66.17/ "pkill -f 'java -jar' || true; nohup java -jar /home/%SSH_USER%/*.jar &"
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