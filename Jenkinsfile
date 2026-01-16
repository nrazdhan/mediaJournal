node {
    def mavenHome = tool 'myMaven' // 'M3' is the name configured in Global Tool Configuration
    stage('Check Environment'){
        sh 'java --version'
        sh 'git --version'
        sh "export PATH=\$PATH:${mavenHome}/bin && mvn --version"
        echo env.BUILD_NUMBER
    }

    stage('Clean and Compile') {
        sh 'mvn clean compile'
    }

    stage('Compile') {
        sh 'mvn clean package'
    }

    stage('Test'){
        sh 'mvn test'
    }

    stage('Package'){
        sh 'mvn package'
    }
}