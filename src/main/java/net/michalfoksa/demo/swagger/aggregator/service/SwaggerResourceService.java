package net.michalfoksa.demo.swagger.aggregator.service;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import springfox.documentation.swagger.web.SwaggerResource;

@Service
public class SwaggerResourceService {

    private static final Logger log = LoggerFactory.getLogger(SwaggerResourceService.class);

    private static final String SCHEME_SUFFIX = "/scheme";
    private static final String PORT_SUFFIX = "/port";
    private static final String HOST_SUFFIX = "/host";
    private static final String PATH_SUFFIX = "/path";
    private static final String VERSION_SUFFIX = "/version";
    private static final String DEFAULT_SWAGGER_VERSION = "2.0";

    @Inject
    private SwaggerAggregatorProperties properties;

    // Default path to apidocs on a remote host with leading slash.
    private String defaultPath;

    // Path to reverse proxy controller
    private String proxyPath;

    // Annotation prefix for an endpoint configuration. It is concatenation of
    // Kubernetes discovery annotation prefix and aggregator's polling
    // annotation prefix.
    private String annotationPrefix;

    // Map of serviceId to api-docs URI
    private Map<String, URI> service2ApiDocsUri = new HashMap<>();

    @PostConstruct
    public void init() throws Exception {
        annotationPrefix = properties.getEndpoint().getAnnotationPrefix();

        // Add leading slash if not set
        defaultPath = properties.getEndpoint().getDefaultPath();
        if (!defaultPath.startsWith("/")) {
            defaultPath = "/" + defaultPath;
        }

        // Add leading slash if not set
        proxyPath = properties.getProxyPath();
        if (!proxyPath.startsWith("/")) {
            proxyPath = "/" + proxyPath;
        }

        log.info("[defaultPath={}, proxyPath={}, annotationPrefix={}]", defaultPath, proxyPath, annotationPrefix);
    }

    /***
     * Return optional with URI to api-docs by service name.
     *
     * @param serviceName
     * @return
     */
    public Optional<URI> getApiDocsUri(String serviceName) {
        return Optional.ofNullable(service2ApiDocsUri.get(serviceName));
    }

    /***
     * Create {@link SwaggerResource} where service instance apidocs will be
     * proxied via local proxy controller.
     *
     * @param serviceName
     * @return
     */
    public SwaggerResource createResource(ServiceInstance instance) {
        String serviceName = instance.getServiceId();
        URI apiDocsUri = createApiDocsUri(instance);

        service2ApiDocsUri.put(serviceName, apiDocsUri);

        SwaggerResource swaggerResource = new SwaggerResource();
        swaggerResource.setName(serviceName);
        swaggerResource.setUrl(proxyPath + "/" + serviceName);

        String version = instance.getMetadata().get(annotationPrefix + VERSION_SUFFIX);
        swaggerResource.setSwaggerVersion(version != null? version : DEFAULT_SWAGGER_VERSION);

        log.info("New SwaggerResource created [name={}, localUrl={}, apiDocsUri={}]", swaggerResource.getName(),
                swaggerResource.getUrl(), apiDocsUri);
        return swaggerResource;
    }

    private URI createApiDocsUri(ServiceInstance instance) {
        Map<String, String> metadata = instance.getMetadata();
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUri(instance.getUri());

        String scheme = metadata.get(annotationPrefix + SCHEME_SUFFIX);
        if (!StringUtils.isEmpty(scheme)) {
            uriBuilder.scheme(scheme);
        }

        String host = metadata.get(annotationPrefix + HOST_SUFFIX);
        if (!StringUtils.isEmpty(host)) {
            uriBuilder.host(host);
        }

        String port = metadata.get(annotationPrefix + PORT_SUFFIX);
        if (!StringUtils.isEmpty(port)) {
            uriBuilder.port(port);
        }

        String path = metadata.get(annotationPrefix + PATH_SUFFIX);
        if (!StringUtils.isEmpty(path)) {
            uriBuilder.path(path);
        } else {
            uriBuilder.path(defaultPath);
        }

        return uriBuilder.build().toUri();
    }

}
