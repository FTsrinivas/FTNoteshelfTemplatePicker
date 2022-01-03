package com.fluidtouch.noteshelf.shelf.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.DragEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.fluidtouch.noteshelf.commons.utils.FTPermissionManager;
import com.fluidtouch.noteshelf.documentproviders.FTShelfItemCollection;
import com.fluidtouch.noteshelf.models.disk.diskItem.shelfItem.FTGroupItem;
import com.fluidtouch.noteshelf.models.disk.diskItem.shelfItem.FTShelfItem;
import com.fluidtouch.noteshelf.models.theme.FTNTheme;
import com.fluidtouch.noteshelf.models.theme.FTNThemeCategory;
import com.fluidtouch.noteshelf.shelf.adapters.FTShelfGroupableAdapter;
import com.fluidtouch.noteshelf2.R;

import java.util.ArrayList;
import java.util.List;

public class FTShelfGroupableActivity {}/* extends FTBaseShelfActivity implements View.OnDragListener {
    public static final int SHELF_GROUPABLE_ACTIVITY = 200;
    private final int DOWN_SCROLL_DISTANCE = 150;
    private final int UP_SCROLL_DISTANCE = -150;
    private final int HORIZONTAL_SCROLL_DISTANCE = 0;
    private final int SCROLL_REQUEST_DELAY_IN_MS = 60;
    //region class variables
    private boolean isUpDragFinished;
    private boolean isDownDragFinished;
    //endregion

    public static void start(Context context, String groupFilePath, boolean isInEditMode, int position) {
        Intent intent = new Intent(context, FTShelfGroupableActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(context.getString(R.string.intent_notes_group_uri), groupFilePath);
        bundle.putBoolean(context.getString(R.string.intent_is_edit_mode), isInEditMode);
        bundle.putInt(context.getString(R.string.intent_position), position);
        intent.putExtras(bundle);
        ((AppCompatActivity) context).startActivityForResult(intent, SHELF_GROUPABLE_ACTIVITY);
    }

    //region Activity life cycle methods
//    @Override
//    protected void onNewIntent(Intent intent) {
//        super.onNewIntent(intent);
//        if (intent.getClipData() != null) {
//            super.onPickingDocumentFromDevice(intent);
//        }
//    }
    //endregion


    @Override
    protected void initializeViewRelated(List<FTShelfItem> notebooks) {
        super.initializeViewRelated(notebooks);

        //Setting up listeners
        mDownScrollingControlTextView.setOnDragListener(this);
        mUpScrollingControlTextView.setOnDragListener(this);
    }

    //region Manual scrolling for drag n drop
    private void smoothControlToDown() {
        isDownDragFinished = false;
        mShelfItemsRecyclerView.smoothScrollBy(HORIZONTAL_SCROLL_DISTANCE, DOWN_SCROLL_DISTANCE);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isDownDragFinished) {
                    smoothControlToDown();
                }
            }
        }, SCROLL_REQUEST_DELAY_IN_MS);
    }

    private void smoothControlToUp() {
        isUpDragFinished = false;
        mShelfItemsRecyclerView.smoothScrollBy(HORIZONTAL_SCROLL_DISTANCE, UP_SCROLL_DISTANCE);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isUpDragFinished) {
                    smoothControlToUp();
                }
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
        if (mergingItem instanceof FTGroupItem) {
            this.mCurrentShelfItemCollection.moveShelfItem(draggingItem, (FTGroupItem) mergingItem, new FTShelfItemCollection.FTMoveShelfItemBlock() {
                @Override
                public void didMoveToGroup(FTGroupItem groupItem, Error error) {
                    ((FTShelfGroupableAdapter) mShelfItemsAdapter).onGroupingFinished(draggingPosition, endPosition, groupItem);
                }
            }, getContext());
        } else {
            ArrayList<FTShelfItem> items = new ArrayList<>();
            items.add(mergingItem);
            items.add(draggingItem);

            setPreviousSelectedPaths(items);
            this.mCurrentShelfItemCollection.createGroupItem(getContext(), items, new FTShelfItemCollection.FTGroupCreationBlock() {
                @Override
                public void didCreateGroup(FTGroupItem groupItem, Error error) {
                    afterPathsChanged(groupItem.getChildren());
                    ((FTShelfGroupableAdapter) mShelfItemsAdapter).onGroupingFinished(draggingPosition, endPosition, groupItem);
                }
            }, getString(R.string.group));
        }
    }

    private Context getContext() {
        return this;
    }

    @Override
    public void onStylusEnabled() {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_ADD_COVER_THEME)
            pickFromGallery(requestCode);
    }

    @Override
    public void addCustomTheme(FTNTheme theme) {
        Log.d("TemlatePicker==>"," ftThemeType::-"+theme.ftThemeType);
        if (theme.ftThemeType == FTNThemeCategory.FTThemeType.COVER) {
            if (FTPermissionManager.checkPermission(this, this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_ADD_COVER_THEME)) {
                pickFromGallery(REQUEST_CODE_ADD_COVER_THEME);
            }
        } else {
            if (FTPermissionManager.checkPermission(this, this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_ADD_PAPER_THEME)) {
                importDocument(REQUEST_CODE_ADD_PAPER_THEME);
            }
        }
    }

    //endregion

    public static void dummy() {


    }

    public void pickFromGallery(int requestCode) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);//
        intent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), requestCode);
    }

    @Override
    public void updateBackupType() {

    }

}*/
