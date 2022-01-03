package com.fluidtouch.noteshelf.shelf.viewholders;

import android.content.Context;
import android.net.Uri;
import android.view.DragEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.fluidtouch.noteshelf.commons.utils.BitmapUtil;
import com.fluidtouch.noteshelf.commons.utils.DateUtil;
import com.fluidtouch.noteshelf.documentframework.Utilities.FTConstants;
import com.fluidtouch.noteshelf.models.disk.diskItem.shelfItem.FTGroupItem;
import com.fluidtouch.noteshelf.shelf.adapters.FTBaseShelfAdapter;
import com.fluidtouch.noteshelf2.R;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;

public class ShelfGroupViewHolder extends BaseShelfViewHolder implements View.OnDragListener {
    @BindView(R.id.item_shelf_group_note_relative_layout)
    public RelativeLayout mNoteRelativeLayout;
    @BindView(R.id.item_shelf_group_title_text_view)
    public TextView mTitleTextView;
    @BindView(R.id.item_shelf_group_image_view)
    public ImageView mImageView;
    @BindView(R.id.item_shelf_group_second_image_view)
    public ImageView mSecondImageView;
    @BindView(R.id.item_shelf_group_third_image_view)
    public ImageView mThirdImageView;
    @BindView(R.id.item_shelf_group_rename_text_view)
    public TextView mRenameTextView;
    @BindView(R.id.item_shelf_group_date_text_view)
    public TextView mDateTextView;
    @BindView(R.id.shelf_item_text_view)
    public View textBottomView;
    @BindView(R.id.item_shelf_group_note_bg_image_view)
    protected ImageView mGroupBgImageView;

    private FTBaseShelfAdapter.GroupOnActionsListener mOnActionsListener;
    private final String NOTE_IMAGE_NAME = FTConstants.COVER_SHELF_IMAGE_NAME;

    public ShelfGroupViewHolder(@NonNull View itemView, FTBaseShelfAdapter.GroupOnActionsListener onActionsListener) {
        super(itemView);
        ButterKnife.bind(this, itemView);

        this.mOnActionsListener = onActionsListener;

        //setListeners
        mNoteRelativeLayout.setOnDragListener(this);
    }

    @Override
    public void setView(int position) {
        Context context = itemView.getContext();
        FTGroupItem item = (FTGroupItem) ((FTBaseShelfAdapter) getBindingAdapter()).getItem(position);
        mNoteRelativeLayout.setTag(position);
        File file = new File(item.getFileURL().getPath(), FTConstants.COVER_SHELF_OVERLAY_IMAGE_NAME);
        if (file != null && file.exists()) {
            textBottomView.setVisibility(View.GONE);
        } else {
            textBottomView.setVisibility(View.VISIBLE);
        }
        mTitleTextView.setText(item.getDisplayTitle(context));
        mDateTextView.setText(DateUtil.getDateFormat(context.getString(R.string.format_dd_mmm_yyyy_hh_mm_a)).format(item.getFileModificationDate()));

        mImageView.setImageResource(R.drawable.notebook);
        if (item.getChildren().size() > 0) {
            mImageView.setImageBitmap(BitmapUtil.getRoundedCornerBitmap(context, BitmapUtil.getBitmap(Uri.withAppendedPath(Uri.parse(item.getChildren().get(0).getFileURL().getPath()), NOTE_IMAGE_NAME)), context.getResources().getInteger(R.integer.three)));
            File file1 = new File(item.getChildren().get(0).getFileURL().getPath(), FTConstants.COVER_SHELF_OVERLAY_IMAGE_NAME);
            if (file1 != null && file1.exists()) {
                textBottomView.setVisibility(View.GONE);
            } else {
                textBottomView.setVisibility(View.VISIBLE);
            }
        }

        if (item.getChildren().size() >= 2) {
            mSecondImageView.setImageBitmap(BitmapUtil.getRoundedCornerBitmap(context, BitmapUtil.getBitmap(Uri.withAppendedPath(Uri.parse(item.getChildren().get(1).getFileURL().getPath()), NOTE_IMAGE_NAME)), context.getResources().getInteger(R.integer.three)));
            if (item.getChildren().size() >= 3) {
                mThirdImageView.setImageBitmap(BitmapUtil.getRoundedCornerBitmap(context, BitmapUtil.getBitmap(Uri.withAppendedPath(Uri.parse(item.getChildren().get(2).getFileURL().getPath()), NOTE_IMAGE_NAME)), context.getResources().getInteger(R.integer.three)));
            } else {
                mThirdImageView.setImageResource(R.drawable.covergray);
            }
        } else {
            mSecondImageView.setImageResource(R.drawable.covergray);
        }

        ((FTBaseShelfAdapter) getBindingAdapter()).setUpDateView(mDateTextView);
        ((FTBaseShelfAdapter) getBindingAdapter()).setUpRenameView(mRenameTextView);
    }

    @OnClick(R.id.item_shelf_group_rename_text_view)
    protected void renameGroup() {
        this.mOnActionsListener.showRenameDialog(getAdapterPosition());
    }

    @OnClick(R.id.item_shelf_group_note_relative_layout)
    protected void showGroupItems() {
        this.mOnActionsListener.showGroupItems(getAdapterPosition());
    }

    @OnLongClick(R.id.item_shelf_group_note_relative_layout)
    protected boolean parentOnLongClick(View view) {
        mOnActionsListener.parentOnLongClick(getBindingAdapterPosition(),view, mImageView);
        return true;
    }

    @Override
    public boolean onDrag(View view, DragEvent event) {
        int position = getAdapterPosition();
        if (view.getId() == R.id.item_shelf_group_note_relative_layout) {
            this.mOnActionsListener.setUpDragActions(event, view, position, this);
        }
        return true;
    }

    @Override
    public void updateGroupingMode(int isItemBackgroundViewVisible) {
        mGroupBgImageView.setVisibility(isItemBackgroundViewVisible);
    }
}