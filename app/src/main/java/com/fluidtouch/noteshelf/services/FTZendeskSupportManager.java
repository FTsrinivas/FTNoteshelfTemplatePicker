package com.fluidtouch.noteshelf.services;

import android.content.Context;
import android.os.Build;

import com.evernote.client.android.EvernoteSession;
import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf.preferences.SystemPref;
import com.fluidtouch.noteshelf2.BuildConfig;
import com.fluidtouch.noteshelf2.R;
import com.zendesk.logger.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import zendesk.configurations.Configuration;
import zendesk.core.AnonymousIdentity;
import zendesk.core.Identity;
import zendesk.core.Zendesk;
import zendesk.support.CustomField;
import zendesk.support.Support;
import zendesk.support.guide.HelpCenterActivity;
import zendesk.support.guide.HelpCenterConfiguration;
import zendesk.support.request.RequestActivity;
import zendesk.support.request.RequestConfiguration;

public class FTZendeskSupportManager {
    private static final long ANDROID = 360003610393L;

    private FTZendeskSupportManager() {
        throw new IllegalStateException("Utility class");
    }

    private static void init(Context context) {
        Zendesk.INSTANCE.init(context, "https://noteshelf.zendesk.com",
                "84d492c26ddd633ef1780ee3d8eade780e83d2dd67b78a25",
                "mobile_sdk_client_bc208087d06c635bb37a");
        Identity identity = new AnonymousIdentity.Builder().withEmailIdentifier(null).build();
        Zendesk.INSTANCE.setIdentity(identity);
        Support.INSTANCE.init(Zendesk.INSTANCE);
        Logger.setLoggable(true);
    }

    public static void showContactActivity(Context context) {
        if (Zendesk.INSTANCE.getIdentity() == null) {
            init(context);
        }

        getRequestBuilder(context).show(context);
    }

    private static RequestConfiguration.Builder getRequestBuilder(Context context) {
        Map<String, String> additionalInfo = new LinkedHashMap<>();
        additionalInfo.put("User Id", FTApp.getPref().get("userId", ""));
        additionalInfo.put("App Version", context.getString(R.string.current_version, BuildConfig.VERSION_CODE, BuildConfig.VERSION_NAME));
        additionalInfo.put("OS Version", Build.VERSION.RELEASE);
        additionalInfo.put("Device", Build.MODEL);
        additionalInfo.put("Stylus Enabled", FTApp.getPref().isStylusEnabled() ? "True" : "False");
        additionalInfo.put("Autobackup", SystemPref.BackUpType.getBackup(FTApp.getPref().getBackUpType()));
        additionalInfo.put("Publish", EvernoteSession.getInstance().isLoggedIn() ? "YES" : "NO");
        additionalInfo.put("Lang", Locale.getDefault().getLanguage());
        additionalInfo.put("Locale", Locale.getDefault().toLanguageTag());
        additionalInfo.put("Store", BuildConfig.FLAVOR);
        additionalInfo.put("HWR", FTApp.getPref().get(SystemPref.CURRENT_HW_REG, "Samsung"));

        StringBuilder result = new StringBuilder();
        Iterator iterator = additionalInfo.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry pair = (Map.Entry) iterator.next();
            result.append(pair.getKey());
            result.append(": ");
            result.append(pair.getValue());
            if (!iterator.hasNext())
                break;
            result.append(" | ");
        }

        return RequestActivity.builder().withRequestSubject("Android App Ticket")
                .withCustomFields(Arrays.asList(new CustomField(360015598614L, result.toString())))
                .withTags("ns_android");
    }

    public static void showHelpCenter(Context context) {
        if (Zendesk.INSTANCE.getIdentity() == null) {
            init(context);
        }

        List<Configuration> uiConfigs = new ArrayList<>();

        HelpCenterConfiguration.Builder builder = new HelpCenterConfiguration.Builder();
        builder.withArticlesForCategoryIds(Collections.singletonList(ANDROID));

        uiConfigs.add(builder.config());
        uiConfigs.add(getRequestBuilder(context).config());

        HelpCenterActivity.builder().show(context, uiConfigs);
    }
}