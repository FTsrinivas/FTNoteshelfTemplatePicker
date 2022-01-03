package com.fluidtouch.noteshelf.commons.ui;

import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import com.fluidtouch.noteshelf.commons.FTLog;
import com.fluidtouch.noteshelf.services.FTFirebaseAnalytics;
import com.fluidtouch.noteshelf.services.FTZendeskSupportManager;
import com.fluidtouch.noteshelf2.R;

public class FTSupportActivity extends FTBaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_support);
        WebView webview = (WebView) findViewById(R.id.webView);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        setUpToolbarTheme();
        WebSettings settings = webview.getSettings();
        settings.setJavaScriptEnabled(true);
        webview.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                ((ProgressBar) findViewById(R.id.progress_bar)).setVisibility(View.GONE);
            }
        });
        webview.loadUrl("http://support.noteshelf.net/article-categories/noteshelf-on-android/");
        ((com.google.android.material.button.MaterialButton) findViewById(R.id.btn_support)).setOnClickListener((v) -> {
            FTLog.crashlyticsLog("UI: Clicked   add support ticket¬");
            FTFirebaseAnalytics.logEvent("support", "support", "support_add");
            FTZendeskSupportManager.showContactActivity(FTSupportActivity.this);
        });
    }
}
