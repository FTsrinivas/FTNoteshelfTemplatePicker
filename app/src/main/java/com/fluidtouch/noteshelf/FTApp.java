package com.fluidtouch.noteshelf;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.util.DisplayMetrics;
import android.widget.TextView;

import com.dd.plist.NSDictionary;
import com.dd.plist.NSNumber;
import com.dd.plist.PropertyListFormatException;
import com.dd.plist.PropertyListParser;
import com.evernote.client.android.EvernoteSession;
import com.fluidtouch.noteshelf.backup.database.DaoMaster;
import com.fluidtouch.noteshelf.cloud.backup.FTServicePublishManager;
import com.fluidtouch.noteshelf.commons.FTLog;
import com.fluidtouch.noteshelf.commons.ui.FTDialogFactory;
import com.fluidtouch.noteshelf.commons.utils.AssetsUtil;
import com.fluidtouch.noteshelf.commons.utils.FTFileManagerUtil;
import com.fluidtouch.noteshelf.commons.utils.FTSqliteOpenHelper;
import com.fluidtouch.noteshelf.documentframework.FTCoverPaperThemeProvider;
import com.fluidtouch.noteshelf.documentframework.FTUrl;
import com.fluidtouch.noteshelf.documentframework.Utilities.FTConstants;
import com.fluidtouch.noteshelf.evernotesync.FTENPublishManager;
import com.fluidtouch.noteshelf.models.theme.FTNPaperTheme;
import com.fluidtouch.noteshelf.models.theme.FTNTheme;
import com.fluidtouch.noteshelf.models.theme.FTNThemeCategory;
import com.fluidtouch.noteshelf.preferences.SystemPref;
import com.fluidtouch.noteshelf.services.FTFirebaseAnalytics;
import com.fluidtouch.noteshelf.templatepicker.FTAppConfig;
import com.fluidtouch.noteshelf.templatepicker.common.FTTemplatesInfoSingleton;
import com.fluidtouch.noteshelf.templatepicker.common.plistdatamodel.FTSelectedDeviceInfo;
import com.fluidtouch.noteshelf.templatepicker.common.util.FTTemplateUtil;
import com.fluidtouch.noteshelf.textrecognition.handwriting.certificates.MyCertificate;
import com.fluidtouch.noteshelf.textrecognition.handwriting.certificates.MyCertificateBeta;
import com.fluidtouch.noteshelf.textrecognition.handwriting.certificates.MyCertificateDev;
import com.fluidtouch.noteshelf.textrecognition.handwriting.languageresourcehandlers.FTLanguageResourceManager;
import com.fluidtouch.noteshelf.textrecognition.handwriting.languageresourcehandlers.FTRecognitionLangResource;
import com.fluidtouch.noteshelf2.BuildConfig;
import com.fluidtouch.noteshelf2.R;
import com.fluidtouch.renderingengine.commons.FTRenderEngineLog;
import com.myscript.iink.Engine;
import com.noteshelf.analytics.AppAnalytics;
import com.noteshelf.cloud.backup.onedrive.FTOneDriveClientApp;
import com.samsung.android.sdk.pen.recogengine.SpenRecognizer;
import com.samsung.android.sdk.pen.recogengine.SpenResourceProvider;
import com.squareup.picasso.LruCache;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Request;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import javax.xml.parsers.ParserConfigurationException;

public class FTApp extends Application {
    //EVERNOTE PRODUCTION
    private static final String CONSUMER_KEY = "pssramakrishnna";
    private static final String CONSUMER_SECRET = "7c45c3fb78eb6dba";
    public static ArrayList<String> CURRENT_EDIT_DOCUMENT_UIDS = new ArrayList<>();
    private static FTApp mInstance;
    private static DaoMaster daoMaster;
    private static SystemPref mPref;
    public static FTServicePublishManager mFTServicePublishManager;
    private static Engine engine;
    private static SpenRecognizer spenRecognizer;
    private Context mActivityContext;
    private AppAnalytics mAppAnalytics;
    private FTAppConfig mFTAppConfig;

    static {
        try {
            System.loadLibrary("opencv_java3");
        } catch (Exception e) {
            e.printStackTrace();
            FTLog.logCrashException(e);
        }
    }

    //region OpenCv
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            if (status == LoaderCallbackInterface.SUCCESS) {
                FTLog.crashlyticsLog("OpenCV loaded successfully");
            } else {
                super.onManagerConnected(status);
                FTLog.logCrashException(new Exception("OpenCV Failed to Load status:" + status));
            }
        }
    };

    public static synchronized Engine getEngine() {
        if (!FTApp.getPref().get(SystemPref.DID_PREVIOUSLY_SEARCHED, false)) {
            return null;
        }
        FTApp.getPref().save(SystemPref.CURRENT_HW_REG, "MyScript");
        String deviceId = Settings.Secure.getString(FTApp.getInstance().getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        FTFirebaseAnalytics.logEvent("HWR: MyScript - " + deviceId);
        if (engine == null) {
            FTLog.crashlyticsLog("HWRecog: Creating MyScript engine for " + BuildConfig.APPLICATION_ID);
            try {
                byte[] bytes;
                if (BuildConfig.FLAVOR.equals("dev"))
                    bytes = MyCertificateDev.getBytes();
                else if (BuildConfig.FLAVOR.equals("beta"))
                    bytes = MyCertificateBeta.getBytes();
                else
                    bytes = MyCertificate.getBytes();
                engine = Engine.create(bytes);
                FTFirebaseAnalytics.logEvent("App", "HW_Recognition", "Engine_created_successfully");
            } catch (Exception e) {
                FTFirebaseAnalytics.logEvent("App", "HW_Recognition", "Failed_to_create engine");
                FTLog.logCrashException(e);
                FTLog.error(FTLog.HW_RECOGNITION, "Engine creation failed");
            }
        }
        return engine;
    }


    public static void setUpPublishManager() {
        if (mFTServicePublishManager == null && FTApp.getPref().getBackUpType() != SystemPref.BackUpType.NONE.ordinal()) {
            mFTServicePublishManager = new FTServicePublishManager();
        }
    }

    //Custom Picasso
    private Picasso getCustomPicasso() {
        //set request transformer
        Picasso.RequestTransformer requestTransformer = new Picasso.RequestTransformer() {
            @Override
            public Request transformRequest(Request request) {
                return request;
            }
        };

        //For reference
//        int MaxCacheDays = 7;
//        OkHttpClient okHttp3Client = new OkHttpClient();
//        okHttp3Client.networkInterceptors().add(provideCacheInterceptor(MaxCacheDays));
//        OkHttp3Downloader okHttp3Downloader = new OkHttp3Downloader(okHttp3Client);

        return new Picasso.Builder(this)
//                .downloader(okHttp3Downloader)
                //set 12% of available app memory for image cache
                .memoryCache(new LruCache(getBytesForMemCache(12)))
                .requestTransformer(requestTransformer)
                .build();
    }

    private int getBytesForMemCache(int percent) {
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        ActivityManager activityManager = (ActivityManager)
                getSystemService(ACTIVITY_SERVICE);
        activityManager.getMemoryInfo(mi);

        double availableMemory = mi.availMem;

        return (int) (percent * availableMemory / 100);
    }

//    public Interceptor provideCacheInterceptor(final int maxDays) {
//        return new Interceptor() {
//            @Override
//            public Response intercept(Chain chain) throws IOException {
//                Response response = chain.proceed(chain.request());
//                CacheControl cacheControl = new CacheControl.Builder()
//                        .maxAge(maxDays, TimeUnit.DAYS)
//                        .build();
//
//                return response.newBuilder()
//                        .header(Constants.CACHE_CONTROL, cacheControl.toString())
//                        .build();
//            }
//        };
//    }

    public static synchronized SpenRecognizer getEngine(Context context) {
        if (!FTApp.getPref().get(SystemPref.DID_SHW_PREVIOUSLY_SEARCHED, false)) {
            return null;
        }
        FTApp.getPref().save(SystemPref.CURRENT_HW_REG, "Samsung");
        String deviceId = Settings.Secure.getString(FTApp.getInstance().getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        FTFirebaseAnalytics.logEvent("HWR: Samsung - " + deviceId);
        if (spenRecognizer == null) {
            spenRecognizer = new SpenRecognizer(context);
            // Set display density
            DisplayMetrics dm = new DisplayMetrics();
            ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(dm);
            float xDpi = dm.xdpi; // these will return the actual dpi horizontally and vertically
            float yDpi = dm.ydpi;
            spenRecognizer.setDisplayMetrics(xDpi, yDpi);
            spenRecognizer.setRecognizerType(SpenRecognizer.RecognizerType.TEXT_MULTILINE);
            spenRecognizer.createResourceProvider(SpenResourceProvider.EngineType.TEXT, SpenResourceProvider.ResourceType.FILE);
        }
        return spenRecognizer;
    }

    public static boolean shouldLog() {
        return BuildConfig.FLAVOR.equals("dev") || BuildConfig.FLAVOR.equals("beta");
    }

    private void initOpenCV() {
        if (!OpenCVLoader.initDebug()) {
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public static synchronized FTApp getInstance() {
        return mInstance;
    }

    public static DaoMaster getDaoMaster() {
        return daoMaster;
    }

    public static SystemPref getPref() {
        return mPref;
    }

    public static FTServicePublishManager getServicePublishManager() {
        return mFTServicePublishManager;
    }

    public static String getRelativePath(String url) {
        String s = url.split(FTConstants.DOCUMENTS_ROOT_PATH + "/Noteshelf.nsdata/User Documents/")[1];
        return "Noteshelf/" + s;
    }

    public static String removeExtension(String name) {
        if (name.contains(".") && !name.contains(".nsa")) {
            return name.substring(0, name.lastIndexOf("."));
        }

        return name;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createInstances();
        FTOneDriveClientApp.createApplication(this);
        init();
        getDeviceLanguage();
        migrationStart();
        FTLog.crashlyticsLog("App started");
    }

    private void init() {
        initDaoMaster();
        initEvernoteSession();
        mAppAnalytics = new AppAnalytics(this);
        mAppAnalytics.initAnalytics();
        FTLog.crashlyticsLog("App started");
        //init OpenCv
        initOpenCV();

        /*if (FTTemplateUtil.getInstance().getFtSelectedDeviceInfo() == null) {
            FTSelectedDeviceInfo ftSelectedDeviceInfo = new FTSelectedDeviceInfo();
            String tabSelcted = FTApp.getPref().get(SystemPref.LAST_SELECTED_TAB, "portrait");
            if (tabSelcted.toLowerCase().equalsIgnoreCase("")) {
                ftSelectedDeviceInfo.setLayoutType("portrait");
            } else {
                ftSelectedDeviceInfo.setLayoutType("landscape");
            }

            ftSelectedDeviceInfo.setPageWidth(Integer.parseInt(FTApp.getPref().get(SystemPref.SELECTED_DEVICE_WIDTH, "0")));
            ftSelectedDeviceInfo.setPageHeight(Integer.parseInt(FTApp.getPref().get(SystemPref.SELECTED_DEVICE_HEIGHT, "0")));
            FTTemplateUtil.getInstance().setFtSelectedDeviceInfo(ftSelectedDeviceInfo);
            Log.d("TemplatePicker==>"," FTAppConfig Notebook getPageWidth::-"+ftSelectedDeviceInfo.getPageWidth()+
                    " getPageHeight::-"+ftSelectedDeviceInfo.getPageHeight());
        }*/

        FTTemplatesInfoSingleton.getInstance().getSupportedDevicesInfo();
        //FTTemplatesInfoSingleton.getInstance().doBackgroundWork();
        FTRenderEngineLog.getInstance().setLogCallback(new FTRenderEngineLog.FTRenderEngineLogCallback() {
            @Override
            public void crashlyticsLog(String s) {
                FTLog.crashlyticsLog(s);
            }

            @Override
            public void logCrashException(Exception e) {
                FTLog.logCrashException(e);
            }

            @Override
            public void logCrashCustomKey(String key, String value) {
                FTLog.logCrashCustomKey(key, value);
            }
        });
        setUpPublishManager();
        Picasso.setSingletonInstance(getCustomPicasso());
    }

    private void initDaoMaster() {
        DaoMaster.DevOpenHelper helper = new FTSqliteOpenHelper(this, "notes-db", null);
        SQLiteDatabase db = helper.getWritableDatabase();
        daoMaster = new DaoMaster(db);
    }

    public void initEvernoteSession() {
        new EvernoteSession.Builder(this)
                .setLocale(getResources().getConfiguration().getLocales().get(0))
                .setEvernoteService(EvernoteSession.EvernoteService.PRODUCTION)
                .setForceAuthenticationInThirdPartyApp(true)
                .build(CONSUMER_KEY, CONSUMER_SECRET)
                .asSingleton();

        if (EvernoteSession.getInstance().isLoggedIn()) {
            FTENPublishManager.getInstance().enablePublisher();
        }
    }

    private void createInstances() {
        mActivityContext = this;
        mInstance = this;
        mPref = new SystemPref().init(SystemPref.PREF_NAME);
        if (BuildConfig.FLAVOR.contains("samsung")) {
            FTApp.getPref().save(SystemPref.IS_FOR_SAMSUNG, true);
        }
        if (!FTApp.getPref().get(SystemPref.DID_PREVIOUSLY_SEARCHED, false)) {
            FTApp.getPref().save(SystemPref.IS_SHW_ENABLED, true);
        }
        if (isForHuawei()) FTApp.getPref().save(SystemPref.SELECTED_LANGUAGE, "zh_CN");
        List<FTRecognitionLangResource> availableLanguageResources;
        if (FTApp.getPref().get(SystemPref.IS_SHW_ENABLED, false)) {
            availableLanguageResources = FTLanguageResourceManager.getInstance().availableLanguageResourcesForSHW();
        } else {
            availableLanguageResources = FTLanguageResourceManager.getInstance().availableLanguageResources();
        }
        for (int i = 0; i < availableLanguageResources.size(); i++) {
            FTRecognitionLangResource langResource = availableLanguageResources.get(i);
            if (langResource.getLanguageCode().equals(FTApp.getPref().get(SystemPref.SELECTED_LANGUAGE, "en_US"))) {
                langResource.downloadResourceOnDemand();
                FTLanguageResourceManager.getInstance().setCurrentLanguageCode(langResource.getLanguageCode());
            }
        }
    }

    public void getDeviceLanguage() {
        String countryCode = getResources().getConfiguration().getLocales().get(0).getCountry();
        if (Locale.getDefault().toLanguageTag().contains("zh-Hans")) {
            countryCode = "cn";
        } else if (Locale.getDefault().toLanguageTag().contains("zh-Hant")) {
            countryCode = "cn";
        }
        getPref().save(SystemPref.COUNTRY_CODE, countryCode);
    }

    public Context getCurActCtx() {
        return mActivityContext;
    }

    public void setCurActCtx(Context activityContext) {
        mActivityContext = activityContext;
    }

    boolean verifyInstallerId(Context context) {
        // A list with valid installers package name
        List<String> validInstallers = new ArrayList<>(Arrays.asList("com.android.vending", "com.google.android.feedback"));

        // The package name of the app that has installed your app
        final String installer = context.getPackageManager().getInstallerPackageName(context.getPackageName());
        // true if your app has been downloaded from Play Store
        return installer != null && validInstallers.contains(installer);
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public AppAnalytics getAppAnalytics() {
        return mAppAnalytics;
    }

    public static boolean isForHuawei() {
        return false || BuildConfig.FLAVOR.equals("samsungChinese");
//        return (Build.MANUFACTURER.equalsIgnoreCase("Huawei"));
    }

    public static boolean isForAppGallery() {
        return BuildConfig.FLAVOR.equals("china");
    }

    public static boolean isForSamsungStore() {
        return FTApp.getPref().get(SystemPref.IS_FOR_SAMSUNG, false);
    }

    public static void userConsentDialog(Context context, FTDialogFactory.OnAlertDialogShownListener listener) {
        AlertDialog alertDialog = new AlertDialog.Builder(context)
                .setMessage(Html.fromHtml(context.getString(R.string.user_consent_message), Html.FROM_HTML_MODE_LEGACY))
                .setPositiveButton(context.getString(R.string.agree), (dialog, which) -> {
                    getPref().save(SystemPref.HAS_AGREED_PRIVACY_POLICY, true);
                    listener.onPositiveClick(dialog, which);
                })
                .setNegativeButton(context.getString(R.string.cancel), (dialog, which) -> {
                    getPref().save(SystemPref.HAS_AGREED_PRIVACY_POLICY, false);
                    listener.onNegativeClick(dialog, which);
                }).show();
        TextView textView = alertDialog.findViewById(android.R.id.message);
        textView.setLinksClickable(true);
        textView.setClickable(true);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
    }

    public static boolean verifyPolicyAgreement(Context context) {
        String oldVersion = FTApp.getPref().get(SystemPref.PRIVACY_POLICY_VERSION, "");
        String newVersion = AssetsUtil.getNewPolicyVersion(context);
        return !TextUtils.isEmpty(oldVersion) && newVersion.equalsIgnoreCase(oldVersion);
    }

    //Migration code

    public void migrationStart() {
        mFTAppConfig = FTAppConfig.getInstance();
        mFTAppConfig.copyMetadataIfNeeded("ThemesMigrationHelper.plist");
    }
    public Locale getCurrentLocale() {
        return getResources().getConfiguration().getLocales().get(0);
    }

    public boolean isChinaRegion() {
        boolean isChinaRegion = false;
        if (Locale.getDefault().getCountry().toLowerCase().contains("cn")) {
            isChinaRegion = true;
        } else if (TimeZone.getDefault().getDisplayName(false, TimeZone.SHORT).equals("GMT+08:00")
                || (TimeZone.getDefault().getID().contains("Urumqi") && TimeZone.getDefault().getDisplayName(false, TimeZone.SHORT).equals("GMT+06:00"))) {
            isChinaRegion = true;
        }
        return isChinaRegion;
    }

    public static boolean isChineseBuild() {
        return BuildConfig.FLAVOR.equalsIgnoreCase("china") || BuildConfig.FLAVOR.equalsIgnoreCase("samsungChinese");
    }

    public static boolean isProduction()
    {
        return !(BuildConfig.FLAVOR.contains("dev") || BuildConfig.FLAVOR.contains("beta"));
    }
}
