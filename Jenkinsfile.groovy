pipeline {
    agent any
    stages {
        stage('Clean workspace') {
            steps {
                cleanWs()
                git 'https://github.com/Shanawar99/zinougeh.git'
            }
        }

        stage('Terraform to Spin New EC2 server') {
            steps {
                dir('zinougeh/task1/terraform') {
                    sh 'terraform init'
                    sh 'terraform apply -auto-approve'
                    script {
                        env.EC2_PUBLIC_IP = sh(script: 'terraform output public_ip', returnStdout: true).trim()
                    }
                }
            }
        }

        stage('Ansible to Install MicroK8s') {
            steps {
                dir('zinougeh/task1/microk8s/') {
                    writeFile file: 'inventory.ini', text: "${env.EC2_PUBLIC_IP},"
                    ansiblePlaybook(
                        credentialsId: 'ubuntu',
                        inventory: 'inventory.ini',
                        playbook: 'setup_microk8s.yml',
                        hostKeyChecking: 'false'
                    )
                }
            }
        }

        stage('Deploy SonarQube using Helm on MicroK8s') {
            steps {
                dir('zinougeh/task1/sonar/') {
                    sshagent(credentials: ['ubuntu']) {
                        sh "scp -o StrictHostKeyChecking=no  -r sonarqube ubuntu@${env.EC2_PUBLIC_IP}:/tmp"
                        sh """ssh -o StrictHostKeyChecking=no  ubuntu@${env.EC2_PUBLIC_IP} 'sudo microk8s kubectl apply -f /tmp/sonarqube'
                        """
                    }
                }
            }
        }
    }
}

stage('Deploy SonarQube on MicroK8s') {
    steps {
        dir('zinougeh/task1/sonar/') {
            sshagent(credentials: ['jenkins']) {
                sh "scp -o StrictHostKeyChecking=no  -r sonarqube ubuntu@${env.EC2_PUBLIC_IP}:/tmp"
                sh """ssh -o StrictHostKeyChecking=no  ubuntu@${env.EC2_PUBLIC_IP} 'sudo microk8s kubectl apply -f /tmp/sonarqube/deployment.yml'
                      ssh -o StrictHostKeyChecking=no  ubuntu@${env.EC2_PUBLIC_IP} 'sudo microk8s kubectl apply -f /tmp/sonarqube/service.yml'
                      ssh -o StrictHostKeyChecking=no  ubuntu@${env.EC2_PUBLIC_IP} 'sudo microk8s kubectl apply -f /tmp/sonarqube/ingress.yml'
                """
            }
        }
    }
}

stage('Deploy SonarQube on MicroK8s') {
    steps {
        dir('zinougeh/task1/sonar/') {
            sshagent(credentials: ['jenkins']) {
                sh "scp -o StrictHostKeyChecking=no  -r sonarqube ubuntu@${env.EC2_PUBLIC_IP}:/tmp"
                sh """ssh -o StrictHostKeyChecking=no  ubuntu@${env.EC2_PUBLIC_IP} 'sudo microk8s kubectl apply -f /tmp/sonarqube/deployment.yml'
                      ssh -o StrictHostKeyChecking=no  ubuntu@${env.EC2_PUBLIC_IP} 'sudo microk8s kubectl apply -f /tmp/sonarqube/service.yml'
                      ssh -o StrictHostKeyChecking=no  ubuntu@${env.EC2_PUBLIC_IP} 'sudo microk8s kubectl apply -f /tmp/sonarqube/ingress.yml'
                """
            }
        }
    }
}
