package net.michalfoksa.demo.swagger.aggregator.service;

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
public class KubernetesEnvVarsUriResolver implements InitializingBean, UrlResolver
{

    @Value("${service.discovery.client.defaultprototol:http}")
    private String defaultProtocol;

    private String upperCaseDefaultProtocol;

    @Override
    public void afterPropertiesSet() throws Exception {
        upperCaseDefaultProtocol = defaultProtocol.toUpperCase();
    }

    /***
     * Create service URI from service name.
     *
     * @param serviceName
     * @return service URI
     */
    @Override
    public String getUrl(String serviceName) {

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

        return defaultProtocol + "://" + host + ":" + port;
    }

}
