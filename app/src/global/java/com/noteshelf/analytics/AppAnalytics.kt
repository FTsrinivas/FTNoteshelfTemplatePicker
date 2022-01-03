package com.noteshelf.analytics

import android.content.Context
import android.os.Bundle
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics

class AppAnalytics(val context: Context) {
    private var mFirebaseAnalytics: FirebaseAnalytics? = null
    private var mFirebaseCrashlytics: FirebaseCrashlytics? = null

    fun initAnalytics() {
        FirebaseApp.initializeApp(context)
        //init Analytics
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(context)
        mFirebaseAnalytics?.setAnalyticsCollectionEnabled(true)
        //init Crashlytics
        mFirebaseCrashlytics = FirebaseCrashlytics.getInstance()
        mFirebaseCrashlytics?.setCrashlyticsCollectionEnabled(true)
    }

    fun logEvent(event: String, bundle: Bundle) {
        mFirebaseAnalytics?.logEvent(event, bundle);
    }

    fun logCrashEvent(message: String) {
        mFirebaseCrashlytics?.log(message);
    }

    fun logCrashCustomKey(key: String, value: String) {
        mFirebaseCrashlytics?.setCustomKey(key, value);
    }

    fun setCrashUserIdentifier(userId: String) {
        mFirebaseCrashlytics?.setUserId(userId);
    }

    fun logCrashException(exception: Exception) {
        mFirebaseCrashlytics?.recordException(exception);
    }
}