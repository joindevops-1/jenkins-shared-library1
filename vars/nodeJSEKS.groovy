def call(Map configMap){
    pipeline {
        agent {
            label 'AGENT-1'
        }

        options {
            timeout(time: 10, unit: 'MINUTES') 
            disableConcurrentBuilds()
        }
        parameters{
            booleanParam(name: 'DEPLOY', defaultValue: false, description: 'Toggle this value')
        }

        environment { 
            appVersion = ''
            account_id = pipelineGlobals.dev_account_id()
            region = 'us-east-1'
            environment = "dev"
            component = configMap.get("component")
            project = configMap.get("project")
        }
        
        stages {
            stage('Read Version') {
                steps {
                    script{
                        def packageJson = readJSON file: 'package.json'
                        appVersion = packageJson.version
                        echo "application version: $appVersion"
                    }
                }
            }
            stage('Install Dependencies') {
                steps {
                sh """
                    npm install
                    ls -ltr
                    echo "application version: $appVersion"
                """
                }
            }
            stage('Build'){
                steps{
                    sh """
                    zip -q -r backend-${appVersion}.zip * -x Jenkinsfile -x backend-${appVersion}.zip
                    ls -ltr
                    """
                }
            }
            stage('Unit Test'){
                steps{
                    sh """
                    npm test
                    ls -ltr

                    """
                }
            /*  post {
                    always {
                        junit 'junit.xml'
                    }
                } */
            }
            /* stage('Sonar Scan'){
                environment {
                    scannerHome = tool 'sonar' //referring scanner CLI
                }
                steps {
                    script {
                        withSonarQubeEnv('sonar') { //referring sonar server
                            sh "${scannerHome}/bin/sonar-scanner"
                        }
                    }
                }
            }
            stage("Quality Gate") {
                steps {
                  timeout(time: 30, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                  }
                }
            } */ 
            stage('Docker build'){
                
                steps{
                    withAWS(credentials: 'aws-creds', region: 'us-east-1') {
                        sh """
                            aws ecr get-login-password --region ${region} | docker login --username AWS --password-stdin ${account_id}.dkr.ecr.${region}.amazonaws.com

                            docker build -t ${account_id}.dkr.ecr.${region}.amazonaws.com/expense/${environment}/backend:${appVersion} .

                            docker push ${account_id}.dkr.ecr.${region}.amazonaws.com/expense/${environment}/backend:${appVersion}
                        """
                    }
                }
            }
            stage('Deploy'){
                when {
                    expression { params.DEPLOY }
                }
                steps{
                    withAWS(credentials: 'aws-creds', region: 'us-east-1') {
                        script{               
                            build job: '../backend-deploy', parameters: [
                                string(name: 'ENVIRONMENT', value: "dev"),
                                string(name: 'VERSION', value: "$appVersion")
                            ], wait: true
                        }
                    }
                }
            }
        }
        post { 
            always { 
                echo 'I will always say Hello again!'
                deleteDir()
            }
            success { 
                echo 'I will run when pipeline is success'
            }
            failure { 
                echo 'I will run when pipeline is failure'
            }
        }
    }
}