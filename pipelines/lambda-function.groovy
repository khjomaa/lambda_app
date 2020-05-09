//properties([pipelineTriggers([pollSCM('*/5 * * * *')])])

def label = "aws-jenkins-${UUID.randomUUID().toString()}"
def gitRepoUrl = "https://github.com/khjomaa/lambda_app.git"

podTemplate(label: label,
        containers: [
                containerTemplate(name: 'jnlp', image: 'jenkins/jnlp-slave:alpine'),
                containerTemplate(name: 'zip', image: 'kramos/alpine-zip', command: 'cat', ttyEnabled: true ),
                containerTemplate(name: 'terraform', image: 'hashicorp/terraform', command: 'cat', ttyEnabled: true)
        ],
        volumes: [
                hostPathVolume(hostPath: '/var/run/docker.sock', mountPath: '/var/run/docker.sock'),
        ],
) {
    node(label) {
        stage('Checkout Repo') {
            git gitRepoUrl
        }

        stage('Zipping app.py file') {
            container('zip') {
                sh 'zip -r app.zip app.py'
            }
        }

        stage('Run lambda-s3 project') {
            container('terraform') {
                withAWS(credentials: 'aws-credentials', region: 'us-east-1') {
                    sh 'terraform init terraform/projects/lambda-s3'
                    sh 'terraform plan terraform/projects/lambda-s3'
                    sh 'terraform apply --auto-approve terraform/projects/lambda-s3'
                }
            }
        }
    }
}