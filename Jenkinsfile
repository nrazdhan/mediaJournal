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
        sh "docker build -t nrazdhan/mediajournal:latest ."
    }

    stage('docker-deploy'){
        withCredentials([usernamePassword(
            credentialsId: 'my-docker-credentials', 
            usernameVariable: 'USER',
            passwordVariable: 'PASS')]){
                sh 'docker login --username $USER --password $PASS'
                sh 'docker push nrazdhan/mediajouranl'
            }
    }

    // docker run -d --privileged --name my_jenkins_docker_sock_plus_docker_client2 -p 8080:8080 -p 50000:50000 -v ~/my_jenkins_home_volume:/var/jenkins_home -v /var/run/docker.sock:/var/run/docker.sock -v $(which docker):/usr/bin/docker jenkins/jenkins:lts

    //RUN jenkins local installation but use docker-jenkins created my_jenkins_home_volume
    //JENKINS_HOME=/Users/nrazdhan/my_jenkins_home_volume /opt/homebrew/opt/openjdk@21/bin/java -Dmail.smtp.starttls.enable\=true -jar /opt/homebrew/opt/jenkins-lts/libexec/jenkins.war --httpListenAddress\=127.0.0.1 --httpPort\=8080
}