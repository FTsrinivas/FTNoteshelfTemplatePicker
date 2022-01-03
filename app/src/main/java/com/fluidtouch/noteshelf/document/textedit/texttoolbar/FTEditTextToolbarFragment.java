package com.fluidtouch.noteshelf.document.textedit.texttoolbar;

import android.content.res.Configuration;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.dd.plist.PropertyListParser;
import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf.commons.utils.ColorUtil;
import com.fluidtouch.noteshelf.commons.utils.DrawableUtil;
import com.fluidtouch.noteshelf.commons.utils.FTFileManagerUtil;
import com.fluidtouch.noteshelf.commons.utils.ScreenUtil;
import com.fluidtouch.noteshelf.document.textedit.FTStyledText;
import com.fluidtouch.noteshelf.documentframework.Utilities.FTConstants;
import com.fluidtouch.noteshelf.preferences.SystemPref;
import com.fluidtouch.noteshelf2.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FTEditTextToolbarFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link FTEditTextToolbarFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FTEditTextToolbarFragment extends FTKeyboardToolbarFragment implements AdapterView.OnItemSelectedListener, FTColorPickerFragment.ColorPickerListener, FTSelectFontPopup.Listener {
    private final int MIN_FONT_SIZE = 6;
    private final float UNSELECTED_ALPHA = 0.3f;
    private final float SELECTED_ALPHA = 1.0f;
    //region Binding variables
    @BindView(R.id.document_paper_text_toolbar_minus_text_view)
    TextView mTextSizeDecreaseTextView;
    @BindView(R.id.document_paper_text_toolbar_plus_text_view)
    TextView mTextSizeIncreaseTextView;
    @BindView(R.id.document_paper_text_toolbar_size_text_view)
    TextView mTextSizeTextView;
    @BindView(R.id.document_paper_text_toolbar_bold_image_view)
    ImageView mBoldImageView;
    @BindView(R.id.document_paper_text_toolbar_italic_image_view)
    ImageView mItalicImageView;
    //endregion
    @BindView(R.id.document_paper_text_toolbar_underline_image_view)
    ImageView mUnderlineImageView;
    @BindView(R.id.document_paper_text_toolbar_text_color_view)
    View mColorView;
    @BindView(R.id.text_toolbar_font_family_text_view)
    TextView mSelectedFontTextView;

    //region Class variables
    private View mParentLayout;
    private FrameLayout mColorViewHolder;
    private FTColorPickerFragment mColorPickerFragment;
    private HashMap<String, ArrayList<String>> mFontFamilies;
    private List<String> rootLevelFonts = new ArrayList<>();
    private int fontSize = FTConstants.TEXT_DEFAULT_SIZE;
    private FTStyledText mInitialText;
    private OnFragmentInteractionListener mFragmentListener;
    //endregion

    public static FTEditTextToolbarFragment newInstance(FTStyledText initialText, OnFragmentInteractionListener listener) {
        FTEditTextToolbarFragment fragment = new FTEditTextToolbarFragment();
        fragment.mFragmentListener = listener;
        fragment.mInitialText = initialText;
        return fragment;
    }

    //region Fragment callback methods
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.document_paper_text_toolbar, container, false);
    }
    //endregion

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        if (view instanceof HorizontalScrollView)
            mParentLayout = (HorizontalScrollView) view;
        else
            mParentLayout = (ConstraintLayout) view;
        ButterKnife.bind(this, view);

        try {
            mFontFamilies = (HashMap<String, ArrayList<String>>) PropertyListParser.parse(FTFileManagerUtil.getFileInputStream(FTFileManagerUtil.copyFileFromAssets(getContext(), "fonts.plist"))).toJavaObject();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (mInitialText == null) {
            updateFontFamily(FTConstants.TEXT_DEFAULT_FONT_FAMILY);
            updateFontSize(this.fontSize);
        } else {
            validateUI(mInitialText);
        }
        onColorSelected(FTApp.getPref().get(SystemPref.RECENT_INPUT_TEXT_COLOR, FTConstants.DEFAULT_INPUT_TEXT_COLOR));
        view.setVisibility(View.VISIBLE);
        setKeyboardVisibilityListener();
    }

    private void setKeyboardVisibilityListener() {
        final View parentView = getView();
        parentView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            private boolean alreadyOpen;
            private final int defaultKeyboardHeightDP = 100;
            private final int EstimatedKeyboardDP = defaultKeyboardHeightDP + (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ? 48 : 0);
            private final Rect rect = new Rect();

            @Override
            public void onGlobalLayout() {
                int estimatedKeyboardHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, EstimatedKeyboardDP, parentView.getResources().getDisplayMetrics());
                parentView.getWindowVisibleDisplayFrame(rect);
                int heightDiff = parentView.getRootView().getHeight() - (rect.bottom - rect.top);
                boolean isShown = heightDiff >= estimatedKeyboardHeight;

                if (isShown == alreadyOpen) {
                    Log.i("Keyboard state", "Ignoring global layout change...");
                    return;
                }
                alreadyOpen = isShown;
                if (!isShown) {
                    isColorPanelShowing();
                }
            }
        });
    }

    @OnClick(R.id.document_paper_text_toolbar_font_family_spinner_layout)
    void onChangeFontClicked(View view) {
        new FTSelectFontPopup(mInitialText).show(view, getChildFragmentManager());
    }

    //region Helper methods
    private void validateUI(FTStyledText spans) {
        updateSize(spans.getSize());
        updateFontStyleButtonInput(FTFontFamily.getFontFamilyForName(spans.getFontFamily()));
        updateFontStyle(spans.getStyle());
        updateFontFamily(spans.getFontFamily());
        updateColor(ColorUtil.hexStringFromInt(spans.getColor()));
        updateUnderline(spans.isUnderline());
    }
    //endregion

    private List<String> getRootLevelFonts() {
        Iterator iterator = mFontFamilies.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, ArrayList<String>> entry = (Map.Entry<String, ArrayList<String>>) iterator.next();
            rootLevelFonts.add(entry.getKey());
            iterator.remove(); // avoids a ConcurrentModificationException
        }
        return rootLevelFonts;
    }

    private void updateFontSize(int size) {
        if (MIN_FONT_SIZE <= size) {
            mTextSizeTextView.setText(Objects.requireNonNull(getContext()).getString(R.string.set_size, size));
            mFragmentListener.onFontSizeChanged(size);
        } else {
            ++fontSize;
            Toast.makeText(getContext(), R.string.font_size_cannot_be_below, Toast.LENGTH_SHORT).show();
        }
    }

    private void updateFontStyleChange(float currentViewAlpha, float otherViewAlpha, int currentTypeface, int otherTypeface) {
        if (currentViewAlpha == SELECTED_ALPHA) {
            if (otherViewAlpha == SELECTED_ALPHA) {
                mFragmentListener.onFontStyleChanged(Typeface.BOLD_ITALIC);
            } else {
                mFragmentListener.onFontStyleChanged(currentTypeface);
            }
        } else {
            if (otherViewAlpha == SELECTED_ALPHA) {
                mFragmentListener.onFontStyleChanged(otherTypeface);
            } else {
                mFragmentListener.onFontStyleChanged(Typeface.NORMAL);
            }
        }
    }

    public void updateSize(int size) {
        mTextSizeTextView.setText(Objects.requireNonNull(getContext()).getString(R.string.set_size, size));
        this.fontSize = size;
    }

    public void updateFontStyle(int fontStyle) {
        if (fontStyle == Typeface.BOLD_ITALIC) {
            mBoldImageView.setAlpha(SELECTED_ALPHA);
            mItalicImageView.setAlpha(SELECTED_ALPHA);
        } else if (fontStyle == Typeface.BOLD) {
            mBoldImageView.setAlpha(SELECTED_ALPHA);
            mItalicImageView.setAlpha(UNSELECTED_ALPHA);
        } else if (fontStyle == Typeface.ITALIC) {
            mBoldImageView.setAlpha(UNSELECTED_ALPHA);
            mItalicImageView.setAlpha(SELECTED_ALPHA);
        } else if (fontStyle == Typeface.NORMAL) {
            mBoldImageView.setAlpha(UNSELECTED_ALPHA);
            mItalicImageView.setAlpha(UNSELECTED_ALPHA);
        }
    }

    public void updateFontStyleButtonInput(FTFontFamily fontFamily) {
        mBoldImageView.setAlpha(UNSELECTED_ALPHA);
        mBoldImageView.setTag(false);
        mItalicImageView.setAlpha(UNSELECTED_ALPHA);
        mItalicImageView.setTag(false);
        if (fontFamily == null || fontFamily.getFontStyles().size() == 1) return;
        for (String fontStyle : fontFamily.getFontStyles()) {
            switch (FTFontFamily.getStyleForString(fontStyle)) {
                case Typeface.BOLD_ITALIC:
                    mBoldImageView.setAlpha(SELECTED_ALPHA);
                    mBoldImageView.setTag(true);
                    mItalicImageView.setAlpha(SELECTED_ALPHA);
                    mItalicImageView.setTag(true);
                    break;
                case Typeface.BOLD:
                    mBoldImageView.setAlpha(SELECTED_ALPHA);
                    mBoldImageView.setTag(true);
                    break;
                case Typeface.ITALIC:
                    mItalicImageView.setAlpha(SELECTED_ALPHA);
                    mItalicImageView.setTag(true);
                    break;
            }
        }
    }

    public void updateFontFamily(String fontFamily) {
        mSelectedFontTextView.setText(fontFamily);
        mInitialText.setFontFamily(fontFamily);
    }

    public void updateUnderline(boolean underline) {
        mUnderlineImageView.setAlpha(underline ? SELECTED_ALPHA : UNSELECTED_ALPHA);
    }

    public void updateColor(String color) {
        color = color.contains("#") ? color.substring(1) : color;
        DrawableUtil.setGradientDrawableColor(mColorView, getContext().getString(R.string.set_color, color), 0);
        FTApp.getPref().save(SystemPref.RECENT_INPUT_TEXT_COLOR, color);
    }

    //region View On Action Call backs
    @OnClick(R.id.document_paper_text_toolbar_minus_text_view)
    public void decreaseFontSize() {
        isColorPanelShowing();
        updateFontSize(--fontSize);
    }
    //endregion

    @OnClick(R.id.document_paper_text_toolbar_plus_text_view)
    public void increaseFontSize() {
        isColorPanelShowing();
        updateFontSize(++fontSize);
    }

    @OnClick(R.id.document_paper_text_toolbar_bold_image_view)
    public void selectBold() {
        if (mBoldImageView.getTag().equals(false)) return;
        isColorPanelShowing();
        mBoldImageView.setAlpha(mBoldImageView.getAlpha() == SELECTED_ALPHA ? UNSELECTED_ALPHA : SELECTED_ALPHA);
        //updateFontStyleChange(mBoldImageView.getAlpha(), mItalicImageView.getAlpha(), Typeface.BOLD, Typeface.ITALIC);
        if (mBoldImageView.getAlpha() == SELECTED_ALPHA) {
            mFragmentListener.onSystemFontSelected(null, FTFontFamily.getStyleForInt(mItalicImageView.getAlpha() == SELECTED_ALPHA ? Typeface.BOLD_ITALIC : Typeface.BOLD));
        } else {
            mFragmentListener.onSystemFontSelected(null, FTFontFamily.getStyleForInt(mItalicImageView.getAlpha() == SELECTED_ALPHA ? Typeface.ITALIC : Typeface.NORMAL));
        }
    }

    @OnClick(R.id.document_paper_text_toolbar_italic_image_view)
    public void selectItalic() {
        if (mItalicImageView.getTag().equals(false)) return;
        isColorPanelShowing();
        mItalicImageView.setAlpha(mItalicImageView.getAlpha() == SELECTED_ALPHA ? UNSELECTED_ALPHA : SELECTED_ALPHA);
        //updateFontStyleChange(mItalicImageView.getAlpha(), mBoldImageView.getAlpha(), Typeface.ITALIC, Typeface.BOLD);
        if (mItalicImageView.getAlpha() == SELECTED_ALPHA) {
            mFragmentListener.onSystemFontSelected(null, FTFontFamily.getStyleForInt(mBoldImageView.getAlpha() == SELECTED_ALPHA ? Typeface.BOLD_ITALIC : Typeface.ITALIC));
        } else {
            mFragmentListener.onSystemFontSelected(null, FTFontFamily.getStyleForInt(mBoldImageView.getAlpha() == SELECTED_ALPHA ? Typeface.BOLD : Typeface.NORMAL));
        }
    }

    @OnClick(R.id.document_paper_text_toolbar_underline_image_view)
    public void selectUnderline() {
        isColorPanelShowing();
        mUnderlineImageView.setAlpha(mUnderlineImageView.getAlpha() == SELECTED_ALPHA ? UNSELECTED_ALPHA : SELECTED_ALPHA);
        mFragmentListener.onTextUnderline(mUnderlineImageView.getAlpha() == SELECTED_ALPHA);
    }

    @OnClick(R.id.document_paper_text_toolbar_align_left_image_view)
    public void alignLeft() {
        isColorPanelShowing();
        mFragmentListener.onFontAlignChanged(Layout.Alignment.ALIGN_NORMAL, Gravity.START);
    }

    @OnClick(R.id.document_paper_text_toolbar_align_center_image_view)
    public void alignCentre() {
        isColorPanelShowing();
        mFragmentListener.onFontAlignChanged(Layout.Alignment.ALIGN_CENTER, Gravity.CENTER);
    }

    @OnClick(R.id.document_paper_text_toolbar_align_right_image_view)
    public void alignRight() {
        isColorPanelShowing();
        mFragmentListener.onFontAlignChanged(Layout.Alignment.ALIGN_OPPOSITE, Gravity.END);
    }

    @OnClick(R.id.document_paper_text_toolbar_text_color_view)
    public void selectColor() {
        mColorViewHolder = new FrameLayout(getContext());
        mColorViewHolder.setTag(getContext().getString(R.string.color_pallette));
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mColorViewHolder.setLayoutParams(params);
        mColorViewHolder.setId(98765);
        ((RelativeLayout) mParentLayout.getParent().getParent()).addView(mColorViewHolder);

        RelativeLayout.LayoutParams layoutParams = ((RelativeLayout.LayoutParams) mColorViewHolder.getLayoutParams());
        //layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        layoutParams.bottomMargin = ScreenUtil.convertDpToPx(getContext(), 48);
        layoutParams.rightMargin = ScreenUtil.convertDpToPx(getContext(), getResources().getInteger(R.integer.fifty));
        layoutParams.leftMargin = ScreenUtil.convertDpToPx(getContext(), getResources().getInteger(R.integer.three_hundered));
        int screenSize = getResources().getConfiguration().screenLayout &
                Configuration.SCREENLAYOUT_SIZE_MASK;
        int orientation = getResources().getConfiguration().orientation;
        if ((screenSize == Configuration.SCREENLAYOUT_SIZE_NORMAL && orientation == Configuration.ORIENTATION_PORTRAIT) || screenSize == Configuration.SCREENLAYOUT_SIZE_SMALL) {
            layoutParams.leftMargin = ScreenUtil.convertDpToPx(getContext(), getResources().getInteger(R.integer.fifty));
        }
        mColorPickerFragment = FTColorPickerFragment.newInstance(this);
        mColorViewHolder.setY(((FrameLayout) mParentLayout.getParent()).getY() - getResources().getDimensionPixelOffset(R.dimen.dimen_68));
        getFragmentManager().beginTransaction().replace(mColorViewHolder.getId(), mColorPickerFragment, "someTag1").commit();
    }

    @OnClick(R.id.document_paper_text_toolbar_parent_layout)
    public void onParentLayoutClicked() {
        isColorPanelShowing();
    }

    //region Custom listeners call back methods
    @Override
    public void onColorSelected(String color) {
        DrawableUtil.setGradientDrawableColor(mColorView, "#" + color, 0);
        mFragmentListener.onColorChanged(color);
    }
    //endregion

    public boolean isColorPanelShowing() {
        if (mColorViewHolder != null) {
            getFragmentManager().beginTransaction().remove(mColorPickerFragment).commit();
            ((RelativeLayout) mParentLayout.getParent().getParent()).removeView(mColorViewHolder);
            mColorViewHolder = null;
            return true;
        }
        return false;
    }

    //region System listeners call back methods
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        mFragmentListener.onFontFamilyChanged(rootLevelFonts.get(position));
    }
    //endregion

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onSystemFontSelected(FTFontFamily fontFamily, String fontStyle) {
        updateFontFamily(fontFamily.getFontName());
        updateFontStyleButtonInput(fontFamily);
        updateFontStyle(FTFontFamily.getStyleForString(fontStyle));
        mFragmentListener.onSystemFontSelected(fontFamily, fontStyle);
    }

    //region delegates
    public interface OnFragmentInteractionListener {
        void onColorChanged(String color);

        void onFontAlignChanged(Layout.Alignment alignment, int gravity);

        void onFontStyleChanged(int typeface);

        void onFontFamilyChanged(String fontFamily);

        void onFontSizeChanged(int sizeInSp);

        void onTextUnderline(boolean isUnderline);

        void onSystemFontSelected(FTFontFamily fontFamily, String fontStyle);
    }
    //endregion
}
