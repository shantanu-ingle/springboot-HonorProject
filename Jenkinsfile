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
                sshPublisher(
                    publishers: [
                        sshPublisherDesc(
                            configName: 'my-ec2-server',
                            transfers: [
                                sshTransfer(
                                    sourceFiles: 'target/your-app.jar',
                                    removePrefix: 'target/',
                                    remoteDirectory: '/home/ec2-user',
                                    execCommand: 'pkill -f your-app.jar || true; nohup java -jar /home/ec2-user/your-app.jar &'
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
            archiveArtifacts 'target/*.jar'
            junit 'target/surefire-reports/*.xml'
        }
    }
}