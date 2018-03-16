def call(Map pipelineParams) {

    pipeline {
        agent none
        stages {
            stage('checkout git') {
                steps {
                    git branch: pipelineParams.branch, credentialsId: 'GitCredentials', url: pipelineParams.scmUrl
                }
            }

            stage('compiler build') {
                agent {
                    dockerfile {
                        filename 'Dockerfile.build'
                        additionalBuildArgs '--build-arg J_BUILD_TAG=${BUILD_TAG} --build-arg J_WORKSPACE=${WORKSPACE}'
                    }
                }
                when {
                    beforeAgent true
                    expression { return pipelineParams.COMP }
                }
                steps {
                    echo 'compiler build'
                }
            }
            
            stage('standard build') {
                agent any
                steps {
                    echo 'standard build'
                    sh 'docker build -t ${pipelineParams.NAME}:latest --build-arg J_BUILD_TAG=${BUILD_TAG} --build-arg J_WORKSPACE=${WORKSPACE} .'
                }
            }

            stage('deploy staging'){
                agent any
                steps {
                    echo 'deploy staging'
                }
            }

        }
    }
}