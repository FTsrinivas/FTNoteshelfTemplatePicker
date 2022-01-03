package com.fluidtouch.noteshelf.store.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf.commons.ui.FTBaseActivity;
import com.fluidtouch.noteshelf.commons.utils.FTNetworkConnectionUtil;
import com.fluidtouch.noteshelf.commons.utils.ObservingService;
import com.fluidtouch.noteshelf.store.adapter.FTStoreAdapter;
import com.fluidtouch.noteshelf.store.data.FTDownloadedStorePackData;
import com.fluidtouch.noteshelf.store.data.FTStorePackData;
import com.fluidtouch.noteshelf.store.model.FTDownloadData;
import com.fluidtouch.noteshelf.store.model.FTStoreMetadata;
import com.fluidtouch.noteshelf.store.model.FTStorePack;
import com.fluidtouch.noteshelf.store.model.FTStorePackItem;
import com.fluidtouch.noteshelf.store.network.FTDownloadDataService;
import com.fluidtouch.noteshelf2.R;
import com.google.gson.Gson;
import com.noteshelf.auth.AppAuthentication;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FTStoreActivity extends FTBaseActivity implements FTStoreCallbacks, AppAuthentication.AuthChangeListener, SearchView.OnQueryTextListener {

    //region Member Variables
    private static final int RC_SIGN_IN = 9001;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.store_recyclerView)
    RecyclerView listStore;
    FTStoreAdapter mFtStoreAdapter;
    ObservingService mDownloadStatusObserver;
    FTStoreMetadata ftStore;
    String storeData;
    private boolean isDownloaded = false;
    private AppAuthentication appAuthentication;
    //endregion

    //region Start Activity
    public static void start(Context context) {
        context.startActivity(new Intent(context, FTStoreActivity.class));
    }
    //endregion

    //region Lifecycle Events
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_store);
        ButterKnife.bind(this);
        mDownloadStatusObserver = new ObservingService();
        setUpToolbar();
        appAuthentication = new AppAuthentication(this);
        appAuthentication.initAuthentication();
        appAuthentication.setCallBackListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        final FTStorePack title = new FTStorePack();
        appAuthentication.setCurrentUser();
        if (appAuthentication.isUserSignedIn()) {
            title.setProfilePic(appAuthentication.getUserPhotoUrl() != null ? appAuthentication.getUserPhotoUrl().toString() : "no-photo");
            title.setTitle(appAuthentication.getUserEmail() != null ? appAuthentication.getUserEmail() : "your Email");
            title.setValid(appAuthentication.isUserEmailVarified());
        }

        FTStorePackData ftStorePackData = new FTStorePackData(this);
        HashMap<String, Object> mStorePackData = ftStorePackData.getStorePackData();
        mStorePackData = (HashMap<String, Object>) mStorePackData.get("downloadableThemes");
        Gson gson = new Gson();
        storeData = gson.toJson(mStorePackData);
        ftStore = gson.fromJson(storeData, FTStoreMetadata.class);
        mFtStoreAdapter = new FTStoreAdapter(this, this, mDownloadStatusObserver);
        mFtStoreAdapter.add(title);
        mFtStoreAdapter.addAll(ftStore.getSections());
        listStore.setAdapter(mFtStoreAdapter);
        listStore.setNestedScrollingEnabled(false);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbar_store_menu, menu);
//        MenuItem searchItem = menu.findItem(R.id.storeSearch);
//
//        SearchView searchView = (SearchView) searchItem.getActionView();
//        searchView.setQueryHint("Search People");
//        searchView.setOnQueryTextListener(this);
//        searchView.setIconified(true);
//        searchView.setMaxWidth(Integer.MAX_VALUE);
        return true;
    }


    //endregion

    //region setup Toolbar
    protected void setUpToolbar() {
        int navigationIcon = R.mipmap.store_close;
        String displayTitle = getResources().getString(R.string.free_downloads);
        mToolbar.setTitle(displayTitle);
        setSupportActionBar(mToolbar);
        mToolbar.setNavigationIcon(navigationIcon);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        setUpToolbarTheme();
    }
    //endregion

    // region Store item callbacks
    @Override
    public void onStoreItemSelected(FTStorePackItem ftStorePackItem) {
        if (!checkInternet())
            return;
        FTStoreDialog ftStoreDialog = FTStoreDialog.newInstance(ftStorePackItem, this, mDownloadStatusObserver);
        ftStoreDialog.show(getSupportFragmentManager(), "ftStoreDialog");
    }

    @Override
    public void onDownloadButtonClick(final FTStorePackItem ftStorePackItem) {
        if (!checkInternet())
            return;
        if (!FTApp.isForSamsungStore() && !FTApp.isForHuawei() && AppAuthentication.Companion.isLoginEnabled(this)) {
            if (!appAuthentication.isUserSignedIn()) {
                AlertDialog.Builder alBuilder = new AlertDialog.Builder(this);
                alBuilder.setTitle(getResources().getString(R.string.login_error_message));
                alBuilder.setPositiveButton(R.string.sign_in, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        onSignClick();
                        dialog.dismiss();
                    }
                });
                alBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                alBuilder.show();
                return;
            }
        }

        new FTDownloadDataService(FTStoreActivity.this, new FTDownloadDataService.DownloadDataCallback() {
            @Override
            public void onDownloadStart() {
                mDownloadStatusObserver.postNotification(ftStorePackItem.getName(), "start");
            }

            @Override
            public void onProgressUpdate(int progress) {
                mDownloadStatusObserver.postNotification(ftStorePackItem.getName(), progress);
            }

            @Override
            public void onDownloadFinish(boolean isSuccess) {
                mDownloadStatusObserver.postNotification(ftStorePackItem.getName(), isSuccess);
                if (isSuccess) {
                    FTDownloadData ftDownLoadData = new FTDownloadData();
                    ftDownLoadData.category = ftStorePackItem.getName();
                    ftDownLoadData.version = ftStorePackItem.getVersion();
                    ftDownLoadData.lastDownloaded = (new Date()).getTime();
                    FTDownloadedStorePackData.getInstance(FTStoreActivity.this).setStorePackData(ftDownLoadData);
                    ftStorePackItem.setDownloaded(true);
                    isDownloaded = true;
                } else {
                    AlertDialog.Builder alBuilder = new AlertDialog.Builder(FTStoreActivity.this);
                    alBuilder.setTitle(getResources().getString(R.string.download_error_message));
                    alBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    if (!isFinishing())
                        alBuilder.show();
                }
            }
        }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, ftStorePackItem.getDownloadUrl(), ftStorePackItem.getName());
    }

    @Override
    public void onProfileIconClick(int showLocation) {
        String email = "";
        email = appAuthentication.getUserEmail();
        FTUserDetailsDialog ftUserDetailsDialog = FTUserDetailsDialog.newInstance(email, showLocation, this);
        ftUserDetailsDialog.show(getSupportFragmentManager(), "ftUserDetailsDialog");
    }
    //endregion

    //region Firebase Sign in and authentication callbacks
    @Override
    public void onSignClick() {
        if (!checkInternet())
            return;
        startActivityForResult(
                appAuthentication.getSignInIntent(),
                RC_SIGN_IN);
    }

    @Override
    public void onSignOut() {
        appAuthentication.signOut();
    }
    //endregion

    //region SearchView callbacks
    @Override
    public boolean onQueryTextSubmit(String s) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        if (s.trim().length() == 0) {
            ftStore = new Gson().fromJson(storeData, FTStoreMetadata.class);
            final FTStorePack title = new FTStorePack();
            if (appAuthentication.isUserSignedIn()) {
                title.setProfilePic(appAuthentication.getUserPhotoUrl() != null ? appAuthentication.getUserPhotoUrl().toString() : "no-photo");
                title.setTitle(appAuthentication.getUserEmail() != null ? appAuthentication.getUserEmail() : "your Email");
                title.setValid(appAuthentication.isUserEmailVarified());
            }
            mFtStoreAdapter = new FTStoreAdapter(this, this, mDownloadStatusObserver);
            mFtStoreAdapter.add(title);
            mFtStoreAdapter.addAll(ftStore.getSections());
            listStore.setAdapter(mFtStoreAdapter);
            return true;
        }
        ArrayList<FTStorePack> filtered = new ArrayList<>();
        ftStore = new Gson().fromJson(storeData, FTStoreMetadata.class);
        for (int i = 0; i < ftStore.getSections().size(); i++) {
            FTStorePack storePack = ftStore.getSections().get(i);
            ArrayList<FTStorePackItem> data = new ArrayList<>();
            for (int j = 0; j < storePack.getPacks().size(); j++) {
                if (storePack.getPacks().get(j).getSearchkeys().contains(s)) {
                    data.add(storePack.getPacks().get(j));
                }
            }
            if (data.size() > 0) {
                storePack.setPacks(data);
                filtered.add(storePack);
            }
        }
        final FTStorePack title = new FTStorePack();
        if (appAuthentication.isUserSignedIn()) {
            title.setProfilePic(appAuthentication.getUserPhotoUrl() != null ? appAuthentication.getUserPhotoUrl().toString() : "no-photo");
            title.setTitle(appAuthentication.getUserEmail() != null ? appAuthentication.getUserEmail() : "your Email");
            title.setValid(appAuthentication.isUserEmailVarified());
        }
        mFtStoreAdapter = new FTStoreAdapter(this, this, mDownloadStatusObserver);
        mFtStoreAdapter.add(title);
        mFtStoreAdapter.addAll(filtered);
        listStore.setAdapter(mFtStoreAdapter);
        return false;
    }
    //endregion

    //region checkInternet
    private boolean checkInternet() {
        if (!FTNetworkConnectionUtil.INSTANCE.isNetworkAvailable(this)) {
            AlertDialog.Builder alBuilder = new AlertDialog.Builder(this);
            alBuilder.setTitle(getResources().getString(R.string.check_your_internet_connection));
            alBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            alBuilder.show();
            return false;
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isDownloaded)
            ObservingService.getInstance().postNotification("onCoverUpdate", null);
    }

    @Override
    public void onUserSignIn() {
        final FTStorePack title = new FTStorePack();
        if (appAuthentication.isUserSignedIn()) {
            title.setProfilePic(appAuthentication.getUserPhotoUrl() != null ? appAuthentication.getUserPhotoUrl().toString() : "no-photo");
            title.setTitle(appAuthentication.getUserEmail() != null ? appAuthentication.getUserEmail() : "your Email");
            title.setValid(appAuthentication.isUserEmailVarified());
        }
        mDownloadStatusObserver.postNotification("storeLogin", title);
    }

    @Override
    public void onUserSignOut() {
        mDownloadStatusObserver.postNotification("storeLogin", "");
    }

    //endregion
}
