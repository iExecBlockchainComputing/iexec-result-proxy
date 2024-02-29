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

package com.iexec.resultproxy.authorization;

import com.iexec.commons.poco.chain.WorkerpoolAuthorization;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@CompoundIndex(name = "workerpool_authorization", def = "{'chainTaskId': 1, 'workerWallet': 1}", unique = true)
@Getter
@NoArgsConstructor
public class Authorization {
    @Id
    private String id;

    @Version
    private Long version;

    private String chainTaskId;
    private String workerWallet;
    private String enclaveChallenge;

    public Authorization(WorkerpoolAuthorization workerpoolAuthorization) {
        this.chainTaskId = workerpoolAuthorization.getChainTaskId();
        this.workerWallet = workerpoolAuthorization.getWorkerWallet();
        this.enclaveChallenge = workerpoolAuthorization.getEnclaveChallenge();
    }

}
