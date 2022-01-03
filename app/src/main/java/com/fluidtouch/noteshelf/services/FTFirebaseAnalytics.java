package com.fluidtouch.noteshelf.services;

import android.os.Bundle;

import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf.commons.FTLog;

public class FTFirebaseAnalytics {
    public static void logEvent(String screen, String module, String event) {
        Bundle bundle = new Bundle();
        bundle.putString("screen", screen);
        bundle.putString("module", module);
        //HMSAnalytic
        FTApp.getInstance().getAppAnalytics().logEvent(event, bundle);
        FTLog.saveLog(screen + " " + module + " " + event);
    }

    public static void logEvent(String event) {
        Bundle bundle = new Bundle();
        FTApp.getInstance().getAppAnalytics().logEvent(event, bundle);
        FTLog.saveLog(event);
    }
}
