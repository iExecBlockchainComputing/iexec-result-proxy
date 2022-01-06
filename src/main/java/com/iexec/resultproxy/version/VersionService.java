package com.iexec.resultproxy.version;

import com.iexec.common.utils.VersionUtils;

import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Service;

@Service
public class VersionService {

    private final BuildProperties buildProperties;

    public VersionService(BuildProperties buildProperties) {
        this.buildProperties = buildProperties;
    }

    public String getVersion() {
        return buildProperties.getVersion();
    }

    public boolean isSnapshot() {
        return VersionUtils.isSnapshot(getVersion());
    }

}
