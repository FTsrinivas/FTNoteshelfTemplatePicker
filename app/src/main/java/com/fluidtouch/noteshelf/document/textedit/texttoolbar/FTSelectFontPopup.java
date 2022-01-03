package com.fluidtouch.noteshelf.document.textedit.texttoolbar;

import android.app.Dialog;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.fluidtouch.noteshelf.commons.ui.BaseRecyclerAdapter;
import com.fluidtouch.noteshelf.commons.ui.FTBaseDialog;
import com.fluidtouch.noteshelf.commons.utils.ObservingService;
import com.fluidtouch.noteshelf.document.textedit.FTFontProvider;
import com.fluidtouch.noteshelf.document.textedit.FTStyledText;
import com.fluidtouch.noteshelf.documentframework.Utilities.FTConstants;
import com.fluidtouch.noteshelf2.R;

import java.util.List;
import java.util.Observer;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FTSelectFontPopup extends FTBaseDialog.Popup implements FTFontStylePopup.Listener {
    @BindView(R.id.font_family_recycler_view)
    RecyclerView mFontFamilyRecyclerView;

    private FTFontFamily mSelectedFontFamily;
    private FTStyledText mInitialText;
    private int prevKeyboardHeight = 0;
    private final Observer mKeyboardHeightObserver = (observable, o) -> {
        int height = (int) o;
        if (isAdded() && prevKeyboardHeight != height && height == 0) dismiss();
        prevKeyboardHeight = height;
    };

    public FTSelectFontPopup(@Nullable FTStyledText initialText) {
        mInitialText = initialText;
    }

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
        return inflater.inflate(R.layout.popup_select_font, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        ObservingService.getInstance().addObserver("onKeyboardHeightChanged", mKeyboardHeightObserver);

        FontFamilyAdapter adapter = new FontFamilyAdapter(new FontFamilyAdapter.FontFamilyAdapterCallback() {
            @Override
            public void onFontClicked(FTFontFamily fontFamily) {
                mSelectedFontFamily = fontFamily;
                if (getParentFragment() != null) {
                    ((Listener) getParentFragment()).onSystemFontSelected(fontFamily, fontFamily.getFontStyles().isEmpty() ? "" : fontFamily.getFontStyles().get(0));
                }
            }

            @Override
            public void onInfoClicked(FTFontFamily fontFamily) {
                mSelectedFontFamily = fontFamily;
                new FTFontStylePopup(fontFamily, mInitialText).show(atView, getChildFragmentManager());
            }
        });
        adapter.addAll(FTFontProvider.getInstance().getAllFonts());
        mFontFamilyRecyclerView.setAdapter(adapter);

        String prevFont = "";
        if (mInitialText == null) {
            prevFont = FTConstants.TEXT_DEFAULT_FONT_FAMILY;
        } else {
            prevFont = mInitialText.getFontFamily();
        }
        List<FTFontFamily> fontFamilies = adapter.getAll();
        for (int i = 0; i < fontFamilies.size(); i++) {
            FTFontFamily fontFamily = fontFamilies.get(i);
            if (fontFamily.getFontName().equals(prevFont)) {
                mSelectedFontFamily = fontFamily;
                adapter.lastSelected = i;
                adapter.notifyItemChanged(i);
                mFontFamilyRecyclerView.scrollToPosition(i);
                break;
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!isMobile() && atView != null) {
            Window window = getDialog().getWindow();
            WindowManager.LayoutParams layoutParams = window.getAttributes();
            layoutParams.gravity = Gravity.TOP | Gravity.START;
            int[] location = new int[2];
            atView.getLocationOnScreen(location);
            int sourceY = location[1];
            View dialogLayout = getView().findViewById(R.id.select_font_popup_layout);
            if (dialogLayout != null && dialogLayout.getLayoutParams() != null) {
                layoutParams.y = Math.abs(sourceY - dialogLayout.getLayoutParams().height);
                window.setAttributes(layoutParams);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ObservingService.getInstance().addObserver("onKeyboardHeightChanged", mKeyboardHeightObserver);
    }

    @OnClick(R.id.dialog_back_button)
    void onBackButtonClicked() {
        dismiss();
    }

    @Override
    public void onFontStyleSelected(String fontStyle) {
        if (getParentFragment() != null && mSelectedFontFamily != null) {
            ((Listener) getParentFragment()).onSystemFontSelected(mSelectedFontFamily, fontStyle);
        }
    }

    static class FontFamilyAdapter extends BaseRecyclerAdapter<FTFontFamily, FontFamilyAdapter.ViewHolder> {
        private FontFamilyAdapterCallback callback;
        private int lastSelected = -1;

        public FontFamilyAdapter(FontFamilyAdapterCallback callback) {
            this.callback = callback;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(getView(parent, R.layout.item_font_style_recycler_view));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            FTFontFamily fontFamily = getItem(position);
            holder.fontFamilyTextView.setText(fontFamily.getFontName());
            holder.infoImageView.setVisibility(fontFamily.getFontStyles().size() > 1 ? View.VISIBLE : View.GONE);
            holder.checkImageView.setVisibility(position == lastSelected ? View.VISIBLE : View.INVISIBLE);
        }

        void onFontFamilyClicked(int position) {
            if (lastSelected != position) {
                lastSelected = position;
                notifyDataSetChanged();
                callback.onFontClicked(getItem(position));
            }
        }

        void onFontFamilyStylesInfoClicked(int position) {
            lastSelected = position;
            notifyDataSetChanged();
            callback.onInfoClicked(getItem(position));
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.check_image_view)
            ImageView checkImageView;
            @BindView(R.id.font_family_text_view)
            TextView fontFamilyTextView;
            @BindView(R.id.font_family_styles_info)
            ImageView infoImageView;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
                itemView.setOnClickListener(v -> {
                    ((FontFamilyAdapter) getBindingAdapter()).onFontFamilyClicked(getAbsoluteAdapterPosition());
                });
            }

            @OnClick(R.id.font_family_styles_info)
            void onFontStylesInfoClicked() {
                ((FontFamilyAdapter) getBindingAdapter()).onFontFamilyStylesInfoClicked(getAbsoluteAdapterPosition());
            }
        }

        public interface FontFamilyAdapterCallback {
            void onFontClicked(FTFontFamily fontFamily);

            void onInfoClicked(FTFontFamily fontFamily);
        }
    }

    public interface Listener {
        void onSystemFontSelected(FTFontFamily fontFamily, String fontStyle);
    }
}
