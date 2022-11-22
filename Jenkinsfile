@Library('global-jenkins-library@hotfix/2.1.2') _
buildJavaProject(
        buildInfo: getBuildInfo(),
        integrationTestsEnvVars: [],
        shouldPublishJars: true,
        shouldPublishDockerImages: true,
        dockerfileDir: '.',
        preDevelopVisibility: 'iex.ec',
        developVisibility: 'iex.ec',
        preProductionVisibility: 'docker.io',
        productionVisibility: 'docker.io')
