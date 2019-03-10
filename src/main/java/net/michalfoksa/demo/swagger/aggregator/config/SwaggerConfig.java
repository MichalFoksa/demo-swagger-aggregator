package net.michalfoksa.demo.swagger.aggregator.config;

import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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

    @Primary
    @Bean
    public SwaggerResourcesProvider swaggerResourcesProvider(DiscoveryClient discoveryClient,
            Predicate<ServiceInstance> pollFilter, SwaggerResourceService resourceService) {

        // Return any instance of each service
        return () -> discoveryClient.getServices().stream().sorted(String::compareTo)
                // Convert service name to service instance and filter those
                // where documentation polling is enabled by an annotation (most
                // likely "swagger.io/apidocs.poll").
                .map(serviceName -> discoveryClient.getInstances(serviceName).stream()
                        .filter(pollFilter).findAny()
                        .orElse(null))
                .filter(Objects::nonNull)
                // Convert service instance to Swagger resource
                .map(resourceService::createResource)
                .collect(Collectors.toList());
    }

    @Bean
    public Predicate<ServiceInstance> pollFilter(SwaggerAggregatorProperties aggregatorProperties) {
        return (instance) -> "true".equals(instance.getMetadata()
                .get(aggregatorProperties.getEndpoint().getAnnotationPrefix() + SwaggerResourceService.POLL_SUFFIX));
    }

}
