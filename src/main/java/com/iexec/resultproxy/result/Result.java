package com.iexec.resultproxy.result;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Result {

    private String chainTaskId;
    private String image;
    private String cmd;
    private String stdout;
    private String deterministHash;

}
