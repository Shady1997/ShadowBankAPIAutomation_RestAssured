pipeline {
    agent any

    environment {
        MAVEN_HOME = '/opt/maven/apache-maven-3.9.0'  // Adjust path to actual install
        JAVA_HOME = '/usr/lib/jvm/java-17-openjdk'    // Adjust to where JDK 17 is installed
        PATH = "${JAVA_HOME}/bin:${MAVEN_HOME}/bin:${env.PATH}"
    }

    parameters {
        choice(
                name: 'ENVIRONMENT',
                choices: ['test', 'staging', 'production'],
                description: 'Environment to run tests against'
        )
        choice(
                name: 'TEST_SUITE',
                choices: [
                        'testng.xml',
                        'smoke-suite.xml',
                        'regression-suite.xml',
                        'user-api-suite.xml',
                        'account-api-suite.xml',
                        'transaction-api-suite.xml',
                        'e2e-suite.xml'
                ],
                description: 'Test suite to execute'
        )
        choice(
                name: 'PARALLEL_EXECUTION',
                choices: ['methods', 'classes', 'tests', 'none'],
                description: 'Parallel execution type'
        )
        string(
                name: 'THREAD_COUNT',
                defaultValue: '3',
                description: 'Number of parallel threads'
        )
        booleanParam(
                name: 'SKIP_TESTS',
                defaultValue: false,
                description: 'Skip test execution (only generate reports)'
        )
    }

    environment {
        MAVEN_HOME = tool 'Maven-3.9.0'
        JAVA_HOME = tool 'JDK-17'
        PATH = "${JAVA_HOME}/bin:${MAVEN_HOME}/bin:${env.PATH}"
    }

    stages {
        stage('Checkout') {
            steps {
                echo 'Checking out source code...'
                checkout scm
            }
        }

        stage('Setup') {
            steps {
                echo 'Setting up environment and dependencies...'
                sh '''
                    mkdir -p logs
                    mkdir -p test-output
                    mkdir -p target/allure-results
                '''
            }
        }

        stage('Compile') {
            steps {
                echo 'Compiling project...'
                sh 'mvn clean compile'
            }
        }

        stage('Run Tests') {
            when {
                expression { !params.SKIP_TESTS }
            }
            steps {
                echo "Running tests on ${params.ENVIRONMENT} environment with suite ${params.TEST_SUITE}"
                script {
                    def mavenCommand = "mvn test -Denv=${params.ENVIRONMENT} -DsuiteXmlFile=${params.TEST_SUITE}"

                    if (params.PARALLEL_EXECUTION != 'none') {
                        mavenCommand += " -Dparallel=${params.PARALLEL_EXECUTION} -DthreadCount=${params.THREAD_COUNT}"
                    }

                    sh mavenCommand
                }
            }
            post {
                always {
                    echo 'Collecting test results...'
                    junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml'
                }
            }
        }

        stage('Generate Allure Report') {
            steps {
                echo 'Generating Allure report...'
                sh 'mvn allure:report'
            }
        }

        stage('Archive Artifacts') {
            steps {
                echo 'Archiving test artifacts...'
                archiveArtifacts artifacts: 'target/surefire-reports/**/*', fingerprint: true, allowEmptyArchive: true
                archiveArtifacts artifacts: 'test-output/**/*', fingerprint: true, allowEmptyArchive: true
                archiveArtifacts artifacts: 'logs/**/*', fingerprint: true, allowEmptyArchive: true
                archiveArtifacts artifacts: 'target/site/allure-maven-plugin/**/*', fingerprint: true, allowEmptyArchive: true
            }
        }
    }

    post {
        always {
            echo 'Publishing test reports...'

            // ✅ Publish JUnit/TestNG results
            junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml'

            // ✅ Publish Allure reports
            allure([
                    includeProperties: false,
                    jdk              : '',
                    properties       : [],
                    reportBuildPolicy: 'ALWAYS',
                    results          : [[path: 'target/allure-results']]
            ])

            // ✅ Publish ExtentReports (HTML)
            publishHTML([
                    allowMissing         : false,
                    alwaysLinkToLastBuild: true,
                    keepAll              : true,
                    reportDir            : 'test-output',
                    reportFiles          : '*.html',
                    reportName           : 'ExtentReports'
            ])
        }

        success {
            echo 'Pipeline completed successfully!'
            emailext(
                    subject: "✅ Shadow Bank API Tests - SUCCESS - Build #${BUILD_NUMBER}",
                    body: """
                    <h3>Banking API Test Results</h3>
                    <p><strong>Environment:</strong> ${params.ENVIRONMENT}</p>
                    <p><strong>Test Suite:</strong> ${params.TEST_SUITE}</p>
                    <p><strong>Build Number:</strong> ${BUILD_NUMBER}</p>
                    <p><strong>Status:</strong> <span style="color: green;">SUCCESS</span></p>
                    <p><strong>Reports:</strong></p>
                    <ul>
                        <li><a href="${BUILD_URL}allure/">Allure Report</a></li>
                        <li><a href="${BUILD_URL}ExtentReports/">ExtentReports</a></li>
                        <li><a href="${BUILD_URL}testReport/">TestNG Results</a></li>
                    </ul>
                """,
                    mimeType: 'text/html',
                    to: 'shadyahmed.n8n@gmail.com'
            )
        }

        failure {
            echo 'Pipeline failed!'
            emailext(
                    subject: "❌ Shadow Bank API Tests - FAILED - Build #${BUILD_NUMBER}",
                    body: """
                    <h3>Banking API Test Results</h3>
                    <p><strong>Environment:</strong> ${params.ENVIRONMENT}</p>
                    <p><strong>Test Suite:</strong> ${params.TEST_SUITE}</p>
                    <p><strong>Build Number:</strong> ${BUILD_NUMBER}</p>
                    <p><strong>Status:</strong> <span style="color: red;">FAILED</span></p>
                    <p><strong>Reports:</strong></p>
                    <ul>
                        <li><a href="${BUILD_URL}allure/">Allure Report</a></li>
                        <li><a href="${BUILD_URL}ExtentReports/">ExtentReports</a></li>
                        <li><a href="${BUILD_URL}testReport/">TestNG Results</a></li>
                        <li><a href="${BUILD_URL}console">Console Log</a></li>
                    </ul>
                """,
                    mimeType: 'text/html',
                    to: 'shadyahmed.n8n@gmail.com'
            )
        }

        cleanup {
            echo 'Cleaning up workspace...'
            cleanWs()
        }
    }
}
