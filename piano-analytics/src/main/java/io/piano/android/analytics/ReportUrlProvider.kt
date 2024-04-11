package io.piano.android.analytics

/**
 * Interface for providing URL parts like [collectDomain], [site] adn [path]
 * SDK doesn't cache results from these methods, which allows you to dynamically change values
 */
public interface ReportUrlProvider {
    /**
     * fully qualified domain name (FQDN) collect
     */
    public val collectDomain: String

    /**
     * site identifier
     */
    public val site: Int

    /**
     * a resource name string prefixed by '/'
     */
    public val path: String
        get() = Configuration.DEFAULT_PATH
}

/**
 * Implementation of [ReportUrlProvider], that return static values
 */
public class StaticReportUrlProvider(
    override val collectDomain: String,
    override val site: Int,
    override val path: String = Configuration.DEFAULT_PATH,
) : ReportUrlProvider
