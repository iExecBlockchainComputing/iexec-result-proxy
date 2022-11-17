package com.iexec.resultproxy.proxy;

import com.iexec.common.chain.eip712.entity.EIP712Challenge;
import com.iexec.common.result.ResultModel;
import com.iexec.common.security.SignedChallenge;
import com.iexec.resultproxy.challenge.ChallengeService;
import com.iexec.resultproxy.ipfs.IpfsService;
import com.iexec.resultproxy.ipfs.task.IpfsNameService;
import com.iexec.resultproxy.jwt.JwtService;
import com.iexec.resultproxy.result.AbstractResultStorage;
import com.iexec.resultproxy.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

import static org.springframework.http.ResponseEntity.ok;

@Slf4j
@CrossOrigin
@RestController
public class ProxyController {

    private final ChallengeService challengeService;
    private final JwtService jwtService;
    private final ProxyService proxyService;
    private final IpfsService ipfsService;
    private final IpfsNameService ipfsNameService;

    public ProxyController(ChallengeService challengeService,
                           JwtService jwtService,
                           ProxyService proxyService,
                           IpfsService ipfsService,
                           IpfsNameService ipfsNameService) {
        this.challengeService = challengeService;
        this.jwtService = jwtService;
        this.proxyService = proxyService;
        this.ipfsService = ipfsService;
        this.ipfsNameService = ipfsNameService;
    }

    @GetMapping(value = "/results/challenge")
    public ResponseEntity<EIP712Challenge> getChallenge(@RequestParam(name = "chainId") Integer chainId) {
        EIP712Challenge eip712Challenge = challengeService.createChallenge(chainId); // TODO generate challenge from walletAddress
        return ResponseEntity.ok(eip712Challenge);
    }

    @PostMapping(value = "/results/login")
    public ResponseEntity<String> login(@RequestParam(name = "chainId") Integer chainId,
                                        @RequestBody String token) {
        SignedChallenge signedChallenge = challengeService.tokenToSignedChallengeObject(token);
        if (!challengeService.isSignedChallengeValid(signedChallenge)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String jwtString = jwtService.getOrCreateJwt(signedChallenge);
        if (jwtString == null || jwtString.isEmpty()) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        challengeService.invalidateChallenge(signedChallenge.getChallengeHash());
        return ResponseEntity.ok(jwtString);
    }

    @PostMapping("/")
    public ResponseEntity<String> addResult(@RequestHeader("Authorization") String token,
                                            @RequestBody ResultModel model) {

        if (!jwtService.isValidJwt(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED.value()).build();
        }

        String walletAddress = jwtService.getWalletAddressFromJwtString(token);
        boolean canUploadResult = proxyService.canUploadResult(model.getChainTaskId(), walletAddress);

        // TODO check if the result to be added is the correct result for that task

        if (!canUploadResult) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED.value()).build();
        }

        String resultLink = proxyService.addResult(
                Result.builder()
                        .chainTaskId(model.getChainTaskId())
                        .image(model.getImage())
                        .cmd(model.getCmd())
                        .deterministHash(model.getDeterministHash())
                        .build(),
                model.getZip());

        if (resultLink.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST.value()).build();
        }

        log.info("Result uploaded successfully [chainTaskId:{}, uploadRequester:{}, resultLink:{}]",
                model.getChainTaskId(), walletAddress, resultLink);

        return ok(resultLink);
    }

    @RequestMapping(method = RequestMethod.HEAD, path = "/results/{chainTaskId}")
    public ResponseEntity<String> isResultUploaded(@PathVariable(name = "chainTaskId") String chainTaskId,
                                                   @RequestHeader("Authorization") String token) {

        if (!jwtService.isValidJwt(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        boolean isResultFound = proxyService.isResultFound(chainTaskId);
        HttpStatus status = isResultFound ? HttpStatus.NO_CONTENT : HttpStatus.NOT_FOUND;
        return ResponseEntity.status(status).build();
    }

    @GetMapping(value = "/results/{chainTaskId}", produces = "application/zip")
    public ResponseEntity<byte[]> getResult(@PathVariable("chainTaskId") String chainTaskId,
                                            @RequestHeader(name = "Authorization") String token,
                                            @RequestParam(name = "chainId") Integer chainId) {

        if (!jwtService.isValidJwt(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED.value()).build();
        }

        Optional<byte[]> zip = proxyService.getResult(chainTaskId);
        if (zip.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND.value()).build();
        }

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename="
                        + AbstractResultStorage.getResultFilename(chainTaskId) + ".zip")
                .body(zip.get());
    }

    /*
        IPFS Gateway endpoint
     */
    @GetMapping(value = "/results/ipfs/{ipfsHash}", produces = "application/zip")
    public ResponseEntity<byte[]> getResult(@PathVariable("ipfsHash") String ipfsHash) {
        Optional<byte[]> zip = ipfsService.get(ipfsHash);
        if (zip.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND.value()).build();
        }
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename="
                        + AbstractResultStorage.getResultFilename(ipfsHash) + ".zip")
                .body(zip.get());
    }

    /*
     *   Retrieves ipfsHash for taskId if required
     * */
    @GetMapping("/results/{chainTaskId}/ipfshash")
    public ResponseEntity<String> getIpfsHashForTask(@PathVariable("chainTaskId") String chainTaskId) {
        String ipfsHashForTask = ipfsNameService.getIpfsHashForTask(chainTaskId);
        if (ipfsHashForTask.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND.value()).build();
        }
        return ResponseEntity.ok(ipfsHashForTask);
    }

}

