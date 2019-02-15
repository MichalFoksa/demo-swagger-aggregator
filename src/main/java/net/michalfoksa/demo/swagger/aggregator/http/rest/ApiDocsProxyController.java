package net.michalfoksa.demo.swagger.aggregator.http.rest;

import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;

import net.michalfoksa.demo.swagger.aggregator.service.SwaggerResourceService;

@RestController
public class ApiDocsProxyController {
    public static final String DEFAULT_PROXY_PATH = "/api-docs/proxy";
    private static final String HAL_MEDIA_TYPE = "application/hal+json";

    Logger log = LoggerFactory.getLogger(ApiDocsProxyController.class);

    @Value("${aggregator.api-docs-proxy.path:" + ApiDocsProxyController.DEFAULT_PROXY_PATH + "}")
    private String proxyControlerPath;

    @Inject
    private SwaggerResourceService resourceService;

    private RestTemplate restTemplate = new RestTemplate();

    @RequestMapping(value = DEFAULT_PROXY_PATH + "/**", method = RequestMethod.GET, produces = {
            APPLICATION_JSON_VALUE, HAL_MEDIA_TYPE })
    // @PropertySourcedMapping(
    // value = "${documentation.swagger.proxycontroler.path}", propertyKey =
    // "documentation.swagger.proxycontroler.path")
    @ResponseBody
    public ResponseEntity<JsonNode> getDocumentation(
            @RequestParam(value = "group", required = false) String swaggerGroup, HttpServletRequest servletRequest) {

        log.debug("Path [servletPath={}, contextPath={}] ", servletRequest.getServletPath(),
                servletRequest.getContextPath());

        String serviceName = servletRequest.getServletPath().substring(proxyControlerPath.length() + 1);

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

        Map<String, String> uriVariables = new HashMap<>();
        if (swaggerGroup != null) {
            uriVariables.put("group", swaggerGroup);
        }

        return restTemplate.getForObject(apiDocsUri.toString(), JsonNode.class, uriVariables);
    }

}