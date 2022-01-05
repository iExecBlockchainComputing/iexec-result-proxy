package com.iexec.resultproxy.version;

import com.iexec.common.utils.VersionUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Service;

@Service
public class VersionService {

    @Autowired
    BuildProperties buildProperties;

    public String getVersion() {
        return buildProperties.getVersion();
    }

    public boolean isSnapshot() {
        return VersionUtils.isSnapshot(getVersion());
    }

}
