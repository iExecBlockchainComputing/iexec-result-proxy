/*
 * Copyright 2020-2024 IEXEC BLOCKCHAIN TECH
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

package com.iexec.resultproxy.proxy;

import com.iexec.common.result.ResultModel;
import com.iexec.common.security.SignedChallenge;
import com.iexec.commons.poco.chain.WorkerpoolAuthorization;
import com.iexec.commons.poco.eip712.entity.EIP712Challenge;
import com.iexec.resultproxy.authorization.AuthorizationService;
import com.iexec.resultproxy.challenge.ChallengeService;
import com.iexec.resultproxy.ipfs.task.IpfsNameService;
import com.iexec.resultproxy.jwt.JwtService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.ResponseEntity.ok;

@Slf4j
@CrossOrigin
@RestController
public class ProxyController {

    private final AuthorizationService authorizationService;
    private final ChallengeService challengeService;
    private final JwtService jwtService;
    private final ProxyService proxyService;
    private final IpfsNameService ipfsNameService;

    public ProxyController(AuthorizationService authorizationService,
                           ChallengeService challengeService,
                           JwtService jwtService,
                           ProxyService proxyService,
                           IpfsNameService ipfsNameService) {
        this.authorizationService = authorizationService;
        this.challengeService = challengeService;
        this.jwtService = jwtService;
        this.proxyService = proxyService;
        this.ipfsNameService = ipfsNameService;
    }

    /**
     * @deprecated Use new endpoint with valid {@code WorkerpoolAuthorization}
     */
    @Deprecated(forRemoval = true)
    @GetMapping(value = "/results/challenge")
    public ResponseEntity<EIP712Challenge> getChallenge(@RequestParam(name = "chainId") Integer chainId) {
        EIP712Challenge eip712Challenge = challengeService.createChallenge(chainId);
        return ResponseEntity.ok(eip712Challenge);
    }

    /**
     * @deprecated Use new endpoint with valid {@code WorkerpoolAuthorization}
     */
    @Deprecated(forRemoval = true)
    @PostMapping(value = "/results/login")
    public ResponseEntity<String> login(@RequestParam(name = "chainId") Integer chainId,
                                        @RequestBody String token) {
        SignedChallenge signedChallenge = challengeService.tokenToSignedChallengeObject(token);
        if (!challengeService.isSignedChallengeValid(signedChallenge)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String jwtString = jwtService.getOrCreateJwt(signedChallenge.getWalletAddress());
        if (jwtString == null || jwtString.isEmpty()) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        challengeService.invalidateChallenge(signedChallenge.getChallengeHash());
        return ResponseEntity.ok(jwtString);
    }

    /**
     * Logs against Result Proxy with valid {@code WorkerpoolAuthorization}.
     */
    @PostMapping("/v1/results/token")
    public ResponseEntity<String> getJwt(@RequestHeader("Authorization") String authorization,
                                         @RequestBody WorkerpoolAuthorization workerpoolAuthorization) {
        final String workerAddress = workerpoolAuthorization.getWorkerWallet();
        final String challenge = authorizationService.getChallengeForWorker(workerpoolAuthorization);
        if (!authorizationService.isSignedByHimself(challenge, authorization, workerAddress)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (authorizationService.isAuthorizedOnExecutionWithDetailedIssue(workerpoolAuthorization).isPresent()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        final String jwtString = jwtService.getOrCreateJwt(workerpoolAuthorization.getWorkerWallet());
        return ResponseEntity.ok(jwtString);
    }

    /**
     * @deprecated Use {@code /v1/results} endpoint, will be removed in v10
     */
    @Deprecated(forRemoval = true)
    @PostMapping("/")
    public ResponseEntity<String> addResultDeprecated(@RequestHeader("Authorization") String token,
                                                      @RequestBody ResultModel model) {
        return addResult(token, model);
    }

    /**
     * Push result on IPFS through iExec Result Proxy.
     *
     * @param token JWT authorization
     * @param model Result payload containing the bytes to push on IPFS
     * @return A response entity indicating the status and details of the operation
     * <ul>
     * <li>HTTP 200 (OK) - If the result file was pushed on IPFS. The multihash will be included in the response body.
     * <li>HTTP 400 (BAD REQUEST) - If the operation was authorized but the file could not be pushed.
     * <li>HTTP 401 (UNAUTHORIZED) - If the operation was not authorized.
     * </ul>
     */
    @PostMapping("/v1/results")
    public ResponseEntity<String> addResult(@RequestHeader("Authorization") String token,
                                            @RequestBody ResultModel model) {
        if (!jwtService.isValidJwt(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED.value()).build();
        }

        final String walletAddress = jwtService.getWalletAddressFromJwtString(token);
        final boolean canUploadResult = proxyService.canUploadResult(model, walletAddress);

        if (!canUploadResult) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED.value()).build();
        }

        final String resultLink = proxyService.addResult(model);

        if (resultLink.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST.value()).build();
        }

        log.info("Result uploaded successfully [chainTaskId:{}, uploadRequester:{}, resultLink:{}]",
                model.getChainTaskId(), walletAddress, resultLink);

        return ok(resultLink);
    }

    /**
     * @deprecated Use {@code /v1/results/{chainTaskId}} endpoint, will be removed in v10
     */
    @Deprecated(forRemoval = true)
    @RequestMapping(method = RequestMethod.HEAD, path = "/results/{chainTaskId}")
    public ResponseEntity<String> isResultUploadedDeprecated(@PathVariable(name = "chainTaskId") String chainTaskId,
                                                             @RequestHeader("Authorization") String token) {
        return isResultUploaded(chainTaskId, token);
    }

    /**
     * Checks if a given task has been uploaded on IPFS through the current iExec Result Proxy instance.
     *
     * @param chainTaskId ID of the task
     * @param token       JWT authorization
     * @return A response entity indicating the status and details of the operation
     * <ul>
     * <li>HTTP 204 (NO CONTENT) - If the query was allowed and the given task result was uploaded through the current instance.
     * <li>HTTP 401 (UNAUTHORIZED) - If the client is not authorized to query the information.
     * <li>HTTP 404 (NOT FOUND) - If the query was allowed and no associated result was found.
     * </ul>
     */
    @RequestMapping(method = RequestMethod.HEAD, path = "/v1/results/{chainTaskId}")
    public ResponseEntity<String> isResultUploaded(@PathVariable(name = "chainTaskId") String chainTaskId,
                                                   @RequestHeader("Authorization") String token) {
        if (!jwtService.isValidJwt(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        boolean isResultFound = proxyService.isResultFound(chainTaskId);
        HttpStatus status = isResultFound ? HttpStatus.NO_CONTENT : HttpStatus.NOT_FOUND;
        return ResponseEntity.status(status).build();
    }

    /**
     * @deprecated Use {@code /v1/results/{chainTaskId}/ipfshash} endpoint, will be removed in v10
     */
    @Deprecated(forRemoval = true)
    @GetMapping("/results/{chainTaskId}/ipfshash")
    public ResponseEntity<String> getIpfsHashForTaskDeprecated(@PathVariable("chainTaskId") String chainTaskId) {
        return getIpfsHashForTask(chainTaskId);
    }

    /**
     * Retrieves ipfsHash for taskId if required
     *
     * @param chainTaskId ID of the task
     * @return IPFS multihash if found
     */
    @GetMapping("/v1/results/{chainTaskId}/ipfshash")
    public ResponseEntity<String> getIpfsHashForTask(@PathVariable("chainTaskId") String chainTaskId) {
        String ipfsHashForTask = ipfsNameService.getIpfsHashForTask(chainTaskId);
        if (ipfsHashForTask.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND.value()).build();
        }
        return ResponseEntity.ok(ipfsHashForTask);
    }

}

