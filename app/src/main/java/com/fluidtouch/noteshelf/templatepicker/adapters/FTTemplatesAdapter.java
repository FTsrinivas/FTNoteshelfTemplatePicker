package com.fluidtouch.noteshelf.templatepicker.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf.models.theme.FTNPaperTheme;
import com.fluidtouch.noteshelf.models.theme.FTNTheme;
import com.fluidtouch.noteshelf.models.theme.FTNThemeCategory;
import com.fluidtouch.noteshelf.preferences.SystemPref;
import com.fluidtouch.noteshelf.store.model.FTStoreMetadata;
import com.fluidtouch.noteshelf.store.ui.FTStoreActivity;
import com.fluidtouch.noteshelf.templatepicker.common.FTTemplatesInfoSingleton;
import com.fluidtouch.noteshelf.templatepicker.common.plistdatamodel.FTSelectedDeviceInfo;
import com.fluidtouch.noteshelf.templatepicker.common.plistdatamodel.FTTemplateColors;
import com.fluidtouch.noteshelf.templatepicker.common.util.FTTemplateUtil;
import com.fluidtouch.noteshelf.templatepicker.common.modelclasses.FTUserSelectedTemplateInfo;
import com.fluidtouch.noteshelf.templatepicker.common.plistdatamodel.FTCategories;
import com.fluidtouch.noteshelf.templatepicker.interfaces.AddCustomThemeListener;
import com.fluidtouch.noteshelf.templatepicker.interfaces.MoreColorsViewInterface;
import com.fluidtouch.noteshelf.templatepicker.interfaces.TemplateBackgroundListener;
import com.fluidtouch.noteshelf.templatepicker.interfaces.ThumGenCallBack;
import com.fluidtouch.noteshelf.templatepicker.interfaces.ThumbnailGenCallBack;
import com.fluidtouch.noteshelf.templatepicker.models.TemplatesInfoModel;
import com.fluidtouch.noteshelf2.R;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.shape.CornerFamily;
import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ir.mahdi.mzip.rar.io.IReadOnlyAccess;

public class FTTemplatesAdapter extends
        RecyclerView.Adapter<FTTemplatesAdapter.TemplatesViewHolder>
        implements ThumbnailGenCallBack {

    ArrayList<TemplatesInfoModel> ftTemplateCategoryInfoArrayList;
    ArrayList<TemplatesInfoModel> ftTemplateCategoryInfoArrayListFiltered;

    ArrayList<FTCategories> ftTemplateCategoryInfoArrayListBckup = new ArrayList<>();
    ArrayList<String> categoryNameList = new ArrayList<>();
    String typeOfLayout;
    private MoreColorsViewInterface moreColorsViewInterface;
    private TemplateBackgroundListener.TemplateInfoRequest templateInfoRequest;
    private TemplateBackgroundListener.CallBackToShowClub callBackToShowClub;
    private AddCustomThemeListener addCustomThemeListener;
    private ThumGenCallBack mThumGenCallBack;
    int[] screen = new int[2];
    TemplatesViewHolder mParentViewHolder;
    boolean moreClrViewSelected = false;
    FTTemplateUtil ftTemplateUtil;
    FTTemplateDetailedInfoAdapter childItemAdapter;

    RecyclerView templateItemsRecyclerview;
    FragmentManager childFragmentManager;
    FTTemplateColors ftTemplateColorsInfo;
    String currentSelectedView = "view1";
    boolean moreClrViewSelectionStatus;
    boolean recentSelection;
    int[] location = new int[2];
    ArrayList<Boolean> newlyDownloadedList;
    ArrayList<String> categoriesNamesList = new ArrayList<>();

    FTNThemeCategory.FTThemeType ftnThemeType;
    Gson recentGson = new Gson();
    ArrayList<FTCategories> ftCategoriesArrayListB = null;
    String recentJson;
    FTNThemeCategory.FTThemeType _themeType;
    ArrayList<TemplatesInfoModel> mOriginalValues = null; // Original Values
    private ArrayList<TemplatesInfoModel> mDisplayedValues;    // Values to be displayed
    public FTTemplatesAdapter(ArrayList<TemplatesInfoModel> ftTemplateCategoryInfoArrayList,
                              MoreColorsViewInterface moreColorsViewInterface,
                              TemplateBackgroundListener.TemplateInfoRequest templateInfoRequest,
                              TemplateBackgroundListener.CallBackToShowClub callBackToShowClub,
                              String typeOfLayout, AddCustomThemeListener addCustomThemeListener,
                              ThumGenCallBack mThumGenCallBack, FragmentManager childFragmentManager,
                              RecyclerView templateItemsRecyclerview, ArrayList<Boolean> newlyDownloadedList, FTNThemeCategory.FTThemeType themeType) {
        this.ftTemplateCategoryInfoArrayListBckup = new ArrayList<>();

        this.ftTemplateCategoryInfoArrayList = ftTemplateCategoryInfoArrayList;
        this.ftTemplateCategoryInfoArrayListFiltered = ftTemplateCategoryInfoArrayList;


        this.templateItemsRecyclerview = templateItemsRecyclerview;
        this.moreColorsViewInterface = moreColorsViewInterface;
        this.addCustomThemeListener = addCustomThemeListener;
        this.childFragmentManager = childFragmentManager;
        this.templateInfoRequest = templateInfoRequest;
        this.callBackToShowClub = callBackToShowClub;
        this.newlyDownloadedList = newlyDownloadedList;
        this.mThumGenCallBack = mThumGenCallBack;
        this.typeOfLayout = typeOfLayout;
        ftTemplateUtil = FTTemplateUtil.getInstance();

        _themeType = themeType;
        this.mOriginalValues = ftTemplateCategoryInfoArrayList;
        this.mDisplayedValues = ftTemplateCategoryInfoArrayList;

    }

    @NonNull
    @Override
    public TemplatesViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        // Here we inflate the corresponding layout of the parent item
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.template_categories_list_items_lyt_new, viewGroup, false);
        return new TemplatesViewHolder(view, viewGroup.getContext());
    }

    @Override
    public void onBindViewHolder(@NonNull TemplatesViewHolder parentViewHolder, int position) {
        Log.d("TemplatePicker==>", "onBindViewHolder list size:: " + ftTemplateCategoryInfoArrayList.size() +" typeOfLayout:: "+typeOfLayout);
        categoryNameList.clear();
        mParentViewHolder = parentViewHolder;
        String parentItem = ftTemplateCategoryInfoArrayList.get(position).get_categoryName();
        ArrayList<FTNTheme> ftnThemeArrayList = ftTemplateCategoryInfoArrayList.get(position).get_themeseList();
        parentViewHolder.templateCategoryTitle.setText(parentItem);
        LinearLayoutManager layoutManager = new LinearLayoutManager(parentViewHolder.templatesRecyclerView.getContext(),
                LinearLayoutManager.HORIZONTAL, false);

        Log.d("TemplatePicker==>", " FTTemplatesAdapter onBindViewHolder FTTemplateCategories FTTemplateDetailedInfoAdapter::- categoryName:-" + parentItem);

        categoriesNamesList.add(parentItem);
        parentViewHolder.templatesRecyclerView.getLocationOnScreen(screen);

        categoryNameList.add(parentItem);
        String keyExists = FTApp.getPref().get(parentItem, null);
        Log.d("TemplatePicker==>", " FTTemplatesAdapter onBindViewHolder keyExists::-" + keyExists + " parentItem::-" + parentItem);
        if (keyExists != null) {

            try {

                Date today = new Date();
                SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
                String dateToStr = format.format(today);

                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

                Date currentDate = sdf.parse(dateToStr);
                Date downloadedDate = sdf.parse(keyExists);

                long difference_In_Time = currentDate.getTime() - downloadedDate.getTime();

                int diffInDays = (int) ((currentDate.getTime() - downloadedDate.getTime()) / (1000 * 60 * 60 * 24));
                long difference_In_Days = (difference_In_Time / (1000 * 60 * 60 * 24)) % 365;
                long difference_In_Minutes = (difference_In_Time / (1000 * 60)) % 60;

                Log.d("TemplatePicker==>", "Date::- FTTemplatesAdapter onBindViewHolder " +
                        "FTTemplateCategories FTTemplateDetailedInfoAdapter::- diffInDays:-" + diffInDays +
                        " difference_In_Minutes::-" + difference_In_Minutes +
                        " downloadedDate::-" + downloadedDate + " currentDate::-" + currentDate +
                        " dateToStr::-" + dateToStr + " keyExists::-" + keyExists);

                if (difference_In_Minutes < 2) {
                    Log.d("TemplatePicker==>", "Date::- newDownloadedPillTag Pill Visibility Visible ");
                    parentViewHolder.newDownloadedPillTag.setVisibility(View.VISIBLE);
                } else {
                    Log.d("TemplatePicker==>", "Date::- newDownloadedPillTag Pill Visibility GONE ");
                    FTApp.getPref().save(parentItem, null);
                    parentViewHolder.newDownloadedPillTag.setVisibility(View.GONE);
                }

            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else {
            parentViewHolder.newDownloadedPillTag.setVisibility(View.GONE);
        }

        currentSelectedView = FTApp.getPref().get(SystemPref.TYPE_OF_CLR_VIEW_SELECTED, "view1");
        moreClrViewSelectionStatus = FTApp.getPref().get(SystemPref.TEMPLATE_BG_CLR_MORE_POPUP_SELECTION_STATUS, false);

        if (moreClrViewSelectionStatus) {
            parentViewHolder.bgShape1.setStroke(2, Color.parseColor("#33000000"));
            parentViewHolder.bgShape2.setStroke(4, Color.parseColor("#1D232F"));
            parentViewHolder.bgShape3.setStroke(4, Color.parseColor("#5377F8"));
        } else if (currentSelectedView.equalsIgnoreCase("view1")) {
            parentViewHolder.bgShape1.setStroke(4, Color.parseColor("#5377F8"));
            parentViewHolder.bgShape2.setStroke(4, Color.parseColor("#1D232F"));
            parentViewHolder.bgShape3.setStroke(2, Color.parseColor("#33000000"));
        } else if (currentSelectedView.equalsIgnoreCase("view2")) {
            parentViewHolder.bgShape1.setStroke(2, Color.parseColor("#33000000"));
            parentViewHolder.bgShape2.setStroke(4, Color.parseColor("#5377F8"));
            parentViewHolder.bgShape3.setStroke(2, Color.parseColor("#33000000"));
        }

        if (ftnThemeArrayList != null) {
            if (parentItem != null) {
                if (parentItem.equalsIgnoreCase(parentViewHolder.mContext.getString(R.string.template_basic))) {
                    if (_themeType == FTNThemeCategory.FTThemeType.COVER) {
                        parentViewHolder.templateColourSelctionLyt.setVisibility(View.GONE);
                    } else {
                        parentViewHolder.templateColourSelctionLyt.setVisibility(View.VISIBLE);
                    }
                } else {
                    parentViewHolder.templateColourSelctionLyt.setVisibility(View.GONE);
                }
            }

            if (moreClrViewSelectionStatus) {
                parentViewHolder.bgShape3.setColor(Color.parseColor
                        (FTApp.getPref().get(SystemPref.TEMPLATE_BG_CLR_MORE_VIEW, "#FFFED6-1.0").substring(0, 7)));
            }
            boolean _searchEnabled = FTApp.getPref().get(SystemPref.SEARCH_ENABLED, false);
            if (parentItem.equalsIgnoreCase("Recent")) {
                if (!ftnThemeArrayList.isEmpty()) {
                    parentViewHolder.templateCategoryTitle.setVisibility(View.VISIBLE);
                    recentSelection = true;
                    if (_searchEnabled) {
                        parentViewHolder.freeDownloadsBanner.setVisibility(View.GONE);
                    } else {
                        parentViewHolder.freeDownloadsBanner.setVisibility(View.VISIBLE);
                    }
                } else {
                    parentViewHolder.templateCategoryTitle.setVisibility(View.GONE);
                    recentSelection = false;
                    parentViewHolder.freeDownloadsBanner.setVisibility(View.GONE);
                }
            } else if ((parentItem.equalsIgnoreCase("Basic")) || (parentItem.equalsIgnoreCase(parentViewHolder.mContext.getString(R.string.template_simple)))) {
                parentViewHolder.templateCategoryTitle.setVisibility(View.VISIBLE);
                if (recentSelection) {
                    parentViewHolder.freeDownloadsBanner.setVisibility(View.GONE);
                } else {
                    if (_searchEnabled) {
                        parentViewHolder.freeDownloadsBanner.setVisibility(View.GONE);
                    } else {
                        parentViewHolder.freeDownloadsBanner.setVisibility(View.VISIBLE);
                    }
                }
            } else {
                parentViewHolder.templateCategoryTitle.setVisibility(View.VISIBLE);
                parentViewHolder.templateCategoryTitle.setText(parentItem);
                recentSelection = false;
                parentViewHolder.freeDownloadsBanner.setVisibility(View.GONE);
            }

            ftTemplateUtil = FTTemplateUtil.getInstance();
            ftTemplateColorsInfo = ftTemplateUtil.getFtTemplateColorsObj();

            childItemAdapter = new FTTemplateDetailedInfoAdapter(ftnThemeArrayList, typeOfLayout,
                    addCustomThemeListener, this, childFragmentManager,_themeType,parentItem);
            parentViewHolder.templatesRecyclerView.setRecycledViewPool(new RecyclerView.RecycledViewPool());
            parentViewHolder.templatesRecyclerView.setAdapter(childItemAdapter);
            parentViewHolder.templatesRecyclerView.setLayoutManager(layoutManager);
            parentViewHolder.templatesRecyclerView.setHasFixedSize(true);

        }

        parentViewHolder.selectionColorView1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                moreClrViewSelected = false;
                FTApp.getPref().save(SystemPref.TEMPLATE_COLOR_SELECTED, true);
                FTApp.getPref().save(SystemPref.TYPE_OF_CLR_VIEW_SELECTED, "view1");
                FTApp.getPref().save(SystemPref.TEMPLATE_BG_CLR_MORE_POPUP_SELECTION_STATUS, false);

                FTSelectedDeviceInfo selectedDeviceInfo = FTSelectedDeviceInfo.selectedDeviceInfo();
                FTSelectedDeviceInfo ftSelectedDeviceInfo = new FTSelectedDeviceInfo();

                ftSelectedDeviceInfo.setPageHeight(selectedDeviceInfo.getPageHeight());
                ftSelectedDeviceInfo.setPageWidth(selectedDeviceInfo.getPageWidth());
                ftSelectedDeviceInfo.setLineType(selectedDeviceInfo.getLineType());
                ftSelectedDeviceInfo.setLayoutType(selectedDeviceInfo.getLayoutType());
                ftSelectedDeviceInfo.setVerticalLineSpacing(selectedDeviceInfo.getVerticalLineSpacing());
                ftSelectedDeviceInfo.setHorizontalLineSpacing(selectedDeviceInfo.getHorizontalLineSpacing());
                ftSelectedDeviceInfo.setThemeBgClrName("White");
                ftSelectedDeviceInfo.setThemeBgClrHexCode("#F7F7F2-1.0");
                ftSelectedDeviceInfo.setHorizontalLineClr("#000000-0.15");
                ftSelectedDeviceInfo.setVerticalLineClr("#000000-0.15");
                ftSelectedDeviceInfo.selectSavedDeviceInfo();

                templateInfoRequest.templateBgColourChangedListener();
            }
        });

        parentViewHolder.selectionColorView2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                moreClrViewSelected = false;
                FTApp.getPref().save(SystemPref.TEMPLATE_COLOR_SELECTED, true);
                FTApp.getPref().save(SystemPref.TYPE_OF_CLR_VIEW_SELECTED, "view2");
                FTApp.getPref().save(SystemPref.TEMPLATE_BG_CLR_MORE_POPUP_SELECTION_STATUS, false);

                FTSelectedDeviceInfo selectedDeviceInfo = FTSelectedDeviceInfo.selectedDeviceInfo();
                FTSelectedDeviceInfo ftSelectedDeviceInfo = new FTSelectedDeviceInfo();

                ftSelectedDeviceInfo.setPageHeight(selectedDeviceInfo.getPageHeight());
                ftSelectedDeviceInfo.setPageWidth(selectedDeviceInfo.getPageWidth());
                ftSelectedDeviceInfo.setLineType(selectedDeviceInfo.getLineType());
                ftSelectedDeviceInfo.setLayoutType(selectedDeviceInfo.getLayoutType());
                ftSelectedDeviceInfo.setVerticalLineSpacing(selectedDeviceInfo.getVerticalLineSpacing());
                ftSelectedDeviceInfo.setHorizontalLineSpacing(selectedDeviceInfo.getHorizontalLineSpacing());
                ftSelectedDeviceInfo.setThemeBgClrName("Midnight");
                ftSelectedDeviceInfo.setThemeBgClrHexCode("#1D232F-1.0");
                ftSelectedDeviceInfo.setHorizontalLineClr("#FFFFFF-0.15");
                ftSelectedDeviceInfo.setVerticalLineClr("#FFFFFF-0.15");
                ftSelectedDeviceInfo.selectSavedDeviceInfo();
                Log.d("TemplatePickerV2", " selectionColorView2" + selectedDeviceInfo.getThemeBgClrName()+" ManigetLayoutType:: "+selectedDeviceInfo.getLayoutType());

                templateInfoRequest.templateBgColourChangedListener();
            }
        });

        parentViewHolder.selectionColorView3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                FTApp.getPref().save(SystemPref.TEMPLATE_COLOR_SELECTED, true);
                FTApp.getPref().save(SystemPref.TYPE_OF_CLR_VIEW_SELECTED, "view3");
                FTApp.getPref().save(SystemPref.TEMPLATE_BG_CLR_MORE_POPUP_SELECTION_STATUS, true);
                Log.d("TemplatePicker==>", " moreClrViewSelected" + moreClrViewSelected);
                FTSelectedDeviceInfo selectedDeviceInfo = FTSelectedDeviceInfo.selectedDeviceInfo();
                FTSelectedDeviceInfo ftSelectedDeviceInfo = new FTSelectedDeviceInfo();

                ftSelectedDeviceInfo.setPageHeight(selectedDeviceInfo.getPageHeight());
                ftSelectedDeviceInfo.setPageWidth(selectedDeviceInfo.getPageWidth());
                ftSelectedDeviceInfo.setLineType(selectedDeviceInfo.getLineType());
                ftSelectedDeviceInfo.setLayoutType(selectedDeviceInfo.getLayoutType());

                ftSelectedDeviceInfo.setThemeBgClrName(selectedDeviceInfo.getThemeMoreBgClrName());
                ftSelectedDeviceInfo.setThemeBgClrHexCode(selectedDeviceInfo.getThemeMoreBgClrHexCode());
                ftSelectedDeviceInfo.setHorizontalLineClr(selectedDeviceInfo.getHorizontalMoreLineClr());
                ftSelectedDeviceInfo.setVerticalLineClr(selectedDeviceInfo.getVerticaMorelLineClr());
                ftSelectedDeviceInfo.setVerticalLineSpacing(selectedDeviceInfo.getVerticalLineSpacing());
                ftSelectedDeviceInfo.setHorizontalLineSpacing(selectedDeviceInfo.getHorizontalLineSpacing());

                ftSelectedDeviceInfo.selectSavedDeviceInfo();

                templateInfoRequest.templateBgColourChangedListener();
                childItemAdapter.notifyDataSetChanged();
            }
        });

        parentViewHolder.selectionMoreColorViewFrmLyt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int[] point = new int[2];
                parentViewHolder.templateColourSelctionLyt.getLocationInWindow(point); // or getLocationInWindow(point)
                location[0] = point[0];
                location[1] = point[1];
                moreColorsViewInterface.moreColorsViewSelected(location[0], location[1]);
            }
        });
    }

    public void updateUI(ArrayList<FTCategories> ftTemplateCategoryInfoArrayListR, int pos) {
        Log.d("TemplatePicker==>", "Filtering ftTemplateCategoryInfoArrayListR ::-" + ftTemplateCategoryInfoArrayListR);
        if (ftTemplateCategoryInfoArrayListR != null &&
                !ftTemplateCategoryInfoArrayListR.isEmpty()) {
            notifyItemChanged(pos);
        }
    }

    public void refreshUI(ArrayList<TemplatesInfoModel> ftTemplateCategoryInfoArrayListR) {
        Log.d("TemplatePicker==>", "Filtering ftTemplateCategoryInfoArrayListR ::-" + ftTemplateCategoryInfoArrayListR);
        if (ftTemplateCategoryInfoArrayListR != null &&
                !ftTemplateCategoryInfoArrayListR.isEmpty()) {
           /* ftTemplateCategoryInfoArrayList.clear();
            ftTemplateCategoryInfoArrayList.addAll(ftTemplateCategoryInfoArrayListR);*/
            ftTemplateCategoryInfoArrayList = ftTemplateCategoryInfoArrayListR;

            notifyDataSetChanged();
        }
    }

    public void updateMoreColorViewColorViewBg() {
        if (mParentViewHolder != null) {
            currentSelectedView = FTApp.getPref().get(SystemPref.TYPE_OF_CLR_VIEW_SELECTED, "view1");
            moreClrViewSelectionStatus = FTApp.getPref().get(SystemPref.TEMPLATE_BG_CLR_MORE_POPUP_SELECTION_STATUS, false);

            if (moreClrViewSelectionStatus) {

                FTSelectedDeviceInfo selectedDeviceInfo = FTSelectedDeviceInfo.selectedDeviceInfo();
                FTSelectedDeviceInfo ftSelectedDeviceInfo = new FTSelectedDeviceInfo();

                ftSelectedDeviceInfo.setPageHeight(selectedDeviceInfo.getPageHeight());
                ftSelectedDeviceInfo.setPageWidth(selectedDeviceInfo.getPageWidth());
                ftSelectedDeviceInfo.setLineType(selectedDeviceInfo.getLineType());
                ftSelectedDeviceInfo.setLayoutType(selectedDeviceInfo.getLayoutType());

                ftSelectedDeviceInfo.setThemeBgClrName(selectedDeviceInfo.getThemeMoreBgClrName());
                ftSelectedDeviceInfo.setThemeBgClrHexCode(selectedDeviceInfo.getThemeMoreBgClrHexCode());
                ftSelectedDeviceInfo.setHorizontalLineClr(selectedDeviceInfo.getHorizontalMoreLineClr());
                ftSelectedDeviceInfo.setVerticalLineClr(selectedDeviceInfo.getVerticaMorelLineClr());
                ftSelectedDeviceInfo.setHorizontalLineSpacing(selectedDeviceInfo.getHorizontalLineSpacing());
                ftSelectedDeviceInfo.setVerticalLineSpacing(selectedDeviceInfo.getVerticalLineSpacing());

                Log.d("TemplatePicker==>", " getThemeMoreBgClrName" + selectedDeviceInfo.getThemeMoreBgClrName());

                ftSelectedDeviceInfo.selectSavedDeviceInfo();
            }
            moreClrViewSelected = true;
        }
    }

    @Override
    public int getItemCount() {
        return ftTemplateCategoryInfoArrayList.size();
    }

    public static Point getLocationOnScreen(View view) {
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        return new Point(location[0], location[1]);
    }

    @Override
    public void thumbnailGenCallBack() {
        mThumGenCallBack.thumbGenCallBack();
    }

    public void searchTemplates(String query) {
        Log.d("TemplatePicker==>","FTTemplateCategories::- searchTemplates ::-"+query+" ftTemplateCategoryAdapter:: "+childItemAdapter);
        if (childItemAdapter != null) {
            //childItemAdapter.searchTemplates(query);
            //childItemAdapter.getFilter().filter(query);
        }
    }

    /*This method will filter the  list and will show Categories & Themes that are with searchable keyword */
    private TemplatesInfoModel getFilteredListDummy(TemplatesInfoModel ftCategories, String searchKeyWord, List<TemplatesInfoModel> originalListTemp) {
        TemplatesInfoModel ftCategoriesTemp = ftCategories;
        ArrayList<FTNTheme> ftnThemes = new ArrayList<>();
        if (ftCategoriesTemp.get_categoryName().equalsIgnoreCase("Custom")) {
//            '0' Position has to be removed,cause of static Cover template to select
            if (ftCategoriesTemp.get_themeseList() != null && !ftCategoriesTemp.get_themeseList().isEmpty() && ftCategoriesTemp.get_themeseList().size() > 0) {
                ftCategoriesTemp.get_themeseList().remove(0);
            }
        }

        if (ftCategoriesTemp.get_themeseList() != null) {
            for (int i = 0; i <= ftCategoriesTemp.get_themeseList().size() - 1; i++) {
                String themeName = FTTemplatesInfoSingleton.getInstance().getNSPFileNameWithoutExtn(ftCategoriesTemp.get_themeseList().get(i));
                Log.d("TemplatePicker==>", "search performFiltering performFiltering LOG packName:: "
                        + themeName
                        + " status:: " + searchKeyWord.toLowerCase() + " status:: "
                        + themeName.toLowerCase().contains(searchKeyWord.toLowerCase()));

                if (themeName != null &&
                        themeName.toLowerCase().contains(searchKeyWord.toLowerCase())) {
                    Log.d("TemplatePicker==>", "search performFiltering performFiltering LOG Inside TRUE packName:: " + themeName);
                    ftnThemes.add(ftCategoriesTemp.get_themeseList().get(i));
                }
            }
        }

        if (ftnThemes != null) {
            if (ftnThemes.size() != 0) {
                ftCategoriesTemp.set_themeseList(ftnThemes);
                return ftCategoriesTemp;
            } else {

                return null;
            }


        } else {
            Log.d("TemplatePicker==>", "search performFiltering getCategory_name whose Themes are NULL:: " + ftCategoriesTemp.get_categoryName());
            return null;
        }


    }


    class TemplatesViewHolder extends RecyclerView.ViewHolder {

        private TextView templateCategoryTitle;
        private RecyclerView templatesRecyclerView;
        private RelativeLayout templateColourSelctionLyt;

        private View selectionColorView1;
        private View selectionColorView2;
        private View selectionColorView3;
        //private View selectionMoreColorView;
        private ImageView selectionMoreColorView;

        GradientDrawable bgShape1;
        GradientDrawable bgShape2;
        GradientDrawable bgShape3;

        Context mContext;

        FTUserSelectedTemplateInfo ftUserSelectedTemplateInfo;
        String moreViewDefaultClr = null;

        FrameLayout freeDownloadsBanner;
        FrameLayout selectionMoreColorViewFrmLyt;
        ShapeableImageView clubDownloadsRandomIcon;
        ImageView newDownloadedPillTag;

        TemplatesViewHolder(final View itemView, Context mContext) {
            super(itemView);

            this.mContext = mContext;

            templateCategoryTitle = itemView.findViewById(R.id.template_category_title);
            templatesRecyclerView = itemView.findViewById(R.id.template_items_recyclerview);
            templateColourSelctionLyt = itemView.findViewById(R.id.template_colour_selction_lyt);

            selectionColorView1 = itemView.findViewById(R.id.selectionColorView1);
            selectionColorView2 = itemView.findViewById(R.id.selectionColorView2);
            selectionColorView3 = itemView.findViewById(R.id.selectionColorView3);
            selectionMoreColorView = itemView.findViewById(R.id.selectionMoreColorView);
            freeDownloadsBanner = itemView.findViewById(R.id.free_downloads_banner);
            clubDownloadsRandomIcon = itemView.findViewById(R.id.club_downlaods_random_icon);
            selectionMoreColorViewFrmLyt = itemView.findViewById(R.id.selectionMoreColorViewFrmLyt);

            bgShape1 = (GradientDrawable) selectionColorView1.getBackground();
            bgShape2 = (GradientDrawable) selectionColorView2.getBackground();
            bgShape3 = (GradientDrawable) selectionColorView3.getBackground();

            newDownloadedPillTag = itemView.findViewById(R.id.new_downloaded_pill_tag);

            ftUserSelectedTemplateInfo = new FTUserSelectedTemplateInfo();

            bgShape1.setColor(Color.parseColor("#FFFFFF"));
            bgShape2.setColor(Color.parseColor("#1D232F"));

            String moreViewDefaultClr = FTApp.getPref().get(SystemPref.TEMPLATE_BG_CLR_MORE_VIEW, "#FFFED6-1.0").substring(0, 7);
            if (moreViewDefaultClr.equalsIgnoreCase("#FFFED6")) {
                FTApp.getPref().save(SystemPref.TEMPLATE_BG_CLR_MORE_VIEW, "#FFFED6-1.0");
            } else {
                moreViewDefaultClr = FTApp.getPref().get(SystemPref.TEMPLATE_BG_CLR_MORE_VIEW, "#FFFED6-1.0").substring(0, 7);
            }

            boolean typeOfClrSelectedByUser = FTApp.getPref().get(SystemPref.TEMPLATE_COLOR_SELECTED, false);
            if (typeOfClrSelectedByUser) {
                String viewType = FTApp.getPref().get(SystemPref.TYPE_OF_CLR_VIEW_SELECTED, "view1");

                if (viewType.equalsIgnoreCase("view1")) {
                    bgShape1.setStroke(4, Color.parseColor("#5377F8"));
                    bgShape2.setStroke(4, Color.parseColor("#1D232F"));
                    bgShape3.setStroke(2, Color.parseColor("#33000000"));
                } else if (viewType.equalsIgnoreCase("view2")) {
                    bgShape1.setStroke(2, Color.parseColor("#33000000"));
                    bgShape2.setStroke(4, Color.parseColor("#5377F8"));
                    bgShape3.setStroke(2, Color.parseColor("#33000000"));
                } else if (viewType.equalsIgnoreCase("view3")) {
                    bgShape1.setStroke(2, Color.parseColor("#33000000"));
                    bgShape2.setStroke(4, Color.parseColor("#1D232F"));
                    bgShape3.setStroke(4, Color.parseColor("#5377F8"));
                }

            } else {
                bgShape3.setColor(Color.parseColor(moreViewDefaultClr));
                bgShape1.setStroke(4, Color.parseColor("#5377F8"));
                bgShape2.setStroke(4, Color.parseColor("#1D232F"));
                bgShape3.setStroke(4, Color.parseColor("#00FFFFFF"));
            }

            bgShape3.setColor(Color.parseColor(moreViewDefaultClr));
            float radius = mContext.getResources().getDimension(R.dimen.default_corner_radius);

            clubDownloadsRandomIcon.setShapeAppearanceModel(clubDownloadsRandomIcon.getShapeAppearanceModel()
                    .toBuilder()
                    .setBottomLeftCorner(CornerFamily.ROUNDED, radius)
                    .setTopLeftCorner(CornerFamily.ROUNDED, radius)
                    .build());

            freeDownloadsBanner.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    FTStoreActivity.start(mContext);
                }
            });

        }
    }
}
