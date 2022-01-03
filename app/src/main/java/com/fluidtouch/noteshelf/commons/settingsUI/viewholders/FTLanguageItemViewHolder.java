package com.fluidtouch.noteshelf.commons.settingsUI.viewholders;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fluidtouch.noteshelf2.R;
import com.wang.avi.AVLoadingIndicatorView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FTLanguageItemViewHolder extends RecyclerView.ViewHolder {
    @BindView(R.id.language_text_view)
    public TextView languageTextView;
    @BindView(R.id.language_name_text_view)
    public TextView languageNameTextView;
    @BindView(R.id.status_image_view)
    public ImageView downloadImageView;
    @BindView(R.id.check_image_view)
    public ImageView checkImageView;
    @BindView(R.id.status_progress_bar)
    public AVLoadingIndicatorView progressBar;

    private FTLanguageItemClickListener listener;

    public FTLanguageItemViewHolder(@NonNull View itemView, FTLanguageItemClickListener listener) {
        super(itemView);
        ButterKnife.bind(this, itemView);
        this.listener = listener;
    }

    @OnClick(R.id.item_language_layout)
    public void onClick() {
        listener.onLanguageClicked(this);
    }

    public interface FTLanguageItemClickListener {
        void onLanguageClicked(FTLanguageItemViewHolder viewHolder);
    }
}