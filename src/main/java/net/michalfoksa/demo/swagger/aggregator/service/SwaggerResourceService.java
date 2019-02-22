package net.michalfoksa.demo.swagger.aggregator.service;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.stereotype.Service;

import net.michalfoksa.demo.swagger.aggregator.http.rest.ApiDocsProxyController;
import springfox.documentation.swagger.web.SwaggerResource;
import springfox.documentation.swagger2.web.Swagger2Controller;

@Service
public class SwaggerResourceService {

    Logger log = LoggerFactory.getLogger(SwaggerResourceService.class);

    // Default to Swagger2Controller.DEFAULT_URL
    @Value("${aggregator.polling.defaultPath:" + Swagger2Controller.DEFAULT_URL + "}")
    private String defaulthApiDocsPath;

    @Value("${aggregator.proxyPath:" + ApiDocsProxyController.DEFAULT_PROXY_PATH + "}")
    private String proxyControlerPath;

    // Map of serviceId to api-docs URI
    private Map<String, URI> service2ApiDocsUri = new HashMap<>();

    @PostConstruct
    public void init() throws Exception {
        // Strip leading slash if exists
        if (defaulthApiDocsPath.startsWith("/")) {
            defaulthApiDocsPath = defaulthApiDocsPath.substring(1);
        }
        log.info("[defaulthApiDocsPath={}, proxyControlerPath={}]", defaulthApiDocsPath, proxyControlerPath);
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
        swaggerResource.setUrl(proxyControlerPath + "/" + serviceName);
        swaggerResource.setSwaggerVersion("2.0");

        log.info("New SwaggerResource created [name={}, localUrl={}, apiDocsUri={}]", swaggerResource.getName(),
                swaggerResource.getUrl(), apiDocsUri);
        return swaggerResource;
    }

    private URI createApiDocsUri(ServiceInstance instance) {
        URI uri = instance.getUri();
        return URI.create(uri + (uri.toString().endsWith("/") ? "" : "/") + defaulthApiDocsPath);
    }

}
