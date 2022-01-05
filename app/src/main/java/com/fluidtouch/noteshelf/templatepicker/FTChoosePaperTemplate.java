package com.fluidtouch.noteshelf.templatepicker;

import static com.fluidtouch.noteshelf.documentframework.Utilities.FTConstants.KEY_IS_NOT_SAVED_FUTURE;
import static com.fluidtouch.noteshelf.documentframework.Utilities.FTConstants.KEY_OBJECT_THEME;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf.commons.ui.FTBaseDialog;
import com.fluidtouch.noteshelf.commons.utils.ObservingService;
import com.fluidtouch.noteshelf.commons.utils.ScreenUtil;
import com.fluidtouch.noteshelf.commons.utils.StringUtil;
import com.fluidtouch.noteshelf.document.FTDocumentActivity;
import com.fluidtouch.noteshelf.documentframework.FTUrl;
import com.fluidtouch.noteshelf.documentframework.Utilities.FTConstants;
import com.fluidtouch.noteshelf.models.theme.FTNTheme;
import com.fluidtouch.noteshelf.models.theme.FTNThemeCategory;
import com.fluidtouch.noteshelf.preferences.SystemPref;
import com.fluidtouch.noteshelf.shelf.activities.FTBaseShelfActivity;
import com.fluidtouch.noteshelf.shelf.fragments.FTQuickCreateSettingsPopup;
import com.fluidtouch.noteshelf.store.ui.FTNewNotebookDialog;
import com.fluidtouch.noteshelf.store.ui.FTStoreActivity;
import com.fluidtouch.noteshelf.templatepicker.adapters.FTTemplateCategoriesAdapter;
import com.fluidtouch.noteshelf.templatepicker.common.FTTemplateCategoryInfo;
import com.fluidtouch.noteshelf.templatepicker.common.FTTemplatesInfoSingleton;
import com.fluidtouch.noteshelf.templatepicker.common.plistdatamodel.FTCategories;
import com.fluidtouch.noteshelf.templatepicker.common.plistdatamodel.FTSelectedDeviceInfo;
import com.fluidtouch.noteshelf.templatepicker.common.supporteddevicesplistdatamodel.ItemModel;
import com.fluidtouch.noteshelf.templatepicker.common.util.FTTemplateUtil;
import com.fluidtouch.noteshelf.templatepicker.interfaces.TemplateCategorySelection;
import com.fluidtouch.noteshelf.templatepicker.models.FTTemplatepickerInputInfo;
import com.fluidtouch.noteshelf.templatepicker.models.TemplatesInfoModel;
import com.fluidtouch.noteshelf2.R;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Observable;
import java.util.Observer;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FTChoosePaperTemplate extends FTBaseDialog
        implements TemplateCategorySelection,
        FTTemplateCategories.TemplateSelctedInfo,
        FTTemplateTabsDialog.DeviceSelectionListener, TextWatcher {

    @BindView(R.id.searchiCon)
    ImageButton searchiCon;

    @BindView(R.id.dialog_close_button)
    ImageView templateCloseButton;

    @BindView(R.id.club_downlaods_btn)
    ImageView clubDownlaodsBtn;

    @BindView(R.id.clear_search_btn)
    ImageView clearSearchBackButton;

    @BindView(R.id.tabsLayout)
    RelativeLayout tabsLayout;

    @BindView(R.id.searchTemplatesLayout)
    LinearLayout searchTemplatesLayout;

    @BindView(R.id.searchTextLyt)
    EditText searchTextLyt;

    /*@BindView(R.id.SVsearchTextLyt)
    SearchView SVsearchTextLyt;*/

    @BindView(R.id.template_orientation)
    TextView templateOrientation;

    @BindView(R.id.device_model_spinner_item)
    TextView deviceModelSpinnerItem;

    @BindView(R.id.paperSizeSelectionLyt)
    RelativeLayout paperSizeSelectionLyt;

    @BindView(R.id.coverTitleLyt)
    RelativeLayout coverTitleLyt;

    @BindView(R.id.coverTitleID)
    TextView coverTitleID;

    @BindView(R.id.portrait_tab)
    TextView portrait_tab;

    @BindView(R.id.landscape_tab)
    TextView landscape_tab;

    @BindView(R.id.portrait_tab1)
    TextView portrait_tab1;

    @BindView(R.id.landscape_tab1)
    TextView landscape_tab1;

    @BindView(R.id.categoriesLyt)
    FrameLayout categoriesLyt;

    @BindView(R.id.orientation_tab_layout2)
    RelativeLayout orientation_tab_layout2;

    @BindView(R.id.orientation_tab_layout1)
    RelativeLayout orientation_tab_layout1;

    @BindView(R.id.chooseTemplateOrientationLyt)
    RelativeLayout chooseTemplateOrientationLyt;

    private String[] splitString;
    private boolean isCurrentPage;
    private String dimension = null;
    private String headerTitle = null;

    private Dialog dialog = null;
    private ItemModel itemModel = null;

    @BindView(R.id.categoryItems)
    RecyclerView categoryItems;

    private LinearLayoutManager categoryLayoutManager;

    //private FTTemplateUtil ftTemplateUtil;
    private TemplatePaperChooseListener listener;
    private CallBackForNewNotebookDialogListener callBackForNewNotebookDialogListener;
    private FTTemplateTabsDialog ftTemplateTabsDialog;
    private FTTemplateCategories mFTTemplateCategories;
    private FTTemplatesInfoSingleton mFTTemplatesInfoSingleton;
    private FTTemplateCategoriesAdapter mTemplateCategoriesAdapter;

    private ArrayList<String> mCategoriesArrayList;
    //private ArrayList<FTDevicesDetailedInfo> ftDeviceDataInfo1List;
    private ArrayList<FTTemplateCategoryInfo> ftTemplateCategoryInfoArrayList;
    private ArrayList<FTCategories> ftCategoriesArrayList = new ArrayList<>();
    ArrayList<FTNTheme> ftnThemeArrayList = new ArrayList<>();
    private FTNThemeCategory.FTThemeType themeType = FTNThemeCategory.FTThemeType.PAPER;
    boolean searchBarEnabled;
    String themeTypeInString;
    String deviceSize;
    String prevSelectedDeviceType;

    FTSelectedDeviceInfo ftSelectedDeviceInfo;
    ArrayList<TemplatesInfoModel> templatesInfoList;

    FTBaseShelfActivity _baseShelfActivity;
    FTNewNotebookDialog ftNewNotebookDialog;
    FTTemplateMode ftTemplateMode;
    private Observer addDownloadsThemeObserver = new Observer() {
        @Override
        public void update(Observable o, Object arg) {
             if (arg != null) {
               templatesInfoList =  FTTemplatesInfoSingleton.getInstance().getTemplatesInfo(themeType);

                 if (themeType == FTNThemeCategory.FTThemeType.COVER) {
                     for (int i=0;i<templatesInfoList.size();i++) {
                         for (int j=0;j<templatesInfoList.get(i).get_themeseList().size();j++) {
                             FTNTheme _ftnTheme = templatesInfoList.get(i).get_themeseList().get(j);
                             if (!_ftnTheme.getCategoryName().contains("Recent")) {
                                 _ftnTheme.thumbnailURLPath      = FTTemplateUtil.getInstance().generateThumbnailURLPath(_ftnTheme.themeFileURL,_ftnTheme);
                                 Log.d("TemplatePickerV2", "TemplatePickerV2 addDownloadsThemeObserver thumbnailURLPath tabSelection thumbnailURLPath:: "
                                         + _ftnTheme.thumbnailURLPath +" _ftnTheme.isLandscape:: "+_ftnTheme.isLandscape);
                             }
                         }
                     }

                     mFTTemplateCategories.updateUI(templatesInfoList, "addDownloadsThemeObserver");
                 } else {
                     FTSelectedDeviceInfo _ftSelectedDeviceInfo = FTSelectedDeviceInfo.selectedDeviceInfo();
                     for (int i=0;i<templatesInfoList.size();i++) {
                         for (int j=0;j<templatesInfoList.get(i).get_themeseList().size();j++) {
                             FTNTheme _ftnTheme = templatesInfoList.get(i).get_themeseList().get(j);
                             if (!_ftnTheme.getCategoryName().contains("Recent")) {
                                 if (_ftnTheme.getCategoryName().equalsIgnoreCase("basic")) {
                                     _ftnTheme.themeBgClr            = _ftSelectedDeviceInfo.getThemeBgClrHexCode();
                                     _ftnTheme.themeBgClrName        = _ftSelectedDeviceInfo.getThemeBgClrName();
                                     _ftnTheme.horizontalLineColor   = _ftSelectedDeviceInfo.getHorizontalLineClr();
                                     _ftnTheme.verticalLineColor     = _ftSelectedDeviceInfo.getVerticalLineClr();
                                     _ftnTheme.horizontalSpacing     = _ftSelectedDeviceInfo.getHorizontalLineSpacing();
                                     _ftnTheme.verticalSpacing       = _ftSelectedDeviceInfo.getVerticalLineSpacing();
                                 /*_ftnTheme.width                 = _ftSelectedDeviceInfo.getPageWidth();
                                 _ftnTheme.height                = _ftSelectedDeviceInfo.getPageHeight();*/
                                 }

                                 _ftnTheme.thumbnailURLPath      = FTTemplateUtil.getInstance().generateThumbnailURLPath(_ftnTheme.themeFileURL,_ftnTheme);
                                 Log.d("TemplatePickerV2", "TemplatePickerV2 addDownloadsThemeObserver thumbnailURLPath tabSelection thumbnailURLPath:: "
                                         + _ftnTheme.thumbnailURLPath +" _ftnTheme.isLandscape:: "+_ftnTheme.isLandscape);
                             }
                             Log.d("::TemplatePickerV2","FTTemplateCategories addDownloadsThemeObserver tabSelection thumbnailURLPath " +
                                     " thumbnailURLPath:: "+_ftnTheme.thumbnailURLPath+" getLayoutType:: "+_ftSelectedDeviceInfo.getLayoutType()+
                                     " getPageWidth:: "+_ftSelectedDeviceInfo.getPageWidth()+
                                     " getPageHeight():: "+_ftSelectedDeviceInfo.getPageHeight());
                         }
                     }

                     mFTTemplateCategories.updateUI(templatesInfoList, "addDownloadsThemeObserver");
                 }

             }
        }
    };

    private Observer addCustomThemeObserver = new Observer() {
        @Override
        public void update(Observable o, Object arg) {
            if (arg != null) {
                templatesInfoList =  FTTemplatesInfoSingleton.getInstance().getTemplatesInfo(themeType);

                if (themeType == FTNThemeCategory.FTThemeType.COVER) {
                    for (int i=0;i<templatesInfoList.size();i++) {
                        for (int j=0;j<templatesInfoList.get(i).get_themeseList().size();j++) {
                            FTNTheme _ftnTheme = templatesInfoList.get(i).get_themeseList().get(j);
                            if (!_ftnTheme.getCategoryName().contains("Recent")) {
                                _ftnTheme.thumbnailURLPath      = FTTemplateUtil.getInstance().generateThumbnailURLPath(_ftnTheme.themeFileURL,_ftnTheme);
                                Log.d("TemplatePickerV2", "TemplatePickerV2 addDownloadsThemeObserver thumbnailURLPath tabSelection thumbnailURLPath:: "
                                        + _ftnTheme.thumbnailURLPath +" _ftnTheme.isLandscape:: "+_ftnTheme.isLandscape);
                            }
                        }
                    }

                    mFTTemplateCategories.updateUI(templatesInfoList, "addDownloadsThemeObserver");
                } else {
                    FTSelectedDeviceInfo _ftSelectedDeviceInfo = FTSelectedDeviceInfo.selectedDeviceInfo();
                    for (int i=0;i<templatesInfoList.size();i++) {
                        for (int j=0;j<templatesInfoList.get(i).get_themeseList().size();j++) {
                            FTNTheme _ftnTheme = templatesInfoList.get(i).get_themeseList().get(j);
                            if (!_ftnTheme.getCategoryName().contains("Recent")) {
                                if (_ftnTheme.getCategoryName().equalsIgnoreCase("basic")) {
                                    _ftnTheme.themeBgClr            = _ftSelectedDeviceInfo.getThemeBgClrHexCode();
                                    _ftnTheme.themeBgClrName        = _ftSelectedDeviceInfo.getThemeBgClrName();
                                    _ftnTheme.horizontalLineColor   = _ftSelectedDeviceInfo.getHorizontalLineClr();
                                    _ftnTheme.verticalLineColor     = _ftSelectedDeviceInfo.getVerticalLineClr();
                                    _ftnTheme.horizontalSpacing     = _ftSelectedDeviceInfo.getHorizontalLineSpacing();
                                    _ftnTheme.verticalSpacing       = _ftSelectedDeviceInfo.getVerticalLineSpacing();
                                 /*_ftnTheme.width                 = _ftSelectedDeviceInfo.getPageWidth();
                                 _ftnTheme.height                = _ftSelectedDeviceInfo.getPageHeight();*/
                                }

                                _ftnTheme.thumbnailURLPath      = FTTemplateUtil.getInstance().generateThumbnailURLPath(_ftnTheme.themeFileURL,_ftnTheme);
                                Log.d("TemplatePickerV2", "TemplatePickerV2 addDownloadsThemeObserver thumbnailURLPath tabSelection thumbnailURLPath:: "
                                        + _ftnTheme.thumbnailURLPath +" _ftnTheme.isLandscape:: "+_ftnTheme.isLandscape);
                            }
                            Log.d("::TemplatePickerV2","FTTemplateCategories addDownloadsThemeObserver tabSelection thumbnailURLPath " +
                                    " thumbnailURLPath:: "+_ftnTheme.thumbnailURLPath+" getLayoutType:: "+_ftSelectedDeviceInfo.getLayoutType()+
                                    " getPageWidth:: "+_ftSelectedDeviceInfo.getPageWidth()+
                                    " getPageHeight():: "+_ftSelectedDeviceInfo.getPageHeight());
                        }
                    }
                    mFTTemplateCategories.updateUI(templatesInfoList, "addDownloadsThemeObserver");
                }

                /*templatesInfoList =  FTTemplatesInfoSingleton.getInstance().getTemplatesInfo(themeType);
                mFTTemplateCategories.updateUI(templatesInfoList, "addDownloadsThemeObserver");
                FTSelectedDeviceInfo selectedDeviceInfo = FTSelectedDeviceInfo.selectedDeviceInfo();

                for (int i=0;i<templatesInfoList.size();i++) {
                    for (int j=0;j<templatesInfoList.get(i).get_themeseList().size();j++) {
                        FTNTheme _ftnTheme = templatesInfoList.get(i).get_themeseList().get(j);
                        if (!_ftnTheme.getCategoryName().contains("Recent")) {
                            if (_ftnTheme.getCategoryName().equalsIgnoreCase("basic")) {
                                _ftnTheme.themeBgClr            = selectedDeviceInfo.getThemeBgClrHexCode();
                                _ftnTheme.themeBgClrName        = selectedDeviceInfo.getThemeBgClrName();
                                _ftnTheme.horizontalLineColor   = selectedDeviceInfo.getHorizontalLineClr();
                                _ftnTheme.verticalLineColor     = selectedDeviceInfo.getVerticalLineClr();
                                _ftnTheme.horizontalSpacing     = selectedDeviceInfo.getHorizontalLineSpacing();
                                _ftnTheme.verticalSpacing       = selectedDeviceInfo.getVerticalLineSpacing();
                            }

                            _ftnTheme.thumbnailURLPath      = FTTemplateUtil.getInstance().generateThumbnailURLPath(_ftnTheme.themeFileURL,_ftnTheme);
                            Log.d("TemplatePickerV2", "TemplatePickerV2 addDownloadsThemeObserver thumbnailURLPath templateBgColourChangedListener thumbnailURLPath::-"
                                    + _ftnTheme.thumbnailURLPath);
                        }
                        Log.d("::TemplatePickerV2","FTTemplateCategories addDownloadsThemeObserver generateThumbnailURLPath thumbnailURLPath " +
                                " thumbnailURLPath:: "+_ftnTheme.thumbnailURLPath+" getLayoutType:: "+selectedDeviceInfo.getLayoutType()+
                                " getPageWidth:: "+selectedDeviceInfo.getPageWidth()+
                                " getPageHeight():: "+selectedDeviceInfo.getPageHeight());
                    }
                }*/
            }
        }
    };

    public static FTChoosePaperTemplate newInstance(FTNThemeCategory.FTThemeType themeType) {
        FTChoosePaperTemplate choosePaperDialog = new FTChoosePaperTemplate();
        choosePaperDialog.themeType = themeType;
        FTApp.getPref().save(SystemPref.SEARCH_QUERY, "");
        FTApp.getPref().save(SystemPref.SEARCH_ENABLED, false);
        if (themeType == FTNThemeCategory.FTThemeType.PAPER) {
            choosePaperDialog.themeTypeInString = "PAPER";
        } else {
            choosePaperDialog.themeTypeInString = "COVER";
        }
        FTApp.getPref().save(SystemPref.THEME_TYPE, choosePaperDialog.themeTypeInString);
        return choosePaperDialog;
    }

    public static FTChoosePaperTemplate newInstance1(FTTemplatepickerInputInfo _fTTemplatepickerInputInfo) {
        FTChoosePaperTemplate choosePaperDialog = new FTChoosePaperTemplate();
        choosePaperDialog.themeType = _fTTemplatepickerInputInfo.get_ftThemeType();
        FTApp.getPref().save(SystemPref.SEARCH_QUERY, "");
        FTApp.getPref().save(SystemPref.SEARCH_ENABLED, false);
        if (_fTTemplatepickerInputInfo.get_ftThemeType() == FTNThemeCategory.FTThemeType.PAPER) {
            choosePaperDialog.themeTypeInString = "PAPER";
        } else {
            choosePaperDialog.themeTypeInString = "COVER";
        }
        FTApp.getPref().save(SystemPref.THEME_TYPE, choosePaperDialog.themeTypeInString);

        choosePaperDialog.ftTemplateMode  = _fTTemplatepickerInputInfo.get_ftTemplateOpenMode();
        choosePaperDialog.ftNewNotebookDialog = _fTTemplatepickerInputInfo.get_ftNewNotebookDialog();
        choosePaperDialog._baseShelfActivity = _fTTemplatepickerInputInfo.get_baseShelfActivity();
        return choosePaperDialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        templatesInfoList =  FTTemplatesInfoSingleton.getInstance().getTemplatesInfo(themeType);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        dialog = super.onCreateDialog(savedInstanceState);
        if (!isMobile() && dialog != null) {
            Window window = dialog.getWindow();
            if (window != null) {
                if (getActivity() instanceof FTDocumentActivity) {
                    hideStatusBar();
                    int width = ViewGroup.LayoutParams.MATCH_PARENT;
                    int height = ViewGroup.LayoutParams.MATCH_PARENT;
                    window.setLayout(width, height);
                } else {
                    int width = ViewGroup.LayoutParams.MATCH_PARENT;
                    int height = getResources().getDisplayMetrics().heightPixels - ScreenUtil.getStatusBarHeight(getContext());
                    window.getAttributes().y += ScreenUtil.getStatusBarHeight(getContext());
                    window.setLayout(width, height);
                }
            }

        } else {
            if (dialog != null) {
                int width = ViewGroup.LayoutParams.MATCH_PARENT;
                int height = ViewGroup.LayoutParams.MATCH_PARENT;
                BottomSheetDialog bottomSheetDialog = (BottomSheetDialog) dialog;
                bottomSheetDialog.getBehavior().setDraggable(false);
                bottomSheetDialog.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);
                bottomSheetDialog.getWindow().setLayout(width, height);
            }
        }

        // Add back button listener
        dialog.setOnKeyListener(new Dialog.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    // Your code here
                    Log.d("TemplatePicker==>", " setOnKeyListener ACTION_UP::-");
                    return true; // Capture onKey
                }
                Log.d("TemplatePicker==>", " setOnKeyListener ACTION_UP::-");
                return false; // Don't capture
            }
        });

        return dialog;
    }

    public void hideStatusBar() {
        dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View rootview = inflater.inflate(R.layout.choosepapertemplateformatnew, container, false);

        return rootview;
    }

    private void tabSelection(String tab) {

        String prevSelectedDeviceName = FTApp.getPref().get(SystemPref.LAST_SELECTED_PAPER, "A4 8.3 x 11.7\"\"");
        FTApp.getPref().save(SystemPref.LAST_SELECTED_PAPER, prevSelectedDeviceName);
        Log.d("TemplatePickerV2", "tabSelection tabSelection:: "+tab);

        if (tab.toLowerCase().contains("portrait")) {
            templateOrientation.setText("Portrait");
        } else {
            templateOrientation.setText("Landscape");
        }

        if (FTApp.getPref().get(SystemPref.SEARCH_ENABLED, false)) {


            updateUI(tab.toLowerCase(),
                   FTSelectedDeviceInfo.selectedDeviceInfo().getItemModel(),
                    "addOnTabSelectedListener");

            //filter(ENTERED_SEARCH_TEXT, "tabSelection");
        } else {

            updateUI(tab.toLowerCase(),
                    FTSelectedDeviceInfo.selectedDeviceInfo().getItemModel(),
//                    FTTemplatesInfoSingleton.getInstance().getRecentlySelctedDevice(),
                    "addOnTabSelectedListener");
        }

    }

    private void filter(String toString, String origin) {
        FTApp.getPref().save(SystemPref.SEARCH_QUERY, toString);
        Log.d("TemplatePicker==>", "FTTemplateCategories::- filter origin::-" + origin+" toString:: "+toString);
        //mFTTemplateCategories.searchTemplates(toString);
        mFTTemplateCategories.getFilter().filter(toString);
    }

    private void initAdapter(ArrayList<String> mCategoriesArrayList) {
        mTemplateCategoriesAdapter = new FTTemplateCategoriesAdapter(getActivity(),
                templatesInfoList, this);
        categoryLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        categoryItems.setAdapter(mTemplateCategoriesAdapter);
        categoryItems.setLayoutManager(categoryLayoutManager);
        int tabPositionPrevSelected = FTApp.getPref().get(SystemPref.CATEGORY_SELECTED_POSITION, 0);
        String tabSelected = FTApp.getPref().get(SystemPref.LAST_SELECTED_TAB, "Portrait");
        categoryItems.smoothScrollToPosition(tabPositionPrevSelected);
        mFTTemplateCategories = new FTTemplateCategories(mTemplateCategoriesAdapter, mCategoriesArrayList, themeType,templatesInfoList,this);

        Log.d("TemplatePicker==>", " getSelectedDeviceName::-" + FTSelectedDeviceInfo.selectedDeviceInfo().getSelectedDeviceName());

        if (themeType == FTNThemeCategory.FTThemeType.COVER) {
            orientation_tab_layout2.setVisibility(View.GONE);
            orientation_tab_layout1.setVisibility(View.GONE);
        }
    }

    public void showClub() {
        FTStoreActivity.start(getContext());
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        categoryItems.setNestedScrollingEnabled(false);
        ObservingService.getInstance().addObserver("addDownloadedTheme", addDownloadsThemeObserver);
        ObservingService.getInstance().addObserver("addCustomTheme", addCustomThemeObserver);
        ftSelectedDeviceInfo = FTSelectedDeviceInfo.selectedDeviceInfo();
        String tabSelected = ftSelectedDeviceInfo.getLayoutType();
        Log.d("TemplatePickerV2", "TemplatePickerV2 LAST_SELECTED_TAB onViewCreated::-" + tabSelected);
        deviceSize = FTApp.getPref().get(SystemPref.LAST_SELECTED_PAPER, "A4 8.3 x 11.7\"\"");

        Log.d("TemplatePicker==>", "DeviceSelection:: FTChoosePaperTemplate onViewCreated deviceSize::-" + deviceSize);

        if (themeType == FTNThemeCategory.FTThemeType.COVER) {
            paperSizeSelectionLyt.setVisibility(View.GONE);
            coverTitleLyt.setVisibility(View.VISIBLE);
            coverTitleID.setText(getResources().getString(R.string.cover));
        } else {
            paperSizeSelectionLyt.setVisibility(View.VISIBLE);
            coverTitleLyt.setVisibility(View.GONE);
        }

        Gson gson = new Gson();
        /*String itemModelInfo = FTApp.getPref().get(SystemPref.TEMPLATE_MODEL_INFO, "ModelInfo");
        if (!itemModelInfo.equalsIgnoreCase("ModelInfo")) {
            itemModel =  FTSelectedDeviceInfo.selectedDeviceInfo().getItemModel();
        } else {
            itemModel = new ItemModel(
                    "595_842",
                    "842_595",
                    "595_842",
                    "A4 8.3 x 11.7\"\"",
                    "standard2");
        }*/

//        FTTemplatesInfoSingleton.getInstance().setRecentlySelctedDevice(itemModel);

        itemModel = FTSelectedDeviceInfo.selectedDeviceInfo().getItemModel();
       // FTSelectedDeviceInfo.selectedDeviceInfo().setItemModel(itemModel);
        if (tabSelected.toLowerCase().contains("port")) {
            Log.d("TemplatePickerV2", "onViewCreated tabSelected port TRue:: "+tabSelected);

            if (deviceSize.contains("A4")) {
                FTSelectedDeviceInfo ftSelectedDeviceInfo1 = new FTSelectedDeviceInfo();
                ftSelectedDeviceInfo1.setPageWidth(595);
                ftSelectedDeviceInfo1.setPageHeight(842);
            }
            templateOrientation.setText("Portrait");
            portrait_tab1.setBackgroundResource(R.drawable.template_picker_tab_item_bg);
            portrait_tab.setBackgroundResource(R.drawable.template_picker_tab_item_bg);
            landscape_tab1.setBackgroundResource(0);
            landscape_tab.setBackgroundResource(0);
        } else {
            Log.d("TemplatePickerV2", "onViewCreated tabSelected land TRue:: "+tabSelected);

            if (deviceSize.contains("A4")) {
                FTSelectedDeviceInfo ftSelectedDeviceInfo1 = new FTSelectedDeviceInfo();
                ftSelectedDeviceInfo1.setPageWidth(842);
                ftSelectedDeviceInfo1.setPageHeight(595);
            }
            templateOrientation.setText("Landscape");
            landscape_tab1.setBackgroundResource(R.drawable.template_picker_tab_item_bg);
            landscape_tab.setBackgroundResource(R.drawable.template_picker_tab_item_bg);
            portrait_tab1.setBackgroundResource(0);
            portrait_tab.setBackgroundResource(0);
        }

        deviceModelSpinnerItem.setText(deviceSize);
        mCategoriesArrayList = new ArrayList<>();

        for (int i = 0; i < ftCategoriesArrayList.size(); i++) {
            mCategoriesArrayList.add(ftCategoriesArrayList.get(i).getCategory_name());
            Log.d("TemplatePicker==>", "FluidTouch getCategory_name::-" + ftCategoriesArrayList.get(i).getCategory_name());
        }

        initAdapter(mCategoriesArrayList);

        /*
         * New code start
         * */
        paperSizeSelectionLyt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("TemplatePicker==>", "FTTemplateTabsDialog paperSizeSelectionLyt::-");
                ftTemplateTabsDialog = FTTemplateTabsDialog.newInstance();
                ftTemplateTabsDialog.show(getChildFragmentManager(), FTTemplateTabsDialog.class.getName());
            }
        });

        Log.d("TemplatePicker==>", " portrait_tab::-" + portrait_tab + " landscape_tab::-" + landscape_tab);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);

        Log.d("TemplatePickerV2", "NewNotebook FTChoosePaperTemplate ftTemplateMode::-" + ftTemplateMode +
                " getActivity():: " + getActivity());

        if (getParentFragment() != null) {
            listener = (TemplatePaperChooseListener) getParentFragment();
            isCurrentPage = listener.isCurrentTheme();
            if (getParentFragment() instanceof FTQuickCreateSettingsPopup) {
                view.clearAnimation();
            }

        } else if (getActivity() != null) {
            listener = (TemplatePaperChooseListener) getActivity();
            isCurrentPage = listener.isCurrentTheme();
        }

        loadTemplatesInfo();

    }

    private void loadTemplatesInfo() {
        FTSelectedDeviceInfo selectedDeviceInfo = FTSelectedDeviceInfo.selectedDeviceInfo();

        for (int i=0;i<templatesInfoList.size();i++) {
            for (int j=0;j<templatesInfoList.get(i).get_themeseList().size();j++) {
                FTNTheme _ftnTheme = templatesInfoList.get(i).get_themeseList().get(j);
                if (!_ftnTheme.getCategoryName().contains("Recent")) {
                    if (_ftnTheme.getCategoryName().equalsIgnoreCase("basic")) {
                        _ftnTheme.themeBgClr            = selectedDeviceInfo.getThemeBgClrHexCode();
                        _ftnTheme.themeBgClrName        = selectedDeviceInfo.getThemeBgClrName();
                        _ftnTheme.horizontalLineColor   = selectedDeviceInfo.getHorizontalLineClr();
                        _ftnTheme.verticalLineColor     = selectedDeviceInfo.getVerticalLineClr();
                        _ftnTheme.horizontalSpacing     = selectedDeviceInfo.getHorizontalLineSpacing();
                        _ftnTheme.verticalSpacing       = selectedDeviceInfo.getVerticalLineSpacing();
                    }

                    _ftnTheme.thumbnailURLPath      = FTTemplateUtil.getInstance().generateThumbnailURLPath(_ftnTheme.themeFileURL,_ftnTheme);
                    Log.d("TemplatePickerV2", "TemplatePickerV2 thumbnailURLPath onCreate thumbnailURLPath::-"
                            + _ftnTheme.thumbnailURLPath);
                }
                Log.d("::TemplatePickerV2","FTTemplateCategories generateThumbnailURLPath onCreate thumbnailURLPath " +
                        " thumbnailURLPath:: "+_ftnTheme.thumbnailURLPath+" getLayoutType:: "+selectedDeviceInfo.getLayoutType()+
                        " getPageWidth:: "+selectedDeviceInfo.getPageWidth()+
                        " getPageHeight():: "+selectedDeviceInfo.getPageHeight());
            }
        }

        String tabSelected = selectedDeviceInfo.getLayoutType();
        Log.d("TemplatePicker==>", " LAST_SELECTED_TAB loadTemplatesInfo::-" + tabSelected);
        ObservingService.getInstance().postNotification("newDeviceSelected", tabSelected);
        mFTTemplateCategories.typeOfLayout = selectedDeviceInfo.getLayoutType();
        getChildFragmentManager().beginTransaction().replace(R.id.fragment_container, mFTTemplateCategories).commitAllowingStateLoss();

    }

    @OnClick({R.id.portrait_tab1, R.id.portrait_tab})
    void portraitTabClicked() {
        FTTemplatesInfoSingleton.getInstance().getSupportedDevicesInfo();
        portrait_tab1.setBackgroundResource(R.drawable.template_picker_tab_item_bg);
        portrait_tab.setBackgroundResource(R.drawable.template_picker_tab_item_bg);
        landscape_tab1.setBackgroundResource(0);
        landscape_tab.setBackgroundResource(0);
        tabSelection("Portrait");
    }

    @OnClick({R.id.landscape_tab1, R.id.landscape_tab})
    void landscapeTabClicked() {
        Log.d("TemplatePicker==>", "setOnClickListener landscape_tab::-");
        FTTemplatesInfoSingleton.getInstance().getSupportedDevicesInfo();
        landscape_tab.setBackgroundResource(R.drawable.template_picker_tab_item_bg);
        landscape_tab1.setBackgroundResource(R.drawable.template_picker_tab_item_bg);
        portrait_tab.setBackgroundResource(0);
        portrait_tab1.setBackgroundResource(0);
        tabSelection("landscape");
    }

    @OnClick(R.id.club_downlaods_btn)
    void onClubDownloadsIConClicked() {
        //showClub();
        /*if (ftNewNotebookDialog != null) {
            ftNewNotebookDialog.dismiss();
        }*/
        FTStoreActivity.start(getContext());
    }

    private ArrayList<String> checkFilesinDownloadedFolder() {
        ArrayList<String> prevDownloadedTemplatesNameList = new ArrayList<>();
        String downloadedThemesFolderPath = themeType == FTNThemeCategory.FTThemeType.PAPER ? FTConstants.DOWNLOADED_PAPERS_PATH2 : FTConstants.DOWNLOADED_COVERS_PATH;
        File downloadedThemesDir = new File(downloadedThemesFolderPath);
        if (downloadedThemesDir.exists() && downloadedThemesDir.isDirectory()) {
            for (File eachThemeDir : downloadedThemesDir.listFiles()) {
                if (eachThemeDir.exists() && eachThemeDir.isDirectory()) {
                    FTNTheme theme = FTNTheme.theme(new FTUrl(eachThemeDir.getAbsolutePath()));
                    prevDownloadedTemplatesNameList.add(theme.categoryName);
                }
            }
        }

        prevDownloadedTemplatesNameList.removeAll(Arrays.asList(null, ""));
        return prevDownloadedTemplatesNameList;
    }

    @OnClick(R.id.dialog_close_button)
    void onTemplateDialogCloseiConClicked() {
        if (listener != null) {
            listener.onClose();
        }

        //addDownloadsThemeObserver.r
        ObservingService.getInstance().removeObserver("addDownloadsThemeObserver", addDownloadsThemeObserver);
        ObservingService.getInstance().removeObserver("addCustomTheme", addCustomThemeObserver);
        dismiss();
    }

    @OnClick(R.id.searchiCon)
    void onSearchiConClicked() {
        FTApp.getPref().save(SystemPref.SEARCH_ENABLED, true);
        categoriesLyt.setVisibility(View.GONE);
        searchTemplatesLayout.setVisibility(View.VISIBLE);
        chooseTemplateOrientationLyt.setVisibility(View.GONE);
        if (themeType == FTNThemeCategory.FTThemeType.COVER) {
            orientation_tab_layout1.setVisibility(View.GONE);
            orientation_tab_layout2.setVisibility(View.GONE);
        } else {
            orientation_tab_layout1.setVisibility(View.VISIBLE);
            orientation_tab_layout2.setVisibility(View.GONE);
        }


        searchTextLyt.setVisibility(View.VISIBLE);
        searchTextLyt.addTextChangedListener(this);
        IBinder windowToken = searchTextLyt.getWindowToken();
        showKeyboard(windowToken, searchTextLyt);

    }

    @OnClick(R.id.clear_search_btn)
    void onCloseSearchBtnClicked() {
        FTApp.getPref().save(SystemPref.SEARCH_ENABLED, false);
        categoriesLyt.setVisibility(View.VISIBLE);
        searchTemplatesLayout.setVisibility(View.GONE);
        chooseTemplateOrientationLyt.setVisibility(View.VISIBLE);

        if (themeType == FTNThemeCategory.FTThemeType.COVER) {
            orientation_tab_layout1.setVisibility(View.GONE);
            orientation_tab_layout2.setVisibility(View.GONE);
        } else {
            orientation_tab_layout1.setVisibility(View.VISIBLE);
            orientation_tab_layout2.setVisibility(View.VISIBLE);
        }


        FTApp.getPref().save(SystemPref.ENTERED_SEARCH_TEXT, "");
        IBinder windowToken = searchTextLyt.getWindowToken();
        hideKeyboard(windowToken);
        searchTextLyt.setText(null);
        searchTextLyt.removeTextChangedListener(this);
        searchTextLyt.clearFocus();

    }

    @Override
    public void onStart() {
        super.onStart();
    }

    public void hideKeyboard(IBinder windowToken) {
        InputMethodManager imm = (InputMethodManager) FTApp.getInstance().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(windowToken, 0);
        }
    }

    public void showKeyboard(IBinder windowToken, EditText remarkEditText) {
        InputMethodManager inputManager =
                (InputMethodManager) FTApp.getInstance().getSystemService(
                        Context.INPUT_METHOD_SERVICE);
        inputManager.showSoftInput(remarkEditText, 0);
    }

    @Override
    public void categorySelected(String catTitle, int position, float yPosition) {
        categoryItems.smoothScrollToPosition(position);
        mFTTemplateCategories.moveToCategorySelction(position);
    }

    @Override
    public void onTemplateSelctedInfo(FTNTheme theme, boolean isLandscapeStatus) {
        Log.d("TemplatePickerV2", "FTChoosePaperTemplate onTemplateSelctedInfo ftTemplateMode:: "+ftTemplateMode);

        if (ftTemplateMode == FTTemplateMode.NewNotebook) {
            Log.d("TemplatePickerV2", "NewNotebook FTChoosepaperTemplate onTemplateSelctedInfo inside NewNotebook ftnTheme:: "+theme.themeName);

            listener.onThemeChosen(theme, isCurrentPage, isLandscapeStatus);
        } else {
            Log.d("TemplatePickerV2", "NewNotebook FTChoosepaperTemplate onTemplateSelctedInfo Otherthan NewNotebook ftnTheme:: "+theme);

            listener.onThemeChosen(theme, isCurrentPage, isLandscapeStatus);
        }
        dismiss();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDeviceSelected(String tabSelected, ItemModel clickedItemInfo, String origin) {
        if(ftTemplateTabsDialog!=null){
            ftTemplateTabsDialog.dismiss();
        }
        deviceModelSpinnerItem.setText(clickedItemInfo.getDisplayName());

        Log.d("TemplatePicker==>", "onDeviceSelected Status::-" +
                (!isMobile() && clickedItemInfo.getDisplayName().toLowerCase().equalsIgnoreCase("Mobile"))
                + " isMobile()::-" + isMobile() +
                " getDisplayName::-" + clickedItemInfo.getDisplayName().toLowerCase());
        orientationTabVisibility(clickedItemInfo.getDisplayName(), clickedItemInfo, origin, tabSelected);
    }

    private void orientationTabVisibility(String deviceType, ItemModel clickedItemInfo, String origin, String tabSelected) {

        if (!isMobile()) {
            if (deviceType.toLowerCase().equalsIgnoreCase("mobile")) {
                orientation_tab_layout2.setVisibility(View.GONE);
                orientation_tab_layout1.setVisibility(View.GONE);
                if (ftTemplateTabsDialog != null) {
                    updateUI("Portrait", clickedItemInfo, "orientationTabVisibility");
                }
            } else {
                orientation_tab_layout2.setVisibility(View.VISIBLE);
                orientation_tab_layout1.setVisibility(View.VISIBLE);
                if (ftTemplateTabsDialog != null) {
                    updateUI(tabSelected,
                            clickedItemInfo,
                            "orientationTabVisibility");
                }
            }
            Log.d("TemplatePicker==>", "onDeviceSelected IF::-"+tabSelected);
        } else {
            if (deviceType.toLowerCase().equalsIgnoreCase("This Device")) {
                orientation_tab_layout2.setVisibility(View.GONE);
                orientation_tab_layout1.setVisibility(View.GONE);
                if (ftTemplateTabsDialog != null) {
                    updateUI("Portrait", clickedItemInfo, "orientationTabVisibility");
                }
            } else {
                orientation_tab_layout2.setVisibility(View.GONE);
                orientation_tab_layout1.setVisibility(View.VISIBLE);
                if (ftTemplateTabsDialog != null) {
                    updateUI(tabSelected,
                            clickedItemInfo,
                            "orientationTabVisibility");
                }
            }
        }
    }

    private void updateUI(String tabSelected, ItemModel clickedItemInfo, String origin) {

        /*if (!origin.contains("addOnTabSelectedListener")) {
            if (ftTemplateTabsDialog != null) {
                ftTemplateTabsDialog.dismiss();
            }
        }*/

        itemModel = clickedItemInfo;
        if (tabSelected.toLowerCase().contains("portrait")) {
            dimension = itemModel.getDimension_port();
        } else {
            dimension = itemModel.getDimension_land();
        }

        headerTitle = itemModel.getHeaderTitle();
        Log.d("::TemplatePickerV2:::", "Device Size::- Size info onDeviceSelected " +
                " tabSelected::-" + tabSelected  +" dimension:: "+dimension);
        if (dimension != null && !dimension.isEmpty()) {

            splitString = dimension.split("_");
            Log.d("TemplatePikcer==>", "Device Size::- Size info onDeviceSelected " +
                    " tabSelected::-" + tabSelected +
                    " getDimension_port::- " + itemModel.getDimension_port() +
                    " getDimension_land::-" + itemModel.getDimension_land() +
                    " theme.width::-" + splitString[0] +
                    " theme.height::-" + splitString[1] +
                    " origin::-" + origin);

            FTSelectedDeviceInfo selectedDeviceInfo = FTSelectedDeviceInfo.selectedDeviceInfo();

            FTSelectedDeviceInfo ftSelectedDeviceInfo = new FTSelectedDeviceInfo();
            ftSelectedDeviceInfo.setPageHeight(Integer.parseInt(splitString[1]));
            ftSelectedDeviceInfo.setPageWidth(Integer.parseInt(splitString[0]));
            ftSelectedDeviceInfo.setLineType(selectedDeviceInfo.getLineType());
            ftSelectedDeviceInfo.setLayoutType(tabSelected.toLowerCase());
            ftSelectedDeviceInfo.setVerticalLineSpacing(selectedDeviceInfo.getVerticalLineSpacing());
            ftSelectedDeviceInfo.setHorizontalLineSpacing(selectedDeviceInfo.getHorizontalLineSpacing());
            ftSelectedDeviceInfo.setThemeBgClrName(selectedDeviceInfo.getThemeBgClrName());
            ftSelectedDeviceInfo.setThemeBgClrHexCode(selectedDeviceInfo.getThemeBgClrHexCode());
            ftSelectedDeviceInfo.setHorizontalLineClr(selectedDeviceInfo.getHorizontalLineClr());
            ftSelectedDeviceInfo.setVerticalLineClr(selectedDeviceInfo.getVerticalLineClr());
            ftSelectedDeviceInfo.selectSavedDeviceInfo();
            Log.d("FTChoosePaperTemplate 1==>","1");
            templatesInfoList =  FTTemplatesInfoSingleton.getInstance().getTemplatesInfo(themeType);
            FTSelectedDeviceInfo _ftSelectedDeviceInfo = FTSelectedDeviceInfo.selectedDeviceInfo();

            for (int i=0;i<templatesInfoList.size();i++) {
                for (int j=0;j<templatesInfoList.get(i).get_themeseList().size();j++) {
                    FTNTheme _ftnTheme = templatesInfoList.get(i).get_themeseList().get(j);
                    if (!_ftnTheme.getCategoryName().contains("Recent")) {
                        if (_ftnTheme.getCategoryName().equalsIgnoreCase("basic")) {
                            _ftnTheme.themeBgClr            = _ftSelectedDeviceInfo.getThemeBgClrHexCode();
                            _ftnTheme.themeBgClrName        = _ftSelectedDeviceInfo.getThemeBgClrName();
                            _ftnTheme.horizontalLineColor   = _ftSelectedDeviceInfo.getHorizontalLineClr();
                            _ftnTheme.verticalLineColor     = _ftSelectedDeviceInfo.getVerticalLineClr();
                            _ftnTheme.horizontalSpacing     = _ftSelectedDeviceInfo.getHorizontalLineSpacing();
                            _ftnTheme.verticalSpacing       = _ftSelectedDeviceInfo.getVerticalLineSpacing();
                            _ftnTheme.width                 = _ftSelectedDeviceInfo.getPageWidth();
                            _ftnTheme.height                = _ftSelectedDeviceInfo.getPageHeight();
                        }
                        _ftnTheme.isLandscape                = (_ftSelectedDeviceInfo.getLayoutType().toLowerCase().contains("land") ? true : false) ;
                        _ftnTheme.thumbnailURLPath      = FTTemplateUtil.getInstance().generateThumbnailURLPath(_ftnTheme.themeFileURL,_ftnTheme);
                        Log.d("TemplatePickerV2", "TemplatePickerV2 thumbnailURLPath tabSelection thumbnailURLPath:: "
                                + _ftnTheme.thumbnailURLPath +" _ftnTheme.isLandscape:: "+_ftnTheme.isLandscape);
                    }
                    Log.d("::TemplatePickerV2","FTTemplateCategories generateThumbnailURLPath tabSelection thumbnailURLPath " +
                            " thumbnailURLPath:: "+_ftnTheme.thumbnailURLPath+" getLayoutType:: "+_ftSelectedDeviceInfo.getLayoutType()+
                            " getPageWidth:: "+_ftSelectedDeviceInfo.getPageWidth()+
                            " getPageHeight():: "+_ftSelectedDeviceInfo.getPageHeight());
                }
            }
            mFTTemplateCategories.updateUI(templatesInfoList, "onDeviceSelected");
            Log.d("TemplatePicker==>", "deviceModelsSpinner.setOnItemSelectedListener createNewShelfItem theme Width::-" + splitString[0] + " Height" + splitString[1] + " tabSelected::-" + tabSelected);
            ObservingService.getInstance().postNotification("newDeviceSelected", tabSelected);

            String ENTERED_SEARCH_TEXT = FTApp.getPref().get(SystemPref.ENTERED_SEARCH_TEXT, "");
            Log.d("TemplatePicker==>", " SEARCH_ENABLED is TRUE ENTERED_SEARCH_TEXT::-" + ENTERED_SEARCH_TEXT +
                    " isEmpty True::-" + ENTERED_SEARCH_TEXT.isEmpty() +
                    " ENTERED_SEARCH_TEXT is Null::-" + (ENTERED_SEARCH_TEXT == null));
            filter(ENTERED_SEARCH_TEXT, "tabSelection");
        }
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        Log.d("TemplatePicker==>", "Search enabled onTextChanged Before charSequence:: " + charSequence.toString());
        if (charSequence.toString().contains("   ")) {
            Toast.makeText(getContext(), "Search is not allowed for More Spaces.", Toast.LENGTH_SHORT).show();
            return ;
        }
        if (!charSequence.toString().isEmpty() &&
                !charSequence.toString().equalsIgnoreCase("")) {
            charSequence.toString().replace("  "," ");
            FTApp.getPref().save(SystemPref.ENTERED_SEARCH_TEXT, charSequence.toString());
            filter(charSequence.toString(), "onTextChanged !charSequence.toString().isEmpty");

        } else {
            Log.d("TemplatePicker==>", "Search enabled onTextChanged charSequence is Empty and double Quotes::-" + charSequence.toString());
            FTApp.getPref().save(SystemPref.ENTERED_SEARCH_TEXT, "");
            filter(charSequence.toString(), "onTextChanged !charSequence.toString().isEmpty");
        }
    }

    private void searchQuery(String charSequence){
        Log.d("TemplatePicker==>", "Search enabled onTextChanged Before charSequence:: " + charSequence.toString());

        if (!charSequence.toString().isEmpty() &&
                !charSequence.toString().equalsIgnoreCase("")) {

            FTApp.getPref().save(SystemPref.ENTERED_SEARCH_TEXT, charSequence.toString());
            filter(charSequence.toString(), "onTextChanged !charSequence.toString().isEmpty");

        } else {
            Log.d("TemplatePicker==>", "Search enabled onTextChanged charSequence is Empty and double Quotes::-" + charSequence.toString());
            FTApp.getPref().save(SystemPref.ENTERED_SEARCH_TEXT, "");
            /*mFTTemplatesInfoSingleton.getCategoriesList(themeType);
            ftCategoriesArrayList.clear();
            ftCategoriesArrayList.addAll(mFTTemplatesInfoSingleton.getFTNThemeCategory("FTChoosePaperTemplate onCreate", themeType));
            mFTTemplateCategories.updateUI(ftCategoriesArrayList, "searchNull");*/

            filter(charSequence.toString(), "onTextChanged !charSequence.toString().isEmpty");
        }
    }

    @Override
    public void afterTextChanged(Editable editable) {

    }

    public interface TemplatePaperChooseListener {
        void onThemeChosen(FTNTheme theme, boolean isCurrentPage, boolean isLandscapeStatus);

        boolean isCurrentTheme();

        void onClose();
    }

    public interface CallBackForNewNotebookDialogListener {
        void openNewNotebookdialog(FTNTheme ftnTheme);
    }


}