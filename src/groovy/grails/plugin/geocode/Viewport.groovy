package grails.plugin.geocode

import groovy.transform.Immutable

@Immutable
class Viewport {
    Point southWest
    Point northEast
}
