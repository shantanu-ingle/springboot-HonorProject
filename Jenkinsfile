pipeline {
       agent any

       stages {
           stage('Checkout') {
               steps {
                   echo "Starting Checkout stage at %DATE% && time /t"
                   cleanWs()
                   checkout scm
                   echo "Checkout stage completed at %DATE% && time /t"
               }
           }

           stage('Build') {
               steps {
                   echo "Starting Build stage at %DATE% && time /t"
                   bat """
                   docker run -v "%CD%":/workspace maven:3.9.9-eclipse-temurin-21 /bin/sh -c "cd /workspace && mvn clean package"
                   """
                   echo "Build stage completed at %DATE% && time /t"
               }
           }

           stage('Test') {
               steps {
                   echo "Starting Test stage at %DATE% && time /t"
                   bat """
                   docker run -v "%CD%":/workspace maven:3.9.9-eclipse-temurin-21 /bin/sh -c "cd /workspace && mvn test"
                   """
                   echo "Test stage completed at %DATE% && time /t"
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
                       echo SSH connection established at %DATE% && time /t

                       echo Copying JAR file...
                       C:\\Windows\\System32\\OpenSSH\\scp.exe -o ConnectTimeout=30 -o StrictHostKeyChecking=no -i "%SSH_KEY%" target\\HonorsProject-0.0.1-SNAPSHOT.jar %SSH_USER%@54.159.204.82:/home/%SSH_USER%/
                       echo JAR file copied at %DATE% && time /t

                       echo Deploying application...
                       C:\\Windows\\System32\\OpenSSH\\ssh.exe -o ConnectTimeout=30 -o StrictHostKeyChecking=no -i "%SSH_KEY%" %SSH_USER%@54.159.204.82 "pkill -f 'java -jar' || true; nohup java -jar /home/%SSH_USER%/HonorsProject-0.0.1-SNAPSHOT.jar &"
                       echo Deployment finished at %DATE% && time /t
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