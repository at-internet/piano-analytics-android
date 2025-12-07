package io.piano.android.analytics

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.res.Resources
import android.util.DisplayMetrics
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock

class DeviceInfoProviderTest {
    private val packageInfo = PackageInfo()
    private val displayMetrics = mock<DisplayMetrics>()
    private val resources = mock<Resources> {
        on { displayMetrics } doReturn displayMetrics
    }
    private val packageManager = mock<PackageManager> {
        on { getPackageInfo(DUMMY, 0) } doReturn packageInfo
        on { getPackageInfo(eq(DUMMY), any<PackageManager.PackageInfoFlags>()) } doReturn packageInfo
    }
    private val context = mock<Context> {
        on { resources } doReturn resources
        on { packageManager } doReturn packageManager
        on { packageName } doReturn DUMMY
    }
    private val deviceInfoProvider = DeviceInfoProvider(context)

    @Test
    fun getDisplayMetrics() {
        assertEquals(displayMetrics, deviceInfoProvider.displayMetrics)
    }

    @Test
    fun getPackageInfo() {
        assertEquals(packageInfo, deviceInfoProvider.packageInfo)
    }

    @Test
    fun getConnectionType() {
    }

    companion object {
        private const val DUMMY = "dummy"
    }
}
