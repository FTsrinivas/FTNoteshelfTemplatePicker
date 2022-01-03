package com.fluidtouch.noteshelf.document;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.widget.TooltipCompat;
import androidx.fragment.app.Fragment;

import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf.FTRuntimeException;
import com.fluidtouch.noteshelf.audio.player.FTAudioPlayer;
import com.fluidtouch.noteshelf.commons.FTLog;
import com.fluidtouch.noteshelf.commons.utils.DrawableUtil;
import com.fluidtouch.noteshelf.document.dialogs.addnew.FTAddNewPopup;
import com.fluidtouch.noteshelf.document.enums.FTToolBarTools;
import com.fluidtouch.noteshelf.preferences.PenRackPref;
import com.fluidtouch.noteshelf.preferences.SystemPref;
import com.fluidtouch.noteshelf.services.FTFirebaseAnalytics;
import com.fluidtouch.noteshelf2.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FTDocumentToolbarFragment extends Fragment implements View.OnClickListener {
    //region Binding variables
    @BindView(R.id.doc_toolbar_undo_image_view)
    public ImageView mImgUndo;
    //    @BindView(R.id.document_custom_toolbar_undo_image_view2)
    public ImageView mImgUndo2;
    public ImageView mPenViewMobile;
    @BindView(R.id.doc_toolbar_back_image_view)
    ImageView mImgBack;
    @BindView(R.id.doc_toolbar_pen_on_image_view)
    RelativeLayout mLayPen;
    @BindView(R.id.doc_toolbar_bluetooth_image_view)
    ImageView mBluetoothImageView;
    @BindView(R.id.doc_toolbar_highlighter_layout)
    RelativeLayout mLayHighlighter;
    @BindView(R.id.doc_toolbar_pen_on)
    ImageView mImgPen;
    @BindView(R.id.doc_toolbar_pen_on_color)
    ImageView mImgPenColor;
    @BindView(R.id.doc_toolbar_highlighter_image_view)
    ImageView mImgHighlighter;
    @BindView(R.id.doc_toolbar_highlighter_color)
    ImageView mImgHighlighterColor;
    @BindView(R.id.doc_toolbar_eraser_image_view)
    ImageView mImgEraser;
    @BindView(R.id.doc_toolbar_text_image_view)
    ImageView mImgText;
    @BindView(R.id.doc_toolbar_pdfview_image_view)
    ImageView mImgPdf;
    @BindView(R.id.doc_toolbar_add_white_image_view)
    ImageView mImgAdd;
    @BindView(R.id.doc_toolbar_lasso_image_view)
    ImageView mLasso;
    @BindView(R.id.doc_toolbar_shape_image_view)
    ImageView mImgShape;
    @BindView(R.id.doc_toolbar_share_image_view)
    ImageView mImgShare;
    @BindView(R.id.doc_toolbar_finder_image_view)
    ImageView mImgFinder;
    @BindView(R.id.doc_toolbar_settings_image_view)
    ImageView mImgSettings;

    LinearLayout layPen;
    LinearLayout layBack;

    //endregion
    Toolbar toolbar;
    LayoutInflater inflater;
    //region Member variables
    private View mLastSelected;
    private View mToolBarItemLastSelected;
    private View mCustomToolbarView;
    private PenRackPref mPenPref;
    private DocumentToolbarFragmentInteractionListener mFragmentListener;
    //endregion
    private FTToolBarTools mCurrentMode = FTToolBarTools.PEN;
    FTToolbarMode currentToolbarMode = FTToolbarMode.ENABLE;
    private String lastSelectedToolKey;
    private String lastSelectedToolOldKey;

    public void onEraserEnded() {
        //Deselect Eraser mode
        mImgEraser.setImageResource(R.drawable.eraser);

        //Selecting Last selected Toll View
        mPenPref.save(lastSelectedToolKey, mPenPref.get(lastSelectedToolOldKey, FTToolBarTools.PEN.toInt()));
        updateLastSelectedToolView(true);
    }

    public void updateToolBarMode(FTToolBarTools tool) {
        updateLastSelectedToolView(false);
        if (mPenPref.get(lastSelectedToolKey, FTToolBarTools.PEN.toInt()) != FTToolBarTools.ERASER.toInt()) {
            mPenPref.save(lastSelectedToolOldKey, mPenPref.get(lastSelectedToolKey, FTToolBarTools.PEN.toInt()));
        }
        mPenPref.save(lastSelectedToolKey, tool.toInt());
        updateLastSelectedToolView(true);
    }

    public void updateToolBarModeToLastSelected() {
        updateLastSelectedToolView(false);
        mPenPref.save(lastSelectedToolKey, mPenPref.get(lastSelectedToolOldKey, FTToolBarTools.PEN.toInt()));
        updateLastSelectedToolView(true);
    }

    enum FTToolbarMode {
        ENABLE, DISABLE
    }

    //endregion
    public FTToolBarTools currentMode() {
        return mCurrentMode;
    }

    //region Fragment Lifecycle Methods
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setRetainInstance(true);
        return inflater.inflate(R.layout.fragment_document_toolbar, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        toolbar = view.findViewById(R.id.toolbar);
        inflater = (LayoutInflater) requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        lastSelectedToolKey = mFragmentListener.getDocUid() + PenRackPref.PEN_TOOL;
        lastSelectedToolOldKey = mFragmentListener.getDocUid() + PenRackPref.PEN_TOOL_OLD;
        if (inflater != null) {
            initViews(R.layout.document_custom_toolbar);
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof DocumentToolbarFragmentInteractionListener) {
            mFragmentListener = (DocumentToolbarFragmentInteractionListener) context;
        } else {
            throw new FTRuntimeException(context.toString() + " must implement DocumentToolbarFragmentInteractionListener");
        }
    }

    public void initViews(int layout) {
        toolbar.removeView(mCustomToolbarView);
        toolbar.invalidate();
        mCustomToolbarView = inflater.inflate(layout, new LinearLayout(getContext()), false);
        toolbar.addView(mCustomToolbarView, 0);
        toolbar.setContentInsetsAbsolute(0, 0);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);
        mFragmentListener.setUpToolbarTheme();
        mPenPref = new PenRackPref().init(PenRackPref.PREF_NAME);
        ButterKnife.bind(this, toolbar);

        int screenSize = getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;
        int orientation = getResources().getConfiguration().orientation;
        if ((screenSize == Configuration.SCREENLAYOUT_SIZE_NORMAL && orientation == Configuration.ORIENTATION_PORTRAIT) || screenSize == Configuration.SCREENLAYOUT_SIZE_SMALL) {
            mPenPref.save(lastSelectedToolKey, FTToolBarTools.VIEW.toInt());
            toolbar.findViewById(R.id.txtDone).setOnClickListener(this);
            mPenViewMobile = toolbar.findViewById(R.id.document_custom_toolbar_pen_view);
            mImgUndo2 = toolbar.findViewById(R.id.document_custom_toolbar_undo_image_view2);
            layPen = toolbar.findViewById(R.id.layPen);
            layBack = toolbar.findViewById(R.id.doc_toolbar_left_options_layout);
            mToolBarItemLastSelected = mPenViewMobile;
            if (mPenPref.get(lastSelectedToolOldKey, -1) == FTToolBarTools.HIGHLIGHTER.toInt()) {
                mPenViewMobile.setImageResource(R.drawable.highlighter);
            }
            mPenViewMobile.setOnClickListener(this);
            mImgUndo2.setEnabled(false);
            mImgUndo2.setAlpha(0.3f);
            mImgUndo2.setOnLongClickListener(v -> {
                mFragmentListener.undoLastChange(true);
                return true;
            });
            mImgUndo2.setOnClickListener(view -> {
                onToolbarItemClick(view);
                mFragmentListener.undoLastChange(false);
            });
            boolean isStylusEnabled = FTApp.getPref().isStylusEnabled();
            if (isStylusEnabled || !FTApp.getPref().hasKey(SystemPref.STYLUS_ENABLED)) {
                FTApp.getPref().saveStylusEnabled(isStylusEnabled);
                mPenViewMobile.performClick();
            }
            TooltipCompat.setTooltipText(mPenViewMobile, getString(R.string.pen_tools));
            TooltipCompat.setTooltipText(mImgUndo2, getString(R.string.undo));
        }
        setUpActions();
        mImgUndo.setEnabled(false);
        mImgUndo.setAlpha(0.3f);

        setUpToolbar(currentToolbarMode);
        setToolTipActions();
    }

    private void setToolTipActions() {
        TooltipCompat.setTooltipText(mImgBack, getString(R.string.back));
        TooltipCompat.setTooltipText(mImgAdd, getString(R.string.add));
        TooltipCompat.setTooltipText(mImgUndo, getString(R.string.undo));
        TooltipCompat.setTooltipText(mLayPen, getString(R.string.pens));
        TooltipCompat.setTooltipText(mLayHighlighter, getString(R.string.highlighters));
        TooltipCompat.setTooltipText(mImgEraser, getString(R.string.eraser));
        TooltipCompat.setTooltipText(mImgText, getString(R.string.text));
        TooltipCompat.setTooltipText(mLasso, getString(R.string.lasso));
        TooltipCompat.setTooltipText(mImgPdf, getString(R.string.view));
        TooltipCompat.setTooltipText(mImgShape, getString(R.string.shape));
        TooltipCompat.setTooltipText(mImgShare, getString(R.string.share));
        TooltipCompat.setTooltipText(mImgFinder, getString(R.string.finder));
        TooltipCompat.setTooltipText(mImgSettings, getString(R.string.settings));
    }
    //endregion

    @OnClick({R.id.doc_toolbar_back_image_view, R.id.doc_toolbar_lasso_image_view, R.id.doc_toolbar_undo_image_view, R.id.doc_toolbar_pen_on_image_view,
            R.id.doc_toolbar_highlighter_layout, R.id.doc_toolbar_eraser_image_view, R.id.doc_toolbar_text_image_view, R.id.doc_toolbar_pdfview_image_view,
            R.id.doc_toolbar_add_white_image_view, R.id.doc_toolbar_shape_image_view, R.id.doc_toolbar_finder_image_view, R.id.doc_toolbar_share_image_view,
            R.id.doc_toolbar_settings_image_view})
    void onClickToolBarItem(View view) {
        logSelection(view);
        if (view.getId() == R.id.doc_toolbar_back_image_view) {
            navigateBack(view);
        } else {
            if (view.getId() != R.id.doc_toolbar_shape_image_view) {
                onToolbarItemClick(view);
            }
            if (isNoteshelfTool(view)) {
                mLastSelected = view;
                updateLastSelectedToolView(false);
                if (view.getId() != R.id.doc_toolbar_eraser_image_view) {
                    mToolBarItemLastSelected = view;
                }
            }

            if (view.getId() == R.id.doc_toolbar_add_white_image_view) {
                new FTAddNewPopup().show(getChildFragmentManager());
                mFragmentListener.clearAnnotation();
            } else if (view.getId() == R.id.doc_toolbar_undo_image_view) {
                mFragmentListener.undoLastChange(false);
            } else if (view.getId() == R.id.doc_toolbar_pen_on_image_view) {
                if (mCurrentMode == FTToolBarTools.PEN) {
                    mFragmentListener.onToolDoubleTapped(FTToolBarTools.PEN);
                }
                mPenPref.save(lastSelectedToolKey, FTToolBarTools.PEN.toInt());
                mCurrentMode = FTToolBarTools.PEN;
            } else if (view.getId() == R.id.doc_toolbar_highlighter_layout) {
                if (mCurrentMode == FTToolBarTools.HIGHLIGHTER) {
                    mFragmentListener.onToolDoubleTapped(FTToolBarTools.HIGHLIGHTER);
                }
                mCurrentMode = FTToolBarTools.HIGHLIGHTER;
                mPenPref.save(lastSelectedToolKey, FTToolBarTools.HIGHLIGHTER.toInt());
            } else if (view.getId() == R.id.doc_toolbar_eraser_image_view) {
                if (mCurrentMode == FTToolBarTools.ERASER) {
                    mFragmentListener.onToolDoubleTapped(FTToolBarTools.ERASER);
                } else {
                    mPenPref.save(lastSelectedToolOldKey, mPenPref.get(lastSelectedToolKey, -1));
                }
                mFragmentListener.lastSelectedViewInToolBar(mToolBarItemLastSelected);

                mPenPref.save(lastSelectedToolKey, FTToolBarTools.ERASER.toInt());
                mCurrentMode = FTToolBarTools.ERASER;
            } else if (view.getId() == R.id.doc_toolbar_text_image_view) {
                mPenPref.save(lastSelectedToolKey, FTToolBarTools.TEXT.toInt());
                mCurrentMode = FTToolBarTools.TEXT;
            } else if (view.getId() == R.id.doc_toolbar_lasso_image_view) {
                mPenPref.save(lastSelectedToolOldKey, mPenPref.get(lastSelectedToolKey, -1));
                mPenPref.save(lastSelectedToolKey, FTToolBarTools.LASSO.toInt());
                mCurrentMode = FTToolBarTools.LASSO;
                mFragmentListener.enableLassoMode(view);
            } else if (view.getId() == R.id.doc_toolbar_pdfview_image_view) {
                mPenPref.save(lastSelectedToolKey, FTToolBarTools.VIEW.toInt());
                mCurrentMode = FTToolBarTools.VIEW;
            } else if (view.getId() == R.id.doc_toolbar_shape_image_view) {
                ImageView img = (ImageView) view;
                if (mPenPref.get(mFragmentListener.getDocUid() + "_is_shape_selected", false)) {
                    FTLog.crashlyticsLog("DocToolbar: Disabled Shapes");
                    mFragmentListener.shapeEnabledIDAndStatusInCurrentDoc(mFragmentListener.getDocUid() + "_is_shape_selected", false);
                    img.setImageResource(R.drawable.shape);
                } else {
                    FTLog.crashlyticsLog("DocToolbar: Enabled Shapes");
                    mFragmentListener.shapeEnabledIDAndStatusInCurrentDoc(mFragmentListener.getDocUid() + "_is_shape_selected", true);
                    img.setImageResource(R.drawable.shape_on);
                }
            } else if (view.getId() == R.id.doc_toolbar_share_image_view) {
                if (mFragmentListener != null)
                    mFragmentListener.share(view);
            } else if (view.getId() == R.id.doc_toolbar_finder_image_view) {
                if (mFragmentListener != null) {
                    mFragmentListener.showThumbnails(false);
                    mFragmentListener.clearAnnotation();
                }
            } else if (view.getId() == R.id.doc_toolbar_settings_image_view) {
                if (mFragmentListener != null) mFragmentListener.showNotebookOptions();
            }
            updateLastSelectedToolView(true);
            mFragmentListener.toolBarItemsClicked();
        }
    }

    private boolean isNoteshelfTool(View view) {
        return view.getId() == R.id.doc_toolbar_lasso_image_view || view.getId() == R.id.doc_toolbar_pen_on_image_view || view.getId() == R.id.doc_toolbar_highlighter_layout
                || view.getId() == R.id.doc_toolbar_eraser_image_view || view.getId() == R.id.doc_toolbar_text_image_view || view.getId() == R.id.doc_toolbar_pdfview_image_view;
    }

    //region Helper methods
    void enableUndo(boolean isEnabled) {
        if (getContext() != null) {
            mImgUndo.setEnabled(isEnabled);
            mImgUndo.setAlpha(isEnabled ? 1.0f : 0.3f);
            if (mImgUndo2 != null) {
                mImgUndo2.setEnabled(isEnabled);
                mImgUndo2.setAlpha(isEnabled ? 1.0f : 0.3f);
            }
        }
    }

    private void logSelection(View view) {
        String eventName = "";
        String logValue = "";
        switch (view.getId()) {
            case R.id.doc_toolbar_pen_on_image_view:
                eventName = "TapPen";
                logValue = "DocToolbar: Pen";
                break;
            case R.id.doc_toolbar_highlighter_layout:
                eventName = "TapHighlighter";
                logValue = "DocToolbar: Highlighter";
                break;
            case R.id.doc_toolbar_eraser_image_view:
                eventName = "TapEraser";
                logValue = "DocToolbar: Eraser";
                break;
            case R.id.doc_toolbar_text_image_view:
                eventName = "TapTextBox";
                logValue = "DocToolbar: TextBox";
                break;
            case R.id.doc_toolbar_lasso_image_view:
                eventName = "TapLasso";
                break;
            case R.id.doc_toolbar_pdfview_image_view:
                eventName = "TapReadOnly";
                logValue = "DocToolbar: ReadOnly";
                break;
            case R.id.doc_toolbar_add_white_image_view:
                eventName = "NB_AddNew";
                logValue = "DocToolbar: AddNew";
                break;
            case R.id.doc_toolbar_undo_image_view:
                eventName = "TapUndo";
                break;
            case R.id.doc_toolbar_shape_image_view:
                eventName = "TapShapes";
                break;
            case R.id.doc_toolbar_share_image_view:
                eventName = "TapShare";
                logValue = "DocToolbar: Share";
                break;
            case R.id.doc_toolbar_finder_image_view:
                eventName = "TapFinder";
                logValue = "DocToolbar: Finder";
                break;
            case R.id.doc_toolbar_settings_image_view:
                eventName = "NB_Options";
                logValue = "DocToolbar: NB_Options";
                break;
        }
        FTFirebaseAnalytics.logEvent(eventName);
        if (!logValue.equals("")) {
            FTLog.crashlyticsLog(logValue);
        }
    }

    @OnClick(R.id.doc_toolbar_back_image_view)
    void navigateBack(View view) {
        if (FTAudioPlayer.getInstance().isRecording()) {
            FTAudioPlayer.showPlayerInProgressAlert(getContext(), () -> {
                mFragmentListener.closeAudioToolbar();
                navigateBack(view);
            });
            return;
        }
        onToolbarItemClick(view);
        requireActivity().onBackPressed();
        FTAudioPlayer.getInstance().stopRecording(getContext(), true);
        mFragmentListener.toolBarItemsClicked();
    }

    private void setUpActions() {
        if (mPenPref.get(mFragmentListener.getDocUid() + "_is_shape_selected", false)) {
            mImgShape.setImageResource(R.drawable.shape_on);
        } else {
            mImgShape.setImageResource(R.drawable.shape);
        }
        mImgUndo.setOnLongClickListener(v -> {
            mFragmentListener.undoLastChange(true);
            return true;
        });
        updateLastSelectedToolView(true);
    }

    void updateBluetoothIcon() {
        if (mBluetoothImageView != null) {
            boolean isStylusEnabled = FTApp.getPref().isStylusEnabled();
            mBluetoothImageView.setVisibility(isStylusEnabled ? View.VISIBLE : View.INVISIBLE);
        }
    }

    public void updateLastSelectedToolView(boolean isForEnabling) {
        int tool = mPenPref.get(lastSelectedToolKey, FTToolBarTools.PEN.toInt());
        mCurrentMode = FTToolBarTools.initWithRawValue(tool);

        if (tool == FTToolBarTools.PEN.ordinal()) {
            mLastSelected = mLayPen;
            mImgPen.setImageResource(isForEnabling ? R.drawable.pen_on : R.drawable.pen);
            mImgPenColor.setVisibility(isForEnabling ? View.VISIBLE : View.GONE);
            if (isForEnabling) {
                DrawableUtil.tintImage(getContext(), mImgPenColor, String.format("#%06X", (0xFFFFFF & mPenPref.get("selectedPenColor", Color.parseColor(PenRackPref.DEFAULT_PEN_COLOR)))), R.mipmap.navcolorpen);
            }
        } else if (tool == FTToolBarTools.HIGHLIGHTER.ordinal()) {
            mLastSelected = mLayHighlighter;
            mImgHighlighter.setImageResource(isForEnabling ? R.drawable.highlighter_on : R.drawable.highlighter);
            mImgHighlighterColor.setVisibility(isForEnabling ? View.VISIBLE : View.GONE);
            if (isForEnabling) {
                DrawableUtil.tintImage(getContext(), mImgHighlighterColor, String.format("#%06X", (0xFFFFFF & mPenPref.get("selectedPenColor_h", Color.parseColor(PenRackPref.DEFAULT_HIGHLIGHTER_COLOR)))), R.mipmap.navcolorhighlighter);
            }
        } else if (tool == FTToolBarTools.ERASER.ordinal()) {
            mLastSelected = mImgEraser;
            ((ImageView) mLastSelected).setImageResource(isForEnabling ? R.drawable.eraser_on : R.drawable.eraser);
        } else if (tool == FTToolBarTools.TEXT.ordinal()) {
            if (mLastSelected != null && mLastSelected.getId() == mLasso.getId() && mLastSelected instanceof ImageView)
                ((ImageView) mLastSelected).setImageResource(R.drawable.cut);
            mLastSelected = mImgText;
            ((ImageView) mLastSelected).setImageResource(isForEnabling ? R.drawable.text_on : R.drawable.text);
        } else if (tool == FTToolBarTools.VIEW.ordinal()) {
            mLastSelected = mImgPdf;
            ((ImageView) mLastSelected).setImageResource(isForEnabling ? R.drawable.pdfview_on : R.drawable.pdfview);
        } else if (tool == FTToolBarTools.LASSO.ordinal()) {
            mLastSelected = mLasso;
            ((ImageView) mLastSelected).setImageResource(isForEnabling ? R.drawable.cut_on : R.drawable.cut);
            if (isForEnabling) {
                mFragmentListener.enableLassoMode(mLasso);
            }
        }
    }

    private void onToolbarItemClick(View view) {
        mFragmentListener.resetAnnotationFragments(view);

        if (view.getId() != R.id.doc_toolbar_undo_image_view) {
            mFragmentListener.updateAddViewPosition();
        }
    }
    //endregion

    //region System callback methods
    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.txtDone) {
            onToolbarItemClick(view);
            updateLastSelectedToolView(false);
            if (mLastSelected == mLayHighlighter) {
                mPenPref.save(lastSelectedToolOldKey, FTToolBarTools.HIGHLIGHTER.toInt());
                mPenViewMobile.setImageResource(R.drawable.highlighter);
            } else {
                mPenPref.save(lastSelectedToolOldKey, FTToolBarTools.PEN.toInt());
                mPenViewMobile.setImageResource(R.drawable.pen);
            }
            mPenPref.save(lastSelectedToolKey, FTToolBarTools.VIEW.toInt());
            mCurrentMode = FTToolBarTools.VIEW;
            layPen.setVisibility(View.GONE);
            layBack.setVisibility(View.VISIBLE);
        } else if (view.getId() == R.id.document_custom_toolbar_pen_view) {
            onToolbarItemClick(view);
            layPen.setVisibility(View.VISIBLE);
            layBack.setVisibility(View.GONE);
            if (mPenPref.get(lastSelectedToolOldKey, -1) == FTToolBarTools.HIGHLIGHTER.toInt()) {
                mLastSelected = mLayHighlighter;
                mImgHighlighter.setImageResource(R.drawable.highlighter_on);
                mImgHighlighterColor.setVisibility(View.VISIBLE);
                DrawableUtil.tintImage(getContext(), mImgHighlighterColor, String.format("#%06X", (0xFFFFFF & mFragmentListener.getCurrentSelectedColor(FTToolBarTools.HIGHLIGHTER))), R.mipmap.navcolorhighlighter);
                mCurrentMode = FTToolBarTools.HIGHLIGHTER;
                mPenPref.save(mFragmentListener.getDocUid() + "_pen_tool", FTToolBarTools.HIGHLIGHTER.toInt());
            } else {
                mLastSelected = mLayPen;
                mImgPen.setImageResource(R.drawable.pen_on);
                mImgPenColor.setVisibility(View.VISIBLE);
                DrawableUtil.tintImage(getContext(), mImgPenColor, String.format("#%06X", (0xFFFFFF & mFragmentListener.getCurrentSelectedColor(FTToolBarTools.PEN))), R.mipmap.navcolorpen);
                mCurrentMode = FTToolBarTools.PEN;
                mPenPref.save(mFragmentListener.getDocUid() + "_pen_tool", FTToolBarTools.PEN.toInt());
            }
        }
        if (mCurrentMode == FTToolBarTools.LASSO && (view.getId() == R.id.doc_toolbar_finder_image_view || view.getId() == R.id.doc_toolbar_shape_image_view
                || view.getId() == R.id.doc_toolbar_settings_image_view || view.getId() == R.id.doc_toolbar_share_image_view)) {
            mFragmentListener.enableLassoMode(view);
        }
    }

    void setBackgroundColor(int color) {
        if (mCustomToolbarView != null)
            mCustomToolbarView.setBackgroundColor(color);
    }

    void setUpToolbar(FTToolbarMode mode) {
        currentToolbarMode = mode;
        boolean isEnabled = currentToolbarMode == FTToolbarMode.ENABLE;
        float alpha = mode == FTToolbarMode.ENABLE ? 1 : 0.3f;

        if (mImgAdd == null) {
            return;
        }
        mImgAdd.setEnabled(isEnabled);
        mImgUndo.setEnabled(isEnabled);
        mLayPen.setEnabled(isEnabled);
        mLayHighlighter.setEnabled(isEnabled);
        mImgEraser.setEnabled(isEnabled);
        mImgText.setEnabled(isEnabled);
        mLasso.setEnabled(isEnabled);
        mImgPdf.setEnabled(isEnabled);
        mImgShape.setEnabled(isEnabled);
        mImgShare.setEnabled(isEnabled);
        mImgFinder.setEnabled(isEnabled);
        mImgSettings.setEnabled(isEnabled);

        mImgAdd.setAlpha(alpha);
        mImgUndo.setAlpha(alpha);
        mLayPen.setAlpha(alpha);
        mLayHighlighter.setAlpha(alpha);
        mImgEraser.setAlpha(alpha);
        mImgText.setAlpha(alpha);
        mLasso.setAlpha(alpha);
        mImgPdf.setAlpha(alpha);
        mImgShape.setAlpha(alpha);
        mImgShare.setAlpha(alpha);
        mImgFinder.setAlpha(alpha);
        mImgSettings.setAlpha(alpha);

        if (mPenViewMobile != null) {
            mPenViewMobile.setAlpha(alpha);
            mPenViewMobile.setEnabled(isEnabled);
        }
    }

    public void updateToPreviousTool() {
        if (mCurrentMode == FTToolBarTools.LASSO) {
            mPenPref.save(lastSelectedToolKey, mPenPref.get(lastSelectedToolOldKey, -1));
        }
    }
    //endregion

    //region Delegates
    public interface DocumentToolbarFragmentInteractionListener {
        void enableLassoMode(View view);

        void undoLastChange(boolean isLongClick);

        void showThumbnails(boolean isExportMode);

        void showNotebookOptions();

        void share(View view);

        void resetAnnotationFragments(View view);

        void closeAudioToolbar();

        void clearAnnotation();

        void updateAddViewPosition();

        void setUpToolbarTheme();

        void toolBarItemsClicked();

        void onToolDoubleTapped(FTToolBarTools mFTToolBarTools);

        void lastSelectedViewInToolBar(View mView);

        void shapeEnabledIDAndStatusInCurrentDoc(String iD, boolean status);

        int getCurrentSelectedColor(FTToolBarTools requestedTool);

        String getDocUid();
    }
}