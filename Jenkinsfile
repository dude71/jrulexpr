pipeline {
    agent any
    options {
        // Timeout counter starts AFTER agent is allocated
        timeout(time: 120, unit: 'SECONDS')
    }
    tools {
        maven 'maven'
    }
    environment {
        OH_CONFIG     = 'hub.ha'
    }  
    stages {
        stage('Build') {
            steps {
                sh 'mvn clean package -DskipTests'
            }
        }
        stage('Test') {
            steps {
                sh 'mvn test'
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                }
            }
        }
        stage('Deploy') {           
          steps {
            script {
                def remote = [:]
                remote.name = 'hub.ha'
                remote.host = 'hub.ha'
                remote.allowAnyHosts = true

                withCredentials([sshUserPrivateKey(credentialsId: 'd71', 
                                                keyFileVariable: 'keyPath', 
                                                usernameVariable: 'remoteUser')]) {
                    remote.user = remoteUser
                    remote.identityFile = keyPath
                    sshCommand remote: remote, command: "rm -f /opt/oh/ext-lib/jrulexpr-*-lib.jar"
                    sshCommand remote: remote, command: "rm -f /opt/oh/rules-jar/jrulexpr-*-rulegen.jar"
                }                
            }
            sshPublisher(
              continueOnError: false,
              failOnError: true,
              publishers: [
                sshPublisherDesc(
                  configName: "${OH_CONFIG}",
                  transfers: [
                    sshTransfer(sourceFiles: 'target/jrulexpr-*-lib.jar', removePrefix: 'target', remoteDirectory: '/opt/oh/ext-lib/'),
                    sshTransfer(sourceFiles: 'target/jrulexpr-*-rulegen.jar', removePrefix: 'target', remoteDirectory: '/opt/oh/rules-jar/')
                  ],
                  verbose: true
                )
              ]
            )
          }
        }
    }
}