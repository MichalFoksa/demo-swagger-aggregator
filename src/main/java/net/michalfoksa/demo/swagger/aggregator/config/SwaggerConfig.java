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

        return () -> discoveryClient.getServices().stream().sorted(String::compareTo)
                .map(serviceName -> resourceService.createResource(serviceName)).filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

}
