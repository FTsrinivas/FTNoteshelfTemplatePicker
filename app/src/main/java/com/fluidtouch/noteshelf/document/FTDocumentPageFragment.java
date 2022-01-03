package com.fluidtouch.noteshelf.document;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.util.SizeF;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;

import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf.FTRuntimeException;
import com.fluidtouch.noteshelf.annotation.FTAnnotationV1;
import com.fluidtouch.noteshelf.annotation.FTAudioAnnotationV1;
import com.fluidtouch.noteshelf.annotation.FTImageAnnotationV1;
import com.fluidtouch.noteshelf.annotation.FTStrokeV1;
import com.fluidtouch.noteshelf.annotation.FTTextAnnotationV1;
import com.fluidtouch.noteshelf.audio.FTAudioView;
import com.fluidtouch.noteshelf.audio.models.FTAudioPlayerStatus;
import com.fluidtouch.noteshelf.audio.models.FTAudioRecording;
import com.fluidtouch.noteshelf.audio.player.FTAudioPlayer;
import com.fluidtouch.noteshelf.commons.FTLog;
import com.fluidtouch.noteshelf.commons.utils.BitmapUtil;
import com.fluidtouch.noteshelf.commons.utils.FileUriUtils;
import com.fluidtouch.noteshelf.commons.utils.NumberUtils;
import com.fluidtouch.noteshelf.commons.utils.ObservingService;
import com.fluidtouch.noteshelf.commons.utils.ScreenUtil;
import com.fluidtouch.noteshelf.document.enums.FTEraserSizes;
import com.fluidtouch.noteshelf.document.enums.FTToolBarTools;
import com.fluidtouch.noteshelf.document.imageedit.FTPopupView;
import com.fluidtouch.noteshelf.document.lasso.FTLassoFragment;
import com.fluidtouch.noteshelf.document.search.FTSearchableItem;
import com.fluidtouch.noteshelf.document.search.FTTextHighlightView;
import com.fluidtouch.noteshelf.document.textedit.FTEditTextFragment;
import com.fluidtouch.noteshelf.document.textedit.FTStyledText;
import com.fluidtouch.noteshelf.document.textedit.texttoolbar.FTKeyboardToolbarFragment;
import com.fluidtouch.noteshelf.document.undomanager.UndoManager;
import com.fluidtouch.noteshelf.document.views.FTDrawingView;
import com.fluidtouch.noteshelf.document.views.FTZoomLockView;
import com.fluidtouch.noteshelf.documentframework.FTNoteshelfDocument.FTNoteshelfPage;
import com.fluidtouch.noteshelf.preferences.FTBasePref;
import com.fluidtouch.noteshelf.preferences.PenRackPref;
import com.fluidtouch.noteshelf.preferences.SystemPref;
import com.fluidtouch.noteshelf.services.FTFirebaseAnalytics;
import com.fluidtouch.noteshelf.textrecognition.handwriting.utils.FontUtils;
import com.fluidtouch.noteshelf.zoomlayout.FTPageContentHolderView;
import com.fluidtouch.noteshelf.zoomlayout.FTWritingView;
import com.fluidtouch.noteshelf.zoomlayout.FTZoomLayout;
import com.fluidtouch.noteshelf.zoomlayout.FTZoomableLayout;
import com.fluidtouch.noteshelf2.R;
import com.fluidtouch.renderingengine.annotation.FTAnnotation;
import com.fluidtouch.renderingengine.annotation.FTAnnotationType;
import com.fluidtouch.renderingengine.annotation.FTAudioAnnotation;
import com.fluidtouch.renderingengine.annotation.FTImageAnnotation;
import com.fluidtouch.renderingengine.annotation.FTPenType;
import com.fluidtouch.renderingengine.annotation.FTSegment;
import com.fluidtouch.renderingengine.annotation.FTStroke;
import com.fluidtouch.renderingengine.annotation.FTTextAnnotation;
import com.fluidtouch.renderingengine.renderer.FtTempClass;
import com.fluidtouch.renderingengine.touchManagement.FTTouch;
import com.fluidtouch.renderingengine.utils.FTGeometryUtils;

import org.benjinus.pdfium.Link;
import org.jetbrains.annotations.NotNull;
import org.opencv.core.Point;
import org.opencv.core.Size;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Observable;
import java.util.Observer;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import butterknife.BindView;
import butterknife.ButterKnife;

@SuppressLint("ResourceType")
public class FTDocumentPageFragment extends Fragment implements FTDrawingView.DrawingViewCallbacksListener,
        FTAnnotationFragment.Callbacks, FTLassoFragment.LassoFragmentInteractionListener,
        FTAudioView.FTAudioViewCallbacks, FTZoomLayout.FTZoomLayoutContainerCallback,
        FTPageContentHolderView.CallBacks, FTAnnotationManager.FTAnnotationManagerCallback {

    private final int MAX_IMAGE_WIDTH = 1024;
    private final int MAX_IMAGE_HEIGHT = 1024;
    private final RectF eraserRefreshRect = new RectF();
    private final RectF eraserOffscreenRect = new RectF();
    private final ArrayList<FTAnnotation> toBeRemoved = new ArrayList<>();

    //region Member Variables
    @BindView(R.id.pageContentHolderView)
    public FTPageContentHolderView mPageContentHolderView;
    @BindView(R.id.writingView)
    public FTWritingView mWritingView;
    @BindView(R.id.page_text_highlight_view)
    FTTextHighlightView mTextHighlighterLayout;
    @BindView(R.id.pager_item_zoom_layout)
    FTZoomLayout mZoomLayout;
    @BindView(R.id.pagerMainLayout)
    RelativeLayout pagerMainLayout;
    @BindView(R.id.zoom_lock_layout)
    FTZoomLockView zoomLockLayout;
    @BindView(R.id.lock_button)
    ImageView lockButton;
    @BindView(R.id.zoom_percent_text_view)
    TextView zoomPercentTextView;
    @BindView(R.id.pager_item_name_text_view)
    TextView documentNameTextView;
    @BindView(R.id.pager_item_page_number_text_view)
    TextView pageNumberTextView;
    @BindView(R.id.pager_item_details_layout)
    LinearLayout pageDetailsLayout;

    boolean isPageChanged = false;
    int eraserOperationCounter = 0;
    FTLassoFragment mLassoFragment;
    GetHyperLinksTask asyncTaskHyperlinks;
    private int pageIndex = 0;
    private boolean isVisibleToUser;
    private PagerToActivityCallBack actionsListener;
    private PenRackPref mPenPref;
    private FTBasePref mDocPref;
    //Image resize views
    private FrameLayout annotationView;
    private FrameLayout textToolbarView;
    //Eraser views
    private View eraserView;
    private int[] eraserSizes = FTEraserSizes.getSizes();
    private ArrayList<RectF> toBeErasedPoints = new ArrayList<>();
    private Thread eraserThread;
    private boolean eraseOperationCancelled = false;
    private FTNoteshelfPage mNoteshelfPage;
    private UndoManager undoManager;
    private FTAnnotationFragment activeFragment;
    private FTAnnotationManager annotationManager;
    private int mKeyboardDimension = 0;
    private int mKeyboardOffset = 0;
    private int mKeyboardHeightOffset = 0;
    private long previousEraserTimeStamp;
    private boolean isDetached = false;
    private boolean isScroll = false;
    private boolean isAllowLayoutChange = true;
    private float scale = 1;
    private float originalScale = 1;
    private float previousZoom = 1.0f;
    private SizeF layoutSize = new SizeF(0, 0);
    private PointF prevLocation;
    private Handler handlerAddView = new Handler();
    //    private int bgTexture;
    private RectF visibleFrame = new RectF();
    private boolean isThumbnailUsing = false;
    private boolean isZoomLocked = false;
    private List<Link> hyperLinks = null;
    private boolean isZoom = false;
    private boolean canSwipePage = true;
    private boolean isZoomLockInTouchMode = false;
    private PointF lastCursorPoint = null;
    List<FTAnnotation> toBeRemovedAnnotations = new ArrayList<>();
    private boolean isFromHW = false;
    private FTPopupView popupList;
    private List<String> popupMenuItemList = new ArrayList<>();
    FTImageAnnotationV1 imgAnnotation;
    boolean isUnlockPopupShowing;
    private float textureScale = 1f;
    private String documentUid = "";
    int textToolbarHeight = 80;
    String mPrefAutoSelectPrevToolSwitch = "mPrefAutoSelectPrevToolSwitch";
    String mPrefEraseEntireStrokeSwitch = "mPrefEraseEntireStrokeSwitch";
    String mPrefEraseHighlighterStrokeSwitch = "mPrefEraseHighlighterStrokeSwitch";
    private FragmentManager childFragmentManager;
    private View mLastSelectedView;

    FTDocumentPageFragment.FTEraseEndedListener mFTEraseEndedListener;
    private Runnable addDrawingViewTask = new Runnable() {
        @Override
        public void run() {
            if (mZoomLayout != null && mWritingView != null) {
                if (mWritingView.getChildCount() == 0 && isVisibleToUser && currentPage() != null) {
                    mWritingView.setReady();
                    SizeF sizeF = ScreenUtil.setScreenSize(getContext());
                    mWritingView.performLayout(Math.max((int) sizeF.getWidth(), (int) sizeF.getHeight()), FTDocumentPageFragment.this);
                    getDrawingView().setDeviceScale(currentPage().getDeviceScale());
                    actionsListener.onLoadingFinished();
                }
            }
        }
    };

    private FTZoomableLayout.OnPanListener mPanListener = new FTZoomableLayout.OnPanListener() {
        private PointF previousPos = new PointF(-555, -555);

        @Override
        public void onPanBegin(FTZoomableLayout view, MotionEvent event) {
            if (null == getDrawingView())
                return;
            isScroll = true;
            RectF drawRect = view.getDrawRect();
            mWritingView.setVisibility(View.INVISIBLE);
            getDrawingView().setAvoidRenderingForNow(true);
        }

        @Override
        public void onPan(FTZoomableLayout view, MotionEvent event) {
            RectF drawRect = view.getDrawRect();
            if (!isZoom)
                mPageContentHolderView.refreshTiles(false, drawRect);
        }

        @Override
        public void onPanEnd(FTZoomableLayout view, MotionEvent event) {
            if (null == getDrawingView() || isZoom) {
                isScroll = false;
                return;
            }
            getDrawingView().setAvoidRenderingForNow(false);
            RectF drawRect = view.getDrawRect();
            if (previousPos.x != drawRect.left || previousPos.y != drawRect.top)
                setDrawingViewPosition(drawRect.left, drawRect.top, mPageContentHolderView.getWidth(), mPageContentHolderView.getHeight(), drawRect, false);
            else {
                mWritingView.setVisibility(View.VISIBLE);
            }
            previousPos = new PointF(drawRect.left, drawRect.top);
            isScroll = false;
        }
    };

    private Observer onLayoutChangeObserver = (observable, arg) -> {
        isAllowLayoutChange = true;
    };

    private Observer viewPagerState = (observable, arg) -> {
        int currentIndex = (int) arg;
        if (pageIndex == currentIndex) {
            mPageContentHolderView.setTileView(mPageContentHolderView);
        } else if (pageIndex == currentIndex - 1 || pageIndex == currentIndex + 1) {
            new Handler().postDelayed(() -> mPageContentHolderView.setTileView(mPageContentHolderView), 100);
        }
    };
    //endregion
    private boolean isStylushBtnPressed = false;
    //region Audio
    private FTAudioView mAudioView;

    private Observer mAudioObserver = new Observer() {
        @Override
        public void update(Observable o, Object arg) {
            FTAudioPlayerStatus status = (FTAudioPlayerStatus) arg;
            if (status.getPlayerMode() == FTAudioPlayerStatus.FTPlayerMode.PLAYING_STARTED
                    || status.getPlayerMode() == FTAudioPlayerStatus.FTPlayerMode.RECORDING_STARTED) {
                if (mAudioView == null && currentPage() != null) {
                    List<FTAudioAnnotation> audioAnnotations = currentPage().getAudioAnnotations();
                    for (int i = 0; i < audioAnnotations.size(); i++) {
                        if (((FTAudioAnnotationV1) audioAnnotations.get(i)).getAudioRecording() != null && ((FTAudioAnnotationV1) audioAnnotations.get(i)).getAudioRecording().fileName.equals(status.audioRecordingName)) {
                            addAudioView(audioAnnotations.get(i), FTAudioPlayer.getInstance().mRecording, true);
                            break;
                        }
                    }
                }
            } else if (status.getPlayerMode() == FTAudioPlayerStatus.FTPlayerMode.PLAYING_STOPPED
                    || status.getPlayerMode() == FTAudioPlayerStatus.FTPlayerMode.RECORDING_STOPPED) {
                if (!status.audioRecordingName.equals(FTAudioPlayer.getInstance().getCurrentAudioName())) {
                    onOutsideTouch(FTToolBarTools.AUDIO, null);
                }
            }
        }
    };

    private final FTDocumentPageFragment.ZoomTouchListener mZoomTouchListener = new ZoomTouchListener() {
        @Override
        public void onTouch(MotionEvent motionEvent) {
            FTDrawingView drawingView = getDrawingView();
            if (mWritingView == null || drawingView == null || mPageContentHolderView.generatingPageBg)
                return;
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                if (!drawingView.isCurrentPage() && isVisibleToUser) {
                    drawingView.setIsCurrentPage(true);
                }
            }
            MotionEvent event = MotionEvent.obtain(motionEvent);
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN && activeFragment instanceof FTEditTextFragment) {
                FTDocumentPageFragment.this.onOutsideTouch(FTToolBarTools.TEXT, motionEvent);
            }

            if (motionEvent.getPointerCount() == 1) {
                if (mWritingView.getVisibility() != View.VISIBLE && !drawingView.isRendering && !isScroll) {
                    mWritingView.setVisibility(View.VISIBLE);
                }
                drawingView.processTouchEvent(motionEvent);
            } else {
                event.setAction(MotionEvent.ACTION_CANCEL);
                drawingView.processTouchEvent(event);
            }
            if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                isScroll = false;
            }
        }

        @Override
        public void onOutsideTouch(MotionEvent event) {
            if (currentMode() == FTToolBarTools.TEXT || currentMode() == FTToolBarTools.IMAGE) {
                FTDocumentPageFragment.this.onOutsideTouch(FTToolBarTools.TEXT, event);
            }

            hideTool();
        }
    };
    private Runnable mRunnable;
    private Handler mHandler = new Handler();
    private FTZoomableLayout.OnZoomListener mZoomListener = new FTZoomableLayout.OnZoomListener() {

        @Override
        public void onZoomBegin(FTZoomableLayout view, float scale, MotionEvent event) {
            FTDrawingView drawingView = getDrawingView();
            if (null == drawingView) {
                return;
            }
            if (event != null) {
                MotionEvent cancelEvent = MotionEvent.obtain(event);
                cancelEvent.setAction(MotionEvent.ACTION_CANCEL);
                drawingView.processTouchEvent(cancelEvent);
            }
            mPageContentHolderView.updateBackgroundImage();
            isScroll = true;
            isZoom = true;
            mWritingView.setVisibility(View.INVISIBLE);
            getDrawingView().setAvoidRenderingForNow(true);
            mPageContentHolderView.removeHandler();
            showZoomLock();
        }

        @Override
        public void onZoom(FTZoomableLayout view, float scale, MotionEvent event) {
            int currentZoom = (int) Math.ceil(previousZoom * scale * 100);
            if (currentZoom < 100) {
                currentZoom = 100;
            } else if (currentZoom > 400) {
                currentZoom = 400;
            }
            zoomPercentTextView.setText(String.format(Locale.US, "%d%%", currentZoom));
        }

        @Override
        public void onZoomEnd(FTZoomableLayout view, float scale, MotionEvent event) {
            if (null == getDrawingView())
                return;
            getDrawingView().setAvoidRenderingForNow(false);
            onPageZoomChanged(scale);
        }
    };
    private Observer zoomLockObserver = (o, arg) -> showZoomLock();

    private Observer onPageZoomChanged = (o, arg) -> {
        float scale = (float) arg;
        onPageZoomChanged(scale);
    };

    //region Lifecycle Events
    public static FTDocumentPageFragment newInstance(FTNoteshelfPage page, Bundle arguments) {
        FTDocumentPageFragment fragment = new FTDocumentPageFragment();
        fragment.isThumbnailUsing = page.getIsinUse();
        fragment.mNoteshelfPage = page;
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public float getScale() {
        return scale;
    }

    @Override
    public boolean isTextInEditMode() {
        return activeFragment instanceof FTEditTextFragment;
    }

    public void removeWritingView() {
        if (mWritingView != null) {
            mWritingView.setVisibility(View.INVISIBLE);
            mPageContentHolderView.removeView(mWritingView);
        }
    }

    @Override
    public void setAudioMode(boolean enabled) {
        if (getActivity() != null && ((FTDocumentActivity) getActivity()).mAudioToolbarFragment == null)
            isAllowLayoutChange = true;
        canSwipePage = !enabled;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(null);
        actionsListener = (PagerToActivityCallBack) getActivity();
        mPenPref = new PenRackPref().init(PenRackPref.PREF_NAME);
        childFragmentManager = getChildFragmentManager();
    }

    /**
     * The Fragment's UI is just a simple text view showing its
     * instance number.
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.pager_item2, container, false);
        ButterKnife.bind(this, view);
        mPageContentHolderView.setCallBacks(this);
        mZoomLayout.setZoomTouchListener(mZoomTouchListener);
        mZoomLayout.addOnPanListener(mPanListener);
        mZoomLayout.addOnZoomListener(mZoomListener);
        mZoomLayout.setCallbacksListener(this);
        mZoomLayout.setAllowParentInterceptOnScaled(true);
        //mZoomLayout.setOnDragListener(this);
        pageIndex = getArguments().getInt("num");
        mDocPref = actionsListener.getDocPref();
        popupMenuItemList.add(getResources().getString(R.string.unlock));
        pagerMainLayout.setClickable(true);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        textToolbarHeight = getResources().getDimensionPixelOffset(R.dimen.new_48dp);
        mZoomLayout.addOnLayoutChangeListener((view1, i, i1, i2, i3, i4, i5, i6, i7) -> {
            int w = view1.getWidth();
            int h = view1.getMeasuredHeight();
            SizeF newSize = new SizeF(w, h);
            SizeF oldSize = new SizeF(layoutSize.getWidth(), layoutSize.getHeight());
            layoutSize = newSize;
            if (isAllowLayoutChange && !newSize.equals(oldSize)) {
                updateViewLayoutWrtView(view1);
                layoutSize = newSize;
                isAllowLayoutChange = false;
            }
        });

        if (mNoteshelfPage == null) {
            mNoteshelfPage = actionsListener.noteshelfPage(pageIndex);
        }
        //Added this block to see if noteshelf page still becomes null
        try {
            documentUid = mNoteshelfPage.getParentDocument().getDocumentUUID();
            mZoomLayout.documentUid = documentUid;
            configureZoomLock();

            ObservingService.getInstance().addObserver("searchObserver_" + documentUid, searchObserver);
            ObservingService.getInstance().addObserver("viewPagerState_" + documentUid, viewPagerState);
            ObservingService.getInstance().addObserver("onPageZoomed", onPageZoomChanged);
            ObservingService.getInstance().addObserver("isZoomLocked_" + documentUid, zoomLockObserver);
            undoManager = new UndoManager();

            new Handler().postDelayed(() -> {
                try {
                    if (annotationManager == null && !isDetached) {
                        annotationManager = new FTAnnotationManager(this);
                        annotationManager.setCurrentPage(mNoteshelfPage);
                    }
                } catch (Exception e) {
                    // Expected crash "'FTNoteshelfPage' null reference"
//                    FTLog.logCrashException(e);
                }
            }, 300);
        } catch (Exception e) {
            // Expected crash "'FTNoteshelfPage.getParentDocument()' on a null object reference"
//            FTLog.logCrashException(e);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        ObservingService.getInstance().removeObserver("viewPagerState_" + documentUid, viewPagerState);
        FTDrawingView drawingView = getDrawingView();
        if (drawingView != null) {
            removeDrawingView();
        }
        if (mPenPref.get(documentUid + PenRackPref.PEN_TOOL, -1) == FTToolBarTools.IMAGE.toInt()) {
            mPenPref.save(documentUid + PenRackPref.PEN_TOOL, mPenPref.get(documentUid + PenRackPref.PEN_TOOL_OLD, -1));
        }
        isDetached = true;
        if (null != mNoteshelfPage) {
            mNoteshelfPage.setIsinUse(isThumbnailUsing);
            mNoteshelfPage.destroy();
        }
    }

    private void updateNameNPageNumber() {
        if (false && mNoteshelfPage != null && mNoteshelfPage.isTemplate()) {
            int padding = (int) (14 * scale * getResources().getDisplayMetrics().density);
            documentNameTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12 * scale);
            pageNumberTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12 * scale);
            pageDetailsLayout.setPadding(padding, padding, padding, padding);
            documentNameTextView.setText(mNoteshelfPage.getParentDocument().getDisplayTitle(requireContext()));
            pageNumberTextView.setText((mNoteshelfPage.pageIndex() + 1) + " of " + mNoteshelfPage.getParentDocument().pages(requireContext()).size());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        FTAudioPlayer.getInstance().addObserver(requireContext(), mAudioObserver);
        isVisibleToUser = true;
        if (mWritingView.getChildCount() == 0) {
            if (mPageContentHolderView.getBGTexturre() == 0)
                handlerAddView.postDelayed(addDrawingViewTask, 1000);
            else
                handlerAddView.postDelayed(addDrawingViewTask, 100);
        } else if (getDrawingView() != null) {
            new Handler().postDelayed(() -> getDrawingView().setIsCurrentPage(true), 50);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (getContext() == null) {
            return;
        }

        FTAudioPlayer.getInstance().removeObserver(requireContext(), mAudioObserver);
        isVisibleToUser = false;
        mWritingView.setVisibility(View.INVISIBLE);
        if (getDrawingView() != null) {
            getDrawingView().setIsCurrentPage(false);
        }
        if (isPageChanged) {
            if (eraserView != null) {
                eraseOperationCancelled = false;
                eraserView.setVisibility(View.GONE);
                mPageContentHolderView.removeView(eraserView);
                eraserView = null;
            }
        }
        if (activeFragment != null && FTDocumentActivity.isAnnotationOpen == 0) {
            activeFragment.outsideClick();
        }
    }

    @Override
    public void onStop() {
        try {
            ArrayList<FTAnnotation> annotations = getPageAnnotations();
            for (int i = 0; i < annotations.size(); i++) {
                if (annotations.get(i).annotationType() != FTAnnotationType.stroke) {
                    annotations.get(i).deleteTexture();
                }
            }
        } catch (Exception e) {
            //Expected crash is "'FTNoteshelfPage.getPageAnnotations()' on a null object reference"
//            FTLog.logCrashException(e);
        }
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (currentPage() != null)
            ObservingService.getInstance().removeObserver("searchObserver_" + documentUid, searchObserver);
        if (mAudioView != null) {
            mAudioView.outsideClick();
            mAudioView = null;
        }
        ObservingService.getInstance().removeObserver("onPageZoomed", onPageZoomChanged);
        ObservingService.getInstance().removeObserver("isZoomLocked_" + documentUid, zoomLockObserver);
        FtTempClass.getInstance().deleteTextures();
    }
    //endregion

    private void updateViewLayoutWrtView(View view) {
        if (mNoteshelfPage == null) {
            return;
        }
        RectF pageRect = mNoteshelfPage.getPageRect();
        float screenWidth = view.getWidth();
        float screenHeight = view.getHeight();
        Log.d("TemplatePicker==>"," updateViewLayoutWrtView getWidth::-"+view.getWidth());
        //new zoom code
        if (isVisibleToUser)
            previousZoom = ((FTDocumentActivity) getContext()).getZoomScale();
        if (previousZoom <= 1)
            previousZoom = 1;
        mZoomLayout.setMinScale(1 / previousZoom);
        mZoomLayout.setMaxScale(4 / previousZoom);
        Log.d("TemplatePicker==>","updateViewLayoutWrtView pageRect.width()::-"+pageRect.width()+" pageRect.height()::-"+pageRect.height()+" screenWidth::-"+screenWidth+" screenHeight::-"+screenHeight);
        SizeF aspectSize = FTGeometryUtils.aspectSize(new SizeF(pageRect.width(), pageRect.height()), new SizeF(screenWidth, screenHeight));
        FrameLayout.LayoutParams params2 = new FrameLayout.LayoutParams((int) (aspectSize.getWidth() * previousZoom), (int) (aspectSize.getHeight() * previousZoom));
        originalScale = aspectSize.getWidth() / pageRect.width();
        aspectSize = FTGeometryUtils.aspectSize(new SizeF(pageRect.width(), pageRect.height()), new SizeF(params2.width, params2.height));
        this.scale = aspectSize.getWidth() / pageRect.width();
        mZoomLayout.currentScale = this.scale;

        mPageContentHolderView.setLayoutParams(params2);
        mPageContentHolderView.setPage(mNoteshelfPage);

        mWritingView.setContentScale(this.scale);
        updateNameNPageNumber();
        mWritingView.invalidate();

        if (mWritingView.getChildCount() > 0) {
            FTDrawingView drawingView = getDrawingView();
            drawingView.scale = mWritingView.getScale();
            mWritingView.requestLayout();
        } else if (isVisibleToUser) {
            handlerAddView.removeCallbacks(addDrawingViewTask);
            handlerAddView.postDelayed(addDrawingViewTask, 1000);
        }

        if (mAudioView != null) {
            mAudioView.onLayoutChanged();
        }

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Executors.newSingleThreadExecutor().execute(() -> {
            if (currentPage().searchForKey(getArguments().getString("searchKey", ""))) {
                if (!isDetached)
                    getActivity().runOnUiThread(() -> {
                        highlightSearchItemsInPage();
                        executorService.shutdownNow();
                    });
            }
        });

        mZoomLayout.setOnHoverListener(new View.OnHoverListener() {
            public boolean onHover(View view, MotionEvent motionEvent) {
                if (((motionEvent.getButtonState() & 32) == 32 || (motionEvent.getButtonState() & 64) == 64 || (motionEvent.getButtonState() & 2) == 2 || (motionEvent.getButtonState() & 4) == 4)) {
                    if (currentMode() == FTToolBarTools.PEN || currentMode() == FTToolBarTools.HIGHLIGHTER) {
                        setCurrentMode(FTToolBarTools.ERASER);
                        isStylushBtnPressed = true;
                    }
                } else if (isStylushBtnPressed) {
                    actionsListener.updateToolBarModeToLastSelected();
                    isStylushBtnPressed = false;
                }
                return true;
            }
        });
    }

    @Override
    public FTNoteshelfPage currentPage() {
        return this.mNoteshelfPage;
    }
    //endregion

    //region TextMode
    @Override
    public void addInputTextView(FTToolBarTools tools, FTAnnotation annotation, MotionEvent event) {
        FTFirebaseAnalytics.logEvent("TapToAddTextBox");
        if (isScroll)
            return;
        if (tools == FTToolBarTools.TEXT) {
            mZoomLayout.setAllowZoom(false);
            if (annotation != null) {
                onLongPress(annotation);
            } else {
                onOutsideTouch(tools, event);
                RectF boundingRect = new RectF(event.getX(), event.getY(), event.getX(), event.getY());
                addAnnotationFragment(FTAnnotationV1.getTextAnnotation(getContext(), FTGeometryUtils.scaleRect(boundingRect, 1 / getDrawingView().scale)));
            }
        }
    }

    public void addInputTextView(String text, float xPos, float yPos) {
        if (isScroll)
            return;
        RectF boundingRect = new RectF(xPos, yPos, xPos, yPos);
        FTTextAnnotationV1 textAnnotation = (FTTextAnnotationV1) FTAnnotationV1.getTextAnnotation(getContext(), FTGeometryUtils.scaleRect(boundingRect, 1 / getDrawingView().scale));
        FTStyledText styledText = new FTStyledText();
        styledText.setPlainText(text);
        textAnnotation.setInputTextWithInfo(styledText);
        addAnnotationFragment(textAnnotation);

    }

    @Override
    public void onOutsideTouch(FTToolBarTools tools, MotionEvent motionEvent) {
        if (activeFragment != null) {
            activeFragment.outsideClick();
        }

        if (tools == FTToolBarTools.AUDIO) {
            isAllowLayoutChange = true;
            if (mAudioView != null) {
                mAudioView.outsideClick();
            }
        }
    }

    @Override
    public boolean isInsideAudio(MotionEvent ev) {
        return mAudioView != null && mAudioView.getBoundingRect().contains(ev.getX(), ev.getY());
    }

    public void addAudioView(FTAnnotation annotation, FTAudioRecording recording, boolean isFromBroadcast) {
        isAllowLayoutChange = true;
        FTDrawingView drawingView = getDrawingView();
        if (annotation == null) {
            annotation = FTAnnotationV1.getAudioAnnotation(getContext(), currentPage(), recording);

            FTAudioPlayer.getInstance().play(getContext(), ((FTAudioAnnotationV1) annotation).getAudioRecording(),
                    currentPage().getParentDocument().resourceFolderItem().getFileItemURL().getPath() + "/");
            FTAnnotation finalAnnotation = annotation;
            mAudioView = new FTAudioView(getContext(), finalAnnotation, this);
            mPageContentHolderView.addView(mAudioView, mPageContentHolderView.getChildCount());
            FTAudioPlayer.currentAudioPageIndex = currentPage().pageIndex();
        } else {
            if (isFromBroadcast) {
                annotation.hidden = true;
                final RectF boundingRect = FTGeometryUtils.scaleRect(annotation.getBoundingRect(), drawingView.scale);

                mAudioView = new FTAudioView(getContext(), annotation, this);
                mPageContentHolderView.addView(mAudioView, mPageContentHolderView.getChildCount());
                FTAudioPlayer.currentAudioPageIndex = currentPage().pageIndex();
                drawingView.reloadInRect(boundingRect);
            } else {
                FTAudioPlayerStatus.FTPlayerMode mode = FTAudioPlayer.getInstance().getPlayerMode();
                if ((mode == FTAudioPlayerStatus.FTPlayerMode.PLAYING_STARTED || mode == FTAudioPlayerStatus.FTPlayerMode.PLAYING_PAUSED)
                        && FTAudioPlayer.getInstance().getCurrentAudioName().equals(((FTAudioAnnotationV1) annotation).getAudioRecording().fileName)) {
                    addAudioView(annotation, ((FTAudioAnnotationV1) annotation).getAudioRecording(), true);
                } else {
                    FTAudioPlayer.getInstance().play(getContext(), ((FTAudioAnnotationV1) annotation).getAudioRecording(),
                            currentPage().getParentDocument().resourceFolderItem().getFileItemURL().getPath() + "/");
                }
            }
        }
    }
    //endregion

    @Override
    public synchronized void onLongPress(MotionEvent motionEvent) {
        if (!isScroll && isVisibleToUser && FTDocumentActivity.SCROLL_STATE == ViewPager.SCROLL_STATE_IDLE && currentMode() != FTToolBarTools.LASSO) {
            PointF p = new PointF(motionEvent.getX(), motionEvent.getY());
            FTAnnotation annotation = annotationUnderPoint(p, false);

            if (null != annotation) {
                motionEvent.setAction(MotionEvent.ACTION_CANCEL);
                getDrawingView().processTouchEvent(motionEvent);
                onLongPress(annotation);
            }

            if ((!FTApp.getPref().get(SystemPref.HYPERLINKS_DISABLED, false) && mDocPref.get("haveLinks" + currentPage().uuid, true))
                    || (currentMode() != FTToolBarTools.PEN && currentMode() != FTToolBarTools.HIGHLIGHTER && currentMode() != FTToolBarTools.ERASER)) {
                applyHyperlinks(motionEvent.getX(), motionEvent.getY());
            }
        }
    }

    @Override
    public void onAudioEditFinish() {
        if (mAudioView != null) {
            mPageContentHolderView.removeView(mAudioView);
            mAudioView = null;
            setAudioMode(false);
        }
    }

    @Override
    public void setAllowZoom(boolean allowZoom) {
        mZoomLayout.setAllowZoom(allowZoom);
    }

    //region LassoMode
    void addLassoFragment(View lassoToolView) {
        if (null == mZoomLayout || null == getActivity()) {
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                addLassoFragment(lassoToolView);
            }, 50);
            return;
        }
        int lastViewIndex = mPageContentHolderView.getChildCount() - 1;
        Object tag = mPageContentHolderView.getChildAt(lastViewIndex) == null ? null : mPageContentHolderView.getChildAt(lastViewIndex).getTag();
        if (tag != null && tag.equals(getString(R.string.tag_lasso_container)) && mLassoFragment != null) {
            mLassoFragment.showLassoOptions(lassoToolView);
        } else {
            mZoomLayout.setAllowZoom(false);
            Log.i("LASSO", "Add Lasso");
            annotationView = new FrameLayout(requireActivity());
            annotationView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            annotationView.setId(54447);
            annotationView.setTag(getString(R.string.tag_lasso_container));
            mPageContentHolderView.addView(annotationView, mPageContentHolderView.getChildCount());
            mLassoFragment = FTLassoFragment.newInstance(requireContext(), this);
            getChildFragmentManager().beginTransaction().add(annotationView.getId(), mLassoFragment, "Lasso").commit();
        }
//        canSwipePage = false;
    }

    @Override
    public void reloadAnnotationsInRect(RectF boundingRect) {
        if (getDrawingView() != null) {
            refreshOffscreen(FTGeometryUtils.scaleRect(boundingRect, 1 / getContainerScale()));
            getDrawingView().reloadInRect(boundingRect);
        }
    }
    //endregion

    @Override
    public void addAnnotations(List<FTAnnotation> annotations, boolean refreshView) {
        annotationManager.addAnnotations(annotations, refreshView);
    }

    @Override
    public void addTextBoxFromHW(@NotNull String text, List<FTAnnotation> annotations) {
        resetAnnotationFragment(null);
        FTStyledText styledText = new FTStyledText();
        RectF rect = new RectF();
        String selectedStyle = FTApp.getPref().get(SystemPref.CONVERT_TO_TEXTBOX_FONT_TYPE, "fit");
        int color = 0;
        toBeRemovedAnnotations = new ArrayList<>();
        for (int i = 0; i < annotations.size(); i++) {
            FTAnnotation annotation = annotations.get(i);
            if (annotation instanceof FTStroke) {
                FTPenType penType = ((FTStroke) annotation).penType;
                if (penType != FTPenType.highlighter && penType != FTPenType.flatHighlighter) {
                    if (color == 0)
                        color = ((FTStroke) annotation).getStrokeColor();
                    rect.union(annotation.getBoundingRect());
                    annotation.hidden = true;
                    toBeRemovedAnnotations.add(annotation);
                }
            }
        }
        int margin = getContext().getResources().getDimensionPixelOffset(R.dimen._10dp);
        rect.left = rect.left - margin;
        rect.top = rect.top - margin;
        rect.right = rect.right + margin;
        rect.bottom = rect.bottom + margin;
        if (!selectedStyle.equalsIgnoreCase("default")) {
            int size = FontUtils.calculateFontSizeByBoundingRect(getContext(), text, (int) rect.width(), (int) rect.height());
            styledText.setSize(size);
            styledText.setColor(color);
        }
        FTTextAnnotationV1 textAnnotation = (FTTextAnnotationV1) FTAnnotationV1.getTextAnnotation(getContext(), rect);
        styledText.setPlainText(text);
        textAnnotation.setInputTextWithInfo(styledText);
        addInputTextView(FTToolBarTools.TEXT, textAnnotation, null);
        isFromHW = true;
    }

    @Override
    public void removeAnnotations(List<FTAnnotation> annotations, boolean refreshView) {
        ArrayList<Integer> indexes = new ArrayList<>();
        for (int i = 0; i < annotations.size(); i++) {
            indexes.add(currentPage().getPageAnnotations().indexOf(annotations.get(i)));
        }
        Collections.sort(indexes);
        annotationManager.removeAnnotations(annotations, indexes, refreshView);
    }

    //region EraserMode
    @Override
    public SizeF eraserSize() {
        int length = ScreenUtil.convertDpToPx(requireContext(), eraserSizes[mPenPref.get(PenRackPref.SELECTED_ERASER_SIZE, 2) - 1]);
        return new SizeF(length, length);
    }

    @Override
    public void enableAllGesture(boolean enable) {
        if (getContext() instanceof FTDocumentActivity) {
            ((FTDocumentActivity) getContext()).enableAllGesture(enable);
            mZoomLayout.enableAllGesture(enable);
        }
    }

    @Override
    public void onEraserBegin(FTTouch ftTouch) {
        FTLog.crashlyticsLog("Eraser: Started");
        int length = ScreenUtil.convertDpToPx(requireContext(), eraserSizes[mPenPref.get(PenRackPref.SELECTED_ERASER_SIZE, 2) - 1]);
        if (eraserView == null) {
            eraserView = new View(getContext());
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(length, length);
            params.leftMargin = (int) ftTouch.currentPosition.x - length / 2;
            params.topMargin = (int) ftTouch.currentPosition.y - length / 2;
            eraserView.setLayoutParams(params);
            eraserView.setBackgroundResource(R.drawable.eraser_size_bg);
            mPageContentHolderView.addView(eraserView, mPageContentHolderView.getChildCount());
        }

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(length, length);
        params.leftMargin = (int) ftTouch.currentPosition.x - length / 2;
        params.topMargin = (int) ftTouch.currentPosition.y - length / 2;
        eraserView.setLayoutParams(params);
        prevLocation = ftTouch.currentPosition;
        previousEraserTimeStamp = ftTouch.timeStamp;
        toBeErasedPoints.clear();

    }

    @Override
    public void onEraserMove(FTTouch ftTouch) {
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) eraserView.getLayoutParams();
        if (mPenPref.get(PenRackPref.SELECTED_ERASER_SIZE, 2) == 4) {
            float distanceFromPrevious = (float) Math.sqrt(Math.pow(ftTouch.currentPosition.x - prevLocation.x, 2) + Math.pow(ftTouch.currentPosition.y - prevLocation.y, 2));
            prevLocation = ftTouch.currentPosition;
            float timeSincePrevious = (float) (ftTouch.timeStamp - previousEraserTimeStamp) / 1000;
            previousEraserTimeStamp = ftTouch.timeStamp;
            float acceleration = distanceFromPrevious / timeSincePrevious;
            int size = ScreenUtil.convertDpToPx(requireContext(), (int) normalizedEraserSizeForAcceleration(acceleration));
            params.width = size;
            params.height = size;
        }
        params.leftMargin = (int) ftTouch.currentPosition.x - params.width / 2;
        params.topMargin = (int) ftTouch.currentPosition.y - params.height / 2;
        if (eraserView != null)
            eraserView.setLayoutParams(params);
    }

    @Override
    public void onEraserCancel(FTTouch ftTouch) {
        if (eraserView != null) {
            eraserView.setVisibility(View.GONE);
            mPageContentHolderView.removeView(eraserView);
            eraserView = null;
        }

        int size = toBeRemoved.size();
        if (size > 0) {
            RectF rectToRefres = new RectF();
            for (int i = 0; i < size; i++) {
                FTStroke annotation = (FTStroke) toBeRemoved.get(i);
                if (i == 0) {
                    rectToRefres.set(annotation.getBoundingRect());
                } else {
                    rectToRefres.union(annotation.getBoundingRect());
                }
                annotation.isErased = false;
            }
            reloadInRect(FTGeometryUtils.scaleRect(rectToRefres, scale));
        }
        toBeRemoved.clear();
    }

    @Override
    public void onEraserEnded(FTTouch ftTouch) {
        FTLog.crashlyticsLog("Eraser: End");
        if (eraserView != null) {
            if (mFTEraseEndedListener != null) {
                //Auto select previous tool switch enabled in pen rack settings
                if (mPenPref.get(mPrefAutoSelectPrevToolSwitch, false)) {
                    mFTEraseEndedListener.eraserEnded();
                }
            }
            eraserView.setVisibility(View.GONE);
            mPageContentHolderView.removeView(eraserView);
            eraserView = null;
        }
    }

    private float normalizedEraserSizeForAcceleration(float acceleration) {
        float eraserSize = FTEraserSizes.auto.getValue();
        acceleration = acceleration / 1000;
        eraserSize = (float) (eraserSize * Math.pow(2.71828182845904523536028747135266250, (double) acceleration));
        return clamp((int) eraserSize, FTEraserSizes.min_size.getValue(), FTEraserSizes.max_size.getValue());
    }

    private int clamp(int x, int low, int high) {
        if (x > high)
            return high;
        else if (x < low)
            return low;
        else
            return x;
    }

    @Override
    public void performEraseAction(PointF erasePoint, FTDrawingView.UITouchPhase phase) {
        float eraserSize = eraserSize().getWidth();
        switch (phase) {
            case BEGAN:
                this.eraseOperationCancelled = false;
                eraserOperationCounter = 0;
                eraserOffscreenRect.set(0, 0, 0, 0);
                break;
            case MOVED:
                this.performEraseAction(erasePoint, (float) eraserSize, null);
                break;
            case ENDED:
                //coommented code to resolve crash
//                this.performEraseAction(erasePoint, (float) eraserSize, new FTEraseCompletionBlock() {
//                    @Override
//                    public void didFinishErasing() {
//                    }
//                });
                //ToDo: Move to completion block
                if (toBeRemoved != null && toBeRemoved.size() > 0) {
                    removeAnnotations(toBeRemoved, true);
                }
                toBeRemoved.clear();
                eraseOperationCancelled = false;
                eraserOperationCounter = 0;
                if (null != eraserThread)
                    eraserThread.interrupt();
                eraserThread = null;
                addEraserSegToUndoManager();
                //The offscreen will refresh after the eraser action is finished.
                refreshOffscreen(FTGeometryUtils.scaleRect(eraserOffscreenRect, 1 / getContainerScale()));
                eraserOffscreenRect.set(0, 0, 0, 0);
                break;
            case CANCELLED:
                this.eraseOperationCancelled = true;
                if (null != eraserThread)
                    eraserThread.interrupt();
                eraserThread = null;
                eraserOperationCounter = 0;
                addEraserSegToUndoManager();
                break;
            default:
                break;
        }
    }

    @Override
    public void onDoubleTap(MotionEvent motionEvent) {
        PointF p = new PointF(motionEvent.getX(), motionEvent.getY());
        FTAnnotation annotation = annotationUnderPoint(p, true);
        motionEvent.setAction(MotionEvent.ACTION_CANCEL);
        addInputTextView(FTToolBarTools.TEXT, annotation, motionEvent);
    }

    private void performEraseAction(final PointF eraserPoint, final float eraserSize, final FTEraseCompletionBlock onCompletion) {
        this.eraserOperationCounter += 1;
        Log.i("Tread start", "" + eraserOperationCounter);

        RectF eraserRect = new RectF(eraserPoint.x - (eraserSize * 0.5f), eraserPoint.y - (eraserSize * 0.5f), 0, 0);
        eraserRect.right = eraserRect.left + eraserSize;
        eraserRect.bottom = eraserRect.top + eraserSize;
        synchronized (currentPage()) {
            toBeErasedPoints.add(eraserRect);
        }

        if (null == eraserThread) {
            eraserRefreshRect.set(0, 0, 0, 0);
            eraserThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (!eraserThread.interrupted()) {
                        if (!eraseOperationCancelled && null != currentPage()) {
                            RectF currentEraseRect = null;
                            synchronized (currentPage()) {
                                if (toBeErasedPoints.size() > 0) {
                                    currentEraseRect = toBeErasedPoints.get(0);
                                    toBeErasedPoints.remove(0);
                                }
                            }
                            if (null != currentEraseRect) {
                                Log.i("Tread", "" + currentEraseRect);
                                float scaleFactor = (1 / scale);
                                RectF eraseRectIn1x = FTGeometryUtils.scaleRect(currentEraseRect, scaleFactor);
                                //new RectF(eraserRect.left * scaleFactor, eraserRect.top * scaleFactor, eraserRect.right * scaleFactor, eraserRect.bottom * scaleFactor);

                                ArrayList<FTAnnotation> annotations = currentPage().getPageAnnotations();

                                for (int a = 0; a < annotations.size(); a++) {
                                    FTAnnotation annotation = annotations.get(a);
                                    if (annotation != null && annotation.annotationType() == FTAnnotationType.stroke &&
                                            RectF.intersects(annotation.getBoundingRect(), eraseRectIn1x)) {
                                        FTStrokeV1 stroke = (FTStrokeV1) annotation;
                                        if (!stroke.isErased) {
                                            if (mPenPref.get(mPrefEraseHighlighterStrokeSwitch, false)) {
                                                /*
                                                 * Remove segmentwise Highlighter strokes when both options are selected in eraser rack
                                                 */
                                                if (stroke.penType.toInt() == FTPenType.highlighter.ordinal() ||
                                                        stroke.penType.toInt() == FTPenType.flatHighlighter.ordinal()) {
                                                    eraseStroke(stroke, eraseRectIn1x);
                                                }
                                            } else {
                                                eraseStroke(stroke, eraseRectIn1x);
                                            }
                                        }
                                    }
                                }
                                if (getActivity() != null) {
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            eraserOperationCounter -= 1;
                                            Log.i("Tread end", "" + eraserOperationCounter);
                                            if (currentPage() != null) {
                                                if (!eraserRefreshRect.isEmpty() && eraserOperationCounter <= 0) {
                                                    reloadInRect(FTGeometryUtils.scaleRect(eraserRefreshRect, scale));
//                                                        refreshOffscreen(FTGeometryUtils.scaleRect(eraserRefreshRect, 1 / getContainerScale()));
                                                    eraserRefreshRect.set(0, 0, 0, 0);
                                                    if (onCompletion != null) {
                                                        onCompletion.didFinishErasing();
                                                    }
                                                    synchronized (currentPage()) {
                                                        currentPage().setPageDirty(true);
                                                    }
                                                }
                                            }
                                        }
                                    });
                                }
                            }
                        }
                    }
                }
            });
            eraserThread.start();
        }

    }

    ArrayList<ErasedSegmentDataObj> erasedSegmentsByStrokes = new ArrayList<>();

    private void eraseStroke(FTStrokeV1 stroke, RectF eraseRectIn1x) {
        RectF eraseRect = new RectF(eraseRectIn1x.left, eraseRectIn1x.top, eraseRectIn1x.right, eraseRectIn1x.bottom);
        if (mPenPref.get(mPrefEraseEntireStrokeSwitch, true)) {
            if (!stroke.isErased && stroke.intersectsRect(eraseRect)) {
                stroke.isErased = true;
                RectF annoationBoundRect = stroke.getBoundingRect();
                if (eraserRefreshRect.isEmpty()) {
                    eraserRefreshRect.set(annoationBoundRect);
                    eraserOffscreenRect.set(annoationBoundRect);
                } else {
                    eraserRefreshRect.union(annoationBoundRect);
                    eraserOffscreenRect.union(annoationBoundRect);
                }
                toBeRemoved.add(stroke);
            }
        } else {
            int erasedSegCount = 0;
            ArrayList<Integer> erasedSegmentsInStroke = new ArrayList<>();
            int segCount = 0;
            ArrayList<FTSegment> segments = new ArrayList<>();

            synchronized (stroke) {
                segments = stroke.getSegments();
                segCount = stroke.segmentCount;
            }
            for (int i = 0; i < segCount; i++) {
                FTSegment segment = segments.get(i);
                RectF segmentBoundRect = segment.boundingRect;
                if (segment.isSegmentErased()) {
                    if (eraserRefreshRect.isEmpty()) {
                        eraserRefreshRect.set(segmentBoundRect);
                        eraserOffscreenRect.set(segmentBoundRect);
                    } else {
                        eraserRefreshRect.union(segmentBoundRect);
                        eraserOffscreenRect.union(segmentBoundRect);
                    }
                    erasedSegCount++;
                } else {
                    if (RectF.intersects(segmentBoundRect, eraseRect)) {
                        erasedSegmentsInStroke.add(i);
                        segment.setSegmentAsErased(true);
                        if (eraserRefreshRect.isEmpty()) {
                            eraserRefreshRect.set(segmentBoundRect);
                            eraserOffscreenRect.set(segmentBoundRect);
                        } else {
                            eraserRefreshRect.union(segmentBoundRect);
                            eraserOffscreenRect.union(segmentBoundRect);
                        }
                        erasedSegCount++;
                    }
                }
            }
            erasedSegmentsByStrokes.add(new ErasedSegmentDataObj(stroke, erasedSegmentsInStroke));
            if (erasedSegCount == stroke.segmentCount) {
                stroke.isErased = true;
                toBeRemoved.add(stroke);
            }
        }
    }
    //endregion


    private void addEraserSegToUndoManager() {
        if (erasedSegmentsByStrokes.size() > 0) {
            ArrayList<ErasedSegmentDataObj> erasedSegmentsByStrokes_temp = new ArrayList<>();
            for (int i = 0; i < erasedSegmentsByStrokes.size(); i++) {
                erasedSegmentsByStrokes_temp.add(erasedSegmentsByStrokes.get(i));
            }
            undoManager.addUndo(FTDocumentPageFragment.class, "UndoErasedSegments", 2, new Object[]{erasedSegmentsByStrokes_temp, false}, this);
//            ((FTDocumentActivity) getContext()).enableUndoButton();
            ObservingService.getInstance().postNotification(FTDocumentActivity.KEY_ENABLE_UNDO, true);
            erasedSegmentsByStrokes.clear();
        }
    }

    public void UndoErasedSegments(ArrayList<ErasedSegmentDataObj> erasedSegmentsData, boolean isErased) {
        undoManager.addUndo(FTDocumentPageFragment.class, "UndoErasedSegments", 2, new Object[]{erasedSegmentsData, !isErased}, this);
        RectF refreshRect = new RectF();
        for (int i = 0; i < erasedSegmentsData.size(); i++) {
            ErasedSegmentDataObj erasedSegmentDataObj = erasedSegmentsData.get(i);
            for (int j = 0; j < erasedSegmentDataObj.erasedSegmentIndexes.size(); j++) {
                FTSegment segment = erasedSegmentDataObj.ftStroke.getSegmentAtIndex(erasedSegmentDataObj.erasedSegmentIndexes.get(j));
                segment.setSegmentAsErased(isErased);
            }
            RectF annoationBoundRect = erasedSegmentDataObj.ftStroke.getBoundingRect();
            if (refreshRect.isEmpty()) {
                refreshRect.set(annoationBoundRect.left, annoationBoundRect.top, annoationBoundRect.right, annoationBoundRect.bottom);
            } else {
                refreshRect.union(annoationBoundRect.left, annoationBoundRect.top, annoationBoundRect.right, annoationBoundRect.bottom);
            }
        }
        reloadInRect(FTGeometryUtils.scaleRect(refreshRect, scale));
        currentPage().setPageDirty(true);
    }

    public void removeLassoRelatedViews() {
        if (mPageContentHolderView != null && mPageContentHolderView.getChildCount() > 4) {
            mPageContentHolderView.removeViews(5, mPageContentHolderView.getChildCount() - 5);
        }
    }

    class ErasedSegmentDataObj {
        public FTStrokeV1 ftStroke;
        public ArrayList<Integer> erasedSegmentIndexes;

        public ErasedSegmentDataObj(FTStrokeV1 ftStroke, ArrayList<Integer> erasedSegmentIndexes) {
            this.ftStroke = ftStroke;
            this.erasedSegmentIndexes = erasedSegmentIndexes;
        }
    }
    //endregion

    //region ImageMode
    private void initResizeImageView(FTAnnotation imageAnnotation) {
        if (isScroll)
            return;
        if (mPenPref.get(documentUid + PenRackPref.PEN_TOOL, -1) != FTToolBarTools.IMAGE.toInt())
            mPenPref.save(documentUid + PenRackPref.PEN_TOOL_OLD, mPenPref.get(documentUid + PenRackPref.PEN_TOOL, -1));
        mPenPref.save(documentUid + PenRackPref.PEN_TOOL, FTToolBarTools.IMAGE.toInt());
        actionsListener.refreshToolbarItems();
        if (mLassoFragment != null) {
            mLassoFragment.lassoCanvasOutsideTouch();
            getChildFragmentManager().beginTransaction().remove(mLassoFragment).commit();
            mLassoFragment = null;
        }
        mZoomLayout.setAllowZoom(false);
        addAnnotationFragment(imageAnnotation);
    }

    void setImageAnnotation(String bitmapUri, boolean isClipart) {
        setImageAnnotation(bitmapUri, isClipart, 0, 0);
    }

    private void setImageAnnotation(String bitmapUri, boolean isClipart, float eventX, float eventY) {
        if (bitmapUri != null && !bitmapUri.isEmpty()) {
            int rotation = FileUriUtils.getCapturedImageOrientation(getActivity(), Uri.parse(bitmapUri));
            Bitmap bitmap;
            try {
                if (isClipart) {
                    bitmap = BitmapUtil.getBitmap(Uri.parse(bitmapUri));
                } else {
                    bitmap = MediaStore.Images.Media.getBitmap(FTApp.getInstance().getCurActCtx().getContentResolver(), Uri.parse(bitmapUri));
                    if (rotation != 0) {
                        Matrix matrix = new Matrix();
                        matrix.postRotate(rotation);
                        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                    }
                }
                int maxWidth = (int) (MAX_IMAGE_WIDTH * getResources().getDisplayMetrics().density);
                int maxHeight = (int) (MAX_IMAGE_HEIGHT * getResources().getDisplayMetrics().density);
                int bitmapWidth = bitmap.getWidth(), bitmapHeight = bitmap.getHeight(), x, y;
                final int gcd = NumberUtils.gcd(bitmapWidth, bitmapHeight);
                x = bitmapWidth / gcd;
                y = bitmapHeight / gcd;
                if (bitmapWidth > maxWidth) {
                    bitmapWidth = maxWidth;
                    bitmapHeight = (int) (maxWidth / ((float) x / (float) y));
                    bitmap = BitmapUtil.getResizedBitmap(bitmap, bitmapWidth, bitmapHeight);
                } else if (bitmapHeight > maxHeight) {
                    bitmapWidth = (int) (maxHeight * ((float) x / (float) y));
                    bitmapHeight = maxHeight;
                    bitmap = BitmapUtil.getResizedBitmap(bitmap, bitmapWidth, bitmapHeight);
                }
                FTAnnotation ftAnnotation = !isClipart ? FTAnnotationV1.getImageAnnotation(getContext(), currentPage(), bitmap) : FTAnnotationV1.getImageAnnotationForClipart(getContext(), currentPage(), bitmap, scale);
                if (eventX > 0 && eventY > 0) {
                    eventX = Math.max(0, eventX - (bitmapWidth / 2));
                    eventY = Math.max(0, eventY - (bitmapHeight / 2));
                    ftAnnotation.setBoundingRect(new RectF(eventX, eventY, eventX, eventY));
                }
                initResizeImageView(ftAnnotation);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (SecurityException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public float getContainerScale() {
        return scale;
    }

    @Override
    public float getTextureScale() {
        if (this.previousZoom <= 1.5)
            return 1f;
        else if (this.previousZoom <= 3)
            return 1.5f;
        else
            return 2f;
    }

    @Override
    public float getOriginalScale() {
        return originalScale;
    }

    public FTDrawingView getDrawingView() {
        if (null != mWritingView) {
            return mWritingView.getDrawingView();
        }
        return null;
    }

    @Override
    public void processEventForAudio(MotionEvent motionEvent) {
        if (null != getDrawingView())
            getDrawingView().processTouchEvent(motionEvent);
    }

    @Override
    public void updateAnnotations(ArrayList<FTAnnotation> oldAnnotations, ArrayList<FTAnnotation> helperAnnotations, boolean refreshView) {
        if (getDrawingView() != null) {
            annotationManager.updateAnnotations(oldAnnotations, helperAnnotations, refreshView);
        }
    }

    @Override
    public void addAnnotation(FTAnnotation annotation) {
        ArrayList annotations = new ArrayList<FTAnnotation>();
        annotations.add(annotation);
        if (annotation instanceof FTTextAnnotation && isFromHW) {
            for (int i = 0; i < toBeRemovedAnnotations.size(); i++) {
                toBeRemovedAnnotations.get(i).hidden = false;
            }
            annotationManager.replaceAnnotations(toBeRemovedAnnotations, annotations);
        } else {
            if (null != getDrawingView())
                addAnnotations(annotations, true);
        }
        isFromHW = false;
    }

    @Override
    public void updateAnnotation(FTAnnotation oldAnnotation, FTAnnotation helperAnnotation) {
        if (null != getDrawingView()) {
            annotationManager.updateAnnotation(oldAnnotation, helperAnnotation, true);
        }

    }

    @Override
    public void removeAnnotation(FTAnnotation annotation) {
        if (annotation.annotationType() == FTAnnotationType.audio) {
            actionsListener.closeAudioToolbar();
        }
        ArrayList annotations = new ArrayList<FTAnnotation>();
        annotations.add(annotation);
        if (null != getDrawingView()) {
            removeAnnotations(annotations, true);
        }
    }

    //endregion

    @Override
    public void onAnnotationEditFinish() {
        new Handler(Looper.myLooper()).postDelayed(() -> {
            if (mZoomLayout == null || isDetached)
                return;
            mZoomLayout.setAllowZoom(true);
            lastCursorPoint = null;
            try {
                if (activeFragment != null) {
                    //Temporary fix to check if the fragment view is becoming null while deleting
                    getChildFragmentManager().beginTransaction().remove(activeFragment).commitAllowingStateLoss();
                    activeFragment = null;
                    mPageContentHolderView.removeView(annotationView);
                    pagerMainLayout.removeView(annotationView);
                    RelativeLayout parent = (RelativeLayout) pagerMainLayout.getParent();
                    parent.removeView(textToolbarView);
                    textToolbarView = null;
                }

                int toolType = mPenPref.get(documentUid + PenRackPref.PEN_TOOL, -1);
                if (toolType == FTToolBarTools.IMAGE.toInt()) {
                    mPenPref.save(documentUid + PenRackPref.PEN_TOOL, mPenPref.get(documentUid + PenRackPref.PEN_TOOL_OLD, -1));
                    actionsListener.refreshToolbarItems();
                }
                Object tag = mZoomLayout.getChildAt(mZoomLayout.getChildCount() - 1).getTag();
                if (tag != null && tag.equals(getString(R.string.tag_lasso_container))) {
                    mZoomLayout.setAllowZoom(false);
                }
            } catch (Exception e) {
                e.printStackTrace();
                FTLog.logCrashException(e);
            }
            canSwipePage = true;
        }, 100);
    }
    //endregion

    //region DrawingView callBacks
    @Override
    public void currentTextBoxCursorPosition(PointF pointF) {
        if (activeFragment == null)
            return;
        if (mKeyboardDimension <= 0) {
            new Handler().postDelayed(() -> currentTextBoxCursorPosition(pointF), 300);
            return;
        }
        lastCursorPoint = pointF;
        RectF visibleRect = getVisibleRect();
        try {
            float cursorPositionX = activeFragment.getView().getX() + pointF.x;
            float cursorPositionY = activeFragment.getView().getY() + pointF.y;
            float textPoX = cursorPositionX + visibleRect.left;
            float textPoY = cursorPositionY + visibleRect.top;
            float freeHeight = (getView().getHeight() - (mKeyboardDimension + mKeyboardOffset + getResources().getDimensionPixelOffset(R.dimen.new_48dp))) / previousZoom;
            int deltaY = 0;
            int deltaX = 0;
            if (visibleRect.bottom > freeHeight && textPoY > freeHeight) {
                deltaY = (int) (textPoY - freeHeight);
            } else if (visibleRect.top < 0 && textPoY < freeHeight) {
                deltaY = Math.max((int) getVisibleRect().top, (int) (textPoY - freeHeight));
            }

            if (visibleRect.right > getView().getWidth() && textPoX > getView().getWidth()) {
                deltaX = (int) (textPoX - getView().getWidth());
            } else if (textPoX < 0 && textPoX < getView().getWidth()) {
                deltaX = (int) textPoX;
            }

            if ((deltaY != 0 && visibleRect.top < deltaY) || (deltaX != 0 && visibleRect.left < deltaX)) {
                if (visibleRect.left > deltaX)
                    deltaX = 0;
                if (visibleRect.top > deltaY)
                    deltaY = 0;
                mZoomLayout.updatePositions(deltaX, deltaY);
                if (activeFragment instanceof FTEditTextFragment) {
                    ((FTEditTextFragment) activeFragment).onVisibleRectChanged();
                }
            }
        } catch (Exception e) {
            //Exception to catch if the activeFragment is null
        }
    }
    //endregion

    //region onLongPress
    public void onLongPress(final FTAnnotation annotation) {
        if (mZoomLayout.isTranslating() || activeFragment instanceof FTEditTextFragment || isScroll || !isVisibleToUser || FTDocumentActivity.SCROLL_STATE != ViewPager.SCROLL_STATE_IDLE) {
            return;
        }
        if (mLassoFragment != null) {
            mLassoFragment.onPause();
        }

        if (annotation.annotationType() == FTAnnotationType.text) {
            mPenPref.save(documentUid + PenRackPref.PEN_TOOL, FTToolBarTools.TEXT.toInt());
        }
        actionsListener.refreshToolbarItems();
        mZoomLayout.setAllowZoom(false);

        if (annotation.annotationType() == FTAnnotationType.image) {
            imgAnnotation = (FTImageAnnotationV1) annotation;
            if (imgAnnotation.getImageLockStatus() == 1) {
                popupView(annotation);
                refreshOffscreen(annotation.annotationType() == FTAnnotationType.image ? ((FTImageAnnotation) annotation).getRenderingRect() : annotation.getBoundingRect());
            } else {
                if (annotation.annotationType() == FTAnnotationType.image) {
                    if (mPenPref.get(documentUid + PenRackPref.PEN_TOOL, -1) != FTToolBarTools.IMAGE.toInt())
                        mPenPref.save(documentUid + PenRackPref.PEN_TOOL_OLD, mPenPref.get(documentUid + PenRackPref.PEN_TOOL, -1));
                    mPenPref.save(documentUid + PenRackPref.PEN_TOOL, FTToolBarTools.IMAGE.toInt());
                }
                annotation.hidden = true;
                annotationLoading(annotation);
            }
        } else {
            annotation.hidden = true;
            annotationLoading(annotation);
        }

    }

    private void annotationLoading(FTAnnotation annotation) {
        FTDrawingView drawingView = getDrawingView();
        final RectF boundingRect = FTGeometryUtils.scaleRect(annotation.getBoundingRect(), drawingView.scale);
        addAnnotationFragment(annotation);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (annotation.annotationType() == FTAnnotationType.image) {
                    drawingView.reloadInRect(FTGeometryUtils.scaleRect(((FTImageAnnotation) annotation).getRenderingRect(), drawingView.scale));
                } else {
                    drawingView.reloadInRect(boundingRect);
                }
            }
        }, 200);
    }

    public void popupView(FTAnnotation annotation) {

        RectF visibleRect = getVisibleRect();
        popupList = new FTPopupView(mPageContentHolderView.getContext());
        RectF mBoundingRect = FTGeometryUtils.scaleRect(((FTImageAnnotation) annotation).getRenderingRect(), getContainerScale());
        float IMAGE_CORD_X = Math.max(0, Math.min(visibleRect.width(), mBoundingRect.left + (mBoundingRect.right - mBoundingRect.left) / 2)) + visibleRect.left;
        float IMAGE_CORD_Y = Math.max(0, mBoundingRect.top + visibleRect.top);

        popupList.showPopupListWindow(mPageContentHolderView, 0, IMAGE_CORD_X,
                IMAGE_CORD_Y, popupMenuItemList, new FTPopupView.PopupListListener() {
                    @Override
                    public boolean showPopupList(View adapterView, View contextView, int contextPosition) {
                        isUnlockPopupShowing = true;
                        return true;
                    }

                    @SuppressLint("ClickableViewAccessibility")
                    @Override
                    public void onPopupListClick(View contextView, int contextPosition, int position) {
                        hideTool();
                        FTImageAnnotationV1 image = (FTImageAnnotationV1) annotation;
                        image.hidden = true;
                        image.setImageLockStatus(0);
                        annotationLoading(annotation);
                    }
                });
    }

    public void hideTool() {
        if (popupList != null) {
            if (isUnlockPopupShowing) {
                isUnlockPopupShowing = false;
                popupList.hidePopupListWindow();
            }
        }
    }

    @Override
    public RectF getContainerRect() {
        return currentPage().getPageRect();
    }

    @Override
    public RectF getRenderingPageRect() {
        if (mPageContentHolderView != null) {
            return mPageContentHolderView.getPageRect();
        }
        return currentPage().getPageRect();
    }

    //region User functions
    private void addAnnotationFragment(FTAnnotation ftAnnotation) {
        if (getContext() == null)
            return;
        clearStack();
        if (annotationView != null)
            mPageContentHolderView.removeView(annotationView);
        annotationView = new FrameLayout(requireContext());
        annotationView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        annotationView.setId(254647);

        activeFragment = FTAnnotationFragment.instance(ftAnnotation, this);
        if (ftAnnotation.annotationType() == FTAnnotationType.text)
            pagerMainLayout.addView(annotationView, pagerMainLayout.getChildCount());
        else
            mPageContentHolderView.addView(annotationView, mPageContentHolderView.getChildCount());
        Objects.requireNonNull(getChildFragmentManager()).beginTransaction().add(annotationView.getId(), activeFragment, "InptText").commitAllowingStateLoss();

        refreshOffscreen(ftAnnotation.annotationType() == FTAnnotationType.image ? ((FTImageAnnotation) ftAnnotation).getRenderingRect() : ftAnnotation.getBoundingRect());

        //text toolbar
        if (activeFragment instanceof FTKeyboardToolbarFragment.Callbacks) {
            int topMargin = mZoomLayout.getHeight() - textToolbarHeight;
            textToolbarView = new FrameLayout(getContext());
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, textToolbarHeight);
            textToolbarView.setLayoutParams(layoutParams);
            textToolbarView.setId(new Random().nextInt(50000));
            RelativeLayout parent = (RelativeLayout) pagerMainLayout.getParent();
            parent.addView(textToolbarView);
            textToolbarView.setY(topMargin);
            textToolbarView.post(() -> {
                if (activeFragment instanceof FTKeyboardToolbarFragment.Callbacks) {
                    FTKeyboardToolbarFragment toolBarFragment = ((FTKeyboardToolbarFragment.Callbacks) activeFragment).getToolBarFragment();
                    if (toolBarFragment != null && textToolbarView != null) {
                        getChildFragmentManager().beginTransaction().replace(textToolbarView.getId(), toolBarFragment, "toolbar").commitAllowingStateLoss();
                    }
                }
            });
        }
        canSwipePage = false;
    }

    synchronized void resetAnnotationFragment(View view) {
        int clickId = view == null ? 0 : view.getId();
        if (activeFragment != null) {
            activeFragment.outsideClick();
            if (activeFragment instanceof FTEditTextFragment)
                activeFragment.outsideClick();
        }

        if (mLassoFragment != null && clickId != R.id.doc_toolbar_lasso_image_view) {
            mLassoFragment.lassoCanvasOutsideTouch();
            if (clickId != R.id.doc_toolbar_undo_image_view) {
                getChildFragmentManager().beginTransaction().remove(mLassoFragment).commit();
                mLassoFragment = null;
            }
        }
        if (pagerMainLayout != null) {
            pagerMainLayout.setPadding(0, 0, 0, 0);
        }
        canSwipePage = true;
    }

    void clearAnnotation() {
        if (mAudioView != null) {
            mAudioView.outsideClick();
            mAudioView = null;
        }
    }

    void updateAudioViewPosition() {
        if (null == mPageContentHolderView)
            return;
        if (mAudioView != null) {
            mPageContentHolderView.removeView(annotationView);
            mPageContentHolderView.removeView(mAudioView);
            mPageContentHolderView.addView(mAudioView, mPageContentHolderView.getChildCount());
        }
        mZoomLayout.setAllowZoom(true);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (mZoomLayout != null) {
            mZoomLayout.setAllowZoom(true);
            mPageContentHolderView.visibleRect = null;
        }
        if (activeFragment instanceof FTEditTextFragment) {
            onOutsideTouch(FTToolBarTools.TEXT, null);
        } else if (mPenPref.get(documentUid + PenRackPref.PEN_TOOL, -1) == FTToolBarTools.IMAGE.toInt() && activeFragment != null) {
            activeFragment.outsideClick();
        }

        if (imgAnnotation != null) {
            if (isUnlockPopupShowing) {
                hideTool();
            }
        }

        isAllowLayoutChange = true;
    }

    synchronized void onKeyboardHeightChanged(int height) {
        if (height <= 0) {
            mKeyboardHeightOffset = Math.abs(height);
        }
        if (height > 0 && activeFragment instanceof FTEditTextFragment && mKeyboardDimension != height) {
            mKeyboardDimension = height;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R || ScreenUtil.hasNavBar(getContext(), getView())) {
                mKeyboardOffset = mKeyboardHeightOffset + textToolbarHeight;// + ScreenUtil.getNavigationBarHeight(getContext());
            } else {
                mKeyboardOffset = mKeyboardHeightOffset + textToolbarHeight + ScreenUtil.getNavigationBarHeight(getContext());
            }
            pagerMainLayout.setPadding(0, 0, 0, height + mKeyboardOffset);
            if (activeFragment instanceof FTEditTextFragment && null != activeFragment.getView() && null != getView()) {
                int textBoxPosition = ((int) activeFragment.getView().getY() + mKeyboardDimension + mKeyboardOffset);
                int diff = getView().getHeight() - textBoxPosition;
                if (diff < 0)
                    mZoomLayout.updatePositions(0, height + textToolbarHeight);
            }
            if (lastCursorPoint != null) {
                new Handler().postDelayed(() -> currentTextBoxCursorPosition(lastCursorPoint), 200);
            }
            mZoomLayout.post(() -> {
                if (textToolbarView != null) {
                    textToolbarView.post(() -> {
                        int topMargin = 0;
                        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
                            topMargin = mZoomLayout.getHeight();
                        else
                            topMargin = mZoomLayout.getHeight() - textToolbarHeight;
                        if (textToolbarView != null)
                            textToolbarView.setY(topMargin);
                    });
                } else if (activeFragment instanceof FTKeyboardToolbarFragment.Callbacks) {
                    int topMargin = mZoomLayout.getHeight();
                    textToolbarView = new FrameLayout(getContext());
                    RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, textToolbarHeight);
                    textToolbarView.setLayoutParams(layoutParams);
                    textToolbarView.setId(new Random().nextInt(50000));
                    RelativeLayout parent = (RelativeLayout) pagerMainLayout.getParent();
                    parent.addView(textToolbarView);
                    textToolbarView.setY(topMargin);
                    textToolbarView.post(() -> {
                        if (activeFragment instanceof FTKeyboardToolbarFragment.Callbacks) {
                            FTKeyboardToolbarFragment toolBarFragment = ((FTKeyboardToolbarFragment.Callbacks) activeFragment).getToolBarFragment();
                            if (toolBarFragment != null && textToolbarView != null) {
                                getChildFragmentManager().beginTransaction().replace(textToolbarView.getId(), toolBarFragment, "toolbar").commitAllowingStateLoss();
                            }
                        }
                    });
                }

                if (activeFragment instanceof FTEditTextFragment) {
                    ((FTEditTextFragment) activeFragment).onVisibleRectChanged();
                }
            });
        } else if (mKeyboardDimension > 0 && height <= 0) {
            if (pagerMainLayout != null) {
                pagerMainLayout.setPadding(0, 0, 0, 0);
                pagerMainLayout.post(new Runnable() {
                    @Override
                    public void run() {
                        setDrawingViewPosition(getVisibleRect().left, getVisibleRect().top, mPageContentHolderView.getWidth(), mPageContentHolderView.getHeight(), getVisibleRect(), false);
                    }
                });
            }
            mKeyboardDimension = 0;
            if (textToolbarView != null) {
                mZoomLayout.post(new Runnable() {
                    @Override
                    public void run() {
                        if (textToolbarView != null) {
                            synchronized (textToolbarView) {
                                int topMargin = mZoomLayout.getHeight() - textToolbarHeight;
                                textToolbarView.setY(topMargin);
                            }
                            if (activeFragment instanceof FTEditTextFragment) {
                                ((FTEditTextFragment) activeFragment).onVisibleRectChanged();
                            }
                        }
                    }
                });
            }
        }
    }

    private void onPageZoomChanged(float scale) {
        RectF drawRect = mZoomLayout.getDrawRect();
        isZoom = false;
        scrollViewDidEndZooming(scale, drawRect.left, drawRect.top);
        highlightSearchItemsInPage();
        isScroll = false;
    }

    private synchronized void scrollViewDidEndZooming(float zoomScale, float x, float y) {
        float currentZoom = previousZoom * zoomScale;
        if (previousZoom != currentZoom) {
            RectF visibleFrame = FTDocumentPageFragment.this.mZoomLayout.getDrawRect();
            if (isVisibleToUser)
                ((FTDocumentActivity) getContext()).setZoomScale(currentZoom);
            previousZoom = currentZoom;
            mZoomLayout.setMinScale(mZoomLayout.getMinScale() / zoomScale);
            mZoomLayout.setMaxScale(mZoomLayout.getMaxScale() / zoomScale);

            int width = (int) (mPageContentHolderView.getWidth() * (zoomScale));
            int height = (int) (mPageContentHolderView.getHeight() * (zoomScale));

            RectF pageRect = mNoteshelfPage.getPageRect();
            SizeF aspectSize = FTGeometryUtils.aspectSize(new SizeF(pageRect.width(), pageRect.height()), new SizeF(width, height));
            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) mPageContentHolderView.getLayoutParams();

            if (width < (int) aspectSize.getWidth()) {
                layoutParams.width = (int) aspectSize.getWidth();
                layoutParams.height = (int) aspectSize.getHeight();
                ((FTDocumentActivity) getContext()).setZoomScale(1);
                mZoomLayout.setMinScale(1.0f);
                mZoomLayout.setMaxScale(4.0f);
            } else {
                layoutParams.width = width;
                layoutParams.height = height;
            }

            mPageContentHolderView.setLayoutParams(layoutParams);

            this.scale = aspectSize.getWidth() / pageRect.width();
            mWritingView.setContentScale(this.scale);
            mPageContentHolderView.setContentScale(zoomScale);
            updateNameNPageNumber();
            if (isVisibleToUser) {
                getDrawingView().scale = this.scale;
                getDrawingView().reset();
            }
            setDrawingViewPosition(x, y, layoutParams.width, layoutParams.height, visibleFrame, true);

            //To update the audio icon size
            if (mAudioView != null) {
                mAudioView.onLayoutChanged();
            }
            if (mLassoFragment != null) {
                mLassoFragment.lassoCanvasOutsideTouch();
            }
            mZoomLayout.currentScale = this.scale;
        } else if (!isZoom) {
            RectF visibleFrame = FTDocumentPageFragment.this.mZoomLayout.getDrawRect();
            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) mPageContentHolderView.getLayoutParams();
            setDrawingViewPosition(x, y, layoutParams.width, layoutParams.height, visibleFrame, false);
        }
    }

    private synchronized void setDrawingViewPosition(float x, float y, float pageContentWidth, float pageContentHeight, RectF visibleFrame, boolean isScaled) {
        int width, height, leftMargin, topMargin;
        width = Math.min((int) visibleFrame.width(), mZoomLayout.getWidth());
        height = Math.min((int) visibleFrame.height(), mZoomLayout.getHeight());

        if (pageContentWidth > mZoomLayout.getWidth() && pageContentHeight > mZoomLayout.getHeight()) {
            leftMargin = -(int) visibleFrame.left;
            topMargin = -(int) visibleFrame.top;
        } else if (pageContentWidth > mZoomLayout.getWidth()) {
            leftMargin = -(int) visibleFrame.left;
            topMargin = 0;
        } else if (pageContentHeight > mZoomLayout.getHeight()) {
            leftMargin = 0;
            topMargin = -(int) visibleFrame.top;
        } else {
            leftMargin = 0;
            topMargin = 0;
        }
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) mWritingView.getLayoutParams();
        layoutParams.width = width;
        layoutParams.height = height;
        layoutParams.leftMargin = leftMargin;
        layoutParams.topMargin = topMargin;
        mWritingView.setLayoutParams(layoutParams);
        this.visibleFrame.set(leftMargin, topMargin, leftMargin + width, topMargin + height);
        if (isScaled) {
            mZoomLayout.resetToNormal();
            mZoomLayout.setContentOffset(Math.abs(x), Math.abs(y));
        }
        float currentTextureScale = getTextureScale();
        if (textureScale != currentTextureScale) {
            textureScale = currentTextureScale;
            mPageContentHolderView.requestNewPageBg = true;
        }
        if (isVisibleToUser)
            requestReload();
        if (!isZoom)
            mPageContentHolderView.refreshTiles(true, new RectF(leftMargin, topMargin, leftMargin + width, topMargin + height));
    }

    private synchronized void requestReload() {
        if (null != getDrawingView())
            getDrawingView().isRendering = true;
        new Handler().postDelayed(() -> {
            if (null != getDrawingView())
                getDrawingView().reloadInRect(visibleFrame());
        }, 0);
//        AsyncTask task = new AsyncTask() {
//            @Override
//            protected void onPreExecute() {
//                super.onPreExecute();
//                if (null != getDrawingView())
//                    getDrawingView().isRendering = true;
//            }
//
//            @Override
//            protected Object doInBackground(Object[] objects) {
//                return null;
//            }
//
//            @Override
//
//            protected void onPostExecute(Object o) {
//                super.onPostExecute(o);
//                if (null != getDrawingView())
//                    getDrawingView().reloadInRect(visibleFrame());
//            }
//        };
//        task.executeOnExecutor(task.SERIAL_EXECUTOR);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (activeFragment != null)
            activeFragment.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public SizeF contentSize() {
        return new SizeF(mPageContentHolderView.getWidth() * mZoomLayout.getScale(), mPageContentHolderView.getHeight() * mZoomLayout.getScale());
    }

    public RectF visibleFrame() {
        if (visibleFrame.width() == 0) {
            visibleFrame = new RectF();
            float x = mWritingView.getLeft();
            float y = mWritingView.getTop();
            float width = mWritingView.getWidth();
            float height = mWritingView.getHeight();
            visibleFrame.set(x, y, x + width, y + height);
        }
        return visibleFrame;
    }

    @Override
    public ArrayList<FTAnnotation> getPageAnnotations() {
        return currentPage().getPageAnnotations();
    }

    @Override
    public void onRenderComplete() {
        highlightSearchItemsInPage();
    }

    @Override
    public void onSingleTapped(MotionEvent motionEvent) {
        if (!isScroll && isVisibleToUser && FTDocumentActivity.SCROLL_STATE == ViewPager.SCROLL_STATE_IDLE) {
            FTAnnotation annotation = audioAnnotationUnderPoint(new PointF(motionEvent.getX(), motionEvent.getY()));
            if (annotation != null) {
                motionEvent.setAction(MotionEvent.ACTION_CANCEL);
                getDrawingView().processTouchEvent(motionEvent);
                FTAudioPlayer.getInstance().stopPlaying(getContext(), false);
                FTAudioPlayer.getInstance().stopRecording(getContext(), true);
                if (mAudioView != null && !annotation.uuid.equals(mAudioView.getCurrentUUID())) {
                    onOutsideTouch(FTToolBarTools.AUDIO, motionEvent);
                }
                addAudioView(annotation, null, false);
            }
            if ((!FTApp.getPref().get(SystemPref.HYPERLINKS_DISABLED, false) && mDocPref.get("haveLinks" + currentPage().uuid, true))
                    || (currentMode() != FTToolBarTools.PEN && currentMode() != FTToolBarTools.HIGHLIGHTER && currentMode() != FTToolBarTools.ERASER)) {
                applyHyperlinks(motionEvent.getX(), motionEvent.getY());
            }
        }

    }

    @Override
    public FTToolBarTools currentMode() {
        if (actionsListener != null) {
            return actionsListener.currentMode();
        } else {
            return FTToolBarTools.PEN;
        }
    }

    @Override
    public void setCurrentMode(FTToolBarTools tool) {
        actionsListener.updateToolbarMode(tool);
    }

    @Override
    public void selectedAnnotationsBringTOFront(ArrayList<FTAnnotation> selectedAnnotations) {
        int lastIndex = currentPage().getPageAnnotations().size();
        for (int i = 0; i < selectedAnnotations.size(); i++) {
            FTAnnotation annotation = selectedAnnotations.get(i);
            int index = currentPage().getPageAnnotations().indexOf(annotation);
            annotation.hidden = false;
            currentPage().getPageAnnotations().add(lastIndex, annotation);
            currentPage().getPageAnnotations().remove(index);
        }
        undoManager.addUndo(FTDocumentPageFragment.class, "selectedAnnotationsSendTOBack", 1, new Object[]{selectedAnnotations}, this);
        currentPage().setPageDirty(true);
        requestReload();
    }

    @Override
    public void selectedAnnotationsSendTOBack(ArrayList<FTAnnotation> selectedAnnotations) {
        for (int i = 0; i < selectedAnnotations.size(); i++) {
            FTAnnotation annotation = selectedAnnotations.get(i);
            int index = currentPage().getPageAnnotations().indexOf(annotation);
            annotation.hidden = false;
            currentPage().getPageAnnotations().remove(index);
            currentPage().getPageAnnotations().add(i, annotation);
        }
        undoManager.addUndo(FTDocumentPageFragment.class, "selectedAnnotationsBringTOFront", 1, new Object[]{selectedAnnotations}, this);
        currentPage().setPageDirty(true);
        requestReload();
    }

    @Override
    public void refreshOffscreen(RectF rect) {
        if (rect != null) {
            mPageContentHolderView.refreshTiles(FTGeometryUtils.scaleRect(rect, getContainerScale()));
        }
    }

    public RectF getVisibleRect() {
        return mZoomLayout.getDrawRect();
    }

    void clearPageAnnotations() {
        if (annotationManager != null) {
            annotationManager.clearPageAnnotations(currentPage().getPageAnnotations());
        }
    }

    public void reloadInRect(RectF rect) {
        if (getDrawingView() != null)
            getDrawingView().reloadInRect(rect);
    }

    @NonNull
    @Override
    public UndoManager currentUndoManager() {
        return this.undoManager;
    }

    private void removeDrawingView() {
        mWritingView.setVisibility(View.INVISIBLE);
        ((FTDocumentActivity) getContext()).removeDrawingView();
    }

    //Search observer
    private Observer searchObserver = (observable, object) -> {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                FTNoteshelfPage searchPage = (FTNoteshelfPage) object;
                if (currentPage() != null && searchPage.uuid.equals(currentPage().uuid) && !searchPage.getSearchableItems().isEmpty()) {
                    mNoteshelfPage = searchPage;
                    new Handler().postDelayed(() -> {
                        if (getDrawingView() != null) {
                            highlightSearchItemsInPage();
                        }
                    }, 100);
                }
            });
        }
    };

    private void highlightSearchItemsInPage() {
        if (mTextHighlighterLayout != null && currentPage() != null) {
            mTextHighlighterLayout.removeAllViews();
            if (currentPage().getSearchableItems() != null && !currentPage().getSearchableItems().isEmpty()) {
                FTTextHighlightView.ParentRectInfo parentRectInfo = new FTTextHighlightView.ParentRectInfo();
                parentRectInfo.scaleFactor = this.scale;
                for (FTSearchableItem searchableItem : currentPage().getSearchableItems()) {
                    parentRectInfo.width = mPageContentHolderView.getWidth();
                    parentRectInfo.height = mPageContentHolderView.getHeight();
                    parentRectInfo.rotation = searchableItem.getRotation();
                    mTextHighlighterLayout.addChildView(searchableItem.getBoundingRect(), parentRectInfo);
                }
            }
        }
    }

    private void applyHyperlinks(float x, float y) {
        if (hyperLinks == null && (asyncTaskHyperlinks == null || asyncTaskHyperlinks.getStatus() != AsyncTask.Status.RUNNING)) {
            asyncTaskHyperlinks = new GetHyperLinksTask(x, y);
            asyncTaskHyperlinks.execute();
        } else if (hyperLinks != null && !hyperLinks.isEmpty()) {
            for (Link link : hyperLinks) {
                RectF requiredRect = link.getBounds();
                requiredRect = FTGeometryUtils.scaleRect(requiredRect, getDrawingView().scale);
                if (requiredRect.contains(x, y)) {
                    if (!TextUtils.isEmpty(link.getUri())) {
                        actionsListener.performURLAction(link.getUri());
                    } else if (link.getDestPageIdx() != null && link.getDestPageIdx() >= 0) {
                        actionsListener.performGotoAction(link.getDestPageIdx());
                    }
                    break;
                }
            }
        }
    }

    private class GetHyperLinksTask extends AsyncTask<Void, Void, Void> {
        private final float x;
        private final float y;

        public GetHyperLinksTask(float x, float y) {
            this.x = x;
            this.y = y;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            hyperLinks = currentPage().getHyperlinks();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (hyperLinks != null) {
                if (hyperLinks.size() <= 0) {
                    mDocPref.save("haveLinks" + currentPage().uuid, false);
                } else if (!isDetached) {
                    applyHyperlinks(x, y);
                }
            }
        }
    }

    void removeHighlighters() {
        mTextHighlighterLayout.removeAllViews();
    }
    //endregion

    public void setOnMouseWheelScrollEvent(MotionEvent event) {
        mZoomLayout.setOnMouseWheelScroll(event);
    }

    public void setOnSpenKeyEvent(float scale) {
        showZoomLock();
        if (!isZoomLocked && ((scale < 1 && previousZoom > 1) || (scale > 1 && previousZoom < 4)))
            mZoomLayout.setOnSpenKeyEvent(scale);
    }

    //region annotation processing
    @Override
    public boolean canProcessTouchDownMotionEventForLongTap(MotionEvent motionEvent) {
        boolean shouldAccept = false;
        int count = motionEvent.getPointerCount();
        if (count == 1) {
            for (int i = 0; i < count; i++) {
                if (motionEvent.getToolType(i) == MotionEvent.TOOL_TYPE_FINGER) {
                    shouldAccept = true;
                    break;
                }
            }

            if (shouldAccept) {
                PointF p = new PointF(motionEvent.getX(), motionEvent.getY());
                shouldAccept = (null != annotationUnderPoint(p, false));

                /*
                 * on Touch outside of Image annotation hiding "Unlock" popup view
                 * */
                if (imgAnnotation != null) {
                    hideTool();
                    refreshOffscreen(imgAnnotation.annotationType() == FTAnnotationType.image ? ((FTImageAnnotation) imgAnnotation).getRenderingRect() : imgAnnotation.getBoundingRect());
                }
            }
        }
        return shouldAccept;
    }

    @Override
    public int bgTexture() {
        return mPageContentHolderView.getBGTexturre();
    }

    public void addCurrentStrokeAnnotation(FTStroke stroke) {
        if (new PenRackPref().init(PenRackPref.PREF_NAME).get(documentUid + "_is_shape_selected", false)) {
            FTStroke shapeStroke = getDrawingView().shapeDetectedStroke().get(0);
            if (shapeStroke.getBoundingRect() != null) {
                ArrayList<FTAnnotation> strokes = new ArrayList<>();
                strokes.add(shapeStroke);
                addAnnotations(strokes, false);

                RectF oldRect = new RectF();
                oldRect.set(stroke.getBoundingRect());
                oldRect.union(shapeStroke.getBoundingRect());
                oldRect = FTGeometryUtils.scaleRect(oldRect, scale);

                oldRect = FTGeometryUtils.integralRect(oldRect);
                reloadInRect(oldRect);
            } else {
                ArrayList<FTAnnotation> strokes = new ArrayList<>();
                strokes.add(stroke);
                addAnnotations(strokes, false);
            }
        } else {
            ArrayList<FTAnnotation> strokes = new ArrayList<>();
            strokes.add(stroke);
            addAnnotations(strokes, false);
        }
        new Handler().postDelayed(() -> refreshOffscreen(stroke.getBoundingRect()), 100);
        this.currentPage().setPageDirty(true);
    }

    private FTAnnotation annotationUnderPoint(PointF point, boolean onlyText) {
        if (null == currentPage())
            return null;
        ArrayList<FTAnnotation> pageAnnotations = currentPage().getPageAnnotations();
        FTAnnotation annotationToReturn = null;
        int count = pageAnnotations.size();

        ArrayList<FTAnnotationType> annotationTypes = new ArrayList<>();
        annotationTypes.add(FTAnnotationType.text);
        if (!onlyText) {
            annotationTypes.add(FTAnnotationType.image);
//            annotationTypes.add(FTAnnotationType.audio);
        }

        int i = count;
        while (i > 0) {
            FTAnnotation annotation = pageAnnotations.get(i - 1);
            FTAnnotationType annotationType = annotation.annotationType();
            if (annotationTypes.contains(annotationType)) {
                RectF scaledBoudingRect = FTGeometryUtils.scaleRect(annotation.getBoundingRect(), scale);
                if (scaledBoudingRect.contains(point.x, point.y)) {
                    annotationToReturn = annotation;
                    break;
                }
            }
            i--;
        }
        return annotationToReturn;
    }

    private FTAnnotation audioAnnotationUnderPoint(PointF point) {
        ArrayList<FTAnnotation> pageAnnotations = currentPage().getPageAnnotations();
        FTAnnotation annotationToReturn = null;
        int count = pageAnnotations.size();

        ArrayList<FTAnnotationType> annotationTypes = new ArrayList<>();
        annotationTypes.add(FTAnnotationType.audio);

        int i = count;
        while (i > 0) {
            FTAnnotation annotation = pageAnnotations.get(i - 1);
            FTAnnotationType annotationType = annotation.annotationType();
            if (annotationTypes.contains(annotationType)) {
                RectF scaledBoudingRect = FTGeometryUtils.scaleRect(annotation.getBoundingRect(), scale);
                if (scaledBoudingRect.contains(point.x, point.y)) {
                    annotationToReturn = annotation;
                    break;
                }
            }
            i--;
        }
        return annotationToReturn;
    }
    //endregion

    //region Zoom Lock UI
    private void configureZoomLock() {
        zoomLockLayout.setVisibility(View.GONE);
        zoomLockLayout.setmZoomLockCallbacks(new FTZoomLockView.ZoomLockCallbacks() {
            @Override
            public Size getContainerSize() {
                return new Size(getView().getWidth(), getView().getHeight());
            }

            @Override
            public void onPositionChange(Point point) {
                setZoomLockVisibility();
                mDocPref.save("zoomLockPosition", (int) point.x + "," + (int) point.y);
            }

            @Override
            public void isDragMode(boolean isInDrag) {
                isZoomLockInTouchMode = isInDrag;
            }

            @Override
            public void onClick(View view) {
                lockButton.setImageResource(isZoomLocked ? R.drawable.lock_off : R.drawable.lock_on);
                isZoomLocked = !isZoomLocked;

                if (mZoomLayout != null) {
                    mZoomLayout.setZoomLocked(isZoomLocked);
                }
            }
        });
    }

    private void showZoomLock() {
        zoomLockLayout.setVisibility(View.VISIBLE);
        setZoomLockPosition();
        setZoomLockVisibility();
    }

    private void setZoomLockVisibility() {
        if (mRunnable != null) {
            mHandler.removeCallbacks(mRunnable);
        }
        mRunnable = () -> {
            if (!isZoom && zoomLockLayout.getVisibility() == View.VISIBLE) {
                zoomLockLayout.setVisibility(View.GONE);
            }
        };
        mHandler.postDelayed(mRunnable, 4000);
    }

    private void setZoomLockPosition() {
        if (getContext() == null)
            return;
        String zoomLockPosition = mDocPref.get("zoomLockPosition", "");
        if (mDocPref.get("zoomLockPosition", "").isEmpty()) {
            zoomLockLayout.setPosition(new Point((int) (ScreenUtil.getScreenWidth(getContext()) / 2 - (getResources().getDimension(R.dimen._114dp) / 2)), (int) getResources().getDimension(R.dimen._60dp)));
        } else {
            String[] values = zoomLockPosition.split(",");
            try {
                zoomLockLayout.setPosition(new Point(Integer.parseInt(values[0].trim()), Integer.parseInt(values[1].trim())));
            } catch (Exception e) {
                e.printStackTrace();
                zoomLockLayout.setPosition(new Point((int) (ScreenUtil.getScreenWidth(getContext()) / 2 - (getResources().getDimension(R.dimen._114dp) / 2)), (int) getResources().getDimension(R.dimen._60dp)));
            }
        }
    }

    private void clearStack() {
        if (childFragmentManager.getBackStackEntryCount() > 0) {
            for (int i = 0; i < childFragmentManager.getBackStackEntryCount(); i++) {
                childFragmentManager.popBackStack();
            }
        }

        if (childFragmentManager.getFragments().size() > 0) {
            for (int i = 0; i < childFragmentManager.getFragments().size(); i++) {
                Fragment mFragment = childFragmentManager.getFragments().get(i);
                if (mFragment != null) {
                    childFragmentManager.beginTransaction().remove(mFragment).commit();
                }
            }
        }
    }

    public boolean canSwipePage() {
        if (currentMode() == FTToolBarTools.LASSO) {
            return !isZoomLockInTouchMode && FTApp.getPref().isStylusEnabled();
        } else {
            return currentMode() == FTToolBarTools.VIEW || (!isZoomLockInTouchMode && canSwipePage);
        }
    }

    public View lastSelectedViewInToolBar(View mView) {
        mLastSelectedView = mView;
        return mLastSelectedView;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof FTDocumentPageFragment.FTEraseEndedListener) {
            mFTEraseEndedListener = (FTDocumentPageFragment.FTEraseEndedListener) context;
        } else {
            throw new FTRuntimeException(context.toString() + " must implement FTEraseEndedListener");
        }
    }

    public interface FTEraseEndedListener {
        void eraserEnded();
    }

    public interface FTEraseCompletionBlock {
        void didFinishErasing();
    }
//endregion

    public interface ZoomTouchListener {
        void onTouch(MotionEvent event);

        void onOutsideTouch(MotionEvent event);
    }

    public interface PagerToActivityCallBack {
        void clearPageAnnotations();

        void performGotoAction(int pageIndex);

        void performURLAction(String uri);

        FTNoteshelfPage getCurrentPage();

        FTToolBarTools currentMode();

        int getCurrentItemPosition();

        FTBasePref getDocPref();

        void onLoadingFinished();

        void refreshFavView(boolean isFromFavWidToolbar);

        void refreshToolbarItems();

        void closeAudioToolbar();

        FTNoteshelfPage noteshelfPage(int pageIndex);

        void updateToolbarMode(FTToolBarTools tool);

        void updateToolBarModeToLastSelected();
    }
}