package net.michalfoksa.demo.swagger.aggregator.http.rest;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import com.fasterxml.jackson.databind.JsonNode;

import net.michalfoksa.demo.swagger.aggregator.service.SwaggerAggregatorProperties;
import net.michalfoksa.demo.swagger.aggregator.service.SwaggerResourceService;
import springfox.documentation.swagger2.web.Swagger2Controller;

/***
 * This is reverse proxy which facilitate communication to discovered services.
 *
 * @author Michal Foksa
 *
 */
@RestController
public class ApiDocsProxyController {

    private static final Logger log = LoggerFactory.getLogger(ApiDocsProxyController.class);

    /**
     * Proxy provides hal+json because {@link Swagger2Controller} it also
     * provides.
     */
    private static final String HAL_MEDIA_TYPE = "application/hal+json";

    @Inject
    private SwaggerAggregatorProperties properties;

    @Inject
    private SwaggerResourceService resourceService;

    @Inject
    private RequestMappingHandlerMapping handlerMapping;

    private RestTemplate restTemplate = new RestTemplate();

    /***
     * Register this controller to URL path from proxyPath property of
     * {@link SwaggerAggregatorProperties}.
     *
     * @throws NoSuchMethodException
     */
    @PostConstruct
    public void init() throws NoSuchMethodException {

        RequestMappingInfo requestMappingInfo = RequestMappingInfo.paths(properties.getProxyPath() + "/**")
                .methods(RequestMethod.GET)
                .produces(MediaType.APPLICATION_JSON_VALUE, HAL_MEDIA_TYPE).build();

        handlerMapping.registerMapping(requestMappingInfo, this,
                ApiDocsProxyController.class.getDeclaredMethod("getDocumentation", String.class,
                        HttpServletRequest.class));
    }

    public ResponseEntity<JsonNode> getDocumentation(
            @RequestParam(value = "group", required = false) String swaggerGroup, HttpServletRequest servletRequest) {

        log.trace("Path [servletPath={}, contextPath={}] ", servletRequest.getServletPath(),
                servletRequest.getContextPath());

        String serviceName = servletRequest.getServletPath().substring(properties.getProxyPath().length() + 1);

        return resourceService.getApiDocsUri(serviceName).map(uri -> {
            log.info("Key [serviceName={}, apiDocsUri={}, group={}] ", serviceName, uri, swaggerGroup);
            return new ResponseEntity<>(fetchApiDocs(uri, swaggerGroup), HttpStatus.OK);
        }).orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /***
     * Read apiDocs from remote URI. Also forward input Swagger group parameter.
     *
     * @param apiDocsUri
     * @param swaggerGroup
     * @return
     */
    private JsonNode fetchApiDocs(URI apiDocsUri, String swaggerGroup) {
        log.debug("Fetching api docs [apiDocsUri={}]", apiDocsUri);

        Map<String, String> uriVariables = new HashMap<>();
        if (swaggerGroup != null) {
            uriVariables.put("group", swaggerGroup);
        }

        return restTemplate.getForObject(apiDocsUri.toString(), JsonNode.class, uriVariables);
    }

}