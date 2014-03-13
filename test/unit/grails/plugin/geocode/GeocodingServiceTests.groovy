package grails.plugin.geocode

import grails.test.mixin.TestFor


/**
 * These tests send requests to the Google maps web service, so they're really integration tests. However they don't
 * need any Spring beans, a database, etc. so I'm going to leave them under test/unit because they'll run more quickly
 * than if they're under test/integration.
 */
@TestFor(GeocodingService)
class GeocodingServiceTests {

    private validAddress = 'Bray, Co. Wicklow, Ireland'
    private invalidAddress = 'not a valid address'
    private addressMatchingMultipleLocations = 'Dublin'

    void testGeocodeValidAddress() {
        Point location = service.getPoint(validAddress)
        compareApproximately '53.2', location.latitude
        compareApproximately '-6.1', location.longitude

        List<Point> locations = service.getPoints(validAddress)
        assertEquals 1, locations.size()
        assertEquals location, locations[0]
    }

    void testGeocodeWithMultipleResults() {
        List<Point> locations = service.getPoints(addressMatchingMultipleLocations)
        assertTrue locations.size() > 1

        // limit the number of results returned
        def limit = 1
        locations = service.getPoints(addressMatchingMultipleLocations, [max: limit])
        assertEquals limit, locations.size()
    }

    void testGeocodeInvalidAddress() {
        assertNull service.getPoint(invalidAddress)
        assertEquals Collections.emptyList(), service.getPoints(invalidAddress)
    }

    private void compareApproximately(String expectedValue, Float floatValue, Integer decimalPlaces = 1) {
        assertTrue new BigDecimal(expectedValue) == floatValue.round(decimalPlaces).toBigDecimal()
    }
}
