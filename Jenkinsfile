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
                docker run -v "%CD%":/workspace maven:3.9.9-eclipse-temurin-21 /bin/sh -c "cd /workspace && mvn clean package"
                """
            }
        }

        stage('Test') {
            steps {
                bat """
                docker run -v "%CD%":/workspace maven:3.9.9-eclipse-temurin-21 /bin/sh -c "cd /workspace && mvn test"
                """
            }
        }

        stage('Deploy') {
            steps {
                withCredentials([sshUserPrivateKey(credentialsId: 'ec2-ssh-key', keyFileVariable: 'SSH_KEY', usernameVariable: 'SSH_USER')]) {
                    bat """
                    icacls "%SSH_KEY%" /inheritance:r
                    icacls "%SSH_KEY%" /grant:r "%USERNAME%:F"

                    scp -i "%SSH_KEY%" target/HonorsProject-0.0.1-SNAPSHOT.jar %SSH_USER%@54.159.204.82:/home/%SSH_USER%/

                    ssh -i "%SSH_KEY%" %SSH_USER%@54.159.204.82 "
                        if ! command -v java &>/dev/null; then
                            sudo apt-get update -qy
                            sudo apt-get install -qy openjdk-21-jdk
                        fi
                        pkill -f 'java -jar' || true
                        sleep 5
                        nohup java -jar /home/%SSH_USER%/HonorsProject-0.0.1-SNAPSHOT.jar --server.address=0.0.0.0 > /home/%SSH_USER%/app.log 2>&1 &
                        sleep 20
                        cat /home/%SSH_USER%/app.log
                        curl -sSf --retry 3 --retry-delay 10 http://localhost:8081/hello || (echo 'App failed to start' && exit 1)
                    "
                    """
                }
            }
        }

        stage('Verify Deployment') {
            steps {
                script {
                    retry(5) {
                        timeout(time: 2, unit: 'MINUTES') {
                            bat """
                            curl -v --retry 5 --retry-delay 10 --max-time 30 http://54.159.204.82:8081/hello
                            """
                        }
                    }
                }
            }
        }
    }

    post {
        always {
            archiveArtifacts 'target/*.jar'
            junit 'target/surefire-reports/*.xml'
        }
        failure {
            withCredentials([sshUserPrivateKey(credentialsId: 'ec2-ssh-key', keyFileVariable: 'SSH_KEY', usernameVariable: 'SSH_USER')]) {
                bat """
                ssh -i "%SSH_KEY%" %SSH_USER%@54.159.204.82 "cat /home/%SSH_USER%/app.log" > deployment.log
                """
            }
            archiveArtifacts artifacts: 'deployment.log', allowEmptyArchive: true
        }
    }
}