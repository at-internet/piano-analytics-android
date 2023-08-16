package io.piano.android.analytics

/**
 * Interface for providing URL parts like [collectDomain], [site] adn [path]
 * SDK doesn't cache results from these methods, which allows you to dynamically change values
 */
interface ReportUrlProvider {
    /**
     * fully qualified domain name (FQDN) collect
     */
    val collectDomain: String

    /**
     * site identifier
     */
    val site: Int

    /**
     * a resource name string prefixed by '/'
     */
    val path: String
        get() = Configuration.DEFAULT_PATH
}

/**
 * Implementation of [ReportUrlProvider], that return static values
 */
class StaticReportUrlProvider(
    override val collectDomain: String,
    override val site: Int,
    override val path: String = Configuration.DEFAULT_PATH,
) : ReportUrlProvider
