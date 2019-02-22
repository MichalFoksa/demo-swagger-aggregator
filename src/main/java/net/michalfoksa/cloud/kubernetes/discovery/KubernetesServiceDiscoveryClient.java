/*
 * Copyright 2013-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package net.michalfoksa.cloud.kubernetes.discovery;

import static java.util.stream.Collectors.toMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.kubernetes.discovery.KubernetesClientServicesFunction;
import org.springframework.cloud.kubernetes.discovery.KubernetesDiscoveryClient;
import org.springframework.cloud.kubernetes.discovery.KubernetesDiscoveryProperties;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.SimpleEvaluationContext;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServicePort;
import io.fabric8.kubernetes.client.KubernetesClient;

/***
 * Discovery client providing methods to retrieve Kubernetes services as if they
 * were service instances in context of Spring cloud discovery.
 *
 * I.e.: services might be returned instead of service instances.
 *
 * @author Michal Foksa
 *
 */
public class KubernetesServiceDiscoveryClient extends KubernetesDiscoveryClient {

    private static final Log log = LogFactory.getLog(KubernetesServiceDiscoveryClient.class);

    private final KubernetesDiscoveryProperties properties;
    private final KubernetesClientServicesFunction kubernetesClientServicesFunction;
    private final SpelExpressionParser parser = new SpelExpressionParser();
    private final SimpleEvaluationContext evalCtxt = SimpleEvaluationContext.forReadOnlyDataBinding()
            .withInstanceMethods().build();

    KubernetesServiceDiscoveryClient(KubernetesClient client, KubernetesDiscoveryProperties kubernetesDiscoveryProperties,
            KubernetesClientServicesFunction kubernetesClientServicesFunction) {

        super(client, kubernetesDiscoveryProperties, kubernetesClientServicesFunction);

        this.properties = kubernetesDiscoveryProperties;
        this.kubernetesClientServicesFunction = kubernetesClientServicesFunction;
    }

    @Override
    public String description() {
        return "Kubernetes Discovery Client V2";
    }

    @Override
    public List<ServiceInstance> getInstances(String serviceId) {
        Assert.notNull(serviceId, "[Assertion failed] - the object argument must not be null");

        Predicate<Service> filter = (Service instance) -> {
            if (instance != null && instance.getMetadata() != null && instance.getMetadata().getName() != null
                    && instance.getMetadata().getName().equals(serviceId)) {
                return true;
            }
            return false;
        };

        return new ArrayList<>(getKubernetesServices(filter));
    }

    public List<KubernetesService> getKubernetesServices() {
        String spelExpression = properties.getFilter();
        Predicate<Service> filteredServices = (Service instance) -> true;
        if (spelExpression == null || spelExpression.isEmpty()) {
            filteredServices = (Service instance) -> true;
        } else {
            Expression filterExpr = parser.parseExpression(spelExpression);
            filteredServices = (Service instance) -> {
                Boolean include = filterExpr.getValue(evalCtxt, instance, Boolean.class);
                if (include == null) {
                    return false;
                }
                return include;
            };
        }
        return getKubernetesServices(filteredServices);
    }

    public List<KubernetesService> getKubernetesServices(Predicate<Service> filter) {
        return kubernetesClientServicesFunction.apply(getClient()).list().getItems().stream()
                .filter(filter).map(s -> {

                    final Map<String, String> serviceMetadata = new HashMap<>();
                    KubernetesDiscoveryProperties.Metadata metadataProps = properties.getMetadata();

                    if (metadataProps.isAddLabels()) {
                        Map<String, String> labelMetadata = prefixMapKeys(s.getMetadata().getLabels(),
                                metadataProps.getLabelsPrefix());
                        if (log.isDebugEnabled()) {
                            log.debug("Adding label metadata: " + labelMetadata);
                        }
                        serviceMetadata.putAll(labelMetadata);
                    }

                    if (metadataProps.isAddAnnotations()) {
                        Map<String, String> annotationMetadata = prefixMapKeys(
                                s.getMetadata().getAnnotations(), metadataProps.getAnnotationsPrefix());
                        if (log.isDebugEnabled()) {
                            log.debug("Adding annotation metadata: " + annotationMetadata);
                        }
                        serviceMetadata.putAll(annotationMetadata);
                    }

                    // Extend the service metadata map with per port information
                    // (if requested)
                    if (metadataProps.isAddPorts()) {
                        Map<String, String> ports = s.getSpec().getPorts().stream()
                                .filter(port -> !StringUtils.isEmpty(port.getName()))
                                .collect(toMap(ServicePort::getName, port -> Integer.toString(port.getPort())));
                        Map<String, String> portsMetadata = prefixMapKeys(ports, metadataProps.getPortsPrefix());
                        if (log.isDebugEnabled()) {
                            log.debug("Adding port metadata: " + portsMetadata);
                        }
                        serviceMetadata.putAll(portsMetadata);
                    }

                    ServicePort port = s.getSpec().getPorts().stream().findFirst().orElseThrow(IllegalStateException::new);
                    return new KubernetesService(s.getMetadata().getName(), s.getMetadata().getNamespace(),
                            s.getSpec().getType(), s.getSpec().getClusterIP(), s.getSpec().getExternalName(), port,
                            serviceMetadata, false);
                }).collect(Collectors.toList());
    }

    /***
     * Returns a new map that contain all the entries of the original map but
     * all keys are prefixed by the <code>prefix</code>
     *
     * If the prefix is null or empty, the map itself is returned (unchanged of
     * course)
     *
     * @param map
     * @param prefix
     * @return
     */
    private Map<String, String> prefixMapKeys(Map<String, String> map, String prefix) {
        if (map == null) {
            return new HashMap<>();
        }

        // when the prefix is empty just return an map with the same entries
        if (!StringUtils.hasText(prefix)) {
            return map;
        }

        final Map<String, String> result = new HashMap<>();
        map.forEach((k, v) -> result.put(prefix + k, v));

        return result;
    }

}
