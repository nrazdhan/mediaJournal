node {
    def mavenHome = tool 'myMaven' // 'M3' is the name configured in Global Tool Configuration
    stage('Check Environment'){
        sh 'java --version'
        sh 'git --version'
        sh "export PATH=\$PATH:${mavenHome}/bin && mvn --version"
        echo env.BUILD_NUMBER
    }

    stage('Clean and Compile') {
        git branch: 'main', url: 'https://github.com/nrazdhan/mediaJournal.git'
        sh "export PATH=\$PATH:${mavenHome}/bin && mvn clean compile"
    }

    stage('Test'){
        git branch: 'main', url: 'https://github.com/nrazdhan/mediaJournal.git'
        sh "export PATH=\$PATH:${mavenHome}/bin && mvn test"
    }

    stage('Package'){
        git branch: 'main', url: 'https://github.com/nrazdhan/mediaJournal.git'
        sh "export PATH=\$PATH:${mavenHome}/bin && mvn package"
    }

    stage('Run static code analysis job'){
        build job: 'helloworldbuild'
    }

    stage('Create docker image') {
        sh "docker build -t nrazdhan/mediaJournal:latest ."
    }

    stage('docker-deploy'){
        withCredentials([usernamePassword(
            credentialsId: 'my-docker-credentials', 
            usernameVariable: 'USER',
            passwordVariable: 'PASS')]){
                sh 'docker login --username $USER --password $PASS'
                sh 'docker push nrazdhan/mediaJouranl'
            }
    }

    // docker run -d --privileged --name my_jenkins -p 8080:8080 -p 50000:50000 -v ~/my_jenkins_home_volume:/var/jenkins_home -v /var/run/docker.sock:/var/run/docker.sock
}