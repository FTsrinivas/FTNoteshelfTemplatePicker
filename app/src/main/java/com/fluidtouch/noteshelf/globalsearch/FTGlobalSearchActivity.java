package com.fluidtouch.noteshelf.globalsearch;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf.commons.FTLog;
import com.fluidtouch.noteshelf.commons.ui.FTBaseActivity;
import com.fluidtouch.noteshelf.commons.ui.FTSmartDialog;
import com.fluidtouch.noteshelf.commons.utils.ObservingService;
import com.fluidtouch.noteshelf.document.FTDocumentActivity;
import com.fluidtouch.noteshelf.documentframework.FTNoteshelfDocument.CompletionBlock;
import com.fluidtouch.noteshelf.documentframework.FTNoteshelfDocument.FTNoteshelfDocument;
import com.fluidtouch.noteshelf.documentframework.FTNoteshelfDocument.FTNoteshelfPage;
import com.fluidtouch.noteshelf.documentframework.ThumbnailGenerator.FTThumbnailGenerator;
import com.fluidtouch.noteshelf.documentframework.Utilities.FTConstants;
import com.fluidtouch.noteshelf.documentproviders.FTShelfCollectionProvider;
import com.fluidtouch.noteshelf.documentproviders.FTShelfItemCollection;
import com.fluidtouch.noteshelf.globalsearch.adapters.FTGlobalSearchAdapter;
import com.fluidtouch.noteshelf.globalsearch.models.FTSearchResultBook;
import com.fluidtouch.noteshelf.globalsearch.models.FTSearchResultPage;
import com.fluidtouch.noteshelf.globalsearch.models.FTSearchSection;
import com.fluidtouch.noteshelf.globalsearch.models.FTSearchSectionTitle;
import com.fluidtouch.noteshelf.globalsearch.processsors.FTSearchProcessor;
import com.fluidtouch.noteshelf.globalsearch.processsors.FTSearchProcessorFactory;
import com.fluidtouch.noteshelf.models.disk.diskItem.shelfItem.FTGroupItem;
import com.fluidtouch.noteshelf.models.disk.diskItem.shelfItem.FTShelfItem;
import com.fluidtouch.noteshelf.preferences.SystemPref;
import com.fluidtouch.noteshelf.services.FTFirebaseAnalytics;
import com.fluidtouch.noteshelf.shelf.enums.FTShelfSortOrder;
import com.fluidtouch.noteshelf2.R;
import com.fluidtouch.renderingengine.renderer.FTRenderMode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Sreenu on 2019-07-23
 */

public class FTGlobalSearchActivity extends FTBaseActivity implements FTGlobalSearchAdapter.GlobalSearchAdapterCallback, SearchView.OnQueryTextListener {
    @BindView(R.id.global_search_view)
    protected SearchView mGlobalSearchView;
    @BindView(R.id.global_search_recycler_view)
    protected RecyclerView mSearchRecyclerView;
    @BindView(R.id.global_search_progress_bar)
    protected ProgressBar mProgressBar;
    @BindView(R.id.search_search_layout)
    ConstraintLayout searchLayout;
    @BindView(R.id.global_search_indexing_message)
    TextView mIndexingMessage;

    private boolean isPreparedForSearch = false;
    private String mSearchKey = "";
    private FTSearchProcessor mSearchProcessor;
    private FTGlobalSearchAdapter mSearchAdapter;
    private ArrayList<FTNoteshelfDocument> noteshelfDocuments = new ArrayList<>();
    private boolean isBookOpening = false;
    private boolean isSearchInProgress = false;
    public FTGlobalSearchCallback globalSearchCallback = new FTGlobalSearchCallback() {
        @Override
        public void onSectionFinding(FTSearchSection searchItem) {
            runOnUiThread(() -> {
                mSearchAdapter.getAll().add(searchItem);
                mSearchAdapter.addAll(((FTSearchSectionTitle) searchItem).items);
                if (((FTSearchSectionTitle) searchItem).items.get(0) instanceof FTSearchResultPage) {
                    GridLayoutManager layoutManager = (GridLayoutManager) mSearchRecyclerView.getLayoutManager();
                    if (layoutManager != null && !isBookOpening) {
                        FTThumbnailGenerator.sharedThumbnailGenerator(FTRenderMode.thumbnailGen).cancelAllThumbnailGeneration();
                        new Handler().postDelayed(() -> mSearchAdapter.onScrollingStops(layoutManager.findFirstVisibleItemPosition(), layoutManager.findLastVisibleItemPosition()), 100);
                    }
                }
                //Dealloc items array before next item
                ((FTSearchSectionTitle) searchItem).items.clear();
                ((FTSearchSectionTitle) searchItem).items = null;
            });
        }

        @Override
        public void onSearchFinding(FTNoteshelfDocument document) {
            document.closePdfDocuments();
            noteshelfDocuments.add(document);
        }

        @Override
        public void onSearchingFinished() {
            runOnUiThread(() -> {
                isSearchInProgress = false;
                if (mIndexingMessage.getVisibility() == View.GONE)
                    mProgressBar.setVisibility(View.GONE);
            });
        }
    };


    private final Observer mPageIndexingObserver = new Observer() {
        @Override
        public void update(Observable observable, Object o) {
            runOnUiThread(() -> {
                if (!isFinishing()) {
                    if (o == null) {
                        if (!isSearchInProgress) mProgressBar.setVisibility(View.GONE);
                        mIndexingMessage.setVisibility(View.GONE);
                    } else {
                        FTNoteshelfPage page = (FTNoteshelfPage) o;
                        mProgressBar.setVisibility(View.VISIBLE);
                        mIndexingMessage.setText("Indexing " + page.getParentDocument().getDisplayTitle(FTGlobalSearchActivity.this));
                        mIndexingMessage.setVisibility(View.VISIBLE);
                    }
                }
            });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_global_search);
        ButterKnife.bind(this);
        setUpToolbarTheme();
        //Set SearchView
        mGlobalSearchView.requestFocusFromTouch();
        View closeButton = mGlobalSearchView.findViewById(getResources().getIdentifier("android:id/search_close_btn", null, null));
        closeButton.setClickable(false);
        closeButton.setBackgroundColor(Color.TRANSPARENT);
        int id = mGlobalSearchView.getContext().getResources().getIdentifier("android:id/search_src_text", null, null);
        TextView textView = mGlobalSearchView.findViewById(id);
        textView.setTextColor(Color.WHITE);
        textView.setHintTextColor(Color.parseColor("#808080"));
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        mGlobalSearchView.setOnQueryTextListener(this);
        //Set recyclerView and adapter
        mSearchRecyclerView.setHasFixedSize(false);
        mSearchRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    GridLayoutManager layoutManager = (GridLayoutManager) mSearchRecyclerView.getLayoutManager();
                    if (layoutManager != null) {
                        FTThumbnailGenerator.sharedThumbnailGenerator(FTRenderMode.thumbnailGen).cancelAllThumbnailGeneration();
                        mSearchAdapter.onScrollingStops(layoutManager.findFirstVisibleItemPosition(), layoutManager.findLastVisibleItemPosition());
                    }
                }
            }
        });
        GridLayoutManager layoutManager = (GridLayoutManager) mSearchRecyclerView.getLayoutManager();
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (mSearchAdapter.getItemViewType(position) == FTGlobalSearchAdapter.CONTENT)
                    return 1;
                else
                    return 5;
            }
        });
        mSearchRecyclerView.setHasFixedSize(true);
        mSearchAdapter = new FTGlobalSearchAdapter(this);
        mSearchRecyclerView.setAdapter(mSearchAdapter);
        //Prepare all categories and notebooks for search
        ObservingService.getInstance().addObserver("pageIndexing", mPageIndexingObserver);

        mSearchProcessor = FTSearchProcessorFactory.getProcessor(FTGlobalSearchActivity.this, FTGlobalSearchType.ALL, globalSearchCallback);
        preLoadSearchData();
    }

    @Override
    public void setUpToolbarTheme() {
        super.setUpToolbarTheme();
        int color = Color.parseColor(FTApp.getPref().get(SystemPref.SELECTED_THEME_TOOLBAR_COLOR, FTConstants.DEFAULT_THEME_TOOLBAR_COLOR));
        searchLayout.setBackgroundColor(color);
    }

    @Override
    protected void onResume() {
        super.onResume();
        isBookOpening = false;
        if (!mSearchKey.isEmpty()) {
            new Handler().postDelayed(() -> {
                View view = this.getCurrentFocus();
                if (view != null) {
                    InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.SHOW_FORCED);
                }
            }, 100);
        }
    }

    @OnClick(R.id.global_search_close_image_view)
    void closeSearch() {
        onBackPressed();
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        if (query.length() < 3) {
            Toast.makeText(this, R.string.please_enter_atleast_3_characters, Toast.LENGTH_LONG).show();
            return true;
        }
        if (!query.equals(mSearchKey) && mSearchProcessor != null && isPreparedForSearch) {
            if (mProgressBar.getVisibility() == View.GONE) {
                isSearchInProgress = true;
                mProgressBar.setVisibility(View.VISIBLE);
            }
            mSearchAdapter.clear();
            mSearchProcessor.cancelSearching();
            mSearchKey = query;
            View view = this.getCurrentFocus();
            if (view != null) {
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
            mSearchProcessor.startProcessing(mSearchKey);
        }
        mGlobalSearchView.clearFocus();
        return true;
    }

    @Override
    public boolean onQueryTextChange(String query) {
        if (TextUtils.isEmpty(query)) {
            mSearchAdapter.clear();
            mSearchKey = "";
        }
        return false;
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        new Handler(Looper.myLooper()).postDelayed(() -> {
            GridLayoutManager layoutManager = (GridLayoutManager) mSearchRecyclerView.getLayoutManager();
            if (layoutManager != null) {
                FTThumbnailGenerator.sharedThumbnailGenerator(FTRenderMode.thumbnailGen).cancelAllThumbnailGeneration();
                mSearchAdapter.onScrollingStops(layoutManager.findFirstVisibleItemPosition(), layoutManager.findLastVisibleItemPosition());
            }
        }, 100);
    }

    @Override
    public void onBackPressed() {
        mProgressBar.setVisibility(View.GONE);
        mIndexingMessage.setVisibility(View.GONE);

        mSearchAdapter.clear();

        if (mSearchProcessor != null) {
            mSearchProcessor.cancelSearching();
        }

        for (FTNoteshelfDocument document : noteshelfDocuments) {
            document.closePdfDocuments();
            document.closePdfDocument();
        }
        noteshelfDocuments.clear();
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ObservingService.getInstance().removeObserver("pageIndexing", mPageIndexingObserver);
        finish();
    }

    @Override
    public void openSelectedDocument(Object item) {
        if (isBookOpening)
            return;
        isBookOpening = true;
        mGlobalSearchView.clearFocus();
        FTLog.crashlyticsLog("Opening Search Result notebook");
        String searchKey = "";
        final FTSmartDialog smartDialog = new FTSmartDialog()
                .setMode(FTSmartDialog.FTSmartDialogMode.SPINNER)
                .setMessage(getString(R.string.opening))
                .show(getSupportFragmentManager());
        if (item instanceof FTSearchResultPage) {
            FTFirebaseAnalytics.logEvent("Shelf_Search_OpenNB");
            FTSearchResultPage page = ((FTSearchResultPage) item);
            searchKey = mSearchKey;
            FTDocumentActivity.openDocument(page.getBookURL(), page.getNoteshelfPage().getParentDocument(), this, page.getPageIndex(), searchKey, new CompletionBlock() {
                @Override
                public void didFinishWithStatus(Boolean success, Error error) {
                    if (smartDialog != null)
                        smartDialog.dismissAllowingStateLoss();
                }
            });
        } else if (item instanceof FTSearchResultBook) {
            FTFirebaseAnalytics.logEvent("Shelf_Search_OpenNB");
            FTSearchResultBook book = ((FTSearchResultBook) item);
            FTDocumentActivity.openDocument(book.getFileURL(), this, 0, searchKey, new CompletionBlock() {
                @Override
                public void didFinishWithStatus(Boolean success, Error error) {
                    if (smartDialog != null && smartDialog.isAdded())
                        smartDialog.dismissAllowingStateLoss();
                }
            });
        }
    }


    private void preLoadSearchData() {
        AsyncTask.execute(() -> FTShelfCollectionProvider.getInstance().shelfs(shelfs -> {
            List<FTShelfItem> searchNotebooks = new ArrayList<>();
            Iterator<FTShelfItemCollection> categoryIterator = shelfs.iterator();
            List<FTShelfItemCollection> searchCategories = new ArrayList<>();
            while (categoryIterator.hasNext()) {
                FTShelfItemCollection category = categoryIterator.next();
                if (category.isTrash(this)) {
                    categoryIterator.remove();
                } else {
                    searchCategories.add(category);
                    category.shelfItems(FTGlobalSearchActivity.this, FTShelfSortOrder.BY_NAME, null, "", (notebooks, error) -> {
                        for (FTShelfItem shelfItem : notebooks) {
                            if (shelfItem instanceof FTGroupItem) {
                                searchNotebooks.addAll(((FTGroupItem) shelfItem).getChildren());
                            } else {
                                searchNotebooks.add(shelfItem);
                            }
                        }
                    });
                }
            }
            mSearchProcessor.setDataToProcess(searchCategories, searchNotebooks);
            runOnUiThread(() -> isPreparedForSearch = true);
        }));
    }
}