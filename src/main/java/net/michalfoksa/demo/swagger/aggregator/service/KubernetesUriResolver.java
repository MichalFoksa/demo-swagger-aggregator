package net.michalfoksa.demo.swagger.aggregator.service;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.composite.CompositeDiscoveryClient;
import org.springframework.cloud.kubernetes.discovery.KubernetesDiscoveryClient;
import org.springframework.stereotype.Service;

/***
 * Service discovery using Kubernetes environment variables.
 *
 * @author Michal Foksa
 *
 */
@Service
public class KubernetesUriResolver implements InitializingBean, ServiceUriResolver {

    Logger log = LoggerFactory.getLogger(KubernetesUriResolver.class);

    @Value("${service.discovery.client.defaultprototol:http}")
    private String defaultProtocol;

    @Inject
    private DiscoveryClient discoveryClient;

    private String upperCaseDefaultProtocol;

    @Override
    public void afterPropertiesSet() throws Exception {
        upperCaseDefaultProtocol = defaultProtocol.toUpperCase();
    }

    @Override
    public URI getUri(String serviceName) {

        return getAllDiscoveryClients().stream()
                .filter(client -> client instanceof KubernetesDiscoveryClient
                        && !client.getInstances(serviceName).isEmpty())
                .findAny().map(client -> {
                    /**
                     * Service discovery using Kubernetes environment variables.
                     * Host and port variables format is:
                     *
                     * [SERVICE_NAME]_SERVICE_HOST
                     * [SERVICE_NAME]_SERVICE_PORT_[PORT_NAME]
                     */
                    // Replace each dash with underscore
                    String normalizeServiceName = serviceName.replaceAll("-", "_").toUpperCase();

                    String host = System.getenv(normalizeServiceName + "_SERVICE_HOST");
                    String port = System.getenv(normalizeServiceName + "_SERVICE_PORT_" + upperCaseDefaultProtocol);
                    URI uri = URI.create(defaultProtocol + "://" + host + ":" + port);

                    log.debug("Service name to service URI [serviceName={}, uri={}]", serviceName, uri);

                    return uri;
                }).orElse(getAnyInstanceUri(serviceName).orElse(null));
    }

    private List<DiscoveryClient> getAllDiscoveryClients() {
        log.trace("discoveryClient class [class={}]", discoveryClient.getClass());

        if (discoveryClient instanceof CompositeDiscoveryClient) {
            return new ArrayList<>(((CompositeDiscoveryClient) discoveryClient).getDiscoveryClients());
        }
        return Arrays.asList(discoveryClient);
    }

    private Optional<URI> getAnyInstanceUri(String serviceName) {
        // Return any instance URI
        return discoveryClient.getInstances(serviceName).stream().findAny().map(ServiceInstance::getUri);
    }

}
