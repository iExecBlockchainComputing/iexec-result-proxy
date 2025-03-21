/*
 * Copyright 2020-2025 IEXEC BLOCKCHAIN TECH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.iexec.resultproxy.ipfs;

import com.google.common.net.InetAddresses;
import io.ipfs.api.IPFS;
import io.ipfs.api.MerkleNode;
import io.ipfs.api.NamedStreamable;
import io.ipfs.multihash.Multihash;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.SmartLifecycle;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Optional;

@Service
@Slf4j
public class IpfsService implements SmartLifecycle {

    private IPFS ipfs;
    private final String multiAddress;

    public IpfsService(IpfsConfig ipfsConfig) {
        try {
            URL ipfsUrl = new URL(ipfsConfig.getUrl());
            String ipfsHost = ipfsUrl.getHost();
            int port = ipfsUrl.getPort();
            String ipfsNodeIp = isIPAddress(ipfsHost) ? ipfsHost : convertHostToIp(ipfsHost);
            this.multiAddress = "/ip4/" + ipfsNodeIp + "/tcp/" + port;
        } catch (MalformedURLException e) {
            log.error("Invalid IPFS URL: {}", ipfsConfig.getUrl(), e);
            throw new IllegalArgumentException("Invalid IPFS URL: " + ipfsConfig.getUrl(), e);
        }
    }

    public Optional<byte[]> get(String ipfsHash) {
        if (!isIpfsHash(ipfsHash)){
            return Optional.empty();
        }
        Multihash filePointer = Multihash.fromBase58(ipfsHash);
        try {
            return Optional.of(ipfs.cat(filePointer));
        } catch (IOException e) {
            log.error("Error when trying to retrieve ipfs object [hash:{}]", ipfsHash);
        }
        return Optional.empty();
    }

    public String add(String fileName, byte[] fileContent) {
        NamedStreamable.ByteArrayWrapper file = new NamedStreamable.ByteArrayWrapper(fileName, fileContent);
        try {
            MerkleNode pushedContent = ipfs.add(file, false).get(0);
            return pushedContent.hash.toString();
        } catch (IOException e) {
            log.error("Error when trying to push ipfs object [fileName:{}]", fileName);
        }
        return "";
    }

    public static boolean isIpfsHash(String hash) {
        try {
            return Multihash.fromBase58(hash).toBase58() != null;
        } catch (Exception e) {
            return false;
        }
    }

    private String convertHostToIp(String hostname) {
        InetAddress address = null;
        try {
            address = InetAddress.getByName(hostname);
        } catch (UnknownHostException e) {
            log.error("No IP address could be found [host:{}]", hostname, e);
        }
        return address != null ? address.getHostAddress() : "";
    }

    private boolean isIPAddress(String host) {
        return InetAddresses.isInetAddress(host);
    }

    @Override
    @Retryable(maxAttempts = 10)
    public void start() {
        this.ipfs = new IPFS(multiAddress);
    }

    @Recover
    public void start(RuntimeException exception) {
        log.error("Exception when initializing IPFS connection", exception);
        log.warn("Shutting down service since IPFS is necessary");
        throw exception;
    }

    @Override
    public void stop() {

    }

    @Override
    public boolean isRunning() {
        return ipfs != null;
    }
}
