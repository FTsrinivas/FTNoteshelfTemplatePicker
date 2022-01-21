package com.fluidtouch.noteshelf.shelf.activities;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.dd.plist.NSDictionary;
import com.dd.plist.NSNumber;
import com.dd.plist.PropertyListParser;
import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf.backup.database.FTBackupOperations;
import com.fluidtouch.noteshelf.commons.FTLog;
import com.fluidtouch.noteshelf.commons.settingsUI.dialogs.FTAutoBackupDialog;
import com.fluidtouch.noteshelf.commons.settingsUI.dialogs.FTChooseCloudDialog;
import com.fluidtouch.noteshelf.commons.settingsUI.dialogs.FTSettingsDialog;
import com.fluidtouch.noteshelf.commons.ui.FTBaseActivity;
import com.fluidtouch.noteshelf.commons.ui.FTDialogFactory;
import com.fluidtouch.noteshelf.commons.ui.FTExportFormatPopup;
import com.fluidtouch.noteshelf.commons.ui.FTSmartDialog;
import com.fluidtouch.noteshelf.commons.utils.AssetsUtil;
import com.fluidtouch.noteshelf.commons.utils.FTAnimationUtils;
import com.fluidtouch.noteshelf.commons.utils.FTFileManagerUtil;
import com.fluidtouch.noteshelf.commons.utils.FTPermissionManager;
import com.fluidtouch.noteshelf.commons.utils.FTPopupFactory;
import com.fluidtouch.noteshelf.commons.utils.FileUriUtils;
import com.fluidtouch.noteshelf.commons.utils.ObservingService;
import com.fluidtouch.noteshelf.commons.utils.SPenSupport;
import com.fluidtouch.noteshelf.commons.utils.ScreenUtil;
import com.fluidtouch.noteshelf.commons.utils.StringUtil;
import com.fluidtouch.noteshelf.document.FTDocumentActivity;
import com.fluidtouch.noteshelf.document.enums.FTCoverOverlayStyle;
import com.fluidtouch.noteshelf.documentframework.FTCoverPaperThemeProvider;
import com.fluidtouch.noteshelf.documentframework.FTDocument.FTDocumentFactory;
import com.fluidtouch.noteshelf.documentframework.FTNoteshelfDocument.FTDocumentPostProcessOperation;
import com.fluidtouch.noteshelf.documentframework.FTNoteshelfDocument.FTDocumentType;
import com.fluidtouch.noteshelf.documentframework.FTNoteshelfDocument.FTDocumentUtils;
import com.fluidtouch.noteshelf.documentframework.FTNoteshelfDocument.FTNoteshelfDocument;
import com.fluidtouch.noteshelf.documentframework.FTUrl;
import com.fluidtouch.noteshelf.documentframework.FileExporter.FTFileExporter;
import com.fluidtouch.noteshelf.documentframework.FileImporter.FTFileImporter;
import com.fluidtouch.noteshelf.documentframework.Utilities.FTConstants;
import com.fluidtouch.noteshelf.documentproviders.FTShelfCollectionProvider;
import com.fluidtouch.noteshelf.documentproviders.FTShelfItemCollection;
import com.fluidtouch.noteshelf.documentproviders.FTShelfItemCollectionLocal;
import com.fluidtouch.noteshelf.evernotesync.FTENPublishManager;
import com.fluidtouch.noteshelf.evernotesync.fragments.FTENPublishDialog;
import com.fluidtouch.noteshelf.globalsearch.FTGlobalSearchActivity;
import com.fluidtouch.noteshelf.models.disk.diskItem.shelfItem.FTDocumentItem;
import com.fluidtouch.noteshelf.models.disk.diskItem.shelfItem.FTGroupItem;
import com.fluidtouch.noteshelf.models.disk.diskItem.shelfItem.FTShelfItem;
import com.fluidtouch.noteshelf.models.theme.FTNCoverTheme;
import com.fluidtouch.noteshelf.models.theme.FTNPaperTheme;
import com.fluidtouch.noteshelf.models.theme.FTNTheme;
import com.fluidtouch.noteshelf.models.theme.FTNThemeCategory;
import com.fluidtouch.noteshelf.preferences.SystemPref;
import com.fluidtouch.noteshelf.scandocument.ScanActivity;
import com.fluidtouch.noteshelf.services.FTFirebaseAnalytics;
import com.fluidtouch.noteshelf.shelf.adapters.FTBaseShelfAdapter;
import com.fluidtouch.noteshelf.shelf.adapters.FTCategoryAdapter;
import com.fluidtouch.noteshelf.shelf.enums.FTShelfSortOrder;
import com.fluidtouch.noteshelf.shelf.fragments.FTBaseShelfFragment;
import com.fluidtouch.noteshelf.shelf.fragments.FTCategoriesFragment;
import com.fluidtouch.noteshelf.shelf.fragments.FTCategoryMoveToFragment;
import com.fluidtouch.noteshelf.shelf.fragments.FTCreateNotebookOptionsPopup;
import com.fluidtouch.noteshelf.shelf.fragments.FTRenameDialog;
import com.fluidtouch.noteshelf.shelf.fragments.FTShelfNotebookOptionsFragment;
import com.fluidtouch.noteshelf.shelf.fragments.ShelfEditModeToolbarOverlayFragment;
import com.fluidtouch.noteshelf.shelf.listeners.ShelfOnEditModeChangedListener;
import com.fluidtouch.noteshelf.shelf.listeners.ShelfOnGroupingActionsListener;
import com.fluidtouch.noteshelf.shelf.rating.FTAppRating;
import com.fluidtouch.noteshelf.store.ui.FTChooseCoverPaperDialog;
import com.fluidtouch.noteshelf.store.ui.FTNewNotebookDialog;
import com.fluidtouch.noteshelf.templatepicker.FTChoosePaperTemplate;
import com.fluidtouch.noteshelf.templatepicker.common.FTTemplatesInfoSingleton;
import com.fluidtouch.noteshelf.templatepicker.common.plistdatamodel.FTSelectedDeviceInfo;
import com.fluidtouch.noteshelf.templatepicker.common.util.FTTemplateUtil;
import com.fluidtouch.noteshelf.templatepicker.models.RecentsInfoModel;
import com.fluidtouch.noteshelf.whatsnew.FTWhatsNewDialog;
import com.fluidtouch.noteshelf2.BuildConfig;
import com.fluidtouch.noteshelf2.R;
import com.google.android.material.snackbar.Snackbar;
import com.noteshelf.cloud.FTCloudServices;
import com.noteshelf.cloud.backup.FTRestoreHandlerCallback;
import com.noteshelf.cloud.backup.FTServiceAccountHandler;
import com.noteshelf.cloud.backup.drive.FTGDriveRestoreHandler;
import com.noteshelf.cloud.backup.dropbox.FTDropboxRestoreHandler;
import com.samsung.android.sdk.penremote.SpenRemote;
import com.thin.downloadmanager.DownloadRequest;
import com.thin.downloadmanager.DownloadStatusListenerV1;
import com.thin.downloadmanager.ThinDownloadManager;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Observer;
import java.util.concurrent.Executors;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by sreenu.
 */

//TODO: SREENU

public class FTBaseShelfActivity extends FTBaseActivity implements
        FTNewNotebookDialog.NoteCreationListener,
        FTCategoryMoveToFragment.OnMovingItemsListener,
        ShelfOnGroupingActionsListener,
        FTBaseShelfAdapter.ShelfAdapterToActivityListener,
        ShelfEditModeToolbarOverlayFragment.OnShelfEditModeToolbarFragmentInteractionListener,
        FTShelfNotebookOptionsFragment.ShelfNotbookOptionsFragmentInteractionListener,
        FTChooseCoverPaperDialog.CoverChooseListener,
        FTChoosePaperTemplate.TemplatePaperChooseListener,
        FTChoosePaperTemplate.CallBackForNewNotebookDialogListener,
        FTSettingsDialog.SettingsListener,
        FTAutoBackupDialog.AutoBackupDialogListener,
        FTCategoryAdapter.NavigationItemListener,
        FTCreateNotebookOptionsPopup.CreateNotebookOptionsListener,
        ShelfOnEditModeChangedListener,
        FTCategoriesFragment.CategoriesFragmentCallbacks {
        public static final int SCAN_DOCUMENT = 104;
        private static final int IMPORT_DOCUMENT = 103;
        public static final int PICK_EXPORTER = 203;
        private static final int IMPORT_IMAGES = 204;

        //region Binding variables
//    @BindView(R.id.categories_layout)
//    protected LinearLayout mCategoriesLayout;
//    @BindView(R.id.categories_dim_view)
//    View mCategoriesDimView;
        //endregion
        @BindView(R.id.categories_panel_container)
        protected RelativeLayout mCategoriesPanelContainer;
        @BindView(R.id.toolbar)
        protected Toolbar mShelfToolbar;
        @BindView(R.id.trash_message_text_view)
        protected TextView trashMessageTextView;
        @BindView(R.id.shelf_left_panel)
        LinearLayout mShelfLeftPanel;
        @BindView(R.id.shelf_parent_layout)
        RelativeLayout mShelfParentLayout;
        @BindView(R.id.shelf_second_parent_layout)
        LinearLayout mShelfSecondParentLayout;
        @BindView(R.id.shelf_view)
        View mShelfView;
        @BindView(R.id.move_to_fragment_container_view)
        View mMoveToFragmentContainerView;
        @BindView(R.id.move_to_fragment_container_parent_layout)
        RelativeLayout mMoveToFragmentContainerParentLayout;
        @BindView(R.id.move_to_fragment_container)
        RelativeLayout mMoveToFragmentContainer;
        @BindView(R.id.categories_divider)
        View mCategoriesDivider;

        //region Class variables
        private boolean isLeftPanelOpened = true;
        private int prevOrientation;
        protected FTShelfItemCollection mCurrentShelfItemCollection;
        protected ShelfEditModeToolbarOverlayFragment mShelfEditModeFragment;
        protected FTShelfNotebookOptionsFragment mBottomOptionsFragment;

        private FTGroupItem mSelectedGroup = null;
        private Fragment mDrawerFragment;
        private Observer evernoteObserver;
        private MenuItem selectOption;

        //endregion
        private String mSearchingText = "";
        private int ndWidth = 0;
        private boolean restoreInProgress;

        private FTAppRating.Builder builder;
        private BroadcastReceiver authorityReceiver;

        private boolean consumedIntent;
        private ArrayList<FTNTheme> ftnThemeArrayList = new ArrayList<>();
        private ArrayList<FTNTheme> ftnThemeArrayListQucikcreate = new ArrayList<>();
        ArrayList<String> packNameList = new ArrayList<>();

        private ThinDownloadManager downloadManager = new ThinDownloadManager();
        Runnable mHandlerTask;
        Handler mHandler = new Handler();
        private final static int INTERVAL = 1000 * 60 * 2; //2 minutes

        ArrayList<FTNTheme> ftnThemeArrayListPrefs = new ArrayList<>();
        ArrayList<FTNTheme> ftnCoversArrayListPrefs = new ArrayList<>();
        ArrayList<String> ftnThemeThumbnailURLPathList = new ArrayList<>();
        ArrayList<String> ftnCoversThumbnailURLPathList = new ArrayList<>();

        private FTBaseShelfFragment mShelfFragment;
        private FTCategoriesFragment mCategoriesFragment;
        private boolean isOpenDocument = false;
        private boolean prevIsMobileLayout;

        //When mode switches between Edit and Normal (Shelf notebooks editing)
        @Override
        public void onEditModeChanged(boolean isInEditMode, int count) {
            //mCategoriesDimView.setVisibility(isInEditMode ? View.VISIBLE : View.GONE);
            if (isInEditMode) {
                mShelfEditModeFragment = ShelfEditModeToolbarOverlayFragment.newInstance(count);
                mBottomOptionsFragment = FTShelfNotebookOptionsFragment.newInstance(getResources().getBoolean(R.bool.isTablet), count, isInsideGroup());
                getSupportFragmentManager().beginTransaction().add(R.id.shelf_toolbar_fragment_layout, mShelfEditModeFragment).commitAllowingStateLoss();
                getSupportFragmentManager().beginTransaction().add(R.id.shelf_bottom_fragment_layout, mBottomOptionsFragment).commitAllowingStateLoss();
            } else {
                getSupportFragmentManager().beginTransaction().remove(mShelfEditModeFragment).remove(mBottomOptionsFragment).commitNowAllowingStateLoss();
            }
        }

        @Override
        public void onSelectedItemsCountChanged(boolean isInEditMode, int count) {
            if (isInEditMode) {
                mShelfEditModeFragment.updateLayout(count);
                mBottomOptionsFragment.updateLayout(count);
            }
        }

        //Dummy view that covers the entire screen when quick access panel is opened in portrait mode
        private View.OnClickListener mShelfViewOnClickListener = view -> {
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                if (isLeftPanelOpened) {
                    closeLeftPanel();
                }
            }
        };

        private FTRestoreHandlerCallback mRestoreHandlerCallback = new FTRestoreHandlerCallback() {
            @Override
            public void onBookRestored(FTDocumentItem documentItem, Error error) {
                runOnUiThread(() -> {
                    FTApp.getPref().saveDefaultNotebookCreated(true);
                    if (error == null) {
                        if (mCurrentShelfItemCollection == null) {
                            FTBaseShelfActivity.this.mCurrentShelfItemCollection = documentItem.getShelfCollection();
                            FTApp.getPref().saveRecentCollectionName(documentItem.getShelfCollection().getDisplayTitle(getContext()));
                            initializeViewRelated();
                        } else {
                            if (mCurrentShelfItemCollection == documentItem.getShelfCollection()) {
                                mShelfFragment.addNewItem(documentItem);
                            }
                        }
                    } else {
                        Toast.makeText(getContext(), getString(R.string.error) + " " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onRestoreCompleted(Error error) {
                if (error != null) {
                    Toast.makeText(getContext(), getString(R.string.error) + " " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
                if (!FTApp.getPref().isDefaultNotebookCreated()) {
                    getShelfs();
                }
            }
        };

        //region Activity callback methods


        @Override
        protected void onSaveInstanceState(@NonNull Bundle outState) {
            super.onSaveInstanceState(outState);
            outState.putBoolean("consumedIntent", consumedIntent);
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            getWindow().setStatusBarColor(FTConstants.statusBarColor);
            super.onCreate(savedInstanceState);
            FTApp.getInstance().setCurActCtx(this);
            setContentView(R.layout.activity_shelf);
            ButterKnife.bind(this);

            prevOrientation = getResources().getConfiguration().orientation;

            //Initializing group
            if (getIntent().hasExtra(getString(R.string.intent_notes_group_uri))) {
                mSelectedGroup = new FTGroupItem(FTUrl.parse(getIntent().getStringExtra(getString(R.string.intent_notes_group_uri))));
            }

            updateCurrentProvider();

            //Getting the shelf items to display
            if (mCurrentShelfItemCollection == null) {
                initializeViewRelated();
            }

            if (!FTApp.getPref().getGroupDocumentUrl().isEmpty()) {
                openGroup(new FTGroupItem(FTUrl.parse(FTApp.getPref().getGroupDocumentUrl())));
            }

            //True, if the activity is invoked by the system for importing a file.
            if (savedInstanceState != null) {
                consumedIntent = savedInstanceState.getBoolean("consumedIntent");
            }
            if (consumedIntent) {
                checkPreviousState();
            } else {
                if (getIntent().getData() != null) {
                    consumedIntent = true;
                    onPickingDocumentFromDevice(getIntent());
                } else if (getIntent().getClipData() != null) {
                    consumedIntent = true;
                    onPickingDocumentFromDevice(getIntent());
                } else {
                    checkPreviousState();
                }
            }

            evernoteObserver = (o, arg) -> runOnUiThread(this::setUpEvernoteErrorIcon);
            FTENPublishManager.getInstance().observingService.addObserver("evernote_error", evernoteObserver);
            ObservingService.getInstance().addObserver("backup_error", backupErrorObserver);

            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                if (isLeftPanelOpened) {
                    initialLeftPanelClosing();
                }
            } else {
                mShelfView.setVisibility(View.GONE);
            }

            mShelfView.setOnClickListener(mShelfViewOnClickListener);
            mMoveToFragmentContainerView.setOnClickListener(view -> onSelectedItemsMoved(new ArrayList<>()));
            ndWidth = getResources().getDimensionPixelSize(R.dimen.shelf_nd_width);

            builder = new FTAppRating.Builder()
                    .setMinimumLaunchTimes(getResources().getInteger(R.integer.minimumLaunchTimes))
                    .setMinimumDays(getResources().getInteger(R.integer.minimumDays))
                    .setMinimumLaunchTimesToShowAgain(getResources().getInteger(R.integer.minimumLaunchTimesToShowAgain))
                    .setMinimumDaysToShowAgain(getResources().getInteger(R.integer.minimumDaysToShowAgain))
                    .showIfMeetConditions();

            IntentFilter filter = new IntentFilter();
            filter.addAction("com.sec.android.app.samsungapps.RESPONSE_INAPP_REVIEW_AUTHORITY");
            authorityReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    boolean hasAuthority = intent.getBooleanExtra("hasAuthority", false);
                    String deeplinkUri = intent.getStringExtra("deeplinkUri");

                    if (hasAuthority) {
                        Intent intent1 = new Intent();
                        intent1.setData(Uri.parse(deeplinkUri));
                        intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                        startActivity(intent1);
                    }
                }
            };
//        requestForReview(this);
            boolean showSpenDialog = !FTApp.getPref().get(SystemPref.FIRST_TIME_INSTALLED_VERSION, "").equals(BuildConfig.VERSION_NAME) && FTApp.getPref().isDefaultNotebookCreated();
            if (SPenSupport.isSPenSupported(getContext())
                    && SpenRemote.getInstance().isFeatureEnabled(SpenRemote.FEATURE_TYPE_AIR_MOTION)
                    && !FTApp.getPref().isSpenAirActionFeatureShown() && showSpenDialog) {
                new FTWhatsNewDialog().show(getSupportFragmentManager(), FTWhatsNewDialog.class.getName());
                FTApp.getPref().spenAirActionFeatureShown(true);
                FTApp.getPref().get(SystemPref.FIRST_TIME_INSTALLED_VERSION, BuildConfig.VERSION_NAME);
            }

            enableBackupPublishing();

            downloadLatestPlist();
        }

        private void downloadLatestPlist() {
            if (FTApp.getInstance().isNetworkAvailable()) {
                boolean jobSchedulerStatus = FTApp.getPref().get(SystemPref.JOB_SCHEDULER_STARTED, false);
                if (!jobSchedulerStatus) {
                    //startRepeatingTask();
                }
            }
        }

        private void doBackgroundWork() {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    deleteExistingFile();
                    DownloadRequest downloadRequest = new DownloadRequest(Uri.parse(FTConstants.PLIST_ENDPOINT))
                            .setDestinationURI(Uri.parse(pListfromServer()))
                            .setStatusListener(new DownloadStatusListenerV1() {

                                @Override
                                public void onDownloadComplete(com.thin.downloadmanager.DownloadRequest downloadRequest) {
                                    FTApp.getPref().save(SystemPref.JOB_SCHEDULER_STARTED, true);
                                    File sourceFile = new File(pListfromServer());
                                    File destinationFile = new File(FTConstants.DOCUMENTS_ROOT_PATH + "/Library/" + FTTemplatesInfoSingleton.getThemesPlist());
                                    if (destinationFile.exists()) {
                                        FTFileManagerUtil.moveFile(sourceFile,destinationFile);
                                    }
                                }

                                @Override
                                public void onDownloadFailed(com.thin.downloadmanager.DownloadRequest downloadRequest, int errorCode, String errorMessage) {
                                    FTApp.getPref().save(SystemPref.JOB_SCHEDULER_STARTED, false);
                                }

                                @Override
                                public void onProgress(com.thin.downloadmanager.DownloadRequest downloadRequest, long totalBytes, long downloadedBytes, int progress) {
                                }

                            });
                    downloadManager.add(downloadRequest);
                }
            }).start();
        }

        public static String pListfromServer() {
            String lang = Locale.getDefault().getLanguage();
            if (Locale.getDefault().toLanguageTag().contains("zh-Hans")) {
                lang = "zh-Hans";
            } else if (Locale.getDefault().toLanguageTag().contains("zh-Hant")) {
                lang = "zh-Hant";
            }
            String plistName = "themes_v8_" + lang + ".plist";
            return FTConstants.TEMP_FOLDER_PATH +"/"+plistName;
        }

        protected void deleteExistingFile() {
            boolean deleted = false;
            File file = new File(pListfromServer());
            if (file.exists()) {
                deleted = file.delete();
            }
        }

        public int getPlistVersion() {
            int plistVersion = 0;
            File plist = new File(FTConstants.TEMP_FOLDER_PATH +"/"+"themes_v8_en"+FTConstants.PLIST_EXTENSION);
            try {
                FileInputStream inputStream = new FileInputStream(plist);
                NSDictionary dictionary = (NSDictionary) PropertyListParser.parse(inputStream);
                NSNumber plistVersionNSNumber = (NSNumber) dictionary.objectForKey("version");
                return plistVersionNSNumber.intValue();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return plistVersion;

        }

        private void initShelfFragment() {
            mShelfFragment = new FTShelfGroupableFragment();
            getSupportFragmentManager().beginTransaction().replace(R.id.shelf_list_holder, mShelfFragment, FTBaseShelfFragment.class.getName()).commitAllowingStateLoss();
        }

        private void initialLeftPanelClosing() {
            mShelfSecondParentLayout.removeView(mShelfLeftPanel);
            mShelfParentLayout.addView(mShelfLeftPanel);
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mShelfLeftPanel.getLayoutParams();
            float scale = getResources().getDisplayMetrics().density;
            lp.setMargins((int) -(getResources().getDimensionPixelOffset(R.dimen.shelf_nd_width) + (2 * scale)), 0, 0, 0);
            mShelfLeftPanel.setLayoutParams(lp);
            isLeftPanelOpened = !isLeftPanelOpened;
            mShelfView.setVisibility(View.GONE);
        }

        @Override
        public boolean onCreateOptionsMenu(Menu menu) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.toolbar_menu, menu);
            setUpBackUpErrorIcon();
            setUpEvernoteErrorIcon();
            if (menu != null) {
                updateMenuOptions(menu);
                selectOption = menu.findItem(R.id.menu_select);
                updateSelectOption();
            }
            return true;
        }

        private void updateMenuOptions(Menu menu) {
            boolean isVisible = mCurrentShelfItemCollection != null && mCurrentShelfItemCollection.isTrash(getContext());
            //Since we get the crash reports with menu is being null or menu item is null, added try catch block (Not sure of exact reason)
            try {
                if (menu != null) {
                    menu.findItem(R.id.menu_sort).setVisible(!isVisible);
                    menu.findItem(R.id.menu_search).setVisible(!isVisible);
                    menu.findItem(R.id.menu_select).setVisible(true);
                    menu.findItem(R.id.menu_create_notebook).setVisible(!isVisible);
                    menu.findItem(R.id.menu_empty_trash).setVisible(isVisible);
                }
            } catch (Exception e) {
                FTLog.crashlyticsLog("Menu error");
            }
        }

        private void updateSelectOption() {
            if (selectOption != null) {
                if (getResources().getBoolean(R.bool.isTablet)) {
                    selectOption.setIcon(null);
                } else {
                    selectOption.setIcon(R.drawable.select);
                }
            }
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.menu_view_list:
                    Toast.makeText(getContext(), R.string.view_list, Toast.LENGTH_SHORT).show();
                    break;
                case R.id.menu_search:
                    FTFirebaseAnalytics.logEvent("Shelf_Search");
                    startActivity(new Intent(getContext(), FTGlobalSearchActivity.class));
                    break;
                case R.id.menu_empty_trash:
                    emptyTrash();
                    break;
                case R.id.menu_sort:
                    showSortingOptionsDialog(mShelfToolbar);
                    break;
                case R.id.menu_select:
                    onEditModeChanged(true, 0);
                    if (null != mShelfFragment) {
                        mShelfFragment.setEditMode();
                    }
                    break;
                case R.id.menu_back_up_error:
                    SystemPref.BackUpType backUpType = SystemPref.BackUpType.values()[FTApp.getPref().getBackUpType()];
                    if (backUpType != SystemPref.BackUpType.NONE)
                        FTAutoBackupDialog.newInstance(backUpType).show(getSupportFragmentManager());
                    break;
                case R.id.menu_evernote_error:
                    super.authenticateEvernoteUser(successful -> FTENPublishDialog.newInstance().show(getSupportFragmentManager()));
                    break;
                case R.id.menu_create_notebook:
                    FTFirebaseAnalytics.logEvent("Shelf_AddNew");
                    new FTCreateNotebookOptionsPopup().show(getSupportFragmentManager());
                    //new FTChoosePaperTemplateFragment().show(getSupportFragmentManager());
                    break;
                default:
                    break;
            }
            return true;
        }

        @Override
        public void onConfigurationChanged(@NotNull Configuration newConfig) {
            super.onConfigurationChanged(newConfig);
            updateSelectOption();
            if (prevOrientation != newConfig.orientation) {
                if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
                    mShelfSecondParentLayout.removeView(mShelfLeftPanel);
                    mShelfParentLayout.addView(mShelfLeftPanel);
                    closeLeftPanel();
                } else {
                    mShelfParentLayout.removeView(mShelfLeftPanel);
                    mShelfSecondParentLayout.addView(mShelfLeftPanel, 0);
                    mShelfView.setVisibility(View.GONE);
                    openLeftPanel();
                }
            }
            if (prevIsMobileLayout != ScreenUtil.isMobile(getContext())) {
                closeLeftPanel();
            }
            prevIsMobileLayout = ScreenUtil.isMobile(getContext());
            prevOrientation = newConfig.orientation;
        }

        @Override
        protected void onResume() {
            super.onResume();
            FTApp.getInstance().setCurActCtx(this);

            if (authorityReceiver != null) {
                registerReceiver(authorityReceiver, new IntentFilter("com.sec.android.app.samsungapps.RESPONSE_INAPP_REVIEW_AUTHORITY"));
            }
        }

        @Override
        protected void onNewIntent(Intent intent) {
            super.onNewIntent(intent);
            if (intent != null) {
                onPickingDocumentFromDevice(intent);
            }
        }

        @Override
        protected void onRestart() {
            super.onRestart();
            isOpenDocument = false;
            mShelfFragment.onRestart();
            setUpBackUpErrorIcon();
            setUpEvernoteErrorIcon();

            enableBackupPublishing();
        }

        @Override
        protected void onDestroy() {
            if (mShelfFragment != null) {
                getSupportFragmentManager().beginTransaction().remove(mShelfFragment).commitAllowingStateLoss();
            }

            if (FTENPublishManager.getInstance().isEngineUnderExecution) {
                FTENPublishManager.getInstance().observingService.removeObserver("evernote_error", evernoteObserver);
            }
            ObservingService.getInstance().removeObserver("backup_error", backupErrorObserver);

            if (authorityReceiver != null) {
                unregisterReceiver(authorityReceiver);
            }
            super.onDestroy();
        }

        @Override
        public void onBackPressed() {
            if (mShelfFragment.isInEditMode()) {
                disableEditMode();
            } else {
                if (isInsideGroup()) {
                    closeGroup();
                } else {
                    super.onBackPressed();
                }
            }
        }
        //endregion

        //region Initialization

        @Override
        protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            if (requestCode == PICK_EXPORTER) {
                if (resultCode == Activity.RESULT_OK)
                    doneEditing();
            } else if (resultCode == FTDocumentActivity.DOCUMENT_ACTIVITY || requestCode == FTDocumentActivity.DOCUMENT_ACTIVITY) {
                FTShelfItemCollection shelf = mCurrentShelfItemCollection;
                if (data != null && data.hasExtra("updatedCollectionUrl")) {
                    shelf = new FTShelfItemCollectionLocal(new FTUrl(data.getStringExtra("updatedCollectionUrl")));
                    if (!shelf.getDisplayTitle(getContext()).equals(mCurrentShelfItemCollection.getDisplayTitle(getContext()))) {
                        shelf.getChildren().clear();
                        onShelfItemCollectionSelected(shelf);
                    }
                }
                refreshAdapter();
            } else if (requestCode == IMPORT_DOCUMENT && data != null) {
                onPickingDocumentFromDevice(data);
            } else if (requestCode == IMPORT_IMAGES && data != null) {
                onPickingDocumentFromDevice(data, true);
            } else if (resultCode == SCAN_DOCUMENT && data != null) {
                onPickingDocumentFromDevice(new Intent().setData(FileUriUtils.getUri(new File(data.getStringExtra(getString(R.string.intent_scanned_doc_path))))));
            }
        }

        @OnClick(R.id.shelf_view)
        void onParentLayoutClicked() {
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT && isLeftPanelOpened)
                closeLeftPanel();
        }

        //region View related
        protected void initializeViewRelated() {
            setUpToolbar(isInsideGroup());
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                openLeftPanel();
            }
            initShelfFragment();
        }
        //endregion
        //endregion

        //region Toolbar initial setup
        protected void setUpToolbar(boolean isInsideGroup) {
            int navigationIcon = R.drawable.drawer;
            String displayTitle = "";
            if (mCurrentShelfItemCollection != null)
                displayTitle = isInsideGroup ? mSelectedGroup.getDisplayTitle(getContext()) : mCurrentShelfItemCollection.getDisplayTitle(getContext());
            String subTitle = isInsideGroup ? mSelectedGroup.getShelfCollection().getDisplayTitle(getContext()) : "";

            mShelfToolbar.setTitle(getToolbarTitle(displayTitle));
            mShelfToolbar.setSubtitle(subTitle);
            mShelfToolbar.setSubtitleTextColor(ContextCompat.getColor(Objects.requireNonNull(getContext()), android.R.color.white));

            setSupportActionBar(mShelfToolbar);
            mShelfToolbar.setNavigationIcon(navigationIcon);
            mShelfToolbar.setOverflowIcon(getDrawable(R.drawable.toolbar_overflow));

            mShelfToolbar.setNavigationOnClickListener(view -> {
            /*if (mCategoriesLayout.getVisibility() == View.GONE) {
                mCategoriesLayout.setVisibility(View.VISIBLE);
            }*/
                if (isLeftPanelOpened) {
                    closeLeftPanel();
                } else {
                    openLeftPanel();
                }
            });

            setUpToolbarTheme();
        }

        @Override
        public void setUpToolbarTheme() {
            super.setUpToolbarTheme();
            if (mShelfEditModeFragment != null) {
                mShelfEditModeFragment.updateToolbarTheme();
            }
            if (mCategoriesFragment != null) mCategoriesFragment.setUpToolbar();
        }

        public void updateCurrentProvider() {
            Intent intent = getIntent();
            boolean restore = false;
            if (intent != null) {
                restore = intent.getBooleanExtra("restoreBackup", false);
            }

            int backUpType = FTApp.getPref().getBackUpType();
            if (restore) {
                FTApp.getPref().saveBackUpType(SystemPref.BackUpType.NONE);
                restoreInProgress = true;
                if (backUpType == SystemPref.BackUpType.GOOGLE_DRIVE.ordinal()
                        && !FTApp.getPref().isDefaultNotebookCreated()
                        && FTCloudServices.INSTANCE.isGooglePlayServicesAvailable(this) && !FTApp.getPref().get(SystemPref.COUNTRY_CODE, "").equalsIgnoreCase("cn")) {
                    FTGDriveRestoreHandler restoreHandler = new FTGDriveRestoreHandler(FTBaseShelfActivity.this);
                    restoreHandler.setListener(mRestoreHandlerCallback);
                    restoreHandler.startRestoring();
                } else if (backUpType == SystemPref.BackUpType.DROPBOX.ordinal() && !FTApp.getPref().isDefaultNotebookCreated()) {
                    FTDropboxRestoreHandler restoreHandler = new FTDropboxRestoreHandler(FTBaseShelfActivity.this);
                    restoreHandler.setListener(mRestoreHandlerCallback);
                    restoreHandler.startRestoring();
                } else {
                    getShelfs();
                }
            } else {
                getShelfs();
            }
        }

        public void getShelfs() {
            FTLog.crashlyticsLog("getShelfs on 1st time of app install");
            FTShelfCollectionProvider.getInstance().shelfs(shelfs -> {
                FTLog.crashlyticsLog("getShelfs size: " + shelfs.size());
                if (shelfs.isEmpty()) {
                    return;
                }
                //For assigning default one
                FTBaseShelfActivity.this.mCurrentShelfItemCollection = shelfs.get(0);
                String recentCategoryName = FTApp.getPref().getRecentCollectionName();
                for (FTShelfItemCollection shelfItemCollection : shelfs) {
                    if (shelfItemCollection.getDisplayTitle(getContext()).equals(recentCategoryName)) {
                        FTBaseShelfActivity.this.mCurrentShelfItemCollection = shelfItemCollection;
                        break;
                    }
                }

                FTApp.getPref().saveDefaultNotebookCreated(true);

                initializeViewRelated();
            });
        }

        @Override
        public FTShelfItemCollection getCurrentShelfItemCollection() {
            return mCurrentShelfItemCollection;
        }

        @Override
        public FTGroupItem getCurrentGroupItem() {
            return mSelectedGroup;
        }
        //endregion

        protected void checkPreviousState() {
            if (!isInsideGroup()) {
                if (!FTApp.getPref().getGroupDocumentUrl().isEmpty()) {
                    openGroup(new FTGroupItem(FTUrl.parse(FTApp.getPref().getGroupDocumentUrl())));
                    checkPreviousStateForOpeningDocument();
                } else {
                    checkPreviousStateForOpeningDocument();
                }
            } else {
                checkPreviousStateForOpeningDocument();
            }
        }

        private void checkPreviousStateForOpeningDocument() {
            if (!FTApp.getPref().getDocumentUrl().isEmpty()) {
                File file = new File(FTUrl.noteshelfRootDirectory() + "/" + FTApp.getPref().getDocumentUrl());
                if (file.exists()) {
                    openSelectedDocument(FTUrl.parse(file.getAbsolutePath()), 0);
                } else {
                    FTApp.getPref().saveDocumentUrl("");
                    FTApp.getPref().saveGroupDocumentUrl("");
                }
            }
        }

        @Override
        public void createNewNotebook() {
            FTFirebaseAnalytics.logEvent("Shelf_AddNew_NewNotebook");
            FTNewNotebookDialog.newInstance(null).show(getSupportFragmentManager(), FTNewNotebookDialog.class.getName());
        }

        @Override
        public void importDocument() {
            FTFirebaseAnalytics.logEvent("shelf", "shelf_quick_access_popup", "import_notebook");
            FTBaseShelfActivity.this.importDocument(IMPORT_DOCUMENT);
        }

        @Override
        public void createNotebookWithDefaultOptions() {
            FTFirebaseAnalytics.logEvent("shelf", "shelf_quick_access_popup", "quick_create");
            FTNCoverTheme coverTheme = null;
            FTNTheme paperTheme = null;

            if (FTApp.getPref().get(SystemPref.RANDOM_COVER_DESIGN_ENABLED, false)) {
                coverTheme = (FTNCoverTheme) FTCoverPaperThemeProvider.getInstance().getRandomTheme(getContext(), FTNThemeCategory.FTThemeType.COVER);
            } else {
                String coverPackName = FTApp.getPref().get(SystemPref.RECENT_COVER_THEME_NAME, FTConstants.DEFAULT_COVER_THEME_NAME);
                if (FTNTheme.theme(FTNThemeCategory.getUrl(coverPackName)) instanceof FTNCoverTheme && coverPackName.endsWith(".nsc")) {
                    coverTheme = (FTNCoverTheme) FTNTheme.theme(FTNThemeCategory.getUrl(coverPackName));
                    coverTheme.bitmap               = FTTemplateUtil.getBitmapFromAsset(FTNThemeCategory.FTThemeType.COVER);
                }

                if (coverTheme == null || coverTheme.themeThumbnail(getContext()) == null) {
                    coverTheme = new FTNThemeCategory(getContext(), "Simple", FTNThemeCategory.FTThemeType.COVER).getCoverThemeForPackName(FTConstants.DEFAULT_COVER_THEME_NAME);
                    FTApp.getPref().save(SystemPref.RECENT_COVER_THEME_NAME, FTConstants.DEFAULT_COVER_THEME_NAME);
                    coverTheme.bitmap               = FTTemplateUtil.getBitmapFromAsset(FTNThemeCategory.FTThemeType.COVER);
                }
            }

            String paperPackName = FTApp.getPref().get(SystemPref.QUICK_CREATE_PAPER_THEME_NAME, FTConstants.DEFAULT_PAPER_THEME_NAME);
            RecentsInfoModel recentsInfoModel  = FTTemplateUtil.getInstance().getRecentPaperThemeFromQuickCreateDialog();

            if (recentsInfoModel != null) {
                paperPackName = recentsInfoModel.get_packName();
            }
            Log.d("TemplatePicker==>", "FTBaseShelfActivity createNotebookWithDefaultOptions paperPackName " +paperPackName);
            if (paperPackName.endsWith(".nsp")) {
                paperTheme = FTNTheme.theme(FTNThemeCategory.getUrl(paperPackName));
                paperTheme.thumbnailURLPath     = FTConstants.TEMP_FOLDER_PATH+"TemplatesCache/"+FTConstants.DEFAULT_PAPER_THEME_URL;
                paperTheme.bitmap               = FTTemplateUtil.getBitmapFromAsset(FTNThemeCategory.FTThemeType.PAPER);
            }

            if (recentsInfoModel != null) {
                paperTheme.categoryName         = recentsInfoModel.get_categoryName();
                paperTheme.packName             = recentsInfoModel.get_packName();
                paperTheme.themeBgClr           = recentsInfoModel.getThemeBgClr();
                paperTheme.themeBgClrName       = recentsInfoModel.getThemeBgClrName();
                paperTheme.horizontalLineColor  = recentsInfoModel.getHorizontalLineColor();
                paperTheme.verticalLineColor    = recentsInfoModel.getVerticalLineColor();
                paperTheme.horizontalSpacing    = recentsInfoModel.getHorizontalSpacing();
                paperTheme.verticalSpacing      = recentsInfoModel.getVerticalSpacing();
                paperTheme.width                = recentsInfoModel.getWidth();
                paperTheme.height               = recentsInfoModel.getHeight();
                paperTheme.themeName            = recentsInfoModel.get_themeName();
                paperTheme.isLandscape          = recentsInfoModel.isLandscape();
                paperTheme.thumbnailURLPath     = recentsInfoModel.get_thumbnailURLPath();
                paperTheme.bitmap               = FTTemplateUtil.getInstance().StringToBitMap(recentsInfoModel.get_themeBitmapInStringFrmt());
            }

            boolean _isThemeDeleted              = FTTemplateUtil.getInstance().isThemeDeleted(FTNThemeCategory.FTThemeType.PAPER,paperTheme);

            if (_isThemeDeleted) {
                paperTheme = FTNTheme.theme(FTNThemeCategory.getUrl(paperPackName));

                paperTheme.thumbnailURLPath     = FTConstants.TEMP_FOLDER_PATH+"TemplatesCache/"+FTConstants.DEFAULT_PAPER_THEME_URL;
                paperTheme.bitmap               = FTTemplateUtil.getBitmapFromAsset(FTNThemeCategory.FTThemeType.PAPER);
                FTTemplateUtil.getInstance().saveRecentPaperThemeFromQuickCreateDialog(paperTheme);
            }

            Log.d("TemplatePicker==>", "FTBaseShelfActivity createNotebookWithDefaultOptions _isThemeDeleted " +_isThemeDeleted+
                    " get_themeName::-"+paperTheme.themeName +" recentsInfoModel:: "+recentsInfoModel);

            createNewShelfItem(getString(R.string.untitled), coverTheme, paperTheme,"createNotebookWithDefaultOptions");
        }

        @Override
        public void createNewNotebookFromImages() {
            FTFirebaseAnalytics.logEvent("shelf", "shelf_quick_access_popup", "multiple_images");
            FTBaseShelfActivity.this.importImages(IMPORT_IMAGES);
        }

        @Override
        public void scanDocument() {
            FTFirebaseAnalytics.logEvent("shelf", "shelf_quick_access_popup", "scan_document");
            startActivityForResult(new Intent(FTBaseShelfActivity.this, ScanActivity.class), SCAN_DOCUMENT);
        }

        protected void emptyTrash() {
            List<FTShelfItem> notebooks = mCurrentShelfItemCollection.getChildren();
            if (!notebooks.isEmpty()) {
                FTDialogFactory.showAlertDialog(getString(R.string.empty_trash),
                        getString(R.string.are_you_sure_you_want_to_delete_notebooks_permanently), new FTDialogFactory.OnAlertDialogShownListener() {
                            @Override
                            public void onPositiveClick(DialogInterface dialog, int which) {
                                final FTSmartDialog smartDialog = new FTSmartDialog()
                                        .setMode(FTSmartDialog.FTSmartDialogMode.SPINNER)
                                        .setMessage(getString(R.string.deleting))
                                        .show(getSupportFragmentManager());
                                for (int i = 0; i < notebooks.size(); i++) {
                                    FTShelfCollectionProvider.getInstance().pinnedShelfProvider.removePinned(notebooks.get(i).getFileURL().getPath());
                                    FTShelfCollectionProvider.getInstance().recentShelfProvider.removeRecent(notebooks.get(i).getFileURL().getPath());
                                }
                                mCurrentShelfItemCollection.removeShelfItems(getContext(), (removedNotebooks, error) -> {
                                    if (error == null) {
                                        refreshAdapter();
                                        if (mShelfFragment.isInEditMode()) {
                                            mShelfEditModeFragment.updateLayout(mCurrentShelfItemCollection.getChildren().size());
                                        }
                                    }
                                    smartDialog.dismissAllowingStateLoss();
                                }, notebooks);
                            }

                            @Override
                            public void onNegativeClick(DialogInterface dialog, int which) {
                            }
                        });
            }
        }

        //endregion

        private void doneEditing() {
            if (mShelfFragment != null && mShelfFragment.isInEditMode()) {
                mShelfFragment.doneEditMode();
                onEditModeChanged(false, 0);
            }
        }

        //region Helper methods
        private void setUpBackUpErrorIcon() {
            FTBackupOperations operations = FTBackupOperations.getInstance();
            if (operations != null) {
                boolean isVisible = !operations.getErrorList().isEmpty();
                if (mShelfToolbar.getMenu() != null && mShelfToolbar.getMenu().findItem(R.id.menu_back_up_error) != null) {
                    mShelfToolbar.getMenu().findItem(R.id.menu_back_up_error).setVisible(isVisible);
                }
            }
        }

        private void setUpEvernoteErrorIcon() {
            boolean isVisible = !FTENPublishManager.getInstance().getErrorList().isEmpty();
            if (mShelfToolbar.getMenu() != null && mShelfToolbar.getMenu().findItem(R.id.menu_evernote_error) != null) {
                mShelfToolbar.getMenu().findItem(R.id.menu_evernote_error).setVisible(isVisible);
            }
        }

        private void showSortingOptionsDialog(View view) {
            int[] sortLocation = new int[2];
            findViewById(R.id.menu_search).getLocationOnScreen(sortLocation);
            final PopupWindow popupWindow = FTPopupFactory.create(this, view, R.layout.sorting_options_dialog, R.dimen.margin_one_sixty, R.dimen.margin_ninety_four, sortLocation[0]);

            final TextView dateTextView = popupWindow.getContentView().findViewById(R.id.sorting_options_by_date_text_view);
            final TextView nameTextView = popupWindow.getContentView().findViewById(R.id.sorting_options_by_name_text_view);

            if (FTApp.getPref().isSortingWithDate()) {
                dateTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.check_mark_black, 0);
            } else {
                nameTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.check_mark_black, 0);
            }

            dateTextView.setOnClickListener(v -> {
                FTFirebaseAnalytics.logEvent("Shelf_TapDate");
                FTApp.getPref().saveSortingWithDate(true);
                refreshAdapter();
                popupWindow.dismiss();
            });

            nameTextView.setOnClickListener(v -> {
                FTFirebaseAnalytics.logEvent("Shelf_TapName");
                FTApp.getPref().saveSortingWithDate(false);
                refreshAdapter();
                popupWindow.dismiss();
            });
        }

        private int getSpanCount() {
            int orientation = getResources().getConfiguration().orientation;
            if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                return 4;
            } else {
                return 5;
            }
        }

        //region Toolbar related
        public void updateToolbarTitle(String title) {
            mShelfToolbar.setTitle(getToolbarTitle(title));
            mShelfToolbar.setSubtitle("");
        }

        private String getToolbarTitle(String title) {
            if (title.equals(getString(R.string.trash))) {
                title = getString(R.string.trash_title);
            }
            return title;
        }

        //Closes the activity when all the items inside group are deleted
        private void closeGroup() {
            FTApp.getPref().saveLastGroupDocumentPosition(-1);
            FTApp.getPref().saveLastDocumentPosition(-1);
            FTApp.getPref().saveGroupDocumentUrl("");

            mSelectedGroup = null;

            mShelfFragment = (FTBaseShelfFragment) getSupportFragmentManager().findFragmentByTag(FTBaseShelfFragment.class.getName());
            getSupportFragmentManager().beginTransaction().replace(R.id.shelf_list_holder, mShelfFragment).commitAllowingStateLoss();

            updateToolbarTitle(mCurrentShelfItemCollection.getDisplayTitle(getContext()));

            doneEditing();

            FTApp.getPref().saveGroupDocumentUrl("");

            refreshAdapter();
        }

        public void closeLeftPanel() {
            valueAnimatorLeft(mShelfLeftPanel, 0, -Math.max(ndWidth, mShelfLeftPanel.getWidth()), () -> {
                if (mCategoriesFragment != null) {
                    getSupportFragmentManager().beginTransaction().remove(mCategoriesFragment).commitAllowingStateLoss();
                }
                isLeftPanelOpened = false;
                mShelfView.setVisibility(View.GONE);
            });
        }

        private void openLeftPanel() {
            mCategoriesFragment = new FTCategoriesFragment();
            getSupportFragmentManager().beginTransaction().replace(mCategoriesPanelContainer.getId(), mCategoriesFragment, FTCategoriesFragment.class.getName()).commitAllowingStateLoss();
            valueAnimatorLeft(mShelfLeftPanel, -Math.max(ndWidth, mShelfLeftPanel.getWidth()), 0);
            isLeftPanelOpened = true;

            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                if (getResources().getConfiguration().smallestScreenWidthDp < 600)
                    mCategoriesDivider.setVisibility(View.GONE);
                mShelfView.setVisibility(View.VISIBLE);
            }
        }

        private void valueAnimatorLeft(final View view, int start, int end) {
            valueAnimatorLeft(view, start, end, null);
        }

        private void valueAnimatorLeft(final View view, int start, int end, FTAnimationUtils.AnimationListener listener) {
            ValueAnimator animator = ValueAnimator.ofInt(start, end);
            animator.setDuration(400);
            animator.addUpdateListener(animation -> {
                if (view.getLayoutParams() instanceof LinearLayout.LayoutParams) {
                    LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) view.getLayoutParams();
                    lp.setMargins((Integer) animation.getAnimatedValue(), 0, 0, 0);
                    view.setLayoutParams(lp);
                } else {
                    RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) view.getLayoutParams();
                    lp.setMargins((Integer) animation.getAnimatedValue(), 0, 0, 0);
                    view.setLayoutParams(lp);
                }
            });
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    if (listener != null) listener.onAnimationEnd();
                }
            });
            animator.start();
        }

        //endregion
        @Override
        public boolean isInsideGroup() {
            return mSelectedGroup != null;
        }

        @Override
        public void openSelectedDocument(FTUrl fileUrl, final int from) {
            FTLog.crashlyticsLog("Doc: Open");
            isOpenDocument = true;
            if (fileUrl.getPath().contains("/" + getString(R.string.trash) + FTConstants.SHELF_EXTENSION)) {
                isOpenDocument = false;
                Snackbar snackbar = Snackbar.make(mShelfToolbar, R.string.cannot_open_notes_in_trash, Snackbar.LENGTH_LONG);
                View snackbarView = snackbar.getView();
                snackbarView.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
                ((Snackbar.SnackbarLayout) snackbarView).getChildAt(0).getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
                ((Snackbar.SnackbarLayout) snackbarView).getChildAt(0).setForegroundGravity(Gravity.CENTER);
                snackbarView.findViewById(R.id.snackbar_text).setForegroundGravity(Gravity.CENTER);
                snackbar.show();
            } else {
                final FTSmartDialog smartDialog = new FTSmartDialog()
                        .setMode(FTSmartDialog.FTSmartDialogMode.SPINNER)
                        .setMessage(getString(R.string.opening))
                        .show(getSupportFragmentManager());
                Executors.newSingleThreadExecutor().execute(() ->
                        FTDocumentActivity.openDocument(fileUrl, getContext(), (success, error) -> {
                            if (!isPinned(fileUrl)) {
                                FTShelfCollectionProvider.getInstance().recentShelfProvider.addRecent(fileUrl.getPath());
                            }
                            if (smartDialog != null && !isFinishing() && smartDialog.isAdded())
                                smartDialog.dismissAllowingStateLoss();
                            isOpenDocument = false;
                        }));
            }
        }

    @Override
    public void pinNotebook(FTUrl fileURL) {

    }

    @Override
    public void removeFromRecents(FTUrl fileURL) {

    }

    @Override
    public void unpinNotebook(FTUrl fileURL) {

    }

    @Override
        public boolean isOpeningDocument() {
            return isOpenDocument;
        }

        private Context getContext() {
            return this;
        }

        private ArrayList<Integer> getSelectedItemsPositions() {
            return mShelfFragment.getSelectedItemsPositions();
        }
        //endregion

        //region Custom listeners call back methods

        private List<FTShelfItem> getSelectedItems(ArrayList<Integer> positions) {
            List<FTShelfItem> selectedList = new ArrayList<>();

            for (Integer position : positions) {
                selectedList.add(mShelfFragment.getItemByPosition(position));
            }

            return selectedList;
        }

        //region Categories related
        @Override
        public void onShelfItemCollectionSelected(FTShelfItemCollection collection) {
            if (isLeftPanelOpened && mShelfView.getVisibility() == View.VISIBLE) {
                closeLeftPanel();
            }
            if (isInsideGroup()) {
                closeGroup();
            }
            doneEditing();
            if (collection != null) {
                mCurrentShelfItemCollection = collection;
                FTApp.getPref().saveRecentCollectionName(collection.getDisplayTitle(getContext()));
                refreshAdapter();
                updateToolbarTitle(collection.getDisplayTitle(getContext()));
                updateMenuOptions(mShelfToolbar.getMenu());
            } else {
                mCategoriesFragment.setCategoriesAdapter();
                refreshAdapter();
            }
        }

    @Override
    public void renameCollectionItem(String categoryName, int position, FTShelfItemCollection item) {

    }

    @Override
    public void removeCollectionItem(int position, FTShelfItemCollection item) {

    }

    @Override
    public void hideNavigationDrawer() {

    }

    //region Shelf adapter related
        public void refreshAdapter() {
            this.mCurrentShelfItemCollection.shelfItems(getContext(), FTShelfSortOrder.BY_NAME, mSelectedGroup, mSearchingText,
                    (notebooks, error) -> mShelfFragment.updateItems(notebooks));
        }

        @Override
        public void onSelectedItemsMoved(List<FTShelfItem> selectedItems) {
            valueAnimatorLeft(mMoveToFragmentContainer, 0, -mMoveToFragmentContainer.getWidth());
            mMoveToFragmentContainerView.setVisibility(View.GONE);
            if (mDrawerFragment != null) {
                getSupportFragmentManager().beginTransaction().remove(mDrawerFragment).commitAllowingStateLoss();
            }

            if (selectedItems != null && !selectedItems.isEmpty()) {
                mShelfFragment.moveItems(selectedItems);
            }

            if (selectedItems.isEmpty()) return;

            enableBackupPublishing();

            if (!selectedItems.isEmpty()) doneEditing();

            if (isInsideGroup() && mSelectedGroup.getChildren().isEmpty()) {
                closeGroup();
            }

            if (mCategoriesFragment != null) mCategoriesFragment.setCategoriesAdapter();
            refreshAdapter();
        }

        private void hideKeyboard(IBinder windowToken) {
            InputMethodManager imm = (InputMethodManager) FTApp.getInstance().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(windowToken, 0);
            }
        }
        //endregion

        @Override
        public void onShowDateModified(boolean isChecked) {
            mShelfFragment.onShowDateModified(isChecked);
        }

        @Override
        public void onStylusEnabled() {

        }

        @Override
        public FTShelfCollectionProvider getCollectionProvider() {
            return FTShelfCollectionProvider.getInstance();
        }

        //region Shelf edit mode operations.
        @Override
        public void disableEditMode() {
            doneEditing();
        }

        @Override
        public void selectAll() {
            mShelfFragment.selectAllItems();
        }

        @Override
        public void selectNone() {
            mShelfFragment.selectNone();
        }

        @Override
        public int getAllNotebooksCount() {
            List<FTShelfItem> items;
            if (isInsideGroup()) {
                items = mSelectedGroup.getChildren();
            } else {
                items = mCurrentShelfItemCollection.getChildren();
            }
            int count = 0;
            for (int i = 0; i < items.size(); i++) {
                if (!(items.get(i) instanceof FTGroupItem)) {
                    count++;
                }
            }
            return count;
        }

        @Override
        public void duplicateInEditMode() {
            FTFirebaseAnalytics.logEvent("shelf", "shelf_toolbar", "duplicate_notebooks");
            FTSmartDialog smartDialog = new FTSmartDialog()
                    .setMode(FTSmartDialog.FTSmartDialogMode.SPINNER)
                    .setMessage(getString(R.string.duplicating))
                    .show(getSupportFragmentManager());

            List<FTShelfItem> selectedItems = getSelectedItems(getSelectedItemsPositions());
            if (selectedItems.isEmpty()) {
                mShelfFragment.addOptionsItem(selectedItems);
            }

            FTDocumentFactory.duplicateDocuments(getContext(), selectedItems, (documents, group) -> {
                mShelfFragment.duplicateSelectedItems(documents);
                if (mShelfEditModeFragment != null && mShelfEditModeFragment.isAdded()) {
                    mShelfEditModeFragment.updateLayout(mShelfFragment.getSelecteditems().size());
                }
                smartDialog.dismissAllowingStateLoss();
                enableBackupPublishing();
            });
        }

        @Override
        public void moveInEditMode() {
            FTFirebaseAnalytics.logEvent("shelf", "shelf_toolbar", "move_notebooks");
            List<FTShelfItem> selectedItems = getSelectedItems(getSelectedItemsPositions());
            if (selectedItems.isEmpty()) {
                mShelfFragment.addOptionsItem(selectedItems);
            }
            if (selectedItems.size() > 0) {
                mDrawerFragment = FTCategoryMoveToFragment.newInstance(FTShelfCollectionProvider.getInstance(), selectedItems);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.move_to_fragment_container, mDrawerFragment)
                        .commitAllowingStateLoss();

                valueAnimatorLeft(mMoveToFragmentContainer, -mMoveToFragmentContainer.getWidth(), 0);
                mMoveToFragmentContainerView.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void deleteInEditMode(View view) {
            FTFirebaseAnalytics.logEvent("shelf", "shelf_toolbar", "delete_notebooks");
            final List<FTShelfItem> selectedItems = getSelectedItems(getSelectedItemsPositions());
            if (!selectedItems.isEmpty()) {
                final PopupWindow popupWindow = FTPopupFactory.create(this, view, R.layout.popup_delete_notebooks, R.dimen.new_320dp, R.dimen.new_154dp);
                View deleteNotebooksOptionView = popupWindow.getContentView();

                final TextView deleteMoveToTrashTextView = deleteNotebooksOptionView.findViewById(R.id.delete_notebooks_popup_delete);
                final TextView deleteCancelTextView = deleteNotebooksOptionView.findViewById(R.id.delete_notebooks_popup_cancel);

                deleteMoveToTrashTextView.setOnClickListener(v -> {
                    popupWindow.dismiss();
                    moveToTrash();
                });

                deleteCancelTextView.setOnClickListener(v -> popupWindow.dismiss());
            }
        }

        @Override
        public void moveToTrash() {
            FTLog.crashlyticsLog("UI: Confirmed deletion of notebooks in dialog");
            FTSmartDialog smartDialog = new FTSmartDialog()
                    .setMode(FTSmartDialog.FTSmartDialogMode.SPINNER)
                    .setMessage(getString(R.string.deleting))
                    .show(getSupportFragmentManager());
            List<FTShelfItem> selectedItems = getSelectedItems(getSelectedItemsPositions());
            if (selectedItems.isEmpty()) {
                mShelfFragment.addOptionsItem(selectedItems);
            }
            for (int i = 0; i < selectedItems.size(); i++) {
                FTShelfCollectionProvider.getInstance().pinnedShelfProvider.removePinned(selectedItems.get(i).getFileURL().getPath());
                FTShelfCollectionProvider.getInstance().recentShelfProvider.removeRecent(selectedItems.get(i).getFileURL().getPath());
            }
            mCurrentShelfItemCollection.removeShelfItems(getContext(), (notebooks, error) -> {
                if (error == null) {
                    doneEditing();
                    if (isInsideGroup() && mSelectedGroup.getChildren().isEmpty()) {
                        closeGroup();
                    }
                    refreshAdapter();
                }
                smartDialog.dismissAllowingStateLoss();
            }, selectedItems);
        }

        @Override
        public boolean isPinned(FTUrl fileURL) {
            return FTShelfCollectionProvider.getInstance().pinnedShelfProvider.checkIfPresent(fileURL.getPath());
        }

        @Override
        public void shareInEditMode(View view) {
            FTExportFormatPopup shareOptionsDialog = new FTExportFormatPopup();
            shareOptionsDialog.showNSAOption(() -> exportAsFormat(FTConstants.NSA_EXTENSION));
            shareOptionsDialog.showPDFOption(() -> exportAsFormat(FTConstants.PDF_EXTENSION));
            shareOptionsDialog.showPNGOption(() -> exportAsFormat(FTConstants.PNG_EXTENSION));
            shareOptionsDialog.show(view, getSupportFragmentManager());
        }

        @Override
        public void onThemeChosen(FTNTheme theme, boolean isCurrentPage,boolean isLandscapeStatus) {
            Log.d("TemplatePicker==>"," FTBaseshelfActivity onThemeChosen coverStyleInEditMode::-");
            //coverStyleInEditMode((FTNCoverTheme) theme);
            coverStyleInEditMode(theme);
        }

        @Override
        public void onClose() {

        }

        @Override
        public boolean isCurrentTheme() {
            return false;
        }

        @Override
        public void renameSelectedItems(String updatedName) {
            List<FTShelfItem> selectedItems = getSelectedItems(getSelectedItemsPositions());
            if (selectedItems.isEmpty()) {
                mShelfFragment.addOptionsItem(selectedItems);
            }

            final FTSmartDialog smartDialog = new FTSmartDialog();
            smartDialog.setMode(FTSmartDialog.FTSmartDialogMode.SPINNER);
            smartDialog.setMessage(getString(R.string.changing));
            smartDialog.show(getSupportFragmentManager());

            renameShelfItem(updatedName, selectedItems, 0, smartDialog);
        }

        private void renameShelfItem(String updatedName, List<FTShelfItem> selectedShelfItems, int index, FTSmartDialog smartDialog) {
            mCurrentShelfItemCollection.renameShelfItem(getContext(), updatedName, (movedBook, error) -> {
                if (selectedShelfItems.size() - 1 > index) {
                    renameShelfItem(updatedName, selectedShelfItems, index + 1, smartDialog);
                } else {
                    refreshAdapter();
                    disableEditMode();
                    if (smartDialog.isAdded()) smartDialog.dismiss();
                }
                enableBackupPublishing();
            }, selectedShelfItems.get(index));
        }

        @Override
        public void groupSelectedItems(String groupName) {
            List<FTShelfItem> selectedItems = getSelectedItems(getSelectedItemsPositions());
            mCurrentShelfItemCollection.createGroupItem(getContext(), selectedItems, (groupItem, error) -> {
                mShelfEditModeFragment.updateLayout(0);
                mBottomOptionsFragment.updateLayout(0);
                mShelfFragment.getSelecteditems().clear();
                refreshAdapter();
                enableBackupPublishing();
            }, groupName);
        }

        private void exportAsFormat(String exportFormat) {
            final FTSmartDialog smartDialog = new FTSmartDialog()
                    .setMessage(getString(R.string.generating))
                    .setMode(FTSmartDialog.FTSmartDialogMode.SPINNER)
                    .show(getSupportFragmentManager());

            List<FTShelfItem> selectedItems = getSelectedItems(getSelectedItemsPositions());
            if (selectedItems.isEmpty()) {
                mShelfFragment.addOptionsItem(selectedItems);
            }
            FTFileExporter fileExporter = new FTFileExporter();
            fileExporter.exportNotebooks(getContext(), selectedItems, exportFormat, (file, error) -> runOnUiThread(() -> {
                smartDialog.setMessage(getString(R.string.exporting));
                if (file != null && error == null) {
                    startActivityForResult(getShareFilteredIntent(FileUriUtils.getUriForFile(getContext(), file)), PICK_EXPORTER);
                } else {
                    Toast.makeText(getContext(), R.string.export_failed, Toast.LENGTH_SHORT).show();
                }
                smartDialog.dismissAllowingStateLoss();
            }));
            smartDialog.setCancellable(() -> {
                Toast.makeText(getContext(), R.string.cancelled, Toast.LENGTH_SHORT).show();
                fileExporter.cancelExporting();
            });
        }
        //endregion

        @Override
        public void coverStyleInEditMode(FTNTheme selectedCoverTheme) {
            Log.d("TemplatePicker==>"," FTBaseShelfActivity coverStyleInEditMode::-");
            final List<FTShelfItem> selectedItems = getSelectedItems(getSelectedItemsPositions());
            if (selectedItems.isEmpty()) {
                mShelfFragment.addOptionsItem(selectedItems);
            }
            FTNCoverTheme tempCoverTheme       = (FTNCoverTheme) coverThemItem(selectedCoverTheme);
            Log.d("TemplatePicker==>"," FTBaseShelfActivity coverStyleInEditMode tempCoverTheme::-"+tempCoverTheme);
            final FTSmartDialog smartDialog = new FTSmartDialog()
                    .setMode(FTSmartDialog.FTSmartDialogMode.SPINNER)
                    .setMessage(getString(R.string.changing))
                    .show(getSupportFragmentManager());
            Log.d("TemplatePicker==>"," FTBaseShelfActivity Inside updateShelfItemsCover::-"+selectedItems);
            FTDocumentFactory.updateShelfItemsCover(getContext(), selectedItems, (FTNCoverTheme) selectedCoverTheme, (success, error) -> {
                mShelfFragment.updateCoverStyleForSelectedItems(selectedItems);
                refreshAdapter();
                doneEditing();
                smartDialog.dismissAllowingStateLoss();
            });
        }
            @Override
            public void createNewShelfItem(String name, FTNTheme coverTheme, FTNTheme paperTheme, String origin) {
                final FTSmartDialog smartDialog = new FTSmartDialog();
                smartDialog.setMode(FTSmartDialog.FTSmartDialogMode.SPINNER)
                        .setMessage(getString(R.string.creating))
                        .show(getSupportFragmentManager());

                final FTNTheme tempPaperTheme       = paperThemeItem(paperTheme);
                final FTNTheme tempCoverTheme       = coverThemItem(coverTheme);

                Log.d("TemplatePicker==>"," FTBaseShelfActivity FTQuickCreateSettingsPopup Status createNewShelfItem paperTheme ftThemeType::-"
                        +paperTheme.ftThemeType+" tempPaperTheme ftThemeType::-"+tempPaperTheme.ftThemeType
                        +" paperTheme themeFileURL path::-" +paperTheme.themeFileURL.getPath()
                        +" isDefaultTheme::-"+paperTheme.isDefaultTheme+
                        " tempPaperTheme isDefaultTheme::-"+tempPaperTheme.isDefaultTheme+
                        " instanceof FTNPaperTheme::-"+(tempPaperTheme instanceof FTNPaperTheme)+
                        " tempPaperTheme::-"+tempPaperTheme+
                        " tempPaperTheme_thumbnailURLPath::-"+tempPaperTheme.thumbnailURLPath+
                        " paperTheme_thumbnailURLPath::-"+paperTheme.thumbnailURLPath+
                        " paperTheme_isLandscape::-"+paperTheme.isLandscape+
                        " tempPaperTheme_isLandscape::-"+tempPaperTheme.isLandscape);
                Log.d("TemplatePicker==>"," FTBaseShelfActivity coverTheme ftThemeType::-"+coverTheme.ftThemeType+" tempCoverTheme ftThemeType::-"+tempCoverTheme.ftThemeType+" coverTheme themeFileURL path::-" +coverTheme.themeFileURL.getPath());

                Log.d("TemplatePicker==>"," FTBaseShelfActivity coverTheme themeFileURL path::-"+coverTheme.themeFileURL.getPath());

                Log.d("TemplatePicker==>"," FTBaseShelfActivity RECENT_PAPER_THEME_URL::-"+paperTheme.themeFileURL.getPath());
                AsyncTask.execute(() -> {
                    tempPaperTheme.template(getContext(), (documentInfo, generationError) -> {
                                final FTUrl fileUri = FTDocumentFactory.tempDocumentPath(FTDocumentUtils.getUDID());
                                final FTNoteshelfDocument document = FTDocumentFactory.documentForItemAtURL(fileUri);
                                if (tempCoverTheme != null) {
                                    documentInfo.overlayStyle = FTCoverOverlayStyle.DEFAULT_STYLE;
                                    documentInfo.setCoverTheme((FTNCoverTheme) tempCoverTheme);
                                }

                                document.createDocument(getContext(), documentInfo, (success, error) -> {
                                    runOnUiThread(() -> {
                                        if (success) {
                                            addNewlyCreatedShelfItemToCurrentCollection(document.getFileURL(), name, (documentItem, error12) -> {
                                                if (documentItem != null) {
                                                    smartDialog.setMessage(getString(R.string.opening));
                                                    document.openDocument(getContext(), (success1, error1) -> runOnUiThread(() -> {
                                                        if (documentInfo.postProcessInfo.documentType == FTDocumentType.autoGeneratedDiary) {
                                                            FTDocumentPostProcessOperation.operation(documentInfo, document.getFileURL()).perform();
                                                        }
                                                        mShelfFragment.addNewItem(documentItem);
                                                        mShelfFragment.scrollToNewlyCreatedItem(documentItem);
                                                        smartDialog.dismissAllowingStateLoss();
                                                    }));
                                                    tempPaperTheme.basicTemplatePDFGenerated(documentInfo.inputFileURL,getContext());
                                                } else {
                                                    smartDialog.dismissAllowingStateLoss();
                                                }
                                            });
                                        } else {
                                            Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                });
                            });
                    });
                }

    private FTNTheme coverThemItem(FTNTheme coverTheme) {
        if ((coverTheme.ftThemeType == FTNThemeCategory.FTThemeType.COVER)) {
            ArrayList<RecentsInfoModel> recentsInfoList =  FTTemplateUtil.getInstance().getRecentCoversDummy();
            boolean recentsInfoListIsNullOrEmpty = FTTemplateUtil.isNullOrEmpty(recentsInfoList);
            Log.d("TemplatePicker==>","FTTemplateUtil FTBaseShelfActivity coverThemItem Recents List Existence status " +recentsInfoListIsNullOrEmpty);

            if (!recentsInfoListIsNullOrEmpty) {
                for (int i=0;i<recentsInfoList.size();i++) {
                    Log.d("TemplatePicker==>","FTTemplateUtil FTBaseShelfActivity coverThemItem get_packName " +recentsInfoList.get(i).get_packName()
                            +"get_thumbnailURLPath:: "+recentsInfoList.get(i).get_thumbnailURLPath());
                }

                boolean _themeAlreadyExists = recentsInfoList.stream()
                        .anyMatch(p -> p.get_thumbnailURLPath().equals(coverTheme.thumbnailURLPath));
                Log.d("TemplatePicker==>","FTTemplateUtil FTBaseShelfActivity coverThemItem Recents List Exists" +
                        " thumbnailURLPath::-"+coverTheme.thumbnailURLPath +
                        " width:: "+coverTheme.width+
                        " height:: "+coverTheme.height+
                        " _themeAlreadyExists:: "+_themeAlreadyExists);
                if (!_themeAlreadyExists) {
                    FTTemplateUtil.getInstance().saveRecentCoversDummy(coverTheme);
                }

            } else {
                Log.d("TemplatePicker==>","FTTemplateUtil FTBaseShelfActivity coverThemItem Recents List Fresh" +
                        " thumbnailURLPath::-"+coverTheme.thumbnailURLPath +
                        " width:: "+coverTheme.width+
                        " height:: "+coverTheme.height+" bitmap:: "+coverTheme.bitmap+
                        " isSavedForFuture:: "+coverTheme.isSavedForFuture);
                if (coverTheme.isCustomTheme) {
                    if (coverTheme.isSavedForFuture) {
                        FTTemplateUtil.getInstance().saveRecentCoversDummy(coverTheme);
                    }
                } else {
                    FTTemplateUtil.getInstance().saveRecentCoversDummy(coverTheme);
                }

            }
        }
        return coverTheme;
    }

    private FTNTheme paperThemeItem(FTNTheme paperTheme) {
        if ((paperTheme.ftThemeType == FTNThemeCategory.FTThemeType.PAPER)) {
            ArrayList<RecentsInfoModel> recentsInfoList =  FTTemplateUtil.getInstance().getRecentPapersDummy();
            boolean recentsInfoListIsNullOrEmpty = FTTemplateUtil.isNullOrEmpty(recentsInfoList);
            Log.d("TemplatePicker==>","FTTemplateUtil FTBaseShelfActivity Recents List Existence status " +recentsInfoListIsNullOrEmpty);

            if (!recentsInfoListIsNullOrEmpty) {
                for (int i=0;i<recentsInfoList.size();i++) {
                    Log.d("TemplatePicker==>","FTTemplateUtil FTBaseShelfActivity get_packName " +recentsInfoList.get(i).get_packName()
                            +"get_thumbnailURLPath:: "+recentsInfoList.get(i).get_thumbnailURLPath());
                }

                boolean _themeAlreadyExists = recentsInfoList.stream()
                        .anyMatch(p -> p.get_thumbnailURLPath().equals(paperTheme.thumbnailURLPath));
                Log.d("TemplatePicker==>","FTTemplateUtil FTBaseShelfActivity Recents List Exists" +
                        " thumbnailURLPath::-"+paperTheme.thumbnailURLPath +
                        " width:: "+paperTheme.width+
                        " height:: "+paperTheme.height+
                        " _themeAlreadyExists:: "+_themeAlreadyExists);
                if (!_themeAlreadyExists) {
                    FTTemplateUtil.getInstance().saveRecentPapersDummy(paperTheme);
                }

            } else {
                Log.d("TemplatePicker==>","FTTemplateUtil FTBaseShelfActivity Recents List Fresh" +
                        " thumbnailURLPath::-"+paperTheme.thumbnailURLPath +
                        " width:: "+paperTheme.width+
                        " height:: "+paperTheme.height+" bitmap:: "+paperTheme.bitmap);
                FTTemplateUtil.getInstance().saveRecentPapersDummy(paperTheme);
            }
        }
        return paperTheme;
    }

    private FTNTheme coverThemeItemDummy(FTNTheme paperTheme) {
        if ((paperTheme.ftThemeType == FTNThemeCategory.FTThemeType.COVER)) {
            ArrayList<RecentsInfoModel> recentsInfoList =  FTTemplateUtil.getInstance().getRecentPapersDummy();
            boolean recentsInfoListIsNullOrEmpty = FTTemplateUtil.isNullOrEmpty(recentsInfoList);
            Log.d("TemplatePicker==>","FTTemplateUtil FTBaseShelfActivity Recents List Existence status " +recentsInfoListIsNullOrEmpty);

            if (!recentsInfoListIsNullOrEmpty) {
                for (int i=0;i<recentsInfoList.size();i++) {
                    Log.d("TemplatePicker==>","FTTemplateUtil FTBaseShelfActivity get_packName " +recentsInfoList.get(i).get_packName()
                            +"get_thumbnailURLPath:: "+recentsInfoList.get(i).get_thumbnailURLPath());
                }

                boolean _themeAlreadyExists = recentsInfoList.stream()
                        .anyMatch(p -> p.get_thumbnailURLPath().equals(paperTheme.thumbnailURLPath));
                Log.d("TemplatePicker==>","FTTemplateUtil FTBaseShelfActivity Recents List Exists" +
                        " thumbnailURLPath::-"+paperTheme.thumbnailURLPath +
                        " width:: "+paperTheme.width+
                        " height:: "+paperTheme.height+
                        " _themeAlreadyExists:: "+_themeAlreadyExists);
                if (!_themeAlreadyExists) {
                    FTTemplateUtil.getInstance().saveRecentPapersDummy(paperTheme);
                }

            } else {
                Log.d("TemplatePicker==>","FTTemplateUtil FTBaseShelfActivity Recents List Fresh" +
                        " thumbnailURLPath::-"+paperTheme.thumbnailURLPath +
                        " width:: "+paperTheme.width+
                        " height:: "+paperTheme.height+" bitmap:: "+paperTheme.bitmap);
                FTTemplateUtil.getInstance().saveRecentPapersDummy(paperTheme);
            }
        }
        return paperTheme;
    }

    public static Boolean copyFile(File sourceFile, File destFile) throws IOException {
        if (!destFile.exists()) {
            destFile.createNewFile();

            FileChannel source = null;
            FileChannel destination = null;
            try {
                source = new FileInputStream(sourceFile).getChannel();
                destination = new FileOutputStream(destFile).getChannel();
                destination.transferFrom(source, 0, source.size());
            } finally {
                if (source != null)
                    source.close();
                if (destination != null)
                    destination.close();
            }
            return true;
        }
        return false;
    }

    public FTUrl getUrl(String fileName) {
        String path = "";
        String extension = StringUtil.getFileExtension(fileName);
        if (extension.equals("nsc")) {
            if (AssetsUtil.isAssetExists("stockCovers/" + fileName)) {
                path = "stockCovers/" + fileName;
            } else if (new File(FTConstants.DOWNLOADED_COVERS_PATH + fileName).exists()) {
                path = FTConstants.DOWNLOADED_COVERS_PATH + fileName;
            } else if (new File(FTConstants.CUSTOM_COVERS_PATH + fileName).exists()) {
                path = FTConstants.CUSTOM_COVERS_PATH + fileName;
            } else {
                return new FTUrl("");
            }
        } else if (extension.equals("nsp")) {
            if (AssetsUtil.isAssetExists("stockPapers/" + fileName) && (new File(FTConstants.DOWNLOADED_PAPERS_PATH2 + fileName).exists())) {
                path = FTConstants.DOWNLOADED_PAPERS_PATH2 + fileName;
            } else if (AssetsUtil.isAssetExists("stockPapers/" + fileName)) {
                path = "stockPapers/" + fileName;
            } else if (new File(FTConstants.DOWNLOADED_PAPERS_PATH2 + fileName).exists()) {
                path = FTConstants.DOWNLOADED_PAPERS_PATH2 + fileName;
            } else if (new File(FTConstants.CUSTOM_PAPERS_PATH + fileName).exists()) {
                path = FTConstants.CUSTOM_PAPERS_PATH + fileName;
            } else {
                return new FTUrl("");
            }
        }

        return new FTUrl(path);
    }

                    @Override
                    public void renameShelfItem(FTShelfItem shelfItem, String updatedName, FTShelfItemCollection.ShelfNotebookAndErrorBlock shelfNotebookAndErrorBlock) {
                        ArrayList<FTShelfItem> items = new ArrayList<>();
                        items.add(shelfItem);
                        mCurrentShelfItemCollection.renameShelfItem(getContext(), updatedName, (movedBook, error) -> {
                            ArrayList<FTShelfItem> items1 = new ArrayList<>();
                            items1.add(movedBook);
                            shelfNotebookAndErrorBlock.didFinishWithNotebookItem(movedBook, error);
                            enableBackupPublishing();
                        }, shelfItem);
                    }

                    @Override
                    public void getShelfItems(FTShelfItemCollection.ShelfNotebookItemsAndErrorBlock shelfNotebookItemsAndErrorBlock) {
                        mCurrentShelfItemCollection.shelfItems(getContext(), FTShelfSortOrder.BY_NAME, mSelectedGroup, mSearchingText, shelfNotebookItemsAndErrorBlock);
                    }

                    @Override
                    public boolean isItemExistsInSearch(FTShelfItem shelfItem) {
                        return shelfItem.getDisplayTitle(getContext()).contains(mSearchingText);
                    }

                    @Override
                    public void showRenameDialog(FTRenameDialog.RenameType type, String name, final int position, FTRenameDialog.RenameListener listener) {
                        FTRenameDialog.newInstance(type, name, position, listener).show(getSupportFragmentManager(), "FTRenameDialog");
                    }

                    //endregion
                    private void addNewlyCreatedShelfItemToCurrentCollection(FTUrl fileUrl, String displayTitle, FTShelfItemCollection.FTDocumentItemAndErrorBlock ftDocumentItemAndErrorBlock) {
                        if (mCurrentShelfItemCollection != null) {
                            if (mCurrentShelfItemCollection.isTrash(getContext())) {
                                FTShelfCollectionProvider.getInstance().shelfs(shelfs -> {
                                    FTBaseShelfActivity.this.mCurrentShelfItemCollection = shelfs.get(0);
                                    mCurrentShelfItemCollection.addShelfItemForDocument(getContext(), displayTitle, mSelectedGroup, ftDocumentItemAndErrorBlock, fileUrl);
                                });
                            } else {
                                mCurrentShelfItemCollection.addShelfItemForDocument(getContext(), displayTitle, mSelectedGroup, ftDocumentItemAndErrorBlock, fileUrl);
                            }
                        } else {
                            FTShelfCollectionProvider.getInstance().shelfs(shelfs -> {
                                String recentCategoryName = FTApp.getPref().getRecentCollectionName();
                                for (FTShelfItemCollection shelfItemCollection : shelfs) {
                                    if (shelfItemCollection.getDisplayTitle(getContext()).equals(recentCategoryName)) {
                                        FTBaseShelfActivity.this.mCurrentShelfItemCollection = shelfItemCollection;
                                        break;
                                    }
                                }
                                initializeViewRelated();
                                FTApp.getPref().saveDefaultNotebookCreated(true);
                                mCurrentShelfItemCollection.addShelfItemForDocument(getContext(), displayTitle, mSelectedGroup, ftDocumentItemAndErrorBlock, fileUrl);
                            });
                        }
                        enableBackupPublishing();
                    }

                    //region Pick file from outside app
                    public void onPickingDocumentFromDevice(Intent intent) {
                        onPickingDocumentFromDevice(intent, false);
                    }

                    private boolean openDocument = false;

                    public void onPickingDocumentFromDevice(Intent intent, boolean singleNewDocument) {
                        FTSmartDialog smartDialog = new FTSmartDialog();
                        smartDialog.setMode(FTSmartDialog.FTSmartDialogMode.SPINNER);
                        smartDialog.setMessage(getString(R.string.importing));
                        smartDialog.show(getSupportFragmentManager());

                        FTFileImporter fileImporter = new FTFileImporter();
                        fileImporter.setProgressListener((progress, total) -> openDocument = total == 1);
                        fileImporter.startImporting(getContext(), intent, true, singleNewDocument,
                                new FTFileImporter.FileImporterCallbacks() {
                                    @Override
                                    public void onEachFileImported(FTUrl importedFileUrl, Error importError) {
                                        if (importedFileUrl != null && importError == null) {
                                            String displayTitle = FTDocumentUtils.getFileNameWithoutExtension(getContext(), importedFileUrl);
                                            addNewlyCreatedShelfItemToCurrentCollection(importedFileUrl, displayTitle, (documentItem, error) -> runOnUiThread(() -> {
                                                if (error == null) {
                                                    mShelfFragment.addNewItem(documentItem);
                                                    if (openDocument) {
                                                        mShelfFragment.scrollToNewlyCreatedItem(documentItem);
                                                    }
                                                } else {
                                                    smartDialog.dismissAllowingStateLoss();
                                                    Toast.makeText(getContext(), R.string.failed_to_import, Toast.LENGTH_SHORT).show();
                                                }
                                                fileImporter.onUIUpdated();
                                            }));
                                        } else {
                                            runOnUiThread(() -> {
                                                smartDialog.dismissAllowingStateLoss();
                                                Toast.makeText(getContext(), importError != null ? (importError.getMessage() != null ? importError.getMessage() : getString(R.string.failed_to_import))
                                                        : getString(R.string.failed_to_import), Toast.LENGTH_SHORT).show();
                                                fileImporter.onUIUpdated();
                                            });
                                        }
                                    }

                                    @Override
                                    public void onAllFilesImported(boolean isCancelled) {
                                        runOnUiThread(() -> {
                                            smartDialog.dismissAllowingStateLoss();
                                            if (isCancelled)
                                                Toast.makeText(getContext(), R.string.cancelled, Toast.LENGTH_LONG).show();
                                        });
                                    }
                                });
                        smartDialog.setCancellable(fileImporter::cancelImporting);
                    }
                    //endregion

                    //region Group operations

                    public void importDocument(int requestCode) {
                        if (FTPermissionManager.checkPermission(this,
                                this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, requestCode)) {
                            Intent intent = new Intent();
                            intent.addCategory(Intent.CATEGORY_OPENABLE);
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType(getString(R.string.mime_type_all));
                            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                            String[] mimeTypes = {getString(R.string.mime_type_application_pdf), getString(R.string.mime_type_application_all), getString(R.string.mime_type_application_text)};
                            intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
                            startActivityForResult(Intent.createChooser(intent, getString(R.string.mime_type_all)), requestCode);
                        }
                    }

                    public void importImages(int requestCode) {
                        if (FTPermissionManager.checkPermission(this, this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, IMPORT_IMAGES)) {
                            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                            intent.addCategory(Intent.CATEGORY_OPENABLE);
                            intent.setType(getString(R.string.mime_type_image));
                            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                            startActivityForResult(Intent.createChooser(intent, getString(R.string.mime_type_image)), requestCode);
                        }
                    }
                    //endregion

                    //region System listeners call back methods

                    @Override
                    public void openGroup(FTGroupItem groupItem) {
                        File groupFolder = new File(groupItem.getFileURL().getPath());
                        if (groupFolder.exists()) {
                            mSelectedGroup = groupItem;
                            FTApp.getPref().saveGroupDocumentUrl(groupItem.getFileURL().getPath());
                            mShelfFragment = new FTShelfGroupableFragment();
                            getSupportFragmentManager().beginTransaction().add(R.id.shelf_list_holder, mShelfFragment, FTShelfGroupableFragment.class.getName()).commitAllowingStateLoss();

                            updateToolbarTitle(groupItem.getDisplayTitle(getContext()));
                            mShelfToolbar.setSubtitle(mCurrentShelfItemCollection.getDisplayTitle(getContext()));
                        }
                    }
                    //endregion

                    @Override
                    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
                        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                        if (requestCode == IMPORT_DOCUMENT || requestCode == IMPORT_IMAGES) {
                            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                                if (requestCode == IMPORT_DOCUMENT) {
                                    importDocument(IMPORT_DOCUMENT);
                                } else {
                                    importImages(IMPORT_IMAGES);
                                }
                            } else {
                                Toast.makeText(this, R.string.document_access_error, Toast.LENGTH_LONG).show();
                            }
                        } else if (requestCode == REQUEST_CODE_ADD_COVER_THEME && (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED))
                            pickFromGallery(requestCode);
                    }

                    private Observer backupErrorObserver = (observable, o) -> setUpBackUpErrorIcon();

                    @Override
                    public void startGrouping(FTShelfItem draggingItem, FTShelfItem mergingItem, int draggingPosition, int endPosition) {

                    }

                    @Override
                    public void onCurrentShelfItemCollectionRenamed(FTShelfItemCollection shelf) {
                        FTApp.getPref().saveRecentCollectionName(shelf.getDisplayTitle(getContext()));
                        mCurrentShelfItemCollection = shelf;
                        if (isInsideGroup()) {
                            String path = mSelectedGroup.getDisplayTitle(getContext());
                            mSelectedGroup.setFileURL(shelf.getFileURL().withAppendedPath(path + FTConstants.GROUP_EXTENSION));
                            mShelfToolbar.setSubtitle(shelf.getDisplayTitle(getContext()));
                        } else {
                            updateToolbarTitle(shelf.getDisplayTitle(getContext()));
                        }
                        if (mCategoriesFragment != null) mCategoriesFragment.setCategoriesAdapter();
                        refreshAdapter();
                    }

                    @Override
                    public void onShelfItemCollectionDeleted(FTShelfItemCollection shelf) {
                        if (shelf.getDisplayTitle(getContext()).equals(mCurrentShelfItemCollection.getDisplayTitle(getContext()))) {
                            if (isInsideGroup()) closeGroup();
                            FTShelfCollectionProvider.getInstance().shelfs(shelfs -> {
                                mCurrentShelfItemCollection = shelfs.get(0);
                                FTApp.getPref().saveRecentCollectionName(mCurrentShelfItemCollection.getDisplayTitle(getContext()));
                                updateToolbarTitle(mCurrentShelfItemCollection.getDisplayTitle(getContext()));

                                mCategoriesFragment.setCategoriesAdapter();
                                refreshAdapter();
                            });
                        } else {
                            mCategoriesFragment.setCategoriesAdapter();
                            refreshAdapter();
                        }
                    }

    @Override
    public void openNewNotebookdialog(FTNTheme ftnTheme) {
        Log.d("TemplatePickerV2", "NewNotebook FTBaseShelfActivity openNewNotebookdialog ftnTheme.themeName:: "+ftnTheme.themeName);
        //FTTemplateUtil.getInstance().saveRecentPaperThemeFromNewNotebookDialog(ftnTheme);
        FTNewNotebookDialog.newInstance(this).show(getSupportFragmentManager(), FTNewNotebookDialog.class.getName());
    }

    //region Interfaces
                    public interface FTCoversUpdatedBlock {
                        void didFinishWithStatus(Boolean success, Error error);
                    }


                    public interface FTDuplicateDocumentsBlock {
                        void didFinishWithWithDocuments(List<FTShelfItem> documents, FTShelfItem group);
                    }
                    //endregion


                    protected void enableBackupPublishing() {
                        if (FTApp.getPref().getBackUpType() != SystemPref.BackUpType.NONE.ordinal() && FTApp.getServicePublishManager() != null) {
                            FTServiceAccountHandler accountHandler = FTServiceAccountHandler.getInstance();
                            if (accountHandler != null && accountHandler.checkSession(getContext())) {
                                FTApp.getServicePublishManager().startPublishing();
                            }
                        }
                    }

                    @Override
                    public void updateBackupType() {

                    }

                    @Override
                    public void openChooseCloudDialog() {
                        FTChooseCloudDialog.newInstance().show(getSupportFragmentManager());
                    }

                    @Override
                    public void addCustomTheme(FTNTheme theme) {
                        if (theme.ftThemeType == FTNThemeCategory.FTThemeType.COVER) {
                            if (FTPermissionManager.checkPermission(this, this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_ADD_COVER_THEME)) {
                                pickFromGallery(REQUEST_CODE_ADD_COVER_THEME);
                            }
                        } else {
                            if (FTPermissionManager.checkPermission(this, this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_ADD_PAPER_THEME)) {
                                importDocument(REQUEST_CODE_ADD_PAPER_THEME);
                            }
                        }
                    }

                    public void pickFromGallery(int requestCode) {
                        Intent intent = new Intent();
                        intent.setType("image/*");
                        intent.setAction(Intent.ACTION_GET_CONTENT);//
                        intent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                        startActivityForResult(Intent.createChooser(intent, "Select Picture"), requestCode);
                    }
                }