pipeline {
  agent any
  tools {
    maven 'Maven3.6'
    jdk 'OpenJDK11'
  }
  options {
    buildDiscarder(logRotator(numToKeepStr: '10'))
    skipStagesAfterUnstable()
    timestamps()
  }

  stages {

    stage('Maven build') {
      steps {
        configFileProvider([configFile(fileId: 'org.jenkinsci.plugins.configfiles.maven.GlobalMavenSettingsConfig1387378707709', variable: 'MAVEN_SETTINGS')]) {
          sh 'mvn -s $MAVEN_SETTINGS clean verify deploy -B -U'
        }
      }
    }

    stage('Build and push Docker image') {
      steps {
        sh 'build/kvs-indexing-docker-build.sh'
      }
    }

  }

    post {
      success {
        echo 'KVS executed successfully!'
      }
      failure {
        echo 'KVS execution failed!'
    }
  }
}
