package com.fluidtouch.noteshelf.shelf.adapters;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Context;
import android.os.Build;
import android.view.DragEvent;
import android.view.View;

import com.fluidtouch.noteshelf.models.disk.diskItem.shelfItem.FTShelfItem;
import com.fluidtouch.noteshelf.shelf.enums.RKShelfItemType;
import com.fluidtouch.noteshelf.shelf.listeners.ShelfOnEditModeChangedListener;
import com.fluidtouch.noteshelf.shelf.listeners.ShelfOnGroupingActionsListener;
import com.fluidtouch.noteshelf.shelf.viewholders.BaseShelfViewHolder;

public class FTShelfGroupableAdapter extends FTBaseShelfAdapter {
    private ShelfOnGroupingActionsListener mShelfAdapterListener;

    public FTShelfGroupableAdapter(Context context, ShelfOnEditModeChangedListener shelfOnEditModeChangedListener, boolean isGroupable, ShelfOnGroupingActionsListener shelfAdapterListener, ShelfAdapterToActivityListener shelfAdapterToActivityListener) {
        super(context, shelfOnEditModeChangedListener, isGroupable, shelfAdapterToActivityListener);
        this.mShelfAdapterListener = shelfAdapterListener;
    }

    @Override
    protected void startDraggingItem(View view, int position) {
        view.setAlpha(SHADOW_ALPHA);
        mDraggingPosition = position;
        mAlphaPosition = position;

        //@ToDo: Check using the canvas using bitmap of VIEW for customising the VIEW
        View.DragShadowBuilder mShadow = new View.DragShadowBuilder(view);
        ClipData.Item item = new ClipData.Item(view.getTag().toString());
        String[] mimeTypes = {ClipDescription.MIMETYPE_TEXT_PLAIN};
        ClipData data = new ClipData(view.getTag().toString(), mimeTypes, item);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            view.startDragAndDrop(data, mShadow, null, 0);
        } else {
            view.startDrag(data, mShadow, null, 0);
        }
    }

    @Override
    protected void setUpDragActions(DragEvent event, View view, int position, BaseShelfViewHolder holder) {
        boolean isTrash = false;
        if (event.getAction() != DragEvent.ACTION_DRAG_ENDED) {
            isTrash = getItem(position).shelfCollection.isTrash(holder.itemView.getContext());
        }
        switch (event.getAction()) {
            case DragEvent.ACTION_DRAG_ENTERED:
                if (mDraggingPosition != position && !isTrash) {
                    holder.updateGroupingMode(View.VISIBLE);
                    view.invalidate();
                }
                break;

            case DragEvent.ACTION_DRAG_EXITED:
                if (mDraggingPosition != position && !isTrash) {
                    holder.updateGroupingMode(View.GONE);
                    view.invalidate();
                }
                break;

            case DragEvent.ACTION_DRAG_ENDED:
                mAlphaPosition = ALPHA_DEFAULT_POSITION;
                notifyItemChanged(position);
                break;

            case DragEvent.ACTION_DROP:
                if (mDraggingPosition != position && !isTrash) {
                    holder.updateGroupingMode(View.GONE);
                    setUpDragEnd(position);
                } else {
                    mAlphaPosition = ALPHA_DEFAULT_POSITION;
                    notifyItemChanged(position);
                }
                break;

            default:
                break;
        }
    }

    private void setUpDragEnd(int endPosition) {
        FTShelfItem draggingItem = getItem(mDraggingPosition);
        FTShelfItem endItem = getItem(endPosition);
        mShelfAdapterListener.startGrouping(draggingItem, endItem, mDraggingPosition, endPosition);
    }

    public void onGroupingFinished(int draggingPosition, int endPosition, FTShelfItem groupedItem) {
        FTShelfItem draggingItem = getItem(draggingPosition);
        FTShelfItem endItem = getItem(endPosition);

        if (endItem.getType() == RKShelfItemType.DOCUMENT && mSelectedMap.containsKey(endItem.getUuid())) {
            mSelectedMap.remove(endItem.getUuid());
        }
        if (mSelectedMap.containsKey(draggingItem.getUuid())) {
            mSelectedMap.remove(draggingItem.getUuid());
        }
        update(endPosition, groupedItem);
        remove(draggingItem);
        refreshAdapter(getAll());
        updateSelectedShelfItemsCount();
    }
}
