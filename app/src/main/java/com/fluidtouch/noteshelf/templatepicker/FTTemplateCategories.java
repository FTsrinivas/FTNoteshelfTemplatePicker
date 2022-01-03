package com.fluidtouch.noteshelf.templatepicker;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf.commons.utils.ObservingService;
import com.fluidtouch.noteshelf.document.FTDocumentActivity;
import com.fluidtouch.noteshelf.documentframework.FTUrl;
import com.fluidtouch.noteshelf.documentframework.Utilities.FTConstants;
import com.fluidtouch.noteshelf.models.theme.FTNPaperTheme;
import com.fluidtouch.noteshelf.models.theme.FTNTheme;
import com.fluidtouch.noteshelf.models.theme.FTNThemeCategory;
import com.fluidtouch.noteshelf.preferences.SystemPref;
import com.fluidtouch.noteshelf.shelf.activities.FTBaseShelfActivity;
//import com.fluidtouch.noteshelf.shelf.activities.FTShelfGroupableActivity;
import com.fluidtouch.noteshelf.templatepicker.adapters.FTTemplateCategoriesAdapter;
import com.fluidtouch.noteshelf.templatepicker.adapters.FTTemplatesAdapter;
import com.fluidtouch.noteshelf.templatepicker.common.modelclasses.FTUserSelectedTemplateInfo;
import com.fluidtouch.noteshelf.templatepicker.common.util.FTTemplateUtil;
import com.fluidtouch.noteshelf.templatepicker.common.FTTemplatesInfoSingleton;
import com.fluidtouch.noteshelf.templatepicker.common.util.MyCustomLayoutManager;
import com.fluidtouch.noteshelf.templatepicker.common.plistdatamodel.FTCategories;
import com.fluidtouch.noteshelf.templatepicker.common.plistdatamodel.FTLineTypes;
import com.fluidtouch.noteshelf.templatepicker.common.plistdatamodel.FTSelectedDeviceInfo;
import com.fluidtouch.noteshelf.templatepicker.common.plistdatamodel.FTTemplateColors;
import com.fluidtouch.noteshelf.templatepicker.interfaces.AddCustomThemeListener;
import com.fluidtouch.noteshelf.templatepicker.interfaces.MoreColorsViewInterface;
import com.fluidtouch.noteshelf.templatepicker.interfaces.TemplateBackgroundListener;
import com.fluidtouch.noteshelf.templatepicker.interfaces.ThumGenCallBack;
import com.fluidtouch.noteshelf.templatepicker.models.RecentsInfoModel;
import com.fluidtouch.noteshelf.templatepicker.models.TemplatesInfoModel;
import com.fluidtouch.noteshelf2.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FTTemplateCategories extends Fragment implements MoreColorsViewInterface,
        TemplateBackgroundListener.TemplateInfoRequest,
        AddCustomThemeListener, ThumGenCallBack, Filterable,TemplateBackgroundListener.CallBackToShowClub {

    @BindView(R.id.parent_recyclerview)
    RecyclerView templateItemsRecyclerview;

    @BindView(R.id.nestedScrollView)
    NestedScrollView nestedScrollView;

    String typeOfLayout;
    String tabSelected;
    ArrayList<FTCategories> ftTemplateCategoryInfoArrayList = new ArrayList<>();
    String cachePath    = FTConstants.TEMP_FOLDER_PATH+"TemplatesCache/";

    FTLineTypes ftLineTypesInfo;
    FTTemplateUtil ftTemplateUtil;

    FTTemplateMoreDetailsPopupNew dialog;
    MyCustomLayoutManager mLayoutManager;
    FTTemplateColors ftTemplateColorsInfo;
    FTTemplatesAdapter ftTemplateCategoryAdapter;
    TemplateSelctedInfo mTemplateSelctedInfoListener;
    FTTemplatesInfoSingleton mFTTemplatesInfoSingleton;
    FTTemplateCategoriesAdapter mTemplateTopbarCategoriesAdapter;
    FTChoosePaperTemplate ftChoosePaperTemplate;
    ArrayList<Boolean> newlyDownloadedList = new ArrayList<>();
    ArrayList<String> mTopbarCategoriesArrayList;

    boolean typeOfClrSelectedByUser = false;

    boolean observerPatternCalled = false;

    int categoryPosition = 0;

    private FTNThemeCategory.FTThemeType themeType = FTNThemeCategory.FTThemeType.PAPER;
    ArrayList<TemplatesInfoModel> templatesInfoList;
    ArrayList<TemplatesInfoModel> filteredData ;

    String tempString;

    private Observer selectedDeviceSpinnerObserver = new Observer() {
        @Override
        public void update(Observable o, Object arg) {
            tabSelected = (String) arg;
            observerPatternCalled = true;
            typeOfClrSelectedByUser = FTApp.getPref().get(SystemPref.TEMPLATE_COLOR_SELECTED, false);
            Log.d("TemplatePicker==>","FTTemplateCategories::-Tabchanged selectedDeviceSpinnerObserver::-"+tabSelected);
            if (tabSelected != null) {
                Log.d("TemplatePicker==>","Tabchanged selectedDeviceSpinnerObserver");
                if (typeOfClrSelectedByUser) {
                    boolean searchMode = FTApp.getPref().get(SystemPref.SEARCH_ENABLED, false);
                    if (!searchMode) {
                        tempLineAndColorInfoResponse(
                                ftTemplateUtil.getFtTemplateColorsObj(),
                                ftTemplateUtil.getFtTemplateLineInfoObj(),
                                ftTemplateUtil.getFtSelectedDeviceInfo());
                    } else {
                        if (ftTemplateCategoryAdapter != null) {
                            Log.d("TemplatePicker==>","FTTemplateCategories::-notifyDataSetChanged selectedDeviceSpinnerObserver");
                            ftTemplateCategoryAdapter.notifyDataSetChanged();
                        }
                    }
                } else {
                    Log.d("TemplatePicker==>","Tabchanged selectedDeviceSpinnerObserver");
                    if (ftTemplateCategoryAdapter != null) {
                        Log.d("TemplatePicker==>","FTTemplateCategories::-notifyDataSetChanged selectedDeviceSpinnerObserver typeOfClrSelectedByUser is TRUE");
                        for (int i=0;i<ftTemplateCategoryInfoArrayList.size();i++) {
                            for (int j=0;j<ftTemplateCategoryInfoArrayList.get(i).getFtThemes().size();j++) {
                                Log.d("TemplatePicker==>","FTTemplateCategories::- Mani selectedDeviceSpinnerObserver isLandscape " +ftTemplateCategoryInfoArrayList.get(i).getFtThemes().get(j).isLandscape+
                                        " themeFileURL "+ftTemplateCategoryInfoArrayList.get(i).getFtThemes().get(j).themeFileURL.getPath());

                            }
                        }
                        ftTemplateCategoryAdapter.notifyDataSetChanged();
                    }
                }
            } else {
                Log.d("TemplatePicker==>","Tabchanged tabSelected is NULL");
            }
        }
    };

    public FTTemplateCategories(FTTemplateCategoriesAdapter mTemplateTopbarCategoriesAdapter,
                                ArrayList<String> mTopbarCategoriesArrayList,
                                FTNThemeCategory.FTThemeType themeType, ArrayList<TemplatesInfoModel> templatesInfoList,
                                FTChoosePaperTemplate ftChoosePaperTemplate) {
        this.mTemplateTopbarCategoriesAdapter = mTemplateTopbarCategoriesAdapter;
        this.mTopbarCategoriesArrayList       = mTopbarCategoriesArrayList;
        this.themeType                        = themeType;
        this.templatesInfoList                = templatesInfoList;
        this.filteredData           = templatesInfoList;

        this.ftChoosePaperTemplate  = ftChoosePaperTemplate;

    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        ObservingService.getInstance().addObserver("newDeviceSelected",
                selectedDeviceSpinnerObserver);
        return inflater.inflate(R.layout.template_categories_main_lyt,container,false);
    }

    public void updateUI(ArrayList<TemplatesInfoModel> ftTemplateCategoryInfoArrayListR,String origin) {
        Log.d("TemplatePicker==>","FTTemplateCategories::-notifyDataSetChanged ftTemplateCategoryInfoArrayListR ::-"+ftTemplateCategoryInfoArrayListR);
        if (ftTemplateCategoryInfoArrayListR != null &&
                !ftTemplateCategoryInfoArrayListR.isEmpty()) {
            templatesInfoList.clear();
            templatesInfoList.addAll(ftTemplateCategoryInfoArrayListR);
            ftTemplateCategoryAdapter.notifyDataSetChanged();
        }
    }

    public void searchTemplates(String query) {
        Log.d("TemplatePicker==>","FTTemplateCategories::- searchTemplates ::-"+query+" ftTemplateCategoryAdapter:: "+ftTemplateCategoryAdapter);
        //ftTemplateCategoryAdapter.getFilter().filter(query);
        //ftTemplateCategoryAdapter.searchTemplates(query);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        Log.d("TemplatePicker==>","Filtering onViewCreated ::-");
        ftTemplateUtil          = FTTemplateUtil.getInstance();
        ftTemplateColorsInfo    = ftTemplateUtil.getFtTemplateColorsObj();
        ftLineTypesInfo         = ftTemplateUtil.getFtTemplateLineInfoObj();
        //ftSelectedDeviceInfo    = FTSelectedDeviceInfo.selectedDeviceInfo();
        tabSelected             = FTSelectedDeviceInfo.selectedDeviceInfo().getLayoutType();

        if (getParentFragment() != null) {
            mTemplateSelctedInfoListener = (TemplateSelctedInfo) getParentFragment();
        } else if (getActivity() != null) {
            mTemplateSelctedInfoListener = (TemplateSelctedInfo) getActivity();
        }

        if (ftTemplateCategoryInfoArrayList != null &&
                !ftTemplateCategoryInfoArrayList.isEmpty()) {
            ftTemplateCategoryInfoArrayList.clear();
        }

        File tempCacheFiles = new File(cachePath);
        if (!tempCacheFiles.exists()) {
            tempCacheFiles.mkdir();
        }

        typeOfClrSelectedByUser         = FTApp.getPref().get(SystemPref.TEMPLATE_COLOR_SELECTED, false);
        mFTTemplatesInfoSingleton       = FTTemplatesInfoSingleton.getInstance();

        initTemplateCategoryView("onViewCreated",typeOfLayout);
    }

    private void initTemplateCategoryView(String Origin,String typeOfLayout) {
        final int[] lastPosition = {0};
        //newlyDownloadedList = checkNewlyDowloadedTemplates(ftTemplateCategoryInfoArrayList);

        Log.d("TemplatePicker==>","FTTemplateCategories::- initTemplateCategoryView ");

        ftTemplateCategoryAdapter = new FTTemplatesAdapter(templatesInfoList,
                this,this,this,typeOfLayout,
                this,this,getParentFragmentManager(),
                templateItemsRecyclerview,newlyDownloadedList,themeType);
        mLayoutManager = new MyCustomLayoutManager(getActivity());
        templateItemsRecyclerview.setHasFixedSize(false);
        templateItemsRecyclerview.setAdapter(ftTemplateCategoryAdapter);
        templateItemsRecyclerview.setLayoutManager(mLayoutManager);

        moveToCategorySelction(FTApp.getPref().get(SystemPref.CATEGORY_SELECTED_POSITION, 0));
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {

                String charString = charSequence.toString();
                tempString = charSequence.toString();
                if (charString.trim().isEmpty()) {
                    Log.d("TemplatePickerV2", " Search:: "+ filteredData  +" Size:: "+templatesInfoList .size());

                    FilterResults filterResults = new FilterResults();
                    filterResults.values = templatesInfoList ;
                    filterResults.count = templatesInfoList.size();
                    return filterResults;
                }else {

                    ArrayList<TemplatesInfoModel> filteredList = new ArrayList<>();

                    Log.d("TemplatePickerV2", " Search:: "+charString +" templatesInfoList Size:: "+templatesInfoList.size());

                    for (int i=0;i<templatesInfoList.size();i++) {
                        TemplatesInfoModel searchedTemplatesInfoModel = new TemplatesInfoModel();
                        searchedTemplatesInfoModel.set_categoryName(templatesInfoList.get(i).get_categoryName());

                        for (int j=0;j<templatesInfoList.get(i).get_themeseList().size();j++) {
                            Log.d("TemplatePickerV2", " Search packName:: "+templatesInfoList.get(i).get_themeseList().get(j).packName);

                            if (templatesInfoList.get(i).get_themeseList().get(j).themeName != null) {
                                Log.d("TemplatePickerV2", " Search:: "+charString +" Search match status :: "+
                                        (templatesInfoList.get(i).get_themeseList().get(j).themeName.toLowerCase().contains(charString.toLowerCase())) +
                                        " PackName:: "+templatesInfoList.get(i).get_themeseList().get(j).themeName.toLowerCase() +
                                        " Searched String:: "+charString.toLowerCase());
                                if (templatesInfoList.get(i).get_themeseList().get(j).themeName.toLowerCase().contains(charString.toLowerCase())) {
                                    Log.d("TemplatePickerV2", " Search:: " + templatesInfoList.get(i).get_themeseList().get(j).themeName);
                                    searchedTemplatesInfoModel.AddThemesToList(templatesInfoList.get(i).get_themeseList().get(j));
                                }
                            }
                        }

                        boolean isNUllOrEmpty = FTTemplateUtil.isNullOrEmpty(searchedTemplatesInfoModel.get_themeseList());
                        if (!isNUllOrEmpty) {
                            filteredList.add(searchedTemplatesInfoModel);
                        }
                    }

                    FilterResults filterResults = new FilterResults();
                    filterResults.values = filteredList;
                    filterResults.count = filteredList.size();
                    return filterResults;
                }

                /*String charString = charSequence.toString();
                List<FTCategories> originalListTemp = new ArrayList<>();
                ArrayList<FTCategories> filteredItems = new ArrayList<FTCategories>();
                if (charString != null && charString.toString().length() > 0) {
                    ArrayList<FTCategories> _searchedList = new ArrayList<>();
                    String themeType = FTApp.getPref().get(SystemPref.THEME_TYPE, "PAPER");

                    if (themeType.equalsIgnoreCase("PAPER")) {

                        originalListTemp.addAll(FTTemplatesInfoSingleton.getInstance().getFTNThemeCategory("search",
                                FTNThemeCategory.FTThemeType.PAPER));
                    } else {
                        originalListTemp.addAll(FTTemplatesInfoSingleton.getInstance().getFTNThemeCategory("search",
                                FTNThemeCategory.FTThemeType.COVER));
                    }

                    for (int i = 0; i < originalListTemp.size(); i++) {
                        FTCategories ftCategory = originalListTemp.get(i);
                        FTCategories mFTCategory = getFilteredListDummy(ftCategory, charString, originalListTemp);
                        if (mFTCategory != null) {
                            _searchedList.add(mFTCategory);
                        }
                    }

                    if (filteredItems != null) {
                        filteredItems.clear();
                    }

                    filteredItems.addAll(_searchedList);
                    Log.d("TemplatePicker==>", "search performFiltering filteredItems size:: " + filteredItems.size());

                } else {
                    String themeType = FTApp.getPref().get(SystemPref.THEME_TYPE, "PAPER");
                    FTTemplatesInfoSingleton mFTTemplatesInfoSingleton = null;
                    FTNThemeCategory.FTThemeType themeTypeNew = FTNThemeCategory.FTThemeType.PAPER;

                    if (themeType.equalsIgnoreCase("PAPER")) {
                        Log.d("TemplatePicker==>", "search performFiltering originalListTemp 1 Before size:: " + originalListTemp.size());

                        originalListTemp.addAll(FTTemplatesInfoSingleton.getInstance().getFTNThemeCategory("search",
                                FTNThemeCategory.FTThemeType.PAPER));

                    } else {

                        originalListTemp.addAll(FTTemplatesInfoSingleton.getInstance().getFTNThemeCategory("search",
                                FTNThemeCategory.FTThemeType.COVER));

                    }
                    if (filteredItems != null) {
                        filteredItems.clear();
                    }

                    filteredItems.addAll(originalListTemp);
                }

                FilterResults filterResults = new FilterResults();

                filterResults.values = filteredItems;
                Log.d("TemplatePicker==>", "search performFiltering result:: " + filterResults + " result.values:: " + filterResults.values);
                return filterResults;*/
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                /*Log.d("TemplatePicker==>", "search performFiltering publishResults:: " + charSequence.toString() + " size:: " + filterResults.values);
                ftTemplateCategoryInfoArrayList.clear();
                if (filterResults.values != null) {
                    ArrayList<FTCategories> ftTemplateCategoryInfoArrayListDummy = (ArrayList<FTCategories>) filterResults.values;
                    Log.d("TemplatePicker==>", "search performFiltering publishResults:: " + charSequence.toString() + " size:: " + ftTemplateCategoryInfoArrayListDummy.size());

                    // TODO: Temp comment
                    //ftTemplateCategoryInfoArrayList.addAll(ftTemplateCategoryInfoArrayListDummy);

                } else {
                    ftTemplateCategoryInfoArrayList.clear();
                }

                notifyDataSetChanged();*/

                //ftTemplateCategoryInfoArrayListFiltered = (ArrayList<TemplatesInfoModel>) filterResults.values;
                // notifyDataSetChanged();

                filteredData = (ArrayList<TemplatesInfoModel>) filterResults.values;
                /*templatesInfoList.clear();
                templatesInfoList.addAll((ArrayList<TemplatesInfoModel>) filterResults.values);*/
                Log.d("TemplatePicker==>", "search performFiltering publishResults:: " + charSequence.toString() + " size:: " + filteredData.size());
                ftTemplateCategoryAdapter.refreshUI(filteredData);
                //ftTemplateCategoryAdapter.notifyDataSetChanged();
                //notifyDataSetChanged();
            }
        };


    }

    public void updateTemplateAdapter(){

    }

    private ArrayList<Boolean> checkNewlyDowloadedTemplates(ArrayList<FTCategories> ftTemplatesInfoArrayList) {
        String filesBeforeNewDownloadsListJSON = FTApp.getPref().get(SystemPref.FILES_BEFORE_NEW_DOWNLOADS_PAPERS_LIST, null);
        String filesAfterNewDownloadsListJSON  = FTApp.getPref().get(SystemPref.FILES_AFTER_NEW_DOWNLOADS_PAPERS_LIST, null);

        Log.d("TemplatePicker==>","NewDownloads::- checkNewlyDowloadedTemplates " +
                "filesBeforeNewDownloadsListJSON::-"+filesBeforeNewDownloadsListJSON+
                " filesAfterNewDownloadsListJSON::-"+filesAfterNewDownloadsListJSON);
        if (filesAfterNewDownloadsListJSON != null &&
                !filesAfterNewDownloadsListJSON.isEmpty()) {

            ArrayList<FTCategories> templateInfoList = new ArrayList<>();
            //templateInfoList.addAll(mFTTemplatesInfoSingleton.getFTNThemeCategory("checkNewlyDowloadedTemplates",themeType));
            ArrayList<String> templateNamesList = new ArrayList<>();

            newlyDownloadedList.clear();

            ArrayList<String> afterDownloadedList = new ArrayList();
            Gson afterDownloadedListGson = new Gson();
            Type afterDownloadedListType = new TypeToken<ArrayList<String>>() {}.getType();
            afterDownloadedList = afterDownloadedListGson.fromJson(filesAfterNewDownloadsListJSON, afterDownloadedListType);
            afterDownloadedList.removeAll(Arrays.asList(null,""));

            for (int i=0;i<templateInfoList.size();i++) {
                templateNamesList.add(templateInfoList.get(i).getCategory_name());
            }
            templateNamesList.removeAll(Arrays.asList(null,""));

            Log.d("TemplatePicker==>","NewDownloads::- checkNewlyDowloadedTemplates ::-"+
                    " templateNamesList size::-"+templateNamesList.size()+
                    " afterDownloadedList size::-"+afterDownloadedList.size());

            Log.d("TemplatePicker==>","NewDownloads::- checkNewlyDowloadedTemplates ::-"+
                    " templateNamesList ::-"+templateNamesList+
                    " afterDownloadedList ::-"+afterDownloadedList);

            for (int i = 0; i< templateInfoList.size(); i++) {
                if (afterDownloadedList.contains(templateInfoList.get(i).getCategory_name())) {
                    newlyDownloadedList.add(i, true);
                    Log.d("TemplatePicker==>","NewDownloads::- checkNewlyDowloadedTemplates IF::-"+
                            " templateNamesList ::-"+templateNamesList.get(i));
                } else {
                    newlyDownloadedList.add(i, false);
                    Log.d("TemplatePicker==>","NewDownloads::- checkNewlyDowloadedTemplates ELSE::-"+
                            " templateNamesList ::-"+templateNamesList.get(i));
                }
            }
        } else {
            newlyDownloadedList.clear();
            /*
             * 1. On Fresh installation case
             * 2. Now downloads from club case
             * */
            ArrayList<String> fileInDownloadedList = new ArrayList<>();
            String downloadedThemesFolderPath = FTConstants.DOWNLOADED_PAPERS_PATH2;
            File downloadedThemesDir = new File(downloadedThemesFolderPath);
            if (downloadedThemesDir.exists() && downloadedThemesDir.isDirectory()) {
                for (File eachThemeDir : downloadedThemesDir.listFiles()) {
                    if (eachThemeDir.exists() && eachThemeDir.isDirectory()) {
                        FTNTheme themes = FTNTheme.theme(new FTUrl(eachThemeDir.getAbsolutePath()));
                        fileInDownloadedList.add(themes.categoryName);
                    }
                }
            }

            Set<String> fileInDownloadedListFinal = new LinkedHashSet<String>(fileInDownloadedList);

            fileInDownloadedListFinal.removeAll(Arrays.asList(null,""));
            Log.d("TemplatePicker==>","NewDownloads::- checkNewlyDowloadedTemplates fileInDownloadedList::-"+
                    fileInDownloadedListFinal);

            if (fileInDownloadedListFinal.contains("")) {
                Log.d("TemplatePicker==>","NewDownloads::- checkNewlyDowloadedTemplates  contains double Quotes::-");
            }

            if (fileInDownloadedListFinal != null &&
                    !fileInDownloadedListFinal.isEmpty()) {
                for (int i = 0; i< ftTemplatesInfoArrayList.size(); i++) {
                    if (fileInDownloadedListFinal.contains(ftTemplatesInfoArrayList.get(i).getCategory_name())) {
                        Log.d("TemplatePicker==>","NewDownloads::- checkNewlyDowloadedTemplates getCategory_name NOT matched::-"+
                                ftTemplatesInfoArrayList.get(i).getCategory_name());
                        newlyDownloadedList.add(i,true);
                    } else {
                        Log.d("TemplatePicker==>","NewDownloads::- checkNewlyDowloadedTemplates getCategory_name MATCHED::-"+
                                ftTemplatesInfoArrayList.get(i).getCategory_name());
                        newlyDownloadedList.add(i,false);
                    }
                }
            }
        }

        return newlyDownloadedList;
    }

    public void moveToCategorySelction(int position) {

        if ( templateItemsRecyclerview != null) {
            templateItemsRecyclerview.post(() -> {
                if (templateItemsRecyclerview.getChildAt(position) != null) {
                    float y = templateItemsRecyclerview.getY() + templateItemsRecyclerview.getChildAt(position).getY();
                    nestedScrollView.smoothScrollTo(0, (int) y);
                }
            });
        }
    }

    @Override
    public void moreColorsViewSelected(int xCordinate, int yCordinate) {
        dialog = new FTTemplateMoreDetailsPopupNew(this);
        dialog.show(getChildFragmentManager());
        dialog.xCordinateLoc = xCordinate;
        dialog.yCordinateLoc = yCordinate;
    }

    @Override
    public void tempLineAndColorInfoResponse(FTTemplateColors ftTemplateColorsInfo,
                                             FTLineTypes ftLineTypesInfo,
                                             FTSelectedDeviceInfo ftSelectedDeviceInfo) {

        this.ftTemplateColorsInfo    = ftTemplateColorsInfo;
        this.ftLineTypesInfo         = ftLineTypesInfo;
        tabSelected                  = FTSelectedDeviceInfo.selectedDeviceInfo().getLayoutType();

        Log.d("TemplatePicker==>","Filtering Template Selected action tempLineAndColorInfoResponse tabSelected::-"+tabSelected);

    }

    @Override
    public void moreColorViewSelected() {
        ftTemplateCategoryAdapter.updateMoreColorViewColorViewBg();
    }

    @Override
    public void templateBgColourChangedListener() {
        ArrayList<FTNTheme> recentBasicTemplates = new ArrayList<>();
        for (int i=0;i<ftTemplateCategoryInfoArrayList.size();i++) {
            if (ftTemplateCategoryInfoArrayList.get(i).getCategory_name().toLowerCase().contains("basic")) {
                for (int j=0;j<ftTemplateCategoryInfoArrayList.get(i).getFtThemes().size();j++) {
                    recentBasicTemplates.add(ftTemplateCategoryInfoArrayList.get(i).getFtThemes().get(j));
                }
            }
        }

        FTTemplatesInfoSingleton.getInstance().savedDataInSharedPrefs(ftTemplateCategoryInfoArrayList,"templateBgColourChangedListener");

        newlyDownloadedList.addAll(checkNewlyDowloadedTemplates(ftTemplateCategoryInfoArrayList));
        String searchQuery = FTApp.getPref().get(SystemPref.SEARCH_QUERY, "");
        boolean searchMode = FTApp.getPref().get(SystemPref.SEARCH_ENABLED, false);
        Log.d("TemplatePicker==>","FTTemplateCategories FTTemplateDetailedInfoAdapter::- searchMode::-"+ searchMode+ " searchQuery::-"+searchQuery);

        FTSelectedDeviceInfo selectedDeviceInfo = FTSelectedDeviceInfo.selectedDeviceInfo();
        Log.d("TemplatePickerV2", "TemplatePickerV2 selectedDeviceInfo FTTemplateCategories templateBgColourChangedListener getThemeBgClrName::-"
                + selectedDeviceInfo.getThemeBgClrName()+" getLayoutType:: "+selectedDeviceInfo.getLayoutType());

        int _refreshItemPos = 0;
        for (int i=0;i<templatesInfoList.size();i++) {
            for (int j=0;j<templatesInfoList.get(i).get_themeseList().size();j++) {
                FTNTheme _ftnTheme = templatesInfoList.get(i).get_themeseList().get(j);
                if (!_ftnTheme.getCategoryName().contains("Recent")) {
                    if (_ftnTheme.getCategoryName().equalsIgnoreCase("basic")) {
                        _refreshItemPos = i;
                        _ftnTheme.themeBgClr            = selectedDeviceInfo.getThemeBgClrHexCode();
                        _ftnTheme.themeBgClrName        = selectedDeviceInfo.getThemeBgClrName();
                        _ftnTheme.horizontalLineColor   = selectedDeviceInfo.getHorizontalLineClr();
                        _ftnTheme.verticalLineColor     = selectedDeviceInfo.getVerticalLineClr();
                        _ftnTheme.horizontalSpacing     = selectedDeviceInfo.getHorizontalLineSpacing();
                        _ftnTheme.verticalSpacing       = selectedDeviceInfo.getVerticalLineSpacing();
                    }

                    _ftnTheme.thumbnailURLPath      = FTTemplateUtil.getInstance().generateThumbnailURLPath(_ftnTheme.themeFileURL,_ftnTheme);
                    Log.d("TemplatePickerV2", "TemplatePickerV2 thumbnailURLPath templateBgColourChangedListener thumbnailURLPath::-"
                            + _ftnTheme.thumbnailURLPath);
                }
                Log.d("::TemplatePickerV2","FTTemplateCategories generateThumbnailURLPath thumbnailURLPath " +
                        " thumbnailURLPath:: "+_ftnTheme.thumbnailURLPath+" getLayoutType:: "+selectedDeviceInfo.getLayoutType()+
                        " getPageWidth:: "+selectedDeviceInfo.getPageWidth()+
                        " getPageHeight():: "+selectedDeviceInfo.getPageHeight());

                Log.d("::TemplatePickerV2","ColourVariants FTTemplateCategories templateBgColourChangedListener ColourVariants and Line Types getThemeBgClrName "
                        +selectedDeviceInfo.getThemeBgClrName()+" getThemeMoreBgClrName:: "
                        +selectedDeviceInfo.getThemeMoreBgClrName()+" getLineType:: "
                        +selectedDeviceInfo.getLineType());

            }
        }

        if (ftTemplateCategoryAdapter != null) {
            ftTemplateCategoryAdapter.notifyItemChanged(_refreshItemPos);
        }

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d("TemplatePicker==>","FTTemplateCategories::- onAttach  context::-"+ context);

    }

    @Override
    public void onTemplateSelect(FTNTheme theme,boolean isLandscapeStatus) {
        Log.d("TemplatePicker==>"," FTTemplateCategories::- Template Selected action FTTemplateCategories " +
                " theme.isLandscapeStatus status::-" +isLandscapeStatus+
                " theme.isLandscape::-"+theme.isLandscape+
                " getContext::-"+getContext());
        if (theme.isCustomTheme &&
                (theme.themeName.contains(getContext().
                        getResources().
                        getString(R.string.template_custom_theme)))) {
            Log.d("TemplatePicker==>","Template Selected action If Custom Theme");
            if (getContext() instanceof FTBaseShelfActivity) {
                ((FTBaseShelfActivity) getContext()).addCustomTheme(theme);
            } else if (getContext() instanceof FTDocumentActivity) {
                ((FTDocumentActivity) getContext()).addCustomTheme(theme);
            }
        } else {
            mTemplateSelctedInfoListener.onTemplateSelctedInfo(theme,isLandscapeStatus);
        }

        ArrayList<String> fileInDownloadedList = new ArrayList<>();
        String json = FTApp.getPref().get(SystemPref.FILES_AFTER_NEW_DOWNLOADS_PAPERS_LIST, null);
        if (json != null && !json.isEmpty()) {
            Gson gson = new Gson();
            Type type = new TypeToken<ArrayList<String>>() {}.getType();
            fileInDownloadedList = gson.fromJson(json, type);
        } else {
            String downloadedThemesFolderPath = FTConstants.DOWNLOADED_PAPERS_PATH2;
            File downloadedThemesDir = new File(downloadedThemesFolderPath);
            if (downloadedThemesDir.exists() && downloadedThemesDir.isDirectory()) {
                for (File eachThemeDir : downloadedThemesDir.listFiles()) {
                    if (eachThemeDir.exists() && eachThemeDir.isDirectory()) {
                        FTNTheme themes = FTNTheme.theme(new FTUrl(eachThemeDir.getAbsolutePath()));
                        fileInDownloadedList.add(themes.categoryName);
                    }
                }
            }
        }

        fileInDownloadedList.removeAll(Arrays.asList(null,""));
        Set<String> fileInDownloadedListFinal = new LinkedHashSet<String>(fileInDownloadedList);

        Log.d("TemplatePicker==>","NewDownloads::- onTemplateSelect Before comparison themeName::-"+theme.categoryName+
                " fileInDownloadedList::-"+fileInDownloadedListFinal);

        if (fileInDownloadedListFinal.contains(theme.categoryName)) {
            fileInDownloadedListFinal.remove(theme.categoryName);
        }

        Gson gson = new Gson();
        String toJson = gson.toJson(fileInDownloadedListFinal);
        FTApp.getPref().save(SystemPref.FILES_AFTER_NEW_DOWNLOADS_PAPERS_LIST, toJson);

        Log.d("TemplatePicker==>","NewDownloads::- onTemplateSelect After comparison themeName::-"+theme.categoryName+
                " fileInDownloadedList::-"+fileInDownloadedListFinal);

        savePositionOfListView(theme);
    }

    private void savePositionOfListView(FTNTheme theme) {
        ArrayList<String> categoriesNamesList = new ArrayList<>();
        for (int i=0;i<ftTemplateCategoryInfoArrayList.size();i++) {
            categoriesNamesList.add(ftTemplateCategoryInfoArrayList.get(i).getCategory_name());
            /*if (ftTemplateCategoryInfoArrayList.get(i).getCategory_name().equalsIgnoreCase(theme.getCategoryName())) {
                 categoryPosition = i;
            }*/
        }

        for (int i=0;i<categoriesNamesList.size();i++) {
            if (ftTemplateCategoryInfoArrayList.get(i).getCategory_name().equalsIgnoreCase(theme.getCategoryName())) {
                if (categoriesNamesList.contains("Recent")) {
                    categoryPosition = i;
                } else {
                    categoryPosition = i+1;
                }
            }
        }

        Log.d("TemplatePicker==>"," savePositionOfListView::-"+categoryPosition);
        FTApp.getPref().save(SystemPref.CATEGORY_SELECTED_POSITION, categoryPosition);
    }

    @Override
    public void onTemplateDelete(FTNTheme theme) {
        removeTemplate(theme);
    }

    private void removeTemplate(FTNTheme theme) {

        if (theme.ftThemeType == FTNThemeCategory.FTThemeType.PAPER) {
            removePaperTemplate(theme);
        } else {
            removeCoverTemplate(theme);
        }

        Log.d("TemplatePicker==>"," removeTemplate::-"+theme.categoryName);

    }

    private void removeCoverTemplate(FTNTheme theme) {

        if (theme.getCategoryName().toLowerCase().contains("recent")) {
            ArrayList<RecentsInfoModel> recentsThemesList = new ArrayList<>();
            for (int i=templatesInfoList.size()-1;i>=0;i--) {
                if (templatesInfoList.get(i).get_categoryName().toLowerCase().contains("recent")) {
                    for (int j=templatesInfoList.get(i).get_themeseList().size()-1;j>=0;j--) {

                        if (theme.thumbnailURLPath.equalsIgnoreCase(templatesInfoList.get(i).get_themeseList().get(j).thumbnailURLPath)) {
                            templatesInfoList.get(i).get_themeseList().remove(j);
                        }

                        Log.d("TemplatePickerV2","RemoveTemplate recent Before get_themeseList().size():: "+templatesInfoList.get(i).get_themeseList().size());
                        if (templatesInfoList.get(i).get_themeseList().size() == 0) {
                            templatesInfoList.remove(i);
                            ftTemplateCategoryAdapter.notifyDataSetChanged();
                            mTemplateTopbarCategoriesAdapter.notifyDataSetChanged();
                            FTTemplateUtil.getInstance().updateRecentCoversThemesListDummy(null);
                        }
                    }

                }
            }

            //Updating Recents List
            for (int i=0;i<templatesInfoList.size();i++) {
                if (templatesInfoList.get(i).get_categoryName().toLowerCase().contains("recent")) {
                    Log.d("TemplatePickerV2","RemoveTemplate recent Updating Recents List get_themeseList().size():: "
                            +templatesInfoList.get(i).get_themeseList().size());

                    for (int j=0;j<templatesInfoList.get(i).get_themeseList().size();j++) {
                        Log.d("TemplatePickerV2","RemoveTemplate recent Before get_themeseList().size():: "
                                +templatesInfoList.get(i).get_themeseList().size() +" ith Position "+i+" jth Position:: "+j);

                        RecentsInfoModel _updatedRecents = new RecentsInfoModel();
                        _updatedRecents.set_categoryName("Recent");
                        _updatedRecents.set_packName(templatesInfoList.get(i).get_themeseList().get(j).packName);
                        _updatedRecents.setThemeBgClr(templatesInfoList.get(i).get_themeseList().get(j).themeBgClr);
                        _updatedRecents.setThemeBgClrName(templatesInfoList.get(i).get_themeseList().get(j).themeBgClrName);
                        _updatedRecents.setHorizontalLineColor(templatesInfoList.get(i).get_themeseList().get(j).horizontalLineColor);
                        _updatedRecents.setVerticalLineColor(templatesInfoList.get(i).get_themeseList().get(j).verticalLineColor);
                        _updatedRecents.setVerticalSpacing(templatesInfoList.get(i).get_themeseList().get(j).verticalSpacing);
                        _updatedRecents.setHorizontalSpacing(templatesInfoList.get(i).get_themeseList().get(j).horizontalSpacing);
                        _updatedRecents.setWidth(templatesInfoList.get(i).get_themeseList().get(j).width);
                        _updatedRecents.setHeight(templatesInfoList.get(i).get_themeseList().get(j).height);
                        _updatedRecents.set_packName(templatesInfoList.get(i).get_themeseList().get(j).packName);
                        _updatedRecents.set_themeName(templatesInfoList.get(i).get_themeseList().get(j).themeName);
                        _updatedRecents.setLandscape(templatesInfoList.get(i).get_themeseList().get(j).isLandscape);
                        _updatedRecents.set_thumbnailURLPath(templatesInfoList.get(i).get_themeseList().get(j).thumbnailURLPath);
                        _updatedRecents.set_themeBitmapInStringFrmt(FTTemplateUtil.getInstance().BitMapToString(templatesInfoList.get(i).get_themeseList().get(j).bitmap));

                        recentsThemesList.add(_updatedRecents);
                    }

                    boolean _recentsThemesListIsNullOrEmpty =  FTTemplateUtil.getInstance().isNullOrEmpty(recentsThemesList);
                    if (!_recentsThemesListIsNullOrEmpty) {
                        FTTemplateUtil.getInstance().updateRecentCoversThemesListDummy(recentsThemesList);
                    } else {
                        FTTemplateUtil.getInstance().updateRecentCoversThemesListDummy(null);
                    }
                }
            }
        } else {
            theme.deleteTemplate();
            boolean _isSearchEnabled = FTApp.getPref().get(SystemPref.SEARCH_ENABLED, false);

            Log.d("TemplatePickerV2","RemoveTemplate Else templatesInfoList().size():: "+templatesInfoList.size());

            for (int i=templatesInfoList.size()-1;i>=0;i--) {
                Log.d("TemplatePickerV2","RemoveTemplate Else get_themeseList().size():: "+templatesInfoList.get(i).get_themeseList().size()
                        +" ith Loop "+i);
                if (!templatesInfoList.get(i).get_categoryName().toLowerCase().contains("recent")) {
                    for (int j=templatesInfoList.get(i).get_themeseList().size()-1;j>=0;j--) {
                        Log.d("TemplatePickerV2","RemoveTemplate Before get_themeseList().size():: "+templatesInfoList.get(i).get_themeseList().size()+
                                " get_categoryName:: "+templatesInfoList.get(i).get_categoryName() +" themeName:: "+templatesInfoList.get(i).get_themeseList().get(j).themeName);

                        Log.d("TemplatePickerV2","RemoveTemplate Before get_themeseList jth Position:: "+j);

                        Log.d("TemplatePickerV2","RemoveTemplate Before get_themeseList themeName:: "+templatesInfoList.get(i).get_themeseList().get(j).themeName);

                        if (theme.themeName.equalsIgnoreCase(templatesInfoList.get(i).get_themeseList().get(j).themeName)) {
                            templatesInfoList.get(i).get_themeseList().remove(j);
                            if (_isSearchEnabled) {
                                refreshUiFromSearch();
                            } else {
                                ftTemplateCategoryAdapter.notifyDataSetChanged();
                            }
                        }

                        Log.d("TemplatePickerV2","RemoveTemplate After get_themeseList().size():: "+templatesInfoList.get(i).get_themeseList().size()+" get_categoryName:: "+templatesInfoList.get(i).get_categoryName());
                        if (templatesInfoList.get(i).get_themeseList().size() == 0) {
                            templatesInfoList.remove(i);
                            ftTemplateCategoryAdapter.notifyDataSetChanged();
                            mTemplateTopbarCategoriesAdapter.notifyDataSetChanged();
                        }
                    }
                }
            }

            for (int i=templatesInfoList.size()-1;i>=0;i--) {
                Log.d("TemplatePickerV2","RemoveTemplate Mani get_categoryName:: "+templatesInfoList.get(i).get_categoryName()+" status:: "+
                        (templatesInfoList.get(i).get_categoryName().toLowerCase().contains("recent")));

                if (templatesInfoList.get(i).get_categoryName().toLowerCase().contains("recent")) {
                    for (int j=templatesInfoList.get(i).get_themeseList().size()-1;j>=0;j--) {
                        Log.d("TemplatePickerV2","RemoveTemplate Mani status:: "+(theme.themeName.equalsIgnoreCase(templatesInfoList.get(i).get_themeseList().get(j).themeName))+
                                " themeName:: "+theme.themeName +" Themename inside List:: "+(templatesInfoList.get(i).get_themeseList().get(j).themeName));
                        if (theme.themeName.equalsIgnoreCase(templatesInfoList.get(i).get_themeseList().get(j).themeName)) {
                            templatesInfoList.get(i).get_themeseList().remove(j);
                            if (_isSearchEnabled) {
                                refreshUiFromSearch();
                                if (_isSearchEnabled) {
                                    refreshUiFromSearch();
                                } else {
                                    ftTemplateCategoryAdapter.notifyDataSetChanged();
                                }
                            }
                        }
                    }

                    if (templatesInfoList.get(i).get_themeseList().size() == 0) {
                        templatesInfoList.remove(i);
                        ftTemplateCategoryAdapter.notifyDataSetChanged();
                        mTemplateTopbarCategoriesAdapter.notifyDataSetChanged();
                    }
                }
            }

            //Updating Recents List
            for (int i=0;i<templatesInfoList.size();i++) {
                boolean _recentsExists = templatesInfoList.stream()
                        .anyMatch(p -> p.get_categoryName().toLowerCase().contains("recent"));
                Log.d("TemplatePickerV2","RemoveTemplate _recentsExists:: "
                        +_recentsExists);
                if (!_recentsExists) {
                    FTTemplateUtil.getInstance().updateRecentCoversThemesListDummy(null);
                } else {
                    if (templatesInfoList.get(i).get_categoryName().toLowerCase().contains("recent")) {

                        ArrayList<RecentsInfoModel> recentsThemesList = new ArrayList<>();
                        Log.d("TemplatePickerV2","RemoveTemplate recent Updating Recents List get_themeseList().size():: "
                                +templatesInfoList.get(i).get_themeseList().size());

                        for (int j=0;j<templatesInfoList.get(i).get_themeseList().size();j++) {
                            Log.d("TemplatePickerV2","RemoveTemplate recent Before get_themeseList().size():: "
                                    +templatesInfoList.get(i).get_themeseList().size() +" ith Position "+i+" jth Position:: "+j);
                            RecentsInfoModel _updatedRecents = new RecentsInfoModel();
                            _updatedRecents.set_categoryName("Recent");
                            _updatedRecents.set_packName(templatesInfoList.get(i).get_themeseList().get(j).packName);
                            _updatedRecents.setThemeBgClr(templatesInfoList.get(i).get_themeseList().get(j).themeBgClr);
                            _updatedRecents.setThemeBgClrName(templatesInfoList.get(i).get_themeseList().get(j).themeBgClrName);
                            _updatedRecents.setHorizontalLineColor(templatesInfoList.get(i).get_themeseList().get(j).horizontalLineColor);
                            _updatedRecents.setVerticalLineColor(templatesInfoList.get(i).get_themeseList().get(j).verticalLineColor);
                            _updatedRecents.setVerticalSpacing(templatesInfoList.get(i).get_themeseList().get(j).verticalSpacing);
                            _updatedRecents.setHorizontalSpacing(templatesInfoList.get(i).get_themeseList().get(j).horizontalSpacing);
                            _updatedRecents.setWidth(templatesInfoList.get(i).get_themeseList().get(j).width);
                            _updatedRecents.setHeight(templatesInfoList.get(i).get_themeseList().get(j).height);
                            _updatedRecents.set_packName(templatesInfoList.get(i).get_themeseList().get(j).packName);
                            _updatedRecents.set_themeName(templatesInfoList.get(i).get_themeseList().get(j).themeName);
                            _updatedRecents.setLandscape(templatesInfoList.get(i).get_themeseList().get(j).isLandscape);
                            _updatedRecents.set_thumbnailURLPath(templatesInfoList.get(i).get_themeseList().get(j).thumbnailURLPath);
                            _updatedRecents.set_themeBitmapInStringFrmt(FTTemplateUtil.getInstance().BitMapToString(templatesInfoList.get(i).get_themeseList().get(j).bitmap));

                            recentsThemesList.add(_updatedRecents);
                        }

                        boolean _recentsThemesListIsNullOrEmpty =  FTTemplateUtil.getInstance().isNullOrEmpty(recentsThemesList);
                        if (!_recentsThemesListIsNullOrEmpty) {
                            FTTemplateUtil.getInstance().updateRecentCoversThemesListDummy(recentsThemesList);
                        } else {
                            FTTemplateUtil.getInstance().updateRecentCoversThemesListDummy(null);
                        }

                    }
                }

            }

        }
    }

    private void removePaperTemplate(FTNTheme theme) {

        if (theme.getCategoryName().toLowerCase().contains("recent")) {
            ArrayList<RecentsInfoModel> recentsThemesList = new ArrayList<>();
            for (int i=templatesInfoList.size()-1;i>=0;i--) {
                if (templatesInfoList.get(i).get_categoryName().toLowerCase().contains("recent")) {
;                    for (int j=templatesInfoList.get(i).get_themeseList().size()-1;j>=0;j--) {

                        if (theme.thumbnailURLPath.equalsIgnoreCase(templatesInfoList.get(i).get_themeseList().get(j).thumbnailURLPath)) {
                            templatesInfoList.get(i).get_themeseList().remove(j);
                        }

                        Log.d("TemplatePickerV2","RemoveTemplate recent Before get_themeseList().size():: "+templatesInfoList.get(i).get_themeseList().size());
                        if (templatesInfoList.get(i).get_themeseList().size() == 0) {
                            templatesInfoList.remove(i);
                            ftTemplateCategoryAdapter.notifyDataSetChanged();
                            mTemplateTopbarCategoriesAdapter.notifyDataSetChanged();
                            FTTemplateUtil.getInstance().updateRecentPapersThemesListDummy(null);
                        }
                    }

                }
            }

            //Updating Recents List
            for (int i=0;i<templatesInfoList.size();i++) {
                if (templatesInfoList.get(i).get_categoryName().toLowerCase().contains("recent")) {
                    Log.d("TemplatePickerV2","RemoveTemplate recent Updating Recents List get_themeseList().size():: "
                            +templatesInfoList.get(i).get_themeseList().size());

                    for (int j=0;j<templatesInfoList.get(i).get_themeseList().size();j++) {
                        Log.d("TemplatePickerV2","RemoveTemplate recent Before get_themeseList().size():: "
                                +templatesInfoList.get(i).get_themeseList().size() +" ith Position "+i+" jth Position:: "+j);

                        RecentsInfoModel _updatedRecents = new RecentsInfoModel();
                        _updatedRecents.set_categoryName("Recent");
                        _updatedRecents.set_packName(templatesInfoList.get(i).get_themeseList().get(j).packName);
                        _updatedRecents.setThemeBgClr(templatesInfoList.get(i).get_themeseList().get(j).themeBgClr);
                        _updatedRecents.setThemeBgClrName(templatesInfoList.get(i).get_themeseList().get(j).themeBgClrName);
                        _updatedRecents.setHorizontalLineColor(templatesInfoList.get(i).get_themeseList().get(j).horizontalLineColor);
                        _updatedRecents.setVerticalLineColor(templatesInfoList.get(i).get_themeseList().get(j).verticalLineColor);
                        _updatedRecents.setVerticalSpacing(templatesInfoList.get(i).get_themeseList().get(j).verticalSpacing);
                        _updatedRecents.setHorizontalSpacing(templatesInfoList.get(i).get_themeseList().get(j).horizontalSpacing);
                        _updatedRecents.setWidth(templatesInfoList.get(i).get_themeseList().get(j).width);
                        _updatedRecents.setHeight(templatesInfoList.get(i).get_themeseList().get(j).height);
                        _updatedRecents.set_packName(templatesInfoList.get(i).get_themeseList().get(j).packName);
                        _updatedRecents.set_themeName(templatesInfoList.get(i).get_themeseList().get(j).themeName);
                        _updatedRecents.setLandscape(templatesInfoList.get(i).get_themeseList().get(j).isLandscape);
                        _updatedRecents.set_thumbnailURLPath(templatesInfoList.get(i).get_themeseList().get(j).thumbnailURLPath);
                        _updatedRecents.set_themeBitmapInStringFrmt(FTTemplateUtil.getInstance().BitMapToString(templatesInfoList.get(i).get_themeseList().get(j).bitmap));

                        recentsThemesList.add(_updatedRecents);
                    }

                    boolean _recentsThemesListIsNullOrEmpty =  FTTemplateUtil.getInstance().isNullOrEmpty(recentsThemesList);
                    if (!_recentsThemesListIsNullOrEmpty) {
                        FTTemplateUtil.getInstance().updateRecentPapersThemesListDummy(recentsThemesList);
                    } else {
                        FTTemplateUtil.getInstance().updateRecentPapersThemesListDummy(null);
                    }
                }
            }
        } else {
            theme.deleteTemplate();
            Log.d("TemplatePickerV2","RemoveTemplate Else templatesInfoList().size():: "+templatesInfoList.size());
            boolean _isSearchEnabled = FTApp.getPref().get(SystemPref.SEARCH_ENABLED, false);

            for (int i=templatesInfoList.size()-1;i>=0;i--) {
                Log.d("TemplatePickerV2","RemoveTemplate Else get_themeseList().size():: "+templatesInfoList.get(i).get_themeseList().size()
                +" ith Loop "+i);
                if (!templatesInfoList.get(i).get_categoryName().toLowerCase().contains("recent")) {
                    for (int j=templatesInfoList.get(i).get_themeseList().size()-1;j>=0;j--) {
                        Log.d("TemplatePickerV2","RemoveTemplate Before get_themeseList().size():: "+templatesInfoList.get(i).get_themeseList().size()+
                                " get_categoryName:: "+templatesInfoList.get(i).get_categoryName() +" themeName:: "+templatesInfoList.get(i).get_themeseList().get(j).themeName);

                        Log.d("TemplatePickerV2","RemoveTemplate Before get_themeseList jth Position:: "+j);

                        Log.d("TemplatePickerV2","RemoveTemplate Before get_themeseList themeName:: "+templatesInfoList.get(i).get_themeseList().get(j).themeName);

                        if (theme.themeName.equalsIgnoreCase(templatesInfoList.get(i).get_themeseList().get(j).themeName)) {
                            templatesInfoList.get(i).get_themeseList().remove(j);
                            if (_isSearchEnabled) {
                                refreshUiFromSearch();
                            } else {
                                ftTemplateCategoryAdapter.notifyDataSetChanged();
                            }
                        }

                        Log.d("TemplatePickerV2","RemoveTemplate After get_themeseList().size():: "+templatesInfoList.get(i).get_themeseList().size()+" get_categoryName:: "+templatesInfoList.get(i).get_categoryName());
                        if (templatesInfoList.get(i).get_themeseList().size() == 0) {
                            templatesInfoList.remove(i);
                            ftTemplateCategoryAdapter.notifyDataSetChanged();
                            mTemplateTopbarCategoriesAdapter.notifyDataSetChanged();
                        }
                    }
                }
            }

            for (int i=templatesInfoList.size()-1;i>=0;i--) {
                Log.d("TemplatePickerV2","RemoveTemplate Mani get_categoryName:: "+templatesInfoList.get(i).get_categoryName()+" status:: "+
                        (templatesInfoList.get(i).get_categoryName().toLowerCase().contains("recent")));

                if (templatesInfoList.get(i).get_categoryName().toLowerCase().contains("recent")) {
                        for (int j=templatesInfoList.get(i).get_themeseList().size()-1;j>=0;j--) {
                            Log.d("TemplatePickerV2","RemoveTemplate Mani status:: "+(theme.themeName.equalsIgnoreCase(templatesInfoList.get(i).get_themeseList().get(j).themeName))+
                                    " themeName:: "+theme.themeName +" Themename inside List:: "+(templatesInfoList.get(i).get_themeseList().get(j).themeName));
                            if (theme.themeName.equalsIgnoreCase(templatesInfoList.get(i).get_themeseList().get(j).themeName)) {
                                templatesInfoList.get(i).get_themeseList().remove(j);

                                if (_isSearchEnabled) {
                                    refreshUiFromSearch();
                                } else {
                                    ftTemplateCategoryAdapter.notifyDataSetChanged();
                                }
                            }

                            if (templatesInfoList.get(i).get_themeseList().size() == 0) {
                                templatesInfoList.remove(i);
                                ftTemplateCategoryAdapter.notifyDataSetChanged();
                                mTemplateTopbarCategoriesAdapter.notifyDataSetChanged();
                            }
                        }
                    }
            }

            //Updating Recents List
            for (int i=0;i<templatesInfoList.size();i++) {
                boolean _recentsExists = templatesInfoList.stream()
                        .anyMatch(p -> p.get_categoryName().toLowerCase().contains("recent"));
                Log.d("TemplatePickerV2","RemoveTemplate _recentsExists:: "
                        +_recentsExists);
                if (!_recentsExists) {
                    FTTemplateUtil.getInstance().updateRecentPapersThemesListDummy(null);
                } else {
                    if (templatesInfoList.get(i).get_categoryName().toLowerCase().contains("recent")) {

                        ArrayList<RecentsInfoModel> recentsThemesList = new ArrayList<>();
                        Log.d("TemplatePickerV2","RemoveTemplate recent Updating Recents List get_themeseList().size():: "
                                +templatesInfoList.get(i).get_themeseList().size());

                        for (int j=0;j<templatesInfoList.get(i).get_themeseList().size();j++) {
                            Log.d("TemplatePickerV2","RemoveTemplate recent Before get_themeseList().size():: "
                                    +templatesInfoList.get(i).get_themeseList().size() +" ith Position "+i+" jth Position:: "+j);
                            RecentsInfoModel _updatedRecents = new RecentsInfoModel();
                            _updatedRecents.set_categoryName("Recent");
                            _updatedRecents.set_packName(templatesInfoList.get(i).get_themeseList().get(j).packName);
                            _updatedRecents.setThemeBgClr(templatesInfoList.get(i).get_themeseList().get(j).themeBgClr);
                            _updatedRecents.setThemeBgClrName(templatesInfoList.get(i).get_themeseList().get(j).themeBgClrName);
                            _updatedRecents.setHorizontalLineColor(templatesInfoList.get(i).get_themeseList().get(j).horizontalLineColor);
                            _updatedRecents.setVerticalLineColor(templatesInfoList.get(i).get_themeseList().get(j).verticalLineColor);
                            _updatedRecents.setVerticalSpacing(templatesInfoList.get(i).get_themeseList().get(j).verticalSpacing);
                            _updatedRecents.setHorizontalSpacing(templatesInfoList.get(i).get_themeseList().get(j).horizontalSpacing);
                            _updatedRecents.setWidth(templatesInfoList.get(i).get_themeseList().get(j).width);
                            _updatedRecents.setHeight(templatesInfoList.get(i).get_themeseList().get(j).height);
                            _updatedRecents.set_packName(templatesInfoList.get(i).get_themeseList().get(j).packName);
                            _updatedRecents.set_themeName(templatesInfoList.get(i).get_themeseList().get(j).themeName);
                            _updatedRecents.setLandscape(templatesInfoList.get(i).get_themeseList().get(j).isLandscape);
                            _updatedRecents.set_thumbnailURLPath(templatesInfoList.get(i).get_themeseList().get(j).thumbnailURLPath);
                            _updatedRecents.set_themeBitmapInStringFrmt(FTTemplateUtil.getInstance().BitMapToString(templatesInfoList.get(i).get_themeseList().get(j).bitmap));

                            recentsThemesList.add(_updatedRecents);
                        }

                        boolean _recentsThemesListIsNullOrEmpty =  FTTemplateUtil.getInstance().isNullOrEmpty(recentsThemesList);
                        if (!_recentsThemesListIsNullOrEmpty) {
                            FTTemplateUtil.getInstance().updateRecentPapersThemesListDummy(recentsThemesList);
                        } else {
                            FTTemplateUtil.getInstance().updateRecentPapersThemesListDummy(null);
                        }

                    }
                }

            }

        }
    }

    private void refreshUiFromSearch() {
        getFilter().filter(tempString);
    }

    @Override
    public void thumbGenCallBack() {
        Log.d("TemplatePicker==>","FTTemplateCategories::-notifyDataSetChanged thumbGenCallBack-");
        if (ftTemplateCategoryAdapter != null) {
            ftTemplateCategoryAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void showClub() {
        ftChoosePaperTemplate.showClub();
    }

    interface TemplateSelctedInfo {
        public void onTemplateSelctedInfo(FTNTheme theme,boolean isLandscapeStatus);
    }

    @Override
    public void onDestroy() {
        ObservingService.getInstance().removeObserver("newDeviceSelected", selectedDeviceSpinnerObserver);
        super.onDestroy();
    }

}
