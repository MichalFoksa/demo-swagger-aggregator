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

        return () -> {

            // return
            // discoveryClient.getServices().stream().sorted(String::compareTo)
            // .map(serviceId ->
            // discoveryClient.getInstances(serviceId).stream().findAny()
            // .map(pod -> resourceService.createResource(pod)).orElse(null))
            // .filter(Objects::nonNull).collect(Collectors.toList());

            return discoveryClient.getServices().stream().sorted(String::compareTo)
                    .map(serviceName -> resourceService.createResource(serviceName))
                    .filter(Objects::nonNull).collect(Collectors.toList());

            // MyInstace[] array = { new MyInstace("bodyshop",
            // "http://localhost:10081/"),
            // new MyInstace("paintshop", "http://localhost:10082/"),
            // new MyInstace("powertrain", "http://localhost:10083/")
            // };
            //
            // return Arrays.stream(array).map(pod ->
            // resourceService.createResource(pod)).collect(Collectors.toList());
        };
    }

}
