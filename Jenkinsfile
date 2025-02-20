#!groovy

    boolean buildImages = false
    def targetEnv = ""
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
        env.BUILD_LABEL = 'ws-service'
        buildImages = params.BUILD_IMAGES
        targetEnv = params.TARGET_ENV?.trim()
        clearImages = params.CLEAR_IMAGES
        cleanAks = params.CLEAN_AKS
        env.TARGET_ROLE = currentEnvironment
        setupDns = params.SETUP_DNS      

        // Check if the build label is set
        if (buildImages) {
            if (!env.BUILD_LABEL) {
                error("Build label must be specified!: build label: ${env.BUILD_LABEL}")
            }
        }
        // Check if this is an overide play
        if (overrideVersion != "1.0.0.0") {    
            env.BUILD_VERSION = overrideVersion
            buildImages = false
            override = true
        }

        echo """Parameters:
            branch: '${branch}' 
            BUILD LABEL: '$env.BUILD_LABEL'
            BUILD VERSION: '$env.BUILD_VERSION'
            buildImages: '${buildImages}'
            targetEnv: '${targetEnv}'
            clearImages: '${clearImages}'
            cleanAks: '${cleanAks}'
            REPLICAS NO: '$env.REPLICAS_NO'
            TARGET_ROLE: '$env.TARGET_ROLE'
            overrideVersion: '${overrideVersion}'
            TARGET PORT: '${env.TARGET_PORT}'
            setupDns: '${setupDns}'
        """

        if(cleanAks) {
            withCredentials([azureServicePrincipal(servicePrincipalId)]) {
                // Login to azure
                sh 'az login --service-principal -u $AZURE_CLIENT_ID -p $AZURE_CLIENT_SECRET -t $AZURE_TENANT_ID'
                // Set subscription context
                sh 'az account set -s $AZURE_SUBSCRIPTION_ID'
                // Set aks context
                sh 'az aks get-credentials --overwrite-existing --resource-group \"mscdevops-aks-rg\" --name \"mscdevops-aks\"'

                // Navigate to ws-service deployment directory
                dir('aks/backend'){
                    // Deploy the service
                    sh "kubectl get deployments -n default --no-headers=true | awk \'/ws-service/{print \$1}\' | xargs kubectl delete -n default deployment"
                    sh "kubectl get services -n default --no-headers=true | awk \'/ws-service/{print \$1}\' | xargs kubectl delete -n default service"
                }
            }
            // If setup dns is not set to true exit right after cleaning the cluster   
            if (!setupDns){
                echo "EXIT NOW!"
                return 0
            }
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

        if(setupDns){
            stage('Setup services'){
                withCredentials([azureServicePrincipal(servicePrincipalId)]) {
                    sh """
                        az login --service-principal -u $AZURE_CLIENT_ID -p $AZURE_CLIENT_SECRET -t $AZURE_TENANT_ID
                        az account set -s $AZURE_SUBSCRIPTION_ID
                        az aks get-credentials --overwrite-existing --resource-group mscdevops-aks-rg --name mscdevops-aks --admin --file kubeconfig
                        bash aks/setup/setup_dns.sh
                        az logout
                    """
                }
            }
        }

        stage('Check Env') {
        // check the current active environment to determine the inactive one that will be deployed to

            withCredentials([azureServicePrincipal(servicePrincipalId)]) {
                // fetch the current service configuration
                sh """
                    az login --service-principal -u $AZURE_CLIENT_ID -p $AZURE_CLIENT_SECRET -t $AZURE_TENANT_ID
                    az account set -s $AZURE_SUBSCRIPTION_ID
                    az aks get-credentials --overwrite-existing --resource-group mscdevops-aks-rg --name mscdevops-aks --admin --file kubeconfig
                    current_role="\$(kubectl --kubeconfig kubeconfig get services svc-ws-service --output json | jq -r .spec.selector.deployment)"
                    if [ "\$current_role" = null ]; then
                      echo "Unable to determine current environment"
                      exit 1
                    fi
                    echo "\$current_role" >current-environment
                """
            }

            // parse the current active backend
            currentEnvironment = readFile('current-environment').trim()

            // set the build name
            echo "***************************  CURRENT: $currentEnvironment     NEW: ${newEnvironment()}  *****************************"
            currentBuild.displayName = newEnvironment().toUpperCase() + ' ' + "${env.BUILD_LABEL}:${env.BUILD_VERSION}"

            env.TARGET_ROLE = newEnvironment()
        }      

        stage("Queue deploy") {

            echo "Queueing Deploy job (${targetEnv}, ${env.BUILD_LABEL})."

            acsDeploy(azureCredentialsId: servicePrincipalId,
                resourceGroupName: 'mscdevops-aks-rg',
                containerService: 'mscdevops-aks | AKS',
                sshCredentialsId: '491fabd9-2952-4e79-9192-66b52c9dd389',
                configFilePaths: '**/backend/deploy.yaml',
                enableConfigSubstitution: true,

                // Kubernetes
                secretName: 'mscdevops',
                secretNamespace: 'default',

            containerRegistryCredentials: [
                [credentialsId: 'dockerRegAccess', url: 'mcsdevopsentarch.azurecr.io'] ])
        }

        def verifyEnvironment = { service ->
            sh """
              endpoint_ip="\$(kubectl get services '${service}' --output json | jq -r '.status.loadBalancer.ingress[0].ip')"
              count=0
              while true; do
                  count=\$(expr \$count + 1)
                  if curl -m 10 "http://\$endpoint_ip"; then
                      break;
                  fi
                  if [ "\$count" -gt 30 ]; then
                      echo 'Timeout while waiting for the ${service} endpoint to be ready'
                      exit 1
                  fi
                  echo "${service} endpoint is not ready, wait 10 seconds..."
                  sleep 10
              done
            """
        }

        // stage('Verify Staged') {
        //     // verify the deployment through the corresponding test endpoint
        //     verifyEnvironment("svc-ws-service-${newEnvironment()}")
        // }
    
        stage('Switch') {
            // Update the production service endpoint to route to the new environment.
            // With enableConfigSubstitution set to true, the variables ${TARGET_ROLE}
            // will be replaced with environment variable values
            acsDeploy azureCredentialsId: servicePrincipalId,
                      resourceGroupName: 'mscdevops-aks-rg',
                      containerService: "mscdevops-aks | AKS",
                      sshCredentialsId: '491fabd9-2952-4e79-9192-66b52c9dd389',
                      configFilePaths: '**/backend/service.yaml',
                      enableConfigSubstitution: true
        }
    
        // stage('Verify Prod') {
        //     // verify the production environment is working properly
        //     verifyEnvironment('svc-ws-service')
        // }

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
