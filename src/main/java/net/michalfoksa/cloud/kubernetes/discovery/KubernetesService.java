/*
 * Copyright 2013-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package net.michalfoksa.cloud.kubernetes.discovery;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import io.fabric8.kubernetes.api.model.ServicePort;

public class KubernetesService implements ServiceInstance {

    private static final String HTTP_PREFIX = "http://";
    private static final String HTTPS_PREFIX = "https://";
    private static final String COLN = ":";

    private final String type;
    private final String name;
    private final String namespace;
    private final String clusterIP;
    private final String externalName;
    private final ServicePort servicePort;
    private final Boolean secure;
    private final Map<String, String> metadata;


    public KubernetesService(String name, String namespace, String type, String clusterIP, String externalName,
            ServicePort servicePort, Map<String, String> metadata, Boolean secure) {

        Assert.hasText(type, "[Assertion failed] - type must not be null or empty");

        if ( StringUtils.isEmpty(clusterIP) && StringUtils.isEmpty(externalName) ) {
            throw new NullPointerException("clusterIP or externalName must not be empty");
        }

        this.name = name;
        this.namespace = namespace;
        this.type = type;
        this.clusterIP = clusterIP;
        this.externalName = externalName;
        this.servicePort = servicePort;
        this.metadata = metadata;
        this.secure = secure;
    }

    public String getName() {
        return name;
    }

    @Override
    public String getServiceId() {
        return name;
    }

    @Override
    public String getInstanceId() {
        return (namespace != null ? namespace + "/" : "") + name;
    }

    @Override
    public String getHost() {
        if ("ClusterIP".equals(type)) {
            return clusterIP;
        }
        return externalName;
    }

    @Override
    public int getPort() {
        return servicePort.getPort();
    }

    @Override
    public boolean isSecure() {
        return secure;
    }

    @Override
    public URI getUri() {
        StringBuilder sb = new StringBuilder();

        if (isSecure()) {
            sb.append(HTTPS_PREFIX);
        } else {
            sb.append(HTTP_PREFIX);
        }

        sb.append(getHost()).append(COLN).append(getPort());
        try {
            return new URI(sb.toString());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public String getNamespace() {
        return namespace;
    }

    public String getType() {
        return type;
    }

    public String getClusterIP() {
        return clusterIP;
    }

    public String getExternalName() {
        return externalName;
    }

    @Override
    public Map<String, String> getMetadata() {
        return metadata;
    }
}

