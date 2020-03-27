package com.iexec.resultproxy.version;

import com.iexec.common.utils.VersionUtils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class VersionService {

    @Value("${version}")
    private String version;

    public String getVersion() {
        return version;
    }

    public boolean isSnapshot() {
        return VersionUtils.isSnapshot(version);
    }

}
