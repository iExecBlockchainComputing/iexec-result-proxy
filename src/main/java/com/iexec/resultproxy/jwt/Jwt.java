/*
 * Copyright 2022 IEXEC BLOCKCHAIN TECH
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

package com.iexec.resultproxy.jwt;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Jwt {

    @Id
    private String id;

    @Version
    private Long version;

    @Indexed(unique = true)
    private String walletAddress;
    private String jwtString;

    public Jwt(String walletAddress, String jwtString) {
        this.walletAddress = walletAddress;
        this.jwtString = jwtString;
    }

    public Jwt withNewToken(String jwtToken) {
        return new Jwt(id, version, walletAddress, jwtToken);
    }

}
