package com.fluidtouch.noteshelf.documentproviders;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import com.fluidtouch.noteshelf.documentframework.FTUrl;
import com.fluidtouch.noteshelf.models.disk.diskItem.FTDiskItem;
import com.fluidtouch.noteshelf.models.disk.diskItem.shelfItem.FTDocumentItem;
import com.fluidtouch.noteshelf.models.disk.diskItem.shelfItem.FTGroupItem;
import com.fluidtouch.noteshelf.models.disk.diskItem.shelfItem.FTShelfItem;
import com.fluidtouch.noteshelf.shelf.enums.FTShelfItemCollectionType;
import com.fluidtouch.noteshelf.shelf.enums.FTShelfSortOrder;
import com.fluidtouch.noteshelf2.R;

import java.util.ArrayList;
import java.util.List;

public class FTShelfItemCollection extends FTDiskItem implements Parcelable {
    private List<FTShelfItem> children = new ArrayList<>();
    private FTShelfItemCollectionType collectionType = FTShelfItemCollectionType.SYSTEM;

    public FTShelfItemCollection(FTUrl fileURL) {
        super(fileURL);
    }

    protected FTShelfItemCollection(Parcel in) {
        super(in);
        children = in.createTypedArrayList(FTShelfItem.CREATOR);
        collectionType = FTShelfItemCollectionType.getType(in.readInt());
    }

    public static final Creator<FTShelfItemCollection> CREATOR = new Creator<FTShelfItemCollection>() {
        @Override
        public FTShelfItemCollection createFromParcel(Parcel in) {
            return new FTShelfItemCollection(in);
        }

        @Override
        public FTShelfItemCollection[] newArray(int size) {
            return new FTShelfItemCollection[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(children);
        dest.writeInt(collectionType.ordinal());
    }

    public boolean isTrash(Context context) {
        return getCollectionType().equals(FTShelfItemCollectionType.SYSTEM) && getDisplayTitle(context).equalsIgnoreCase(context.getString(R.string.trash));
    }

    public synchronized List<FTShelfItem> getChildren() {
        return children;
    }

    public void setChildren(List<FTShelfItem> children) {
        this.children = children;
    }

    public FTShelfItemCollectionType getCollectionType() {
        return collectionType;
    }

    public void setCollectionType(FTShelfItemCollectionType collectionType) {
        this.collectionType = collectionType;
    }


    public void addChild(FTShelfItem childItem) {
        this.children.add(childItem);
        childItem.parent = null;
        childItem.shelfCollection = this;

        if (childItem instanceof FTGroupItem) {
            FTGroupItem groupItem = (FTGroupItem) childItem;
            for (FTShelfItem item : groupItem.getChildren()) {
                item.parent = groupItem;
                item.shelfCollection = this;
            }
        }
    }

    public void removeChild(FTShelfItem childItem) {
        int index = getIndex(childItem);

        if (index >= 0) {
            childItem.parent = null;
            childItem.shelfCollection = null;
            if (childItem instanceof FTGroupItem) {
                FTGroupItem groupItem = (FTGroupItem) childItem;
                for (FTShelfItem item : groupItem.getChildren()) {
                    item.parent = null;
                    item.shelfCollection = null;
                }
            }

            this.children.remove(childItem);
        }
    }

    public synchronized void shelfItems(Context context, FTShelfSortOrder sortOrder, FTGroupItem parent, String searchKey, ShelfNotebookItemsAndErrorBlock onCompletion) {
        //Subclass should implement this
    }

    public void addShelfItemForDocument(Context context, String toTitle, FTGroupItem toGroup, FTDocumentItemAndErrorBlock onCompletion, FTUrl fileURL) {
        //Subclass should implement this
    }

    public void removeShelfItems(Context context, ShelfNotebookItemsAndErrorBlock onCompletion, List<FTShelfItem> shelfItems) {

    }

    public void moveShelfItem(FTShelfItem notebook, FTGroupItem toGroup, FTMoveShelfItemBlock onCompletion, Context context) {
        //Subclass should implement this
    }

    public void renameShelfItem(Context context, String toTitle, ShelfNotebookAndErrorBlock onCompletion, FTShelfItem shelfItem) {

    }

    public void createGroupItem(Context context, List<FTShelfItem> shelfItemsToGroup, FTGroupCreationBlock onCompletion, String groupName) {
        //Subclass should implement this
    }

    public FTGroupItem createGroupItem(String groupName) {
        return null;
    }

    public Boolean moveItemInCache(FTDiskItem shelfItem, FTUrl toURL) {
        //Subclass should implement this
        return false;
    }

    private int getIndex(FTShelfItem childItem) {
        for (int i = 0; i < this.children.size(); i++) {
            if (this.children.get(i).getFileURL() == childItem.getFileURL()) {
                return i;
            }
        }

        return -1;
    }

    public interface FTGroupCreationBlock {
        void didCreateGroup(FTGroupItem groupItem, Error error);
    }

    public interface FTMoveShelfItemBlock {
        void didMoveToGroup(FTGroupItem groupItem, Error error);
    }

    public interface FTDocumentItemAndErrorBlock {
        void didFinishAddingItem(FTDocumentItem documentItem, Error error);
    }

    public interface ShelfNotebookItemsAndErrorBlock {
        public void didFinishWithNotebookItems(List<FTShelfItem> notebooks, Error error);
    }

    public interface ShelfNotebookAndErrorBlock {
        public void didFinishWithNotebookItem(FTShelfItem movedBook, Error error);
    }
}
