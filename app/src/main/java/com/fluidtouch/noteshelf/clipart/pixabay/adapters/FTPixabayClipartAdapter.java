package com.fluidtouch.noteshelf.clipart.pixabay.adapters;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fluidtouch.noteshelf.clipart.pixabay.models.Clipart;
import com.fluidtouch.noteshelf.commons.FTLog;
import com.fluidtouch.noteshelf.commons.ui.BaseRecyclerAdapter;
import com.fluidtouch.noteshelf2.R;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FTPixabayClipartAdapter extends BaseRecyclerAdapter<Clipart, FTPixabayClipartAdapter.ClipartViewHolder> {
    private FTPixabayAdapterCallback mListener;

    public FTPixabayClipartAdapter(FTPixabayAdapterCallback listener) {
        mListener = listener;
    }

    @NonNull
    @Override
    public ClipartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ClipartViewHolder(getView(parent, R.layout.item_pixabay_clipart));
    }

    @Override
    public void onBindViewHolder(@NonNull ClipartViewHolder holder, int position) {
        Picasso.get().load(getItem(position).getPreviewURL()).placeholder(R.drawable.loadingclipart).into(holder.clipartImageView);
    }

    @Override
    public void onViewRecycled(@NonNull ClipartViewHolder holder) {
        Picasso.get().cancelRequest(holder.clipartImageView);
    }

    class ClipartViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.clipart_thumbnail_image_view)
        ImageView clipartImageView;
        @BindView(R.id.clipart_remove_button)
        ImageButton clipartRemoveButton;

        ClipartViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            clipartRemoveButton.setVisibility(View.GONE);
        }

        @OnClick(R.id.clipart_thumbnail_image_view)
        void onClipartSelected() {
            if (mListener != null) {
                FTLog.crashlyticsLog("UI: Selected a clipart");
                mListener.onClipartSelected(getItem(getAbsoluteAdapterPosition()), false);
            }
        }
    }
}