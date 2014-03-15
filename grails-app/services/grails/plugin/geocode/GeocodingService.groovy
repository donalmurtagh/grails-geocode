package grails.plugin.geocode

import groovy.transform.InheritConstructors
import groovyx.net.http.HTTPBuilder
import org.apache.http.HttpStatus
import org.codehaus.groovy.grails.commons.GrailsApplication

import javax.annotation.PostConstruct

/**
 * Uses the <a href="https://developers.google.com/maps/documentation/geocoding">Google Geocoding API</a>
 * to perform geocoding and reverse geocoding
 */
class GeocodingService {

    static transactional = false

    GrailsApplication grailsApplication

    private HTTPBuilder httpBuilder = new HTTPBuilder('https://maps.googleapis.com')

    @PostConstruct
    private initializeBuilder() {

        if (grailsApplication.config.geocode?.useHttp) {
            httpBuilder = new HTTPBuilder('http://maps.googleapis.com')
        }
    }

    /**
     * Response status codes are <a href="https://developers.google.com/maps/documentation/geocoding/#StatusCodes">documented here</a>
     */
    private static final SUCCESS_STATUS_CODES = ['OK', 'ZERO_RESULTS']

    /**
     * Reverse geocode a location
     *
     * @param point the location to be reverse geocoded
     * @param optionalArgs parameters that are defined as optional in the <a href="https://developers.google.com/maps/documentation/geocoding/#ReverseGeocoding">API docs</a>.
     * Additionally, two further arguments may be provided in this map
     * <ul>
     *     <li>sensor - a boolean that indicates whether or not the geocoding request comes from a device with a location sensor. If omitted, false is assumed</li>
     *     <li>max - an integer that limits the number of results returned
     * </ul>
     * If none of these arguments are required, this parameter may be omitted
     *
     * @return a list of addresses
     * @throws GeocodingException
     */
    List<Address> getAddresses(Point point, Map optionalArgs = [:]) throws GeocodingException {

        // clone the args to prevent side effects
        Map queryParams = optionalArgs.clone()
        queryParams.latlng = "$point.latitude,$point.longitude"

        // if the sensor param is not specified it defaults to false
        if (queryParams.sensor == null) {
            queryParams.sensor = false
        }

        def results = submitGeocodeRequest(queryParams)

        // convert the nested maps to a list of Address
        results.collect { result ->
            def geometry = result.geometry
            def viewport = geometry.viewport
            new Address(
                    formattedAddress: result.formatted_address,
                    types: result.types,
                    addressComponents: result.address_components.collect { addressComponent ->
                        new AddressComponent(
                                longName: addressComponent.long_name,
                                shortName: addressComponent.short_name,
                                types: addressComponent.types
                        )
                    },
                    geometry: new Geometry(
                            viewport: new Viewport(
                                    southWest: new Point(
                                            latitude: viewport.southwest.lat,
                                            longitude: viewport.southwest.lng
                                    ),
                                    northEast: new Point(
                                            latitude: viewport.northeast.lat,
                                            longitude: viewport.northeast.lng
                                    )
                            ),
                            locationType: geometry.location_type,
                            location: new Point(
                                    latitude: geometry.location.lat,
                                    longitude: geometry.location.lng)
                    )
            )
        }
    }

    /**
     * Reverse geocode a location
     *
     * @param point the location to be reverse geocoded
     * @param optionalArgs parameters that are defined as optional in the <a href="https://developers.google.com/maps/documentation/geocoding/#ReverseGeocoding">API docs</a>.
     * Additionally, one further argument may be provided in this map
     * <ul>
     *     <li>sensor - a boolean that indicates whether or not the geocoding request comes from a device with a location sensor. If omitted, false is assumed</li>
     * </ul>
     * If none of these arguments are required, this parameter may be omitted
     *
     * @return a list of addresses
     * @throws GeocodingException
     */
    Address getAddress(Point point, Map optionalArgs = [:]) throws GeocodingException {

        optionalArgs.max = 1
        List<Address> addresses = getAddresses(point, optionalArgs)
        addresses ? addresses[0] : null
    }

    /**
     * Geocode an address
     *
     * @param address the address to be geocoded
     * @param optionalArgs parameters that are defined as optional in the <a href="https://developers.google.com/maps/documentation/geocoding/#geocoding">API docs</a>.
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

        def jsonPoints = submitGeocodeRequest(queryParams)

        jsonPoints.collect { point ->
            point.geometry.location.latitude
            new Point(latitude: point.geometry.location.lat, longitude: point.geometry.location.lng)
        }
    }

    /**
     * Geocode an address
     *
     * @param address the address to be geocoded
     * @param optionalArgs parameters that are defined as optional in the <a href="https://developers.google.com/maps/documentation/geocoding/#geocoding">API docs</a>.
     * Additionally, one further argument may be provided in this map
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

        Integer maxResults = queryParams.remove('max')

        httpBuilder.get(path: '/maps/api/geocode/json', query: queryParams) { resp, json ->

            if (resp.status != HttpStatus.SC_OK) {
                throw new GeocodingException("HTTP response error code: ${resp.status}")
            }

            if (json.status in SUCCESS_STATUS_CODES) {
                List results = json.results
                maxResults && results.size() > maxResults ? results[0..<maxResults] : results

            } else {
                throw new GeocodingException("Error: ${json.error_message} Status: ${json.status}")
            }
        }
    }
}

@InheritConstructors
class GeocodingException extends RuntimeException {
}
