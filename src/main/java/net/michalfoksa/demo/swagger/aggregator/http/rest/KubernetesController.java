package net.michalfoksa.demo.swagger.aggregator.http.rest;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.fabric8.kubernetes.api.model.Endpoints;
import io.fabric8.kubernetes.api.model.EndpointsList;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceList;
import io.fabric8.kubernetes.client.KubernetesClient;

@RestController("/kubernetes")
public class KubernetesController {

    Logger log = LoggerFactory.getLogger(KubernetesController.class);

    @Inject
    private KubernetesClient client;

    @GetMapping("/services")
    public ServiceList allServices() {
        return client.services().list();
    }

    @GetMapping("/services/{serviceName}")
    public Service service(@RequestParam("serviceName") String serviceName) {
        log.debug("REST Request to get service [serviceName={}]", serviceName);
        return client.services().withName(serviceName).get();
    }

    @GetMapping("/endpoints")
    public EndpointsList endpoints(@RequestParam("serviceName") String serviceName) {
        log.debug("REST Request to get service endpoints [serviceName={}]", serviceName);
        return client.endpoints().list();
    }

    @GetMapping("/services/{serviceName}/endpoints")
    public Endpoints serviceEndpoints(@RequestParam("serviceName") String serviceName) {
        log.debug("REST Request to get service endpoints [serviceName={}]", serviceName);
        return client.endpoints().withName(serviceName).get();
    }
}
