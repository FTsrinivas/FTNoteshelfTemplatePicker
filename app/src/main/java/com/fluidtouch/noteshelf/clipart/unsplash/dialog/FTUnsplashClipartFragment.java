package com.fluidtouch.noteshelf.clipart.unsplash.dialog;

import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
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
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf.clipart.ClipartError;
import com.fluidtouch.noteshelf.clipart.FTClipartFragment;
import com.fluidtouch.noteshelf.clipart.pixabay.dialog.FTPixabayClipartFragment;
import com.fluidtouch.noteshelf.clipart.unsplash.adapters.FTLibraryUnsplashAdapter;
import com.fluidtouch.noteshelf.clipart.unsplash.adapters.FTRecentUnsplashAdapter;
import com.fluidtouch.noteshelf.clipart.unsplash.adapters.FTUnsplashAdapterCallback;
import com.fluidtouch.noteshelf.clipart.unsplash.models.UnsplashPhotoInfo;
import com.fluidtouch.noteshelf.clipart.unsplash.providers.FTLocalUnsplashProvider;
import com.fluidtouch.noteshelf.clipart.unsplash.providers.FTUnsplashProvider;
import com.fluidtouch.noteshelf.clipart.unsplash.providers.FTUnsplashProviderCallback;
import com.fluidtouch.noteshelf.commons.ui.FTSmartDialog;
import com.fluidtouch.noteshelf.commons.utils.ScreenUtil;
import com.fluidtouch.noteshelf.preferences.SystemPref;
import com.fluidtouch.noteshelf2.R;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FTUnsplashClipartFragment extends FTClipartFragment implements FTUnsplashProviderCallback, FTUnsplashAdapterCallback, SearchView.OnQueryTextListener {
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

    private FTUnsplashProvider mClipartProvider;
    private FTLocalUnsplashProvider mLocalClipartProvider;
    private FTLibraryUnsplashAdapter mLibraryClipartAdapter;
    private FTRecentUnsplashAdapter mRecentClipartAdapter;
    private String mPrevSearchKey = "";
    private String mSelectedCategory = "";
    private FTSmartDialog smartDialog = new FTSmartDialog();
    private int pastVisibleItems, visibleItemCount, totalItemCount;


    public FTUnsplashClipartFragment(String withSearchKey) {
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

        mClipartProvider = new FTUnsplashProvider();
        mLocalClipartProvider = new FTLocalUnsplashProvider();

        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL);
        mLibraryClipartRecyclerView.setLayoutManager(layoutManager);
        mLibraryClipartRecyclerView.setItemAnimator(null);
        mLibraryClipartRecyclerView.addItemDecoration(new SpacesItemDecoration(ScreenUtil.convertDpToPx(getContext(), 6)));

        mLibraryClipartAdapter = new FTLibraryUnsplashAdapter(this);
        mLibraryClipartRecyclerView.setAdapter(mLibraryClipartAdapter);
        mRecentClipartAdapter = new FTRecentUnsplashAdapter(this);
        mRecentClipartRecycler.setAdapter(mRecentClipartAdapter);

        mLibraryClipartRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NotNull RecyclerView recyclerView, int dx, int dy) {
                StaggeredGridLayoutManager gridLayoutManager = (StaggeredGridLayoutManager) recyclerView.getLayoutManager();
                visibleItemCount = gridLayoutManager.getChildCount();
                totalItemCount = gridLayoutManager.getItemCount();
                int[] firstVisibleItems = null;
                firstVisibleItems = gridLayoutManager.findFirstVisibleItemPositions(firstVisibleItems);
                if (firstVisibleItems != null && firstVisibleItems.length > 0) {
                    pastVisibleItems = firstVisibleItems[0];
                }

                if ((visibleItemCount + pastVisibleItems) >= totalItemCount) {
                    mClipartProvider.getNextPage(FTUnsplashClipartFragment.this);
                }
            }
        });

        if (TextUtils.isEmpty(mPrevSearchKey)) {
            String category = FTApp.getPref().get(SystemPref.LAST_SELECTED_UNSPLASH_CATEGORY, "Featured");
            View categoryView = view.findViewWithTag(category);
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
        mSearchView.setQuery("", false);
        mPrevSearchKey = mSelectedCategory = "";
        String category = FTApp.getPref().get(SystemPref.LAST_SELECTED_UNSPLASH_CATEGORY, "Featured");
        View categoryView = getView().findViewWithTag(category);
        if (categoryView != null) categoryView.callOnClick();
    }

    @Override
    public void onCategorySelected(String category) {
        if (!category.equals(mSelectedCategory)) {
            if (category.equals(CLIPART_RECENT)) {
                mLibraryClipartRecyclerView.setVisibility(View.GONE);
                mRecentClipartRecycler.setVisibility(View.VISIBLE);
                mProgressLayout.setVisibility(View.GONE);
            } else {
                mLibraryClipartRecyclerView.setVisibility(View.VISIBLE);
            }
            mPrevSearchKey = mSelectedCategory = category;
            FTApp.getPref().save(SystemPref.LAST_SELECTED_UNSPLASH_CATEGORY, category);
            performSearch();
        }
    }

    @Override
    public void onLoadCliparts(List<UnsplashPhotoInfo> clipartList, ClipartError clipartError) {
        if (isAdded()) {
            mProgressLayout.setVisibility(View.GONE);

            if (clipartList != null && !clipartList.isEmpty()) {
                mLibraryClipartRecyclerView.setVisibility(View.VISIBLE);
                mRecentClipartRecycler.setVisibility(View.VISIBLE);
                if (mSelectedCategory.equals(CLIPART_RECENT))
                    mRecentClipartAdapter.setData(clipartList);
                else
                    mLibraryClipartAdapter.addAll(clipartList);
            } else {
                if (clipartError == ClipartError.NO_RESULTS && mLibraryClipartAdapter.getItemCount() > 0) {
                    return;
                }
                mLibraryClipartRecyclerView.setVisibility(View.GONE);
                mRecentClipartRecycler.setVisibility(View.GONE);
                if (getParentFragment() != null)
                    ((FTPixabayClipartFragment.Callback) getParentFragment()).updateErrorUI(clipartError);
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
    public void onClipartSelected(UnsplashPhotoInfo clipart, boolean delete) {
        if (delete) {
            mLocalClipartProvider.deleteClipart(clipart);
            if (mRecentClipartAdapter.getItemCount() == 0 && getParentFragment() != null) {
                ((FTPixabayClipartFragment.Callback) getParentFragment()).updateErrorUI(ClipartError.NO_RECENTS);
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
    public void onPhotographerNameSelected(int position) {
        if (FTApp.getInstance().isNetworkAvailable()) {
            String url = mLibraryClipartAdapter.getItem(position).getmUnsplashPhotoOwner().getHtml();
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            if (getActivity() != null) getActivity().startActivity(i);
        } else {
            if (getParentFragment() != null)
                ((Callback) getParentFragment()).updateErrorUI(ClipartError.NETWORK_ERROR);
        }
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
                ((FTPixabayClipartFragment.Callback) getParentFragment()).setSearchKey(mPrevSearchKey);
        }
        return false;
    }

    private void performSearch() {
        if (!FTApp.getInstance().isNetworkAvailable() && !mSelectedCategory.toLowerCase().contains("recent")) {
            onLoadCliparts(null, ClipartError.NETWORK_ERROR);
            return;
        }
        if (getParentFragment() != null)
            ((Callback) getParentFragment()).updateErrorUI(ClipartError.NONE);
        if (mSelectedCategory.equals(CLIPART_RECENT)) {
            mRecentClipartAdapter.clearData();
            mLocalClipartProvider.searchInRecentCliparts(mPrevSearchKey, this);
        } else {
            mProgressLayout.setVisibility(View.VISIBLE);
            mLibraryClipartAdapter.clear();
            mClipartProvider.searchClipartInLibrary(mPrevSearchKey, this);
        }
    }

    private void updateSearchUI(boolean show) {
        mSearchView.setVisibility(show ? View.VISIBLE : View.GONE);
        mCancelSearchView.setVisibility(show ? View.VISIBLE : View.GONE);
        mSearchLayout.setBackgroundResource(show ? R.drawable.finder_search_bg : 0);
        mCategoriesLayout.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private static class SpacesItemDecoration extends RecyclerView.ItemDecoration {

        private final int spacing;
        private int spanCount;
        private int itemWidth;

        public SpacesItemDecoration(int spacing) {
            this.spacing = spacing;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            if (spanCount == 0 && view.getWidth() > 0) {
                this.spanCount = ((StaggeredGridLayoutManager) parent.getLayoutManager()).getSpanCount();
                this.itemWidth = view.getWidth();
                parent.getLayoutParams().width = (itemWidth * spanCount) + (spacing * spanCount);
            }
        }
    }

    public interface Callback {
        void setSearchKey(String searchKey);

        void updateErrorUI(ClipartError errorCode);

        void setClipartImageAnnotation(String bitmapUri);

        void dismissOnClipartSelected();
    }
}
