package com.fluidtouch.noteshelf.document;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ComponentCallbacks2;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.DragEvent;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf.audio.FTAudioToolbarFragment;
import com.fluidtouch.noteshelf.audio.models.FTAudioPlayerStatus;
import com.fluidtouch.noteshelf.audio.models.FTAudioRecording;
import com.fluidtouch.noteshelf.audio.player.FTAudioPlayer;
import com.fluidtouch.noteshelf.audio.popup.FTAudioDialog;
import com.fluidtouch.noteshelf.clipart.FTClipartDialog;
import com.fluidtouch.noteshelf.commons.FTLog;
import com.fluidtouch.noteshelf.commons.settingsUI.dialogs.FTSettingsDialog;
import com.fluidtouch.noteshelf.commons.ui.FTBaseActivity;
import com.fluidtouch.noteshelf.commons.ui.FTDialogFactory;
import com.fluidtouch.noteshelf.commons.ui.FTExportFormatPopup;
import com.fluidtouch.noteshelf.commons.ui.FTSmartDialog;
import com.fluidtouch.noteshelf.commons.utils.FTFileManagerUtil;
import com.fluidtouch.noteshelf.commons.utils.FTPermissionManager;
import com.fluidtouch.noteshelf.commons.utils.FTSPenAirActions;
import com.fluidtouch.noteshelf.commons.utils.FileUriUtils;
import com.fluidtouch.noteshelf.commons.utils.KeyboardHeightProvider;
import com.fluidtouch.noteshelf.commons.utils.ObservingService;
import com.fluidtouch.noteshelf.document.dialogs.FTNotebookOptionsPopup;
import com.fluidtouch.noteshelf.document.dialogs.FTSelectPagesPopup;
import com.fluidtouch.noteshelf.document.dialogs.addnew.AddNewPopupListener;
import com.fluidtouch.noteshelf.document.enums.FTToolBarTools;
import com.fluidtouch.noteshelf.document.enums.PenOrHighlighterInfo;
import com.fluidtouch.noteshelf.document.lasso.FTLassoFragment;
import com.fluidtouch.noteshelf.document.lasso.ScreenshotDialogFragment;
import com.fluidtouch.noteshelf.document.penracks.FTFavPenRackDialog;
import com.fluidtouch.noteshelf.document.penracks.FTPenRackDialog;
import com.fluidtouch.noteshelf.document.penracks.favorites.Favorite;
import com.fluidtouch.noteshelf.document.search.FTFinderSearchOptions;
import com.fluidtouch.noteshelf.document.thumbnailview.FTThumbnailFragment;
import com.fluidtouch.noteshelf.document.undomanager.FTUndoRedoDialog;
import com.fluidtouch.noteshelf.document.undomanager.UndoManager;
import com.fluidtouch.noteshelf.document.views.FTDrawerLayout;
import com.fluidtouch.noteshelf.document.views.FTDrawingView;
import com.fluidtouch.noteshelf.document.views.FTViewPager;
import com.fluidtouch.noteshelf.documentframework.FTDocument.FTDocument;
import com.fluidtouch.noteshelf.documentframework.FTDocument.FTDocumentFactory;
import com.fluidtouch.noteshelf.documentframework.FTNoteshelfDocument.CompletionBlock;
import com.fluidtouch.noteshelf.documentframework.FTNoteshelfDocument.FTDocumentInputInfo;
import com.fluidtouch.noteshelf.documentframework.FTNoteshelfDocument.FTNoteshelfDocument;
import com.fluidtouch.noteshelf.documentframework.FTNoteshelfDocument.FTNoteshelfPage;
import com.fluidtouch.noteshelf.documentframework.FTUrl;
import com.fluidtouch.noteshelf.documentframework.FileExporter.FTFileExporter;
import com.fluidtouch.noteshelf.documentframework.FileImporter.FTFileImporter;
import com.fluidtouch.noteshelf.documentframework.ThumbnailGenerator.FTThumbnailGenerator;
import com.fluidtouch.noteshelf.documentframework.Utilities.FTConstants;
import com.fluidtouch.noteshelf.documentproviders.FTShelfCollectionProvider;
import com.fluidtouch.noteshelf.documentproviders.FTShelfItemCollection;
import com.fluidtouch.noteshelf.generator.FTAutoTemplateGenerationCallback;
import com.fluidtouch.noteshelf.models.disk.diskItem.shelfItem.FTShelfItem;
import com.fluidtouch.noteshelf.models.theme.FTNCoverTheme;
import com.fluidtouch.noteshelf.models.theme.FTNPaperTheme;
import com.fluidtouch.noteshelf.models.theme.FTNTheme;
import com.fluidtouch.noteshelf.models.theme.FTNThemeCategory;
import com.fluidtouch.noteshelf.preferences.FTBasePref;
import com.fluidtouch.noteshelf.preferences.IndividualDocumentPref;
import com.fluidtouch.noteshelf.preferences.PenRackPref;
import com.fluidtouch.noteshelf.preferences.SystemPref;
import com.fluidtouch.noteshelf.scandocument.ScanActivity;
import com.fluidtouch.noteshelf.services.FTFirebaseAnalytics;
import com.fluidtouch.noteshelf.shelf.activities.FTBaseShelfActivity;
import com.fluidtouch.noteshelf.shelf.adapters.FTCategoryAdapter;
import com.fluidtouch.noteshelf.shelf.fragments.FTRenameDialog;
import com.fluidtouch.noteshelf.shelf.fragments.FtShelfItemsViewFragment;
import com.fluidtouch.noteshelf.shelf.viewholders.FTCategoryViewHolder;
import com.fluidtouch.noteshelf.store.ui.FTChooseCoverPaperDialog;
import com.fluidtouch.noteshelf.templatepicker.FTChoosePaperTemplate;
import com.fluidtouch.noteshelf.templatepicker.FTTemplateMode;
import com.fluidtouch.noteshelf.templatepicker.common.plistdatamodel.FTSelectedDeviceInfo;
import com.fluidtouch.noteshelf.templatepicker.common.util.FTTemplateUtil;
import com.fluidtouch.noteshelf.templatepicker.models.FTTemplatepickerInputInfo;
import com.fluidtouch.noteshelf.templatepicker.models.RecentsInfoModel;
import com.fluidtouch.noteshelf2.BuildConfig;
import com.fluidtouch.noteshelf2.R;
import com.fluidtouch.renderingengine.annotation.FTPenType;
import com.fluidtouch.renderingengine.currentStroke.FTStrokeAttributes;
import com.fluidtouch.renderingengine.renderer.FTGLContextFactory;
import com.fluidtouch.renderingengine.renderer.FTRenderMode;
import com.fluidtouch.renderingengine.renderer.FtTempClass;
import com.fluidtouch.renderingengine.utils.FTGeometryUtils;
import com.google.gson.Gson;
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.fluidtouch.noteshelf.document.imageedit.FTImageAdvanceEditingAcitivity.IMAGE_ACTIVITY;

public class FTDocumentActivity extends FTBaseActivity implements FTThumbnailFragment.FTThumbnailListener,
        FTChooseCoverPaperDialog.CoverChooseListener,
        KeyboardHeightProvider.KeyboardHeightObserver,
        ComponentCallbacks2, FTDocument.FTDocumentDelegate,
        FTDocumentToolbarFragment.DocumentToolbarFragmentInteractionListener,
        FTDocumentPageFragment.PagerToActivityCallBack,
        FTAudioDialog.AudioDialogContainerCallback, View.OnDragListener,
        FTRefreshFragment.RefreshFragmentListener, FTClipartDialog.ClipartDialogListener,
        FTPenRackDialog.PenRackDialogListener, FTFavPenRackDialog.FavWidgetToolBarListener,
        FTNotebookOptionsPopup.NotebookOptionsPopupListener, FTSelectPagesPopup.SelectPagesPopupListener,
        FTSettingsDialog.SettingsListener,
        ScreenshotDialogFragment.ScreenShotDialogListener, FTLassoFragment.LassoFragmentScreenshotListener,
        FTDocumentPageFragment.FTEraseEndedListener, AddNewPopupListener, FTChoosePaperTemplate.TemplatePaperChooseListener {

    public static final int DOCUMENT_ACTIVITY = 198;
    private static final int PICK_FROM_GALLERY = 101;
    private static final int PICK_FROM_CAMERA = 102;
    private static final int IMPORT_DOCUMENT = 103;
    private static final int REQ_PERMISSION_RECORD_AUDIO = 104;
    public static final int SCAN_DOCUMENT = 105;
    public static final int IMPORT_PAGE_PHOTO = 106;
    private static final int PAGE_FROM_PHOTO = 107;
    public static int SCROLL_STATE = ViewPager.SCROLL_STATE_IDLE;
    public static int isAnnotationOpen = 0;
    public static String KEY_ENABLE_UNDO = "enableUndoButton";
    //region Member Variables
    @BindView(R.id.ftdocument_drawer_layout)
    FTDrawerLayout mDrawerLayout;
    @BindView(R.id.surfacePager)
    FTViewPager surfacePager;

    @BindView(R.id.ftdocument_fav_toolbar_fragment_layout)
    FrameLayout favToolBarLyt;

    private FTNoteshelfDocument currentDocument;
    private boolean isFirstTimeLoading = true;
    private boolean showThumbnails = false;
    private boolean isThumbnailRefreshed = false;
    private int newThumbnailIndex = 0;
    private int currPosition = 0;
    private IndividualDocumentPref mDocPref;
    protected FTAudioToolbarFragment mAudioToolbarFragment;
    private FTFinderSearchOptions searchOptions;
    private FTDocumentPagerAdapter adapter;
    //private ObservingService mLayoutChangeObserver;
    private Uri fileProvider;
    private FTDocumentPageFragment currentPageController;
    private FTDocumentToolbarFragment mDocumentToolbarFragment;
    private FTFavPenRackDialog mFTFavPenRackDialog;
    private Fragment drawerFragment;
    //Eraser views
    //endregion
    private KeyboardHeightProvider keyboardHeightProvider;
    private PenRackPref mPenPref;
    private String mPrefPenKey = "", mPrefSizeKey = "", mPrefColorKey = "", mPrefCheckBoxKey = "mPrefCheckBoxKey", mPrefCurrentSelection = "mPrefCurrentSelection";
    public static final int PEN = 1;
    public static final int HIGHLIGHTER = 2;
    public static final int ERASER = 3;
    FTPenRackDialog penRackDialog;
    private float zoomScale = 1;
    private boolean isAllGesturesEnabled = true;
    private int currentPencolor = 0;
    private int currentHighlighterColor = 0;
    private int currentPenSize = 0;
    private int currentHighlighterSize = 0;
    private String CURRENT_DOCUMENT_UID = "";

    private FTSmartDialog docOpeningDialog;
    ArrayList<FTNTheme> ftnThemeArrayListPrefs = new ArrayList<>();
    ArrayList<String> ftnThemeThumbnailURLPathList = new ArrayList<>();

    private Observer mAudioObserver = new Observer() {

        @Override
        public void update(Observable o, Object arg) {
            FTAudioPlayerStatus status = (FTAudioPlayerStatus) arg;
            if (status.getPlayerMode() == FTAudioPlayerStatus.FTPlayerMode.PLAYING_STARTED) {
                addAudioToolbarFragment(false);
            } else if (status.getPlayerMode() == FTAudioPlayerStatus.FTPlayerMode.RECORDING_STARTED) {
                addAudioToolbarFragment(true);
            } else if (status.getPlayerMode() == FTAudioPlayerStatus.FTPlayerMode.RECORDING_PROGRESS) {
                if (mAudioToolbarFragment == null) {
                    addAudioToolbarFragment(true);
                }
            } else if (status.getPlayerMode() == FTAudioPlayerStatus.FTPlayerMode.RECORDING_STOPPED) {
                //Do Nothing
            } else {
                if (mAudioToolbarFragment == null) {
                    addAudioToolbarFragment(false);
                }
            }
        }

        private void addAudioToolbarFragment(boolean isForRecording) {
            mAudioToolbarFragment = FTAudioToolbarFragment.newInstance(isForRecording, () -> {
                currentPageController.onOutsideTouch(FTToolBarTools.AUDIO, null);
                mAudioToolbarFragment = null;
            });
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.document_audio_fragment_container, mAudioToolbarFragment)
                    .commit();
        }
    };

    private Observer mSwipeGestureObserver = new Observer() {
        @Override
        public void update(Observable o, Object arg) {
            mDrawerLayout.setDrawerLockMode();
        }
    };

    private Observer onSearchKeyChanged = (o, arg) -> {
        adapter.setSearchKey((String) arg);
        if (this.searchOptions == null) {
            this.searchOptions = new FTFinderSearchOptions();
        }
        this.searchOptions.searchedKeyword = (String) arg;
    };

    private Observer enableUndoButtonObserver = (o, arg) -> {
        enableUndoButton();
    };

    private DrawerLayout.SimpleDrawerListener mDrawerListener = new DrawerLayout.SimpleDrawerListener() {
        @Override
        public void onDrawerClosed(@NonNull View view) {
            ((FTThumbnailFragment) drawerFragment).isExportMode = false;
            ((FTThumbnailFragment) drawerFragment).setInitialSetUp();
            ((FTThumbnailFragment) drawerFragment).onDrawerClosed();
            if (drawerFragment != null) {
                if (isThumbnailRefreshed) {
                    isThumbnailRefreshed = false;
                }
            }

            if (mShelfItemsViewFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .remove(mShelfItemsViewFragment).commitAllowingStateLoss();
            }

            if (currentMode() == FTToolBarTools.LASSO && currentPageController.mLassoFragment == null) {
                currentPageController.addLassoFragment(findViewById(R.id.doc_toolbar_lasso_image_view));
            }

            currentPageController.mWritingView.setVisibility(View.VISIBLE);

        }

        @Override
        public void onDrawerOpened(View drawerView) {
            currentPageController.mWritingView.setVisibility(View.INVISIBLE);
            showFinder();
            super.onDrawerOpened(drawerView);
        }
    };
    private ViewPager.SimpleOnPageChangeListener mPageChangeListener = new ViewPager.SimpleOnPageChangeListener() {
        @Override
        public void onPageSelected(int position) {
            if (position == 0) {
                mDocPref.save("currentPage", position + 1);
                mDocumentToolbarFragment.setUpToolbar(FTDocumentToolbarFragment.FTToolbarMode.DISABLE);
                hideFavToolbar();

            } else if (position == adapter.getCount() - 1) {
                mDocPref.save("currentPage", position - 1);
                mDocumentToolbarFragment.setUpToolbar(FTDocumentToolbarFragment.FTToolbarMode.DISABLE);
                hideFavToolbar();
            } else {
                mDocPref.save("currentPage", position);
                mDocumentToolbarFragment.setUpToolbar(FTDocumentToolbarFragment.FTToolbarMode.ENABLE);
                if (mPenPref.get(mPrefCheckBoxKey, false)) {
                    setupFavToolbar();
                }
            }
            currPosition = position;
            mDocumentToolbarFragment.enableUndo(false);
            Fragment fragment = adapter.getFragmentAtIndex(position);
            if (fragment instanceof FTDocumentPageFragment) {
                setCurrentPageController((FTDocumentPageFragment) fragment);
                if (mDocumentToolbarFragment.currentMode() != FTToolBarTools.LASSO && currentPageController != null) {
                    currentPageController.resetAnnotationFragment(null);
                    currentPageController.removeLassoRelatedViews();
                }
            }

            saveDocumentNow(null);
            FTLog.saveLog("DocumentActivity onPageSelected ");
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            super.onPageScrollStateChanged(state);
            SCROLL_STATE = state;
            if (state == ViewPager.SCROLL_STATE_IDLE) {
                setZoomScale(1);
                ObservingService.getInstance().postNotification("viewPagerState_" + CURRENT_DOCUMENT_UID, surfacePager.getCurrentItem());
            }
        }
    };
    private FTDrawingView drawingView = null;
    private boolean isSharedContextDestroyed = false;

    public static void openDocument(FTUrl fileUrl, Context context, CompletionBlock completionBlock) {
        openDocument(fileUrl, context, -1, "", completionBlock);
    }

    public static void openDocument(FTUrl fileUrl, FTNoteshelfDocument document, Context context, int newIndex, String searchKey, CompletionBlock completionBlock) {
        if (document != null) {
            new Handler().postDelayed(() -> {
                completionBlock.didFinishWithStatus(true, null);
                FTNDTempHolder.INSTANCE.putNDDocument(document);
                FTApp.CURRENT_EDIT_DOCUMENT_UIDS.add(document.getDocumentUUID());
                startActivity(fileUrl, context, newIndex + 1, searchKey);
            }, 100);
        } else {
            openDocument(fileUrl, context, newIndex, searchKey, completionBlock);
        }
    }

    public static void openDocument(FTUrl fileUrl, Context context, int newIndex, String searchKey, CompletionBlock completionBlock) {
        final FTNoteshelfDocument bookToOpen = FTDocumentFactory.documentForItemAtURL(fileUrl);
        bookToOpen.openDocument(context, (success, error) -> {
            if (success) {
                FTNDTempHolder.INSTANCE.putNDDocument(bookToOpen);
                if (FTConstants.ENABLE_HW_RECOGNITION) {
                    bookToOpen.handwritingRecognitionHelper(context).startPendingRecognition();
                }
                if (FTConstants.ENABLE_VISION_RECOGNITION)
                    bookToOpen.visionRecognitionHelper(context).startPendingRecognition();
                bookToOpen.imageRecognitionHelper(context).wakeUpRecognitionHelperIfNeeded();
                FTApp.CURRENT_EDIT_DOCUMENT_UIDS.add(bookToOpen.getDocumentUUID());
                completionBlock.didFinishWithStatus(success, error);
                startActivity(fileUrl, context, newIndex, searchKey);
            } else {
                ((Activity) context).runOnUiThread(() -> {
                    Toast.makeText(context, error.getMessage(), Toast.LENGTH_SHORT).show();
                    completionBlock.didFinishWithStatus(success, error);
                });
            }
        });
    }

    private static void startActivity(FTUrl fileUrl, Context context, int newIndex, String searchKey) {
        Intent intent = new Intent(context, FTDocumentActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable(context.getString(R.string.intent_current_document), fileUrl);
        if (newIndex >= 0)
            bundle.putInt(context.getString(R.string.intent_new_page_index), newIndex);
        bundle.putBoolean(context.getString(R.string.intent_is_thumbnail_refreshed), false);
        bundle.putString(context.getString(R.string.intent_search_key), searchKey);
        intent.putExtras(bundle);
        ((AppCompatActivity) context).startActivityForResult(intent, DOCUMENT_ACTIVITY);
    }

    private void setCurrentPageController(FTDocumentPageFragment fragment) {
        if (null != currentPageController) {
            if (currentMode() == FTToolBarTools.LASSO && currentPageController.mLassoFragment != null) {
                currentPageController.mLassoFragment.lassoCanvasOutsideTouch();
            }
            currentPageController.isPageChanged = true;
        }

        currentPageController = fragment;

        if (currentMode() == FTToolBarTools.LASSO && currentPageController.mLassoFragment == null) {
            enableLassoMode(mDocumentToolbarFragment.mLasso);
        }
    }

    //region Lifecycle Events
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        FTGLContextFactory.getInstance().setUpContext();
        FTLog.crashlyticsLog("DocActivity: Starting");
        setFullScreenMode();
        setContentView(R.layout.activity_ftdocument);
        FTApp.getInstance().setCurActCtx(this);
        ButterKnife.bind(this);

        FTApp.getPref().saveDocumentUrl("");
        mPenPref = new PenRackPref().init(PenRackPref.PREF_NAME);

        if (favToolBarLyt != null) {
            favToolBarLyt.setBackgroundColor(Color.TRANSPARENT);
        }

        if (mPenPref.get(mPrefCheckBoxKey, false)) {
            setupFavToolbar();
        }

        docOpeningDialog = new FTSmartDialog()
                .setMode(FTSmartDialog.FTSmartDialogMode.SPINNER)
                .setMessage(getString(R.string.opening))
                .show(getSupportFragmentManager());
//        Executors.newSingleThreadExecutor().execute(() -> {
        initCurrentStrokeAttributes();
        setUpToolBar();
        currentDocument = FTNDTempHolder.INSTANCE.getNDDocument(0);
        if (null == currentDocument) {
            FTUrl fileURL = (FTUrl) getIntent().getSerializableExtra(this.getString(R.string.intent_current_document));
            final FTNoteshelfDocument bookToOpen = FTDocumentFactory.documentForItemAtURL(fileURL);
            bookToOpen.openDocument(getContext(), new CompletionBlock() {
                @Override
                public void didFinishWithStatus(Boolean success, Error error) {
                    //Log.d("TemplatePicker==>","VMK PasswordProtected FTDocumentActivity openDocument success::-"+success);
                    runOnUiThread(() -> {
                        if (success) {
                            currentDocument = bookToOpen;
                            initData();
                        } else {
                            Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        } else {
            initData();
        }
        mDrawerLayout.addDrawerListener(mDrawerListener);
        StrictMode.setVmPolicy(StrictMode.VmPolicy.LAX);
        getWindow().setStatusBarColor(FTConstants.statusBarColor);
        keyboardHeightProvider = new KeyboardHeightProvider(this);
//        });

    }

    private void setupFavToolbar() {
        favToolBarLyt.setVisibility(View.VISIBLE);
        mFTFavPenRackDialog = new FTFavPenRackDialog();
        getSupportFragmentManager().
                beginTransaction().
                replace(R.id.ftdocument_fav_toolbar_fragment_layout, mFTFavPenRackDialog).
                commitAllowingStateLoss();
    }

    private void hideFavToolbar() {
        if (mFTFavPenRackDialog != null) {
            favToolBarLyt.setVisibility(View.GONE);
            getSupportFragmentManager().
                    beginTransaction().
                    remove(mFTFavPenRackDialog).
                    commitAllowingStateLoss();
            mFTFavPenRackDialog = null;
        }
    }

    private void setFullScreenMode() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        final int flags = getFullScreenModeFlags();

        getWindow().getDecorView().setSystemUiVisibility(flags);
        // Code below is to handle presses of Volume up or Volume down.
        // Without this, after pressing volume buttons, the navigation bar will
        // show up and won't hide
        final View decorView = getWindow().getDecorView();
        decorView.setOnSystemUiVisibilityChangeListener(visibility -> {
            if ((visibility & View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION) == 0) {
                decorView.setSystemUiVisibility(flags);
            }
        });
    }

    private int getFullScreenModeFlags() {
        return View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(getFullScreenModeFlags());
        }
    }

    public Fragment getVisibleFragment() {
        FragmentManager fragmentManager = this.getSupportFragmentManager();
        List<Fragment> fragments = fragmentManager.getFragments();
        if (fragments != null) {
            for (Fragment fragment : fragments) {
                if (fragment != null && fragment.isVisible())
                    return fragment;
            }
        }
        return null;
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mDrawerLayout.post(() -> {
            if (keyboardHeightProvider != null) keyboardHeightProvider.start();
        });
        FTApp.getInstance().setCurActCtx(this);

        FTDocumentActivity.isAnnotationOpen = 0;
        if (keyboardHeightProvider != null) {
            keyboardHeightProvider.setKeyboardHeightObserver(this);
        }
        if (mDocumentToolbarFragment != null) {
            mDocumentToolbarFragment.updateLastSelectedToolView(true);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (keyboardHeightProvider != null)
            keyboardHeightProvider.setKeyboardHeightObserver(null);
        saveDocumentNow(null);
        if (currentPageController != null && FTDocumentActivity.isAnnotationOpen == 0) {
            currentPageController.resetAnnotationFragment(null);
        }
        FTDocumentActivity.isAnnotationOpen = 0;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (bgTimeStart != 0 && System.currentTimeMillis() - bgTimeStart >= 5000 * 60) {
            resetPagerAdapter(currPosition);
        }
        bgTimeStart = 0;
        FTAudioPlayer.getInstance().addObserver(this, mAudioObserver);
        ObservingService.getInstance().addObserver("EdgeSwipeGestureState", mSwipeGestureObserver);
        ObservingService.getInstance().addObserver(KEY_ENABLE_UNDO, enableUndoButtonObserver);
    }

    private long bgTimeStart = 0;

    @Override
    protected void onStop() {
        super.onStop();
        bgTimeStart = System.currentTimeMillis();
//        timer.start();
        if (penRackDialog != null) {
            penRackDialog.dismissAllowingStateLoss();
        }

        FTAudioPlayer.getInstance().removeObserver(this, mAudioObserver);
        ObservingService.getInstance().removeObserver(KEY_ENABLE_UNDO, enableUndoButtonObserver);
    }

    //endregion
    @Override
    protected void onDestroy() {
        FTLog.crashlyticsLog("DocActivity: Destroying");
        if (mFTFavPenRackDialog != null) {
            mFTFavPenRackDialog = null;
        }

        if (keyboardHeightProvider != null)
            keyboardHeightProvider.close();
        FTApp.getPref().save(SystemPref.FINDER_SHOWING_BOOKMARKED_PAGES, false);
        if (drawingView != null) {
            drawingView.onDestroy();
            drawingView = null;
        }
        String odcID = null;
        if (null != currentDocument) {
            odcID = currentDocument.getDocumentUUID();
            currentDocument.stopSearching();
            currentDocument.closePdfDocuments();
            currentDocument.destroyRecognitionManager();
        }
        final String documentID = odcID;
        if (mDocumentToolbarFragment != null)
            mDocumentToolbarFragment.updateToPreviousTool();
        FTApp.CURRENT_EDIT_DOCUMENT_UIDS.remove(CURRENT_DOCUMENT_UID);
        mPenPref.save(getDocUid() + PenRackPref.PEN_TOOL_OLD, mPenPref.get(getDocUid() + PenRackPref.PEN_TOOL, -1));
        FTRenderManagerProvider.getInstance().destroyAll();
        super.onDestroy();
        if (!isSharedContextDestroyed) {
            new Handler().postDelayed(() -> {
                FTTextureManager.sharedInstance().deleteAllTextures(documentID);
                FtTempClass.getInstance().deleteTextures();
                FTThumbnailGenerator.sharedThumbnailGenerator(FTRenderMode.offScreen).releaseThumbnailRenderer();
                FTGLContextFactory.getInstance().destroySharedContext();
            }, 50);
        }
    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        if (0 != (event.getSource() & InputDevice.SOURCE_CLASS_POINTER)) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_SCROLL:
                    if (KeyEvent.metaStateHasModifiers(event.getMetaState(), KeyEvent.META_CTRL_ON)) {
                        currentPageController.setOnMouseWheelScrollEvent(event);
                    }
                    return true;
            }
        }
        return super.onGenericMotionEvent(event);
    }
    //endregion

    //region SetUp ViewPager;

    //region SetUp ToolBar
    private void setUpToolBar() {
        mDocumentToolbarFragment = new FTDocumentToolbarFragment();
        getSupportFragmentManager().
                beginTransaction().
                replace(R.id.ftdocument_toolbar_fragment_layout, mDocumentToolbarFragment).
                commitAllowingStateLoss();
        super.setUpToolbarTheme();
    }

    public void enableUndoButton() {
        mDocumentToolbarFragment.enableUndo(true);
    }

    public void initData() {
        CURRENT_DOCUMENT_UID = currentDocument.getDocumentUUID();
        mDocPref = new IndividualDocumentPref().init(CURRENT_DOCUMENT_UID);
        if (getIntent().hasExtra(getString(R.string.intent_new_page_index)))
            mDocPref.save("currentPage", getIntent().getIntExtra(getString(R.string.intent_new_page_index), 1));
        currentDocument.delegate = this;
        setDataViewpager();

        if (getIntent().hasExtra(getString(R.string.intent_is_thumbnail_refreshed))) {
            showThumbnails = getIntent().getBooleanExtra(getString(R.string.intent_is_thumbnail_refreshed), false);
        }
        if (getIntent().hasExtra(getString(R.string.intent_search_key))) {
            if (this.searchOptions == null) {
                this.searchOptions = new FTFinderSearchOptions();
            }
            this.searchOptions.searchedKeyword = getIntent().getStringExtra(getString(R.string.intent_search_key));
        }
        ObservingService.getInstance().addObserver("onSearchKeyChanged_" + currentDocument().getDocumentUUID(), onSearchKeyChanged);

        //Category Related
        setCategoriesAdapter();
        setUpFinder();
        new Handler().postDelayed(() -> {
            if (!isFinishing())
                FTApp.getPref().save(getString(R.string.intent_document_url), currentDocument.getFileURL().relativePathWRTCollection());
        }, 1000);
    }

    private void setDataViewpager() {
        SCROLL_STATE = ViewPager.SCROLL_STATE_IDLE;
        adapter = new FTDocumentPagerAdapter(getSupportFragmentManager(), this, currentDocument, this, null);
        adapter.setSearchKey(getIntent().getExtras().getString(getString(R.string.intent_search_key), ""));
        surfacePager.addOnPageChangeListener(mPageChangeListener);
        surfacePager.setAdapter(adapter);

        int savedPosition = mDocPref.get("currentPage", 1);
        currPosition = Math.min(savedPosition == 0 ? 1 : savedPosition, currentDocument.pages(this).size());
        surfacePager.addOnLayoutChangeListener((view, i, i1, i2, i3, i4, i5, i6, i7) -> {
            if (isFirstTimeLoading) {
                setCurrentPage(currPosition, false, true);
                isFirstTimeLoading = false;
            }
        });

        surfacePager.setCallbacksListener(new FTViewPager.FTPagerContainerCallback() {
            @Override
            public boolean isAllowScroll() {
                if (isAllGesturesEnabled)
                    return currentPageController.canSwipePage();
                return false;
            }

            @Override
            public FTToolBarTools currentMode() {
                return FTDocumentActivity.this.currentMode();
            }
        });

        surfacePager.setCurrentItem(currPosition, true);
        surfacePager.setOnDragListener(this);

    }
    //endregion

    //region ToolBar action callBacks
    @Override
    public void enableLassoMode(View view) {
        if (currentPageController != null)
            currentPageController.addLassoFragment(view);
    }
    //endregion

    //region ToolBar action callBacks
    @Override
    public void showThumbnails(boolean isExportMode) {
        final FTSmartDialog smartDialog = new FTSmartDialog()
                .setMode(FTSmartDialog.FTSmartDialogMode.SPINNER)
                .setMessage(getString(R.string.saving))
                .show(getSupportFragmentManager());
        ((FTThumbnailFragment) drawerFragment).isExportMode = isExportMode;
        ((FTThumbnailFragment) drawerFragment).setInitialSetUp();
        saveDocumentNow(() -> FTDocumentActivity.this.runOnUiThread(() -> {
            showThumbnails = false;
            FTDocumentActivity.this.showFinder();
            mDrawerLayout.openDrawer(GravityCompat.END);
            smartDialog.dismissAllowingStateLoss();
        }));
    }

    private void setUpFinder() {
        drawerFragment = new FTThumbnailFragment();//FTThumbnailFragment.newInstance();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.document_nd_layout, drawerFragment)
                .commitAllowingStateLoss();
    }

    private void addThumbnailInFinder(int atIndex) {
        ((FTThumbnailFragment) drawerFragment).addItem(atIndex, currentDocument.pages(getContext()).get(atIndex));
    }

    private void showFinder() {
        if (showThumbnails) return;
        FTFirebaseAnalytics.logEvent("inside_document", "document_toolbar", "finder");
        if (currPosition - 1 >= 0)
            ((FTThumbnailFragment) drawerFragment).notifyDataSetChanged(currPosition - 1);

        if (FTConstants.ENABLE_HW_RECOGNITION)
            currentDocument.handwritingRecognitionHelper(FTDocumentActivity.this.getContext()).wakeUpRecognitionHelperIfNeeded();

        showThumbnails = true;
        if (this.searchOptions == null) {
            this.searchOptions = new FTFinderSearchOptions();
        }
    }

    @Override
    public void showNotebookOptions() {
        isChangeCurrentPageTemplate = true;
        new FTNotebookOptionsPopup().show(getSupportFragmentManager());
    }

    @OnClick(R.id.document_nd_layout)
    void onNdLayoutClick(View view) {
        closePanel();
    }

    @Override
    public void closePanel() {
        showThumbnails = false;
        mDrawerLayout.closeDrawer(GravityCompat.END);
        showThumbnails = false;
    }

    public void removeHighlighters() {
        if (currentPageController != null) {
            currentPageController.removeHighlighters();
        }
    }

    @Override
    public FTNoteshelfDocument currentDocument() {
        return currentDocument;
    }

    @Override
    public int currentPageIndex() {
        return currPosition;
    }

    @Override
    public FTFinderSearchOptions searchOptions() {
        return searchOptions;
    }

    @Override
    public void share(View view) {
        new FTSelectPagesPopup().show(getSupportFragmentManager());
    }

    private void showExportFormatPopup(List<FTNoteshelfPage> pagesToExport) {
        FTFirebaseAnalytics.logEvent("inside_document", "document_toolbar", "export");
        FTExportFormatPopup shareOptionsDialog = new FTExportFormatPopup();
        shareOptionsDialog.showPDFOption(() -> {
            FTFirebaseAnalytics.logEvent("inside_document", "share_dialog", "pdf_option");

            FTSmartDialog smartDialog = new FTSmartDialog()
                    .setMessage(getString(R.string.exporting))
                    .setMode(FTSmartDialog.FTSmartDialogMode.SPINNER)
                    .show(getSupportFragmentManager());
            FTFileExporter fileExporter = new FTFileExporter();
            fileExporter.exportPages(FTDocumentActivity.this.getContext(), pagesToExport, FTConstants.PDF_EXTENSION, (file, error) -> FTDocumentActivity.this.runOnUiThread(() -> {
                smartDialog.dismissAllowingStateLoss();
                if (file != null && error == null) {
                    FTDocumentActivity.this.startActivityForResult(FTDocumentActivity.this.getShareFilteredIntent(FileUriUtils.getUriForFile(FTDocumentActivity.this, file)), FTBaseShelfActivity.PICK_EXPORTER);
                } else {
                    Toast.makeText(FTDocumentActivity.this.getContext(), R.string.export_failed, Toast.LENGTH_SHORT).show();
                }
            }));
            smartDialog.setCancellable(() -> {
                Toast.makeText(getContext(), R.string.cancelled, Toast.LENGTH_SHORT).show();
                fileExporter.cancelExporting();
            });
            smartDialog.setCancellable(fileExporter::cancelExporting);
        });
        shareOptionsDialog.showPNGOption(() -> {
            FTFirebaseAnalytics.logEvent("inside_document", "share_dialog", "png_option");

            FTSmartDialog smartDialog = new FTSmartDialog()
                    .setMessage(getString(R.string.exporting))
                    .setMode(FTSmartDialog.FTSmartDialogMode.SPINNER)
                    .show(getSupportFragmentManager());
            FTFileExporter fileExporter = new FTFileExporter();
            fileExporter.exportPages(FTDocumentActivity.this.getContext(), pagesToExport, FTConstants.PNG_EXTENSION, (file, error) -> FTDocumentActivity.this.runOnUiThread(() -> {
                smartDialog.dismissAllowingStateLoss();
                if (file != null && error == null) {
                    FTDocumentActivity.this.startActivityForResult(FTDocumentActivity.this.getShareFilteredIntent(FileUriUtils.getUriForFile(FTDocumentActivity.this, file)), FTBaseShelfActivity.PICK_EXPORTER);
                } else {
                    Toast.makeText(FTDocumentActivity.this.getContext(), R.string.export_failed, Toast.LENGTH_SHORT).show();
                }
            }));
            smartDialog.setCancellable(() -> {
                Toast.makeText(getContext(), R.string.cancelled, Toast.LENGTH_SHORT).show();
                fileExporter.cancelExporting();
            });
        });
        shareOptionsDialog.show(getSupportFragmentManager());
    }

    @Override
    public void resetAnnotationFragments(View view) {
        if (this.currentPageController == null) {
            if (adapter != null) {
                Fragment fragment = adapter.getFragmentAtIndex(currPosition);
                if (fragment instanceof FTDocumentPageFragment) {
                    currentPageController = (FTDocumentPageFragment) fragment;
                }
            }
        } else {
            currentPageController.resetAnnotationFragment(view);
        }
    }

    @Override
    public void refreshToolbarItems() {
        mDocumentToolbarFragment.updateLastSelectedToolView(true);
    }

    public void setStylusMode() {
        mDocumentToolbarFragment.updateBluetoothIcon();
    }

    //region Audio
    @Override
    public void addNewAudio() {
        if (FTPermissionManager.checkPermission(this, FTDocumentActivity.this, new String[]{Manifest.permission.RECORD_AUDIO}, REQ_PERMISSION_RECORD_AUDIO)) {
            FTAudioPlayer.getInstance().stopPlaying(getContext(), false);
            FTAudioPlayer.getInstance().stopRecording(this, true);
            currentPageController.onOutsideTouch(FTToolBarTools.AUDIO, null);
            new Handler().postDelayed(() -> currentPageController.addAudioView(null, new FTAudioRecording(), false), 100);
        }
    }

    @Override
    public void closeAudioToolbar() {
        if (mAudioToolbarFragment != null && getSupportFragmentManager().getFragments().contains(mAudioToolbarFragment)) {
            mAudioToolbarFragment.closeAudioToolbar();
        }
    }

    @Override
    public FTNoteshelfPage noteshelfPage(int pageIndex) {
        if (currentDocument != null)
            return currentDocument.pages(this).get(pageIndex);

        return null;
    }

    @Override
    public void updateToolbarMode(FTToolBarTools tool) {
        mDocumentToolbarFragment.updateToolBarMode(tool);
    }

    @Override
    public void updateToolBarModeToLastSelected() {
        mDocumentToolbarFragment.updateToolBarModeToLastSelected();
    }
    //endregion

    @Override
    public void performGotoAction(int pageIndex) {
        List<FTNoteshelfPage> pages = currentDocument.pages(getContext());
        for (FTNoteshelfPage page : pages) {
            if (currentPageController != null &&
                    currentPageController.currentPage().associatedPDFFileName.equals(page.associatedPDFFileName) &&
                    (page.associatedPageIndex - 1) == pageIndex) {
                scrollToPageAtIndex(page.pageIndex());
                break;
            }
        }
    }

    @Override
    public void performURLAction(String uri) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(uri));
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(getContext(), R.string.link_not_found, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void clearAnnotation() {
        if (currentPageController != null) {
            currentPageController.clearAnnotation();
        }
    }

    @Override
    public void updateAddViewPosition() {
        if (currentPageController != null) {
            currentPageController.updateAudioViewPosition();
        }
    }

    @Override
    public void onActivityReenter(int resultCode, Intent data) {
        super.onActivityReenter(resultCode, data);
    }

    @Override
    public FTNoteshelfPage getCurrentPage() {
        return currentPageController.currentPage();
    }

    @Override
    public FTToolBarTools currentMode() {
        return mDocumentToolbarFragment.currentMode();
    }

    @Override
    public void setClipartImageAnnotation(String clipartImagePath) {
        if (currentPageController != null) {
            currentPageController.setImageAnnotation(clipartImagePath, true);
        }
    }

    @Override
    public void setUpToolbarTheme() {
        super.setUpToolbarTheme();
        int color = Color.parseColor(FTApp.getPref().get(SystemPref.SELECTED_THEME_TOOLBAR_COLOR, FTConstants.DEFAULT_THEME_TOOLBAR_COLOR));
        mDocumentToolbarFragment.setBackgroundColor(color);
        mNDHeaderLayout.setBackgroundColor(color);
    }

    @Override
    public FTBasePref getDocPref() {
        return mDocPref;
    }

    @Override
    public void toolBarItemsClicked() {
        /*
         * Hiding unlock option of Image Annotations
         * when any tool of Toolbar is enabled*/
        if (currentPageController != null) {
            currentPageController.hideTool();
        }
        refreshFavView(false);
        setCurrentStrokeAttributes(true);
    }

    @Override
    public void onToolDoubleTapped(FTToolBarTools toolBarTool) {
        if (mDocumentToolbarFragment != null) {
            penRackDialog = FTPenRackDialog.newInstance(toolBarTool);
            penRackDialog.show(this.getSupportFragmentManager(), "FTPenRackDialog");
        }

        if (mFTFavPenRackDialog != null) {
            mFTFavPenRackDialog = null;
            if (mPenPref.get(mPrefCheckBoxKey, false)) {
                setupFavToolbar();
            } else {
                hideFavToolbar();
            }
        }
    }

    @Override
    public void lastSelectedViewInToolBar(View mView) {
        if (currentPageController != null) {
            currentPageController.lastSelectedViewInToolBar(mView);
        }
    }

    @Override
    public void shapeEnabledIDAndStatusInCurrentDoc(String currentEditDocUID, boolean status) {
        mPenPref.save(currentEditDocUID, status);
    }

    @Override
    public int getCurrentSelectedColor(FTToolBarTools requestedTool) {
        if (requestedTool == FTToolBarTools.HIGHLIGHTER)
            return currentHighlighterColor;
        else
            return currentPencolor;
    }

    @Override
    public String getDocUid() {
        return CURRENT_DOCUMENT_UID;
    }

    @Override
    public void onLoadingFinished() {
        if (docOpeningDialog != null) {
            docOpeningDialog.dismissAllowingStateLoss();
        }
    }

    @Override
    public void refreshFavView(boolean isFromFavWidToolbar) {
        if (!isFromFavWidToolbar && mFTFavPenRackDialog != null && mFTFavPenRackDialog.isAdded()) {
            mFTFavPenRackDialog.refreshListView();
        }
    }

    private void showLoadingDialog() {
        if (!docOpeningDialog.isVisible()) {
            docOpeningDialog.setMessage(getString(R.string.loading));
            docOpeningDialog.show(getSupportFragmentManager());
        }
    }

    public void onShowDateModified(boolean isChecked) {
        //wait for the hunt
    }

    @Override
    public void onStylusEnabled() {
        mDocumentToolbarFragment.updateBluetoothIcon();
    }

    @Override
    public FTShelfCollectionProvider getCollectionProvider() {
        return FTShelfCollectionProvider.getInstance();
    }

    @Override
    public void onThemeChosen(FTNTheme theme, boolean isCurrentPage,boolean isLandScapeStatus) {
        Log.d("TemplatePicker==>","Mani onThemeChosen Template Selected action FTDocumentActivity " +
                "isLandscape Status::-"+isLandScapeStatus+" theme.isLandscape::-"+theme.isLandscape+
                " currentPageController::-"+currentPageController+
                " currentPageController.currentPage()::-"+currentPageController.currentPage());
        if (currentPageController != null && currentPageController.currentPage() != null) {
            final FTSmartDialog smartDialog = new FTSmartDialog();
            if (isCurrentPage) {
                smartDialog.setMode(FTSmartDialog.FTSmartDialogMode.SPINNER)
                        .setMessage(getString(R.string.changing))
                        .show(getSupportFragmentManager());
            }
            final FTNoteshelfPage currentPage = currentPageController.currentPage();
            Log.d("TemplatePicker==>","PAPAERTHEME SAVED RECENTLY Sample Notebook FTDcoumentAcitivity theme.ftThemeType::-"+(theme.ftThemeType == FTNThemeCategory.FTThemeType.PAPER));
            Log.d("TemplatePicker==>","PAPAERTHEME SAVED RECENTLY Sample Notebook FTDcoumentAcitivity theme::-"+theme);
            if (theme == null ||
                    theme.themeThumbnail(getContext()) == null)  {
                theme = new FTNThemeCategory(getContext(),
                        "Simple", FTNThemeCategory.FTThemeType.PAPER).getPaperThemeForPackName(FTConstants.DEFAULT_PAPER_THEME_NAME);
            }

            if ((theme.ftThemeType == FTNThemeCategory.FTThemeType.PAPER)) {
                theme       = paperThemeItem(theme);

                /*FTSelectedDeviceInfo ftSelectedDeviceInfo = FTSelectedDeviceInfo.selectedDeviceInfo();

                ArrayList<FTNTheme> ftRecentPapersThemesList = FTTemplateUtil.getInstance().getRecentPapersThemesList();
                if (ftRecentPapersThemesList != null &&
                        !ftRecentPapersThemesList.isEmpty()) {
                    ArrayList<String> recentPapersThumbnailPathURLsList = FTTemplateUtil.getInstance().getRecentPapersThumbnailPathURLsList();
                    Log.d("TemplatePicker==>","FTTemplateUtil FTDcoumentAcitivity ftRecentPapersThemesList packName::-"+
                            recentPapersThumbnailPathURLsList +" packName::-"+theme.thumbnailURLPath);
                    if (!recentPapersThumbnailPathURLsList.contains(theme.thumbnailURLPath)) {
                        if (theme.thumbnailURLPath != null) {
                            FTTemplateUtil.getInstance().saveRecentPapersThemesList(theme,"FTDcoumentAcitivity already in recents some papers exists");
                        }
                    }
                } else {
                    FTTemplateUtil.getInstance().saveRecentPapersThemesList(theme,"FTDcoumentAcitivity when No recents case");
                }*/
            }

            final FTNTheme tempPaperTheme = theme;
            /* FTTemplateUtil.getInstance().saveRecentPaperTheme(theme,"FTDcoumentAcitivity already in recents some papers exists");
            Log.d("TemplatePicker==>"," FTDocumentActivity RECENT_PAPER_THEME_URL::-"+theme.themeFileURL.getPath());
            FTSelectedDeviceInfo ftSelectedDeviceInfo = FTSelectedDeviceInfo.selectedDeviceInfo();*/
            tempPaperTheme.template(getContext(), new FTAutoTemplateGenerationCallback() {
                @Override
                public void onGenerated(@Nullable FTDocumentInputInfo documentInfo, @Nullable Error generationError) {

                    if (isCurrentPage) {
                        documentInfo.insertAt = currPosition - 1;
                    } else {
                        documentInfo.insertAt = currPosition;
                    }

                    if (currPosition == adapter.getCount() - 1) {
                        documentInfo.insertAt = documentInfo.insertAt - 1;
                    }

                    currentDocument.insertFileFromInfo(getContext(), documentInfo, isCurrentPage, new CompletionBlock() {
                        @Override
                        public void didFinishWithStatus(Boolean success, Error error) {
                            runOnUiThread(() -> {
                                if (success) {
                                    if (FTConstants.ENABLE_VISION_RECOGNITION) {
                                        currentDocument.visionRecognitionHelper(getContext()).setRecognitionComplete(false);
                                        currentDocument.visionRecognitionHelper(getContext()).wakeUpRecognitionHelperIfNeeded();
                                    }
                                    if (isCurrentPage) {
                                        currentPageController.reloadInRect(FTGeometryUtils.scaleRect(currentPage.getPageRect(), currentPageController.getScale()));
                                        //@ToDo Removing the current fragment to reload but need to refactor this
                                        FTDocumentPageFragment pagerFragment = (FTDocumentPageFragment) adapter.getFragmentAtIndex(currPosition);
                                        getSupportFragmentManager().beginTransaction().remove(pagerFragment).commit();

                                        resetPagerAdapter(currPosition);
                                        smartDialog.dismissAllowingStateLoss();
                                        if (searchOptions != null && !searchOptions.searchPageResults.isEmpty()) {
                                            searchOptions.searchPageResults.clear();
                                        }
                                    } else {
                                        saveDocumentNow(() -> addNewPageAtIndex(documentInfo.insertAt + 1));
                                    }

                                    /*if (tempPaperTheme instanceof FTNPaperTheme) {
                                        Log.d("TemplatePicker==>","RECENT_PAPER_THEME_NAME FTDocumentActivity insertFileFromInfo paperTheme.themeName::-"+tempPaperTheme.themeName);
                                        Gson gson = new Gson();
                                        String json = gson.toJson(tempPaperTheme);
                                        Log.d("TemplatePicker==>","RECENTLY_SELECTED_THUMBNAIL_URL_PATH FTDocumentActivity onThemeChosen " +
                                                tempPaperTheme.thumbnailURLPath);
                                    } else if (tempPaperTheme instanceof FTNCoverTheme) {
                                        FTApp.getPref().save(SystemPref.RECENT_COVER_THEME_NAME, tempPaperTheme.packName);
                                    }*/

                                    if (tempPaperTheme instanceof FTNCoverTheme) {
                                        FTApp.getPref().save(SystemPref.RECENT_COVER_THEME_NAME, tempPaperTheme.packName);
                                    }
                                    getCurrentPage().setPageDirty(true);
                                } else {
                                    FTLog.crashlyticsLog("DocAct: Insert failed from picked theme");
                                    Log.i(this.getClass().getName(), error.getMessage());
                                }
                            });
                        }
                    });
                }
            });
        }
    }

    private FTNTheme paperThemeItem(FTNTheme paperTheme) {
        if ((paperTheme.ftThemeType == FTNThemeCategory.FTThemeType.PAPER)) {
            ArrayList<RecentsInfoModel> recentsInfoList =  FTTemplateUtil.getInstance().getRecentPapersDummy();
            boolean recentsInfoListIsNullOrEmpty = FTTemplateUtil.isNullOrEmpty(recentsInfoList);
            Log.d("TemplatePicker==>","FTTemplateUtil FTDocumentActivity Recents List Existence status " +recentsInfoListIsNullOrEmpty);

            if (!recentsInfoListIsNullOrEmpty) {
                for (int i=0;i<recentsInfoList.size();i++) {
                    Log.d("TemplatePicker==>","FTTemplateUtil FTDocumentActivity get_packName " +recentsInfoList.get(i).get_packName()
                            +"get_thumbnailURLPath:: "+recentsInfoList.get(i).get_thumbnailURLPath());
                }

                boolean _themeAlreadyExists = recentsInfoList.stream()
                        .anyMatch(p -> p.get_thumbnailURLPath().equals(paperTheme.thumbnailURLPath));
                Log.d("TemplatePicker==>","FTTemplateUtil FTDocumentActivity Recents List Exists" +
                        " thumbnailURLPath::-"+paperTheme.thumbnailURLPath +
                        " width:: "+paperTheme.width+
                        " height:: "+paperTheme.height+
                        " _themeAlreadyExists:: "+_themeAlreadyExists);
                if (!_themeAlreadyExists) {
                    FTTemplateUtil.getInstance().saveRecentPapersDummy(paperTheme);
                }
            } else {
                Log.d("TemplatePicker==>","FTTemplateUtil FTDocumentActivity Recents List Fresh" +
                        " thumbnailURLPath::-"+paperTheme.thumbnailURLPath +
                        " width:: "+paperTheme.width+
                        " height:: "+paperTheme.height+" bitmap:: "+paperTheme.bitmap);
                FTTemplateUtil.getInstance().saveRecentPapersDummy(paperTheme);
            }
        }
        return paperTheme;
    }

    @Override
    public void addCustomTheme(FTNTheme theme) {
        if (FTPermissionManager.checkPermission(this, this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_ADD_PAPER_THEME)) {
            importDocumentAndImage(REQUEST_CODE_ADD_PAPER_THEME);
        }
    }

    @Override
    public void onClose() {

    }

    @Override
    public boolean isCurrentTheme() {
        return isChangeCurrentPageTemplate;
    }
    //endregion

    //region Thumbnails action callBacks
    @Override
    public void reloadDocumentData() {
        isThumbnailRefreshed = true;
        newThumbnailIndex = Math.min(currPosition, currentDocument.pages(getContext()).size());
        currentPageController.removeWritingView();

        //This is to remove the fragment memory to avoid leak
        Fragment fragment = adapter.getFragmentAtIndex(currPosition);
//        adapter.removeFragmentAtIndex(currPosition);
        getSupportFragmentManager().beginTransaction().remove(fragment).commitAllowingStateLoss();

        resetPagerAdapter(newThumbnailIndex);
    }

    @Override
    public void scrollToPageAtIndex(int indexToShow) {
        newThumbnailIndex = indexToShow;
        showThumbnails = false;
        if (currPosition != indexToShow + 1) {
            addNewPageAtIndex(indexToShow + 1, -1);
        }
    }
    //endregion

    //region User PermissionsResult
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PICK_FROM_GALLERY:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    pickFromGallery();
                } else {
                    Toast.makeText(this, R.string.gallery_access_error, Toast.LENGTH_LONG).show();
                }
                break;
            case PICK_FROM_CAMERA:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    pickFromCamera();
                } else {
                    Toast.makeText(this, getString(R.string.camera_access_error), Toast.LENGTH_LONG).show();
                }
                break;
            case IMPORT_DOCUMENT:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    importDocument();
                } else {
                    Toast.makeText(this, getString(R.string.document_access_error), Toast.LENGTH_LONG).show();
                }
                break;
            case PAGE_FROM_PHOTO:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    addPageFromPhoto();
                } else {
                    Toast.makeText(this, getString(R.string.document_access_error), Toast.LENGTH_LONG).show();
                }
                break;
            case REQUEST_CODE_ADD_PAPER_THEME:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    importDocumentAndImage(REQUEST_CODE_ADD_PAPER_THEME);
                } else {
                    Toast.makeText(this, R.string.document_access_error, Toast.LENGTH_LONG).show();
                }
                break;

            case REQ_PERMISSION_RECORD_AUDIO:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    addNewAudio();
                } else {
                    Toast.makeText(this, getString(R.string.record_audio_permission_error), Toast.LENGTH_LONG).show();
                }
                break;
            case IMPORT_PAGE_PHOTO:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    addNewPageFromPhoto();
                } else {
                    Toast.makeText(this, R.string.document_access_error, Toast.LENGTH_LONG).show();
                }
                break;
        }
    }
    //endregion

    //region Other Activity Data Results
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FROM_GALLERY) {
            if (resultCode == Activity.RESULT_OK) {
                if (data != null && data.getData() != null) {
                    if (currentPageController != null)
                        currentPageController.setImageAnnotation(data.getData().toString(), false);
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(this, R.string.cancelled, Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == PICK_FROM_CAMERA && resultCode == RESULT_OK) {
            if (currentPageController != null)
                currentPageController.setImageAnnotation(fileProvider.toString(), false);
        } else if ((requestCode == IMPORT_PAGE_PHOTO || requestCode == IMPORT_DOCUMENT) && resultCode == RESULT_OK) {
            importDocumentFromDevice(data);
        } else if (requestCode == IMAGE_ACTIVITY) {
            if (currentPageController != null)
                currentPageController.onActivityResult(requestCode, resultCode, data);
        } else if (requestCode == SCAN_DOCUMENT && data != null) {
            importDocumentFromDevice(new Intent().setData(FileUriUtils.getUri(new File(data.getStringExtra(getString(R.string.intent_scanned_doc_path))))));
        }
    }

    private Error importError = null;

    private void importDocumentFromDevice(Intent intent) {
        FTSmartDialog smartDialog = new FTSmartDialog();
        smartDialog.setMode(FTSmartDialog.FTSmartDialogMode.SPINNER);
        smartDialog.setMessage(getString(R.string.importing));
        smartDialog.show(getSupportFragmentManager());
        FTFileImporter fileImporter = new FTFileImporter();
        fileImporter.startImporting(getContext(), intent, false, false, new FTFileImporter.FileImporterCallbacks() {

            @Override
            public void onEachFileImported(FTUrl importedFileUrl, Error importError) {
                runOnUiThread(() -> {
                    if (importedFileUrl != null && importError == null) {
                        final FTDocumentInputInfo info = new FTDocumentInputInfo();
                        info.isTemplate = false;
                        info.inputFileURL = importedFileUrl;
                        info.insertAt = currPosition;
                        if (currPosition == adapter.getCount() - 1) {
                            info.insertAt -= 1;
                        }
                        currentDocument.insertFileFromInfo(FTDocumentActivity.this.getContext(), info, false, (success, error) -> {
                            if (error != null) {
                                Toast.makeText(FTDocumentActivity.this.getContext(), R.string.unexpected_error_occurred_please_try_again, Toast.LENGTH_SHORT).show();
                                fileImporter.onUIUpdated();
                            } else {
                                saveDocumentNow(fileImporter::onUIUpdated);
                            }
                            FTDocumentActivity.this.importError = error;
                        });
                    } else {
                        FTDocumentActivity.this.importError = importError;
                        fileImporter.onUIUpdated();
                    }
                });
            }

            @Override
            public void onAllFilesImported(boolean isCancelled) {
                runOnUiThread(() -> {
                    smartDialog.dismissAllowingStateLoss();
                    if (isCancelled) {
                        Toast.makeText(FTDocumentActivity.this.getContext(), R.string.cancelled, Toast.LENGTH_LONG).show();
                    } else {
                        if (FTDocumentActivity.this.importError == null) {
                            if (currentPageIndex() == adapter.getCount() - 1) {
                                showLoadingDialog();
                                resetPagerAdapter(currPosition);
                            } else {
                                ((FTThumbnailFragment) drawerFragment).updateAll(currentDocument.pages(getContext()));
                                addNewPageAtIndex(currPosition + 1);
                            }
                        }
                    }
                    if (FTConstants.ENABLE_VISION_RECOGNITION) {
                        currentDocument.visionRecognitionHelper(FTDocumentActivity.this.getContext()).setRecognitionComplete(false);
                        currentDocument.visionRecognitionHelper(FTDocumentActivity.this.getContext()).wakeUpRecognitionHelperIfNeeded();
                    }
                });
            }
        });
        smartDialog.setCancellable(fileImporter::cancelImporting);
    }

    @Override
    public void clearPageAnnotations() {
        if (currentPageController != null) {
            currentPageController.clearPageAnnotations();
        }
    }

    @Override
    public void addNewPage() {
        int newIndex = currPosition;
        if (currPosition == adapter.getCount() - 1) {
            newIndex = currPosition - 1;
        }
        currentPageController.resetAnnotationFragment(null);
        if (currentDocument != null) {
            int finalNewIndex = newIndex;
            currentDocument.insertPageAtIndex(getContext(), newIndex, true, (insertedPage, error) -> {
                insertedPage.setPageDirty(true);
                saveDocumentNow(() -> runOnUiThread(() -> addNewPageAtIndex(finalNewIndex + 1)));
            });
        }
    }

    @Override
    public void scanDocument() {
        startActivityForResult(new Intent(FTDocumentActivity.this, ScanActivity.class), SCAN_DOCUMENT);
    }

    @Override
    public void addPageFromPhoto() {
        if (FTPermissionManager.checkPermission(this, this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PAGE_FROM_PHOTO)) {
            Intent intent = new Intent();
            String[] mimeTypes = {getString(R.string.mime_type_image)};
            intent.setType(getString(R.string.mime_type_image));
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivityForResult(Intent.createChooser(intent, getString(R.string.intent_select_pic_pdf)), IMPORT_DOCUMENT);
        }
    }

    boolean isChangeCurrentPageTemplate = false;

    @Override
    public void addNewPageFromTemplate() {
        isChangeCurrentPageTemplate = false;
        //FTChoosePaperTemplate.newInstance(FTNThemeCategory.FTThemeType.PAPER).show(getSupportFragmentManager(), FTChooseCoverPaperDialog.class.getName());

        FTTemplatepickerInputInfo _ftTemplatepickerInputInfo = new FTTemplatepickerInputInfo();
        _ftTemplatepickerInputInfo.set_baseShelfActivity(null);
        _ftTemplatepickerInputInfo.set_ftTemplateOpenMode(FTTemplateMode.InsertPage);
        _ftTemplatepickerInputInfo.set_ftThemeType(FTNThemeCategory.FTThemeType.PAPER);
        _ftTemplatepickerInputInfo.set_notebookTitle(null);
        FTChoosePaperTemplate.newInstance1(_ftTemplatepickerInputInfo).show(getSupportFragmentManager(), FTChooseCoverPaperDialog.class.getName());

    }

    @Override
    public void addNewPageFromPhoto() {
        pickFromGallery(IMPORT_PAGE_PHOTO);
    }

    @Override
    public void undoLastChange(boolean isLongClick) {
        UndoManager undoManager = currentPageController.currentUndoManager();
        if (undoManager == null)
            return;
        if (isLongClick || (!undoManager.canUndo() && undoManager.canRedo())) {
            FTUndoRedoDialog.newInstance(undoManager)
                    .show(getSupportFragmentManager(), "FTUndoRedoDialog");
        } else {
            undoManager.undo();
        }
    }

    public UndoManager getUndoManager() {
        return currentPageController.currentUndoManager();
    }
    //endregion

    //region User functions
    private Context getContext() {
        return this;
    }

    private void addNewPageAtIndex(int index) {
        showLoadingDialog();
        if (currPosition == index) {
            resetPagerAdapter(index);
        } else {
            this.currPosition = index;
            mDocPref.save("currentPage", currPosition);
            addThumbnailInFinder(currPosition - 1);
            adapter.addItem(index);
            adapter.removeFragmentAtIndex(index);
            adapter.notifyDataSetChanged();
            surfacePager.setCurrentItem(index, true);
        }
    }

    private void addNewPageAtIndex(final int currentPage, final int newIndex) {
        if (newIndex >= 0) {
            this.currPosition = newIndex;
            mDocPref.save("currentPage", currPosition);
            ((FTThumbnailFragment) drawerFragment).notifyDataSetChanged(currPosition);
            adapter.notifyDataSetChanged();
        } else if (currentPage != -1) {
            mDocPref.save("currentPage", currentPage);
            this.currPosition = currentPage;
        }
        surfacePager.setCurrentItem(currPosition, true);
    }

    private void resetPagerAdapter(int newPosition) {
        if (adapter != null && adapter.getFragmentAtIndex(currPosition) instanceof FTDocumentPageFragment) {
            FTDocumentPageFragment pagerFragment = (FTDocumentPageFragment) adapter.getFragmentAtIndex(currPosition);
            pagerFragment.onPause();
        }
        currPosition = newPosition;
        mDocPref.save("currentPage", currPosition);
        adapter = new FTDocumentPagerAdapter(getSupportFragmentManager(), this, currentDocument, this, null);
        surfacePager.setAdapter(adapter);
        surfacePager.setCurrentItem(currPosition, true);

        Fragment fragment = adapter.getFragmentAtIndex(currPosition);
        if (fragment instanceof FTDocumentPageFragment) {
            setCurrentPageController((FTDocumentPageFragment) fragment);
        }

        ((FTThumbnailFragment) drawerFragment).updateAll(currentDocument.pages(getContext()));
    }

    private void saveDocumentNow(final OnDocumentSavedListener onDocumentSavedListener) {
        if (null != currentDocument) {
            if (currentDocument.hasAnyUnsavedChanges(getContext())) {
                currentDocument.saveNoteshelfDocument(getContext(), (success, error) -> {
                    if (onDocumentSavedListener != null)
                        onDocumentSavedListener.onSaved();
                });
            } else {
                if (onDocumentSavedListener != null)
                    onDocumentSavedListener.onSaved();
            }
        }
    }

    @Override
    public void onKeyboardHeightChanged(int height, int orientation) {
        if (adapter == null)
            return;
        if (this.currentPageController != null) {
            Fragment fragment = adapter.getFragmentAtIndex(currPosition);
            if (fragment instanceof FTDocumentPageFragment) {
                currentPageController = (FTDocumentPageFragment) fragment;
                currentPageController.onKeyboardHeightChanged(height);
                ObservingService.getInstance().postNotification("onKeyboardHeightChanged", height);
            }
        }
    }

    @Override
    public int currentPageDisplayed() {
        return currPosition;
    }

    @Override
    public int getCurrentItemPosition() {
        return surfacePager.getCurrentItem();
    }

    @Override
    public void importDocument() {
        if (FTPermissionManager.checkPermission(this, this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, IMPORT_DOCUMENT)) {
            Intent intent = new Intent();
            String[] mimeTypes = {getString(R.string.mime_type_application_pdf), getString(R.string.mime_type_application_text)};
            intent.setType(getString(R.string.mime_type_application_pdf));
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            startActivityForResult(Intent.createChooser(intent, getString(R.string.intent_select_pic_pdf)), IMPORT_DOCUMENT);
        }
    }

    public void importDocumentAndImage(int requestCode) {
        if (FTPermissionManager.checkPermission(this, this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, requestCode)) {
            Intent intent = new Intent();
            intent.setType(getString(R.string.mime_type_all));
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivityForResult(Intent.createChooser(intent, getString(R.string.intent_select_pic_pdf)), requestCode);
        }
    }

    @Override
    public void pickFromGallery() {
        pickFromGallery(PICK_FROM_GALLERY);
    }

    private void pickFromGallery(int requestCode) {
        if (FTPermissionManager.checkPermission(this, this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, requestCode)) {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);//
            intent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), requestCode);
        }
    }

    @Override
    public void pickFromCamera() {
        if (FTPermissionManager.checkPermission(this, this, new String[]{Manifest.permission.CAMERA}, PICK_FROM_CAMERA)) {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                File photoFile = FTFileManagerUtil.getPhotoFileUri(this, "IMAGE");
                fileProvider = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider);
                startActivityForResult(takePictureIntent, PICK_FROM_CAMERA);
            } else {
                Toast.makeText(this, R.string.camera_error, Toast.LENGTH_LONG).show();
            }
        }
    }
    //endregion
    //endregion

    //region system Override functions
    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.END)) {
            mDrawerLayout.closeDrawers();
            showThumbnails = false;
            return;
        }
        FTLog.crashlyticsLog("Doc: Close");
        if (FTApp.getPref().get("lastGroupDocumentPos", -1) > -1) {
            FTApp.getPref().save("lastGroupDocumentPos", -1);
        } else {
            FTApp.getPref().save("lastDocumentPos", -1);
        }
        final FTSmartDialog smartDialog = new FTSmartDialog();
        if (!isFinishing()) {
            smartDialog.setMode(FTSmartDialog.FTSmartDialogMode.SPINNER)
                    .setMessage(getString(R.string.saving))
                    .show(getSupportFragmentManager());
        }
        closeAudioToolbar();
        FTAudioPlayer.getInstance().removeAudioFocus();
        FTApp.getPref().save("searchedKeyword", "");
        if (getCurrentItemPosition() == 0 && getCurrentPage().isPageDirty()) {
            getCurrentPage().thumbnail().thumbnailImage(FTApp.getInstance().getCurActCtx());
        }
        saveDocumentNow(() -> {
            if (!isFinishing() && smartDialog != null) {
                if (FTConstants.ENABLE_HW_RECOGNITION)
                    currentDocument.handwritingRecognitionHelper(FTDocumentActivity.this.getContext()).wakeUpRecognitionHelperIfNeeded();
                smartDialog.dismissAllowingStateLoss();
                FTThumbnailGenerator.sharedThumbnailGenerator(FTRenderMode.offScreen).cancelAllThumbnailGeneration();
                FTThumbnailGenerator.sharedThumbnailGenerator(FTRenderMode.offScreen).releaseThumbnailRenderer();
                Intent intent = new Intent();
                if (updatedCollectionUrl != null)
                    intent.putExtra("updatedCollectionUrl", updatedCollectionUrl.getPath());
                setResult(DOCUMENT_ACTIVITY, intent);
                FTApp.getPref().saveDocumentUrl("");
                finish();
            }
        });
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        FTLog.saveLog("Document Page Configuration Changed " + newConfig.orientation);
        if (null != currentPageController) {
            currentPageController.resetAnnotationFragment(null);
            currentPageController.onOutsideTouch(FTToolBarTools.AUDIO, null);
        }
//        if ((currPosition <= 1 || currPosition >= adapter.getCount() - 2) && null != currentDocument) {
//            resetPagerAdapter(currPosition);
//        }

        if (mDocumentToolbarFragment != null) {
            mDocumentToolbarFragment.initViews(R.layout.document_custom_toolbar);
        }
        mDocumentToolbarFragment.enableUndo(false);
    }

    //endregion

    private void setCurrentPage(int index, boolean animate, boolean forcibly) {
        int currentIndex = surfacePager.getCurrentItem();
        if (currentIndex != index || forcibly) {
            surfacePager.setCurrentItem(index, animate, forcibly);
        }
    }

    public FTDrawingView getDrawingView(int size) {
        if (drawingView == null) {
            drawingView = new FTDrawingView(this);
            FrameLayout.LayoutParams paramsNew = new FrameLayout.LayoutParams(size, size);
            drawingView.setLayoutParams(paramsNew);
        }
        setCurrentStrokeAttributes(true);
        return drawingView;
    }

    public void removeDrawingView() {
        if (drawingView != null) {
            FrameLayout parent = (FrameLayout) drawingView.getParent();
            if (null != parent)
                parent.removeAllViews();
        }
    }

    private FTStrokeAttributes strokeAttributes;

    private void setCurrentStrokeAttributes(boolean isAttributesChanged) {
        if (drawingView == null)
            return;
        int currentPenType = mPenPref.get(CURRENT_DOCUMENT_UID + PenRackPref.PEN_TOOL, FTToolBarTools.PEN.toInt());
        if (currentPenType == FTToolBarTools.PEN.toInt()) {
            if (isAttributesChanged) {
                currentPencolor = mPenPref.get("selectedPenColor", Color.parseColor(PenRackPref.DEFAULT_PEN_COLOR));
                currentPenSize = mPenPref.get("selectedPenSize", PenRackPref.DEFAULT_SIZE);
            }

            if (strokeAttributes == null) {
                strokeAttributes = new FTStrokeAttributes(FTPenType.valueOf(mPenPref.get("selectedPen", FTPenType.pen.toString())), currentPencolor, currentPenSize);
            } else {
                strokeAttributes.penType = FTPenType.valueOf(mPenPref.get("selectedPen", FTPenType.pen.toString()));
                strokeAttributes.strokeColor = currentPencolor;
                strokeAttributes.strokeSize = currentPenSize;
            }

            drawingView.setFtStrokeAttributes(strokeAttributes);
        } else if (currentPenType == FTToolBarTools.HIGHLIGHTER.toInt()) {
            if (isAttributesChanged) {
                currentHighlighterColor = mPenPref.get("selectedPenColor_h", Color.parseColor(PenRackPref.DEFAULT_HIGHLIGHTER_COLOR));
                currentHighlighterSize = mPenPref.get("selectedPenSize_h", PenRackPref.DEFAULT_SIZE);
            }

            if (strokeAttributes == null) {
                strokeAttributes = new FTStrokeAttributes(FTPenType.valueOf(mPenPref.get("selectedPen_h", FTPenType.highlighter.toString())), currentHighlighterColor, currentHighlighterSize);
            } else {
                strokeAttributes.penType = FTPenType.valueOf(mPenPref.get("selectedPen_h", FTPenType.highlighter.toString()));
                strokeAttributes.strokeColor = currentHighlighterColor;
                strokeAttributes.strokeSize = currentHighlighterSize;
            }

            drawingView.setFtStrokeAttributes(strokeAttributes);
        }
    }

    private void initCurrentStrokeAttributes() {
        currentPencolor = mPenPref.get("selectedPenColor", Color.parseColor(PenRackPref.DEFAULT_PEN_COLOR));
        currentPenSize = mPenPref.get("selectedPenSize", PenRackPref.DEFAULT_SIZE);
        currentHighlighterColor = mPenPref.get("selectedPenColor_h", Color.parseColor(PenRackPref.DEFAULT_HIGHLIGHTER_COLOR));
        currentHighlighterSize = mPenPref.get("selectedPenSize_h", PenRackPref.DEFAULT_SIZE);
    }

    @Override
    public boolean onDrag(View view, DragEvent dragEvent) {
        switch (dragEvent.getAction()) {
            case DragEvent.ACTION_DRAG_ENDED:
            case DragEvent.ACTION_DRAG_EXITED:
            case DragEvent.ACTION_DRAG_ENTERED:
            case DragEvent.ACTION_DRAG_LOCATION:
            case DragEvent.ACTION_DRAG_STARTED: {
                return true;
            }
            case DragEvent.ACTION_DROP: {
                ActivityCompat.requestDragAndDropPermissions(this, dragEvent);
                handleDropEvent(dragEvent);
                return true;
            }
            default: {
                return false;
            }
        }
    }

    private void handleDropEvent(DragEvent dragEvent) {
        if (dragEvent.getClipData() != null) {
            ClipData.Item item = dragEvent.getClipData().getItemAt(0);
            Uri uri = item.getUri();
            if (uri != null) {
                String mimeType = FileUriUtils.getMimeType(getContext(), uri);
                if (mimeType != null && mimeType.toLowerCase().contains("image")) {
                    if (currentPageController != null)
                        currentPageController.setImageAnnotation(uri.toString(), false);
                }
            } else if (item.getText() != null) {
                if (currentPageController != null)
                    currentPageController.addInputTextView(item.getText().toString(), dragEvent.getX(), dragEvent.getY());
            }
        }
    }

    @NotNull
    @Override
    public RectF getPageRect(int positon) {
        return currentDocument.pages(getContext()).get(positon).getPageRect();
    }

    @Override
    public void importPdfDocument() {
        if (FTPermissionManager.checkPermission(this, this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, IMPORT_DOCUMENT)) {
            Intent intent = new Intent();
            String[] mimeTypes = {getString(R.string.mime_type_application_pdf), getString(R.string.mime_type_application_text)};
            intent.setType(getString(R.string.mime_type_application_pdf));
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivityForResult(Intent.createChooser(intent, getString(R.string.intent_select_pic_pdf)), IMPORT_DOCUMENT);
        }
    }

    @Override
    public void onClearPageSelected() {
        if (mDocumentToolbarFragment != null) {
            clearPageAnnotations();
            //When clear page is selected, we need to move back to the previous selected tool
            mDocumentToolbarFragment.onEraserEnded();
        }
    }

    @Override
    public void penOrHighlighterInfoChanged(PenOrHighlighterInfo mPenOrHighlighterInfo) {

        /*
         * Saving into Shared preferences when ever there is change in PenOrHighlighterInfo
         * */
        sharedPrefsInfo(mPenOrHighlighterInfo.getPenRackType());

        /*
         * Save pen or highlighter info only when pen type is not eraser
         * */
        if (mPenOrHighlighterInfo.getPenRackType() != ERASER) {
//            mPenPref.save(mPrefCurrentSelection, mPenOrHighlighterInfo.getPenType());
            mPenPref.save(mPrefPenKey, mPenOrHighlighterInfo.getPenType());
            mPenPref.save(mPrefColorKey, mPenOrHighlighterInfo.getPenColor());
            mPenPref.save(mPrefSizeKey, mPenOrHighlighterInfo.getPenSize());
        } else if (mPenOrHighlighterInfo.getPenRackType() == ERASER) {
            mPenPref.save(mPrefSizeKey, mPenOrHighlighterInfo.getPenSize());
        }

        /*
         * Saving pen/highlghter/eraser IDs correspondingly changed
         * */
        if (mPenOrHighlighterInfo.getPenRackType() == PEN) {
            mPenPref.save(CURRENT_DOCUMENT_UID + PenRackPref.PEN_TOOL, FTToolBarTools.PEN.toInt());
        } else if (mPenOrHighlighterInfo.getPenRackType() == HIGHLIGHTER) {
            mPenPref.save(CURRENT_DOCUMENT_UID + PenRackPref.PEN_TOOL, FTToolBarTools.HIGHLIGHTER.toInt());
        } else if (mPenOrHighlighterInfo.getPenRackType() == ERASER) {
            mPenPref.save(CURRENT_DOCUMENT_UID + PenRackPref.PEN_TOOL, FTToolBarTools.ERASER.toInt());
        }

        /*
         * refresh list view of favorite widget tool bar
         * */
        if (mFTFavPenRackDialog != null) {
            mFTFavPenRackDialog = null;
            setupFavToolbar();
        }

        if (mDocumentToolbarFragment != null) {
            mDocumentToolbarFragment.updateLastSelectedToolView(true);
        }
        setCurrentStrokeAttributes(true);

    }

    private int getPenrackGroupType(FTPenType penType) {
        if (penType == FTPenType.pen || penType == FTPenType.pilotPen || penType == FTPenType.caligraphy) {
            return PEN;
        } else if (penType == FTPenType.highlighter || penType == FTPenType.flatHighlighter) {
            return HIGHLIGHTER;
        }

        return PEN;
    }

    @Override
    public void favAdded(@NotNull Favorite favorite) {
        if (mFTFavPenRackDialog != null && mFTFavPenRackDialog.isAdded()) {
            mFTFavPenRackDialog.addFavourite(favorite);
        }
    }

    @Override
    public void favRemoved(@NotNull Favorite favorite) {
        if (mFTFavPenRackDialog != null && mFTFavPenRackDialog.isAdded()) {
            mFTFavPenRackDialog.removeFavourite(favorite);
        }
    }

    @Override
    public void enableFavWidgetToolbar(boolean enableFavWidgetToolbar) {
        if (enableFavWidgetToolbar) {
            setupFavToolbar();
        } else {
            hideFavToolbar();
        }
    }

    private void sharedPrefsInfo(int penRackType) {

        if (penRackType == PEN) {
            mPrefPenKey = "selectedPen";
            mPrefSizeKey = "selectedPenSize";
            mPrefColorKey = "selectedPenColor";
        } else if (penRackType == HIGHLIGHTER) {
            mPrefPenKey = "selectedPen_h";
            mPrefSizeKey = "selectedPenSize_h";
            mPrefColorKey = "selectedPenColor_h";
        } else {
            mPrefSizeKey = PenRackPref.SELECTED_ERASER_SIZE;
        }

    }

    @Override
    public void penOrHighlighterClickedInFavWidget(PenOrHighlighterInfo mPenOrHighlighterInfo, boolean isFromFavWidToolbar) {

        /*
         * Saving into Shared preferences when ever there is change in PenOrHighlighterInfo
         * */
        sharedPrefsInfo(mPenOrHighlighterInfo.getPenRackType());

        /*
         * Save pen or highlighter info only when pen type is not eraser
         * */
        if (mPenOrHighlighterInfo.getPenRackType() != ERASER) {
//            mPenPref.save(mPrefCurrentSelection, mPenOrHighlighterInfo.getPenType());
            mPenPref.save(mPrefPenKey, mPenOrHighlighterInfo.getPenType());
            mPenPref.save(mPrefColorKey, mPenOrHighlighterInfo.getPenColor());
            mPenPref.save(mPrefSizeKey, mPenOrHighlighterInfo.getPenSize());
        }

        if (mDocumentToolbarFragment != null) {
            mDocumentToolbarFragment.updateLastSelectedToolView(false);
        }
        /*
         * Saving pen/highlghter/eraser IDs correspondingly changed
         * */
        if (mPenOrHighlighterInfo.getPenRackType() == PEN) {
            mPenPref.save(CURRENT_DOCUMENT_UID + PenRackPref.PEN_TOOL, FTToolBarTools.PEN.toInt());
        } else if (mPenOrHighlighterInfo.getPenRackType() == HIGHLIGHTER) {
            mPenPref.save(CURRENT_DOCUMENT_UID + PenRackPref.PEN_TOOL, FTToolBarTools.HIGHLIGHTER.toInt());
        } else if (mPenOrHighlighterInfo.getPenRackType() == ERASER) {
            mPenPref.save(CURRENT_DOCUMENT_UID + PenRackPref.PEN_TOOL, FTToolBarTools.ERASER.toInt());
        }

        if (mDocumentToolbarFragment != null) {
            mDocumentToolbarFragment.updateLastSelectedToolView(true);
        }

        if (isFromFavWidToolbar) {
            resetAnnotationFragments(null);
        }

        setCurrentStrokeAttributes(true);
    }

    @Override
    public void closeFavToolbarWidget() {
        if (mFTFavPenRackDialog != null) {
            hideFavToolbar();
            mPenPref.save(mPrefCheckBoxKey, false);
        }
    }

    @Override
    public void onSelectedPagesToShare(FTSelectPagesPopup.SelectPageOption option) {
        ArrayList<FTNoteshelfPage> pagesToExport = new ArrayList<>();
        switch (option) {
            case CURRENT_PAGE:
                if (currentPageController != null) {
                    pagesToExport.add(currentPageController.currentPage());
                }
                break;
            case ALL_PAGES:
                if (currentDocument != null)
                    pagesToExport.addAll(currentDocument.pages(getContext()));
                break;
            case SELECT_PAGES:
                showThumbnails(true);
                if (drawerFragment instanceof FTThumbnailFragment) {
                    ((FTThumbnailFragment) drawerFragment).setToExportMode(pages -> {
                        pagesToExport.addAll(pages);
                        showExportFormatPopup(pagesToExport);
                    }, true);
                }
                break;
        }
        if (!pagesToExport.isEmpty()) showExportFormatPopup(pagesToExport);
    }

    public float getZoomScale() {
        return zoomScale;
    }

    public void setZoomScale(float zoomScale) {
        this.zoomScale = zoomScale;
    }

    //region Left Panel
    @BindView(R.id.nd_item_recycler_view)
    protected RecyclerView mNavRecyclerView;
    @BindView(R.id.nd_header_layout)
    ConstraintLayout mNDHeaderLayout;

    private FTCategoryAdapter mCategoryAdapter;
    private FtShelfItemsViewFragment mShelfItemsViewFragment;
    private FTUrl updatedCollectionUrl;

    private void setCategoriesAdapter() {
        String toolbarColor = FTApp.getPref().get(SystemPref.SELECTED_THEME_TOOLBAR_COLOR, FTConstants.DEFAULT_THEME_TOOLBAR_COLOR);
        mNDHeaderLayout.setBackgroundColor(Color.parseColor(toolbarColor));

        FTShelfCollectionProvider.getInstance().shelfs(colllections -> {
            colllections.remove(colllections.size() - 1);
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
                    mCategoryAdapter = new FTCategoryAdapter(groups, getContext(), false, new FTCategoryAdapter.NavigationItemListener() {
                        @Override
                        public void onShelfItemCollectionSelected(FTShelfItemCollection collection) {
                            mShelfItemsViewFragment = FtShelfItemsViewFragment.newInstance(collection, new FTShelfItem(currentDocument.getFileURL()), null, fileURL -> {
                                updatedCollectionUrl = collection.getFileURL();
                                FTApp.getPref().saveRecentCollectionName(collection.getDisplayTitle(getContext()));
                                openSelectedDocument(fileURL, 0);
                            });
                            getSupportFragmentManager().beginTransaction()
                                    .add(R.id.document_fragment_container, mShelfItemsViewFragment)
                                    .commit();
                        }

                        @Override
                        public void renameCollectionItem(String categoryName, int position, FTShelfItemCollection item) {
                            //Do Nothing
                        }

                        @Override
                        public void removeCollectionItem(int position, FTShelfItemCollection item) {
                            //Do Nothing
                        }

                        @Override
                        public void hideNavigationDrawer() {
                            mDrawerLayout.closeDrawer(GravityCompat.START);
                        }

                        @Override
                        public void openSelectedDocument(FTUrl fileURL, int from) {
                            if (currentDocument.getFileURL().equals(fileURL)) return;
                            hideNavigationDrawer();
//                            FTGLContextFactory.getInstance().destroySharedContext();
                            FTDocumentActivity.this.isSharedContextDestroyed = true;
                            FTDocumentActivity.openDocument(fileURL, getContext(), (success, error) -> {
                                if (!FTShelfCollectionProvider.getInstance().pinnedShelfProvider.checkIfPresent(fileURL.getPath())) {
                                    FTShelfCollectionProvider.getInstance().recentShelfProvider.addRecent(fileURL.getPath());
                                }

                                Intent intent = new Intent();
                                if (updatedCollectionUrl != null)
                                    intent.putExtra("updatedCollectionUrl", updatedCollectionUrl.getPath());
                                setResult(DOCUMENT_ACTIVITY, intent);
                                finish();
                            });
//                            openDocument(fileURL, FTDocumentActivity.this, new CompletionBlock() {
//                                @Override
//                                public void didFinishWithStatus(Boolean success, Error error) {
////                                    Intent intent = new Intent();
////                                    if (updatedCollectionUrl != null)
////                                        intent.putExtra("updatedCollectionUrl", updatedCollectionUrl.getPath());
////                                    setResult(DOCUMENT_ACTIVITY, intent);
////                                    finish();
//                                }
//                            });
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
                    });
                    mNavRecyclerView.setAdapter(mCategoryAdapter);
                    mCategoryAdapter.expandAllGroups();
                });
            });
        });
    }

    @OnClick(R.id.nd_header_plus_image_view)
    protected void showNewCategoryDialog() {
        FTRenameDialog.newInstance(FTRenameDialog.RenameType.NEW_CATEGORY, getString(R.string.untitled), -1, new FTRenameDialog.RenameListener() {
            @Override
            //public void renameShelfItem(String updatedName, int position) {
                //Log.d("TemplatePicker==>","VMK PasswordProtected FTDocumentActivity renameShelfItem::-"+updatedName);
                public void renameShelfItem(String updatedName, int position, DialogFragment dialogFragment) {
                    if (updatedName != null) {
                        didFinishUserInputWithText(updatedName);
                    } else {
                        //Log.d("TemplatePicker==>","VMK PasswordProtected FTDocumentActivity renameShelfItem::-");
                    }
                }

                @Override
                public void dialogActionCancel() {
                    //Sit idle for now
                    //Log.d("TemplatePicker==>","VMK PasswordProtected FTDocumentActivity dialogActionCancel::-");
                }
            }).show(getSupportFragmentManager(), "FTRenameDialog");
        }

        public void didFinishUserInputWithText(final String text) {
            if (!TextUtils.isEmpty(text)) {
                if (text.equalsIgnoreCase(getString(R.string.trash))) {
                    FTDialogFactory.showAlertDialog(getContext(), "", getString(R.string.cannot_use_trash_as_it_is_reserved_by_the_app), "", getString(R.string.ok), null);
                } else {
                    FTShelfCollectionProvider.getInstance().currentProvider().createShelfWithTitle(getContext(), text, (shelf, error) -> {
                        updatedCollectionUrl = shelf.getFileURL();
                        FTApp.getPref().saveRecentCollectionName(shelf.getDisplayTitle(getContext()));
                        setCategoriesAdapter();
                    });
                }
            }
        }

        @OnClick(R.id.nd_header_settings_image_view)
        void showSettings() {
            FTLog.crashlyticsLog("UI: Opened Settings panel");
            FTSettingsDialog.newInstance().show(getSupportFragmentManager());
        }
        //endregion

        @Override
        public void screenshotCaptured(Bitmap bitmapLocal) {
            ScreenshotDialogFragment ftUserDetailsDialog = ScreenshotDialogFragment.newInstance(bitmapLocal);
            ftUserDetailsDialog.show(getSupportFragmentManager(), "ftUserDetailsDialog");
        }

        @Override
        public void shareScreenShot(Uri exportFileUri) {
            startActivityForResult(FTDocumentActivity.this.getShareFilteredIntent(exportFileUri), FTBaseShelfActivity.PICK_EXPORTER);
        }

        @Override
        public void eraserEnded() {
            mDocumentToolbarFragment.onEraserEnded();
        }

        public void enableAllGesture(boolean enable) {
            isAllGesturesEnabled = enable;
        }

        public DrawerLayout getDrawerLayout() {
            return mDrawerLayout;
        }

        @Override
        public boolean onKeyDown(int keyCode, KeyEvent event) {
            boolean isRefreshFragment = adapter.getFragmentAtIndex(getCurrentItemPosition()) instanceof FTRefreshFragment;

            if ((event.getMetaState() & KeyEvent.META_CTRL_ON) != 0) {
                currentPageController.onOutsideTouch(null, null);
                switch (keyCode) {
                    case FTSPenAirActions.SINGLE_CLICK:
                        if (isRefreshFragment) {
                            onKeyDown(getCurrentItemPosition() == 0 ? FTSPenAirActions.SWIPE_NEXT : FTSPenAirActions.SWIPE_PREV, event);
                        }
                        if (currentMode() == FTToolBarTools.PEN) {
                            mPenPref.save(getDocUid() + PenRackPref.PEN_TOOL, FTToolBarTools.HIGHLIGHTER.toInt());
                            toolBarItemsClicked();
                        } else if (currentMode() == FTToolBarTools.HIGHLIGHTER) {
                            mPenPref.save(getDocUid() + PenRackPref.PEN_TOOL, FTToolBarTools.PEN.toInt());
                            toolBarItemsClicked();
                        } else {
                            mPenPref.save(getDocUid() + PenRackPref.PEN_TOOL, FTToolBarTools.PEN.toInt());
                            toolBarItemsClicked();
                        }
                        mDocumentToolbarFragment.updateLastSelectedToolView(true);
                        break;
                    case FTSPenAirActions.DOUBLE_CLICK:
                        if (isRefreshFragment) {
                            onKeyDown(getCurrentItemPosition() == 0 ? FTSPenAirActions.SWIPE_NEXT : FTSPenAirActions.SWIPE_PREV, event);
                        }
                        if (mDocumentToolbarFragment != null) {
                            penRackDialog = FTPenRackDialog.newInstance(currentMode());
                            penRackDialog.show(this.getSupportFragmentManager(), "FTPenRackDialog");
                        }
                        break;
                    case FTSPenAirActions.SWIPE_NEXT:
                        surfacePager.setCurrentItem(currPosition + 1, true);
                        break;
                    case FTSPenAirActions.SWIPE_PREV:
                        surfacePager.setCurrentItem(currPosition - 1, true);
                        break;
                    case FTSPenAirActions.SWIPE_DOWN:
//                    importDocumentAndImage(IMPORT_DOCUMENT);
                        if (!isRefreshFragment)
                            currentPageController.setOnSpenKeyEvent(.75f);
                        break;
                    case FTSPenAirActions.SWIPE_UP:
                        if (!isRefreshFragment)
                            currentPageController.setOnSpenKeyEvent(1.5f);
//                    ArrayList<FTNoteshelfPage> noteshelfPages = new ArrayList<>();
//                    noteshelfPages.add(currentPageController.currentPage());
//                    showExportFormatPopup(noteshelfPages);
                        break;
                    case FTSPenAirActions.ROTATE_CW:
                        showThumbnails(false);
                        break;
                    case FTSPenAirActions.ROTATE_CCW:
                        mDrawerLayout.openDrawer(GravityCompat.START);
                        break;
                }
            }

            return super.onKeyDown(keyCode, event);
        }

        private interface OnDocumentSavedListener {
            void onSaved();
        }
    }