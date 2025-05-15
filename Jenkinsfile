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
                sh """
                docker run -v \$(pwd):/workspace maven:3.9.9-eclipse-temurin-21 /bin/sh -c "cd /workspace && mvn clean package"
                """
            }
        }

        stage('Test') {
            steps {
                sh """
                docker run -v \$(pwd):/workspace maven:3.9.9-eclipse-temurin-21 /bin/sh -c "cd /workspace && mvn test"
                """
            }
        }

        stage('Deploy') {
            steps {
                retry(2) {
                    timeout(time: 3, unit: 'MINUTES') {
                        withCredentials([sshUserPrivateKey(credentialsId: 'ec2-ssh-key', keyFileVariable: 'SSH_KEY', usernameVariable: 'SSH_USER')]) {
                            sh """
                            echo "Starting deployment at \$(date)"
                            # Test SSH connectivity before deployment
                            echo "Testing SSH connection..."
                            ssh -i \$SSH_KEY \$SSH_USER@54.159.204.82 "echo 'SSH connection successful'" || { echo "SSH connection failed"; exit 1; }
                            echo "SSH connection established at \$(date)"

                            # Compress the JAR to reduce transfer size
                            echo "Compressing JAR at \$(date)"
                            gzip target/HonorsProject-0.0.1-SNAPSHOT.jar
                            echo "Compression finished at \$(date)"

                            # Transfer and execute commands in a single SSH session
                            echo "Starting file transfer at \$(date)"
                            ssh -i \$SSH_KEY \$SSH_USER@54.159.204.82 << 'EOF'
                                # Receive the compressed JAR
                                cat > /home/\$SSH_USER/HonorsProject-0.0.1-SNAPSHOT.jar.gz

                                # Decompress the JAR
                                gunzip -f /home/\$SSH_USER/HonorsProject-0.0.1-SNAPSHOT.jar.gz

                                # Stop any running instance and start the new one
                                pkill -f 'java -jar' || true
                                nohup java -jar /home/\$SSH_USER/HonorsProject-0.0.1-SNAPSHOT.jar &
                            EOF
                            echo "File transfer and deployment finished at \$(date)"

                            # Clean up the compressed file
                            rm target/HonorsProject-0.0.1-SNAPSHOT.jar.gz
                            echo "Deployment finished at \$(date)"
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
    }
}