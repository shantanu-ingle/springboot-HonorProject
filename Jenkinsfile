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
                    echo Setting permissions on SSH key...
                    icacls "%SSH_KEY%" /inheritance:r
                    icacls "%SSH_KEY%" /grant:r "%USERNAME%:F"

                    echo Starting deployment at %DATE% && time /t

                    echo Testing SSH connection...
                    C:\\Windows\\System32\\OpenSSH\\ssh.exe -o ConnectTimeout=30 -o StrictHostKeyChecking=no -i "%SSH_KEY%" %SSH_USER%@54.159.204.82 "echo Connected" || (echo SSH connection failed && exit /b 1)

                    echo Copying JAR file...
                    C:\\Windows\\System32\\OpenSSH\\scp.exe -o ConnectTimeout=30 -o StrictHostKeyChecking=no -i "%SSH_KEY%" target\\HonorsProject-0.0.1-SNAPSHOT.jar %SSH_USER%@54.159.204.82:/home/%SSH_USER%/

                    echo Deploying application...
                    C:\\Windows\\System32\\OpenSSH\\ssh.exe -o ConnectTimeout=30 -o StrictHostKeyChecking=no -i "%SSH_KEY%" %SSH_USER%@54.159.204.82 "
                        echo Stopping any running Java processes...
                        pkill -f 'java -jar' || true;
                        sleep 5;

                        echo Starting the application...
                        nohup java -jar /home/%SSH_USER%/HonorsProject-0.0.1-SNAPSHOT.jar > /home/%SSH_USER%/app.log 2>&1 &
                        pid=$!;

                        echo Waiting for the application to start...
                        sleep 30;

                        echo Checking if the application is running...
                        for i in {1..5}; do
                            curl -f http://localhost:8081/hello && break || sleep 5;
                        done || (echo App failed to start && tail /home/%SSH_USER%/app.log && exit 1);

                        echo Application started successfully.
                    "
                    echo Deployment finished at %DATE% && time /t
                    """
                }
            }
        }

        stage('Verify Deployment') {
            steps {
                script {
                    echo "Verifying application is running at %DATE% && time /t"
                    retry(3) {
                        script {
                            bat """
                            ping 127.0.0.1 -n 6 > nul
                            powershell -Command "Invoke-WebRequest -Uri http://54.159.204.82:8081/hello -Method GET -UseBasicParsing -TimeoutSec 30 | Select-Object -ExpandProperty Content"
                            """
                        }
                    }
                    echo "Application verified at %DATE% && time /t"
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