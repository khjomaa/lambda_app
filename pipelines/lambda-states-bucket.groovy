def label = "aws-jenkins-${UUID.randomUUID().toString()}"
def gitRepoUrl = "https://github.com/khjomaa/lambda_app.git"

podTemplate(label: label,
        containers: [
                containerTemplate(name: 'jnlp', image: 'jenkins/jnlp-slave:alpine'),
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

        stage('Create S3 bucket for states') {
            container('terraform') {
                withAWS(credentials: 'aws-credentials', region: 'us-east-1') {
                    sh 'terraform init terraform/projects/terraform-states'
                    sh 'terraform plan terraform/projects/terraform-states'
                    sh 'terraform apply --auto-approve terraform/projects/terraform-states'
                }
            }
        }
    }
}