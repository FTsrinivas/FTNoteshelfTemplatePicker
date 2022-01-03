package com.fluidtouch.noteshelf.store.holder;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.fluidtouch.noteshelf2.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FTImagePreviewViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.txtDes)
    public TextView txtDes;
    @BindView(R.id.txtUpdate)
    public TextView txtUpdate;
    @BindView(R.id.imgDes)
    public ImageView imageDes;
    @BindView(R.id.layImages)
    public LinearLayout layImages;
    public ImageView imagePreview;

    public FTImagePreviewViewHolder(View itemView, int viewType) {
        super(itemView);
        if (viewType == 0)
            ButterKnife.bind(this, itemView);
        imagePreview = (ImageView) itemView.findViewById(R.id.imageView);

    }

}
