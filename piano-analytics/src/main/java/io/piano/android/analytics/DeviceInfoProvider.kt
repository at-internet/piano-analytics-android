package io.piano.android.analytics

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.os.Build
import android.telephony.TelephonyManager
import android.util.DisplayMetrics
import androidx.core.content.ContextCompat
import io.piano.android.analytics.model.ConnectionType
import timber.log.Timber

internal class DeviceInfoProvider(
    private val context: Context,
) {
    val displayMetrics: DisplayMetrics by lazy { context.resources.displayMetrics }

    val packageInfo: PackageInfo? by lazy {
        try {
            with(context) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
                } else {
                    @Suppress("DEPRECATION")
                    packageManager.getPackageInfo(packageName, 0)
                }
            }
        } catch (e: PackageManager.NameNotFoundException) {
            Timber.w(e, "Problems during package info search")
            null
        }
    }

    internal val connectionType: ConnectionType
        get() {
            val manager = ContextCompat.getSystemService(context, ConnectivityManager::class.java)
                ?: return ConnectionType.OFFLINE

            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                manager.getNetworkCapabilities(manager.activeNetwork)?.toConnectionType()
                    ?: ConnectionType.OFFLINE
            } else {
                @Suppress("DEPRECATION")
                manager.activeNetworkInfo.toConnectionType()
            }
        }

    private fun NetworkCapabilities.toConnectionType(): ConnectionType = when {
        !hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) -> ConnectionType.OFFLINE

        hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
            hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> ConnectionType.WIFI

        hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> ConnectionType.MOBILE

        else -> ConnectionType.UNKNOWN
    }

    @Suppress("DEPRECATION")
    @SuppressLint("MissingPermission") // use only for old Android
    private fun NetworkInfo?.toConnectionType(): ConnectionType = when {
        this == null || !isConnected -> ConnectionType.OFFLINE
        type == ConnectivityManager.TYPE_WIFI -> ConnectionType.WIFI
        else -> ContextCompat.getSystemService(context, TelephonyManager::class.java)?.run {
            val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) dataNetworkType else networkType
            type.toConnectionType()
        } ?: ConnectionType.UNKNOWN
    }

    private fun Int.toConnectionType(): ConnectionType = when (this) {
        TelephonyManager.NETWORK_TYPE_GPRS -> ConnectionType.GPRS
        TelephonyManager.NETWORK_TYPE_EDGE -> ConnectionType.EDGE

        TelephonyManager.NETWORK_TYPE_CDMA,
        TelephonyManager.NETWORK_TYPE_IDEN,
        TelephonyManager.NETWORK_TYPE_1xRTT,
        -> ConnectionType.TWOG

        TelephonyManager.NETWORK_TYPE_UMTS,
        TelephonyManager.NETWORK_TYPE_EVDO_0,
        TelephonyManager.NETWORK_TYPE_EVDO_A,
        TelephonyManager.NETWORK_TYPE_EVDO_B,
        -> ConnectionType.THREEG

        TelephonyManager.NETWORK_TYPE_HSPA,
        TelephonyManager.NETWORK_TYPE_HSDPA,
        TelephonyManager.NETWORK_TYPE_HSUPA,
        -> ConnectionType.THREEGPLUS

        TelephonyManager.NETWORK_TYPE_HSPAP,
        TelephonyManager.NETWORK_TYPE_LTE,
        -> ConnectionType.FOURG

        TelephonyManager.NETWORK_TYPE_NR -> ConnectionType.FIVEG

        else -> ConnectionType.UNKNOWN
    }
}
