package net.michalfoksa.demo.swagger.aggregator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
@EnableDiscoveryClient
@EnableSwagger2
@ComponentScan({ "net.michalfoksa.demo.swagger.aggregator", "net.michalfoksa.cloud.kubernetes.discovery" })
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}

