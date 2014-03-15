class GeocodeGrailsPlugin {
    // the plugin version
    def version = "0.3"
    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "2.0 > *"
    // resources that are excluded from plugin packaging
    def pluginExcludes = [
    ]

    // TODO Fill in these fields
    def title = "Geocode Plugin" // Headline display name of the plugin
    def author = "Donal Murtagh"
    def authorEmail = ""
    def description = '''\
Uses the Google maps web service to perform address geocoding and reverse geocoding
'''

    // URL to the plugin's documentation
    def documentation = "https://github.com/domurtag/grails-geocode"

    // Extra (optional) plugin metadata

    // License: one of 'APACHE', 'GPL2', 'GPL3'
    def license = "APACHE"


    // Location of the plugin's issue tracker.
    def issueManagement = [ system: "GitHub", url: "https://github.com/domurtag/grails-geocode/issues" ]

    // Online location of the plugin's browseable source code.
    def scm = [ url: "https://github.com/domurtag/grails-geocode" ]

    def doWithWebDescriptor = { xml ->
        // TODO Implement additions to web.xml (optional), this event occurs before
    }

    def doWithSpring = {
        // TODO Implement runtime spring config (optional)
    }

    def doWithDynamicMethods = { ctx ->
        // TODO Implement registering dynamic methods to classes (optional)
    }

    def doWithApplicationContext = { ctx ->
        // TODO Implement post initialization spring config (optional)
    }

    def onChange = { event ->
        // TODO Implement code that is executed when any artefact that this plugin is
        // watching is modified and reloaded. The event contains: event.source,
        // event.application, event.manager, event.ctx, and event.plugin.
    }

    def onConfigChange = { event ->
        // TODO Implement code that is executed when the project configuration changes.
        // The event is the same as for 'onChange'.
    }

    def onShutdown = { event ->
        // TODO Implement code that is executed when the application shuts down (optional)
    }
}
