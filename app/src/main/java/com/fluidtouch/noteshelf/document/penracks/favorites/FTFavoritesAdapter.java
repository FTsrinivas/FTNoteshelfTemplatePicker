package com.fluidtouch.noteshelf.document.penracks.favorites;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.fluidtouch.noteshelf.commons.FTLog;
import com.fluidtouch.noteshelf.commons.utils.DrawableUtil;
import com.fluidtouch.noteshelf.commons.utils.ScreenUtil;
import com.fluidtouch.noteshelf.services.FTFirebaseAnalytics;
import com.fluidtouch.noteshelf2.R;
import com.thesurix.gesturerecycler.GestureAdapter;
import com.thesurix.gesturerecycler.GestureViewHolder;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;

public class FTFavoritesAdapter extends GestureAdapter<Favorite, FTFavoritesAdapter.FavoritesViewHolder> {
    public boolean isEditMode = false;
    private Context context;
    private Favorite selectedFavorite;
    private FavoritesAdapterCallback listener;
    private FavoritesViewHolder lastSelected;
    private boolean isAnimated;
    private boolean frmWidgetToolbar;
    private View view;
    private long mLastClickTime = 0;

    public FTFavoritesAdapter(Context context, FavoritesAdapterCallback listener, boolean frmWidgetToolbar) {
        this.listener = listener;
        this.context = context;
        this.frmWidgetToolbar = frmWidgetToolbar;
    }

    @NonNull
    @Override
    public FavoritesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if (this.frmWidgetToolbar) {
            view = LayoutInflater.from(context).inflate(R.layout.item_fav_widget_recycler_view, parent, false);
        } else {
            view = LayoutInflater.from(context).inflate(R.layout.item_favorites_recycler_view, parent, false);
        }

        ButterKnife.bind(this, view);
        return new FavoritesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FavoritesViewHolder holder, int position) {
        Favorite favorite = getItem(holder.getBindingAdapterPosition());
        int penColorBg = R.mipmap.pencolor_small;
        switch (favorite.getPenType()) {
            case pen:
                if (this.frmWidgetToolbar) {
                    penColorBg = R.mipmap.penminicolor;
                    holder.penType.setImageResource(R.mipmap.penminimask);
                    holder.penShadow.setImageResource(R.mipmap.penminishadow);
                } else {
                    penColorBg = R.mipmap.pencolor_small;
                    holder.penType.setImageResource(R.mipmap.penmask_small);
                    holder.penShadow.setImageResource(R.mipmap.penshadow_small);
                }

                break;
            case caligraphy:
                if (this.frmWidgetToolbar) {
                    penColorBg = R.mipmap.calligraphyminicolor;
                    holder.penType.setImageResource(R.mipmap.calligraphyminimask);
                    holder.penShadow.setImageResource(R.mipmap.calligraphyminishadow);
                } else {
                    penColorBg = R.mipmap.calligraphycolor_small;
                    holder.penType.setImageResource(R.mipmap.calligraphymask_small);
                    holder.penShadow.setImageResource(R.mipmap.calligraphyshadow_small);
                }

                break;
            case highlighter:
                if (this.frmWidgetToolbar) {
                    penColorBg = R.mipmap.highroundminicolor;
                    holder.penType.setImageResource(R.mipmap.highroundminimask);
                    holder.penShadow.setImageResource(R.mipmap.highroundminishadow);
                } else {
                    penColorBg = R.mipmap.highroundcolor_small;
                    holder.penType.setImageResource(R.mipmap.highroundmask_small);
                    holder.penShadow.setImageResource(R.mipmap.highroundshadow_small);
                }

                break;
            case pilotPen:
                if (this.frmWidgetToolbar) {
                    penColorBg = R.mipmap.fineminicolor;
                    holder.penType.setImageResource(R.mipmap.fineminimask);
                    holder.penShadow.setImageResource(R.mipmap.fineminishadow);
                } else {
                    penColorBg = R.mipmap.finecolor_small;
                    holder.penType.setImageResource(R.mipmap.finemask_small);
                    holder.penShadow.setImageResource(R.mipmap.fineshadow_small);
                }

                break;
            case flatHighlighter:
                if (this.frmWidgetToolbar) {
                    penColorBg = R.mipmap.highflatminicolor;
                    holder.penType.setImageResource(R.mipmap.highflatminimask);
                    holder.penShadow.setImageResource(R.mipmap.highflatminishadow);
                } else {
                    penColorBg = R.mipmap.highflatcolor_small;
                    holder.penType.setImageResource(R.mipmap.highflatmask_small);
                    holder.penShadow.setImageResource(R.mipmap.highflatshadow_small);
                }

                break;
        }

        if (selectedFavorite != null
                && favorite.getPenColor().equals(selectedFavorite.getPenColor())
                && favorite.getPenType().equals(selectedFavorite.getPenType())
                && favorite.getPenSize() == selectedFavorite.getPenSize()) {
            DrawableUtil.setGradientDrawableColor(holder.penView, "#c5c5b2", 0);
            ((GradientDrawable) holder.penSize.getBackground()).setColor(Color.parseColor("#ecece4"));
            if (!isAnimated) {
                valueAnimator(holder.layFavorite, 20, 0);
            } else {
                valueAnimator(holder.layFavorite, 0, 0);
            }
            selectedFavorite = favorite;
            lastSelected = holder;
            isAnimated = true;
        } else {
            DrawableUtil.setGradientDrawableColor(holder.penView, "#ecece4", 0);
            ((GradientDrawable) holder.penSize.getBackground()).setColor(Color.parseColor("#c5c5b2"));
            valueAnimator(holder.layFavorite, 20, 20);
        }

        holder.penColor.setImageBitmap(getBitmap(penColorBg, favorite.getPenColor()));
        holder.penSize.setText(String.valueOf(favorite.getPenSize()));

        if (isEditMode) {
            holder.removeButton.setVisibility(View.VISIBLE);
        } else {
            holder.removeButton.setVisibility(View.GONE);
        }
    }

    public void setSelectedFavorite(Favorite selectedFavorite) {
        if (this.selectedFavorite == null || !this.selectedFavorite.equals(selectedFavorite)) {
            this.selectedFavorite = selectedFavorite;
            notifyDataSetChanged();
        }
    }

    private Bitmap getBitmap(int penColorBg, String color) {
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), penColorBg);
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);

        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

        Canvas canvas = new Canvas(output);
        canvas.drawBitmap(bitmap, rect, rect, paint);
        canvas.drawColor(Color.parseColor(color), PorterDuff.Mode.SRC_ATOP);

        return output;
    }

    private void valueAnimator(View view, int start, int end) {

        if (this.frmWidgetToolbar) {
            if (start > 0)
                start = -ScreenUtil.convertDpToPx(context, 10);
            if (end > 0)
                end = -ScreenUtil.convertDpToPx(context, 10);
        } else {
            if (start > 0)
                start = -ScreenUtil.convertDpToPx(context, 80);
            if (end > 0)
                end = -ScreenUtil.convertDpToPx(context, 80);
            if (start == 0)
                start = -ScreenUtil.convertDpToPx(context, 65);
            if (end == 0)
                end = -ScreenUtil.convertDpToPx(context, 65);
        }

        ValueAnimator animator = ValueAnimator.ofInt(start, end);
        animator.setDuration(200);
        animator.addUpdateListener(animation -> {
            RelativeLayout view1 = (RelativeLayout) ((RelativeLayout) view).getChildAt(1);
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) view1.getLayoutParams();
            lp.setMargins(0, (Integer) animation.getAnimatedValue(), 0, 0);
            view1.setLayoutParams(lp);
        });
        animator.start();
    }

    public void updateEditMode(Boolean isEditMode) {
        if (isEditMode.compareTo(this.isEditMode) != 0) {
            this.isEditMode = isEditMode;
            notifyDataSetChanged();
        }
    }

    public void remove(Favorite favorite) {
        List<Favorite> favorites = getData();
        if (!favorites.isEmpty() && favorite != null) {
            for (Favorite item : favorites) {
                if (favorite.getPenColor().equals(item.getPenColor()) &&
                        favorite.getPenType().equals(item.getPenType()) &&
                        favorite.getPenSize() == item.getPenSize()) {
                    getData().remove(item);
                    break;
                }
            }
        }
        notifyDataSetChanged();
    }

    public interface FavoritesAdapterCallback {
        void onPenSelected(Favorite favorite);

        void removeFromFavorites(int position);
    }

    class FavoritesViewHolder extends GestureViewHolder {
        @BindView(R.id.pen_type)
        ImageView penType;
        @BindView(R.id.pen_color)
        ImageView penColor;
        @BindView(R.id.pen_size)
        TextView penSize;
        @BindView(R.id.pen_shadow)
        ImageView penShadow;
        @BindView(R.id.favorite_remove_button)
        ImageButton removeButton;
        @BindView(R.id.pen_layout)
        RelativeLayout penLayout;
        @BindView(R.id.favorite_item)
        RelativeLayout layFavorite;
        @BindView(R.id.pen_view)
        ImageView penView;

        FavoritesViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @OnClick(R.id.favorite_item)
        void onFavoriteClicked() {
            if (isEditMode) {
                isEditMode = false;
                removeButton.setVisibility(View.GONE);
                notifyDataSetChanged();
            } else {
                //Deselect previous pen
//                if (lastSelected != null && !lastSelected.equals(this)) {
//                    DrawableUtil.setGradientDrawableColor(lastSelected.penView, "#ecece4", 0);
//                    ((GradientDrawable) penSize.getBackground()).setColor(Color.parseColor("#c5c5b2"));
//                    valueAnimator(lastSelected.layFavorite, 0, 20);
//                }
                //Select another pen
                Favorite favorite = getItem(getBindingAdapterPosition());
                if (selectedFavorite == null ||
                        !favorite.getPenColor().equals(selectedFavorite.getPenColor()) ||
                        !favorite.getPenType().equals(selectedFavorite.getPenType())
                        || favorite.getPenSize() != selectedFavorite.getPenSize()) {
//                    DrawableUtil.setGradientDrawableColor(penView, "#c5c5b2", 0);
//                    ((GradientDrawable) penSize.getBackground()).setColor(Color.parseColor("#ecece4"));
//                    valueAnimator(layFavorite, 20, 0);
                    penShadow.setVisibility(View.VISIBLE);
                    lastSelected = this;
                    selectedFavorite = favorite;
                    if (listener != null) {
                        listener.onPenSelected(favorite);
                    }
                    notifyDataSetChanged();
                }
            }
        }

        @OnLongClick(R.id.favorite_item)
        boolean onLongPressed() {
            if (!frmWidgetToolbar) {
                FTFirebaseAnalytics.logEvent("PenFavorites_LongPress");
                FTLog.crashlyticsLog("Tapped: PenFavorites_LongPress");
                isEditMode = true;
                notifyDataSetChanged();
            }
            return true;
        }

        @OnClick(R.id.favorite_remove_button)
        void onRemoveFavoriteClicked() {
            if (SystemClock.elapsedRealtime() - mLastClickTime < 500) {
                return;
            }
            FTFirebaseAnalytics.logEvent("PenFavorites_LongPress_Delete");
            FTLog.crashlyticsLog("Tapped: PenFavorites_LongPress_Delete");
            mLastClickTime = SystemClock.elapsedRealtime();
            listener.removeFromFavorites(getBindingAdapterPosition());
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