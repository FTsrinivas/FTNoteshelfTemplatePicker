//package com.fluidtouch.noteshelf.clipart.dialog;
//
//import android.app.Dialog;
//import android.content.DialogInterface;
//import android.graphics.Color;
//import android.os.Bundle;
//import android.text.TextUtils;
//import android.view.Gravity;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.view.Window;
//import android.widget.ImageView;
//import android.widget.LinearLayout;
//import android.widget.SearchView;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.recyclerview.widget.GridLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.fluidtouch.noteshelf.FTApp;
//import com.fluidtouch.noteshelf.clipart.adapters.ClipartAdapterCallback;
//import com.fluidtouch.noteshelf.clipart.adapters.FTLibraryClipartAdapter;
//import com.fluidtouch.noteshelf.clipart.adapters.FTRecentClipartAdapter;
//import com.fluidtouch.noteshelf.clipart.models.ClipartDialogMode;
//import com.fluidtouch.noteshelf.clipart.models.pixabay.Clipart;
//import com.fluidtouch.noteshelf.clipart.providers.ClipartProviderListener;
//import com.fluidtouch.noteshelf.clipart.providers.FTClipartProvider;
//import com.fluidtouch.noteshelf.clipart.providers.FTLocalClipartProvider;
//import com.fluidtouch.noteshelf.commons.FTLog;
//import com.fluidtouch.noteshelf.commons.ui.FTBaseDialog;
//import com.fluidtouch.noteshelf.commons.ui.FTSmartDialog;
//import com.fluidtouch.noteshelf.commons.utils.FTNetworkConnectionUtil;
//import com.fluidtouch.noteshelf.commons.utils.ScreenUtil;
//import com.fluidtouch.noteshelf.preferences.SystemPref;
//import com.fluidtouch.noteshelf.services.FTFirebaseAnalytics;
//import com.fluidtouch.noteshelf.shelf.activities.FTGridLayoutManager;
//import com.fluidtouch.noteshelf2.R;
//
//import org.jetbrains.annotations.NotNull;
//
//import java.util.Collections;
//import java.util.List;
//
//import butterknife.BindView;
//import butterknife.ButterKnife;
//import butterknife.OnClick;
//
//public class FTClipartDialog extends FTBaseDialog.Popup implements ClipartAdapterCallback, ClipartProviderListener, SearchView.OnQueryTextListener {
//    //region View Bindings
//    @BindView(R.id.clipart_search_view)
//    SearchView clipartSearchView;
//    @BindView(R.id.clipart_dialog_recent_tab)
//    TextView recentTextView;
//    @BindView(R.id.clipart_dialog_library_tab)
//    TextView libraryTextView;
//    @BindView(R.id.clipart_library_recycler_view)
//    RecyclerView mLibraryRecyclerView;
//    @BindView(R.id.clipart_recent_recycler_view)
//    RecyclerView mRecentRecyclerView;
//    @BindView(R.id.clipart_dialog_error_image_view)
//    ImageView errorImageView;
//    @BindView(R.id.clipart_dialog_error_title)
//    TextView errorTitleTextView;
//    @BindView(R.id.clipart_dialog_error_desc)
//    TextView errorDescTextView;
//    @BindView(R.id.clipart_dialog_progress_layout)
//    LinearLayout clipartProgressLayout;
//    //endregion
//
//    //region Member Variables
//    private FTClipartProvider clipartProvider;
//    private FTLocalClipartProvider localClipartProvider;
//    private FTRecentClipartAdapter mRecentClipartAdapter;
//    private FTLibraryClipartAdapter mLibraryClipartAdapter;
//    private com.fluidtouch.noteshelf.clipart.models.ClipartDialogMode mode = com.fluidtouch.noteshelf.clipart.models.ClipartDialogMode.LIBRARY;
//    private String prevSearchKey = "";
//    private FTSmartDialog smartDialog = new FTSmartDialog();
//    //endregion
//
//    @NonNull
//    @Override
//    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
//        Dialog dialog = super.onCreateDialog(savedInstanceState);
//        if (!isMobile()) {
//            Window window = dialog.getWindow();
//            if (window != null) {
//                window.setGravity(Gravity.TOP | Gravity.START);
//            }
//        }
//        return dialog;
//    }
//
//    //region Lifecycle methods
//    @Nullable
//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        return inflater.inflate(R.layout.popup_clipart, container, false);
//    }
//
//    @Override
//    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
//        super.onViewCreated(view, savedInstanceState);
//        ButterKnife.bind(this, view);
//
//        this.clipartProvider = new FTClipartProvider();
//        this.localClipartProvider = new FTLocalClipartProvider();
//
//        clipartSearchView.setOnQueryTextListener(this);
//
//        FTGridLayoutManager layoutManager = new FTGridLayoutManager(getContext(), ScreenUtil.convertDpToPx(getContext(), 94));
//        mLibraryRecyclerView.setLayoutManager(layoutManager);
//
//        mLibraryRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
//            @Override
//            public void onScrolled(@NotNull RecyclerView recyclerView,
//                                   int dx, int dy) {
//                super.onScrolled(recyclerView, dx, dy);
//                GridLayoutManager gridLayoutManager = (GridLayoutManager) recyclerView.getLayoutManager();
//                if (gridLayoutManager != null) {
//                    int lasPos = gridLayoutManager.findLastVisibleItemPosition() + 1;
//                    int diff = mLibraryClipartAdapter.getItemCount() - lasPos;
//                    if (mLibraryClipartAdapter.getItemCount() == 0 || diff >= 28) {
//                        clipartProvider.getNextPage(FTClipartDialog.this);
//                    }
//                }
//            }
//        });
//
//        mLibraryClipartAdapter = new FTLibraryClipartAdapter(this);
//        mLibraryRecyclerView.setAdapter(mLibraryClipartAdapter);
//        mRecentClipartAdapter = new FTRecentClipartAdapter(this);
//        mRecentRecyclerView.setAdapter(mRecentClipartAdapter);
//
//        if (FTApp.getPref().get(SystemPref.CLIPART_RECENT_CLICKED, false)) {
//            onRecentClicked();
//        } else {
//            onLibraryClicked();
//        }
//
//        //SearchView custom modification
//        View closeButton = clipartSearchView.findViewById(getContext().getResources().getIdentifier("android:id/search_close_btn", null, null));
//        closeButton.setClickable(false);
//        closeButton.setBackgroundColor(Color.TRANSPARENT);
//    }
//    //endregion
//
//    //region Search
//    @Override
//    public boolean onQueryTextSubmit(String query) {
//        searchForClipart(query);
//        return false;
//    }
//
//    @Override
//    public boolean onQueryTextChange(String newText) {
//        if (!TextUtils.isEmpty(prevSearchKey) && TextUtils.isEmpty(newText)) {
//            searchForClipart(newText);
//        }
//        return false;
//    }
//
//    private void searchForClipart(String searchQuery) {
//        updateErrorUI(false);
//        if (mode.equals(ClipartDialogMode.RECENT)) {
//            mRecentClipartAdapter.clearData();
//            localClipartProvider.searchInRecentCliparts(searchQuery, FTClipartDialog.this);
//        } else if (mode.equals(ClipartDialogMode.LIBRARY)) {
//            if (!searchQuery.equals(prevSearchKey))
//                mLibraryClipartAdapter.clear();
//            if (FTNetworkConnectionUtil.INSTANCE.isNetworkAvailable(getContext())) {
//                clipartProgressLayout.setVisibility(View.VISIBLE);
//                clipartProvider.searchClipartInLibrary(searchQuery, FTClipartDialog.this);
//            } else {
//                updateErrorUI(true);
//            }
//        }
//        prevSearchKey = searchQuery;
//    }
//    //endregion
//
//    @OnClick(R.id.clipart_dialog_back_button)
//    void onBackClicked() {
//        dismiss();
//    }
//
//    @OnClick(R.id.clipart_dialog_recent_tab)
//    void onRecentClicked() {
//        FTLog.crashlyticsLog("UI: Selected recents for cliparts");
//        FTFirebaseAnalytics.logEvent("inside_document", "popup_clipart", "recent_clipart");
//        FTApp.getPref().save(SystemPref.CLIPART_RECENT_CLICKED, true);
//
//        mode = com.fluidtouch.noteshelf.clipart.models.ClipartDialogMode.RECENT;
//        clipartProvider.terminateCurrentTask();
//
//        updateTabSelectionUI();
//
//        mRecentRecyclerView.setVisibility(View.VISIBLE);
//        mLibraryRecyclerView.setVisibility(View.GONE);
//
//        localClipartProvider.searchInRecentCliparts(prevSearchKey, this);
//    }
//
//    @OnClick(R.id.clipart_dialog_library_tab)
//    void onLibraryClicked() {
//        FTLog.crashlyticsLog("UI: Selected library for cliparts");
//        FTFirebaseAnalytics.logEvent("inside_document", "popup_clipart", "library_clipart");
//        FTApp.getPref().save(SystemPref.CLIPART_RECENT_CLICKED, false);
//
//        mode = com.fluidtouch.noteshelf.clipart.models.ClipartDialogMode.LIBRARY;
//
//        updateTabSelectionUI();
//
//        mLibraryRecyclerView.setVisibility(View.VISIBLE);
//        mRecentRecyclerView.setVisibility(View.GONE);
//
//        if (FTNetworkConnectionUtil.INSTANCE.isNetworkAvailable(getContext())) {
//            if (mLibraryClipartAdapter.getItemCount() == 0) {
//                clipartProgressLayout.setVisibility(View.VISIBLE);
//                clipartProvider.searchClipartInLibrary(prevSearchKey, this);
//            }
//            updateErrorUI(false);
//        } else {
//            updateErrorUI(true);
//        }
//    }
//
//    @OnClick(R.id.clipart_dialog_error_image_view)
//    void onReloadClicked() {
//        if (mode.equals(com.fluidtouch.noteshelf.clipart.models.ClipartDialogMode.LIBRARY)) {
//            searchForClipart(prevSearchKey);
//        }
//    }
//
//    private void updateTabSelectionUI() {
//        if (mode.equals(com.fluidtouch.noteshelf.clipart.models.ClipartDialogMode.RECENT)) {
//            recentTextView.setBackgroundResource(R.drawable.tab_selector_item_bg);
//            libraryTextView.setBackgroundResource(android.R.color.transparent);
//            recentTextView.setClickable(false);
//            libraryTextView.setClickable(true);
//        } else {
//            libraryTextView.setBackgroundResource(R.drawable.tab_selector_item_bg);
//            recentTextView.setBackgroundResource(android.R.color.transparent);
//            libraryTextView.setClickable(false);
//            recentTextView.setClickable(true);
//        }
//    }
//
//    //region Provider Callbacks
//    @Override
//    public void clipartData(List<Clipart> clipartList, int errorCode) {
//        if (getDialog() != null && isAdded()) {
//            if (clipartList != null) {
//                switch (mode) {
//                    case RECENT:
//                        if (clipartList.isEmpty()) {
//                            updateErrorUI(true);
//                        } else {
//                            updateErrorUI(false);
//                            mRecentClipartAdapter.clearData();
//                            mRecentClipartAdapter.setData(clipartList);
//                            mRecentClipartAdapter.notifyDataSetChanged();
//                        }
//                        break;
//                    case LIBRARY:
//                        if (clipartList.isEmpty()) {
//                            errorTitleTextView.setVisibility(View.VISIBLE);
//                            errorImageView.setVisibility(View.VISIBLE);
//                            errorImageView.setImageResource(R.drawable.emptycliparts);
//                            errorTitleTextView.setText(R.string.no_matches_found);
//                        } else {
//                            errorImageView.setVisibility(View.GONE);
//                            errorTitleTextView.setVisibility(View.GONE);
//                            updateErrorUI(false);
//                            mLibraryClipartAdapter.addAll(clipartList);
//                            mLibraryClipartAdapter.notifyDataSetChanged();
//                        }
//                        break;
//                }
//            } else {
//                if (mode.equals(ClipartDialogMode.LIBRARY) && errorCode != 0) {
//                    if (!FTNetworkConnectionUtil.INSTANCE.isNetworkAvailable(getContext())) {
//                        updateErrorUI(true);
//                    } else if (errorCode == 404) {
//                        updateErrorUI(true);
//                    }
//                }
//            }
//            clipartProgressLayout.setVisibility(View.GONE);
//        }
//    }
//
//    @Override
//    public void onClipartDownloaded(String path) {
//        if (getDialog() != null && isAdded()) {
//            if (smartDialog != null && smartDialog.isAdded()) {
//                smartDialog.dismissAllowingStateLoss();
//            }
//            getDialog().setCanceledOnTouchOutside(false);
//            if (!TextUtils.isEmpty(path) && getActivity() != null) {
//                ((ClipartDialogListener) getActivity()).setClipartImageAnnotation(path);
//                dismissAllowingStateLoss();
//            } else {
//                Toast.makeText(getContext(), R.string.unable_to_download_clipart, Toast.LENGTH_SHORT).show();
//            }
//        }
//    }
//    //endregion
//
//    //region Adapter Callback
//    @Override
//    public void onClipartSelected(int position, boolean delete) {
//        FTFirebaseAnalytics.logEvent("inside_document", "popup_clipart", "clipart_selected");
//
//        if (getDialog() != null)
//            getDialog().setCanceledOnTouchOutside(true);
//
//        if (mode.equals(ClipartDialogMode.RECENT)) {
//            Clipart clipart = mRecentClipartAdapter.getItem(position);
//            if (delete) {
//                mRecentClipartAdapter.remove(position);
//                mRecentClipartAdapter.notifyDataSetChanged();
//                updateErrorUI(mRecentClipartAdapter.getItemCount() == 0);
//                localClipartProvider.deleteClipart(clipart);
//            } else {
//                onClipartDownloaded(localClipartProvider.getClipartImagePath(clipart.getId()));
//            }
//        } else {
//            if (FTNetworkConnectionUtil.INSTANCE.isNetworkAvailable(getContext())) {
//                if (smartDialog != null) {
//                    smartDialog.setMode(FTSmartDialog.FTSmartDialogMode.SPINNER)
//                            .setMessage(getString(R.string.downloading))
//                            .show(getChildFragmentManager());
//                }
//                localClipartProvider.saveToRecents(mLibraryClipartAdapter.getItem(position), this);
//            }
//            updateErrorUI(!FTNetworkConnectionUtil.INSTANCE.isNetworkAvailable(getContext()));
//        }
//    }
//
//    @Override
//    public void updateClipartsOrder(int from, int to) {
//        Collections.swap(mRecentClipartAdapter.getData(), from, to);
//        localClipartProvider.saveAllRecentCliparts(mRecentClipartAdapter.getData());
//    }
//    //endregion
//
//    private void updateErrorUI(boolean show) {
//        errorImageView.setVisibility(show ? View.VISIBLE : View.GONE);
//        errorTitleTextView.setVisibility(show ? View.VISIBLE : View.GONE);
//        errorDescTextView.setVisibility(show && mode.equals(ClipartDialogMode.LIBRARY) ? View.VISIBLE : View.GONE);
//
//        errorImageView.setImageResource(show && mode.equals(ClipartDialogMode.RECENT) ? R.drawable.emptycliparts : R.drawable.emptyconnection);
//        errorTitleTextView.setText(show && mode.equals(ClipartDialogMode.RECENT) ? R.string.no_recents_cliparts : R.string.no_internet_connection);
//
//        if (mode.equals(ClipartDialogMode.RECENT)) {
//            mRecentRecyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
//            errorDescTextView.setVisibility(View.GONE);
//        } else {
//            mLibraryRecyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
//            errorDescTextView.setVisibility(show ? View.VISIBLE : View.GONE);
//            if (show)
//                errorDescTextView.setText(R.string.check_your_connection_and_tap_here_to_reload);
//        }
//    }
//
//    @Override
//    public void onDismiss(@NonNull DialogInterface dialog) {
//        super.onDismiss(dialog);
//        clipartProvider.terminateCurrentTask();
//    }
//
//    public interface ClipartDialogListener {
//        void setClipartImageAnnotation(String clipartImagePath);
//    }
//}