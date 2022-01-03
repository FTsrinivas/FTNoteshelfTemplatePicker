package com.fluidtouch.noteshelf.shelf.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf.documentframework.FTUrl;
import com.fluidtouch.noteshelf.documentproviders.FTShelfItemCollection;
import com.fluidtouch.noteshelf.shelf.viewholders.FTCategoryGroupViewHolder;
import com.fluidtouch.noteshelf.shelf.viewholders.FTCategoryViewHolder;
import com.fluidtouch.noteshelf2.R;
import com.thoughtbot.expandablerecyclerview.ExpandableRecyclerViewAdapter;
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;

import java.util.List;

/**
 * Created by sreenu on 04/08/20.
 */
public class FTCategoryAdapter extends ExpandableRecyclerViewAdapter<FTCategoryGroupViewHolder, FTCategoryViewHolder> {
    public String displayTitle = FTApp.getPref().getRecentCollectionName();
    private Context mContext;
    private NavigationItemListener mNavigationItemListener;
    public boolean isCollectionEditable = true;

    private CategoryOnActionsLister categoryOnActionsLister = new CategoryOnActionsLister() {
        @Override
        public void onCollectionSelected(FTShelfItemCollection selectedCollection) {
            displayTitle = selectedCollection.getDisplayTitle(getContext());
            notifyDataSetChanged();
            selectCollection(selectedCollection);
        }

        @Override
        public void selectCollection(FTShelfItemCollection selectedCollection) {
            mNavigationItemListener.onShelfItemCollectionSelected(selectedCollection);
        }

        @Override
        public void onBookSelected(FTUrl fileURL) {
            mNavigationItemListener.hideNavigationDrawer();
            mNavigationItemListener.openSelectedDocument(fileURL, 0);
        }

        @Override
        public void removeItem(int childIndex, FTShelfItemCollection currentCollection) {
            mNavigationItemListener.removeCollectionItem(childIndex, currentCollection);
        }

        @Override
        public void renameItem(String toCategoryName, int childIndex, FTShelfItemCollection currentCollection) {
            mNavigationItemListener.renameCollectionItem(toCategoryName, childIndex, currentCollection);
        }

        @Override
        public void pinNotebook(FTUrl fileURL) {
            mNavigationItemListener.pinNotebook(fileURL);
        }

        @Override
        public void removeFromRecents(FTUrl fileURL) {
            mNavigationItemListener.removeFromRecents(fileURL);
        }

        @Override
        public void unpinNotebook(FTUrl fileURL) {
            mNavigationItemListener.unpinNotebook(fileURL);
        }
    };

    public FTCategoryAdapter(List<? extends ExpandableGroup> groups, Context context, NavigationItemListener navigationItemListener) {
        this(groups, context, true, navigationItemListener);
    }

    public FTCategoryAdapter(List<? extends ExpandableGroup> groups, Context context, boolean isCollectionEditable, NavigationItemListener navigationItemListener) {
        super(groups);
        this.mContext = context;
        this.mNavigationItemListener = navigationItemListener;
        this.isCollectionEditable = isCollectionEditable;
    }

    @Override
    public FTCategoryGroupViewHolder onCreateGroupViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_nd_group_recycler_view, parent, false);
        return new FTCategoryGroupViewHolder(view);
    }

    @Override
    public FTCategoryViewHolder onCreateChildViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_nd_recycler_view, parent, false);
        return new FTCategoryViewHolder(view, categoryOnActionsLister);
    }

    @Override
    public void onBindChildViewHolder(FTCategoryViewHolder holder, int flatPosition, ExpandableGroup group, int childIndex) {
        FTShelfItemCollection collection = (FTShelfItemCollection) group.getItems().get(childIndex);
        holder.setView(group, childIndex, displayTitle);
    }

    @Override
    public void onBindGroupViewHolder(FTCategoryGroupViewHolder holder, int flatPosition, ExpandableGroup group) {
        holder.setGroupName(group.getTitle());
    }

    private Context getContext() {
        return mContext;
    }

    public void expandAllGroups() {
        for (int i = 0; i < getGroups().size(); i++) {
            if (!isGroupExpanded(getGroups().get(i))) {
                onGroupClick(expandableList.getFlattenedGroupIndex(i));
            }
        }
    }

    public interface CategoryOnActionsLister {
        void onCollectionSelected(FTShelfItemCollection selectedCollection);

        void selectCollection(FTShelfItemCollection selectedCollection);

        void onBookSelected(FTUrl fileURL);

        void removeItem(int childIndex, FTShelfItemCollection currentCollection);

        void renameItem(String toCategoryName, int childIndex, FTShelfItemCollection currentCollection);

        void pinNotebook(FTUrl fileURL);

        void removeFromRecents(FTUrl fileURL);

        void unpinNotebook(FTUrl fileURL);
    }

    public interface NavigationItemListener {
        void onShelfItemCollectionSelected(FTShelfItemCollection collection);

        void renameCollectionItem(String categoryName, int position, FTShelfItemCollection item);

        void removeCollectionItem(int position, FTShelfItemCollection item);

        void hideNavigationDrawer();

        void openSelectedDocument(FTUrl fileURL, int from);

        void pinNotebook(FTUrl fileURL);

        void removeFromRecents(FTUrl fileURL);

        void unpinNotebook(FTUrl fileURL);
    }
}
