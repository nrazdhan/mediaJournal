node {
    stage('Check Environment'){
        sh 'git --version'
        sh 'mvn --version'
        sh 'java --version'
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