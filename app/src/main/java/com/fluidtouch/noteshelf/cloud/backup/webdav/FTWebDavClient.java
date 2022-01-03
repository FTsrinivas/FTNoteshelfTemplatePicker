package com.fluidtouch.noteshelf.cloud.backup.webdav;

import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf.commons.FTLog;
import com.thegrizzlylabs.sardineandroid.impl.OkHttpSardine;

public class FTWebDavClient {
    public final OkHttpSardine sardine;

    private static FTWebDavClient webDavClient;

    public static FTWebDavClient getInstance() {
        if (webDavClient == null) {
            webDavClient = new FTWebDavClient();
        }
        return webDavClient;
    }

    private FTWebDavClient() {
        this.sardine = new OkHttpSardine();
        if (new FTWebDavServiceAccountHandler().checkSession(FTApp.getInstance().getApplicationContext())) {
            FTWebDavCredentials webDavCredentials = FTApp.getPref().getWebDavCredentials();
            this.sardine.setCredentials(webDavCredentials.getUsername(), webDavCredentials.getPassword());
        } else {
            FTLog.error(FTLog.WEBDAV_BACKUP, "Error while creating WebDAV client. Invalid credentials");
        }
    }
}