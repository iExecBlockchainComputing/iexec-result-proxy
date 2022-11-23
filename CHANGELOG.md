# Changelog

All notable changes to this project will be documented in this file.

## [7.1.0] 2022-07-01

[Release link on GitHub](https://github.com/iExecBlockchainComputing/iexec-result-proxy/releases/tag/v7.1.0)

* Try to establish connection against IPFS node several times before shutting down the service.
* Add OpenFeign client library in dedicated iexec-result-proxy-library jar.
* Use new EIP-712 implementation from iexec-common.
* Use Java 11.0.15.

## [7.0.2] 2022-01-03

[Release link on GitHub](https://github.com/iExecBlockchainComputing/iexec-result-proxy/releases/tag/v7.0.2)

* Fixed wallet address duplicate key on JWT.
* Fix ConcurrentModificationException on authentication challenge map.
* Upgrade automated build system
* Upgrade Jacoco/Sonarqube reporting and plugins.
* Upgrade to Spring Boot 2.6.2.
* Upgrade to Gradle 6.8.3.

## [7.0.1] 2021-12-15

[Release link on GitHub](https://github.com/iExecBlockchainComputing/iexec-result-proxy/releases/tag/v7.0.1)

* Use iexec-common 5.9.1.

## [7.0.0] 2021-12-14

Not released on GitHub

* Highly improved throughput of the iExec protocol.

## [6.1.0] 2021-10-27

[Release link on GitHub](https://github.com/iExecBlockchainComputing/iexec-result-proxy/releases/tag/v6.1.0)

* Compatibility with IPFS@v0.5+.
* Ensure JWT uniqueness per user (and IpfsName per task).

## [6.0.0] 2021-06-17

[Release link on GitHub](https://github.com/iExecBlockchainComputing/iexec-result-proxy/releases/tag/v6.0.0)

* Updated default port.

## [1.0.0] 2020-07-15

[Release link on GitHub](https://github.com/iExecBlockchainComputing/iexec-result-proxy/releases/tag/1.0.0)

* First version.
