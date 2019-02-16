package net.michalfoksa.demo.swagger.aggregator.service;

import java.net.URI;

/***
 * Abstracts mechanism to retrieve service URL by service name.
 *
 * @author Michal Foksa
 *
 */
public interface ServiceUriResolver {

    /***
     * Create service URI from service name.
     *
     * @param serviceName
     * @return service URL
     */
    URI getUri(String serviceName);

}