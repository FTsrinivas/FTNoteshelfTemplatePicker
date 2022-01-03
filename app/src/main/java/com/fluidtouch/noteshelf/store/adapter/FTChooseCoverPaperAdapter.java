package com.fluidtouch.noteshelf.store.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fluidtouch.noteshelf.commons.ui.BaseRecyclerAdapter;
import com.fluidtouch.noteshelf.models.theme.FTNAutoTemlpateDiaryTheme;
import com.fluidtouch.noteshelf.models.theme.FTNTheme;
import com.fluidtouch.noteshelf.models.theme.FTNThemeCategory;
import com.fluidtouch.noteshelf.services.FTFirebaseAnalytics;
import com.fluidtouch.noteshelf.store.ui.FTDiaryDatePickerPopup;
import com.fluidtouch.noteshelf2.R;

import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;

public class FTChooseCoverPaperAdapter extends BaseRecyclerAdapter<FTNTheme, FTChooseCoverPaperAdapter.ViewHolder>
        implements FTDiaryDatePickerPopup.DatePickerListener {

    private FragmentManager mFragmentManager;
    private ChooseCoverPaperItemAdapterListener mListener;
    public boolean isEditMode = false;

    public FTChooseCoverPaperAdapter(FragmentManager fragmentManager, ChooseCoverPaperItemAdapterListener listener) {
        this.mFragmentManager = fragmentManager;
        this.mListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        return new ViewHolder(getView(viewGroup, R.layout.item_choose_cover_paper));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Context context = holder.itemView.getContext();
        FTNTheme theme = getItem(position);
        if (theme.themeName.equals("addCustomTheme")) {
            holder.mCoverPaperImageView.setImageResource(R.mipmap.notebookcustom);
            holder.mCoverPaperImageView.setLayoutParams(new FrameLayout.LayoutParams(context.getResources().getDimensionPixelOffset(R.dimen.new_142dp), context.getResources().getDimensionPixelOffset(R.dimen.new_175dp)));
            holder.mTitleTextView.setText("");
        } else {
            Bitmap bitmap = theme.themeThumbnail(context);
            if (bitmap != null) {
                holder.mCoverPaperImageView.setImageBitmap(bitmap);
                holder.mCoverPaperImageView.setLayoutParams(getLayoutParams(context, holder.mCoverPaperImageView.getLayoutParams(), position));
                holder.mTitleTextView.setText(theme.themeName);
            }
        }
        if (isEditMode && !theme.isTemplate() && getItem(0).isDownloadTheme == getItem(position).isDownloadTheme) {
            holder.mCoverPaperDeleteButton.setVisibility(View.VISIBLE);
        } else {
            holder.mCoverPaperDeleteButton.setVisibility(View.GONE);
        }
    }

    @Override
    public void clear() {
        super.clear();
    }

    @Override
    public void onDatesSelected(FTNTheme theme, Date startDate, Date endDate) {
        FTNAutoTemlpateDiaryTheme diaryTheme = (FTNAutoTemlpateDiaryTheme) theme;
        diaryTheme.startDate = startDate;
        diaryTheme.endDate = endDate;
        mListener.onTemplateSelect(theme);
    }

    private ViewGroup.LayoutParams getLayoutParams(Context context, ViewGroup.LayoutParams layoutParams, int position) {
        FTNTheme ftnTheme = getItem(position);
        boolean isLandScape = ftnTheme.isLandscape();

        int landSscapeWidth = context.getResources().getDimensionPixelOffset(R.dimen.new_142dp);
        int landscapeHeight = context.getResources().getDimensionPixelOffset(R.dimen.new_116dp);
        int portraitWidth = context.getResources().getDimensionPixelOffset(R.dimen.new_142dp);
        int portraitHeight = context.getResources().getDimensionPixelOffset(R.dimen.new_175dp);

        layoutParams.width = isLandScape ? landSscapeWidth : portraitWidth;
        layoutParams.height = isLandScape ? landscapeHeight : portraitHeight;
        return layoutParams;
    }

    public interface ChooseCoverPaperItemAdapterListener {
        void onTemplateSelect(FTNTheme theme);

        void onTemplateDelete(FTNTheme theme);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.item_choose_cover_paper_image_view)
        protected ImageView mCoverPaperImageView;
        @BindView(R.id.item_choose_cover_paper_title_text_view)
        protected TextView mTitleTextView;
        @BindView(R.id.item_choose_cover_paper_delete_image_view)
        ImageButton mCoverPaperDeleteButton;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @OnClick(R.id.item_choose_cover_paper_image_view)
        void onSingleClicked() {
            if (isEditMode) {
                isEditMode = false;
                notifyDataSetChanged();
            } else {
                if (getBindingAdapterPosition() >= 0) {
                    FTNTheme theme = getItem(getBindingAdapterPosition());
                    if (theme.dynamicId == 1) {
                        new FTDiaryDatePickerPopup(theme, FTChooseCoverPaperAdapter.this).show(itemView, mFragmentManager);
                    } else {
                        if (!theme.isCustomTheme)
                            FTFirebaseAnalytics.logEvent(theme.ftThemeType == FTNThemeCategory.FTThemeType.COVER ? (theme.isDownloadTheme ? "NoteshelfClub_CoverPack_" : "ChooseCover_") : (theme.isDownloadTheme ? "NoteshelfClub_PaperPack_" : "ChoosePaper_") + theme.diplayName);
                        mListener.onTemplateSelect(theme);
                    }
                }
            }
        }

        @OnClick(R.id.item_choose_cover_paper_delete_image_view)
        void onDeleteClicked() {
            if (getAdapterPosition() >= 0)
                mListener.onTemplateDelete(getItem(getAdapterPosition()));
        }

        @OnLongClick(R.id.item_choose_cover_paper_image_view)
        void onLongClicked() {
            if (!getItem(getAdapterPosition()).isTemplate()) {
                isEditMode = true;
                notifyDataSetChanged();
            }
        }
    }
}