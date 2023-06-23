# Changelog

All notable changes to this project will be documented in this file.

## [[8.1.1]](https://github.com/iExecBlockchainComputing/iexec-result-proxy/releases/tag/v8.1.1) 2023-06-23

### Dependency Upgrades
- Upgrade to `iexec-common` 8.2.1. (#92)
- Upgrade to `iexec-commons-poco` 3.0.4.(#92)

## [[8.1.0]](https://github.com/iExecBlockchainComputing/iexec-result-proxy/releases/tag/v8.1.0) 2023-06-07

### New Features
- Enable Prometheus actuator. (#84)
### Bug Fixes
- Fix link in changelog. (#83)
- `ChainConfig` instances are immutable. (#89)
### Dependency Upgrades
- Upgrade to `expiringmap` 0.5.10. (#84)
- Upgrade to `iexec-common` 8.2.0. (#85 #87 #88)
- Upgrade to `iexec-commons-poco` 3.0.2. (#85 #87 #88)

## [[8.0.0]](https://github.com/iExecBlockchainComputing/iexec-result-proxy/releases/tag/v8.0.0) 2023-03-03

### New Features
* Support Gramine framework for TEE tasks.
* Add iExec banner at startup.
* Show application version on banner.
### Bug Fixes
* Sign issued JWT tokens.
* Fix JWT tokens flow.
### Quality
* Clean controllers.
### Dependency Upgrades
* Replace the deprecated `openjdk` Docker base image with `eclipse-temurin` and upgrade to Java 11.0.18 patch.
* Upgrade to Spring Boot 2.6.14.
* Upgrade to Gradle 7.6.
* Upgrade OkHttp to 4.9.0.
* Upgrade `java-http-ipfs-client` to 1.4.0 for latest IPFS Kubo support (v0.18.1).
* Upgrade `jjwt` to `jjwt-api` 0.11.5.
* Upgrade to `iexec-common` 7.0.0.
* Upgrade to `jenkins-library` 2.4.0.

## [[7.3.0]](https://github.com/iExecBlockchainComputing/iexec-result-proxy/releases/tag/v7.3.0) 2023-01-18

* Add endpoint to allow health checks.

## [[7.2.0]](https://github.com/iExecBlockchainComputing/iexec-result-proxy/releases/tag/v7.2.0) 2023-01-09

* Increments jenkins-library up to version 2.2.3. Enable SonarCloud analyses on branches and pull requests.

## [[7.1.1]](https://github.com/iExecBlockchainComputing/iexec-result-proxy/releases/tag/v7.1.1) 2022-11-29

* Update build workflow to 2.1.4, update documentation in README and add CHANGELOG.

## [[7.1.0]](https://github.com/iExecBlockchainComputing/iexec-result-proxy/releases/tag/v7.1.0) 2022-07-01

* Try to establish connection against IPFS node several times before shutting down the service.
* Add OpenFeign client library in dedicated iexec-result-proxy-library jar.
* Use new EIP-712 implementation from iexec-common.
* Use Java 11.0.15.

## [[7.0.2]](https://github.com/iExecBlockchainComputing/iexec-result-proxy/releases/tag/v7.0.2) 2022-01-03

* Fixed wallet address duplicate key on JWT.
* Fix ConcurrentModificationException on authentication challenge map.
* Upgrade automated build system.
* Upgrade Jacoco/Sonarqube reporting and plugins.
* Upgrade to Spring Boot 2.6.2.
* Upgrade to Gradle 6.8.3.

## [[7.0.1]](https://github.com/iExecBlockchainComputing/iexec-result-proxy/releases/tag/v7.0.1) 2021-12-15

* Use iexec-common 5.9.1.

## [[7.0.0]](https://github.com/iExecBlockchainComputing/iexec-result-proxy/releases/tag/v7.0.0) 2021-12-14

* Highly improved throughput of the iExec protocol.

## [[6.1.0]](https://github.com/iExecBlockchainComputing/iexec-result-proxy/releases/tag/v6.1.0) 2021-10-27

* Compatibility with IPFS@v0.5+.
* Ensure JWT uniqueness per user (and IpfsName per task).

## [[6.0.0]](https://github.com/iExecBlockchainComputing/iexec-result-proxy/releases/tag/v6.0.0) 2021-06-17

* Updated default port.

## [[1.0.0]](https://github.com/iExecBlockchainComputing/iexec-result-proxy/releases/tag/1.0.0) 2020-07-15

* First version.
