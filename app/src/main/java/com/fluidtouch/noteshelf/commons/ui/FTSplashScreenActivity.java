package com.fluidtouch.noteshelf.commons.ui;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.dd.plist.NSArray;
import com.dd.plist.NSDictionary;
import com.dd.plist.NSNumber;
import com.dd.plist.PropertyListParser;
import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf.commons.utils.AssetsUtil;
import com.fluidtouch.noteshelf.documentframework.Utilities.FTConstants;
import com.fluidtouch.noteshelf.preferences.SystemPref;
import com.fluidtouch.noteshelf.shelf.activities.FTBaseShelfActivity;
import com.fluidtouch.noteshelf.welcome.FTWelcomeScreenActivity;
import com.fluidtouch.noteshelf2.BuildConfig;
import com.fluidtouch.noteshelf2.R;
import com.fluidtouch.renderingengine.renderer.OpenGLES3Renderer;

import java.io.File;
import java.io.FileOutputStream;

public class FTSplashScreenActivity extends AppCompatActivity {
    private final int SPLASH_TIME = 1000;
    private RelativeLayout layFtDrawingView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        Just for reference to restore the book
//        if (false) {
//            new BooksRestoringUtil().copyPlistData(this);
//            return;
//        }

        if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
            openShelfActivity();
            finish();
            return;
        }

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);
//        if (true) {
//            new SignatureHashUtil().generateSignatureHash();
//            return;
//        }

        layFtDrawingView = findViewById(R.id.splash_parent_layout);

//        setUpFBRRenderingFlag();
        setUpFBRRenderingFlagFromPlist();

        //To avoid lag while opening pdf
        long initialTIme = System.currentTimeMillis();
        new Thread(() -> {
            if (!FTApp.getPref().get(SystemPref.DID_PREVIOUSLY_SEARCHED, false)) {
                FTApp.getPref().save(SystemPref.IS_SHW_ENABLED, true);
                FTApp.getEngine(this);
            } else {
                FTApp.getEngine();
            }
            runOnUiThread(() -> {
                if (System.currentTimeMillis() - initialTIme >= SPLASH_TIME) {
                    layFtDrawingView.removeViews(1, layFtDrawingView.getChildCount() - 1);
                    openShelfActivity();
                } else {
                    new Handler().postDelayed(() -> {
                        layFtDrawingView.removeViews(1, layFtDrawingView.getChildCount() - 1);
                        openShelfActivity();
                    }, SPLASH_TIME);
                }
            });
        }).start();
    }

    private void setUpFBRRenderingFlag() {
        //Enable this to turn FBR rendering on
        boolean useFBRRendering = true;
        if (useFBRRendering) {
            boolean isTestingDone = FTApp.getPref().get(SystemPref.IS_FBR_SUPPORT_TESTING_DONE, false);
            if (isTestingDone) {
                OpenGLES3Renderer.useFBRRendering = FTApp.getPref().get(SystemPref.SUPPORTS_FBR, true);
            } else {
                FTDummySurfaceView drawingSurfaceView = new FTDummySurfaceView(this);
                drawingSurfaceView.setLayoutParams(new RelativeLayout.LayoutParams(1, 1));
                layFtDrawingView.addView(drawingSurfaceView, 0);
            }
        } else {
            OpenGLES3Renderer.useFBRRendering = useFBRRendering;
        }
    }

    private void setUpFBRRenderingFlagFromPlist() {
        boolean useFBRRendering = false;
        NSArray fbrArray = null;
        try {
            fbrArray = (NSArray) PropertyListParser.parse(AssetsUtil.getInputStream("fbr_supported.plist"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        String model = Build.MODEL;

        if (fbrArray != null) {
            for (int i = 0; i < fbrArray.count(); i++) {
                if (model.equals(fbrArray.objectAtIndex(i).toString())) {
                    useFBRRendering = true;
                    break;
                }
            }
        }
        OpenGLES3Renderer.useFBRRendering = useFBRRendering;
    }

    private void openShelfActivity() {
        if (BuildConfig.FLAVOR.contains("samsung")) {
            FTApp.getPref().save(SystemPref.IS_FOR_SAMSUNG, true);
        }
        if (!FTApp.getPref().isDefaultNotebookCreated()) {
            Intent intent = new Intent(FTSplashScreenActivity.this, FTWelcomeScreenActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.setAction("restoreBackup");
            startActivity(intent);
            finish();
        } else {
            Intent intent = new Intent(FTSplashScreenActivity.this, FTBaseShelfActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            if (getIntent().getData() != null)
                intent.setData(getIntent().getData());
            else if (getIntent().getClipData() != null)
                intent.setClipData(getIntent().getClipData());
            startActivity(intent);
            finish();
        }
    }
}