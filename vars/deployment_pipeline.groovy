def call(Map pipelineParams) {

    pipeline {
        agent any
        stages {
            stage('compile') {
                when {
                    expression { return fileExists('Dockerfile.compile') }
                }
                steps {
                    echo '................................compile stage................................'
                    sh "mkdir ${BUILD_NUMBER}"
                    sh "docker build -t ${pipelineParams.NAME}:compile -f Dockerfile.compile ."
                    sh "docker run -u root --rm -v ${WORKSPACE}:${pipelineParams.srcPath} -v ${WORKSPACE}/${BUILD_NUMBER}:${pipelineParams.binPath} ${pipelineParams.NAME}:compile"
                }
            }
            
            stage('build') {
                steps {
                    echo '................................build stage................................'
                    sh "docker build -t ${pipelineParams.NAME}:build-${BUILD_NUMBER} --build-arg BUILD_NUM=${BUILD_NUMBER} ."
                }
            }
            
            stage('pre deploy') {
                steps {
                    echo '................................pre deploy cleanup................................'
                    sh "docker stop ${pipelineParams.NAME} || echo 'nothing to do'"
                }
            }
            
            stage('deploy staging') {
                steps {
                    echo '................................deploy staging................................'
                    script {
                        if (pipelineParams.PORT) {
                            echo 'got port'
                        } else {
                            echo 'no port'
                        }
                    }
                }
            }
            
        }
        post {
            always {
                echo '................................clean workspace................................'
                deleteDir()
            }
        }
    }
}