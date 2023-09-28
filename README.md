# iExec Result Proxy

## Overview

The iExec Result Proxy stores results of iExec tasks on IPFS.

The iExec Result Proxy is available as an OCI image on [Docker Hub](https://hub.docker.com/r/iexechub/iexec-result-proxy/tags).

To run properly, the iExec Result Proxy requires:
* A blockchain node. iExec smart contracts must be deployed on the blockchain network.
* A MongoDB instance to persist its data.
* An IPFS node where iExec tasks results will be pushed and retrieved.

## Configuration

You can configure the iExec Result Proxy with the following properties:

### Main application properties

| Environment variable | Description | Type | Default value |
| --- | --- | --- | --- |
| `IEXEC_RESULT_PROXY_PORT` | Server HTTP port of the result proxy. | Positive integer | `13200` |
| `MONGO_HOST` | Mongo server host. Cannot be set with URI. | String | `localhost` |
| `MONGO_PORT` | Mongo server port. Cannot be set with URI. | Positive integer | `13202` |
| `IEXEC_CHAIN_ID` | Chain ID of the blockchain network to connect. | `Integer | `134` |
| `IEXEC_IS_SIDECHAIN` | Define whether iExec on-chain protocol is built on top of token (`false`) or native currency (`true`). | Boolean | `true` |
| `IEXEC_PRIVATE_CHAIN_ADDRESS` | Private URL to connect to the blockchain node. | URL | `https://bellecour.iex.ec` |
| `IEXEC_HUB_ADDRESS` | Proxy contract address to interact with the iExec on-chain protocol. | Ethereum address | `0x3eca1B216A7DF1C7689aEb259fFB83ADFB894E7f` |
| `IEXEC_BLOCK_TIME` | Duration between consecutive blocks on the blockchain network. | String | `PT5S` |
| `IEXEC_GAS_PRICE_MULTIPLIER` | Transactions will be sent with `networkGasPrice * IEXEC_GAS_PRICE_MULTIPLIER`. | Float | `1.0` |
| `IEXEC_GAS_PRICE_CAP` | In Wei, will be used for transactions if `networkGasPrice * IEXEC_GAS_PRICE_MULTIPLIER > gasPriceCap`. | Integer | `22000000000` |
| `IEXEC_IPFS_HOST` | Host to connect to the IPFS node. | String | `127.0.0.1` |
| `IEXEC_IPFS_PORT` | Server port of the IPFS node. | Positive integer | `5001` |

### Spring web application properties

In addition to the aforementioned properties, it is possible to leverage [Spring Common Application Properties](https://docs.spring.io/spring-boot/docs/current/reference/html/application-properties.html).
More specifically, you may want to configure limits of uploadable file sizes.
In order to do so, you can override the default for the following [Web Properties](https://docs.spring.io/spring-boot/docs/current/reference/html/application-properties.html#appendix.application-properties.web).

| Environment variable | Description | Type | Default value |
| --- | --- | --- | --- |
| `SPRING_SERVLET_MULTIPART_MAX_FILE_SIZE` | Max file size. | String | `1MB` |
| `SPRING_SERVLET_MULTIPART_MAX_REQUEST_SIZE` | Max request size. | String | `10MB` |

## Health checks

A health endpoint (`/actuator/health`) is enabled by default and can be accessed on the **IEXEC_RESULT_PROXY_PORT**.
This endpoint allows to define health checks in an orchestrator or a [compose file](https://github.com/compose-spec/compose-spec/blob/master/spec.md#healthcheck).
No default strategy has been implemented in the [Dockerfile](Dockerfile) at the moment.

## License

This repository code is released under the [Apache License 2.0](LICENSE).
