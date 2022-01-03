package com.fluidtouch.noteshelf.document.thumbnailview;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf.audio.player.FTAudioPlayer;
import com.fluidtouch.noteshelf.commons.FTLog;
import com.fluidtouch.noteshelf.commons.ui.FTSmartDialog;
import com.fluidtouch.noteshelf.commons.utils.FTPopupFactory;
import com.fluidtouch.noteshelf.commons.utils.ObservingService;
import com.fluidtouch.noteshelf.commons.utils.ScreenUtil;
import com.fluidtouch.noteshelf.document.FTDocumentActivity;
import com.fluidtouch.noteshelf.document.dialogs.FTBookmarkDialog;
import com.fluidtouch.noteshelf.document.search.FTFinderSearchOptions;
import com.fluidtouch.noteshelf.documentframework.FTNoteshelfDocument.FTNoteshelfDocument;
import com.fluidtouch.noteshelf.documentframework.FTNoteshelfDocument.FTNoteshelfPage;
import com.fluidtouch.noteshelf.documentframework.ThumbnailGenerator.FTThumbnailGenerator;
import com.fluidtouch.noteshelf.preferences.SystemPref;
import com.fluidtouch.noteshelf.services.FTFirebaseAnalytics;
import com.fluidtouch.noteshelf.shelf.activities.FTGridLayoutManager;
import com.fluidtouch.noteshelf2.R;
import com.fluidtouch.renderingengine.renderer.FTRenderMode;
import com.thesurix.gesturerecycler.GestureManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Observable;
import java.util.Observer;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FTThumbnailFragment extends Fragment implements FinderAdapterListener, SearchView.OnQueryTextListener, FTPageMoveToFragment.PageMoveCallback, FTFinderFilterPopup.Listener {

    //region View Bindings
    //header section
    @BindView(R.id.finder_title_layout)
    LinearLayout mTitleLayout;
    @BindView(R.id.finder_select_all_text_view)
    TextView mSelectAllTextView;
    @BindView(R.id.finder_title_text_view)
    TextView mTitleTextView;
    @BindView(R.id.finder_done_text_view)
    TextView mDoneTextView;
    //search section
    @BindView(R.id.finder_search_layout)
    LinearLayout mSearchLayout;
    @BindView(R.id.finder_search_view)
    SearchView mSearchView;
    @BindView(R.id.finder_filter_button)
    ImageButton mFilterButton;
    //operations section
    @BindView(R.id.finder_buttons_layout)
    RelativeLayout mButtonsLayout;
    @BindView(R.id.delete_button)
    TextView mDeleteButton;
    @BindView(R.id.duplicate_button)
    TextView mDuplicateButton;
    @BindView(R.id.move_button)
    TextView mMoveButton;
    //bookmark section
    @BindView(R.id.finder_bookmark_layout)
    LinearLayout mBookmarkLayout;
    @BindView(R.id.finder_bookmark_thumbnails_tab)
    TextView mBookmarkThumbnailsTab;
    @BindView(R.id.finder_bookmark_list_tab)
    TextView mBookmarkListTab;
    @BindView(R.id.no_bookmarks_layout)
    LinearLayout mNoBookmarksLayout;
    //recycler view section
    @BindView(R.id.finder_thumbnail_recycler_view)
    RecyclerView mThumbnailRecyclerView;
    @BindView(R.id.finder_bookmark_recycler_view)
    RecyclerView mBookmarkRecyclerView;
    //misc section
    @BindView(R.id.finder_no_pages_found_message)
    TextView mNoPagesFoundTextView;
    @BindView(R.id.finder_expand_layout)
    FrameLayout mExpandLayout;
    //page indexing section
    @BindView(R.id.finder_indexing_dialog)
    View mIndexingDialog;
    @BindView(R.id.indexing_page_value_text_view)
    TextView mIndexingPageTextView;
    //endregion

    //region Member Variables
    private FTThumbnailItemAdapter thumbnailItemAdapter;
    private FTBookmarkItemAdapter bookmarkItemAdapter;
    private FTNoteshelfDocument currentDocument;
    private GestureManager gestureManager;
    private FTFinderSearchOptions searchOptions = new FTFinderSearchOptions();
    private boolean isFinderInBookmarkState;
    private FTThumbnailListener thumbnailListener;
    private boolean isEditMode;
    public boolean isExportMode = false;
    private ExportPagesCallback callback;
    private String searchKey = "";
    private ArrayList<FTNoteshelfPage> mSelectedPages = new ArrayList<>();
    private View.OnTouchListener mExpandOnTouchListener = new View.OnTouchListener() {
        private float xInitial;
        private int width = 0;
        private int defaultWidth = 0;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            RelativeLayout.LayoutParams lParams;
            final float x = event.getX();
            final float y = event.getY();

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    FTFirebaseAnalytics.logEvent("Finder_ExpandPanel");
                    ((FTDocumentActivity) getActivity()).getDrawerLayout().setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_OPEN, Gravity.END);
                    xInitial = x;
                    width = 0;
                    defaultWidth = getResources().getDimensionPixelOffset(R.dimen.new_300dp);
                    if (!FTApp.getPref().get(SystemPref.FINDER_SHOWING_BOOKMARK_TITLES, false))
                        thumbnailItemAdapter.notifyDataSetChanged();
                    break;

                case MotionEvent.ACTION_UP:
                    lParams = (RelativeLayout.LayoutParams) getView().getLayoutParams();
                    //width = (width / defaultWidth) * defaultWidth;
                    //lParams.width = width > defaultWidth ? width : defaultWidth;
                    FTApp.getPref().saveThumbnailWidth(lParams.width);
                    getView().setLayoutParams(lParams);
                    ((FTDocumentActivity) getActivity()).getDrawerLayout().setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED, Gravity.END);
                    break;

                case MotionEvent.ACTION_MOVE:
                    lParams = (RelativeLayout.LayoutParams) getView().getLayoutParams();
                    width = (int) ((xInitial - x) + getView().getWidth());
                    if (width >= defaultWidth) {
                        lParams.width = width;
                    }
                    if (width == defaultWidth) FTFirebaseAnalytics.logEvent("Finder_CollapsePanel");
                    getView().setLayoutParams(lParams);
                    break;
            }
            return true;
        }
    };
    private final Observer observer = new Observer() {
        @Override
        public void update(Observable o, Object arg) {
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    FTNoteshelfPage searchPage = (FTNoteshelfPage) arg;

                    if (thumbnailItemAdapter.getData().contains(searchPage) || (isFinderInBookmarkState && bookmarkItemAdapter.getAll().contains(searchPage))) {
                        return;
                    }

                    if (searchOptions != null && !TextUtils.isEmpty(searchOptions.searchedKeyword)) {
                        searchOptions.searchPageResults.add(searchPage);
                    }
                    if (isFinderInBookmarkState) {
                        if (searchPage.isBookmarked) {
                            thumbnailItemAdapter.add(searchPage);
                            bookmarkItemAdapter.add(searchPage);
                            thumbnailItemAdapter.notifyItemChanged(thumbnailItemAdapter.getItemCount() - 1);
                        }
                    } else {
                        thumbnailItemAdapter.add(searchPage);
                        thumbnailItemAdapter.notifyItemChanged(thumbnailItemAdapter.getItemCount() - 1);
                    }
                    mNoPagesFoundTextView.setVisibility(View.GONE);
                });
            }
        }
    };

    private final Observer mPageIndexingObserver = new Observer() {
        @Override
        public void update(Observable observable, Object o) {
            getActivity().runOnUiThread(() -> {
                if (isAdded()) {
                    if (o == null) {
                        mIndexingDialog.setVisibility(View.GONE);
                    } else {
                        FTNoteshelfPage page = (FTNoteshelfPage) o;
                        if (searchOptions != null && !TextUtils.isEmpty(searchOptions.searchedKeyword)) {
                            mIndexingPageTextView.setText(getString(R.string.indexing_page_num, (page.pageIndex() + 1)));
                            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mIndexingDialog.getLayoutParams();
                            layoutParams.bottomMargin = getResources().getDimensionPixelOffset(isEditMode ? R.dimen.new_78dp : R.dimen.new_24dp);
                            mIndexingDialog.setVisibility(View.VISIBLE);
                        }
                    }
                }
            });
        }
    };

    private final Observer mPageBookmarkObserver = new Observer() {
        @Override
        public void update(Observable observable, Object o) {
            if (isAdded()) {
                FTNoteshelfPage bookmarkedPage = (FTNoteshelfPage) o;
                thumbnailItemAdapter.notifyItemChanged(bookmarkedPage.pageIndex());
                bookmarkItemAdapter.notifyItemChanged(bookmarkedPage.pageIndex());
            }
        }
    };
    //endregion

    //region Lifecycle
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        thumbnailListener = (FTThumbnailListener) getActivity();
        ObservingService.getInstance().addObserver("pageIndexing", mPageIndexingObserver);
        ObservingService.getInstance().addObserver("page_bookmark", mPageBookmarkObserver);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.thumbnail_list_layout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        ButterKnife.bind(FTThumbnailFragment.this, view);

        currentDocument = thumbnailListener.currentDocument();
        searchOptions = thumbnailListener.searchOptions();

        thumbnailItemAdapter = new FTThumbnailItemAdapter(this);

        if (currentDocument != null) {
            thumbnailItemAdapter.setData(this.currentDocument.pages(getContext()));
        }
        bookmarkItemAdapter = new FTBookmarkItemAdapter(this);

        GridLayoutManager manager = new FTGridLayoutManager(getContext(), ScreenUtil.convertDpToPx(getContext(), 206));
        mThumbnailRecyclerView.setHasFixedSize(true);
        mThumbnailRecyclerView.setLayoutManager(manager);
        mThumbnailRecyclerView.setAdapter(thumbnailItemAdapter);

        gestureManager = new GestureManager.Builder(mThumbnailRecyclerView)
                .setSwipeEnabled(false)
                .setLongPressDragEnabled(true)
                .build();
        setOnScrollListenerForRecyclerView();
        new Handler().postDelayed(() -> mThumbnailRecyclerView.scrollToPosition(thumbnailListener.currentPageIndex()), 100);
        mBookmarkRecyclerView.setAdapter(bookmarkItemAdapter);

        mExpandLayout.setOnTouchListener(mExpandOnTouchListener);

        setInitialSetUp();
    }

    public void setInitialSetUp() {
        if (mSelectedPages != null) {
            mSelectedPages.clear();
        }
        if (!isExportMode) {
            if (this.searchOptions != null) {
                mSearchView.setQuery(this.searchOptions.searchedKeyword, false);
            }
        }

        setSelectMode(false);
        if (isExportMode) {
            mDoneTextView.setVisibility(View.VISIBLE);
            mDoneTextView.setText(R.string.share);
            mSelectAllTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.check_all, 0, 0, 0);
            mTitleTextView.setText(R.string.select_pages);
        }

        mSearchView.setOnQueryTextListener(this);
        if (currentDocument != null) {
            ObservingService.getInstance().addObserver("searchObserver_" + currentDocument.getDocumentUUID(), observer);
        }

        isFinderInBookmarkState = FTApp.getPref().get(SystemPref.FINDER_SHOWING_BOOKMARKED_PAGES, false);
        onFilterOptionSelected(isFinderInBookmarkState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        setSelectMode(false);
        mThumbnailRecyclerView.setAdapter(null);
        mBookmarkRecyclerView.setAdapter(null);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ObservingService.getInstance().removeObserver("searchedPage", observer);
        ObservingService.getInstance().removeObserver("pageIndexing", mPageIndexingObserver);
        ObservingService.getInstance().removeObserver("page_bookmark", mPageBookmarkObserver);
    }
    //endregion

    //region OnScrollListener
    private void setOnScrollListenerForRecyclerView() {
        mThumbnailRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    FTThumbnailGenerator.sharedThumbnailGenerator(FTRenderMode.thumbnailGen).cancelAllThumbnailGeneration();
                    GridLayoutManager layoutManager = (GridLayoutManager) mThumbnailRecyclerView.getLayoutManager();
                    if (layoutManager != null) {
                        thumbnailItemAdapter.onScrollingStops(layoutManager.findFirstVisibleItemPosition(), layoutManager.findLastVisibleItemPosition());
                    }

                }
            }
        });
    }
    //endregion

    //region Select Mode UI
    public void setSelectMode(boolean enable) {
        isEditMode = enable;
        if (enable) {
            mDoneTextView.setText(isExportMode ? R.string.share : R.string.done);
            mDoneTextView.setAlpha(isExportMode ? 0.5f : 1.0f);
            mSelectAllTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.check_all, 0, 0, 0);
            mTitleTextView.setText(R.string.select_pages);
            mButtonsLayout.setVisibility(View.VISIBLE);
            mBookmarkLayout.setVisibility(View.GONE);
            mSearchLayout.setVisibility(View.GONE);
            changeButtonsState(false);
        } else {
            mDoneTextView.setText(R.string.edit);
            mSelectAllTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.close_dark, 0, 0, 0);
            mTitleTextView.setText(R.string.pages);
            mButtonsLayout.setVisibility(View.GONE);
            mSearchLayout.setVisibility(View.VISIBLE);
            if (isFinderInBookmarkState) mBookmarkLayout.setVisibility(View.VISIBLE);
        }
    }

    @OnClick(R.id.finder_done_text_view)
    void onDoneClicked() {
        if (isExportMode && callback != null) {
            if (!mSelectedPages.isEmpty()) {
                callback.onPagesSelected(mSelectedPages);
                thumbnailListener.closePanel();
            }
            return;
        }

        if (isEditMode) FTFirebaseAnalytics.logEvent("Finder_TapEditPage");

        setSelectMode(!isEditMode);
        mSelectedPages.clear();
        if (FTApp.getPref().get(SystemPref.FINDER_SHOWING_BOOKMARK_TITLES, false))
            bookmarkItemAdapter.notifyDataSetChanged();
        thumbnailItemAdapter.notifyDataSetChanged();
    }
    //endregion

    //region Duplicate
    @OnClick(R.id.duplicate_button)
    void onDuplicateClicked(View view) {
        FTFirebaseAnalytics.logEvent("Finder_EditPage_Duplicate");
        FTLog.crashlyticsLog("Finder: Clicked duplicate thumbnails in finder " + mSelectedPages.size());

        if (FTAudioPlayer.getInstance().isRecording()) {
            FTAudioPlayer.showPlayerInProgressAlert(getContext(), () -> onDuplicateClicked(view));
            return;
        }

        if (!mSelectedPages.isEmpty()) {
            new FTFinderInsertPagePopup(mSelectedPages.size(), insertPosition -> {
                final FTSmartDialog smartDialog = new FTSmartDialog()
                        .setMode(FTSmartDialog.FTSmartDialogMode.SPINNER)
                        .setMessage(getString(R.string.duplicating))
                        .show(getChildFragmentManager());

                FTThumbnailFragment.this.currentDocument.recursivelyCopyPages(getContext(), mSelectedPages, true, insertPosition,
                        (success, error, copiedPages) -> {
                            if (isAdded() && getActivity() != null) {
                                FTThumbnailFragment.this.getActivity().runOnUiThread(() -> {
                                    FTThumbnailFragment.this.updateSearchFilterAndLoadData();
                                    FTThumbnailFragment.this.onSelectUpdateUI();
                                    thumbnailListener.reloadDocumentData();
                                    smartDialog.dismiss();
                                });
                            }
                        });
            }).show(mButtonsLayout, getChildFragmentManager());
        }
    }
    //endregion

    //region Delete
    @OnClick(R.id.delete_button)
    void onDeleteClicked(View view) {
        FTFirebaseAnalytics.logEvent("Finder_EditPage_MoveToTrash");
        FTLog.crashlyticsLog("UI: Clicked delete thumbnails in finder");

        if (FTAudioPlayer.getInstance().isRecording()) {
            FTAudioPlayer.showPlayerInProgressAlert(getContext(), () -> {
                onDeleteClicked(view);
            });
            return;
        }

        if (mSelectedPages.size() >= 1) {
            final PopupWindow deletePopup = FTPopupFactory.create(getContext(), view, R.layout.popup_delete_thumbnail, R.dimen.new_300dp, R.dimen.new_154dp);
            TextView selectedThumbnailCountTextView = deletePopup.getContentView().findViewById(R.id.thumbnail_delete_popup_delete);
            if (mSelectedPages.size() == 1) {
                selectedThumbnailCountTextView.setText(getString(R.string.delete_page_singular, 1));
            } else {
                selectedThumbnailCountTextView.setText(getString(R.string.delete_page_plural, mSelectedPages.size()));
            }

            selectedThumbnailCountTextView.setOnClickListener(v -> {
                deletePopup.dismiss();

                thumbnailItemAdapter.getData().removeAll(mSelectedPages);
                thumbnailItemAdapter.notifyDataSetChanged();
                if (isFinderInBookmarkState && thumbnailItemAdapter.getItemCount() == 0) {
                    mNoBookmarksLayout.setVisibility(View.VISIBLE);
                } else {
                    mNoBookmarksLayout.setVisibility(View.GONE);
                }

                final FTSmartDialog smartDialog = new FTSmartDialog()
                        .setMode(FTSmartDialog.FTSmartDialogMode.SPINNER)
                        .setMessage(getString(R.string.deleting))
                        .show(getChildFragmentManager());

                final ArrayList<FTNoteshelfPage> selectedPages = mSelectedPages;
                if (FTAudioPlayer.currentAudioPageIndex != -1) {
                    for (int i = 0; i < selectedPages.size(); i++) {
                        if (selectedPages.get(i).pageIndex() == FTAudioPlayer.currentAudioPageIndex) {
                            thumbnailListener.closeAudioToolbar();
                            FTAudioPlayer.currentAudioPageIndex = -1;
                            break;
                        }
                    }
                }

                if (selectedPages.size() == this.currentDocument.pages(getContext()).size()) {
                    int pageIndex = this.currentDocument.pages(getContext()).size();
                    this.currentDocument.insertPageAtIndex(getContext(), pageIndex, false, (insertedPage, error) ->
                            this.currentDocument.deletePages(getContext(), selectedPages, error1 -> getActivity().runOnUiThread(() -> {
                                mSelectedPages.clear();
                                onSelectUpdateUI();
                                searchOptions.searchPageResults.clear();
                                saveDocument(smartDialog);
                            })));
                } else {
                    this.currentDocument.deletePages(getContext(), selectedPages, error -> getActivity().runOnUiThread(() -> {
                        mSelectedPages.clear();
                        onSelectUpdateUI();
                        searchOptions.searchPageResults.clear();
                        saveDocument(smartDialog);
                    }));
                }
            });

            deletePopup.getContentView().findViewById(R.id.thumbnail_delete_popup_cancel)
                    .setOnClickListener(v -> deletePopup.dismiss());
        }
    }
    //endregion

    //region Move
    @OnClick(R.id.move_button)
    void onMoveClicked() {
        FTFirebaseAnalytics.logEvent("Finder_EditPage_Move");
        FTLog.crashlyticsLog("UI: Clicked move pages to another document");

        if (FTAudioPlayer.getInstance().isRecording()) {
            FTAudioPlayer.showPlayerInProgressAlert(getContext(), this::onMoveClicked);
            return;
        }

        if (!mSelectedPages.isEmpty()) {
            FTPageMoveToFragment pageMoveToFragment = FTPageMoveToFragment.newInstance(mSelectedPages, null);
            getChildFragmentManager().beginTransaction()
                    .setCustomAnimations(R.anim.slide_end_right_to_left, 0)
                    .add(R.id.moveto_panel_container, pageMoveToFragment)
                    .commit();
        }
    }

    @Override
    public void onMovePages(FTNoteshelfDocument destinationDocument, FTSmartDialog smartDialog) {
        if (mSelectedPages.size() == this.currentDocument.pages(getContext()).size()) {
            int pageIndex = FTThumbnailFragment.this.currentDocument.pages(getContext()).size();
            this.currentDocument.insertPageAtIndex(getContext(), pageIndex, false, (insertedPage, error) -> {
                if (error == null) {
                    currentDocument.movePagesToOtherDocument(getContext(), mSelectedPages, destinationDocument, (success, error1) -> getActivity().runOnUiThread(() -> {
                        if (success) {
                            mSelectedPages.clear();
                            searchOptions.searchPageResults.clear();
                            saveDocument(smartDialog);
                            onSelectUpdateUI();
                        }
                    }));
                }
            });
        } else {
            this.currentDocument.movePagesToOtherDocument(getContext(), mSelectedPages, destinationDocument, (success, error) -> getActivity().runOnUiThread(() -> {
                if (success) {
                    mSelectedPages.clear();
                    searchOptions.searchPageResults.clear();
                    saveDocument(smartDialog);
                    onSelectUpdateUI();
                }
            }));
        }
    }
    //endregion

    //region Filter
    @OnClick(R.id.finder_filter_button)
    void onFilterButtonClicked() {
        FTFirebaseAnalytics.logEvent("Finder_TapFilters");
        mFilterButton.setImageResource(R.drawable.filteron_dark);
        FTFinderFilterPopup filterPopup = new FTFinderFilterPopup();
        filterPopup.show(getChildFragmentManager());
    }

    @Override
    public void onFilterOptionSelected(boolean isBookmarkView) {
        isFinderInBookmarkState = isBookmarkView;

        mFilterButton.setImageResource(isFinderInBookmarkState ? R.drawable.filtercheck_dark : R.drawable.filter_dark);
        mBookmarkLayout.setVisibility(isFinderInBookmarkState ? View.VISIBLE : View.GONE);

        if (isFinderInBookmarkState) {
            if (FTApp.getPref().get(SystemPref.FINDER_SHOWING_BOOKMARK_TITLES, false)) {
                onBookmarkTitlesClicked();
            } else {
                onBookmarkThumbnailsClicked();
            }
        } else {
            mBookmarkRecyclerView.setVisibility(View.GONE);
            mThumbnailRecyclerView.setVisibility(View.VISIBLE);
            updateSearchFilterAndLoadData();
        }
    }
    //endregion

    //region Bookmark UI
    @OnClick(R.id.finder_bookmark_thumbnails_tab)
    void onBookmarkThumbnailsClicked() {
        FTFirebaseAnalytics.logEvent("Finder_Bookmarked_Thumbnails");
        FTLog.crashlyticsLog("Showing bookmarked thumbnails");
        FTApp.getPref().save(SystemPref.FINDER_SHOWING_BOOKMARK_TITLES, false);
        mBookmarkThumbnailsTab.setBackgroundResource(R.drawable.finder_tab_item_bg);
        mBookmarkListTab.setBackgroundColor(Color.TRANSPARENT);
        mThumbnailRecyclerView.setVisibility(View.VISIBLE);
        mBookmarkRecyclerView.setVisibility(View.GONE);
        mDoneTextView.setVisibility(View.VISIBLE);

        checkForBookmarks();
        checkPageCountAndUpdate();
    }

    @OnClick(R.id.finder_bookmark_list_tab)
    void onBookmarkTitlesClicked() {
        FTFirebaseAnalytics.logEvent("Finder_Bookmarked_List");
        FTLog.crashlyticsLog("Showing bookmarked titles");
        FTApp.getPref().save(SystemPref.FINDER_SHOWING_BOOKMARK_TITLES, true);
        mBookmarkListTab.setBackgroundResource(R.drawable.finder_tab_item_bg);
        mBookmarkThumbnailsTab.setBackgroundColor(Color.TRANSPARENT);
        mBookmarkRecyclerView.setVisibility(View.VISIBLE);
        mThumbnailRecyclerView.setVisibility(View.GONE);

        checkPageCountAndUpdate();
    }

    @OnClick(R.id.finder_bookmarks_close)
    void onBookmarkCloseClicked() {
        FTApp.getPref().save(SystemPref.FINDER_SHOWING_BOOKMARKED_PAGES, false);
        onFilterOptionSelected(false);
    }

    @Override
    public void checkPageCountAndUpdate() {
        if (this.isFinderInBookmarkState && filterBookmarkedPages(thumbnailItemAdapter.getData()).isEmpty()) {
            mNoBookmarksLayout.setVisibility(View.VISIBLE);
        } else {
            mNoBookmarksLayout.setVisibility(View.GONE);
            updateSearchFilterAndLoadData();
        }
    }

    @Override
    public void showBookmarkDialog(View atView, FTNoteshelfPage page) {
        new FTBookmarkDialog(page).show(atView, getChildFragmentManager());
    }

    private List<FTNoteshelfPage> filterBookmarkedPages(List<FTNoteshelfPage> pages) {
        List<FTNoteshelfPage> bookmarkedPages = new ArrayList<>();
        for (FTNoteshelfPage page : pages) {
            if (page.isBookmarked && !bookmarkedPages.contains(page)) {
                bookmarkedPages.add(page);
            }
        }
        return bookmarkedPages;
    }

    private void checkForBookmarks() {
        if (this.isFinderInBookmarkState && filterBookmarkedPages(thumbnailItemAdapter.getData()).isEmpty()) {
            mNoBookmarksLayout.setVisibility(View.VISIBLE);
        } else {
            mNoBookmarksLayout.setVisibility(View.GONE);
        }
    }
    //endregion

    //region Select All
    @OnClick(R.id.finder_select_all_text_view)
    void onSelectAllButtonClick() {
        FTFirebaseAnalytics.logEvent("Finder_EditPage_SelectAll");
        if (isEditMode || isExportMode) {
            if (mSelectedPages.size() == thumbnailItemAdapter.getItemCount()) {
                mSelectedPages.clear();
            } else {
                mSelectedPages.clear();
                mSelectedPages.addAll(thumbnailItemAdapter.getData());
            }
            thumbnailItemAdapter.notifyDataSetChanged();
            onSelectUpdateUI();
        } else {
            thumbnailListener.closePanel();
        }
    }

    @Override
    public void onSelectUpdateUI() {
        if (!mSelectedPages.isEmpty()) {
            if (mSelectedPages.size() == thumbnailItemAdapter.getItemCount()) {
                mSelectAllTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.check_all_on, 0, 0, 0);
            } else {
                mSelectAllTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.check_all, 0, 0, 0);
            }
            mTitleTextView.setText(getString(R.string.num_selected, mSelectedPages.size()));
        } else {
            mTitleTextView.setText(R.string.select_pages);
            mSelectAllTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.check_all, 0, 0, 0);
        }
        changeButtonsState(mSelectedPages.size() > 0);
    }

    private void changeButtonsState(boolean enable) {
        float buttonAlpha = enable ? 1.0f : 0.5f;

        mDeleteButton.setAlpha(buttonAlpha);
        mDeleteButton.setClickable(enable);
        mDuplicateButton.setAlpha(buttonAlpha);
        mDuplicateButton.setClickable(enable);
        mMoveButton.setAlpha(buttonAlpha);
        mMoveButton.setClickable(enable);
    }
    //endregion

    //region Search
    @Override
    public boolean onQueryTextSubmit(String query) {
        FTFirebaseAnalytics.logEvent("Finder_Search");
        if (this.searchOptions != null) {
            if (!this.searchOptions.searchedKeyword.equals(query) || (this.isExportMode && !this.searchKey.equals(query))) {
                FTLog.crashlyticsLog("UI: Search query submitted in finder");
                if (isExportMode) {
                    this.searchKey = query;
                } else {
                    this.searchOptions.searchedKeyword = query;
                    this.searchOptions.searchPageResults.clear();
                    ObservingService.getInstance().postNotification("onSearchKeyChanged_" + currentDocument.getDocumentUUID(), query);
                }
                updateSearchFilterAndLoadData();
            }
        }
        mSearchView.clearFocus();
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        if (searchOptions != null) {
            if (!searchOptions.searchedKeyword.isEmpty() && newText.isEmpty()) {
                this.searchOptions.searchedKeyword = "";
                this.searchOptions.searchPageResults.clear();

                AsyncTask.execute(() -> {
                    ArrayList<FTNoteshelfPage> pages = this.currentDocument.pages(getActivity());
                    for (int p = 0; p < pages.size(); p++) {
                        if (!this.searchOptions.searchedKeyword.isEmpty() || !this.searchKey.isEmpty())
                            break;
                        pages.get(p).clearSearchableItems();
                    }
                });

                if (isExportMode) {
                    this.searchKey = "";
                }

                this.thumbnailListener.removeHighlighters();
                updateSearchFilterAndLoadData();
            }
        }
        if (TextUtils.isEmpty(newText)) {
            mIndexingDialog.setVisibility(View.GONE);
        }
        return true;
    }

    private void updateSearchFilterAndLoadData() {
        this.thumbnailItemAdapter.clearData();
        this.bookmarkItemAdapter.clear();

        List<FTNoteshelfPage> pages = null;
        if (this.searchOptions != null) {
            if (!isExportMode && !this.searchOptions.searchedKeyword.isEmpty() && !this.searchOptions.searchPageResults.isEmpty()) {
                pages = this.searchOptions.searchPageResults;
            } else if ((!this.searchOptions.searchedKeyword.isEmpty() && !isExportMode) || (isExportMode && !this.searchKey.isEmpty())) {
                String searchKey;
                if (isExportMode) {
                    searchKey = this.searchKey;
                } else {
                    searchKey = this.searchOptions.searchedKeyword;
                }
                currentDocument.searchDocumentForKey(getContext(), searchKey, cancelled -> {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            FTThumbnailFragment.this.checkForBookmarks();
                            if (!TextUtils.isEmpty(this.searchOptions.searchedKeyword) && this.searchOptions.searchPageResults.isEmpty()) {
                                mNoPagesFoundTextView.setVisibility(View.VISIBLE);
                                mThumbnailRecyclerView.setVisibility(View.GONE);
                                mBookmarkRecyclerView.setVisibility(View.GONE);
                            } else {
                                mNoPagesFoundTextView.setVisibility(View.GONE);
                            }
                        });
                    }
                });
            } else {
                pages = this.currentDocument.pages(getContext());
            }
        }

        if (pages != null && !pages.isEmpty()) {
            if (isFinderInBookmarkState) {
                pages = filterBookmarkedPages(pages);
                this.bookmarkItemAdapter.addAll(pages);
                this.thumbnailItemAdapter.setData(pages);
                checkForBookmarks();
            } else {
                mNoBookmarksLayout.setVisibility(View.GONE);
                this.thumbnailItemAdapter.setData(pages);
            }
            mNoPagesFoundTextView.setVisibility(View.GONE);
            this.thumbnailItemAdapter.notifyDataSetChanged();
        }

        if (this.searchOptions != null) {
            if (!this.searchOptions.searchedKeyword.isEmpty() || isFinderInBookmarkState) {
                gestureManager.setLongPressDragEnabled(false);
                gestureManager.setSwipeEnabled(false);
            } else {
                gestureManager.setLongPressDragEnabled(true);
                gestureManager.setSwipeEnabled(true);
            }
        } else {
            gestureManager.setLongPressDragEnabled(true);
            gestureManager.setSwipeEnabled(true);
        }

        if (isFinderInBookmarkState && FTApp.getPref().get(SystemPref.FINDER_SHOWING_BOOKMARK_TITLES, false)) {
            mThumbnailRecyclerView.setVisibility(View.GONE);
        }
    }
    //endregion

    //region Adapter Callbacks
    public void displayThumbnailAsPage(int position) {
        FTFirebaseAnalytics.logEvent("Finder_TapPage");
        thumbnailListener.scrollToPageAtIndex(position);
        mThumbnailRecyclerView.setAdapter(thumbnailItemAdapter);
        setSelectMode(false);
        thumbnailListener.closePanel();
    }

    @Override
    public void swapPages(int fromPosition, int toIndex) {
        ArrayList<FTNoteshelfPage> movablePage = new ArrayList<>();
        FTNoteshelfPage page = currentDocument.pages(getContext()).get(fromPosition);
        page.setPageDirty(true);
        movablePage.add(page);
        if (!searchOptions.searchPageResults.isEmpty()) {
            Collections.swap(thumbnailItemAdapter.getData(), fromPosition, toIndex);
        }
        this.currentDocument.movePages(movablePage, toIndex);
        saveDocument(null);
//        setSelectMode(false);
    }

    @Override
    public void noBookmarkedPages() {
        mNoBookmarksLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean isShowingBookmarks() {
        return isFinderInBookmarkState;
    }

    @Override
    public boolean isExportMode() {
        return isExportMode;
    }

    @Override
    public boolean isEditMode() {
        return isEditMode;
    }

    @Override
    public ArrayList<FTNoteshelfPage> selectedPages() {
        return mSelectedPages;
    }

    @Override
    public int currentPageIndex() {
        return thumbnailListener.currentPageIndex();
    }
    //endregion

    //region Saving Doc
    private void saveDocument(final FTSmartDialog smartDialog) {
        if (isVisible())
            this.currentDocument.saveNoteshelfDocument(getContext(), (success, error) -> Objects.requireNonNull(getActivity()).runOnUiThread(() -> {
                new Handler().postDelayed(() -> {
                    thumbnailListener.reloadDocumentData();
                    if (smartDialog != null) {
                        mSelectedPages.clear();
                    }
                    if (!isFinderInBookmarkState)
                        updateSearchFilterAndLoadData();
                    thumbnailItemAdapter.notifyDataSetChanged();
                    if (smartDialog != null)
                        smartDialog.dismiss();
                }, 200); //Delay has given to show "Deleting..." for a moment
            }));
    }
    //endregion

    //region Export
    public void setToExportMode(@NonNull ExportPagesCallback callback, boolean isExportMode) {
        this.callback = callback;
        this.isExportMode = isExportMode;
    }

    public void notifyDataSetChanged(int currentPosition) {
        thumbnailItemAdapter.notifyItemChanged(currentPosition);
        mThumbnailRecyclerView.scrollToPosition(currentPosition);

        if (isFinderInBookmarkState && FTApp.getPref().get(SystemPref.FINDER_SHOWING_BOOKMARK_TITLES, false)) {
            mThumbnailRecyclerView.setVisibility(View.GONE);
        }
    }

    public void addItem(int atIndex, FTNoteshelfPage page) {
        List<FTNoteshelfPage> pages = thumbnailItemAdapter.getData();
        if (pages.stream().noneMatch(p -> p.uuid.equalsIgnoreCase(page.uuid)) && atIndex <= this.thumbnailItemAdapter.getItemCount())
            thumbnailItemAdapter.getData().add(atIndex, page);

        if (isFinderInBookmarkState && FTApp.getPref().get(SystemPref.FINDER_SHOWING_BOOKMARK_TITLES, false)) {
            mThumbnailRecyclerView.setVisibility(View.GONE);
        }
    }

    public void updateAll(List<FTNoteshelfPage> pages) {
        //This seems like getting null pointer when called before the fragment is initialized. So added null check
        if (pages != null && thumbnailItemAdapter != null) {
            updateSearchFilterAndLoadData();
        }
    }

    public void onDrawerClosed() {
        List<Fragment> fragments = getChildFragmentManager().getFragments();
        for (Fragment fragment : fragments) {
            if (fragment.isAdded())
                getChildFragmentManager().beginTransaction().remove(fragment).commitAllowingStateLoss();
        }
    }
    //endregion

    public interface FTThumbnailListener {
        void scrollToPageAtIndex(int indexToShow);

        void reloadDocumentData();

        void closePanel();

        void closeAudioToolbar();

        void removeHighlighters();

        FTNoteshelfDocument currentDocument();

        int currentPageIndex();

        FTFinderSearchOptions searchOptions();
    }

    public interface ExportPagesCallback {
        void onPagesSelected(List<FTNoteshelfPage> pages);
    }
    //endregion
}