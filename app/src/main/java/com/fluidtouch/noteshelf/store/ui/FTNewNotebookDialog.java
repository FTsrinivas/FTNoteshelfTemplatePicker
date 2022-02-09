package com.fluidtouch.noteshelf.store.ui;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf.commons.FTLog;
import com.fluidtouch.noteshelf.commons.ui.FTBaseDialog;
import com.fluidtouch.noteshelf.documentframework.Utilities.FTConstants;
import com.fluidtouch.noteshelf.models.theme.FTNCoverTheme;
import com.fluidtouch.noteshelf.models.theme.FTNPaperTheme;
import com.fluidtouch.noteshelf.models.theme.FTNTheme;
import com.fluidtouch.noteshelf.models.theme.FTNThemeCategory;
import com.fluidtouch.noteshelf.preferences.SystemPref;
import com.fluidtouch.noteshelf.shelf.activities.FTBaseShelfActivity;
import com.fluidtouch.noteshelf.templatepicker.FTChoosePaperTemplate;
import com.fluidtouch.noteshelf.templatepicker.FTTemplateMode;
import com.fluidtouch.noteshelf.templatepicker.common.FTTemplatesInfoSingleton;
import com.fluidtouch.noteshelf.templatepicker.common.util.FTTemplateUtil;
import com.fluidtouch.noteshelf.templatepicker.common.plistdatamodel.FTSelectedDeviceInfo;
import com.fluidtouch.noteshelf.services.FTFirebaseAnalytics;
import com.fluidtouch.noteshelf.templatepicker.models.FTTemplatepickerInputInfo;
import com.fluidtouch.noteshelf.templatepicker.models.RecentsInfoModel;
import com.fluidtouch.noteshelf2.R;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FTNewNotebookDialog extends FTBaseDialog implements
        FTChooseCoverPaperDialog.CoverChooseListener,
        FTChoosePaperTemplate.TemplatePaperChooseListener{
    @BindView(R.id.new_notebook_cover_image_view)
    ImageView coverImageView;
    @BindView(R.id.new_notebook_cover_title_text_view)
    TextView coverTitleTextView;
    @BindView(R.id.new_notebook_paper_image_view)
    ImageView paperImageView;
    @BindView(R.id.new_notebook_paper_title_text_view)
    TextView paperTitleTextView;
    @BindView(R.id.new_notebook_dialog_edit_text)
    EditText notebookTitleEditText;

    //String cachePath        = FTConstants.TEMP_FOLDER_PATH+"TemplatesCache/";

    private NoteCreationListener listener;
    //private FTNCoverTheme coverTheme;
    private FTNTheme coverTheme;
    private FTNTheme paperTheme;
    private FTNThemeCategory.FTThemeType selectedTheme = FTNThemeCategory.FTThemeType.PAPER;
    FTSelectedDeviceInfo ftSelectedDeviceInfo;
    FTTemplateUtil ftTemplateUtil;
    FTNTheme ftnTheme;
    Dialog dialog ;

    FTBaseShelfActivity _baseShelfActivity;

    public static FTNewNotebookDialog newInstance(FTBaseShelfActivity _baseShelfActivity) {
        FTNewNotebookDialog fragment = new FTNewNotebookDialog();
        fragment._baseShelfActivity = _baseShelfActivity;
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        listener = (NoteCreationListener) getActivity();
       // ftTemplateUtil = FTTemplateUtil.getInstance();
       // ftSelectedDeviceInfo = ftTemplateUtil.getFtSelectedDeviceInfo();
        //Log.d("TemplatePicker==>", "ftSelectedDeviceInfo::-" + ftSelectedDeviceInfo);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        dialog = super.onCreateDialog(savedInstanceState);
        if (!isMobile()) {
            Window window = dialog.getWindow();
            if (window != null) {
                window.setDimAmount(0.1f);
                window.setBackgroundDrawableResource(R.drawable.window_bg);
            }
        }
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_new_notebook, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d("TemplatePicker==>", "FTNewNotebookDialog onStart paperTheme::-");



    }

    @Override
    public void onResume() {
        //super.onResume();
        Log.d("TemplatePicker==>", "FTNewNotebookDialog onResume ::-");
        super.onResume();

        RecentsInfoModel coversRecentsInfoModel =  FTTemplateUtil.getInstance().getRecentCoverTheme();
        String coverPackName = FTApp.getPref().get(SystemPref.RECENT_COVER_THEME_NAME, FTConstants.DEFAULT_COVER_THEME_NAME);
        String defaultCoverPackName = coverPackName;
        if (coversRecentsInfoModel != null) {
            coverPackName = coversRecentsInfoModel.get_packName();
        }

        if (coverPackName.endsWith(".nsc")) {
            coverTheme = FTNTheme.theme(FTNThemeCategory.getUrl(coverPackName));
        }

        Log.d("TemplatePicker==>", "FTNewNotebookDialog getRecentCoverTheme get_packName recentsInfoModel ::-"
                +coversRecentsInfoModel +""+coverTheme.themeName);

        if (coversRecentsInfoModel == null) {

            Log.d("TemplatePicker==>", "FTNewNotebookDialog Paper is null paperTheme ::-"+coverTheme);

            if (coverTheme == null || coverTheme.themeThumbnail(getContext()) == null) {
                coverTheme  = FTNTheme.theme(FTNThemeCategory.getUrl(defaultCoverPackName));
                Log.d("TemplatePicker==>", "FTNewNotebookDialog Paper is null paperTheme.bitmap ::-"+coverTheme.bitmap);
            }

            coverTheme.bitmap   = FTTemplateUtil.getBitmapFromAsset(FTNThemeCategory.FTThemeType.COVER);

            /*coverTitleTextView.setText(coverTheme.themeName);
            coverImageView.setImageBitmap(coverTheme.themeThumbnail(getContext()));
            coverImageView.setLayoutParams(getLayoutParams(coverImageView.getLayoutParams(), coverTheme));*/

        } else {

            boolean _isThemeDeleted             = FTTemplateUtil.getInstance().isThemeDeleted(FTNThemeCategory.FTThemeType.COVER,coverTheme);
            Log.d("TemplatePicker==>", "FTNewNotebookDialog onResume Covers _isThemeDeleted ::-"+_isThemeDeleted +" defaultCoverPackName:: "+defaultCoverPackName +" coverPackName:: "+coverPackName);

            if (_isThemeDeleted) {
                //coverTheme                    = new FTNThemeCategory(getContext(), "Simple", FTNThemeCategory.FTThemeType.COVER).getCoverThemeForPackName(FTConstants.DEFAULT_COVER_THEME_NAME);
                coverTheme                      = FTNTheme.theme(FTNThemeCategory.getUrl(defaultCoverPackName));
                coverTheme.thumbnailURLPath     = FTConstants.TEMP_FOLDER_PATH+"TemplatesCache/"+FTConstants.DEFAULT_COVER_THEME_URL;
                coverTheme.bitmap               = FTTemplateUtil.getBitmapFromAsset(FTNThemeCategory.FTThemeType.COVER);
            } else {
                coverTheme.categoryName         = coversRecentsInfoModel.get_categoryName();
                coverTheme.packName             = coversRecentsInfoModel.get_packName();
                coverTheme.themeBgClr           = coversRecentsInfoModel.getThemeBgClr();
                coverTheme.themeBgClrName       = coversRecentsInfoModel.getThemeBgClrName();
                coverTheme.horizontalLineColor  = coversRecentsInfoModel.getHorizontalLineColor();
                coverTheme.verticalLineColor    = coversRecentsInfoModel.getVerticalLineColor();
                coverTheme.horizontalSpacing    = coversRecentsInfoModel.getHorizontalSpacing();
                coverTheme.verticalSpacing      = coversRecentsInfoModel.getVerticalSpacing();
                coverTheme.width                = coversRecentsInfoModel.getWidth();
                coverTheme.height               = coversRecentsInfoModel.getHeight();
                coverTheme.themeName            = coversRecentsInfoModel.get_themeName();
                coverTheme.isLandscape          = coversRecentsInfoModel.isLandscape();
                coverTheme.thumbnailURLPath     = coversRecentsInfoModel.get_thumbnailURLPath();
                coverTheme.bitmap               = FTTemplateUtil.getInstance().StringToBitMap(coversRecentsInfoModel.get_themeBitmapInStringFrmt());
            }

            Log.d("TemplatePicker==>", "FTNewNotebookDialog getRecentPaperThemeFromNewNotebookDialog _isThemeDeleted " +_isThemeDeleted+
                    " get_themeName::-"+coversRecentsInfoModel.get_themeName()+" get_themeBitmap:: "+FTTemplateUtil.getInstance().StringToBitMap(coversRecentsInfoModel.get_themeBitmapInStringFrmt()));

        }

        RecentsInfoModel recentsInfoModel =  FTTemplateUtil.getInstance().getRecentPaperThemeFromNewNotebookDialog();
        String paperPackName = FTApp.getPref().get(SystemPref.RECENT_PAPER_THEME_NAME, FTConstants.DEFAULT_PAPER_THEME_NAME);
        String defaultPaperPackName = paperPackName;
        if (recentsInfoModel != null) {
            paperPackName = recentsInfoModel.get_packName();
        }

        if (paperPackName.endsWith(".nsp")) {
            paperTheme = FTNTheme.theme(FTNThemeCategory.getUrl(paperPackName));
        }

        Log.d("TemplatePicker==>", "FTNewNotebookDialog getRecentPaperThemeFromNewNotebookDialog get_packName recentsInfoModel ::-"+recentsInfoModel);

        if (recentsInfoModel == null) {

            Log.d("TemplatePicker==>", "FTNewNotebookDialog Paper is null paperTheme ::-"+paperTheme);

            if (paperTheme == null || paperTheme.themeThumbnail(getContext()) == null) {
//                paperTheme          = new FTNThemeCategory(getContext(), "Basic", FTNThemeCategory.FTThemeType.PAPER).getPaperThemeForPackName(FTConstants.DEFAULT_PAPER_THEME_NAME);
                paperTheme          = FTNTheme.theme(FTNThemeCategory.getUrl(defaultPaperPackName));
                Log.d("TemplatePicker==>", "FTNewNotebookDialog Paper is null paperTheme.bitmap ::-"+paperTheme.bitmap);
            }

            paperTheme.bitmap   = FTTemplateUtil.getBitmapFromAsset(FTNThemeCategory.FTThemeType.PAPER);

            /*paperTitleTextView.setText(paperTheme.themeName);
            paperImageView.setImageBitmap(paperTheme.themeThumbnail(getContext()));
            paperImageView.setLayoutParams(getLayoutParams(paperImageView.getLayoutParams(), paperTheme));*/

        } else {

            boolean _isThemeDeleted             = FTTemplateUtil.getInstance().isThemeDeleted(FTNThemeCategory.FTThemeType.PAPER,paperTheme);
            Log.d("TemplatePicker==>", "FTNewNotebookDialog onResume Papers _isThemeDeleted ::-"+_isThemeDeleted +" defaultPaperPackName:: "+defaultPaperPackName +" paperPackName:: "+paperPackName);

            if (_isThemeDeleted) {
                //paperTheme                    = new FTNThemeCategory(getContext(), "Basic", FTNThemeCategory.FTThemeType.PAPER).getPaperThemeForPackName(FTConstants.DEFAULT_PAPER_THEME_NAME);
                paperTheme                      = FTNTheme.theme(FTNThemeCategory.getUrl(defaultPaperPackName));
                paperTheme.thumbnailURLPath     = FTConstants.TEMP_FOLDER_PATH+"TemplatesCache/"+FTConstants.DEFAULT_PAPER_THEME_URL;
                paperTheme.bitmap               = FTTemplateUtil.getBitmapFromAsset(FTNThemeCategory.FTThemeType.PAPER);
            } else {
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

            Log.d("TemplatePicker==>", "FTNewNotebookDialog getRecentPaperThemeFromNewNotebookDialog _isThemeDeleted " +_isThemeDeleted+
                    " get_themeName::-"+recentsInfoModel.get_themeName()+" get_themeBitmap:: "+FTTemplateUtil.getInstance().StringToBitMap(recentsInfoModel.get_themeBitmapInStringFrmt()));

        }

        coverTitleTextView.setText(coverTheme.themeName);
        coverImageView.setImageBitmap(coverTheme.bitmap);
        coverImageView.setLayoutParams(getLayoutParams(coverImageView.getLayoutParams(), coverTheme));

        paperTitleTextView.setText(paperTheme.themeName);
        paperImageView.setImageBitmap(paperTheme.bitmap);
        paperImageView.setLayoutParams(getLayoutParams(paperImageView.getLayoutParams(), paperTheme));

    }

    public boolean isThemeDeleted(FTNThemeCategory.FTThemeType _ftThemeType,FTNTheme ftnTheme) {
        File _file = new File(ftnTheme.themeFileURL.getPath());
        if (_file.exists()) {
            return  false;
        } else {
            return true;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("TemplatePicker==>", "FTNewNotebookDialog onDestroy paperTheme::-");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d("TemplatePicker==>", "FTNewNotebookDialog onDestroyView paperTheme::-");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d("TemplatePicker==>", "FTNewNotebookDialog onPause paperTheme::-");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d("TemplatePicker==>", "FTNewNotebookDialog onStop paperTheme::-");
    }

    @OnClick({R.id.new_notebook_cover_choose_text_view, R.id.new_notebook_cover_image_view})
    void onChooseCoverClicked() {
        FTFirebaseAnalytics.logEvent("Shelf_AddNew_NewNB_ChooseCover");
        selectedTheme = FTNThemeCategory.FTThemeType.COVER;
        //FTChoosePaperTemplate.newInstance(FTNThemeCategory.FTThemeType.COVER).show(getChildFragmentManager(), FTChooseCoverPaperDialog.class.getName());

        FTTemplatepickerInputInfo _ftTemplatepickerInputInfo = new FTTemplatepickerInputInfo();
        _ftTemplatepickerInputInfo.set_baseShelfActivity(null);
        _ftTemplatepickerInputInfo.set_ftTemplateOpenMode(null);
        _ftTemplatepickerInputInfo.set_ftThemeType(FTNThemeCategory.FTThemeType.COVER);
        _ftTemplatepickerInputInfo.set_notebookTitle(null);
        FTChoosePaperTemplate.newInstance1(_ftTemplatepickerInputInfo).show(getChildFragmentManager(), FTChooseCoverPaperDialog.class.getName());

    }

    @OnClick({R.id.new_notebook_paper_choose_text_view, R.id.new_notebook_paper_image_view})
    void onChoosePaperClicked() {

        FTTemplatepickerInputInfo _ftTemplatepickerInputInfo = new FTTemplatepickerInputInfo();
        _ftTemplatepickerInputInfo.set_baseShelfActivity(_baseShelfActivity);
        _ftTemplatepickerInputInfo.set_ftNewNotebookDialog(this);
        _ftTemplatepickerInputInfo.set_ftTemplateOpenMode(FTTemplateMode.NewNotebook);
        _ftTemplatepickerInputInfo.set_ftThemeType(FTNThemeCategory.FTThemeType.PAPER);
        _ftTemplatepickerInputInfo.set_notebookTitle("Sample");

        FTChoosePaperTemplate.newInstance1(_ftTemplatepickerInputInfo).show(getChildFragmentManager(), FTChoosePaperTemplate.class.getName());
        FTFirebaseAnalytics.logEvent("Shelf_AddNew_NewNB_ChoosePaper");

        //FTTemplatesInfoSingleton.getInstance().getTemplatesInfo(FTNThemeCategory.FTThemeType.PAPER);
        /*selectedTheme = FTNThemeCategory.FTThemeType.PAPER;
        FTChooseCoverPaperDialog.newInstance(selectedTheme).show(getChildFragmentManager(), FTChooseCoverPaperDialog.class.getName());*/
    }

    @OnClick(R.id.new_note_book_create_text_view)
    void onCreateClicked() {
        FTFirebaseAnalytics.logEvent("Shelf_AddNew_NewNB_CreateNB");
        FTLog.crashlyticsLog("UI: Clicked Create notebook");
        Log.d("TemplatePicker==>", "FTNewNotebookDialog onCreateClicked paperTheme::-" + paperTheme + " paperTheme::-" +
                paperTheme.themeName + " themeBgClr::-" + paperTheme.themeBgClr + " thumbnailURLPath::-" + paperTheme.thumbnailURLPath
         +"\n Width::- "+paperTheme.width+"\t height::- "+paperTheme.height);
        String name = notebookTitleEditText.getText().toString();
        if (TextUtils.isEmpty(name)) {
            FTTemplateUtil.getInstance().saveRecentPaperThemeFromNewNotebookDialog(paperTheme);
            if (coverTheme.isCustomTheme) {
                if (coverTheme.isSavedForFuture) {
                    FTTemplateUtil.getInstance().saveRecentCoverTheme(coverTheme);
                }
            } else {
                FTTemplateUtil.getInstance().saveRecentCoverTheme(coverTheme);
            }
            listener.createNewShelfItem(notebookTitleEditText.getHint().toString(), coverTheme, paperTheme, "new_note_book_create_text_view");
        } else {
            name = name.trim();
            if (TextUtils.isEmpty(name)) {
                name = notebookTitleEditText.getHint().toString();
            }
            FTTemplateUtil.getInstance().saveRecentPaperThemeFromNewNotebookDialog(paperTheme);
            if (coverTheme.isCustomTheme) {
                if (coverTheme.isSavedForFuture) {
                    FTTemplateUtil.getInstance().saveRecentCoverTheme(coverTheme);
                }
            } else {
                FTTemplateUtil.getInstance().saveRecentCoverTheme(coverTheme);
            }
            listener.createNewShelfItem(name, coverTheme, paperTheme, "new_note_book_create_text_view");
        }
        dismiss();
    }

    @OnClick(R.id.new_notebook_close_image_view)
    void onCloseClicked() {
        dismiss();
    }

    @Override
    public void onThemeChosen(FTNTheme theme, boolean isCurrentPage, boolean isLandscapeStatus) {

        Bitmap bitmap = null;
        Log.d("TemplatePicker==>", "FTNewNotebookDialog onThemeChosen theme::-" + theme + " theme_isLandscapeStatus::-" + (theme instanceof FTNPaperTheme));
        if (theme instanceof FTNPaperTheme) {
            Log.d("TemplatePickerV2", "TemplatePickerV2 ThemeClicked FTNewNotebookDialog onThemeChosen thumbnailURLPath2::-" + theme.thumbnailURLPath +
                    " themeName::-" + theme.themeName + " isLandscape::-" + theme.isLandscape() + " themeBgClr::-" + theme.themeBgClr);

            paperTheme = theme;
            paperTitleTextView.setText(paperTheme.themeName);
            //paperImageView.setImageBitmap(theme.themeThumbnail(getContext()));
            paperImageView.setImageBitmap(theme.bitmap);
            paperImageView.setLayoutParams(getLayoutParams(paperImageView.getLayoutParams(), theme));

            FTLog.crashlyticsLog("UI: Paper chosen for notebook");
            Log.d("TemplatePicker==>", "Mani onThemeChosen FTNewNotebookDialog paperTheme.themeName::-" + paperTheme.themeName);
        } else {
            coverTheme = theme;
            coverTitleTextView.setText(FTTemplatesInfoSingleton.getInstance().getNSPFileNameWithoutExtn(coverTheme));
            coverImageView.setLayoutParams(getLayoutParams(coverImageView.getLayoutParams(), coverTheme));
            //coverImageView.setImageBitmap(coverTheme.themeThumbnail(getContext()));
            coverImageView.setImageBitmap(coverTheme.bitmap);

            FTLog.crashlyticsLog("UI: Cover chosen for notebook");
        }
    }

    public static float dipToPixels(Context context, float dipValue) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, metrics);
    }

    private ViewGroup.LayoutParams getLayoutParams(ViewGroup.LayoutParams
                                                           layoutParams, FTNTheme ftnTheme) {

        if (ftnTheme.ftThemeType == FTNThemeCategory.FTThemeType.COVER) {
            layoutParams.height = getResources().getDimensionPixelOffset(R.dimen.new_170dp);
        } else {
            if (ftnTheme.isLandscape) {
                layoutParams.height = getResources().getDimensionPixelOffset(R.dimen.new_110dp);
            } else {
                layoutParams.height = getResources().getDimensionPixelOffset(R.dimen.new_170dp);
            }
        }
        layoutParams.width = getResources().getDimensionPixelOffset(R.dimen.new_137dp);
        return layoutParams;
    }

    @Override
    public void addCustomTheme(FTNTheme theme) {
        listener.addCustomTheme(theme);
    }

    @Override
    public void onClose() {
        onResume();
    }

    @Override
    public boolean isCurrentTheme() {
        return false;
    }

    public interface NoteCreationListener {
        void createNewShelfItem(String name, FTNTheme coverTheme, FTNTheme paperTheme, String origin);

        void addCustomTheme(FTNTheme theme);
    }
}