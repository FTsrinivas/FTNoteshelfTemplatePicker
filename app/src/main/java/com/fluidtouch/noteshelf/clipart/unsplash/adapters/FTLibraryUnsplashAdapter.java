package com.fluidtouch.noteshelf.clipart.unsplash.adapters;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fluidtouch.noteshelf.clipart.unsplash.models.UnsplashPhotoInfo;
import com.fluidtouch.noteshelf.commons.FTLog;
import com.fluidtouch.noteshelf.commons.ui.BaseRecyclerAdapter;
import com.fluidtouch.noteshelf2.R;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FTLibraryUnsplashAdapter extends BaseRecyclerAdapter<UnsplashPhotoInfo, FTLibraryUnsplashAdapter.UnsplashViewHolder> {
    private FTUnsplashAdapterCallback mListener;

    public FTLibraryUnsplashAdapter(FTUnsplashAdapterCallback listener) {
        mListener = listener;
    }

    @NonNull
    @Override
    public UnsplashViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new UnsplashViewHolder(getView(parent, R.layout.item_unsplash_clipart));
    }

    @Override
    public void onBindViewHolder(@NonNull UnsplashViewHolder holder, int position) {
        if (!TextUtils.isEmpty(getItem(position).getThumbURL())) {
            Picasso.get().load(getItem(position).getThumbURL()).placeholder(R.drawable.loadingclipart).into(holder.clipartImageView);
            holder.authorName.setText(holder.authorName.getContext().getString(R.string.unsplash_person_name, getItem(position).getmUnsplashPhotoOwner().getName()));
        }
    }

    @Override
    public void onViewRecycled(@NonNull UnsplashViewHolder holder) {
        Picasso.get().cancelRequest(holder.clipartImageView);
    }

    class UnsplashViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.clipart_thumbnail_image_view)
        ImageView clipartImageView;
        @BindView(R.id.clipart_remove_button)
        ImageButton clipartRemoveButton;
        @BindView(R.id.photographer_name)
        TextView authorName;

        UnsplashViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            clipartRemoveButton.setVisibility(View.GONE);
        }

        @OnClick(R.id.clipart_thumbnail_image_view)
        void onClipartSelected() {
            if (mListener != null) {
                Log.d("FTouch==>", " FTLibraryUnsplashAdapter onClipartSelected");
                FTLog.crashlyticsLog("UI: Selected a clipart");
                mListener.onClipartSelected(getItem(getAbsoluteAdapterPosition()), false);
            }
        }

        @OnClick(R.id.photographer_name)
        void onPicOwnerSelected() {
            if (mListener != null) {
                FTLog.crashlyticsLog("UI: Clicked on a photographer name");
                mListener.onPhotographerNameSelected(getAbsoluteAdapterPosition());
            }
        }
    }
}