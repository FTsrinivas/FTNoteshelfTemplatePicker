package com.fluidtouch.noteshelf.commons.settingsUI.adapters;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf.commons.FTLog;
import com.fluidtouch.noteshelf.commons.ui.BaseRecyclerAdapter;
import com.fluidtouch.noteshelf.documentframework.Utilities.FTConstants;
import com.fluidtouch.noteshelf.models.theme.Theme;
import com.fluidtouch.noteshelf.preferences.SystemPref;
import com.fluidtouch.noteshelf2.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FTThemeItemAdapter extends BaseRecyclerAdapter<Theme, FTThemeItemAdapter.FTThemeItemViewHolder> {
    private ThemeItemAdapterCallback mListener;
    private int prevSelectedPos;

    public FTThemeItemAdapter(ThemeItemAdapterCallback listener) {
        mListener = listener;
    }

    @NonNull
    @Override
    public FTThemeItemViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, int viewType) {
        return new FTThemeItemViewHolder(getView(parent, R.layout.item_theme_recycler_view));
    }

    @Override
    public void onBindViewHolder(@NonNull FTThemeItemViewHolder holder, int position) {
        Theme theme = getItem(position);
        holder.themeImageView.setImageResource(theme.getDrawableResId());
        String selectedThemeColor = FTApp.getPref().get(SystemPref.SELECTED_THEME_TOOLBAR_COLOR, FTConstants.DEFAULT_THEME_TOOLBAR_COLOR);
        holder.checkMarkImageView.setImageResource(selectedThemeColor.equals(theme.getToolbarColor()) ? R.drawable.check_badge : R.drawable.checkbadgeoff_dark);
    }

    class FTThemeItemViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.item_theme_image_view)
        public ImageView themeImageView;
        @BindView(R.id.item_theme_check)
        public ImageView checkMarkImageView;

        FTThemeItemViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @OnClick(R.id.item_theme_layout)
        void onClick() {
            FTLog.crashlyticsLog("UI: Selected a theme");
            FTApp.getPref().save(SystemPref.SELECTED_THEME_TOOLBAR_COLOR, getItem(getAdapterPosition()).getToolbarColor());
            notifyItemChanged(prevSelectedPos);
            prevSelectedPos = getAbsoluteAdapterPosition();
            notifyItemChanged(getAdapterPosition());
            notifyDataSetChanged();
            if (mListener != null) mListener.onThemeSelected();
        }
    }

    public interface ThemeItemAdapterCallback {
        void onThemeSelected();
    }
}