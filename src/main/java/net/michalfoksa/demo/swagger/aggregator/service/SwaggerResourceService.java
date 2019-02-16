package net.michalfoksa.demo.swagger.aggregator.service;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import net.michalfoksa.demo.swagger.aggregator.http.rest.ApiDocsProxyController;
import springfox.documentation.swagger.web.SwaggerResource;
import springfox.documentation.swagger2.web.Swagger2Controller;

@Service
public class SwaggerResourceService implements InitializingBean {

    Logger log = LoggerFactory.getLogger(SwaggerResourceService.class);

    // Default to Swagger2Controller.DEFAULT_URL
    @Value("${aggregator.documentation.swagger.v2.defaultpath:" + Swagger2Controller.DEFAULT_URL + "}")
    private String defaulthApiDocsPath;

    @Value("${aggregator.api-docs-proxy.path:" + ApiDocsProxyController.DEFAULT_PROXY_PATH + "}")
    private String proxyControlerPath;

    @Inject
    private UrlResolver urlResolver;

    // Map of serviceId to api-docs URI
    private Map<String, URI> service2ApiDocsUri = new HashMap<>();

    @Override
    public void afterPropertiesSet() throws Exception {
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
    public SwaggerResource createResource(String serviceName) {
        URI apiDocsUri = createApiDocsUri(serviceName);

        SwaggerResource swaggerResource = new SwaggerResource();
        swaggerResource.setName(serviceName);
        swaggerResource.setUrl(proxyControlerPath + "/" + serviceName);
        swaggerResource.setSwaggerVersion("2.0");

        service2ApiDocsUri.put(serviceName, apiDocsUri);

        log.info("New SwaggerResource created [name={}, localUrl={}, apiDocsUri={}]", swaggerResource.getName(),
                swaggerResource.getUrl(), apiDocsUri);
        return swaggerResource;
    }

    private URI createApiDocsUri(String serviceName) {
        String serviceUrl = urlResolver.getUrl(serviceName);
        return URI.create(serviceUrl + (serviceUrl.endsWith("/") ? "" : "/") + defaulthApiDocsPath);
    }

}
