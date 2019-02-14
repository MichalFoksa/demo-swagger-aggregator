package net.michalfoksa.demo.swagger.aggregator.config;

import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import springfox.documentation.swagger.web.SwaggerResource;
import springfox.documentation.swagger.web.SwaggerResourcesProvider;

@Configuration
public class SwaggerConfig {

    @Value("${documentation.swagger.v2.defaultpath:'/api-docs'}")
    private String defaulthApiDocsPath;

    @Primary
    @Bean
    public SwaggerResourcesProvider swaggerResourcesProvider(DiscoveryClient discoveryClient) {

        return () -> {
            return discoveryClient.getServices().stream()
                    .map(serviceId -> discoveryClient.getInstances(serviceId).stream().findAny()
                            .map(pod -> createResource(serviceId, pod)).orElse(null))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            //            List<SwaggerResource> resources = new ArrayList<>();
            //            SwaggerResource swaggerResource;
            //
            //            swaggerResource = new SwaggerResource();
            //            swaggerResource.setName("test bodyshop");
            //            swaggerResource.setLocation("http://localhost:10081/api-docs");
            //            swaggerResource.setSwaggerVersion("2.0");
            //            resources.add(swaggerResource);
            //
            //            swaggerResource = new SwaggerResource();
            //            swaggerResource.setName("test paintshop");
            //            swaggerResource.setLocation("http://localhost:10082/api-docs");
            //            swaggerResource.setSwaggerVersion("2.0");
            //            resources.add(swaggerResource);
            //
            //            return resources;
        };
    }

    private SwaggerResource createResource(String name, ServiceInstance instance) {
        SwaggerResource swaggerResource = new SwaggerResource();
        swaggerResource.setName(name);
        swaggerResource.setLocation(instance.getUri() + defaulthApiDocsPath);
        swaggerResource.setSwaggerVersion("2.0");
        return swaggerResource;
    }

}
