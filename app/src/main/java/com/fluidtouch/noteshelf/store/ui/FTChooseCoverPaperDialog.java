package com.fluidtouch.noteshelf.store.ui;

import android.content.res.AssetManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf.commons.FTLog;
import com.fluidtouch.noteshelf.commons.ui.FTBaseDialog;
import com.fluidtouch.noteshelf.commons.utils.FTAnimationUtils;
import com.fluidtouch.noteshelf.commons.utils.FileUriUtils;
import com.fluidtouch.noteshelf.commons.utils.ObservingService;
import com.fluidtouch.noteshelf.documentframework.FTUrl;
import com.fluidtouch.noteshelf.documentframework.Utilities.FTConstants;
import com.fluidtouch.noteshelf.models.theme.FTNTheme;
import com.fluidtouch.noteshelf.models.theme.FTNThemeCategory;
import com.fluidtouch.noteshelf.preferences.SystemPref;
import com.fluidtouch.noteshelf.shelf.activities.FTGridLayoutManager;
import com.fluidtouch.noteshelf.shelf.fragments.FTQuickCreateSettingsPopup;
import com.fluidtouch.noteshelf.store.adapter.FTChooseCoverPaperAdapter;
import com.fluidtouch.noteshelf.store.adapter.FTChooseCoverPaperCategoryAdapter;
import com.fluidtouch.noteshelf.store.data.FTStorePackData;
import com.fluidtouch.noteshelf.store.model.FTStoreCategories;
import com.fluidtouch.noteshelf.store.model.FTStoreCategoryItem;
import com.fluidtouch.noteshelf.templatepicker.common.FTTemplatesInfoSingleton;
import com.fluidtouch.noteshelf2.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.stream.Collectors;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FTChooseCoverPaperDialog extends FTBaseDialog
        implements FTChooseCoverPaperAdapter.ChooseCoverPaperItemAdapterListener,
        FTChooseCoverPaperCategoryAdapter.ChooseCoverPaperCategoryAdapterCallback {
    @BindView(R.id.choose_cover_paper_category_recyclerview)
    RecyclerView categoryRecyclerView;
    @BindView(R.id.choose_cover_paper_item_recyclerview)
    RecyclerView templateItemRecyclerView;
    @BindView(R.id.choose_cover_paper_orientation_layout)
    LinearLayout paperOrientationLayout;
    @BindView(R.id.choose_cover_paper_portrait_text_view)
    TextView portraitTextView;
    @BindView(R.id.choose_cover_paper_landscape_text_view)
    TextView landscapeTextView;
    @BindView(R.id.choose_cover_paper_error)
    TextView errorTextView;
    @BindView(R.id.choose_cover_paper_templates_layout)
    RelativeLayout templatesLayout;
    @BindView(R.id.choose_cover_paper_dialog_title)
    TextView dialogTitle;
    @BindView(R.id.choose_cover_paper_close_button)
    ImageView mCloseButton;

    private LinkedHashMap<FTNTheme, List<FTNTheme>> themesByCategory = new LinkedHashMap<>();
    private FTNThemeCategory.FTThemeType themeType = FTNThemeCategory.FTThemeType.PAPER;
    private FTChooseCoverPaperCategoryAdapter themeCategoryAdapter;
    private FTChooseCoverPaperAdapter themeAdapter;
    private FTNTheme currentCategory;
    private boolean isLandscape = false;
    private CoverChooseListener listener;
    private boolean isCurrentPage;
    private Observer addStoreThemeObserver = (observable, o) -> {
        if (isVisible())
            updateCategories();
    };

    private Observer addCustomThemeObserver = new Observer() {
        @Override
        public void update(Observable o, Object arg) {
            FTNTheme addedTheme = (FTNTheme) arg;
            if (isVisible() && addedTheme != null && addedTheme.ftThemeType == themeType && addedTheme.themeThumbnail(getContext()) != null) {
                updateCategories();
                listener.onThemeChosen(addedTheme, isCurrentPage,addedTheme.isLandscape);
                dismissAllowingStateLoss();
            }
        }
    };

    public static FTChooseCoverPaperDialog newInstance(FTNThemeCategory.FTThemeType themeType) {
        FTChooseCoverPaperDialog chooseCoverPaperDialog = new FTChooseCoverPaperDialog();
        chooseCoverPaperDialog.themeType = themeType;
        return chooseCoverPaperDialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_choose_cover_paper, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        if (getParentFragment() != null) {
            listener = (CoverChooseListener) getParentFragment();
            isCurrentPage = listener.isCurrentTheme();
            if (getParentFragment() instanceof FTQuickCreateSettingsPopup) {
                view.clearAnimation();
                mCloseButton.setImageResource(R.drawable.close_dark);
            } else
                mCloseButton.setImageResource(R.drawable.back_dark_chevron);
        } else if (getActivity() != null) {
            listener = (CoverChooseListener) getActivity();
            isCurrentPage = listener.isCurrentTheme();
            mCloseButton.setImageResource(R.drawable.close_dark);
        }

        if (isChildFragment() && !(getParentFragment() instanceof FTQuickCreateSettingsPopup)) {
            mCloseButton.setImageResource(R.drawable.back_dark_chevron);
        } else {
            mCloseButton.setImageResource(R.drawable.close_dark);
        }

        paperOrientationLayout.setVisibility(themeType.equals(FTNThemeCategory.FTThemeType.PAPER) ? View.VISIBLE : View.GONE);

        dialogTitle.setText(themeType.equals(FTNThemeCategory.FTThemeType.COVER) ? R.string.select_cover_template : R.string.select_paper_template);

        ObservingService.getInstance().addObserver("onCoverUpdate", addStoreThemeObserver);
        ObservingService.getInstance().addObserver("addCustomTheme", addCustomThemeObserver);

        FTGridLayoutManager layoutManager = new FTGridLayoutManager(getContext(), getResources().getDimensionPixelOffset(R.dimen.new_153dp));
        templateItemRecyclerView.setHasFixedSize(true);
        templateItemRecyclerView.setLayoutManager(layoutManager);

        themeCategoryAdapter = new FTChooseCoverPaperCategoryAdapter(this);
        categoryRecyclerView.setAdapter(themeCategoryAdapter);

        themeAdapter = new FTChooseCoverPaperAdapter(getChildFragmentManager(), this);
        templateItemRecyclerView.setAdapter(themeAdapter);

        updateCategories();
        getThemesByCategory();

        if (!isMobile()) {
            FTNTheme selectedTheme = null;
            if (themeType.equals(FTNThemeCategory.FTThemeType.COVER)) {
                FTNTheme coverTheme = null;
                String coverPackName = FTApp.getPref().get(SystemPref.RECENT_COVER_THEME_NAME, FTConstants.DEFAULT_COVER_THEME_NAME);
                if (coverPackName.endsWith(".nsc")) {
                    coverTheme = (FTNTheme) FTNTheme.theme(FTNThemeCategory.getUrl(coverPackName));
                }
                if (coverTheme == null || coverTheme.themeThumbnail(getContext()) == null) {
                    coverTheme = new FTNThemeCategory(getContext(), "Simple", FTNThemeCategory.FTThemeType.COVER).getCoverThemeForPackName(FTConstants.DEFAULT_COVER_THEME_NAME);
                    FTApp.getPref().save(SystemPref.RECENT_COVER_THEME_NAME, FTConstants.DEFAULT_COVER_THEME_NAME);
                }
                selectedTheme = coverTheme;
            } else if (themeType.equals(FTNThemeCategory.FTThemeType.PAPER)) {
                FTNTheme paperTheme = null;
                String paperPackName = FTApp.getPref().get(SystemPref.RECENT_PAPER_THEME_NAME, FTConstants.DEFAULT_PAPER_THEME_NAME);
                if (paperPackName.endsWith(".nsp")) {
                    paperTheme = FTNTheme.theme(FTNThemeCategory.getUrl(paperPackName));
                }
                if (paperTheme == null || paperTheme.themeThumbnail(getContext()) == null) {
                    paperTheme = new FTNThemeCategory(getContext(), "Simple", FTNThemeCategory.FTThemeType.PAPER).getPaperThemeForPackName(FTConstants.DEFAULT_PAPER_THEME_NAME);
                    FTApp.getPref().save(SystemPref.RECENT_PAPER_THEME_NAME, FTConstants.DEFAULT_PAPER_THEME_NAME);
                    Log.d("TemplatePicker==>","RECENT_PAPER_THEME_NAME FTChooseCoverPaperDialog insertFileFromInfo paperTheme.themeName::-"+paperTheme.themeName);
                    Gson gson = new Gson();
                    String json = gson.toJson(paperTheme);
                    FTApp.getPref().save(SystemPref.RECENT_PAPER_THEME, json);
                    FTTemplatesInfoSingleton.getInstance().setPaperTheme(paperTheme);

                    Log.d("TemplatePicker==>","Mani FTChooseCoverPaperDialog onViewCreated paperTheme.themeName::-"+paperTheme.themeName);
                }
                selectedTheme = paperTheme;
            }
            if (selectedTheme != null) {
                outerLoop:
                for (Map.Entry<FTNTheme, List<FTNTheme>> entry : themesByCategory.entrySet()) {
                    for (FTNTheme theme : entry.getValue()) {
                        if (selectedTheme.packName.equals(theme.packName)) {
                            currentCategory = entry.getKey();
                            break outerLoop;
                        }
                    }
                }
            }
            onTemplateCategorySelected(currentCategory);
            themeCategoryAdapter.selectTheme(currentCategory);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ObservingService.getInstance().removeObserver("onCoverUpdate", addStoreThemeObserver);
        ObservingService.getInstance().removeObserver("addCustomTheme", addCustomThemeObserver);
    }

    @OnClick(R.id.choose_cover_paper_portrait_text_view)
    void onPortraitTemplateClicked() {
        portraitTextView.setBackgroundResource(R.drawable.tab_item_bg);
        landscapeTextView.setBackgroundResource(android.R.color.transparent);
        isLandscape = false;
        getThemesByCategory();
    }

    @OnClick(R.id.choose_cover_paper_landscape_text_view)
    void onLandscapeTemplateClicked() {
        landscapeTextView.setBackgroundResource(R.drawable.tab_item_bg);
        portraitTextView.setBackgroundResource(android.R.color.transparent);
        isLandscape = true;
        getThemesByCategory();
    }

    @OnClick(R.id.choose_cover_paper_close_button)
    void onCloseClicked() {
        if (getDialog() instanceof BottomSheetDialog && templatesLayout.getVisibility() == View.VISIBLE) {
            if (currentCategory.categoryName.equals(getString(R.string.free_downloads))) {
                dialogTitle.setText(themeType.equals(FTNThemeCategory.FTThemeType.COVER) ? R.string.select_cover_template : R.string.select_paper_template);
            } else {
                FTAnimationUtils.showEndPanelAnimation(getContext(), templatesLayout, false, () -> {
                    templatesLayout.setVisibility(View.GONE);
                    dialogTitle.setText(themeType.equals(FTNThemeCategory.FTThemeType.COVER) ? R.string.select_cover_template : R.string.select_paper_template);
                    templatesLayout.setBackgroundColor(getResources().getColor(android.R.color.transparent));
                    mCloseButton.setImageResource(R.drawable.close_dark);
                });
            }
        } else {
            dismissAllowingStateLoss();
            listener.onClose();
        }
    }

    @Override
    public void onTemplateSelect(FTNTheme theme) {
        Log.d("TemplatePicker==>","Template Selected action onTemplateSelect::-");
        if (theme.themeName.equals("addCustomTheme")) {
            if (getFragmentManager() != null && theme.ftThemeType == FTNThemeCategory.FTThemeType.COVER) {
                Log.d("TemplatePicker==>","Template Selected action FTSelectThemeStyleDialog onTemplateSelect::-");
                FTSelectThemeStyleDialog selectThemeStyleDialog = FTSelectThemeStyleDialog.newInstance(selectedCover -> {
                    FTApp.getPref().save(SystemPref.SELECTED_COVER_STYLE, selectedCover);
                    listener.addCustomTheme(theme);
                });
                selectThemeStyleDialog.show(getFragmentManager(), FTSelectThemeStyleDialog.class.getName());
            } else {
                listener.addCustomTheme(theme);
            }
        } else {
            Log.d("TemplatePicker==>","Template Selected action FTCHooseCoberPaperDialog onTemplateSelect::-"+theme.isLandscape);
            listener.onThemeChosen(theme, isCurrentPage,theme.isLandscape);
            dismiss();
        }
    }

    @Override
    public void onTemplateDelete(FTNTheme theme) {
        if (!theme.isTemplate()) {
            themeAdapter.remove(theme);
            theme.deleteTemplate();
            theme.isDeleted = true;

            List<FTNTheme> themes = themesByCategory.entrySet().stream()
                    .filter(entry -> entry.getKey().getCategoryName().equals(currentCategory.getCategoryName()))
                    .flatMap(entry -> entry.getValue().stream())
                    .collect(Collectors.toList());
            if (!themes.isEmpty()) {
                themes.remove(theme);
            }
            List<String> categoryNames = themeCategoryAdapter.getAll().stream().map(FTNTheme::getCategoryName).collect(Collectors.toList());
            int index = categoryNames.indexOf(currentCategory.getCategoryName());
            if (themes.isEmpty()) {
                themeCategoryAdapter.remove(currentCategory);
                themesByCategory.remove(currentCategory);
            }
            if (index >= 0) {
                currentCategory = themeCategoryAdapter.getItem(index);
            }
            updateCategories();
            getThemesByCategory();
        }
    }

    @Override
    public void onTemplateCategorySelected(FTNTheme themeCategory) {
        themeAdapter.isEditMode = false;
        if (!themeCategory.categoryName.equals(getString(R.string.free_downloads))) {
            currentCategory = themeCategory;
            if (themeType == FTNThemeCategory.FTThemeType.PAPER) {
                paperOrientationLayout.setVisibility(currentCategory.categoryName.equals(getString(R.string.custom)) ? View.GONE : View.VISIBLE);
            }
            getThemesByCategory();

            if (getDialog() != null && getDialog() instanceof BottomSheetDialog) {
                templatesLayout.setVisibility(View.VISIBLE);
                templatesLayout.setBackgroundColor(getResources().getColor(R.color.ns_dialog_bg));
                dialogTitle.setText(themeCategory.categoryName);
                mCloseButton.setImageResource(R.drawable.back_dark_chevron);
                FTAnimationUtils.showEndPanelAnimation(getContext(), templatesLayout, true, null);
            }
        } else {
            themeCategoryAdapter.setSelectedPos(-1);
            themeCategoryAdapter.notifyDataSetChanged();
        }
    }

    private void updateCategories() {
        themeCategoryAdapter.clear();
        getAllThemes();
        List<FTNTheme> categories = new ArrayList<>(themesByCategory.keySet());
        if (!categories.isEmpty()) {
            FTNTheme themeCategory = new FTNTheme();
            themeCategory.setCategoryName(getString(R.string.free_downloads));
            categories.add(categories.size(), themeCategory);
            List<String> categoryNames = themesByCategory.keySet().stream().map(FTNTheme::getCategoryName).collect(Collectors.toList());
            if (currentCategory == null) {
                currentCategory = themesByCategory.keySet().iterator().next();
            }
            int selectedIndex = categoryNames.indexOf(currentCategory.categoryName);
            themeCategoryAdapter.setSelectedPos(Math.max(selectedIndex, 0));
            themeCategoryAdapter.addAll(categories);
        }
    }

    private void getThemesByCategory() {
        themeAdapter.clear();
        if (!themesByCategory.isEmpty()) {
            if (currentCategory == null)
                currentCategory = themesByCategory.keySet().iterator().next();
            List<FTNTheme> themes = themesByCategory.entrySet().stream()
                    .filter(entry -> entry.getKey().categoryName.equals(currentCategory.categoryName))
                    .flatMap(entry -> entry.getValue().stream())
                    .collect(Collectors.toList());
            themes = cleanThemes(themes);
            if (!themes.isEmpty()) {
                if (themeType == FTNThemeCategory.FTThemeType.PAPER && !currentCategory.categoryName.equals(getString(R.string.custom))) {

                    for (FTNTheme theme : themes) {
                        if (theme.isLandscape() == isLandscape) {
                            themeAdapter.add(theme);
                        }
                    }
                    errorTextView.setVisibility(themeAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
                } else {
                    themeAdapter.addAll(themes);
                }
            } else {
                themeAdapter.updateAll(themes);
            }
        }
    }

    //This is added to check if any theme is partially downloaded
    private List<FTNTheme> cleanThemes(List<FTNTheme> themes) {
        for (int i = 0; i < themes.size(); i++) {
            if (themes.get(i).themeName.isEmpty()) {
                themes.remove(i);
                i--;
            }
        }

        return themes;
    }

    private void getAllThemes() {
        themesByCategory.clear();
        HashMap<String, FTNTheme> allThemes = new HashMap<>();

        //Default themes
        try {
            String categoryFolderName = themeType == FTNThemeCategory.FTThemeType.PAPER ? "stockPapers" : "stockCovers";
            AssetManager assetManager = getContext().getAssets();

            for (String eachThemeDirName : assetManager.list(categoryFolderName)) {
                FTNTheme theme = FTNTheme.theme(new FTUrl(categoryFolderName + "/" + eachThemeDirName));
                theme.packName = eachThemeDirName;
                allThemes.put(theme.packName, theme);
            }
        } catch (IOException e) {
            FTLog.debug(getClass().getName(), e.getMessage());
        }

        //Downloaded themes
        //String downloadedThemesFolderPath = themeType == FTNThemeCategory.FTThemeType.PAPER ? FTConstants.DOWNLOADED_PAPERS_PATH : FTConstants.DOWNLOADED_COVERS_PATH;
        String downloadedThemesFolderPath = themeType == FTNThemeCategory.FTThemeType.PAPER ? FTConstants.DOWNLOADED_PAPERS_PATH2 : FTConstants.DOWNLOADED_COVERS_PATH;
        File downloadedThemesDir = new File(downloadedThemesFolderPath);
        if (downloadedThemesDir.exists() && downloadedThemesDir.isDirectory()) {
            for (File eachThemeDir : downloadedThemesDir.listFiles()) {
                if (eachThemeDir.exists() && eachThemeDir.isDirectory()) {
                    FTNTheme theme = FTNTheme.theme(new FTUrl(eachThemeDir.getAbsolutePath()));
                    theme.packName = eachThemeDir.getName();
                    theme.isDownloadTheme = true;
                    allThemes.put(theme.packName, theme);
                }
            }
        }

        //Theme filtering
        FTStorePackData storePackData = new FTStorePackData(getContext());
        HashMap<String, Object> mStorePackData = storePackData.getStorePackData();
        String categoryType = themeType == FTNThemeCategory.FTThemeType.PAPER ? "papers" : "covers";
        mStorePackData = (HashMap<String, Object>) mStorePackData.get(categoryType);
        Gson gson = new Gson();
        String storeData = gson.toJson(mStorePackData);
        FTStoreCategories storeCategories = gson.fromJson(storeData, FTStoreCategories.class);
        for (FTStoreCategoryItem storeCategoryItem : storeCategories.getCategories()) {
            String storeCategoryName = storeCategoryItem.getCategory_name();
            List<String> storeThemes = storeCategoryItem.getThemes();

            FTNTheme themeCategory = new FTNTheme();
            themeCategory.setCategoryName(storeCategoryName);

            List<FTNTheme> filteredThemes = new ArrayList<>();
            for (int j = 0; j < storeThemes.size(); j++) {
                FTNTheme theme = allThemes.get(storeThemes.get(j));
                if (theme != null) {
//                    theme.setCategoryName(storeCategoryName);
                    filteredThemes.add(theme);

                    themeCategory.isDownloadTheme = theme.isDownloadTheme;

                    allThemes.remove(storeThemes.get(j));
                }
            }
            if (!filteredThemes.isEmpty()) {
                customSort(filteredThemes);
                themesByCategory.put(themeCategory, filteredThemes);
            }
        }

        /*Commented out by Sreenu. Rather doing this we need to upgrade the plist.
        
        List<String> categories = new ArrayList<>();
        for (FTNTheme theme : allThemes.values()) {
            FTNTheme themeCategory = new FTNTheme();
            if (!TextUtils.isEmpty(theme.getCategoryName()))
                themeCategory.setCategoryName(theme.getCategoryName());
            themeCategory.isDownloadTheme = theme.isDownloadTheme;
            if (theme.isDownloadTheme && !categories.contains(theme.categoryName)) {
                List<FTNTheme> themes = new ArrayList<>();
                for (FTNTheme eachTheme : allThemes.values()) {
                    if (!TextUtils.isEmpty(eachTheme.categoryName) && eachTheme.categoryName.equals(theme.categoryName) && eachTheme.isDownloadTheme) {
                        themes.add(eachTheme);
                    }
                }
                customSort(themes);
                themesByCategory.put(themeCategory, themes);
                categories.add(theme.categoryName);
            }
        }*/

        //Custom themes
        FTNTheme themeCategory = new FTNTheme();
        themeCategory.setCategoryName(getString(R.string.custom));

        List<FTNTheme> customThemes = new ArrayList<>();
        //String customThemesDirPath = themeType == FTNThemeCategory.FTThemeType.PAPER ? FTConstants.DOWNLOADED_PAPERS_PATH : FTConstants.DOWNLOADED_COVERS_PATH;
        String customThemesDirPath = themeType == FTNThemeCategory.FTThemeType.PAPER ? FTConstants.DOWNLOADED_PAPERS_PATH2 : FTConstants.DOWNLOADED_COVERS_PATH;
        FTNTheme custom = new FTNTheme();
        custom.themeName = "addCustomTheme";
        custom.ftThemeType = themeType;
        custom.setCategoryName(getString(R.string.custom));
        customThemes.add(custom);
        File customThemesDir = new File(customThemesDirPath.replace("download", "custom"));
        if (customThemesDir.exists() && customThemesDir.isDirectory()) {
            for (File eachThemeDir : customThemesDir.listFiles()) {
                if (eachThemeDir.exists() && eachThemeDir.isDirectory()) {
                    FTNTheme theme = FTNTheme.theme(new FTUrl(eachThemeDir.getAbsolutePath()));
                    theme.themeName = FileUriUtils.getName((eachThemeDir.getName().replace(".nsc", "")).replace(".nsp", ""));
                    theme.setCategoryName(getString(R.string.custom));
                    customThemes.add(theme);
                    themeCategory.isCustomTheme = theme.isCustomTheme;
                }
            }
        }
        customSort(customThemes);
        themesByCategory.put(themeCategory, customThemes);

        Iterator<FTNTheme> iterator = themesByCategory.keySet().iterator();
        while (iterator.hasNext()) {
            if (TextUtils.isEmpty(iterator.next().categoryName)) {
                iterator.remove();
            }
        }
    }

    private void customSort(List<FTNTheme> themes) {
        Collections.sort(themes, new Comparator<FTNTheme>() {
            public int compare(FTNTheme o1, FTNTheme o2) {
                return (int) (extractInt(o1.themeName) - extractInt(o2.themeName));
            }

            long extractInt(String s) {
                String num = s.replaceAll("\\D", "");
                return num.isEmpty() ? 0 : Long.parseLong(num);
            }
        });
    }

    public interface CoverChooseListener {
        void onThemeChosen(FTNTheme theme, boolean isCurrentPage,boolean isLandscape);

        void addCustomTheme(FTNTheme theme);

        void onClose();

        boolean isCurrentTheme();
    }
}