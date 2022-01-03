package com.fluidtouch.noteshelf.shelf.viewholders;

import android.graphics.PointF;
import android.util.Log;
import android.view.DragEvent;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.fluidtouch.noteshelf.commons.utils.DateUtil;
import com.fluidtouch.noteshelf.documentframework.Utilities.FTConstants;
import com.fluidtouch.noteshelf.models.disk.diskItem.shelfItem.FTShelfItem;
import com.fluidtouch.noteshelf.shelf.adapters.FTBaseShelfAdapter;
import com.fluidtouch.noteshelf2.R;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;
import butterknife.OnTouch;

public class ShelfDocumentViewHolder extends BaseShelfViewHolder implements View.OnDragListener {
    @BindView(R.id.item_shelf_document_note_relative_layout)
    public RelativeLayout mNoteRelativeLayout;
    @BindView(R.id.item_shelf_document_title_text_view)
    public TextView mTitleTextView;
    @BindView(R.id.item_shelf_document_image_view)
    public ImageView mImageView;
    @BindView(R.id.item_shelf_document_rename_text_view)
    public TextView mRenameTextView;
    @BindView(R.id.item_shelf_document_date_text_view)
    public TextView mDateTextView;
    @BindView(R.id.shelf_item_text_view)
    public View textBottomView;
    @BindView(R.id.item_shelf_document_note_bg_image_view)
    ImageView mNoteBgImageView;
    @BindView(R.id.item_shelf_document_selection_image_view)
    ImageView mSelectionImageView;
    private FTBaseShelfAdapter.DocumentOnActionsListener mDocumentOnActionsListener;

    private GestureDetector gestureDetector;

    public ShelfDocumentViewHolder(@NonNull final View itemView, FTBaseShelfAdapter.DocumentOnActionsListener documentOnActionsListener) {
        super(itemView);
        ButterKnife.bind(this, itemView);

        this.mDocumentOnActionsListener = documentOnActionsListener;

        //setListeners
        mNoteRelativeLayout.setOnDragListener(this);
        gestureDetector = new GestureDetector(itemView.getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                mDocumentOnActionsListener.parentOnClick(getBindingAdapterPosition());
                return true;
            }

            @Override
            public void onLongPress(MotionEvent e) {
                super.onLongPress(e);
                mDocumentOnActionsListener.parentOnLongClick(getBindingAdapterPosition(), mNoteRelativeLayout, mImageView);
            }
        });
    }

    @Override
    public void setView(int position) {
        FTShelfItem item = ((FTBaseShelfAdapter) getBindingAdapter()).getItem(position);
        mNoteRelativeLayout.setTag(position);
        mTitleTextView.setText(item.getDisplayTitle(itemView.getContext()));
        mDateTextView.setText(DateUtil.getDateFormat(itemView.getContext().getString(R.string.format_dd_mmm_yyyy_hh_mm_a)).format(item.getFileModificationDate()));
        mNoteRelativeLayout.setAlpha(position == ((FTBaseShelfAdapter) getBindingAdapter()).mAlphaPosition ? ((FTBaseShelfAdapter) getBindingAdapter()).SHADOW_ALPHA : ((FTBaseShelfAdapter) getBindingAdapter()).FINAL_ALPHA);
        File file = new File(item.getFileURL().getPath(), FTConstants.COVER_SHELF_OVERLAY_IMAGE_NAME);
        if (file != null && file.exists()) {
            textBottomView.setVisibility(View.GONE);
        } else {
            textBottomView.setVisibility(View.VISIBLE);
        }
        ((FTBaseShelfAdapter) getBindingAdapter()).setUpDateView(mDateTextView);
        ((FTBaseShelfAdapter) getBindingAdapter()).setUpRenameView(mRenameTextView);
        ((FTBaseShelfAdapter) getBindingAdapter()).setUpImageView(mImageView, mSelectionImageView, item);
    }

    @OnClick(R.id.item_shelf_document_rename_text_view)
    protected void renameDocument() {
        this.mDocumentOnActionsListener.showRenameDialog(getBindingAdapterPosition());
    }

    @OnClick(R.id.item_shelf_document_note_relative_layout)
    protected void parentOnClick() {
        this.mDocumentOnActionsListener.parentOnClick(getBindingAdapterPosition());
    }

    private PointF initialTapPoint;

    @OnTouch(R.id.item_shelf_document_note_relative_layout)
    protected boolean onTouchParentLayout(View view, MotionEvent motionEvent) {
        gestureDetector.onTouchEvent(motionEvent);
        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
            initialTapPoint = new PointF(motionEvent.getX(), motionEvent.getY());
        } else if (motionEvent.getAction() == MotionEvent.ACTION_MOVE) {
            if (mDocumentOnActionsListener.isLongTapDetected() && getDistance(initialTapPoint, new PointF(motionEvent.getX(), motionEvent.getY())) > 15) {
                mDocumentOnActionsListener.dismissNotebookOptions();
                mDocumentOnActionsListener.startDragging(view, getBindingAdapterPosition());
            }
        }
        return true;
    }

    private double getDistance(PointF initialTapPoint, PointF finalTapPoint) {
        float dx = initialTapPoint.x - finalTapPoint.x;
        float dy = initialTapPoint.y - finalTapPoint.y;

        return Math.sqrt(dx * dx + dy * dy);
    }

    @OnLongClick(R.id.item_shelf_document_note_relative_layout)
    protected boolean parentOnLongClick(View view) {
        this.mDocumentOnActionsListener.parentOnLongClick(getBindingAdapterPosition(), view, mImageView);
        return true;
    }

    @Override
    public boolean onDrag(View view, DragEvent event) {
        int position = getBindingAdapterPosition();
        if (view.getId() == R.id.item_shelf_document_note_relative_layout) {
            this.mDocumentOnActionsListener.setUpDragActions(event, view, position, this);
        }

        return true;
    }

    @Override
    public void updateGroupingMode(int isItemBackgroundViewVisible) {
        mNoteBgImageView.setVisibility(isItemBackgroundViewVisible);
    }
}