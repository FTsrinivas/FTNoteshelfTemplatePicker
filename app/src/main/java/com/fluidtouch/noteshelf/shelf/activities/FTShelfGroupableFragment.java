package com.fluidtouch.noteshelf.shelf.activities;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.DragEvent;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.fluidtouch.noteshelf.models.disk.diskItem.shelfItem.FTGroupItem;
import com.fluidtouch.noteshelf.models.disk.diskItem.shelfItem.FTShelfItem;
import com.fluidtouch.noteshelf.shelf.adapters.FTShelfGroupableAdapter;
import com.fluidtouch.noteshelf.shelf.fragments.FTBaseShelfFragment;
import com.fluidtouch.noteshelf2.R;

import java.util.ArrayList;

import butterknife.BindView;

public class FTShelfGroupableFragment extends FTBaseShelfFragment implements View.OnDragListener {
    @BindView(R.id.shelf_drag_drop_down_control_text_view)
    protected TextView mDownScrollingControlTextView;
    @BindView(R.id.shelf_drag_drop_up_control_text_view)
    protected TextView mUpScrollingControlTextView;

    private final int DOWN_SCROLL_DISTANCE = 150;
    private final int UP_SCROLL_DISTANCE = -150;
    private final int HORIZONTAL_SCROLL_DISTANCE = 0;
    private final int SCROLL_REQUEST_DELAY_IN_MS = 60;
    //region class variables
    private boolean isUpDragFinished;
    private boolean isDownDragFinished;
    //endregion

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mDownScrollingControlTextView.setOnDragListener(this);
        mUpScrollingControlTextView.setOnDragListener(this);
    }

    //region Manual scrolling for drag n drop
    private void smoothControlToDown() {
        isDownDragFinished = false;
        mShelfItemsRecyclerView.smoothScrollBy(HORIZONTAL_SCROLL_DISTANCE, DOWN_SCROLL_DISTANCE);
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (!isDownDragFinished) {
                smoothControlToDown();
            }
        }, SCROLL_REQUEST_DELAY_IN_MS);
    }

    private void smoothControlToUp() {
        isUpDragFinished = false;
        mShelfItemsRecyclerView.smoothScrollBy(HORIZONTAL_SCROLL_DISTANCE, UP_SCROLL_DISTANCE);
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (!isUpDragFinished) {
                smoothControlToUp();
            }
        }, SCROLL_REQUEST_DELAY_IN_MS);
    }
    //endregion

    //region Grouping operations
    @Override
    public boolean onDrag(View view, DragEvent event) {
        if (view.getId() == R.id.shelf_drag_drop_down_control_text_view) {
            if (event.getAction() == DragEvent.ACTION_DRAG_ENTERED) {
                smoothControlToDown();
            } else if (event.getAction() == DragEvent.ACTION_DRAG_EXITED || event.getAction() == DragEvent.ACTION_DRAG_ENDED) {
                isDownDragFinished = true;
                mShelfItemsRecyclerView.smoothScrollBy(0, 0);
            }
        } else if (view.getId() == R.id.shelf_drag_drop_up_control_text_view) {
            if (event.getAction() == DragEvent.ACTION_DRAG_ENTERED) {
                smoothControlToUp();
            } else if (event.getAction() == DragEvent.ACTION_DRAG_EXITED || event.getAction() == DragEvent.ACTION_DRAG_ENDED) {
                isUpDragFinished = true;
                mShelfItemsRecyclerView.smoothScrollBy(0, 0);
            }
        }

        return true;
    }

    @Override
    public void startGrouping(FTShelfItem draggingItem, FTShelfItem mergingItem, final int draggingPosition, final int endPosition) {
        if (adapterToActivityListener.getCurrentGroupItem() != null) return;
        if (mergingItem instanceof FTGroupItem) {
            adapterToActivityListener.getCurrentShelfItemCollection().moveShelfItem(draggingItem, (FTGroupItem) mergingItem, (groupItem, error) -> ((FTShelfGroupableAdapter) mShelfItemsAdapter).onGroupingFinished(draggingPosition, endPosition, groupItem), getContext());
        } else {
            ArrayList<FTShelfItem> items = new ArrayList<>();
            items.add(mergingItem);
            items.add(draggingItem);

            adapterToActivityListener.getCurrentShelfItemCollection().createGroupItem(getContext(), items, (groupItem, error) -> {
                ((FTShelfGroupableAdapter) mShelfItemsAdapter).onGroupingFinished(draggingPosition, endPosition, groupItem);
            }, getString(R.string.group));
        }
        if (getActivity() != null) ((FTBaseShelfActivity) getActivity()).enableBackupPublishing();
    }
}
