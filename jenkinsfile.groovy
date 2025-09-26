pipeline {
    agent any

    stages {
        stage('Get Code') { // must install git or gitbitbucket
            steps {
                git changelog: false, poll: false, url: 'https://github.com/Shady1997/ShadowBankAPIAutomation_RestAssured.git'
            }
        }

        stage('Build Code') {
            steps {
                bat 'mvn clean compile'
            }
        }

        stage('Run Test') {
            steps {
                bat 'mvn clean test'
            }
        }

        stage('Publish Report') {
            steps {// must install HTML Publisher Plugin
                publishHTML([
                        allowMissing: false,
                        alwaysLinkToLastBuild: true,
                        keepAll: true,
                        reportDir: 'target/surefire-reports',
                        reportFiles: 'emailable-report.html',
                        reportName: 'Automation Exercise Report'
                ])
            }
        }
    }
}
