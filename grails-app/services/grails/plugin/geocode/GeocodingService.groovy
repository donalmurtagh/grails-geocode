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
     * @param address
     * @return One location (lat/lng) if geocoding was successful, otherwise null
     * @throws GeocodingException
     */
    Point getPoint(String address) throws GeocodingException {

        log.debug "Attempting to geocode address: $address"
        def latLngs = getPoints(address)

        if (latLngs) {
            log.debug "Geocoded address '$address' to ${latLngs[0]}"
            return latLngs[0]
        }
        log.warn "Failed to geocode address $address"
    }

    /**
     * Geocode an address
     * @param address
     * @return list of locations if geocoding was successful otherwise an empty list
     * @throws GeocodingException
     */
    List<Point> getPoints(String address) throws GeocodingException {
        def points = submitGeocodeRequest([sensor: 'false', address: address])

        points.collect { point ->
            point.geometry.location.latitude
            new Point(latitude: point.geometry.location.lat, longitude: point.geometry.location.lng)
        }
    }

    private submitGeocodeRequest(Map queryParams) throws GeocodingException {

        http.get(path: '/maps/api/geocode/json', query: queryParams) { resp, json ->

            if (resp.status != HttpStatus.SC_OK) {
                throw new GeocodingException("HTTP response error code: ${resp.status}")
            }

            if (json.status in SUCCESS_STATUS_CODES) {
                return json.results

            } else {
                throw new GeocodingException("Error: ${json.error_message}, Status: ${json.status}")
            }
        }
    }
}

@InheritConstructors
class GeocodingException extends RuntimeException {
}