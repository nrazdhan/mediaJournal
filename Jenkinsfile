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
        build job: helloworldbuild
    }
}