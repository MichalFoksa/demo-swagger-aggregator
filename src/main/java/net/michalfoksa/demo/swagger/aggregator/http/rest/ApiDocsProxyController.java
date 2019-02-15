package net.michalfoksa.demo.swagger.aggregator.http.rest;

import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

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

import net.michalfoksa.demo.swagger.aggregator.service.SwaggerResourceService;
import springfox.documentation.spring.web.json.Json;

@RestController
public class ApiDocsProxyController {
    public static final String DEFAULT_PROXY_PATH = "/api-docs/proxy";
    private static final String HAL_MEDIA_TYPE = "application/hal+json";

    Logger log = LoggerFactory.getLogger(ApiDocsProxyController.class);

    @Value("${aggregator.api-docs-proxy.path:" + ApiDocsProxyController.DEFAULT_PROXY_PATH + "}")
    private String proxyControlerPath;

    @Inject
    private SwaggerResourceService resourceService;

    @RequestMapping(value = DEFAULT_PROXY_PATH + "/**", method = RequestMethod.GET, produces = {
            APPLICATION_JSON_VALUE, HAL_MEDIA_TYPE })
    // @PropertySourcedMapping(
    // value = "${documentation.swagger.proxycontroler.path}", propertyKey =
    // "documentation.swagger.proxycontroler.path")
    @ResponseBody
    public ResponseEntity<Json> getDocumentation(
            @RequestParam(value = "group", required = false) String swaggerGroup,
            HttpServletRequest servletRequest) {

        log.debug("Path [servletPath={}, contextPath={}] ", servletRequest.getServletPath(),
                servletRequest.getContextPath());

        String serviceName = servletRequest.getServletPath().substring(proxyControlerPath.length() + 1);

        return resourceService.getApiDocsUri(serviceName).map(uri -> {
            log.info("Key [serviceName={}, apiDocsUri={}] ", serviceName, uri);
            return new ResponseEntity<>(new Json(
                    "{\"swagger\":\"2.0\",\"info\":{\"title\":\"bodyshop API\"},\"host\":\"localhost:10081\",\"basePath\":\"/\",\"tags\":[{\"name\":\"work-orders-controller\",\"description\":\"the WorkOrders API\"}],\"paths\":{\"/workorders\":{\"post\":{\"tags\":[\"workOrders\"],\"summary\":\"createWorkOrder\",\"description\":\"Create a work order\",\"operationId\":\"createWorkOrder\",\"consumes\":[\"application/json\"],\"produces\":[\"application/json\"],\"parameters\":[{\"in\":\"body\",\"name\":\"workOrder\",\"description\":\"Work order for a workstation\",\"required\":true,\"schema\":{\"$ref\":\"#/definitions/WorkOrder\"}}],\"responses\":{\"200\":{\"description\":\"Success\",\"schema\":{\"type\":\"array\",\"items\":{\"$ref\":\"#/definitions/CreateWorkOrderResponse\"}}},\"201\":{\"description\":\"Created\"},\"401\":{\"description\":\"Unauthorized\"},\"403\":{\"description\":\"Forbidden\"},\"404\":{\"description\":\"Not Found\"}},\"deprecated\":false}}},\"definitions\":{\"CreateWorkOrderResponse\":{\"type\":\"object\",\"properties\":{\"body\":{\"$ref\":\"#/definitions/Workstation\"},\"messageContext\":{\"$ref\":\"#/definitions/MessageContext\"},\"runtimeContext\":{\"type\":\"object\",\"description\":\"Describes runtime environment of a pod or container executing the request.\"}},\"title\":\"CreateWorkOrderResponse\"},\"MessageContext\":{\"type\":\"object\",\"properties\":{\"correlationId\":{\"type\":\"string\",\"description\":\"Client generated ID to correlate all service requests in one business operation (transaction).\"}},\"title\":\"MessageContext\",\"description\":\"Describes how a request or response was created.\"},\"WorkOrder\":{\"type\":\"object\",\"required\":[\"nextStations\",\"parameters\",\"workstationName\"],\"properties\":{\"nextStations\":{\"type\":\"array\",\"items\":{\"$ref\":\"#/definitions/Workstation\"}},\"parameters\":{\"type\":\"object\",\"description\":\"Free named parameters. They do not have any pupose, just to pass some data through assebly line workstations.\",\"additionalProperties\":{\"type\":\"string\"}},\"workstationName\":{\"type\":\"string\",\"description\":\"Name of the workstation which is supposed to execute the work, e.g.: car body.\"}},\"title\":\"WorkOrder\",\"description\":\"Work order for a workstation.\"},\"Workstation\":{\"type\":\"object\",\"required\":[\"name\",\"parameters\"],\"properties\":{\"name\":{\"type\":\"string\",\"description\":\"Name of the workstation\"},\"parameters\":{\"type\":\"object\",\"description\":\"Free named parameters. They do not have any pupose, just to pass some data through assebly line workstations.\",\"additionalProperties\":{\"type\":\"string\"}},\"url\":{\"type\":\"string\",\"description\":\"Optional URL of the workstation. Is is appended with `/works`.\"}},\"title\":\"Workstation\",\"description\":\"Workstation\"}}}"),
                    HttpStatus.OK);
        }).orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

}