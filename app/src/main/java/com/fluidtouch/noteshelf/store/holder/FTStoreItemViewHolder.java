package com.fluidtouch.noteshelf.store.holder;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.fluidtouch.noteshelf2.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FTStoreItemViewHolder extends RecyclerView.ViewHolder {
    @BindView(R.id.storeListContainer)
    public LinearLayout layContainer;
    @BindView(R.id.txtTitle)
    public TextView txtTitle;
    public TextView txtSignUp;
    public ImageView imgProfile;

    public FTStoreItemViewHolder(View itemView, int viewType) {
        super(itemView);
        if (viewType == 0) {
            txtSignUp = (TextView) itemView.findViewById(R.id.txtSignUp);
            imgProfile = (ImageView) itemView.findViewById(R.id.imageProfile);
        } else {
            ButterKnife.bind(this, itemView);
        }
    }

}
