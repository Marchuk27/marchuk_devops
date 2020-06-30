pipeline {
    agent{node('master')}
    stages {
        stage('Clean workspace & dowload dist') {
            steps {
                script {
                    cleanWs()
                    withCredentials([
                        usernamePassword(credentialsId: 'srv_sudo',
                        usernameVariable: 'username',
                        passwordVariable: 'password')
                    ]) {
                        try {
                            sh "echo '${password}' | sudo -S docker stop marchuk_nginx"
                            sh "echo '${password}' | sudo -S docker container rm marchuk_nginx"
                        } catch (Exception e) {
                            print 'container does not exist, skip clean'
                        }
                    }
                }
                script {
                    echo 'Update from repository'
                    checkout([$class                           : 'GitSCM',
                              branches                         : [[name: '*/master']],
                              doGenerateSubmoduleConfigurations: false,
                              extensions                       : [[$class           : 'RelativeTargetDirectory',
                                                                   relativeTargetDir: 'auto']],
                              submoduleCfg                     : [],
                              userRemoteConfigs                : [[credentialsId: 'MarchukGit', url: 'https://github.com/Marchuk27/marchuk_devops.git']]])
                }
            }
        }
        stage ('Build & run docker image') {
            steps{
                script{
                     withCredentials([
                        usernamePassword(credentialsId: 'srv_sudo',
                        usernameVariable: 'username',
                        passwordVariable: 'password')
                    ]) {
                        sh "echo '${password}' | sudo -S docker build ${WORKSPACE}/auto -t marchuk_nginx"
                        currentBuild.result = 'failed'
                        sh "echo '${password}' | sudo -S docker run -d -p 2703:80 --name marchuk_nginx -v /home/adminci/is_mount_dir:/stat marchuk_nginx"
                    }
                }
            }
        }
        
        stage ('Get stats & write to file') {
            steps{
                script{
                    withCredentials([
                        usernamePassword(credentialsId: 'srv_sudo',
                        usernameVariable: 'username',
                        passwordVariable: 'password')
                    ]) {
                        sh "echo '${password}' | sudo -S docker exec -t marchuk_nginx bash -c 'df -h > /stat/statinfo.txt'"
                        sh "echo '${password}' | sudo -S docker exec -t marchuk_nginx bash -c 'top -n 1 -b >> /stat/statinfo.txt'"
                    }
                }
            }
        }
        
        stage ('Stopim & ronyaem'){
            steps{
                script{
                     withCredentials([
                        usernamePassword(credentialsId: 'srv_sudo',
                        usernameVariable: 'username',
                        passwordVariable: 'password')
                    ]) {
                        try {
                            sh "echo '${password}' | sudo -S docker stop marchuk_nginx"
                        } catch (Exception e) {
                            print 'container does not exist'
                        }
                    }
                }
            }
        }
    }   
}
