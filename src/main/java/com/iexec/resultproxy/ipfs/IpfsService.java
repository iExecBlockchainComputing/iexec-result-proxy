package com.iexec.resultproxy.ipfs;

import com.iexec.common.utils.NetworkUtils;
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
import java.util.Optional;

@Service
@Slf4j
public class IpfsService implements SmartLifecycle {

    private IPFS ipfs;
    private final String multiAddress;

    public IpfsService(IpfsConfig ipfsConfig) {
        String ipfsHost = ipfsConfig.getHost();
        String ipfsNodeIp = NetworkUtils.isIPAddress(ipfsHost) ? ipfsHost : NetworkUtils.convertHostToIp(ipfsHost);
        this.multiAddress = "/ip4/" + ipfsNodeIp + "/tcp/" + ipfsConfig.getPort();
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
