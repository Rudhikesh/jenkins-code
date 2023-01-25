pipeline {
    agent any
    stages {
        stage("git-pull") {
            steps { 
                sh 'sudo apt-get update -y'
                sh 'sudo apt-get install git -y'
                git credentialsId: 'ubuntu', url: 'git@github.com:Rudhikesh/student-ui.git'
            }
        }
        stage("build-maven") {
            steps { 
                sh 'sudo apt-get update -y'
                sh 'sudo apt-get install maven curl unzip -y'
                sh 'mvn clean package'
            }
        }
        stage("build-artifacts") {
            steps { 
                //sh 'curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"'
                //sh 'unzip awscliv2.zip'
                //sh 'sudo ./aws/install'
                sh 'aws s3 mb s3://rrkkggbckt'
                sh 'aws s3 cp **/*.war s3://rrkkggbckt/student-${BUILD_ID}.war'
            }
        }
        stage("tomcat-build") {
            steps { 
                withCredentials([sshUserPrivateKey(credentialsId: 'tomcat-key', keyFileVariable: 'tomcat', usernameVariable: 'ubuntu')]) {
                sh '''
                ssh -i ${tomcat-key} -o StrictHostKeyChecking=no ubuntu@18.183.239.236<<EOF
                sudo apt-get update -y
                sudo apt install unzip -y
                #curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"
                #sudo unzip awscliv2.zip
                #sudo ./aws/install
                aws s3 cp s3://rrkkggbckt/student-${BUILD_ID}.war .
                curl -O https://dlcdn.apache.org/tomcat/tomcat-8/v8.5.85/bin/apache-tomcat-8.5.85.tar.gz
                sudo tar -xvf apache-tomcat-8.5.85.tar.gz -C /opt/
                sudo sh /opt/apache-tomcat-8.5.85/bin/shutdown.sh
                sudo cp -rv student-${BUILD_ID}.war studentapp.war
                sudo cp -rv studentapp.war /opt/apache-tomcat-8.5.85/webapps/
                sudo sh /opt/apache-tomcat-8.5.85/bin/startup.sh
                '''
                }
            }
        }     
    }
}