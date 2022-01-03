package com.fluidtouch.noteshelf.document.textedit;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.text.Editable;
import android.text.Layout;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ActionMode;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf.annotation.FTTextAnnotationV1;
import com.fluidtouch.noteshelf.commons.FTLog;
import com.fluidtouch.noteshelf.commons.utils.ScreenUtil;
import com.fluidtouch.noteshelf.document.FTAnnotationFragment;
import com.fluidtouch.noteshelf.document.enums.NSTextAlignment;
import com.fluidtouch.noteshelf.document.textedit.texttoolbar.FTEditTextToolbarFragment;
import com.fluidtouch.noteshelf.document.textedit.texttoolbar.FTFontFamily;
import com.fluidtouch.noteshelf.document.textedit.texttoolbar.FTKeyboardToolbarFragment;
import com.fluidtouch.noteshelf.document.undomanager.InputTextUndoManager;
import com.fluidtouch.noteshelf.documentframework.Utilities.FTConstants;
import com.fluidtouch.noteshelf2.BuildConfig;
import com.fluidtouch.noteshelf2.R;
import com.fluidtouch.renderingengine.annotation.FTAnnotation;
import com.fluidtouch.renderingengine.utils.FTGeometryUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.content.Context.CLIPBOARD_SERVICE;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 */
public class FTEditTextFragment extends FTAnnotationFragment implements FTEditTextToolbarFragment.OnFragmentInteractionListener, FTKeyboardToolbarFragment.Callbacks {
    private final int MAX_UN_CLIPPED_WIDTH = 50;
    //region Binding variables
    @BindView(R.id.document_paper_edit_text)
    AppCompatEditText mEditText;
    @BindView(R.id.document_paper_edit_text_container)
    RelativeLayout mEditTextContainer;

    @BindView(R.id.document_paper_edit_text_parent)
    ConstraintLayout mEditTextParent;
    //endregion
    @BindView(R.id.document_paper_expand_image_view)
    ImageView mExpandImageView;
    boolean isFirstTime = true;
    //region Class variables
    private InputTextUndoManager mInputTextUndoManager;
    private boolean shouldPerformUndoBuffering = true;
    private FTEditTextToolbarFragment mFtEditTextToolbarFragment;
    private FTTextAnnotationV1 mAnnotation;
    private Callbacks mParentCallbacks;
    private FTStyledText mCurrentEditingFtStyledText;
    private View mainView;

    private GestureDetector detectorForCompleteView;

    //endregion
    public View getView() {
        return mEditTextParent;
    }

    private TextWatcher mEditTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            checkParentHeight();
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            //Stand back
        }

        @Override
        public void afterTextChanged(final Editable s) {
            //Stand back
            getCursorPosition();
            // }
        }
    };

    private ActionMode.Callback mActionCallback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            ClipboardManager clipboardManager = (ClipboardManager) requireContext().getSystemService(CLIPBOARD_SERVICE);
            if (null != clipboardManager) {
                try {
                    ClipData data = clipboardManager.getPrimaryClip();

                    if (null != data && data.getDescription().getLabel().equals(BuildConfig.APPLICATION_ID)) {
                        MenuItem pasteItem = menu.findItem(android.R.id.paste);
                        pasteItem.setEnabled(false);
                    }
                } catch (Exception e) {
                    Log.i(this.getClass().getName(), e.getMessage());
                }
            }

            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            if (item.getTitle().toString().equalsIgnoreCase("paste")) {
                FrameLayout.LayoutParams lParams = (FrameLayout.LayoutParams) getView().getLayoutParams();
                lParams.height = FrameLayout.LayoutParams.WRAP_CONTENT;
                getView().setLayoutParams(lParams);
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            Log.i("ActionMode", "Done");
        }
    };

    private View.OnTouchListener mParentOnTouchListener = new View.OnTouchListener() {
        private int xDelta;
        private int yDelta;

        private float xParentInitial;
        private float yParentInitial;

        @Override
        public boolean onTouch(View view, MotionEvent event) {
            detectorForCompleteView.onTouchEvent(event);
            final int x = (int) event.getRawX();
            final int y = (int) event.getRawY();
            int viewWidth = (int) (getView().getWidth() * mParentCallbacks.getContainerScale());
            int viewHeight = (int) (getView().getHeight() * mParentCallbacks.getContainerScale());
            RectF scaledContainerRect = FTGeometryUtils.scaleRect(mParentCallbacks.getContainerRect(), mParentCallbacks.getContainerScale());
            int textToolbarHeight = (mFtEditTextToolbarFragment == null || mFtEditTextToolbarFragment.getView() == null) ? ScreenUtil.convertDpToPx(getContext(), getResources().getInteger(R.integer.fifty_four)) : mFtEditTextToolbarFragment.getView().getHeight();
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    if (getView() != null) {
                        FrameLayout.LayoutParams lParams = (FrameLayout.LayoutParams) getView().getLayoutParams();

                        xParentInitial = x;
                        yParentInitial = y;

                        xDelta = x - (int) getView().getX();
                        yDelta = y - (int) getView().getY();
                    }
                    break;

                case MotionEvent.ACTION_UP:
                    if (x == xParentInitial && y == yParentInitial) {
                        return false;
                    }

                    return true;

                case MotionEvent.ACTION_MOVE:
                    if (getView() != null) {
                        float left = getView().getX();
                        float top = getView().getY();
                        int bundX = x - xDelta;
                        int bundY = y - yDelta;
                        if (bundX > -(viewWidth - MAX_UN_CLIPPED_WIDTH) && bundX < (scaledContainerRect.width() - MAX_UN_CLIPPED_WIDTH)) {
                            left = x - xDelta;
                        }
                        if (bundY > -(viewHeight - MAX_UN_CLIPPED_WIDTH) && bundY < (scaledContainerRect.height() - textToolbarHeight - MAX_UN_CLIPPED_WIDTH)) {
                            top = y - yDelta;
                        }
                        getView().setX(left);
                        getView().setY(top);
                    }
                    return true;

                default:
                    break;
            }
            return false;
        }
    };

    private View.OnTouchListener mExpandOnTouchListener = new View.OnTouchListener() {
        private float xInitial;
        private float yInitial;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            FrameLayout.LayoutParams lParams;
            final float x = event.getX();
            final float y = event.getY();

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    xInitial = x;
                    yInitial = y;
                    break;

                case MotionEvent.ACTION_UP:
                    lParams = (FrameLayout.LayoutParams) getView().getLayoutParams();
                    if (lParams.height < mEditTextContainer.getHeight() + getDimension(R.dimen._10dp)) {
                        lParams.height = FrameLayout.LayoutParams.WRAP_CONTENT;
                    }
                    getView().setLayoutParams(lParams);
                    break;

                case MotionEvent.ACTION_MOVE:
                    lParams = (FrameLayout.LayoutParams) getView().getLayoutParams();
                    int width = (int) (x - xInitial + getView().getWidth());
                    int height = (int) (y - yInitial + getView().getHeight());
                    if (width >= getDimension(R.dimen.edit_text_minimum_width)) {
                        lParams.width = width;
                    }
                    if (height >= getDimension(R.dimen.edit_text_minimum_height)) {
                        lParams.height = height;
                    }
                    getView().setLayoutParams(lParams);
                    break;
            }
            return true;
        }
    };


    //region constructor
    public FTEditTextFragment() {
        // Required empty public constructor
    }

    public static FTEditTextFragment newInstance(FTAnnotation ftTextAnnotation, Callbacks listener) {
        FTEditTextFragment ftEditTextFragment = new FTEditTextFragment();
        ftEditTextFragment.mParentCallbacks = listener;
        ftEditTextFragment.mAnnotation = (FTTextAnnotationV1) ftTextAnnotation;
        return ftEditTextFragment;
    }
    //endregion

    @Override
    public void outsideClick() {
        if ((mEditText == null) || (mFtEditTextToolbarFragment != null && mFtEditTextToolbarFragment.isColorPanelShowing()))
            return;
        saveSpannableString();
        IBinder windowToken = mEditText.getWindowToken();
        try {
            if (mFtEditTextToolbarFragment != null) {
                getFragmentManager().beginTransaction().remove(mFtEditTextToolbarFragment).commitAllowingStateLoss();
            }
            mParentCallbacks.onAnnotationEditFinish();
        } catch (Exception e) {
            e.printStackTrace();
            FTLog.logCrashException(e);
        }
//        if (mFtEditTextToolbarFragment != null && !mFtEditTextToolbarFragment.isVisible())
            hideKeyboard(windowToken);
    }

    //region Fragment callback methods
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.document_paper_edit_text_layout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        ButterKnife.bind(this, view);
        mainView = view;
        RectF visibleFrame = mParentCallbacks.visibleFrame();
        int mInitialWidth;
        int mInitialHeight;
        RectF boundingRect = mAnnotation.getBoundingRect();
        if (boundingRect.width() <= 0) {
            mInitialWidth = (int) ((visibleFrame.right) - boundingRect.left * mParentCallbacks.getContainerScale()) - (int) (ScreenUtil.convertDpToPx(getContext(), 20) * mParentCallbacks.getContainerScale());
            mInitialWidth = (int) (mInitialWidth / mParentCallbacks.getContainerScale());
            if (mInitialWidth < ScreenUtil.convertDpToPx(getContext(), 80) / mParentCallbacks.getContainerScale()) {
                mAnnotation.getBoundingRect().left = Math.max(visibleFrame.left + ScreenUtil.convertDpToPx(getContext(), 20) * mParentCallbacks.getContainerScale(), visibleFrame.right - ScreenUtil.convertDpToPx(getContext(), 140) * mParentCallbacks.getContainerScale()) / mParentCallbacks.getContainerScale();
                mAnnotation.getBoundingRect().right = mAnnotation.getBoundingRect().left;
                mInitialWidth = (int) ((visibleFrame.right) - mAnnotation.getBoundingRect().left * mParentCallbacks.getContainerScale()) - (int) (ScreenUtil.convertDpToPx(getContext(), 20) * mParentCallbacks.getContainerScale());
                mInitialWidth = (int) (mInitialWidth / mParentCallbacks.getContainerScale());
            }
            mInitialHeight = view.getContext().getResources().getDimensionPixelOffset(R.dimen.edit_text_initial_height);
        } else {
            mInitialWidth = (int) boundingRect.width();
            mInitialHeight = (int) boundingRect.height();
        }

        //view.setMinimumHeight((int) (getDimension(R.dimen._100dp) * mParentCallbacks.getContainerScale()));
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) view.getLayoutParams();
        layoutParams.leftMargin = (int) mParentCallbacks.getVisibleRect().left;
        layoutParams.rightMargin = (int) mParentCallbacks.getVisibleRect().left;
        layoutParams.topMargin = (int) mParentCallbacks.getVisibleRect().top;
        layoutParams.bottomMargin = (int) mParentCallbacks.getVisibleRect().top;
        view.setLayoutParams(layoutParams);
        getView().setLayoutParams(new FrameLayout.LayoutParams(mInitialWidth, mInitialHeight));
        mEditTextParent.setX((int) (mAnnotation.getBoundingRect().left * mParentCallbacks.getContainerScale()));
        mEditTextParent.setY((int) (mAnnotation.getBoundingRect().top * mParentCallbacks.getContainerScale()));
        initializeViews(mAnnotation.getTextInputInfo());
        Drawable backgroundDrawable = ContextCompat.getDrawable(view.getContext(), R.drawable.document_paper_edit_layout_bg);
        backgroundDrawable.setAlpha(5 * 255 / 100);
        mEditTextParent.setBackground(backgroundDrawable);

        //OnTouchListeners
        getView().setOnTouchListener(mParentOnTouchListener);
        detectorForCompleteView = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                showKeyboard();
                return super.onSingleTapUp(e);
            }
        });
        this.mEditText.setOnTouchListener(mParentOnTouchListener);
        this.mExpandImageView.setOnTouchListener(mExpandOnTouchListener);

        //TextChangeListeners
        this.mEditText.addTextChangedListener(mEditTextWatcher);

        //Custom menu options listeners
        this.mEditText.setCustomSelectionActionModeCallback(mActionCallback);
        this.mEditText.setCustomInsertionActionModeCallback(mActionCallback);

        //Adding scale to the parent
        mEditTextParent.setPivotX(0);
        mEditTextParent.setPivotY(0);
        mEditTextParent.setScaleX(mParentCallbacks.getContainerScale());
        mEditTextParent.setScaleY(mParentCallbacks.getContainerScale());
        getView().addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (isFirstTime) {
                    getCursorPosition();
                    showKeyboard();
                }
                isFirstTime = false;
            }
        });
    }
    //endregion

    //region Initialisation
    private void initializeViews(FTStyledText initialText) {
        mCurrentEditingFtStyledText = FTStyledText.instance(initialText);
        int margin = ScreenUtil.convertDpToPx(requireContext(), initialText.getPadding());
        mEditText.measure(0, 0);
        mEditTextContainer.setPadding(margin, margin, margin, margin);
        this.mInputTextUndoManager = new InputTextUndoManager(mEditText);

        this.mEditText.setText(initialText.getPlainText());
        this.mEditText.setSelection(initialText.getPlainText().length());
        this.mEditText.setTextSize(initialText.getSize());
        try {
            this.mEditText.setTypeface(Typeface.createFromFile(getFullFontFamily(initialText, false)));
        } catch (Exception e) {
            if (initialText.getFontFamily().equals(FTConstants.TEXT_DEFAULT_FONT_FAMILY) && initialText.getStyle() == -1) {
                initialText.setStyle(0);
            }
            this.mEditText.setTypeface(Typeface.createFromAsset(getResources().getAssets(), getFullFontFamily(initialText, true)));
        }
        this.mEditText.setTextColor(initialText.getColor());
        this.mEditText.setGravity(getGravity(initialText.getAlignment()));
        if (initialText.isUnderline()) {
            this.mEditText.setPaintFlags(mEditText.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        }

        initTimer();
    }

    private void initTimer() {
        mInputTextUndoManager.add(new SpannableStringBuilder(mEditText.getText()));
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (shouldPerformUndoBuffering)
                    initTimer();
            }
        }, 5000);
    }
    //endregion

    //region System callback methods
    private void getCursorPosition() {
        int pos = mEditText.getSelectionStart();
        Layout layout = mEditText.getLayout();
        int line = layout.getLineForOffset(pos);
        int baseline = layout.getLineBaseline(line);
        int ascent = layout.getLineAscent(line);
        float x = layout.getPrimaryHorizontal(pos);
        float y = baseline + ascent;
        int margin = ScreenUtil.convertDpToPx(getContext(), mCurrentEditingFtStyledText.getPadding());
        int extraOffsetX = margin;
        if (x + getView().getX() < mParentCallbacks.visibleFrame().right / 2)
            extraOffsetX = -margin;
        int extraOffsetY = mEditText.getLineHeight() + margin;
        mParentCallbacks.currentTextBoxCursorPosition(new PointF((x + extraOffsetX) * mParentCallbacks.getContainerScale(), (y + extraOffsetY) * mParentCallbacks.getContainerScale()));
    }
    //endregion

    //region Custom listeners call back methods
    @Override
    public void onColorChanged(String color) {
        if (!color.contains("#")) {
            color = "#" + color;
        }
        int color_ = Color.parseColor(color);
        mCurrentEditingFtStyledText.setColor(color_);
        this.mEditText.setTextColor(color_);
    }

    @Override
    public void onFontAlignChanged(Layout.Alignment alignment, int gravity) {
        mCurrentEditingFtStyledText.setAlignment(getAlignmentEnumType(gravity));
        this.mEditText.setGravity(gravity);
    }

    @Override
    public void onFontStyleChanged(int typeface) {
        mCurrentEditingFtStyledText.setStyle(typeface);
        setFullFontFamily();
    }

    @Override
    public void onFontFamilyChanged(String fontFamily) {
        mCurrentEditingFtStyledText.setFontFamily(fontFamily);
        setFullFontFamily();
    }

    @Override
    public void onFontSizeChanged(int sizeInSp) {
        checkParentHeight();
        mCurrentEditingFtStyledText.setSize(sizeInSp);
        this.mEditText.setTextSize(sizeInSp);
    }

    @Override
    public void onTextUnderline(boolean isUnderline) {
        mCurrentEditingFtStyledText.setUnderline(isUnderline);
        if (isUnderline) {
            this.mEditText.setPaintFlags(this.mEditText.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            return;
        }

        this.mEditText.setPaintFlags(this.mEditText.getPaintFlags() & (~Paint.UNDERLINE_TEXT_FLAG));
    }

    @Override
    public void onSystemFontSelected(FTFontFamily fontFamily, String fontStyle) {
        mCurrentEditingFtStyledText.setStyle(FTFontFamily.getStyleForString(fontStyle));
        if (fontFamily == null) {
            mCurrentEditingFtStyledText.setFontFamily(mCurrentEditingFtStyledText.getFontFamily());
            mEditText.setTypeface(Typeface.createFromAsset(getResources().getAssets(), getFullFontFamily(mCurrentEditingFtStyledText, true)));
        } else {
            mCurrentEditingFtStyledText.setFontFamily(fontFamily.getFontName());
            mCurrentEditingFtStyledText.setDefaultFont(fontFamily.isDefault);
            mEditText.setTypeface(fontFamily.isDefault ? Typeface.createFromAsset(getResources().getAssets(), fontFamily.getFontPathForStyle(fontStyle))
                    : Typeface.createFromFile(fontFamily.getFontPathForStyle(fontStyle)), FTFontFamily.getStyleForString(fontStyle));
        }


        checkParentHeight();
    }
    //endregion

    //region Helper methods
    private void showKeyboard() {
        InputMethodManager imm = (InputMethodManager) FTApp.getInstance().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
    }

    private void hideKeyboard(IBinder windowToken) {
        InputMethodManager imm = (InputMethodManager) FTApp.getInstance().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(windowToken, 0);
        }
    }

    private int getDimension(int dimenId) {
        return getContext().getResources().getDimensionPixelOffset(dimenId);
    }

    private void checkParentHeight() {
        int containerHeight = (int) (mEditText.getMeasuredHeight() + (2 * ScreenUtil.convertDpToPx(getContext(), mCurrentEditingFtStyledText.getPadding() + 1)));
        if (containerHeight >= getView().getHeight()) {
            FrameLayout.LayoutParams lParams = (FrameLayout.LayoutParams) getView().getLayoutParams();
            lParams.height = FrameLayout.LayoutParams.WRAP_CONTENT;
            getView().setLayoutParams(lParams);
        }
    }

    private NSTextAlignment getAlignmentEnumType(int gravity) {
        if (gravity == Gravity.START) {
            return NSTextAlignment.NSTextAlignmentLeft;
        } else if (gravity == Gravity.CENTER) {
            return NSTextAlignment.NSTextAlignmentCenter;
        } else if (gravity == Gravity.END) {
            return NSTextAlignment.NSTextAlignmentRight;
        }

        return NSTextAlignment.NSTextAlignmentLeft;
    }

    private int getGravity(NSTextAlignment alignment) {
        if (alignment == NSTextAlignment.NSTextAlignmentLeft) {
            return Gravity.START;
        } else if (alignment == NSTextAlignment.NSTextAlignmentCenter) {
            return Gravity.CENTER;
        } else if (alignment == NSTextAlignment.NSTextAlignmentRight) {
            return Gravity.END;
        }

        return Gravity.START;
    }

    private void setFullFontFamily() {
        //this.mEditText.setTypeface(Typeface.createFromAsset(getContext().getAssets(), "fonts/" + getFullFontFamily(mCurrentEditingFtStyledText) + ".ttf"));
        //this.mEditText.setTypeface(Typeface.createFromFile(FTConstants.SYSTEM_FONTS_PATH + getFullFontFamily(mCurrentEditingFtStyledText) + ".ttf"));
    }

    private String getFullFontFamily(FTStyledText inputText, boolean isDefaultFont) {
        if (isDefaultFont) {
            if (inputText.getStyle() != -1)
                return "fonts/" + inputText.getFontFamily() + "_" + FTFontFamily.getStyleForInt(inputText.getStyle()).toLowerCase() + ".ttf";
            else
                return "fonts/" + inputText.getFontFamily() + "_regular.ttf";
        } else {
            if (inputText.getStyle() != -1)
                return FTConstants.SYSTEM_FONTS_PATH + inputText.getFontFamily() + "-" + FTFontFamily.getStyleForInt(inputText.getStyle()) + ".ttf";
            else
                return FTConstants.SYSTEM_FONTS_PATH + inputText.getFontFamily() + "_regular.ttf";
        }
    }

    private String getFontStyle(FTStyledText styledText) {
        int style = styledText.getStyle();
        /*if (style == Typeface.ITALIC) {
            return "italic";
        } else if (style == Typeface.BOLD) {
            return "bold";
        } else if (style == Typeface.BOLD_ITALIC) {
            return "bold_italic";
        } else {
            return "regular";
        }*/
        if (style == Typeface.ITALIC) {
            return "Italic";
        } else if (style == Typeface.BOLD) {
            return "Bold";
        } else if (style == Typeface.BOLD_ITALIC) {
            return "BoldItalic";
        } else if (style == Typeface.NORMAL) {
            return "Regular";
        } else {
            return "";
        }
    }

    private RectF getContainerScaledRect() {
        RectF rectF = new RectF();
        rectF.left = getView().getX();
        rectF.right = rectF.left + getView().getWidth() * mParentCallbacks.getContainerScale();
        rectF.top = getView().getY();
        rectF.bottom = rectF.top + getView().getHeight() * mParentCallbacks.getContainerScale();
        return FTGeometryUtils.scaleRect(rectF, 1 / mParentCallbacks.getContainerScale());
    }

    private void saveSpannableString() {
        shouldPerformUndoBuffering = false;
        if (mEditText.getText().toString().trim().length() > 0) {
            addConfiguredAnnotation();
        } else {
            removeAnnotation();
        }
    }

    private void addConfiguredAnnotation() {
        boolean isNewAnnotation = mAnnotation.isNew;

        if (isNewAnnotation) {
            mAnnotation.setInputTextWithInfo(mCurrentEditingFtStyledText);
            mAnnotation.getTextInputInfo().setPlainText(mEditText.getText().toString());
            mAnnotation.setBoundingRect(getContainerScaledRect());
            mAnnotation.isNew = false;
            mAnnotation.hidden = false;
            mParentCallbacks.addAnnotation(mAnnotation);
        } else {
            FTTextAnnotationV1 helperAnnotation = new FTTextAnnotationV1(getContext());
            helperAnnotation.setInputTextWithInfo(mCurrentEditingFtStyledText);
            helperAnnotation.getTextInputInfo().setPlainText(mEditText.getText().toString());
            helperAnnotation.setBoundingRect(getContainerScaledRect());
            mParentCallbacks.updateAnnotation(mAnnotation, helperAnnotation);
        }
    }

    private void removeAnnotation() {
        if (mAnnotation.getBoundingRect().width() > 0) {
            mParentCallbacks.removeAnnotation(mAnnotation);
        }
    }

    @Override
    public FTKeyboardToolbarFragment getToolBarFragment() {
        this.mFtEditTextToolbarFragment = FTEditTextToolbarFragment.newInstance(mAnnotation.getTextInputInfo(), this);
        return mFtEditTextToolbarFragment;
    }

    public void onVisibleRectChanged() {
        if (mainView == null)
            return;
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) mainView.getLayoutParams();
        layoutParams.leftMargin = (int) mParentCallbacks.getVisibleRect().left;
        layoutParams.rightMargin = (int) mParentCallbacks.getVisibleRect().left;
        layoutParams.topMargin = (int) mParentCallbacks.getVisibleRect().top;
        layoutParams.bottomMargin = (int) mParentCallbacks.getVisibleRect().top;
        mainView.setLayoutParams(layoutParams);
    }
    //endregion
}
