node {
    stage('Check Environment'){
        def mav = tool name: 'myMaven'
        sh 'java --version'
        sh 'git --version'
        sh 'mvn --version'
        
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