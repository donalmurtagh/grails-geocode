package grails.plugin.geocode

import groovy.transform.InheritConstructors
import groovyx.net.http.HTTPBuilder
import org.apache.http.HttpStatus

/**
 * Uses the <a href="https://developers.google.com/maps/documentation/geocoding">Google Geocoding API</a>
 * to perform geocoding and reverse geocoding
 */
class GeocodingService {

    static transactional = false

    private final http = new HTTPBuilder('http://maps.googleapis.com')

    /**
     * Response status codes are <a href="https://developers.google.com/maps/documentation/geocoding/#StatusCodes">documented here</a>
     */
    private static final SUCCESS_STATUS_CODES = ['OK', 'ZERO_RESULTS']

    /**
     * Geocode an address
     *
     * @param address the address to be geocoded
     * @param optionalArgs parameters that are defined as optional in the <a href="https://developers.google.com/maps/documentation/geocoding/#geocoding"API docs</a>.
     * Additionally, two further arguments may be provided in this map
     * <ul>
     *     <li>sensor - a boolean that indicates whether or not the geocoding request comes from a device with a location sensor. If omitted, false is assumed</li>
     *     <li>max - an integer that limits the number of results returned
     * </ul>
     * If none of these arguments are required, this parameter may be omitted
     *
     * @return list of locations if geocoding was successful, otherwise an empty list
     * @throws GeocodingException
     */
    List<Point> getPoints(String address, Map optionalArgs = [:]) throws GeocodingException {

        // clone the args to prevent side effects
        Map queryParams = optionalArgs.clone()
        queryParams.address = address

        // if the sensor param is not specified it defaults to false
        if (queryParams.sensor == null) {
            queryParams.sensor = false
        }

        Integer maxResults = optionalArgs.remove('max')
        def jsonPoints = submitGeocodeRequest(queryParams)

        List<Point> points = jsonPoints.collect { point ->
            point.geometry.location.latitude
            new Point(latitude: point.geometry.location.lat, longitude: point.geometry.location.lng)
        }

        // truncate the number of results returned if necessary
        maxResults && points.size() > maxResults ? points[0..<maxResults] : points
    }


    /**
     * Geocode an address
     *
     * @param address the address to be geocoded
     * @param optionalArgs parameters that are defined as optional in the <a href="https://developers.google.com/maps/documentation/geocoding/#geocoding"API docs</a>.
     * Additionally, two further arguments may be provided in this map
     * <ul>
     *     <li>sensor - a boolean that indicates whether or not the geocoding request comes from a device with a location sensor. If omitted, false is assumed</li>
     * </ul>
     * If none of these arguments are required, this parameter may be omitted
     *
     * @return A single location (lat/lng) if geocoding was successful, otherwise null
     * @throws GeocodingException
     */
    Point getPoint(String address, Map optionalArgs = [:]) throws GeocodingException {

        optionalArgs.max = 1
        List<Point> points = getPoints(address, optionalArgs)

        points ? points[0] : null
    }


    private submitGeocodeRequest(Map queryParams) throws GeocodingException {

        http.get(path: '/maps/api/geocode/json', query: queryParams) { resp, json ->

            if (resp.status != HttpStatus.SC_OK) {
                throw new GeocodingException("HTTP response error code: ${resp.status}")
            }

            if (json.status in SUCCESS_STATUS_CODES) {
                return json.results

            } else {
                throw new GeocodingException("Error: ${json.error_message} Status: ${json.status}")
            }
        }
    }
}

@InheritConstructors
class GeocodingException extends RuntimeException {
}