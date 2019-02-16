package net.michalfoksa.demo.swagger.aggregator.service;

/***
 * Abstracts mechanism to retrieve service URL by service name.
 *
 * @author Michal Foksa
 *
 */
public interface UrlResolver {

    /***
     * Create service URI from service name.
     *
     * @param serviceName
     * @return service URL
     */
    String getUrl(String serviceName);

}