node {
    def mavenHome = tool 'myMaven' // 'M3' is the name configured in Global Tool Configuration
    stage('Check Environment'){
        sh 'java --version'
        sh 'git --version'
        sh "export PATH=\$PATH:${mavenHome}/bin && mvn --version"
        echo env.BUILD_NUMBER
    }

    stage('Clean and Compile') {
        sh "export PATH=\$PATH:${mavenHome}/bin && mvn clean compile"
    }

    stage('Test'){
        sh "export PATH=\$PATH:${mavenHome}/bin && mvn test"
    }

    stage('Package'){
        sh "export PATH=\$PATH:${mavenHome}/bin && mvn package"
    }
}