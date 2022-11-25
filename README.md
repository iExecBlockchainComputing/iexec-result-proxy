# iexec-result-proxy

## Overview

The iExec Result Proxy stores results of iExec tasks to IPFS.

The iExec Result Proxy is available as an OCI image on [Docker Hub](https://hub.docker.com/r/iexechub/iexec-result-proxy/tags).

To run properly, the iExec Result Proxy requires:
* A reachable blockchain node URL hosting iExec smart contracts.
* A reachable MongoDB instance to persist its data.
* A reachable IPFS node where iExec tasks results will be pushed and retrieved.

## Configuration

You can configure the result-proxy with the following properties:

| Environment variable | Description | Type | Default value |
| --- | --- | --- | --- |
| IEXEC_RESULT_PROXY_PORT | Server HTTP port of the result proxy. | Positive integer | `13200` |
| MONGO_HOST | Mongo server host. Cannot be set with URI. | String | `localhost` |
| MONGO_PORT | Mongo server port. Cannot be set with URI. | Positive integer | `13202` |
| IEXEC_CHAIN_ID | Chain ID of the blockchain network to connect. | Integer | `17` |
| IEXEC_IS_SIDECHAIN | Define if iExec on-chain protocol is built on top of token (`false`) or native currency (`true`). | Boolean | `false` |
| IEXEC_PRIVATE_CHAIN_ADDRESS | Private URL to connect to the blockchain node. | URL | `http://localhost:8545` |
| IEXEC_PUBLIC_CHAIN_ADDRESS | [unused] Public URL to connect to the blockchain node. | URL | `http://localhost:8545` |
| IEXEC_HUB_ADDRESS | Proxy contract address to interact with the iExec on-chain protocol. | Ethereum address | `0xBF6B2B07e47326B7c8bfCb4A5460bef9f0Fd2002` |
| IEXEC_START_BLOCK_NUMBER | [Unused] | Positive integer | `0` |
| IEXEC_GAS_PRICE_MULTIPLIER | Transactions will be sent with `networkGasPrice * IEXEC_GAS_PRICE_MULTIPLIER`. | Float | `1.0` |
| IEXEC_GAS_PRICE_CAP | In Wei, will be used for transactions if `networkGasPrice * IEXEC_GAS_PRICE_MULTIPLIER > gasPriceCap`. | Integer | `22000000000` |
| IEXEC_IPFS_HOST | Host to connect to the IPFS node. | String | `127.0.0.1` |
| IEXEC_IPFS_PORT | Server port of the IPFS node. | Positive integer | `5001` |
