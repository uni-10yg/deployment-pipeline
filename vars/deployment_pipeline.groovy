def call(Map pipelineParams) {

    pipeline {
        agent any
        stages {
            stage('checkout git') {
                steps {
                    git branch: pipelineParams.branch, credentialsId: 'GitCredentials', url: pipelineParams.scmUrl
                }
            }

            stage('compile') {
                when {
                    expression { return fileExists('Dockerfile.compile') }
                }
                steps {
                    echo 'compile stage...'
                    sh 'mkdir ${BUILD_NUMBER}'
                    sh 'docker build -t ${pipelineParams.NAME}:compile -f Dockerfile.compile .'
                    sh 'docker run -u root --rm -v ${WORKSPACE}:${pipelineParams.srcPath} -v ${BUILD_NUMBER}:${pipelineParams.binPath} ${pipelineParams.NAME}:compile'
                }
            }

        }
    }
}