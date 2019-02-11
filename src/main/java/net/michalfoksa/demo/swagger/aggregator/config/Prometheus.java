package net.michalfoksa.demo.swagger.aggregator.config;

import javax.inject.Inject;

import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.micrometer.core.instrument.MeterRegistry;
import net.michalfoksa.demo.swagger.aggregator.context.RuntimeContext;

@Configuration
public class Prometheus {

    @Inject
    private RuntimeContext runtimeContext;

    @Bean
    MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
        return registry -> {
            registry.config().commonTags("application", runtimeContext.getApplication()).commonTags("hostname",
                    runtimeContext.getHostname()).commonTags("ip", runtimeContext.getIp());

            if (runtimeContext.getAllFieldsMap().get("nodeName") != null) {
                registry.config().commonTags("nodeName", runtimeContext.getAllFieldsMap().get("nodeName").toString());
            }

            if (runtimeContext.getAllFieldsMap().get("namespace") != null) {
                registry.config().commonTags("namespace", runtimeContext.getAllFieldsMap().get("namespace").toString());
            }

        };
    }

}
