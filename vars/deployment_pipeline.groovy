def call(Map pipelineParams) {

    pipeline {
        agent any
        stages {
            stage('compile') {
                when {
                    expression { return fileExists('Dockerfile.compile') }
                }
                steps {
                    echo 'compile stage...'
                    sh "mkdir ${BUILD_NUMBER}"
                    sh "docker build -t ${pipelineParams.NAME}:compile -f Dockerfile.compile ."
                    sh "docker run -u root --rm -v \"$PWD\":${pipelineParams.srcPath} -v \"$PWD\"/${BUILD_NUMBER}:${pipelineParams.binPath} ${pipelineParams.NAME}:compile"
                }
            }
            
            stage('build') {
                steps {
                    echo 'build stage...'
                    sh "docker build -t ${pipelineParams.NAME}:latest --build-arg BUILD_NUM=${BUILD_NUMBER} --build-arg WORKSPACE=${WORKSPACE} ."
                }
            }

            stage('deploy staging'){
                steps {
                    echo 'deploy staging...'
                }
            }
        }
    }
}