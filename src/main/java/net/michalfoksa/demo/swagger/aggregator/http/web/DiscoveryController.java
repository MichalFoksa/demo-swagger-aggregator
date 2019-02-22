package net.michalfoksa.demo.swagger.aggregator.http.web;

import java.util.stream.Collectors;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import net.michalfoksa.demo.swagger.aggregator.context.RuntimeContext;

@Controller
@RequestMapping(path = "/discovery")
public class DiscoveryController {

    Logger log = LoggerFactory.getLogger(DiscoveryController.class);

    @Inject
    private RuntimeContext runtimeContext;

    @Inject
    private DiscoveryClient discoveryClient;


    @GetMapping()
    public @ResponseBody String getApplicationInfo() {
        log.trace("discoveryClient implementation [class={}]", discoveryClient.getClass());

        StringBuilder sb = new StringBuilder();

        sb.append("<table>\n<tr>");
        String runtimeTable = runtimeContext.getAllFieldsMap().entrySet().stream()
                .sorted((a, b) -> a.getKey().compareTo(b.getKey()))
                .map(entry -> "<td>" + entry.getKey() + ":</td> <td>" + entry.getValue().toString() + "</td>")
                .collect(Collectors.joining("</tr>\n<tr>"));
        sb.append(runtimeTable).append("</tr>\n</table>\n");

        sb.append("<br/>\n");

        sb.append("<table rules=\"rows\">\n<tr>");
        try {
            String servicesTable =
                    discoveryClient.getServices().stream().sorted(String::compareTo)
                    .map(name -> "<tr><td>Service:</td><th colspan=\"2\">" + name + "</th></tr>\n"
                                    + "<tr><th colspan=\"3\"> Instaces </th></tr>\n"
                            + discoveryClient.getInstances(name).stream().map(instance ->

                                    "<tr><td> instanceId :</td> <td>" + instance.getInstanceId() + "</td><td/></tr>\n"
                            + "<tr><td> scheme :</td> <td>" + instance.getScheme() + "</td><td/></tr>\n"
                            + "<tr><td> host :</td> <td>" + instance.getHost() + "</td><td/></tr>\n"
                            + "<tr><td> port :</td> <td>" + instance.getPort() + "</td><td/></tr>\n"
                            + "<tr><td> uri :</td> <td>" + instance.getUri() + "</td><td/></tr>\n"
                            // Metadata
                            + "<tr><th>metadata</th><th colspan=\"2\"> instance " + instance.getInstanceId()
                            + " </th></tr>\n"
                            + instance.getMetadata().entrySet().stream()
                            .sorted((a, b) -> a.getKey().compareTo(b.getKey()))
                            .map(entry -> "<td/><td>" + entry.getKey() + ":</td> <td>"
                                    + entry.getValue().toString() + "</td>")
                            .collect(Collectors.joining("</tr>\n<tr>"))

                                    ).collect(Collectors.joining("</tr>\n<tr>")))
                    .collect(Collectors.joining("</tr>\n<tr></tr>\n<tr>"));
            sb.append(servicesTable);
        } catch (Exception e) {
            sb.append(e.getMessage());
            sb.append(e.getCause() != null ? "<br/>\n" + e.getCause().getMessage() : "");
            log.error("Error", e);
        }
        sb.append("</tr>\n</table>\n");

        return sb.toString();
    }

}
