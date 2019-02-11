package net.michalfoksa.demo.swagger.aggregator.context;

import java.util.Map;

public interface RuntimeContext {

    String getApplication();

    String getIp();

    String getHostname();

    /***
     * Return map with each field name and its value relevant for the context.
     *
     * @return
     */
    Map<String, Object> getAllFieldsMap();
}