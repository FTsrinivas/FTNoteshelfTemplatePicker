package com.fluidtouch.noteshelf.shelf.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf.commons.FTLog;
import com.fluidtouch.noteshelf.commons.ui.FTBaseDialog;
import com.fluidtouch.noteshelf.commons.utils.ScreenUtil;
import com.fluidtouch.noteshelf.documentframework.Utilities.FTConstants;
import com.fluidtouch.noteshelf.models.theme.FTNAutoTemlpateDiaryTheme;
import com.fluidtouch.noteshelf.models.theme.FTNPaperTheme;
import com.fluidtouch.noteshelf.models.theme.FTNTheme;
import com.fluidtouch.noteshelf.models.theme.FTNThemeCategory;
import com.fluidtouch.noteshelf.preferences.SystemPref;
import com.fluidtouch.noteshelf.services.FTFirebaseAnalytics;
import com.fluidtouch.noteshelf.shelf.activities.FTBaseShelfActivity;
import com.fluidtouch.noteshelf.store.ui.FTChooseCoverPaperDialog;
import com.fluidtouch.noteshelf.store.ui.FTNewNotebookDialog;
import com.fluidtouch.noteshelf.templatepicker.FTChoosePaperTemplate;
import com.fluidtouch.noteshelf.templatepicker.FTTemplateMode;
import com.fluidtouch.noteshelf.templatepicker.common.FTTemplatesInfoSingleton;
import com.fluidtouch.noteshelf.templatepicker.common.plistdatamodel.FTSelectedDeviceInfo;
import com.fluidtouch.noteshelf.templatepicker.common.util.FTRecentlyDeletedTemplateInfo;
import com.fluidtouch.noteshelf.templatepicker.common.util.FTTemplateUtil;
import com.fluidtouch.noteshelf.templatepicker.models.FTTemplatepickerInputInfo;
import com.fluidtouch.noteshelf.templatepicker.models.RecentsInfoModel;
import com.fluidtouch.noteshelf.templatepicker.models.TemplatesInfoModel;
import com.fluidtouch.noteshelf2.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;

public class FTQuickCreateSettingsPopup extends FTBaseDialog.Popup implements
                                                FTChooseCoverPaperDialog.CoverChooseListener,
                                                FTChoosePaperTemplate.TemplatePaperChooseListener {
    @BindView(R.id.tvDefaultPaper)
    TextView mDefaultPaperTextView;

    @BindView(R.id.switch_RandomCoverDesign)
    Switch mRandomCoverDesignSwitch;

    ArrayList<FTNTheme> ftnThemeArrayListPrefs = new ArrayList<>();
    ArrayList<String> ftnThemeThumbnailURLPathList = new ArrayList<>();

    private FTNTheme paperTheme;
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        if (!isMobile()) {
            Window window = dialog.getWindow();
            if (window != null) {
                window.setGravity(Gravity.TOP | Gravity.END);
                window.getAttributes().y += ScreenUtil.getStatusBarHeight(getContext());
            }
        }
        super.dismissParent = false;
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.popup_quick_create_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        view.getViewTreeObserver().addOnWindowFocusChangeListener(hasFocus -> {
            if (hasFocus) {
                String paperPackName = FTApp.getPref().get(SystemPref.QUICK_CREATE_PAPER_THEME_NAME, FTConstants.DEFAULT_PAPER_THEME_NAME);
                FTNTheme paperTheme = FTNTheme.theme(FTNThemeCategory.getUrl(paperPackName));
                if (TextUtils.isEmpty(paperTheme.packName)) {
                    mDefaultPaperTextView.setText(FTConstants.DEFAULT_PAPER_THEME_NAME.replace("_", " ").replace(".nsp", ""));
                    FTApp.getPref().save(SystemPref.QUICK_CREATE_PAPER_THEME_NAME, FTConstants.DEFAULT_PAPER_THEME_NAME);
                }
            }
        });

        String paperPackName = FTApp.getPref().get(SystemPref.QUICK_CREATE_PAPER_THEME_NAME, FTConstants.DEFAULT_PAPER_THEME_NAME);
        FTNTheme paperTheme = FTNTheme.theme(FTNThemeCategory.getUrl(paperPackName));
        if (!TextUtils.isEmpty(paperPackName)) {
            mDefaultPaperTextView.setText((paperTheme.dynamicId == 1) ? ((FTNAutoTemlpateDiaryTheme) paperTheme).templateId : paperPackName.replace("_", " ").replace(".nsp", ""));
        }

        mRandomCoverDesignSwitch.setChecked(FTApp.getPref().get(SystemPref.RANDOM_COVER_DESIGN_ENABLED, false));
    }

    @OnClick(R.id.tvDialogTitle)
    void onBackClicked() {
        dismiss();
    }

    @OnClick(R.id.default_paper_layout)
    void onDefaultPaperClicked() {
        //FTChoosePaperTemplate.newInstance(FTNThemeCategory.FTThemeType.PAPER).show(getChildFragmentManager(), FTChooseCoverPaperDialog.class.getName());
        FTFirebaseAnalytics.logEvent("Shelf_AddNew_QuickCreateSett_DefPaper");
        //FTChooseCoverPaperDialog.newInstance(FTNThemeCategory.FTThemeType.PAPER).show(getChildFragmentManager(), FTChooseCoverPaperDialog.class.getName());

        FTTemplatepickerInputInfo _ftTemplatepickerInputInfo = new FTTemplatepickerInputInfo();
        _ftTemplatepickerInputInfo.set_baseShelfActivity(null);
        _ftTemplatepickerInputInfo.set_ftTemplateOpenMode(FTTemplateMode.QuickCreateNotebook);
        _ftTemplatepickerInputInfo.set_ftThemeType(FTNThemeCategory.FTThemeType.PAPER);
        _ftTemplatepickerInputInfo.set_notebookTitle(null);
        FTChoosePaperTemplate.newInstance1(_ftTemplatepickerInputInfo).show(getChildFragmentManager(), FTChooseCoverPaperDialog.class.getName());

    }

    @OnCheckedChanged(R.id.switch_RandomCoverDesign)
    void onRandomCoverDesignCheckChanged(CompoundButton compoundButton, boolean isChecked) {
        FTFirebaseAnalytics.logEvent("Shelf_AddNew_QuickCreateSett_RandCover");
        FTApp.getPref().save(SystemPref.RANDOM_COVER_DESIGN_ENABLED, isChecked);
    }

    @Override
    public void onThemeChosen(FTNTheme paperTheme, boolean isCurrentPage,boolean isLandScapeStatus) {

        /*if ((paperTheme.ftThemeType == FTNThemeCategory.FTThemeType.PAPER)) {
             Log.d("TemplatePicker==>","FTQuickCreateSettingsPopup instanceof FTNPaperTheme Status onThemeChosen Status::-"+
                     (paperTheme instanceof FTNPaperTheme));
             FTSelectedDeviceInfo ftSelectedDeviceInfo = FTSelectedDeviceInfo.selectedDeviceInfo();
            ArrayList<FTNTheme> ftRecentPapersThemesList = FTTemplateUtil.getInstance().getRecentPapersThemesList();
            if (ftRecentPapersThemesList != null &&
                    !ftRecentPapersThemesList.isEmpty()) {
                ArrayList<String> recentPapersThumbnailPathURLsList = FTTemplateUtil.getInstance().getRecentPapersThumbnailPathURLsList();
                Log.d("TemplatePicker==>","FTQuickCreateSettingsPopup Status ftRecentPapersThemesList packName::-"+
                        recentPapersThumbnailPathURLsList +" packName::-"+paperTheme.thumbnailURLPath);
                if (!recentPapersThumbnailPathURLsList.contains(paperTheme.thumbnailURLPath)) {
                    if (paperTheme.thumbnailURLPath != null) {
                        Log.d("TemplatePicker==>","FTQuickCreateSettingsPopup instanceof FTNPaperTheme Status onThemeChosen IF Status::-"+
                                (paperTheme instanceof FTNPaperTheme)+ " thumbnailURLPath::-"+paperTheme.thumbnailURLPath);
                        FTTemplateUtil.getInstance().saveRecentPapersThemesList(paperTheme,"FTQuickCreateSettingsPopup already in recents some papers exists");
                        FTTemplateUtil.getInstance().saveRecentPaperThemeFromQuickCreateOption(paperTheme);
                    }
                } else {
                    for (int i=0;i<ftRecentPapersThemesList.size();i++) {
                        if (ftRecentPapersThemesList.get(i).thumbnailURLPath.equalsIgnoreCase(paperTheme.thumbnailURLPath)) {
                            Log.d("TemplatePicker==>","FTQuickCreateSettingsPopup instanceof FTNPaperTheme Status onThemeChosen ELSE Status::-"+
                                    (ftRecentPapersThemesList.get(i) instanceof FTNPaperTheme)+ " thumbnailURLPath::-"+paperTheme.thumbnailURLPath);
                            FTTemplateUtil.getInstance().saveRecentPaperThemeFromQuickCreateOption(ftRecentPapersThemesList.get(i));
                        }
                    }
                }
            } else {
                Log.d("TemplatePicker==>","FTQuickCreateSettingsPopup instanceof FTNPaperTheme Status onThemeChosen Main ELSE Status::-"+
                        (paperTheme instanceof FTNPaperTheme));

                paperTheme.width  = ftSelectedDeviceInfo.getPageWidth();
                paperTheme.height = ftSelectedDeviceInfo.getPageHeight();

                FTTemplateUtil.getInstance().saveRecentPapersThemesList(paperTheme,"FTQuickCreateSettingsPopup already in recents some papers exists");
                FTTemplateUtil.getInstance().saveRecentPaperThemeFromQuickCreateOption(paperTheme);
            }
        }*/

        paperTheme = paperThemeItem(paperTheme);
        mDefaultPaperTextView.setText(paperTheme.themeName);

        /*if (paperTheme instanceof FTNPaperTheme) {
            Log.d("TemplatePicker==>","FTQuickCreateSettingsPopup onThemeChosen thumbnailURLPath2::-"+paperTheme.thumbnailURLPath+
                    " themeName::-"+paperTheme.themeName+" isLandscape::-"+paperTheme.isLandscape()+" themeBgClr::-"+paperTheme.themeBgClr);
            mDefaultPaperTextView.setText(FTTemplatesInfoSingleton.getInstance().getNSPFileNameWithoutExtn(paperTheme));

            FTLog.crashlyticsLog("UI: Paper chosen for notebook");
            Log.d("TemplatePicker==>","FTQuickCreateSettingsPopup onThemeChosen paperTheme.themeName::-"+paperTheme.themeName);
        }*/

    }

    private FTNTheme paperThemeItem(FTNTheme paperTheme) {
        if ((paperTheme.ftThemeType == FTNThemeCategory.FTThemeType.PAPER)) {
            ArrayList<RecentsInfoModel> recentsInfoList =  FTTemplateUtil.getInstance().getRecentPapersDummy();
            boolean recentsInfoListIsNullOrEmpty = FTTemplateUtil.isNullOrEmpty(recentsInfoList);
            Log.d("TemplatePicker==>","FTTemplateUtil FTQuickCreateSettingsPopup Recents List Existence status " +recentsInfoListIsNullOrEmpty);

            if (!recentsInfoListIsNullOrEmpty) {
                for (int i=0;i<recentsInfoList.size();i++) {
                    Log.d("TemplatePicker==>","FTTemplateUtil FTQuickCreateSettingsPopup get_packName " +recentsInfoList.get(i).get_packName()
                            +"get_thumbnailURLPath:: "+recentsInfoList.get(i).get_thumbnailURLPath());
                }

                boolean _themeAlreadyExists = recentsInfoList.stream()
                        .anyMatch(p -> p.get_thumbnailURLPath().equals(paperTheme.thumbnailURLPath));
                Log.d("TemplatePicker==>","FTTemplateUtil FTQuickCreateSettingsPopup Recents List Exists" +
                        " thumbnailURLPath::-"+paperTheme.thumbnailURLPath +
                        " width:: "+paperTheme.width+
                        " height:: "+paperTheme.height+
                        " _themeAlreadyExists:: "+_themeAlreadyExists);
                if (!_themeAlreadyExists) {
                    FTTemplateUtil.getInstance().saveRecentPaperThemeFromQuickCreateDialog(paperTheme);
                    FTTemplateUtil.getInstance().saveRecentPapersDummy(paperTheme);
                }
            } else {
                Log.d("TemplatePicker==>","FTTemplateUtil FTQuickCreateSettingsPopup Recents List Fresh" +
                        " thumbnailURLPath::-"+paperTheme.thumbnailURLPath +
                        " width:: "+paperTheme.width+
                        " height:: "+paperTheme.height+" bitmap:: "+paperTheme.bitmap);
                FTTemplateUtil.getInstance().saveRecentPaperThemeFromQuickCreateDialog(paperTheme);
                FTTemplateUtil.getInstance().saveRecentPapersDummy(paperTheme);
            }
        }
        return paperTheme;
    }

    @Override
    public void addCustomTheme(FTNTheme theme) {
        if (getContext() instanceof FTBaseShelfActivity) {
            ((FTBaseShelfActivity) getContext()).addCustomTheme(theme);
        }
    }

    @Override
    public void onClose() {
        onResume();
    }

    @Override
    public boolean isCurrentTheme() {
        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
        String paperPackName = FTApp.getPref().get(SystemPref.RECENT_PAPER_THEME_NAME, FTConstants.DEFAULT_PAPER_THEME_NAME);
        RecentsInfoModel recentsInfoModel  = FTTemplateUtil.getInstance().getRecentPaperThemeFromQuickCreateDialog();

        if (recentsInfoModel != null) {
            paperPackName = recentsInfoModel.get_packName();
        }

        if (paperPackName.endsWith(".nsp")) {
            paperTheme = FTNTheme.theme(FTNThemeCategory.getUrl(paperPackName));
        }

        if (recentsInfoModel != null) {
            paperTheme.categoryName         = recentsInfoModel.get_categoryName();
            paperTheme.packName             = recentsInfoModel.get_packName();
            paperTheme.themeBgClr           = recentsInfoModel.getThemeBgClr();
            paperTheme.themeBgClrName       = recentsInfoModel.getThemeBgClrName();
            paperTheme.horizontalLineColor  = recentsInfoModel.getHorizontalLineColor();
            paperTheme.verticalLineColor    = recentsInfoModel.getVerticalLineColor();
            paperTheme.horizontalSpacing    = recentsInfoModel.getHorizontalSpacing();
            paperTheme.verticalSpacing      = recentsInfoModel.getVerticalSpacing();
            paperTheme.width                = recentsInfoModel.getWidth();
            paperTheme.height               = recentsInfoModel.getHeight();
            paperTheme.themeName            = recentsInfoModel.get_themeName();
            paperTheme.isLandscape          = recentsInfoModel.isLandscape();
            paperTheme.thumbnailURLPath     = recentsInfoModel.get_thumbnailURLPath();
            paperTheme.bitmap               = FTTemplateUtil.getInstance().StringToBitMap(recentsInfoModel.get_themeBitmapInStringFrmt());
        }

        boolean _isThemeDeleted              = FTTemplateUtil.getInstance().isThemeDeleted(FTNThemeCategory.FTThemeType.PAPER,paperTheme);

        if (_isThemeDeleted) {
//            paperTheme                      = new FTNThemeCategory(getContext(), "Simple", FTNThemeCategory.FTThemeType.PAPER).getPaperThemeForPackName(FTConstants.DEFAULT_PAPER_THEME_NAME);
            paperTheme = FTNTheme.theme(FTNThemeCategory.getUrl(paperPackName));
            paperTheme.thumbnailURLPath     = FTConstants.TEMP_FOLDER_PATH+"TemplatesCache/"+FTConstants.DEFAULT_PAPER_THEME_URL;
            paperTheme.bitmap               = FTTemplateUtil.getBitmapFromAsset(FTNThemeCategory.FTThemeType.PAPER);
            FTTemplateUtil.getInstance().saveRecentPaperThemeFromQuickCreateDialog(paperTheme);
        }

        Log.d("TemplatePicker==>", "FTQuickCreateSettingsPopup onResume _isThemeDeleted " +_isThemeDeleted+
                " get_themeName::-"+paperTheme.themeName);

        mDefaultPaperTextView.setText(paperTheme.themeName);

        mRandomCoverDesignSwitch.setChecked(FTApp.getPref().get(SystemPref.RANDOM_COVER_DESIGN_ENABLED, false));

        /*Log.d("TemplatePicker==>","FTTemplateUtil FTNewNotebookDialog Before packName::-"+
                recentlySelectedPaperThemeFromNewNotebookOption.packName +
                " recentJson::-"+recentlySelectedPaperThemeFromNewNotebookOption.thumbnailURLPath+" deletedThemeInfo::-"+deletedThemeInfo);

        if (deletedThemeInfo != null) {
            Log.d("TemplatePicker==>","FTTemplateUtil FTNewNotebookDialog Inside packName::-"+
                    recentlySelectedPaperThemeFromNewNotebookOption.packName +
                    " recentJson::-"+recentlySelectedPaperThemeFromNewNotebookOption.thumbnailURLPath+" deletedThemeInfo::-"+deletedThemeInfo.thumbnailURLPath);
            if (deletedThemeInfo.thumbnailURLPath.equalsIgnoreCase(recentlySelectedPaperThemeFromNewNotebookOption.thumbnailURLPath)) {
                recentlySelectedPaperThemeFromNewNotebookOption = new FTNThemeCategory(FTApp.getInstance().getApplicationContext(),
                        "Plain", FTNThemeCategory.FTThemeType.PAPER).getPaperThemeForPackName(FTConstants.DEFAULT_PAPER_THEME_NAME);
                recentlySelectedPaperThemeFromNewNotebookOption.isBasicTheme   = true;
                recentlySelectedPaperThemeFromNewNotebookOption.isDefaultTheme = true;
                FTTemplateUtil.getInstance().saveDeletedThemeFromNewNotebookOption(recentlySelectedPaperThemeFromNewNotebookOption);

            }
        }

        Log.d("TemplatePicker==>","FTTemplateUtil FTQuickCreateSettingsPopup onResume packName::-"+
                recentlySelectedPaperThemeFromNewNotebookOption.packName +" recentJson::-"+recentlySelectedPaperThemeFromNewNotebookOption.thumbnailURLPath);
        mDefaultPaperTextView.setText(FTTemplatesInfoSingleton.getInstance().
                getNSPFileNameWithoutExtn(recentlySelectedPaperThemeFromNewNotebookOption));

        mRandomCoverDesignSwitch.setChecked(FTApp.getPref().get(SystemPref.RANDOM_COVER_DESIGN_ENABLED, false));*/

        /*FTNTheme recentlySelectedPaperThemeFromNewNotebookOption    = FTTemplateUtil.getInstance().getRecentlySelectedPaperThemeFromQuickCreateOption();
        FTNTheme deletedThemeInfo                                   = FTTemplateUtil.getInstance().getDeletedThemeFromNewNotebookOption();

        Log.d("TemplatePicker==>","FTTemplateUtil FTNewNotebookDialog Before packName::-"+
                recentlySelectedPaperThemeFromNewNotebookOption.packName +
                " recentJson::-"+recentlySelectedPaperThemeFromNewNotebookOption.thumbnailURLPath+" deletedThemeInfo::-"+deletedThemeInfo);

        if (deletedThemeInfo != null) {
            Log.d("TemplatePicker==>","FTTemplateUtil FTNewNotebookDialog Inside packName::-"+
                    recentlySelectedPaperThemeFromNewNotebookOption.packName +
                    " recentJson::-"+recentlySelectedPaperThemeFromNewNotebookOption.thumbnailURLPath+" deletedThemeInfo::-"+deletedThemeInfo.thumbnailURLPath);
            if (deletedThemeInfo.thumbnailURLPath.equalsIgnoreCase(recentlySelectedPaperThemeFromNewNotebookOption.thumbnailURLPath)) {
                recentlySelectedPaperThemeFromNewNotebookOption = new FTNThemeCategory(FTApp.getInstance().getApplicationContext(),
                        "Plain", FTNThemeCategory.FTThemeType.PAPER).getPaperThemeForPackName(FTConstants.DEFAULT_PAPER_THEME_NAME);
                recentlySelectedPaperThemeFromNewNotebookOption.isBasicTheme   = true;
                recentlySelectedPaperThemeFromNewNotebookOption.isDefaultTheme = true;
                FTTemplateUtil.getInstance().saveDeletedThemeFromNewNotebookOption(recentlySelectedPaperThemeFromNewNotebookOption);
                FTTemplateUtil.getInstance().saveRecentPaperThemeFromQuickCreateOption(recentlySelectedPaperThemeFromNewNotebookOption);
            }
        }

        Log.d("TemplatePicker==>","FTTemplateUtil FTQuickCreateSettingsPopup onResume packName::-"+
                recentlySelectedPaperThemeFromNewNotebookOption.packName +" recentJson::-"+recentlySelectedPaperThemeFromNewNotebookOption.thumbnailURLPath);
        mDefaultPaperTextView.setText(FTTemplatesInfoSingleton.getInstance().
                getNSPFileNameWithoutExtn(recentlySelectedPaperThemeFromNewNotebookOption));

        mRandomCoverDesignSwitch.setChecked(FTApp.getPref().get(SystemPref.RANDOM_COVER_DESIGN_ENABLED, false));*/
    }

}