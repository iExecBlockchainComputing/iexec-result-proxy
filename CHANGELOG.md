# Changelog

All notable changes to this project will be documented in this file.

## [[NEXT]](https://github.com/iExecBlockchainComputing/iexec-result-proxy/releases/tag/vNEXT) 2024

### Dependency Upgrades

- Upgrade to `eclipse-temurin:11.0.24_8-jre-focal`. (#141)
- Upgrade to Gradle 8.10.2. (#142)

## [[8.5.0]](https://github.com/iExecBlockchainComputing/iexec-result-proxy/releases/tag/v8.5.0) 2024-06-18

### New Features

- Replace `CredentialsService` with `SignerService`. (#137)

### Bug Fixes

- Fix conditions to retrieve a JWT or to allow a result upload. (#132)

### Quality

- Configure Gradle JVM Test Suite Plugin. (#133)

### Dependency Upgrades

- Upgrade to Gradle 8.7. (#134)
- Upgrade to `eclipse-temurin:11.0.22_7-jre-focal`. (#135)
- Upgrade to Spring Boot 2.7.18. (#136)
- Upgrade to `iexec-commons-poco` 4.1.0. (#138)
- Upgrade to `iexec-common` 8.5.0. (#138)

## [[8.4.0]](https://github.com/iExecBlockchainComputing/iexec-result-proxy/releases/tag/v8.4.0) 2024-02-29

### Deprecation Notices

- Deprecate `/results/challenge` and `/results/login` endpoints. They will be removed in **v10**. (#119)
- Deprecate `/` endpoint. Use `/v1/results` instead. The `/` endpoint will be removed in **v10**. (#119 #120)

### New Features

- Add `AuthorizationService` to enable `WorkerpoolAuthorization` validation. (#116)
- Label REST API with `v1` version. (#120)
- Add an endpoint to retrieve a JWT against a valid `WorkerpoolAuthorization`. (#123 #124)
- Verify TEE tasks `enclaveSignature` before accepting IPFS upload. (#125 #126)
- Persist `WorkerpoolAuthorization` instances to MongoDB. (#127)

### Bug Fixes

- Persist JWT signing key in dedicated `/data` folder by default. (#128)

### Quality

- Remove results download endpoints which are never used. (#117)
- Add tests and javadoc on `ProxyController` class. (#118)
- Remove `AbstractResultStorage` class. (#121)
- Rework `ProxyService` class methods to use `ResultModel` as a parameter. (#122)

### Dependency Upgrades

- Upgrade to `iexec-common` 8.4.0. (#129)

## [[8.3.0]](https://github.com/iExecBlockchainComputing/iexec-result-proxy/releases/tag/v8.3.0) 2024-01-10

### New Features

- Expose version through prometheus endpoint and through VersionController. (#111 #112)

### Bug Fixes

- Remove duplicated call to blockchain in `ProxyService`. (#110)

### Quality

- Add and use a non-root user in the dockerfile. (#106)

### Dependency Upgrades

- Upgrade to `eclipse-temurin:11.0.21_9-jre-focal`. (#109)
- Upgrade to Spring Boot 2.7.17. (#108)
- Upgrade to Spring Dependency Management Plugin 1.1.4. (#108)
- Upgrade to Spring Doc OpenAPI 1.7.0. (#111)
- Upgrade to `jenkins-library` 2.7.4. (#107)
- Upgrade to `iexec-commons-poco` 3.2.0. (#113)
- Upgrade to `iexec-common` 8.3.1. (#113)

## [[8.2.0]](https://github.com/iExecBlockchainComputing/iexec-result-proxy/releases/tag/v8.2.0) 2023-09-28

### New Features

- Remove `nexus.intra.iex.ec` repository. (#94)
- Check result hash before uploading. (#101)

### Bug Fixes

- Fix and harmonize `Dockerfile entrypoint` in all Spring Boot applications. (#98)
- Describe upload limits configuration in README.md. (#99)

### Quality

- Upgrade to Gradle 8.2.1 with up-to-date plugins. (#97)
- Remove `VersionService#isSnapshot`. (#103)

### Dependency Upgrades

- Upgrade to `eclipse-temurin` 11.0.20. (#95)
- Upgrade to Spring Boot 2.7.14. (#96)
- Upgrade to Spring Dependency Management Plugin 1.1.3. (#96)
- Upgrade to `jenkins-library` 2.7.3. (#100)
- Upgrade to `iexec-common` 8.3.0. (#102)
- Upgrade to `iexec-common-poco` 3.1.0. (#102)

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
