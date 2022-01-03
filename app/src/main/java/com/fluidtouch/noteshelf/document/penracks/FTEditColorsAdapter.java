package com.fluidtouch.noteshelf.document.penracks;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.fluidtouch.noteshelf.commons.utils.ColorUtil;
import com.fluidtouch.noteshelf.commons.utils.DrawableUtil;
import com.fluidtouch.noteshelf.services.FTFirebaseAnalytics;
import com.fluidtouch.noteshelf2.R;
import com.thesurix.gesturerecycler.GestureAdapter;
import com.thesurix.gesturerecycler.GestureViewHolder;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Sreenu on 18/04/19
 */
public class FTEditColorsAdapter extends GestureAdapter<String, GestureViewHolder> {
    private int plusImage = 0;
    private int colorImage = 1;

    boolean mIsInDeleteMode = false;

    private EditColorsAdapterContainerListener mContainerListener;

    public FTEditColorsAdapter(EditColorsAdapterContainerListener containerListener) {
        mContainerListener = containerListener;
    }

    @Override
    public int getItemViewType(int position) {
        if (getItem(position).equals("")) {
            return plusImage;
        }
        return colorImage;
    }

    @NonNull
    @Override
    public GestureViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == plusImage) {
            return new ColorsPlusViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_colors_plus_recycler_view, parent, false));
        } else {
            return new ColorsViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_colors_recycler_view, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull GestureViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        if (getItemViewType(position) == colorImage) {
            DrawableUtil.setGradientDrawableColor(((ColorsViewHolder) holder).colorImageView, "#" + getItem(position), 0);
            ((ColorsViewHolder) holder).deleteMarkView.setVisibility(mIsInDeleteMode ? View.VISIBLE : View.GONE);
            ((ColorsViewHolder) holder).mBorderView.setVisibility(ColorUtil.isLightColor(Color.parseColor("#" + getItem(position))) ? View.VISIBLE : View.GONE);
        }
    }

    public void setMode(boolean isInDeleteMode) {
        mIsInDeleteMode = isInDeleteMode;
        notifyDataSetChanged();
    }

    class ColorsViewHolder extends GestureViewHolder {
        @BindView(R.id.item_colors_image_view)
        protected ImageView colorImageView;
        @BindView(R.id.item_colors_delete_mark_view)
        protected View deleteMarkView;
        @BindView(R.id.item_colors_border_view)
        protected View mBorderView;

        ColorsViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @Override
        public boolean canDrag() {
            return true;
        }

        @Override
        public boolean canSwipe() {
            return false;
        }

        @OnClick(R.id.item_colors_image_view)
        void showColorPicker() {
            int position = getAbsoluteAdapterPosition();
            if (position == -1) {
                return;
            }
            if (mIsInDeleteMode) {
                FTFirebaseAnalytics.logEvent("Pen_DeleteColor_Select");
                int minimumColorCount = 10;
                if (getItemCount() > minimumColorCount) {
                    String color = getItem(position);
                    remove(position);
                    notifyDataSetChanged();
                    mContainerListener.removeColor(color, position);
                } else {
                    mContainerListener.showMinimumColorsError(minimumColorCount);
                }
            } else {
                mContainerListener.onColorSelected(getItem(position), position);
            }
        }
    }

    class ColorsPlusViewHolder extends GestureViewHolder {
        @BindView(R.id.item_colors_plus_image_view)
        protected ImageView colorImageView;

        ColorsPlusViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @Override
        public boolean canDrag() {
            return false;
        }

        @Override
        public boolean canSwipe() {
            return false;
        }

        @OnClick(R.id.item_colors_plus_image_view)
        void showColorPicker() {
            mContainerListener.showColorPickerDialog("7BB2B9", getAbsoluteAdapterPosition());
        }
    }

    public interface EditColorsAdapterContainerListener {
        void onColorSelected(String color, int position);

        void showColorPickerDialog(String color, int position);

        void removeColor(String color, int position);

        void showMinimumColorsError(int minimumColorCount);
    }
}