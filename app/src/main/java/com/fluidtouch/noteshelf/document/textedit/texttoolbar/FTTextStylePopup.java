package com.fluidtouch.noteshelf.document.textedit.texttoolbar;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.fluidtouch.noteshelf.commons.ui.FTBaseDialog;
import com.fluidtouch.noteshelf.documentframework.Utilities.FTConstants;
import com.fluidtouch.noteshelf2.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FTTextStylePopup extends FTBaseDialog.Popup implements FTColorPickerFragment.ColorPickerListener {
    private static final int MIN_FONT_SIZE = 6;

    @BindView(R.id.text_style_tab)
    TextView mTextStyleTab;
    @BindView(R.id.favorites_tab)
    TextView mFavoritesTab;
    @BindView(R.id.preview_text_view)
    TextView mPreviewTextView;
    @BindView(R.id.favorites_button)
    ImageView mFavoritesButton;
    @BindView(R.id.font_family_text_view)
    TextView mFontFamilyTextView;
    @BindView(R.id.font_size)
    TextView mFontSizeTextView;

    private int fontSize = FTConstants.TEXT_DEFAULT_SIZE;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        if (!isMobile()) {
            Window window = dialog.getWindow();
            if (window != null) {
                window.setGravity(Gravity.BOTTOM | Gravity.START);
            }
        }
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.popup_text_style, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        getChildFragmentManager().beginTransaction().replace(R.id.color_picker_fragment, FTColorPickerFragment.newInstance(this)).commit();

        updateFontSize(this.fontSize);
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        Fragment colorPickerFragment = getChildFragmentManager().findFragmentById(R.id.color_picker_fragment);
        if (colorPickerFragment != null) {
            getChildFragmentManager().beginTransaction().remove(colorPickerFragment);
        }
    }

    @OnClick(R.id.text_style_tab)
    void onTextStyleClicked() {
        mTextStyleTab.setBackgroundResource(R.drawable.tab_item_bg);
        mFavoritesTab.setBackgroundResource(0);
    }

    @OnClick(R.id.favorites_tab)
    void onFavoritesClicked() {
        mFavoritesTab.setBackgroundResource(R.drawable.tab_item_bg);
        mTextStyleTab.setBackgroundResource(0);
    }

    @OnClick(R.id.favorites_button)
    void onFavoriteButtonClicked() {
        mFavoritesButton.setImageResource(R.drawable.iconfavoriteon);
    }

    @OnClick(R.id.font_family_text_view)
    void onFontFamilyClicked() {
        new FTSelectFontPopup(null).show(getChildFragmentManager());
    }

    @OnClick(R.id.text_size_minus_button)
    void onMinusClicked() {
        updateFontSize(--fontSize);
    }

    @OnClick(R.id.text_size_plus_button)
    void onPlusClicked() {
        updateFontSize(++fontSize);
    }

    @OnClick(R.id.bold_button)
    void onBoldClicked() {
        mPreviewTextView.setTypeface(Typeface.defaultFromStyle(mPreviewTextView.getTypeface().getStyle() == Typeface.NORMAL ? Typeface.BOLD : Typeface.NORMAL));
    }

    @OnClick(R.id.italic_button)
    void onItalicClicked() {
        mPreviewTextView.setTypeface(Typeface.defaultFromStyle(mPreviewTextView.getTypeface().getStyle() == Typeface.NORMAL ? Typeface.ITALIC : Typeface.NORMAL));
    }

    @OnClick(R.id.underline_button)
    void onUnderlineClicked() {
        if (mPreviewTextView.getPaintFlags() == (mPreviewTextView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG)) {
            mPreviewTextView.setPaintFlags(mPreviewTextView.getPaintFlags() & (~Paint.UNDERLINE_TEXT_FLAG));
        } else {
            mPreviewTextView.setPaintFlags(mPreviewTextView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        }
    }

    @OnClick(R.id.set_as_default_text_view)
    void onSetAsDefaultClicked() {

    }

    @Override
    public void onColorSelected(String color) {

    }

    private void updateFontSize(int size) {
        if (MIN_FONT_SIZE <= size) {
            mFontSizeTextView.setText(getString(R.string.set_size, size));
            mPreviewTextView.setTextSize(size);
        } else {
            ++fontSize;
            Toast.makeText(getContext(), R.string.font_size_cannot_be_below, Toast.LENGTH_SHORT).show();
        }
    }
}