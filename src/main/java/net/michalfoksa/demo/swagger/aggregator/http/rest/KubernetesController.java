package net.michalfoksa.demo.swagger.aggregator.http.rest;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.fabric8.kubernetes.api.model.Endpoints;
import io.fabric8.kubernetes.api.model.EndpointsList;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceList;
import io.fabric8.kubernetes.client.KubernetesClient;

@RestController
@RequestMapping("/kubernetes")
public class KubernetesController {

    Logger log = LoggerFactory.getLogger(KubernetesController.class);

    @Inject
    private KubernetesClient client;

    @GetMapping(path = "/services")
    public ServiceList allServices() {
        log.debug("REST Request to get all services");
        return client.services().list();
    }

    @GetMapping("/services/{serviceName}")
    public Service service(@PathVariable("serviceName") String serviceName) {
        log.debug("REST Request to get service [serviceName={}]", serviceName);
        return client.services().withName(serviceName).get();
    }

    @GetMapping("/endpoints")
    public EndpointsList endpoints() {
        log.debug("REST Request to get all endpoints");
        return client.endpoints().list();
    }

    @GetMapping("/endpoints/{serviceName}")
    public Endpoints serviceEndpoints(@PathVariable("serviceName") String serviceName) {
        log.debug("REST Request to get service endpoints [serviceName={}]", serviceName);
        return client.endpoints().withName(serviceName).get();
    }

    @GetMapping("/pods")
    public PodList pods() {
        log.debug("REST Request to get all pods");
        return client.pods().list();
    }
}
