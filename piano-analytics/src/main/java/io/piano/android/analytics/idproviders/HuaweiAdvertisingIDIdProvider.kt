package io.piano.android.analytics.idproviders

import android.content.Context
import com.huawei.hms.ads.identifier.AdvertisingIdClient
import timber.log.Timber

internal class HuaweiAdvertisingIDIdProvider(
    private val context: Context
) : IdProvider {
    private val info: AdvertisingIdInfo? by lazy {
        runCatching {
            with(AdvertisingIdClient.getAdvertisingIdInfo(context)) {
                AdvertisingIdInfo(id, isLimitAdTrackingEnabled)
            }
        }.onFailure {
            Timber.w(it)
        }.getOrNull()
    }
    override val visitorId: String?
        get() = info?.id
    override val isLimitAdTrackingEnabled: Boolean
        get() = info?.isLimitAdTrackingEnabled ?: true
}
