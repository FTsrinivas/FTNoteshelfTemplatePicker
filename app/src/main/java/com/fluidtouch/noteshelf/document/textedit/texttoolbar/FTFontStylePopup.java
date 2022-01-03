package com.fluidtouch.noteshelf.document.textedit.texttoolbar;

import android.app.Dialog;
import android.content.DialogInterface;
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
import com.fluidtouch.noteshelf.document.textedit.FTStyledText;
import com.fluidtouch.noteshelf2.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FTFontStylePopup extends FTBaseDialog.Popup {
    @BindView(R.id.font_family_recycler_view)
    RecyclerView mFontFamilyRecyclerView;
    @BindView(R.id.dialog_title)
    TextView mDialogTitle;

    private FTFontFamily fontFamily;
    private FTStyledText mStyledText;

    FTFontStylePopup(FTFontFamily fontFamily, FTStyledText styledText) {
        this.fontFamily = fontFamily;
        mStyledText = styledText;
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

        mDialogTitle.setText(fontFamily.getFontName());

        FontStyleAdapter adapter = new FontStyleAdapter(fontStyle -> {
            if (getParentFragment() != null) {
                ((Listener) getParentFragment()).onFontStyleSelected(fontStyle);
            }
        });
        adapter.addAll(fontFamily.getFontStyles());
        mFontFamilyRecyclerView.setAdapter(adapter);
        if (mStyledText != null) {
            int index = adapter.getAll().indexOf(FTFontFamily.getStyleForInt(mStyledText.getStyle()).toLowerCase());
            adapter.lastSelected = index;
            adapter.notifyItemChanged(index);
            mFontFamilyRecyclerView.scrollToPosition(index);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!isMobile() && atView != null) {
            Dialog parentDialog = getParentDialog();
            if (parentDialog != null) {
                Window window = getDialog().getWindow();
                WindowManager.LayoutParams layoutParams = window.getAttributes();
                layoutParams.x = parentDialog.getWindow().getAttributes().x;
                layoutParams.y = parentDialog.getWindow().getAttributes().y;
                window.setAttributes(layoutParams);
            }
        }
    }

    @OnClick(R.id.dialog_back_button)
    void onBackButtonClicked() {
        dismiss();
    }

    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        super.onCancel(dialog);
        getParentDialog().dismiss();
    }

    static class FontStyleAdapter extends BaseRecyclerAdapter<String, FontStyleAdapter.ViewHolder> {
        private Listener listener;
        private int lastSelected = -1;

        public FontStyleAdapter(Listener listener) {
            this.listener = listener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(getView(parent, R.layout.item_font_style_recycler_view));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.fontFamilyTextView.setText(getItem(position));
            holder.checkImageView.setVisibility(position == lastSelected ? View.VISIBLE : View.INVISIBLE);
        }

        void onFontStyleClicked(int position) {
            lastSelected = position;
            notifyDataSetChanged();
            listener.onFontStyleClicked(getItem(position));
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
                infoImageView.setVisibility(View.GONE);
                itemView.setOnClickListener(v -> ((FontStyleAdapter) getBindingAdapter()).onFontStyleClicked(getAbsoluteAdapterPosition()));
            }
        }

        interface Listener {
            void onFontStyleClicked(String fontStyle);
        }
    }

    public interface Listener {
        void onFontStyleSelected(String fontStyle);
    }
}
