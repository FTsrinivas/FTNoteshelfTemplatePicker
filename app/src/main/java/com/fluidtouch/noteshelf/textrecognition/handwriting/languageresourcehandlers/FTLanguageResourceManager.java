package com.fluidtouch.noteshelf.textrecognition.handwriting.languageresourcehandlers;

import android.content.Context;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.widget.Toast;

import com.dd.plist.PropertyListParser;
import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf.commons.FTLog;
import com.fluidtouch.noteshelf.commons.utils.FTFileManagerUtil;
import com.fluidtouch.noteshelf.commons.utils.FTNetworkConnectionUtil;
import com.fluidtouch.noteshelf.commons.utils.ObservingService;
import com.fluidtouch.noteshelf.commons.utils.ZipUtil;
import com.fluidtouch.noteshelf.documentframework.Utilities.FTConstants;
import com.fluidtouch.noteshelf.preferences.SystemPref;
import com.fluidtouch.noteshelf.textrecognition.helpers.FTRecognitionUtils;
import com.fluidtouch.noteshelf2.R;
import com.thin.downloadmanager.DownloadRequest;
import com.thin.downloadmanager.DownloadStatusListenerV1;
import com.thin.downloadmanager.ThinDownloadManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FTLanguageResourceManager {

    public static final String languageCodeNone = "<<None>>";
    private static final String ENDPOINT = FTApp.getInstance().isChinaRegion() ? FTConstants.HUAWEI_STORE_ENDPOINT + "HW Recognition/" : "https://s3.amazonaws.com/noteshelfv2-public/";
    private static final String SAMSUNG_ENDPOINT = FTApp.getInstance().isChinaRegion() ? ENDPOINT : "https://noteshelfv2-public.s3.amazonaws.com/";
    private static final String DOWNLOAD_URL = FTApp.getInstance().isChinaRegion() ? "My_Script/v2/" : "My_Script/v2/New_Assets/";
    private static final String SAMSUNG_DOWNLOAD_URL = FTApp.getInstance().isChinaRegion() ? "Samsung/" : "samsung_hwrdb/";
    private static final FTLanguageResourceManager languageResourceManager = new FTLanguageResourceManager();

    private final ThinDownloadManager downloadManager = new ThinDownloadManager();
    private final List<FTRecognitionLangResource> languageResources = new ArrayList<>();

    private HashMap<String, String> languagesMap = new HashMap<>();

    private FTLanguageResourceManager() {
        //For copying the language tokens plist
        if (FTApp.getInstance().isChinaRegion()) {
            final String tokensPlist = FTApp.getPref().get(SystemPref.IS_SHW_ENABLED, false) ? "samsung_recog_lang.plist" : "my_script_recog_lang.plist";
            File plistFile = new File(FTConstants.DOCUMENTS_ROOT_PATH + "/" + tokensPlist);
            try {
                if (!plistFile.exists()) plistFile.getParentFile().mkdirs();
                AssetManager assetmanager = FTApp.getInstance().getApplicationContext().getAssets();
                InputStream bundleInputStrem = assetmanager.open(tokensPlist);
                plistFile = FTFileManagerUtil.createFileFromInputStream(bundleInputStrem, plistFile.getAbsolutePath());
                FileInputStream inputStream = new FileInputStream(plistFile);
                languagesMap = (HashMap<String, String>) PropertyListParser.parse(inputStream).toJavaObject();
                FTLog.debug(FTLog.LANGUAGE_DOWNLOAD, "Loaded recognition language resource tokens.");
            } catch (Exception e) {
                FTLog.error(FTLog.LANGUAGE_DOWNLOAD, "Error while copying tokens plist from assets to app storage.\n" + e.getMessage());
            }
        }
    }

    public static FTLanguageResourceManager getInstance() {
        return languageResourceManager;
    }

    public String getCurrentLanguageCode() {
        return FTApp.getPref().get(SystemPref.SELECTED_LANGUAGE, "en_US");
    }

    public void setCurrentLanguageCode(String currentLanguageCode) {
        FTApp.getPref().save(SystemPref.SELECTED_LANGUAGE, currentLanguageCode);
    }

    public String currentLanguageDisplayName() {
        FTRecognitionLangResource currentlanguage = null;
        List<FTRecognitionLangResource> availableLanguageResources = new ArrayList<>();
        if (FTApp.getPref().get(SystemPref.IS_SHW_ENABLED, false)) {
            availableLanguageResources = FTLanguageResourceManager.getInstance().availableLanguageResourcesForSHW();
        } else {
            availableLanguageResources = FTLanguageResourceManager.getInstance().availableLanguageResources();
        }
        for (FTRecognitionLangResource language : availableLanguageResources) {
            if (language.getLanguageCode().equals(getCurrentLanguageCode())) {
                currentlanguage = language;
                break;
            }
        }
        return currentlanguage != null ? getNativeDisplayName(FTApp.getInstance().getApplicationContext(), currentlanguage.getLanguageCode()) : "";
    }

    public synchronized List<FTRecognitionLangResource> availableLanguageResources() {
        if (this.languageResources.isEmpty()) {
            String[] arrayLangList = {"en_US", "en_GB", "zh_CN", "zh_TW", "de_DE", "es_ES", "fr_FR", "it_IT", "pt_PT", "ja_JP", "ko_KR", "bs_BA", "ceb_PH",
                    "az_AZ", "no_NO", "sw_TZ", "fil_PH", "mg_MG", "af_ZA", "sq_AL", "hy_AM", "eu_ES", "be_BY", "bg_BG", "ca_ES",
                    "hr_HR", "cs_CZ", "da_DK", "et_EE", "fi_FI", "gl_ES", "ka_GE", "el_GR", "hu_HU", "is_IS", "id_ID", "ga_IE", "kk_KZ", "lv_LV", "lt_LT",
                    "mk_MK", "ms_MY", "mn_MN", "pl_PL", "ro_RO", "ru_RU", "sk_SK", "sl_SI", "sv_SE", "tt_RU", "tr_TR", "uk_UA", "vi_VN", "<<None>>"};
            for (String lang : arrayLangList) {
                FTRecognitionLangResource langResource = new FTRecognitionLangResource(getNativeDisplayName(FTApp.getInstance().getApplicationContext(), lang), lang);
                if (lang.equals(languageCodeNone))
                    langResource.setResourceStatus(FTLanguageResourceStatus.DOWNLOADED);
                this.languageResources.add(langResource);
            }
        }
        return this.languageResources;
    }

    public synchronized List<FTRecognitionLangResource> availableLanguageResourcesForSHW() {
        if (this.languageResources.isEmpty()) {
            String[] arrayLangList = {"en_US", "en_GB", "zh_CN", "zh_TW", "zh_HK", "de_DE", "es_ES", "fr_FR", "it_IT", "pt_PT", "ja_JP", "ko_KR", "bs_BA",
                    "az_AZ", "af_ZA", "sq_AL", "hy_AM", "eu_ES", "be_BY", "bg_BG", "ca_ES",
                    "hr_HR", "cs_CZ", "da_DK", "et_EE", "fi_FI", "gl_ES", "ka_GE", "el_GR", "hu_HU", "is_IS", "id_ID", "ga_IE", "kk_KZ", "lv_LV", "lt_LT",
                    "mk_MK", "ms_MY", "mn_MN", "pl_PL", "ro_RO", "ru_RU", "sk_SK", "sl_SI", "sv_SE", "tr_TR", "uk_UA", "vi_VN", "ar", "bn_BD",
                    "en_AU", "es_MX", "es_US", "fa_IR", "fr_CA", "he_IL", "hg_IN", "hi_IN", "mr_IN", "nb_NO", "nl_BE", "nl_NL", "pt_BR",
                    "sr_RS", "tg_TJ", "th_TH", "tk_TM", "tl_PH", "ur_PK", "uz_UZ", "<<None>>"};
            for (String lang : arrayLangList) {
                FTRecognitionLangResource langResource = new FTRecognitionLangResource(getNativeDisplayName(FTApp.getInstance().getApplicationContext(), lang), lang);
                if (lang.equals(languageCodeNone))
                    langResource.setResourceStatus(FTLanguageResourceStatus.DOWNLOADED);
                this.languageResources.add(langResource);
            }
        }
        return this.languageResources;
    }

    public static String getNativeDisplayName(Context context, String languageCode) {
        String nativeDisplayTitle;
        switch (languageCode) {
            case "en_US":
                nativeDisplayTitle = "English (US)";
                break;
            case "en_GB":
                nativeDisplayTitle = "English (UK)";
                break;
            case "zh_CN":
                nativeDisplayTitle = "汉语（简体）";
                break;
            case "zh_TW":
                nativeDisplayTitle = "漢語（繁體）";
                break;
            case "de_DE":
                nativeDisplayTitle = "Deutsch";
                break;
            case "fr_FR":
                nativeDisplayTitle = "French(France)";
                break;
            case "fr_CA":
                nativeDisplayTitle = "Français (Canada)";
                break;
            case "es_ES":
                nativeDisplayTitle = "Español (España)";
                break;
            case "it_IT":
                nativeDisplayTitle = "Italiana";
                break;
            case "ja_JP":
                nativeDisplayTitle = "日本語";
                break;
            case "pt_PT":
                nativeDisplayTitle = "Português (Portugal)";
                break;
            case "ko_KR":
                nativeDisplayTitle = "한국어";
                break;
//==========================================================
            case "af_ZA":
                nativeDisplayTitle = "Afrikaners";
                break;
            case "sq_AL":
                nativeDisplayTitle = "shqiptar";
                break;
            case "hy_AM":
                nativeDisplayTitle = "հայերեն";
                break;
            case "az_AZ":
                nativeDisplayTitle = "Azərbaycan";
                break;
            case "eu_ES":
                nativeDisplayTitle = "Euskal";
                break;
            case "be_BY":
                nativeDisplayTitle = "беларускі";
                break;
            case "bg_BG":
                nativeDisplayTitle = "български";
                break;
            case "ca_ES":
                nativeDisplayTitle = "català";
                break;
            case "zh_HK":
                nativeDisplayTitle = "中國（香港)";
                break;
            case "hr_HR":
                nativeDisplayTitle = "Hrvatski";
                break;
            case "cs_CZ":
                nativeDisplayTitle = "čeština";
                break;
            case "da_DK":
                nativeDisplayTitle = "dansk";
                break;
            case "nl_BE":
                nativeDisplayTitle = "Nederlands (België)";
                break;
            case "nl_NL":
                nativeDisplayTitle = "Nederlands (Nederland)";
                break;
            case "en_CA":
                nativeDisplayTitle = "English(Canada)";
                break;
            case "et_EE":
                nativeDisplayTitle = "eesti";
                break;
            case "fi_FI":
                nativeDisplayTitle = "Suomalainen";
                break;
            case "gl_ES":
                nativeDisplayTitle = "galego";
                break;
            case "ka_GE":
                nativeDisplayTitle = "ქართული";
                break;
            case "de_AT":
                nativeDisplayTitle = "Deutsch (Österreich)";
                break;
            case "el_GR":
                nativeDisplayTitle = "Ελληνικά";
                break;
            case "hu_HU":
                nativeDisplayTitle = "Magyar";
                break;
            case "id_ID":
                nativeDisplayTitle = "bahasa Indonesia";
                break;
            case "ga_IE":
                nativeDisplayTitle = "Gaeilge";
                break;
            case "is_IS":
                nativeDisplayTitle = "Íslensku";
                break;
            case "kk_KZ":
                nativeDisplayTitle = "Қазақ";
                break;
            case "lv_LV":
                nativeDisplayTitle = "Latvijas";
                break;
            case "lt_LT":
                nativeDisplayTitle = "Lietuvos";
                break;
            case "mk_MK":
                nativeDisplayTitle = "Македонски";
                break;
            case "ms_MY":
                nativeDisplayTitle = "Malay";
                break;
            case "mn_MN":
                nativeDisplayTitle = "Монгол";
                break;
            case "no_NO":
                nativeDisplayTitle = "norsk";
                break;
            case "pl_PL":
                nativeDisplayTitle = "Polskie";
                break;
            case "pt_BR":
                nativeDisplayTitle = "Português (Brasil)";
                break;
            case "ro_RO":
                nativeDisplayTitle = "Română";
                break;
            case "ru_RU":
                nativeDisplayTitle = "русский";
                break;
            case "sr_Cyrl_RS":
                nativeDisplayTitle = "Српски језик (Ћирилица)";
                break;
            case "sr_Latn_RS":
                nativeDisplayTitle = "Српски (Ћирилица)";
                break;
            case "sk_SK":
                nativeDisplayTitle = "slovenský";
                break;
            case "sl_SI":
                nativeDisplayTitle = "Slovenščina";
                break;
            case "es_MX":
                nativeDisplayTitle = "Español (México)";
                break;
            case "sv_SE":
                nativeDisplayTitle = "svenska";
                break;
            case "tt_RU":
                nativeDisplayTitle = "Tatar";
                break;
            case "tr_TR":
                nativeDisplayTitle = "Türkçe";
                break;
            case "uk_UA":
                nativeDisplayTitle = "український";
                break;
            case "vi_VN":
                nativeDisplayTitle = "Tiếng Việt";
                break;
            case "sw_TZ":
                nativeDisplayTitle = "Swahili (Tanzania)";
                break;
            case "bs_BA":
                nativeDisplayTitle = "Bosanski";
                break;
            case "ceb_PH":
                nativeDisplayTitle = "Sugbo (Pilipinas)";
                break;
            case "en_PH":
                nativeDisplayTitle = "English (Philippines)";
                break;
            case "fil_PH":
                nativeDisplayTitle = "Filipino (Pilipinas)";
                break;
            case "mg_MG":
                nativeDisplayTitle = "Malagasy (Madagascar)";
                break;
            case "es_CO":
                nativeDisplayTitle = "Español (Colombia)";
                break;
            case "ar":
                nativeDisplayTitle = "العربية";
                break;
            case "bn_BD":
                nativeDisplayTitle = "বাাংলা(Bangladesh)";
                break;
            case "en_AU":
                nativeDisplayTitle = "English(Australia)";
                break;
            case "es_US":
                nativeDisplayTitle = "Español(Estados Unidos)";
                break;
            case "fa_IR":
                nativeDisplayTitle = "فارسی";
                break;
            case "he_IL":
                nativeDisplayTitle = "עברית";
                break;
            case "hg_IN":
                nativeDisplayTitle = "Hinglish";
                break;
            case "hi_IN":
                nativeDisplayTitle = "हिन्दी";
                break;
            case "mr_IN":
                nativeDisplayTitle = "मराठी";
                break;
            case "nb_NO":
                nativeDisplayTitle = "Norsk Bokmål";
                break;
            case "sr_RS":
                nativeDisplayTitle = "Srpski(Latin)";
                break;
            case "tg_TJ":
                nativeDisplayTitle = "тоҷикӣ(Tajikistan)";
                break;
            case "th_TH":
                nativeDisplayTitle = "ไทย";
                break;
            case "tk_TM":
                nativeDisplayTitle = "Türkmen, Түркмен(Turkmenistan)";
                break;
            case "tl_PH":
                nativeDisplayTitle = "Wikang Tagalog(Philippines)";
                break;
            case "ur_PK":
                nativeDisplayTitle = "اردو";
                break;
            case "uz_UZ":
                nativeDisplayTitle = "Oʻzbek, Ўзбек";
                break;
//==========================================================
            case "<<None>>":
                nativeDisplayTitle = context.getString(R.string.disable_recognition);
                break;
            default:
                nativeDisplayTitle = "English (US)";
                break;
        }
        return nativeDisplayTitle;
    }

    public static String getLocalisedLanguage(Context context, String languageCode) {
        String nativeDisplayTitle;
        switch (languageCode) {
            case "en_US":
                nativeDisplayTitle = context.getString(R.string.english_us);
                break;
            case "en_GB":
                nativeDisplayTitle = context.getString(R.string.english_uk);
                break;
            case "zh_CN":
                nativeDisplayTitle = context.getString(R.string.chinese_simplified);
                break;
            case "zh_TW":
                nativeDisplayTitle = context.getString(R.string.chinese_traditional);
                break;
            case "de_DE":
                nativeDisplayTitle = context.getString(R.string.german);
                break;
            case "fr_FR":
                nativeDisplayTitle = context.getString(R.string.french);
                break;
            case "fr_CA":
                nativeDisplayTitle = context.getString(R.string.french_canada);
                break;
            case "es_ES":
                nativeDisplayTitle = context.getString(R.string.spanish);
                break;
            case "it_IT":
                nativeDisplayTitle = context.getString(R.string.italian);
                break;
            case "ja_JP":
                nativeDisplayTitle = context.getString(R.string.japanese);
                break;
            case "pt_PT":
                nativeDisplayTitle = context.getString(R.string.portugal);
                break;
            case "ko_KR":
                nativeDisplayTitle = context.getString(R.string.korean);
                break;
//==========================================================
            case "af_ZA":
                nativeDisplayTitle = context.getString(R.string.africans);
                break;
            case "sq_AL":
                nativeDisplayTitle = context.getString(R.string.albanian);
                break;
            case "hy_AM":
                nativeDisplayTitle = context.getString(R.string.armenian);
                break;
            case "az_AZ":
                nativeDisplayTitle = context.getString(R.string.azeri);
                break;
            case "eu_ES":
                nativeDisplayTitle = context.getString(R.string.spanish_spain);
                break;
            case "be_BY":
                nativeDisplayTitle = context.getString(R.string.belarusian);
                break;
            case "bg_BG":
                nativeDisplayTitle = context.getString(R.string.bulgarian);
                break;
            case "ca_ES":
                nativeDisplayTitle = context.getString(R.string.spanish_colombia);
                break;
            case "zh_HK":
                nativeDisplayTitle = context.getString(R.string.chinese_hongkong);
                break;
            case "hr_HR":
                nativeDisplayTitle = context.getString(R.string.croatian);
                break;
            case "cs_CZ":
                nativeDisplayTitle = context.getString(R.string.czech);
                break;
            case "da_DK":
                nativeDisplayTitle = context.getString(R.string.danish);
                break;
            case "nl_BE":
                nativeDisplayTitle = context.getString(R.string.dutch_belgium);
                break;
            case "nl_NL":
                nativeDisplayTitle = context.getString(R.string.dutch_netherlands);
                break;
            case "en_CA":
                nativeDisplayTitle = context.getString(R.string.english_canada);
                break;
            case "et_EE":
                nativeDisplayTitle = context.getString(R.string.estonian);
                break;
            case "fi_FI":
                nativeDisplayTitle = context.getString(R.string.finnish);
                break;
            case "gl_ES":
                nativeDisplayTitle = context.getString(R.string.galician);
                break;
            case "ka_GE":
                nativeDisplayTitle = context.getString(R.string.georgian);
                break;
            case "de_AT":
                nativeDisplayTitle = context.getString(R.string.german_austria);
                break;
            case "el_GR":
                nativeDisplayTitle = context.getString(R.string.greek);
                break;
            case "hu_HU":
                nativeDisplayTitle = context.getString(R.string.hungarian);
                break;
            case "id_ID":
                nativeDisplayTitle = context.getString(R.string.indonesian);
                break;
            case "ga_IE":
                nativeDisplayTitle = context.getString(R.string.irish);
                break;
            case "is_IS":
                nativeDisplayTitle = context.getString(R.string.icelandic);
                break;
            case "kk_KZ":
                nativeDisplayTitle = context.getString(R.string.kazakh);
                break;
            case "lv_LV":
                nativeDisplayTitle = context.getString(R.string.latvian);
                break;
            case "lt_LT":
                nativeDisplayTitle = context.getString(R.string.lithuanian);
                break;
            case "mk_MK":
                nativeDisplayTitle = context.getString(R.string.macedonian);
                break;
            case "ms_MY":
                nativeDisplayTitle = context.getString(R.string.malay);
                break;
            case "mn_MN":
                nativeDisplayTitle = context.getString(R.string.mongolian);
                break;
            case "no_NO":
                nativeDisplayTitle = context.getString(R.string.norwegian);
                break;
            case "pl_PL":
                nativeDisplayTitle = context.getString(R.string.polish);
                break;
            case "pt_BR":
                nativeDisplayTitle = context.getString(R.string.portuguese_brazil);
                break;
            case "ro_RO":
                nativeDisplayTitle = context.getString(R.string.romanian);
                break;
            case "ru_RU":
                nativeDisplayTitle = context.getString(R.string.russian);
                break;
            case "sr_Cyrl_RS":
                nativeDisplayTitle = context.getString(R.string.serbian_cyrillic);
                break;
            case "sr_Latn_RS":
                nativeDisplayTitle = context.getString(R.string.serbian_latin);
                break;
            case "sk_SK":
                nativeDisplayTitle = context.getString(R.string.slovak);
                break;
            case "sl_SI":
                nativeDisplayTitle = context.getString(R.string.slovenian);
                break;
            case "es_MX":
                nativeDisplayTitle = context.getString(R.string.spanish_mexico);
                break;
            case "sv_SE":
                nativeDisplayTitle = context.getString(R.string.swedish);
                break;
            case "tt_RU":
                nativeDisplayTitle = context.getString(R.string.tatar);
                break;
            case "tr_TR":
                nativeDisplayTitle = context.getString(R.string.turkish);
                break;
            case "uk_UA":
                nativeDisplayTitle = context.getString(R.string.ukrainian);
                break;
            case "vi_VN":
                nativeDisplayTitle = context.getString(R.string.vietnamese);
                break;
            case "sw_TZ":
                nativeDisplayTitle = context.getString(R.string.swahili_tanzania);
                break;
            case "bs_BA":
                nativeDisplayTitle = context.getString(R.string.bosnian);
                break;
            case "ceb_PH":
                nativeDisplayTitle = context.getString(R.string.cebuano_philippines);
                break;
            case "en_PH":
                nativeDisplayTitle = context.getString(R.string.english_philippines);
                break;
            case "fil_PH":
                nativeDisplayTitle = context.getString(R.string.filipino_philippines);
                break;
            case "mg_MG":
                nativeDisplayTitle = context.getString(R.string.malagasy_madagascar);
                break;
            case "es_CO":
                nativeDisplayTitle = context.getString(R.string.spanish_colombia);
                break;
            case "ar":
                nativeDisplayTitle = context.getString(R.string.arabic);
                break;
            case "bn_BD":
                nativeDisplayTitle = context.getString(R.string.bengali);
                break;
            case "en_AU":
                nativeDisplayTitle = context.getString(R.string.bengali);
                break;
            case "es_US":
                nativeDisplayTitle = context.getString(R.string.spanish_US);
                break;
            case "fa_IR":
                nativeDisplayTitle = context.getString(R.string.farsi_Persian);
                break;
            case "he_IL":
                nativeDisplayTitle = context.getString(R.string.hebrew);
                break;
            case "hg_IN":
                nativeDisplayTitle = context.getString(R.string.hinglish);
                break;
            case "hi_IN":
                nativeDisplayTitle = context.getString(R.string.hindi);
                break;
            case "mr_IN":
                nativeDisplayTitle = context.getString(R.string.marathi);
                break;
            case "nb_NO":
                nativeDisplayTitle = context.getString(R.string.norwegian_bokmål);
                break;
            case "sr_RS":
                nativeDisplayTitle = context.getString(R.string.serbian);
                break;
            case "tg_TJ":
                nativeDisplayTitle = context.getString(R.string.tajik);
                break;
            case "th_TH":
                nativeDisplayTitle = context.getString(R.string.thai);
                break;
            case "tk_TM":
                nativeDisplayTitle = context.getString(R.string.turkmen);
                break;
            case "tl_PH":
                nativeDisplayTitle = context.getString(R.string.tagalog);
                break;
            case "ur_PK":
                nativeDisplayTitle = context.getString(R.string.urdu);
                break;
            case "uz_UZ":
                nativeDisplayTitle = context.getString(R.string.uzbek);
                break;
//==========================================================
            case "<<None>>":
                nativeDisplayTitle = context.getString(R.string.disable_recognition);
                break;
            default:
                nativeDisplayTitle = "English (US)";
                break;
        }
        return nativeDisplayTitle;
    }

    public static String languageMapping(String currentLang) {
        currentLang = currentLang.toLowerCase();
        String scriptLanguageCode = "en_US";
        if (currentLang.contains("zh")) {
            if (currentLang.contains("cn")) {
                scriptLanguageCode = "zh_CN";
            } else if (currentLang.contains("hk")) {
                scriptLanguageCode = "zh_HK";
            } else if (currentLang.contains("tw")) {
                scriptLanguageCode = "zh_TW";
            }
        }
        return scriptLanguageCode;
    }

    synchronized void downloadResource(FTRecognitionLangResource language) {
        if (!FTNetworkConnectionUtil.INSTANCE.isNetworkAvailable(FTApp.getInstance().getApplicationContext())) {
            downloadManager.cancelAll();
            language.setResourceStatus(FTLanguageResourceStatus.NONE);
            ObservingService.getInstance().postNotification("languageDownloaded", language);
            return;
        }

        File recognitionAssetsFolder = new File(FTRecognitionUtils.recognitionResourcesFolderURL(FTApp.getInstance().getApplicationContext()).getPath() + "/");
        if (!recognitionAssetsFolder.exists())
            recognitionAssetsFolder.mkdirs();

        Uri downloadUri;
        Uri destinationUri;
        String endPoint = "";
        String token = languagesMap.get(language.getLanguageCode());

        if (FTApp.getPref().get(SystemPref.IS_SHW_ENABLED, false)) {
            endPoint = SAMSUNG_ENDPOINT;
            String URL = SAMSUNG_ENDPOINT + SAMSUNG_DOWNLOAD_URL;
            downloadUri = Uri.parse(URL + "hwr_" + language.getLanguageCode() + FTConstants.DAT_EXTENSION + (TextUtils.isEmpty(token) ? "" : "?token=" + token));
            destinationUri = Uri.parse(FTRecognitionUtils.recognitionResourcesFolderURL(FTApp.getInstance().getCurActCtx()).getPath() + "/hwr_" + language.getLanguageCode() + FTConstants.DAT_EXTENSION);
        } else {
            endPoint = ENDPOINT;
            String URL = ENDPOINT + DOWNLOAD_URL;
            downloadUri = Uri.parse(URL + "recognition-assets-" + language.getLanguageCode() + FTConstants.ZIP_EXTENSION + (TextUtils.isEmpty(token) ? "" : "?token=" + token));
            destinationUri = Uri.parse(FTConstants.HW_DOWNLOAD_PATH + "recognition-assets-" + language.getLanguageCode() + FTConstants.ZIP_EXTENSION);
        }
        if (FTApp.shouldLog()) {
            Toast.makeText(FTApp.getInstance().getApplicationContext(), endPoint, Toast.LENGTH_LONG).show();
        }

        DownloadRequest downloadRequest = new DownloadRequest(downloadUri)
                .setDestinationURI(destinationUri)
                .setDeleteDestinationFileOnFailure(true)
                .setPriority(DownloadRequest.Priority.HIGH)
                .setStatusListener(new DownloadStatusListenerV1() {
                    @Override
                    public void onDownloadComplete(final DownloadRequest downloadRequest) {
                        if (FTApp.getPref().get(SystemPref.IS_SHW_ENABLED, false)) {
                            FTLog.debug(FTLog.LANGUAGE_DOWNLOAD, "Downloaded recognition language resource = " + language.getLanguageCode());
                            language.setResourceStatus(FTLanguageResourceStatus.DOWNLOADED);
                            ObservingService.getInstance().postNotification("languageDownloaded", language);
                        } else {
                            FTLog.debug(FTLog.LANGUAGE_DOWNLOAD, "Downloaded recognition language resource = " + language.getLanguageCode());
                            AsyncTask.execute(() -> ZipUtil.unzip(FTApp.getInstance().getCurActCtx(), downloadRequest.getDestinationURI().getPath(), FTRecognitionUtils.recognitionResourcesFolderURL(FTApp.getInstance().getApplicationContext()).getPath(), (file, error) -> {
                                FTLog.debug(FTLog.LANGUAGE_DOWNLOAD, "UnZipped asset " + language.getLanguageCode());
                                language.setResourceStatus(FTLanguageResourceStatus.DOWNLOADED);
                                ObservingService.getInstance().postNotification("languageDownloaded", language);
                            }));
                        }
                    }

                    @Override
                    public void onDownloadFailed(DownloadRequest downloadRequest,
                                                 int errorCode, String errorMessage) {
                        FTLog.error(FTLog.LANGUAGE_DOWNLOAD, "Failed to download recognition language\n" + errorMessage);
                        if (FTApp.getPref().get(SystemPref.IS_SHW_ENABLED, false)) {
                            FTFileManagerUtil.deleteRecursive(Uri.parse(FTRecognitionUtils.recognitionResourcesFolderURL(FTApp.getInstance().getCurActCtx()).getPath() + "/hwr_" + language.getLanguageCode() + FTConstants.DAT_EXTENSION).getPath());
                        } else
                            FTFileManagerUtil.deleteRecursive(Uri.parse(FTConstants.HW_DOWNLOAD_PATH + "recognition-assets-" + language.getLanguageCode() + FTConstants.ZIP_EXTENSION).getPath());
                        language.setResourceStatus(FTLanguageResourceStatus.NONE);
                        ObservingService.getInstance().postNotification("languageDownloaded", language);
                    }

                    @Override
                    public void onProgress(DownloadRequest downloadRequest, long totalBytes, long downloadedBytes, int progress) {
                        if (progress == 100)
                            FTLog.debug(FTLog.LANGUAGE_DOWNLOAD, "Downloaded" + (downloadedBytes + "/" + totalBytes));
                    }
                });

        synchronized (downloadManager) {
            downloadManager.add(downloadRequest);
            FTLog.debug(FTLog.LANGUAGE_DOWNLOAD, "Started downloading recognition language resource from " + downloadRequest.getUri());
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        downloadManager.cancelAll();
        downloadManager.release();
    }
}