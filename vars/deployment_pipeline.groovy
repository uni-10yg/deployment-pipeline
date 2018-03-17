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
                    sh "docker build -t ${pipelineParams.name}:compile -f Dockerfile.compile ."
                    sh "docker run -u root --rm -v ${WORKSPACE}:${pipelineParams.srcPath} -v ${WORKSPACE}/${BUILD_NUMBER}:${pipelineParams.binPath} ${pipelineParams.name}:compile"
                }
            }
            
            stage('build') {
                steps {
                    echo '................................build stage................................'
                    sh "docker build -t ${pipelineParams.name}:build-${BUILD_NUMBER} --build-arg BUILD_NUM=${BUILD_NUMBER} ."
                }
            }
            
            stage('pre deploy') {
                steps {
                    echo '................................pre deploy cleanup................................'
                    sh "docker stop ${pipelineParams.name} || echo 'nothing to do'"
                }
            }
            
            stage('deploy staging') {
                steps {
                    echo '................................deploy staging................................'
                    script {
                        if (pipelineParams.port) {
                            sh (
                                script: "docker run -d --rm -p ${pipelineParams.port}:${pipelineParams.port} --network staging --name ${pipelineParams.name} ${pipelineParams.name}:build-${BUILD_NUMBER}"
                            )
                        } else {
                            sh (
                                script: "docker run -d --rm --network staging --name ${pipelineParams.name} ${pipelineParams.name}:build-${BUILD_NUMBER}"
                            )
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