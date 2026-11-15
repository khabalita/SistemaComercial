package com.khabalita.sistemacomercial.config;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Component;

@Component
public class ApplicationVersion {

    private final String version;

    public ApplicationVersion(ObjectProvider<BuildProperties> buildProperties) {
        BuildProperties properties = buildProperties.getIfAvailable();
        this.version = properties == null ? "desarrollo" : properties.getVersion();
    }

    public String value() {
        return version;
    }
}
