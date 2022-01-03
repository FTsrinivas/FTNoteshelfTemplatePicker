package com.fluidtouch.noteshelf.shelf.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf.commons.FTLog;
import com.fluidtouch.noteshelf.commons.settingsUI.dialogs.FTSettingsDialog;
import com.fluidtouch.noteshelf.commons.ui.FTDialogFactory;
import com.fluidtouch.noteshelf.documentframework.FTUrl;
import com.fluidtouch.noteshelf.documentframework.Utilities.FTConstants;
import com.fluidtouch.noteshelf.documentproviders.FTShelfCollectionProvider;
import com.fluidtouch.noteshelf.documentproviders.FTShelfItemCollection;
import com.fluidtouch.noteshelf.preferences.SystemPref;
import com.fluidtouch.noteshelf.shelf.adapters.FTCategoryAdapter;
import com.fluidtouch.noteshelf.shelf.viewholders.FTCategoryViewHolder;
import com.fluidtouch.noteshelf2.R;
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FTCategoriesFragment extends Fragment implements FTCategoryAdapter.NavigationItemListener {

    @BindView(R.id.nd_item_recycler_view)
    RecyclerView mNavRecyclerView;
    @BindView(R.id.nd_header_layout)
    ConstraintLayout mNDHeaderLayout;

    private FTCategoryAdapter mCategoryAdapter;
    private CategoriesFragmentCallbacks mListener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mListener = ((CategoriesFragmentCallbacks) getActivity());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.categories_layout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        ButterKnife.bind(this, view);
        setUpToolbar();
        setCategoriesAdapter();
    }

    @OnClick(R.id.nd_header_plus_image_view)
    protected void showNewCategoryDialog() {
        FTRenameDialog.newInstance(FTRenameDialog.RenameType.NEW_CATEGORY, getString(R.string.untitled), -1, new FTRenameDialog.RenameListener() {
            @Override
            public void renameShelfItem(String updatedName, int position, DialogFragment dialogFragment) {
                if (updatedName != null) {
                    if (!TextUtils.isEmpty(updatedName)) {
                        if (updatedName.equalsIgnoreCase(getString(R.string.trash))) {
                            FTDialogFactory.showAlertDialog(getContext(), "", getString(R.string.cannot_use_trash_as_it_is_reserved_by_the_app), "", getString(R.string.ok), null);
                        } else {
                            FTShelfCollectionProvider.getInstance().currentProvider().createShelfWithTitle(getContext(), updatedName, (shelf, error) -> {
                                FTApp.getPref().saveRecentCollectionName(shelf.getDisplayTitle(getContext()));
                                setCategoriesAdapter();
                                onShelfItemCollectionSelected(shelf);
                            });
                        }
                    }
                }
            }

            @Override
            public void dialogActionCancel() {
                //Sit idle for now
            }
        }).show(getChildFragmentManager(), "FTRenameDialog");
    }

    @OnClick(R.id.nd_header_settings_image_view)
    void showSettings() {
        FTLog.crashlyticsLog("UI: Opened Settings panel");
        FTSettingsDialog.newInstance().show(getParentFragmentManager());
    }

    public void setCategoriesAdapter() {
        FTShelfCollectionProvider.getInstance().shelfs(colllections -> {
            List<ExpandableGroup> groups = new ArrayList<>();
            groups.add(new ExpandableGroup(FTCategoryViewHolder.CATEGORIES, colllections));
            FTShelfCollectionProvider.getInstance().pinned(pinnedCollections -> {
                if (!pinnedCollections.isEmpty()) {
                    groups.add(new ExpandableGroup(FTCategoryViewHolder.PINNED, pinnedCollections));
                }
                FTShelfCollectionProvider.getInstance().recents(recentCollections -> {
                    if (!recentCollections.isEmpty()) {
                        groups.add(new ExpandableGroup(FTCategoryViewHolder.RECENT, recentCollections));
                    }
                    mCategoryAdapter = new FTCategoryAdapter(groups, getContext(), this);
                    mNavRecyclerView.setAdapter(mCategoryAdapter);
                    mCategoryAdapter.expandAllGroups();
                });
            });
        });
    }

    @Override
    public void onShelfItemCollectionSelected(FTShelfItemCollection collection) {
        mListener.onShelfItemCollectionSelected(collection);
    }

    @Override
    public void renameCollectionItem(String categoryName, int position, FTShelfItemCollection item) {
        if (categoryName.equalsIgnoreCase(getString(R.string.trash))) {
            FTDialogFactory.showAlertDialog(getContext(), "", getString(R.string.trash_error_message), "", getString(R.string.ok), null);
        } else {
            boolean refreshAdapter = item.getDisplayTitle(getContext()).equals(mListener.getCurrentShelfItemCollection().getDisplayTitle(getContext()));
            FTShelfCollectionProvider.getInstance().currentProvider().renameShelf(getContext(), categoryName, (shelf, error) -> {
                if (refreshAdapter) {
                    mListener.onCurrentShelfItemCollectionRenamed(shelf);
                }
                setCategoriesAdapter();
            }, item);
        }
    }

    @Override
    public void removeCollectionItem(int position, FTShelfItemCollection shelf) {
        FTShelfCollectionProvider.getInstance().currentProvider().deleteShelf(getContext(), shelf, (shelf1, error) -> {
            mListener.onShelfItemCollectionDeleted(shelf1);
        });
    }

    @Override
    public void hideNavigationDrawer() {
        mListener.closeLeftPanel();
    }

    @Override
    public void openSelectedDocument(FTUrl fileURL, int from) {
        mListener.openSelectedDocument(fileURL, from);
    }

    @Override
    public void pinNotebook(FTUrl fileURL) {
        FTShelfCollectionProvider.getInstance().recentShelfProvider.removeRecent(fileURL.getPath());
        FTShelfCollectionProvider.getInstance().pinnedShelfProvider.pinNotbook(fileURL.getPath());
        setCategoriesAdapter();
    }

    @Override
    public void removeFromRecents(FTUrl fileURL) {
        FTShelfCollectionProvider.getInstance().recentShelfProvider.removeRecent(fileURL.getPath());
        setCategoriesAdapter();
    }

    @Override
    public void unpinNotebook(FTUrl fileURL) {
        FTShelfCollectionProvider.getInstance().pinnedShelfProvider.removePinned(fileURL.getPath());
        setCategoriesAdapter();
    }

    public void setUpToolbar(){
        mNDHeaderLayout.setBackgroundColor(Color.parseColor(FTApp.getPref().get(SystemPref.SELECTED_THEME_TOOLBAR_COLOR, FTConstants.DEFAULT_THEME_TOOLBAR_COLOR)));
    }

    public interface CategoriesFragmentCallbacks {

        void onShelfItemCollectionSelected(FTShelfItemCollection collection);

        void onCurrentShelfItemCollectionRenamed(FTShelfItemCollection shelf);

        void onShelfItemCollectionDeleted(FTShelfItemCollection shelf);

        void openSelectedDocument(FTUrl fileUrl, final int from);

        void closeLeftPanel();

        FTShelfItemCollection getCurrentShelfItemCollection();
    }
}