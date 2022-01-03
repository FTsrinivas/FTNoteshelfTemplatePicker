package com.fluidtouch.noteshelf.clipart.pixabay.adapters;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.fluidtouch.noteshelf.clipart.pixabay.models.Clipart;
import com.fluidtouch.noteshelf.commons.FTLog;
import com.fluidtouch.noteshelf2.R;
import com.squareup.picasso.Picasso;
import com.thesurix.gesturerecycler.GestureAdapter;
import com.thesurix.gesturerecycler.GestureViewHolder;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;

public class FTPixabayRecentClipartAdapter extends GestureAdapter<Clipart, FTPixabayRecentClipartAdapter.ClipartViewHolder> implements GestureAdapter.OnDataChangeListener<Clipart> {
    private FTPixabayAdapterCallback listener;
    private boolean isSelected = false;

    public FTPixabayRecentClipartAdapter(FTPixabayAdapterCallback listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ClipartViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        return new ClipartViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_pixabay_clipart, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ClipartViewHolder holder, int position) {
        holder.clipartRemoveButton.setVisibility(isSelected ? View.VISIBLE : View.GONE);
        Clipart clipart = getItem(position);
        if (!TextUtils.isEmpty(clipart.getPreviewURL())) {
            Picasso.get().load(clipart.getPreviewURL()).resize(holder.clipartImageView.getLayoutParams().width, holder.clipartImageView.getLayoutParams().height).placeholder(R.drawable.loadingclipart).into(holder.clipartImageView);
        }
    }

    @Override
    public int getItemCount() {
        return getData().size();
    }

    @Override
    public void onItemRemoved(Clipart clipart, int i) {
        // Not used
    }

    @Override
    public void onItemReorder(Clipart clipart, int from, int to) {
        listener.reorderCliparts(from, to);
    }

    class ClipartViewHolder extends GestureViewHolder {
        @BindView(R.id.clipart_thumbnail_image_view)
        protected ImageView clipartImageView;
        @BindView(R.id.clipart_remove_button)
        protected ImageButton clipartRemoveButton;

        ClipartViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @OnClick(R.id.clipart_thumbnail_image_view)
        void onClipartClicked() {
            if (isSelected) {
                isSelected = false;
                notifyDataSetChanged();
            } else {
                FTLog.crashlyticsLog("UI: Selected a recent clipart");
                listener.onClipartSelected(getItem(getAbsoluteAdapterPosition()), false);
            }
        }

        @OnLongClick(R.id.clipart_thumbnail_image_view)
        boolean onClipartLongPressed() {
            isSelected = true;
            notifyDataSetChanged();
            return true;
        }

        @OnClick(R.id.clipart_remove_button)
        void onClipartRemoveClicked() {
            FTLog.crashlyticsLog("UI: Clicked delete clipart");
            listener.onClipartSelected(getItem(getAbsoluteAdapterPosition()), true);
            remove(getAbsoluteAdapterPosition());
            notifyItemRemoved(getAbsoluteAdapterPosition());
        }

        @Override
        public boolean canDrag() {
            return true;
        }

        @Override
        public boolean canSwipe() {
            return false;
        }
    }
}