package net.michalfoksa.demo.swagger.aggregator.config;

import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import net.michalfoksa.demo.swagger.aggregator.service.SwaggerResourceService;
import springfox.documentation.swagger.web.SwaggerResourcesProvider;

@Configuration
public class SwaggerConfig {

    @Primary
    @Bean
    public SwaggerResourcesProvider swaggerResourcesProvider(DiscoveryClient discoveryClient,
            SwaggerResourceService resourceService) {

        // Return any instance for each service
        return () -> discoveryClient.getServices().stream().sorted(String::compareTo)
                .map(serviceName -> discoveryClient.getInstances(serviceName).stream().findAny()
                        .map(instance -> resourceService.createResource(instance)).orElse(null))
                .filter(Objects::nonNull).collect(Collectors.toList());
    }

}
