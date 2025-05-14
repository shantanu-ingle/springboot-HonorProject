pipeline {
    agent none

    stages {
        stage('Checkout') {
            agent {
                docker {
                    image 'maven:3.9.6-eclipse-temurin-21'
                    args """
                      --user root \
                      -v C:/Users/OMEN/.m2:/root/.m2 \
                      -v ${env.WORKSPACE.replaceAll('\\\\','/')}:${'/workspace'} \
                      -w /workspace
                    """
                    alwaysPull true
                }
            }
            steps {
                // this will now check out into /workspace inside the container
                checkout scm
            }
        }

        stage('Build') {
            agent {
                docker {
                    image 'maven:3.9.6-eclipse-temurin-21'
                    args """
                      --user root \
                      -v C:/Users/OMEN/.m2:/root/.m2 \
                      -v ${env.WORKSPACE.replaceAll('\\\\','/')}:${'/workspace'} \
                      -w /workspace
                    """
                    alwaysPull true
                }
            }
            steps {
                sh 'mvn clean package'
            }
        }

        stage('Test') {
            agent {
                docker {
                    image 'maven:3.9.6-eclipse-temurin-21'
                    args """
                      --user root \
                      -v C:/Users/OMEN/.m2:/root/.m2 \
                      -v ${env.WORKSPACE.replaceAll('\\\\','/')}:${'/workspace'} \
                      -w /workspace
                    """
                    alwaysPull true
                }
            }
            steps {
                sh 'mvn test'
            }
        }

        stage('Deploy') {
            agent any
            steps {
                withCredentials([sshUserPrivateKey(credentialsId: 'ec2-ssh-key', keyFileVariable: 'SSH_KEY', usernameVariable: 'SSH_USER')]) {
                    bat """
                    scp -i %SSH_KEY% target\\HonorsProject-0.0.1-SNAPSHOT.jar %SSH_USER%@44.203.66.17:/home/%SSH_USER/
                    ssh -i %SSH_KEY% %SSH_USER%@44.203.66.17 "nohup java -jar /home/%SSH_USER/HonorsProject-0.0.1-SNAPSHOT.jar &"
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
