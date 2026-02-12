pipeline {
    agent any
    options {
        // Timeout counter starts AFTER agent is allocated
        timeout(time: 120, unit: 'SECONDS')
    }
    tools {
        maven 'maven'
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
            sshPublisher(
              continueOnError: false,
              failOnError: true,
              publishers: [
                sshPublisherDesc(
                  configName: "hub.ha",
                  transfers: [
                    sshTransfer(sourceFiles: 'target/jrulexpr-1.0-SNAPSHOT-lib.jar', removePrefix: 'target', remoteDirectory: '/etc/openhab5/automation/jrule/ext-lib/'),
                    sshTransfer(sourceFiles: 'target/jrulexpr-1.0-SNAPSHOT-rulegen.jar', removePrefix: 'target', remoteDirectory: '/etc/openhab5/automation/jrule/rules-jar/')
                ],
                  verbose: true
                )
              ]
            )
          }
        }
    }
}
