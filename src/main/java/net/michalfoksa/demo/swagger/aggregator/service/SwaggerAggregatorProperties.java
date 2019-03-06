package net.michalfoksa.demo.swagger.aggregator.service;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.kubernetes.discovery.KubernetesDiscoveryProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("aggregator")
public class SwaggerAggregatorProperties {

    @Inject
    private KubernetesDiscoveryProperties discoveryProperties;

    // Path to local reverse proxy controller
    private String proxyPath = "/api-docs/proxy";

    private Endpoint endpoint = new Endpoint();

    public String getProxyPath() {
        return proxyPath;
    }

    public void setProxyPath(String proxyPath) {
        // Strip trailing lash if exists
        if (proxyPath.endsWith("/")) {
            proxyPath = proxyPath.substring(0, proxyPath.length() - 1);
        }
        this.proxyPath = proxyPath;
    }

    public Endpoint getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(Endpoint endpoint) {
        this.endpoint = endpoint;
    }

    @Override
    public String toString() {
        return "SwaggerAggregatorProperties [proxyPath=" + proxyPath + ", endpoint=" + endpoint + "]";
    }

    /***
     * ApiDocs default enpoint configuration.
     *
     * @author Michal Foksa
     *
     */
    public class Endpoint {

        // Default path to Swagger API documentation in JSON format.
        private String defaultPath = "/v2/api-docs";

        // Annotation prefix used to configure apidocs endpoint for polling.

        // It is concatenation of Kubernetes discovery annotation prefix and
        // aggregator's polling annotation prefix
        private String annotationPrefix = "swagger.io/apidocs";

        @PostConstruct
        public void init() {
            // Concatenate Kubernetes discovery annotation prefix and
            // aggregator's polling annotation prefix
            annotationPrefix = (discoveryProperties.getMetadata().getAnnotationsPrefix() == null ? ""
                    : discoveryProperties.getMetadata().getAnnotationsPrefix()) + annotationPrefix;
        }

        public String getDefaultPath() {
            return defaultPath;
        }

        public void setDefaultPath(String defaultPath) {
            this.defaultPath = defaultPath;
        }

        public String getAnnotationPrefix() {
            return annotationPrefix;
        }

        public void setAnnotationPrefix(String annotationPrefix) {
            this.annotationPrefix = annotationPrefix;
        }

        @Override
        public String toString() {
            return "Polling [defaultPath=" + defaultPath + ", annotationPrefix=" + annotationPrefix + "]";
        }
    }
}
