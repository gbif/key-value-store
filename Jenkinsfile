pipeline {
  agent any
  tools {
    maven 'Maven 3.8.5'
    jdk 'OpenJDK11'
  }
  options {
    buildDiscarder(logRotator(numToKeepStr: '10'))
    skipStagesAfterUnstable()
    timestamps()
  }
  parameters {
    separator(name: "release_separator", sectionHeader: "Release Project Parameters")
    booleanParam(name: 'RELEASE', defaultValue: false, description: 'Do a Maven release')
    string(name: 'RELEASE_VERSION', defaultValue: '', description: 'Release version (optional)')
    string(name: 'DEVELOPMENT_VERSION', defaultValue: '', description: 'Development version (optional)')
    booleanParam(name: 'DRY_RUN_RELEASE', defaultValue: false, description: 'Dry Run Maven release')
  }
  environment {
    POM_VERSION = readMavenPom().getVersion()
  }
  stages {

    stage('Validate') {
      when {
        allOf {
          expression { params.RELEASE }
            not {
                branch 'master'
            }
        }
      }
      steps {
        script {
          error('Releases are only allowed from the master branch.')
        }
      }
    }

    stage('Maven build') {
      when {
          allOf {
              not { expression { params.RELEASE } };
          }
      }
      steps {
        configFileProvider([configFile(fileId: 'org.jenkinsci.plugins.configfiles.maven.GlobalMavenSettingsConfig1387378707709', variable: 'MAVEN_SETTINGS')]) {
          sh 'mvn -s $MAVEN_SETTINGS clean verify deploy -B -U'
        }
      }
    }

    stage('Maven release: Main project') {
      when {
          allOf {
              expression { params.RELEASE };
              branch 'master';
          }
      }
      environment {
          RELEASE_ARGS = createReleaseArgs(params.RELEASE_VERSION, params.DEVELOPMENT_VERSION, params.DRY_RUN_RELEASE)
      }
      steps {
          configFileProvider(
                  [configFile(fileId: 'org.jenkinsci.plugins.configfiles.maven.GlobalMavenSettingsConfig1387378707709',
                          variable: 'MAVEN_SETTINGS_XML')]) {
              git 'https://github.com/gbif/key-value-store.git'
              sh 'mvn -s $MAVEN_SETTINGS_XML -B -Denforcer.skip=true release:prepare release:perform $RELEASE_ARGS'
          }
      }
    }

    stage('Build and push Docker image') {
      environment {
          VERSION = getDockerVersion()
      }
      steps {
        sh 'build/kvs-indexing-docker-build.sh ${VERSION}'
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

def createReleaseArgs(inputVersion, inputDevVersion, inputDryrun) {
    def args = ""
    if (inputVersion != '') {
        args += " -DreleaseVersion=" + inputVersion
    }
    if (inputDevVersion != '') {
        args += " -DdevelopmentVersion=" + inputDevVersion
    }
    if (inputDryrun) {
        args += " -DdryRun=true"
    }

    return args
}

def getDockerVersion() {
    if (params.RELEASE && params.RELEASE_VERSION != '') {
        return inputVersion
    }
    return "${POM_VERSION}"
}
