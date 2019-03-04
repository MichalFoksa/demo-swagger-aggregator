package net.michalfoksa.demo.swagger.aggregator.config;

import java.util.Objects;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import net.michalfoksa.demo.swagger.aggregator.service.SwaggerAggregatorProperties;
import net.michalfoksa.demo.swagger.aggregator.service.SwaggerResourceService;
import springfox.documentation.swagger.web.SwaggerResourcesProvider;

@Configuration
public class SwaggerConfig {

    @Inject
    private SwaggerAggregatorProperties aggregatorProperties;

    @Primary
    @Bean
    public SwaggerResourcesProvider swaggerResourcesProvider(DiscoveryClient discoveryClient,
            SwaggerResourceService resourceService) {

        // Return any instance for each service
        return () -> discoveryClient.getServices().stream().sorted(String::compareTo)
                .map(serviceName -> discoveryClient.getInstances(serviceName).stream()
                        .filter(this::isPollingEligible).findAny()
                        .map(instance -> resourceService.createResource(instance)).orElse(null))
                .filter(Objects::nonNull).collect(Collectors.toList());
    }

    private boolean isPollingEligible(ServiceInstance instance) {
        return "true".equals(instance.getMetadata()
                .get(aggregatorProperties.getEndpoint().getAnnotationPrefix() + SwaggerResourceService.POLL_SUFFIX));
    }

}
