#!groovy

    boolean buildImages = false
    def targetEnv = ""
    def deploymentType = ""
    boolean clearImages = true
    boolean cleanAks = false
    def branch = env.GIT_BRANCH?.trim().split('/').last().toLowerCase() //master
    def overrideVersion = params.BUILD_VERSION_OVERRIDE?.trim()
    boolean override = false
    def servicePrincipalId = '72555f61-7a9f-4145-8bb7-a163f107bccf'
    def currentEnvironment = 'blue'
    def newEnvironment = { ->
        currentEnvironment == 'blue' ? 'green' : 'blue'
    }
    boolean setupDns = false

node {

    properties([disableConcurrentBuilds()])

    try {

        env.BUILD_VERSION = "1.0.0.${env.BUILD_ID}"
        env.BUILD_LABEL = params.BUILD_LABEL?.trim()
        buildImages = params.BUILD_IMAGES
        targetEnv = params.TARGET_ENV?.trim()
        deploymentType = params.TARGET_ROLE?.trim()
        clearImages = params.CLEAR_IMAGES
        cleanAks = params.CLEAN_AKS        

        // Check if the build label is set
        if (buildImages) {
            if (!env.BUILD_LABEL) {
                error("Build label must be specified!: build label: ${env.BUILD_LABEL}")
            }
        // Check if this is an overide play
        } else if (overrideVersion != "1.0.0.0") {    
            env.BUILD_VERSION = overrideVersion
            buildImages = false
            override = true
        }

        //env.BUILD_LABEL = env.BUILD_LABEL + ':' + env.BUILD_VERSION

        switch(branch){
            case 'development':
                env.TARGET_ROLE = 'blue'
                env.TARGET_PORT = '8080'
            break
            case 'master':
                env.TARGET_ROLE = 'green'
                env.TARGET_PORT = '80'
            break
            default:
                echo "branch is neither development or master, deploying to BLUE type"
                env.TARGET_ROLE = 'blue'
                env.TARGET_PORT = "${TARGET_PORT_MAN}"
        }

        echo """Parameters:
            branch: '${branch}' 
            BUILD LABEL: '$env.BUILD_LABEL'
            BUILD VERSION: '$env.BUILD_VERSION'
            buildImages: '${buildImages}'
            targetEnv: '${targetEnv}'
            clearImages: '${clearImages}'
            deploymentType: '${deploymentType}'
            cleanAks: '${cleanAks}'
            REPLICAS NO: '$env.REPLICAS_NO'
            TARGET_ROLE: '$env.TARGET_ROLE'
            overrideVersion: '${overrideVersion}'
            TARGET PORT: '${env.TARGET_PORT}'
        """

        if(cleanAks) {
            withCredentials([azureServicePrincipal('72555f61-7a9f-4145-8bb7-a163f107bccf')]) {
                // Login to azure
                sh 'az login --service-principal -u $AZURE_CLIENT_ID -p $AZURE_CLIENT_SECRET -t $AZURE_TENANT_ID'
                // Set subscription context
                sh 'az account set -s $AZURE_SUBSCRIPTION_ID'
                // Set aks context
                sh 'az aks get-credentials --overwrite-existing --resource-group \"mscdevops-aks-rg\" --name \"mscdevops-aks\"'

                // Navigate to ws-service deployment directory
                dir('aks/backend'){
                // Deploy the service
                sh "kubectl delete deployment ws-service-${env.TARGET_ROLE}"
                sh "kubectl delete services svc-ws-service-${env.TARGET_ROLE}"
                }
            }
            return 0
        }
        
        stage("Pull Source") {
            //trying to get the hash without checkout gets the hash set in previous build.
            def checkout = checkout scm
            env.COMMIT_HASH = checkout.GIT_COMMIT
            echo "Checkout done; Hash: '${env.COMMIT_HASH}'"
        }

        if(!cleanAks && !override){
            //This will use the content of the package.json file and install any needed dependencies into /node-modules folder
            stage("Install npm dependencies") {

                configFileProvider([configFile(fileId: 'ae106d79-9e5d-4d2c-86f2-2f1827f8606f', replaceTokens: true, targetLocation: 'src/main/resources/config.properties', variable: 'databaseUrl')]) {
                sh "mvn clean package"
            }
                
                echo "dependencies install completed"            
            }

            if (buildImages) {
                stage("Build Images") {
                    echo "setting version: BUILD_LABEL='${env.BUILD_LABEL}'; COMMIT_HASH='${env.COMMIT_HASH}'"

                    sh "docker build -f Dockerfile-hollow -t ${env.BUILD_LABEL}:${env.BUILD_VERSION} ."

                    echo "Docker containers built with tag '${env.BUILD_LABEL}:${env.BUILD_VERSION}'"
                    sh "docker images ${env.BUILD_LABEL}:${env.BUILD_VERSION}"
                }

                stage("Push Images") {
                    sh "chmod +x ./push_images.sh"
                    sh "./push_images.sh ${env.BUILD_LABEL} ${env.BUILD_VERSION}"
                    echo "Docker images pushed to repository"
                }
            }
   
        }

        stage("Queue deploy") {

            echo "Queueing Deploy job (${targetEnv}, ${env.BUILD_LABEL})."

            acsDeploy(azureCredentialsId: '72555f61-7a9f-4145-8bb7-a163f107bccf',
                resourceGroupName: 'mscdevops-aks-rg',
                containerService: 'mscdevops-aks | AKS',
                sshCredentialsId: '491fabd9-2952-4e79-9192-66b52c9dd389',
                configFilePaths: '**/backend/*.yaml',
                enableConfigSubstitution: true,

                // Kubernetes
                secretName: 'mscdevops',
                secretNamespace: 'default',

                // Docker Swarm
                swarmRemoveContainersFirst: true,

                // DC/OS Marathon
                //dcosDockerCredentialsPath: '<dcos-credentials-path>',

            containerRegistryCredentials: [
                [credentialsId: 'dockerRegAccess', url: 'mcsdevopsentarch.azurecr.io'] ])
        }

        stage("Get container public ip"){
            //sh "kubectl describe services svc-ws-service-${env.TARGET_ROLE} | grep 'LoadBalancer Ingress:' | awk '{printf \"%s\\n\", \$3}'"
            sh "kubectl get service svc-ws-service-${env.TARGET_ROLE} | awk '{printf \"%s\\n\", \$4}'"
        }

    } catch (e) {
        throw e
    } finally {
        if (buildImages && clearImages) {
            stage("Clear Images") {
                echo "Removing images with tag '${env.BUILD_LABEL}'"
                sh "docker images ${env.BUILD_LABEL}"
                sh "docker rmi -f \$(docker images | grep '${env.BUILD_LABEL}' | awk '{print \$3}')"
            }
        }
        // Recursively delete the current directory from the workspace
        deleteDir()
        echo "Build done."
    }
}
