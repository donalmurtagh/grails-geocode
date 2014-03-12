package grails.plugin.geocode


import groovy.transform.Immutable

@Immutable
class Point {
    Float longitude
    Float latitude

    @Override
    String toString() {
        "lat: $latitude, long: $longitude"
    }
}
