# Changelog

All notable changes to this project will be documented in this file.

## [[7.1.1]](https://github.com/iExecBlockchainComputing/iexec-result-proxy/releases/tag/v7.1.1) 2022-11-29

* Update build workflow to 2.1.2, update documentation in README and add CHANGELOG.

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