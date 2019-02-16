package net.michalfoksa.demo.swagger.aggregator.service;

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/***
 * Service discovery using Kubernetes environment variables.
 *
 * @author Michal Foksa
 *
 */
@Service
public class KubernetesEnvVarsUriResolver implements InitializingBean, ServiceUriResolver {

    Logger log = LoggerFactory.getLogger(KubernetesEnvVarsUriResolver.class);

    @Value("${service.discovery.client.defaultprototol:http}")
    private String defaultProtocol;

    private String upperCaseDefaultProtocol;

    @Override
    public void afterPropertiesSet() throws Exception {
        upperCaseDefaultProtocol = defaultProtocol.toUpperCase();
    }

    @Override
    public URI getUri(String serviceName) {

        /**
         * Service discovery using Kubernetes environment variables. Host
         * and port variables format is:
         *
         * [SERVICE_NAME]_SERVICE_HOST
         * [SERVICE_NAME]_SERVICE_PORT_[PORT_NAME]
         */
        String host = System.getenv(serviceName.toUpperCase() + "_SERVICE_HOST");
        String port = System
                .getenv(serviceName.toUpperCase() + "_SERVICE_PORT_" + upperCaseDefaultProtocol);
        URI uri = URI.create(defaultProtocol + "://" + host + ":" + port);

        log.debug("Service name to service URI [serviceName={}, uri={}]", serviceName, uri);

        return uri;
    }

}
