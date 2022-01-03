package com.fluidtouch.noteshelf.shelf.fragments;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf.commons.utils.ScreenUtil;
import com.fluidtouch.noteshelf.models.disk.diskItem.shelfItem.FTDocumentItem;
import com.fluidtouch.noteshelf.models.disk.diskItem.shelfItem.FTShelfItem;
import com.fluidtouch.noteshelf.preferences.SystemPref;
import com.fluidtouch.noteshelf.shelf.activities.FTGridLayoutManager;
import com.fluidtouch.noteshelf.shelf.adapters.FTBaseShelfAdapter;
import com.fluidtouch.noteshelf.shelf.adapters.FTShelfGroupableAdapter;
import com.fluidtouch.noteshelf.shelf.enums.FTShelfSortOrder;
import com.fluidtouch.noteshelf.shelf.listeners.ShelfOnEditModeChangedListener;
import com.fluidtouch.noteshelf.shelf.listeners.ShelfOnGroupingActionsListener;
import com.fluidtouch.noteshelf2.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

public abstract class FTBaseShelfFragment extends Fragment implements ShelfOnGroupingActionsListener {
    @BindView(R.id.shelf_recycler_view)
    protected RecyclerView mShelfItemsRecyclerView;

    protected FTBaseShelfAdapter mShelfItemsAdapter;
    protected FTBaseShelfAdapter.ShelfAdapterToActivityListener adapterToActivityListener;
    private ShelfOnEditModeChangedListener mShelfOnEditModeChangedListener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapterToActivityListener = (FTBaseShelfAdapter.ShelfAdapterToActivityListener) getActivity();
        mShelfOnEditModeChangedListener = (ShelfOnEditModeChangedListener) getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.content_shelf, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        ButterKnife.bind(this, view);

        if (adapterToActivityListener.getCurrentShelfItemCollection() == null)
            initializeViewRelated(new ArrayList<>());
        else
            adapterToActivityListener.getCurrentShelfItemCollection().shelfItems(getContext(), FTShelfSortOrder.BY_NAME, adapterToActivityListener.getCurrentGroupItem(), "", (notebooks, error) -> {
                initializeViewRelated(notebooks);
            });
    }

    private void initializeViewRelated(List<FTShelfItem> notebooks) {
        GridLayoutManager manager = new FTGridLayoutManager(getContext(), getResources().getDimensionPixelOffset(R.dimen.new_258dp));
        mShelfItemsRecyclerView.setVisibility(View.VISIBLE);
        mShelfItemsRecyclerView.setLayoutManager(manager);
        mShelfItemsRecyclerView.addItemDecoration(new ShelfSeparatorDecorator(6, 10));

        mShelfItemsAdapter = new FTShelfGroupableAdapter(getContext(), mShelfOnEditModeChangedListener, isInsideGroup(), this, adapterToActivityListener);
        mShelfItemsAdapter.addAll(notebooks);
        mShelfItemsRecyclerView.setAdapter(mShelfItemsAdapter);

        if (getActivity().getIntent().getBooleanExtra(getString(R.string.intent_is_edit_mode), false)) {
            mShelfItemsAdapter.mIsInEditMode = true;
            mShelfOnEditModeChangedListener.onEditModeChanged(true, 0);
        }

        final int position = isInsideGroup() ? FTApp.getPref().getLastGroupDocumentPosition() : FTApp.getPref().getLastDocumentPosition();
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (position > -1) {
                ((GridLayoutManager) Objects.requireNonNull(mShelfItemsRecyclerView.getLayoutManager())).scrollToPositionWithOffset(position, 0);
            }
        }, 50);
    }

    private boolean isInsideGroup() {
        return adapterToActivityListener.getCurrentGroupItem() != null;
    }

    @Override
    public void startGrouping(FTShelfItem draggingItem, FTShelfItem mergingItem, int draggingPosition, int endPosition) {

    }

    public FTShelfItem getItemByPosition(int position) {
        return mShelfItemsAdapter.getItem(position);
    }

    public boolean isInEditMode() {
        return mShelfItemsAdapter.mIsInEditMode;
    }

    public void setEditMode() {
        mShelfItemsAdapter.mIsInEditMode = true;
        mShelfItemsAdapter.notifyDataSetChanged();
    }

    public void doneEditMode() {
        mShelfItemsAdapter.doneWithChanges();
    }

    public void onRestart() {
        mShelfItemsAdapter.isShowingDate = FTApp.getPref().get(SystemPref.IS_SHOWING_DATE, true);
    }

    public void updateItems(List<FTShelfItem> notebooks) {
        mShelfItemsAdapter.updateAll(notebooks);
    }

    public void addNewItem(FTShelfItem shelfItem) {
        mShelfItemsAdapter.add(0, shelfItem);
    }

    public void moveItems(List<FTShelfItem> selectedItems) {
        mShelfItemsAdapter.moveInEditMode(selectedItems);
    }

    public void onShowDateModified(boolean isChecked) {
        mShelfItemsAdapter.isShowingDate = isChecked;
        mShelfItemsAdapter.notifyDataSetChanged();
    }

    public void selectAllItems() {
        mShelfItemsAdapter.selectAll();
    }

    public void selectNone() {
        mShelfItemsAdapter.selectNone();
    }

    public void addOptionsItem(List<FTShelfItem> selectedItems) {
        mShelfItemsAdapter.addOptionsItem(selectedItems);
    }

    public void updateCoverStyleForSelectedItems(List<FTShelfItem> selectedItems) {
        mShelfItemsAdapter.coverStyleInEditMode(getSelectedItemsPositions(), selectedItems);
    }

    public void duplicateSelectedItems(List<FTShelfItem> selectedItems) {
        mShelfItemsAdapter.duplicateInEditMode(selectedItems);
    }

    public void deleteSelectedItems(List<FTShelfItem> selectedItems) {
        mShelfItemsAdapter.deleteInEditMode(selectedItems);
    }

    public Map<String, String> getSelecteditems() {
        return mShelfItemsAdapter.mSelectedMap;
    }

    public ArrayList<Integer> getSelectedItemsPositions() {
        ArrayList<Integer> selectedPositions = new ArrayList<>();
        List<FTShelfItem> list = mShelfItemsAdapter.getAll();

        for (int i = 0; i < list.size(); i++) {
            if (mShelfItemsAdapter.mSelectedMap.containsKey(list.get(i).getUuid())) {
                selectedPositions.add(i);
            }
        }
        return selectedPositions;
    }

    public void scrollToNewlyCreatedItem(final FTDocumentItem item) {
        List<FTShelfItem> list = mShelfItemsAdapter.getAll();
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getUuid().equals(item.getUuid())) {
                mShelfItemsRecyclerView.smoothScrollToPosition(i);
                FTApp.getPref().saveLastDocumentPosition(i);
                new Handler(Looper.getMainLooper()).postDelayed(() -> adapterToActivityListener.openSelectedDocument(item.getFileURL(), 1), 500);
                break;
            }
        }
    }

    private class ShelfSeparatorDecorator extends RecyclerView.ItemDecoration {
        private Drawable mDivider;
        private int mNo_of_col;
        private int offset;

        ShelfSeparatorDecorator(int mNo_of_col, int offset) {
            mDivider = getResources().getDrawableForDensity(R.drawable.shelf_bg, DisplayMetrics.DENSITY_XHIGH);
            this.mNo_of_col = mNo_of_col;
            this.offset = offset;
        }

        @Override
        public void onDrawOver(@NonNull Canvas canvas, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
            drawVertical(canvas, parent);
        }

        /**
         * Draw dividers at each expected grid interval
         */
        private void drawVertical(Canvas canvas, RecyclerView parent) {
            if (parent.getChildCount() == 0) return;

            final int childCount = parent.getChildCount();

            for (int i = 0; i < childCount; i++) {
                final View child = parent.getChildAt(i);
                final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();

                final int left = child.getLeft() - params.leftMargin;
                final int right = child.getRight() + params.rightMargin;
                final int top = child.getBottom() + params.bottomMargin;
                final int bottom = top + mDivider.getIntrinsicHeight();
                if (i == childCount - 1) {
                    mDivider.setBounds(left, top, ScreenUtil.getScreenWidth(getContext()), bottom);
                } else {
                    mDivider.setBounds(left, top, right, bottom);
                }
                mDivider.setAlpha(250);
                mDivider.draw(canvas);
            }
        }

        @Override
        public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
            outRect.set(0, ScreenUtil.convertDpToPx(view.getContext(), offset), 0, 0);
        }
    }
}
