package net.michalfoksa.demo.swagger.aggregator.context;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class KubernetesRuntimeContext implements RuntimeContext {

    private String application;
    private String podIp;
    private String podName;
    private String podNamespace;
    private String podServiceAccount;
    private String nodeName;

    @Override
    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public KubernetesRuntimeContext application(String application) {
        this.application = application;
        return this;
    }

    @Override
    public String getIp() {
        return podIp;
    }

    public void setIp(String ip) {
        this.podIp = ip;
    }

    public KubernetesRuntimeContext ip(String ip) {
        this.podIp = ip;
        return this;
    }

    @Override
    public String getHostname() {
        return podName;
    }

    public void setHostname(String hostname) {
        this.podName = hostname;
    }

    public KubernetesRuntimeContext hostname(String hostname) {
        this.podName = hostname;
        return this;
    }

    public String getPodNamespace() {
        return podNamespace;
    }

    public void setPodNamespace(String podNamespace) {
        this.podNamespace = podNamespace;
    }

    public KubernetesRuntimeContext podNamespace(String podNamespace) {
        this.podNamespace = podNamespace;
        return this;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public KubernetesRuntimeContext nodeName(String nodeName) {
        this.nodeName = nodeName;
        return this;
    }

    public String getPodServiceAccount() {
        return podServiceAccount;
    }

    public void setPodServiceAccount(String podServiceAccount) {
        this.podServiceAccount = podServiceAccount;
    }

    public KubernetesRuntimeContext podServiceAccount(String podServiceAccount) {
        this.podServiceAccount = podServiceAccount;
        return this;
    }

    @Override
    public Map<String, Object> getAllFieldsMap() {
        // Collect all non null class properties into map.
        return Arrays.stream(KubernetesRuntimeContext.class.getDeclaredFields())
                .filter(this::isNotNull)
                .collect(Collectors.toMap(field -> field.getName(), field -> getFieldValue(field)));
    }

    /***
     * Return true if field value is not null
     *
     * @param field
     * @return true if field value is not null
     */
    private boolean isNotNull(Field field) {
        return getFieldValue(field) != null;
    }

    private Object getFieldValue(Field field) {
        field.setAccessible(true);
        try {
            return field.get(this);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            // Ignore exceptions
        }
        return null;
    }

    @Override
    public String toString() {
        return "KubernetesRuntimeContext [application=" + application + ", podIp=" + podIp + ", podName=" + podName
                + ", podNamespace=" + podNamespace + ", podServiceAccount=" + podServiceAccount + ", nodeName="
                + nodeName + "]";
    }

}
