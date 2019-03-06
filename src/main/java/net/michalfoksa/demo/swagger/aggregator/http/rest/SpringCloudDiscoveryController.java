package net.michalfoksa.demo.swagger.aggregator.http.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.composite.CompositeDiscoveryClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/discovery")
public class SpringCloudDiscoveryController {

    private static final Logger log = LoggerFactory.getLogger(SpringCloudDiscoveryController.class);

    @Inject
    private DiscoveryClient client;

    @GetMapping(path = "/clients")
    public List<Class<?>> allDiscoveryClients() {
        log.debug("REST Request to get all discovery clinets");

        ArrayList<Class<?>> ret = new ArrayList<>();
        ret.add(client.getClass());

        if (client instanceof CompositeDiscoveryClient) {
            ret.addAll(((CompositeDiscoveryClient) client).getDiscoveryClients().stream().map(Object::getClass)
                    .collect(Collectors.toList()));
        }
        return ret;
    }

    @GetMapping(path = "/services")
    public List<String> allServices() {
        log.debug("REST Request to get all services");
        return client.getServices();
    }

    @GetMapping(path = "/instances")
    public List<ServiceInstance> allInstances() {
        log.debug("REST Request to get all instances");
        return client.getServices().stream().map(serviceName -> client.getInstances(serviceName))
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    @GetMapping(path = "/instances/{serviceName}")
    public List<ServiceInstance> serviceInstances(@PathVariable("serviceName") String serviceName) {
        log.debug("REST Request to get service instances [serviceName={}]", serviceName);
        return client.getInstances(serviceName);
    }

}
