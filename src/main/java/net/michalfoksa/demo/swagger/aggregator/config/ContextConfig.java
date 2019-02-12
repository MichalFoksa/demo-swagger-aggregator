package net.michalfoksa.demo.swagger.aggregator.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextListener;

import net.michalfoksa.demo.swagger.aggregator.context.KubernetesRuntimeContext;
import net.michalfoksa.demo.swagger.aggregator.context.RuntimeContext;

@Configuration
public class ContextConfig {

    Logger log = LoggerFactory.getLogger(ContextConfig.class);

    // Exposes current request to the current thread
    @Bean
    public RequestContextListener requestContextListener(){
        return new RequestContextListener();
    }

    @Value("${spring.application.name}")
    private String application;

    @Value("${spring.cloud.client.hostname}")
    private String hostname;

    @Value("${spring.cloud.client.ip-address}")
    private String ipAddress;

    @Value("${POD_NAME:#{null}}")
    private String podName;

    @Value("${POD_IP:#{null}}")
    private String podIp;

    @Value("${POD_NAMESPACE:#{null}}")
    private String podNamespace;

    @Value("${POD_SERVICE_ACCOUNT:#{null}}")
    private String podServiceAccount;

    @Value("${NODE_NAME:#{null}}")
    private String nodeName;

    @Bean
    public RuntimeContext runtimeContext() {
        KubernetesRuntimeContext rt = new KubernetesRuntimeContext().application(application)
                .hostname(podName != null ? podName : hostname).ip(podIp != null ? podIp : ipAddress)
                .podNamespace(podNamespace).podServiceAccount(podServiceAccount).nodeName(nodeName);

        log.debug("Runtime context [runtimeContext={}]", rt);

        return rt;
    }

}
