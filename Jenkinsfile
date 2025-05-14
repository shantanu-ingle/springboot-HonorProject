pipeline {
    agent any

    stages {
        stage('Checkout') {
            steps {
                cleanWs() // Clean the workspace before starting
                checkout scm // Checkout code from your SCM (e.g., Git)
            }
        }

        stage('Build') {
            steps {
                bat 'mvn clean package' // Compile and package the app
            }
        }

        stage('Test') {
            steps {
                bat 'mvn test' // Run unit tests
            }
        }

        stage('Deploy') {
            steps {
                sshPublisher(
                    publishers: [
                        sshPublisherDesc(
                            configName: 'AWS_EC2', // Name of your SSH server config in Jenkins
                            transfers: [
                                sshTransfer(
                                    sourceFiles: 'target/*.jar', // Files to transfer
                                    remoteDirectory: '/home/ec2-user/app', // Destination on EC2
                                    execCommand: 'pkill -f "java -jar" || true; nohup java -jar /home/ec2-user/app/*.jar &' // Restart app
                                )
                            ]
                        )
                    ]
                )
            }
        }
    }

    post {
        always {
            archiveArtifacts 'target/*.jar' // Archive the JAR file
            junit 'target/surefire-reports/*.xml' // Publish test results
        }
    }
}