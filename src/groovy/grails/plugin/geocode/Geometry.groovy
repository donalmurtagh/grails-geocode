package grails.plugin.geocode

import groovy.transform.Immutable

@Immutable
class Geometry {
    Viewport viewport
    String locationType
    Point location
}
