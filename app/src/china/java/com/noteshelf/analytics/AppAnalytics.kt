package com.noteshelf.analytics

import android.content.Context;
import android.os.Bundle;
import com.huawei.agconnect.crash.AGConnectCrash
import com.huawei.hms.analytics.HiAnalyticsInstance

class AppAnalytics(val context: Context) {
    var mHiAnalyticsInstance: HiAnalyticsInstance? = null
    var mCrashAnalyticsInstance: AGConnectCrash? = null

    fun initAnalytics() {
        com.huawei.hms.analytics.HiAnalyticsTools.enableLog()
        mCrashAnalyticsInstance = AGConnectCrash.getInstance()
        mCrashAnalyticsInstance?.enableCrashCollection(true)
        mHiAnalyticsInstance = com.huawei.hms.analytics.HiAnalytics.getInstance(context)
    }

    fun logEvent(event: String, bundle: Bundle) {
        mHiAnalyticsInstance?.onEvent(event, bundle);
    }

    fun logCrashEvent(message: String) {
        mCrashAnalyticsInstance?.log(message);
    }

    fun logCrashCustomKey(key: String, value: String) {
        mCrashAnalyticsInstance?.setCustomKey(key, value);
    }

    fun setCrashUserIdentifier(userId: String) {
        mCrashAnalyticsInstance?.setUserId(userId);
    }

    fun logCrashException(exception: Exception) {
//        mCrashAnalyticsInstance?.recordException(exception);
    }
}