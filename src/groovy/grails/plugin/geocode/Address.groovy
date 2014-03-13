package grails.plugin.geocode

import groovy.transform.Immutable

@Immutable
class Address {
    List<AddressComponent> addressComponents
    String formattedAddress
    List<String> types
    Geometry geometry
}
