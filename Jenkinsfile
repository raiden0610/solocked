#!/usr/bin/env groovy

node {
    stage('checkout') {
        deleteDir()
        checkout scm
    }

    stage('check java') {
        sh "java -version"
    }

    stage('clean') {
        sh "chmod +x mvnw"
        sh "./mvnw clean"
    }

    stage('install tools') {
        sh "./mvnw com.github.eirslett:frontend-maven-plugin:install-node-and-yarn -DnodeVersion=v6.11.3 -DyarnVersion=v1.1.0"
    }

    stage('yarn install') {
        sh "./mvnw com.github.eirslett:frontend-maven-plugin:yarn"
    }

    stage('backend tests') {
        try {
            sh "./mvnw test"
        } catch(err) {
            throw err
        } finally {
            junit '**/target/surefire-reports/TEST-*.xml'
        }
    }

    stage('frontend tests') {
        try {
            sh "./mvnw com.github.eirslett:frontend-maven-plugin:yarn -Dfrontend.yarn.arguments=test"
        } catch(err) {
            throw err
        } finally {
            junit '**/target/test-results/karma/TESTS-*.xml'
        }
    }

    stage('integration test') {
        try {
            sh "./mvnw"
            sh "./mvnw com.github.eirslett:frontend-maven-plugin:yarn e2e"
        } catch(err) {
            throw err
        } finally {
            publishHTML (target: [
                allowMissing: false,
                alwaysLinkToLastBuild: false,
                keepAll: true,
                reportDir: 'target/reports/e2e/screenshots',
                reportFiles: 'report.html',
                reportName: "E2E Report"
            ])
            junit '**/target/reports/e2e/junitresults-*.xml'
        }
    }
}
