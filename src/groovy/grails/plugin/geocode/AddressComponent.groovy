package grails.plugin.geocode

import groovy.transform.Immutable

@Immutable
class AddressComponent {
    String longName
    String shortName
    List<String> types
}
