package com.fluidtouch.noteshelf.clipart.pixabay.dialog;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf.clipart.ClipartError;
import com.fluidtouch.noteshelf.clipart.FTClipartFragment;
import com.fluidtouch.noteshelf.clipart.pixabay.adapters.FTPixabayAdapterCallback;
import com.fluidtouch.noteshelf.clipart.pixabay.adapters.FTPixabayClipartAdapter;
import com.fluidtouch.noteshelf.clipart.pixabay.adapters.FTPixabayRecentClipartAdapter;
import com.fluidtouch.noteshelf.clipart.pixabay.models.Clipart;
import com.fluidtouch.noteshelf.clipart.pixabay.providers.FTPixabayClipartProvider;
import com.fluidtouch.noteshelf.clipart.pixabay.providers.FTPixabayLocalClipartProvider;
import com.fluidtouch.noteshelf.clipart.pixabay.providers.FTPixabayProviderCallback;
import com.fluidtouch.noteshelf.clipart.unsplash.dialog.FTUnsplashClipartFragment;
import com.fluidtouch.noteshelf.commons.ui.FTSmartDialog;
import com.fluidtouch.noteshelf.commons.utils.ScreenUtil;
import com.fluidtouch.noteshelf.preferences.SystemPref;
import com.fluidtouch.noteshelf.shelf.activities.FTGridLayoutManager;
import com.fluidtouch.noteshelf2.R;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FTPixabayClipartFragment extends FTClipartFragment implements FTPixabayProviderCallback, FTPixabayAdapterCallback, SearchView.OnQueryTextListener {
    @BindView(R.id.clipart_search_layout)
    LinearLayout mSearchLayout;
    @BindView(R.id.clipart_search_cancel)
    TextView mCancelSearchView;
    @BindView(R.id.clipart_search_view)
    SearchView mSearchView;
    @BindView(R.id.library_clipart_recycler_view)
    RecyclerView mLibraryClipartRecyclerView;
    @BindView(R.id.clipart_recent_recycler_view)
    RecyclerView mRecentClipartRecycler;
    @BindView(R.id.clipart_dialog_progress_layout)
    LinearLayout mProgressLayout;
    @BindView(R.id.clipart_categories_layout)
    LinearLayout mCategoriesLayout;

    private FTPixabayClipartProvider mClipartProvider;
    private FTPixabayLocalClipartProvider mLocalClipartProvider;
    private FTPixabayClipartAdapter mLibraryClipartAdapter;
    private FTPixabayRecentClipartAdapter mRecentClipartAdapter;
    private String mPrevSearchKey = "";
    private String mSelectedCategory = "";
    private FTSmartDialog smartDialog = new FTSmartDialog();

    public FTPixabayClipartFragment(String withSearchKey) {
        mPrevSearchKey = withSearchKey;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cliparts, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        updateSearchUI(false);
        mSearchView.setOnQueryTextListener(this);

        mClipartProvider = new FTPixabayClipartProvider();
        mLocalClipartProvider = new FTPixabayLocalClipartProvider();

        mLibraryClipartRecyclerView.setLayoutManager(new FTGridLayoutManager(getContext(), ScreenUtil.convertDpToPx(getContext(), 97)));

        mLibraryClipartAdapter = new FTPixabayClipartAdapter(this);
        mLibraryClipartRecyclerView.setAdapter(mLibraryClipartAdapter);
        mRecentClipartAdapter = new FTPixabayRecentClipartAdapter(this);
        mRecentClipartRecycler.setAdapter(mRecentClipartAdapter);

        mLibraryClipartRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NotNull RecyclerView recyclerView,
                                   int dx, int dy) {
                GridLayoutManager gridLayoutManager = (GridLayoutManager) recyclerView.getLayoutManager();
                if (gridLayoutManager != null) {
                    int lasPos = gridLayoutManager.findLastVisibleItemPosition() + 1;
                    int loadedCount = mLibraryClipartAdapter.getItemCount();
                    if (mLibraryClipartAdapter.getItemCount() == 0 || (lasPos < loadedCount && lasPos >= (loadedCount / 3))) {
                        mClipartProvider.getNextPage(FTPixabayClipartFragment.this);
                    }
                }
            }
        });

        if (TextUtils.isEmpty(mPrevSearchKey)) {
            String category = FTApp.getPref().get(SystemPref.LAST_SELECTED_PIXABAY_CATEGORY, "photo");
            View categoryView = getView().findViewWithTag(category);
            if (categoryView != null) categoryView.callOnClick();
        } else {
            performSearch();
        }
    }

    @OnClick(R.id.clipart_search_icon)
    void onSearchIconClicked() {
        updateSearchUI(true);
    }

    @OnClick(R.id.clipart_search_cancel)
    void onSearchCancel() {
        updateSearchUI(false);
        mSelectedCategory = mPrevSearchKey = "";
        mSearchView.setQuery(mPrevSearchKey, false);
        String category = FTApp.getPref().get(SystemPref.LAST_SELECTED_PIXABAY_CATEGORY, "photo");
        View categoryView = getView().findViewWithTag(category);
        if (categoryView != null) categoryView.callOnClick();
    }

    @Override
    public void onCategorySelected(String category) {
        if (!category.equals(mSelectedCategory)) {
            if (category.equals(CLIPART_RECENT)) {
                mProgressLayout.setVisibility(View.GONE);
                mLibraryClipartRecyclerView.setVisibility(View.GONE);
                mRecentClipartRecycler.setVisibility(View.VISIBLE);
            } else {
                mRecentClipartRecycler.setVisibility(View.GONE);
                mLibraryClipartRecyclerView.setVisibility(View.VISIBLE);
            }
            mSelectedCategory = category;
            FTApp.getPref().save(SystemPref.LAST_SELECTED_PIXABAY_CATEGORY, mSelectedCategory);
            performSearch();
        }
    }

    @Override
    public void onLoadCliparts(List<Clipart> clipartList, ClipartError clipartError) {
        if (isAdded()) {
            mProgressLayout.setVisibility(View.GONE);

            if (clipartList != null && !clipartList.isEmpty()) {
                if (mSelectedCategory.equals(CLIPART_RECENT)) {
                    mRecentClipartRecycler.setVisibility(View.VISIBLE);
                    mRecentClipartAdapter.setData(clipartList);
                } else {
                    mLibraryClipartRecyclerView.setVisibility(View.VISIBLE);
                    mLibraryClipartAdapter.addAll(clipartList);
                }
            } else {
                if (clipartError == ClipartError.NO_RESULTS && mLibraryClipartAdapter.getItemCount() > 0) {
                    return;
                }
                mLibraryClipartRecyclerView.setVisibility(View.GONE);
                mRecentClipartRecycler.setVisibility(View.GONE);
                if (getParentFragment() != null)
                    ((Callback) getParentFragment()).updateErrorUI(clipartError);
            }
        }
    }

    @Override
    public void onClipartDownloaded(String path) {
        if (smartDialog.isAdded()) smartDialog.dismiss();
        if (getParentFragment() != null) {
            ((Callback) getParentFragment()).setClipartImageAnnotation(path);
            ((Callback) getParentFragment()).dismissOnClipartSelected();
        }
    }

    @Override
    public void onClipartSelected(Clipart clipart, boolean delete) {
        if (delete) {
            mLocalClipartProvider.deleteClipart(clipart);
            if (mRecentClipartAdapter.getItemCount() == 0 && getParentFragment() != null) {
                ((Callback) getParentFragment()).updateErrorUI(ClipartError.NO_RECENTS);
            }
        } else {
            if (!smartDialog.isAdded()) {
                smartDialog.setMessage(getString(R.string.downloading));
                smartDialog.setMode(FTSmartDialog.FTSmartDialogMode.SPINNER);
                smartDialog.show(getChildFragmentManager());
                mLocalClipartProvider.saveToRecents(clipart, this);
            }
        }
    }

    @Override
    public void reorderCliparts(int from, int to) {
        Collections.swap(mRecentClipartAdapter.getData(), from, to);
        mLocalClipartProvider.saveAllRecentCliparts(mRecentClipartAdapter.getData());
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        if (!query.equals(mPrevSearchKey)) {
            mPrevSearchKey = query;
            if (getParentFragment() != null) ((Callback) getParentFragment()).setSearchKey(query);
            performSearch();
        }
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        if (TextUtils.isEmpty(newText)) {
            mPrevSearchKey = "";
            if (getParentFragment() != null)
                ((Callback) getParentFragment()).setSearchKey(mPrevSearchKey);
        }
        return false;
    }

    private void performSearch() {
        if (!FTApp.getInstance().isNetworkAvailable() && !mSelectedCategory.toLowerCase().contains("recent")) {
            onLoadCliparts(null, ClipartError.NETWORK_ERROR);
            return;
        }
        if (getParentFragment() != null)
            ((FTUnsplashClipartFragment.Callback) getParentFragment()).updateErrorUI(ClipartError.NONE);
        if (mSelectedCategory.equals(CLIPART_RECENT)) {
            mRecentClipartAdapter.clearData();
            mLocalClipartProvider.searchInRecentCliparts(mPrevSearchKey, this);
        } else {
            mProgressLayout.setVisibility(View.VISIBLE);
            mLibraryClipartAdapter.clear();
            mClipartProvider.searchClipartInLibrary(mPrevSearchKey, mSelectedCategory, this);
        }
    }

    private void updateSearchUI(boolean show) {
        mSearchView.setVisibility(show ? View.VISIBLE : View.GONE);
        mCancelSearchView.setVisibility(show ? View.VISIBLE : View.GONE);
        mSearchLayout.setBackgroundResource(show ? R.drawable.finder_search_bg : 0);
        mCategoriesLayout.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    public interface Callback {
        void setSearchKey(String searchKey);

        void updateErrorUI(ClipartError errorCode);

        void setClipartImageAnnotation(String bitmapUri);

        void dismissOnClipartSelected();
    }
}
