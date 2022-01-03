package com.fluidtouch.noteshelf.shelf.viewholders;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf.commons.ui.FTDialogFactory;
import com.fluidtouch.noteshelf.commons.utils.BitmapUtil;
import com.fluidtouch.noteshelf.commons.utils.FTPopupFactory;
import com.fluidtouch.noteshelf.documentframework.Utilities.FTConstants;
import com.fluidtouch.noteshelf.documentproviders.FTShelfItemCollection;
import com.fluidtouch.noteshelf.models.disk.diskItem.shelfItem.FTShelfItem;
import com.fluidtouch.noteshelf.shelf.adapters.FTCategoryAdapter;
import com.fluidtouch.noteshelf.shelf.enums.FTShelfSortOrder;
import com.fluidtouch.noteshelf.shelf.fragments.FTRenameDialog;
import com.fluidtouch.noteshelf2.R;
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;
import com.thoughtbot.expandablerecyclerview.viewholders.ChildViewHolder;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;

public class FTCategoryViewHolder extends ChildViewHolder {
    public static final String CATEGORIES = FTApp.getInstance().getCurActCtx().getString(R.string.categories);
    public static final String RECENT = FTApp.getInstance().getCurActCtx().getString(R.string.recent);
    public static final String PINNED = FTApp.getInstance().getCurActCtx().getString(R.string.pinned);

    @BindView(R.id.item_nd_item_image_view)
    ImageView mImageView;
    @BindView(R.id.item_nd_item_title_text_view)
    TextView mTitleTextView;
    @BindView(R.id.item_nd_item_layout)
    LinearLayout mParentLayout;

    private FTShelfItemCollection currentCollection;
    private FTCategoryAdapter.CategoryOnActionsLister mCategoryOnActionsLister;
    private FTCategoryGroupType groupType = FTCategoryGroupType.COLLECTION;
    private int childIndex = 0;

    public FTCategoryViewHolder(View itemView, FTCategoryAdapter.CategoryOnActionsLister categoryOnActionsLister) {
        super(itemView);
        ButterKnife.bind(this, itemView);

        mCategoryOnActionsLister = categoryOnActionsLister;
    }

    public void setView(ExpandableGroup group, int childIndex, String recentCategoryTitle) {
        Context context = itemView.getContext();
        this.childIndex = childIndex;
        currentCollection = (FTShelfItemCollection) group.getItems().get(childIndex);
        String title = currentCollection.getDisplayTitle(context);
        if (group.getTitle().equals(CATEGORIES)) {
            if (currentCollection.isTrash(context)) {
                title = context.getString(R.string.trash_title);
                mImageView.setImageResource(R.drawable.category_delete);
            } else {
                mImageView.setImageResource(R.drawable.category_folder_dark);
            }
            groupType = FTCategoryGroupType.COLLECTION;
            int padding = (int) (Resources.getSystem().getDisplayMetrics().density * 12);
            mImageView.setPadding(padding, 0, padding, 0);
        } else {
            int padding = (int) (Resources.getSystem().getDisplayMetrics().density * 12);
            mImageView.setPadding(padding, padding, padding, padding);
            if (group.getTitle().equals(RECENT)) {
                groupType = FTCategoryGroupType.RECENT;
            } else {
                groupType = FTCategoryGroupType.PINNED;
            }
            mImageView.setImageBitmap(BitmapUtil.getBitmap(Uri.withAppendedPath(Uri.parse(currentCollection.getFileURL().getPath()), FTConstants.COVER_SHELF_IMAGE_NAME)));
        }
        mTitleTextView.setText(title);
        if (group.getTitle().equals(CATEGORIES)) {
            if (title.equals(recentCategoryTitle)) {
                mParentLayout.setBackgroundResource(R.drawable.highlight);
            } else {
                mParentLayout.setBackgroundResource(R.drawable.cell);
            }
        } else {
            mParentLayout.setBackgroundResource(R.drawable.cell);
        }
    }

    @OnClick(R.id.item_nd_item_layout)
    void onItemSelected() {
        if (groupType == FTCategoryGroupType.COLLECTION) {
            mCategoryOnActionsLister.onCollectionSelected(currentCollection);
        } else {
            mCategoryOnActionsLister.onBookSelected(currentCollection.getFileURL());
        }
    }

    @OnLongClick(R.id.item_nd_item_layout)
    boolean onItemLongClicked(View view) {
        if (groupType == FTCategoryGroupType.COLLECTION) {
            if (((FTCategoryAdapter) getBindingAdapter()).isCollectionEditable) {
                if (currentCollection.isTrash(view.getContext())) {
                    return false;
                } else {
                    showCollectionOptions(view.getContext(), view);
                }
            }
        } else if (groupType == FTCategoryGroupType.RECENT) {
            showRecentCollectionOptions(view.getContext(), view);
        } else if (groupType == FTCategoryGroupType.PINNED) {
            showPinnedCollectionOptions(view.getContext(), view);
        }
        return true;
    }

    private void showCollectionOptions(Context context, View view) {
        PopupWindow window = FTPopupFactory.create(view.getContext(), view, R.layout.category_options_dialog, R.dimen._320dp, 0);

        window.getContentView().findViewById(R.id.category_options_remove_text_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                window.dismiss();
                currentCollection.shelfItems(context, FTShelfSortOrder.BY_NAME, null, "", new FTShelfItemCollection.ShelfNotebookItemsAndErrorBlock() {
                    @Override
                    public void didFinishWithNotebookItems(List<FTShelfItem> notebooks, Error error) {
                        if (notebooks.isEmpty()) {
                            mCategoryOnActionsLister.removeItem(childIndex, currentCollection);
                        } else {
                            FTDialogFactory.showAlertDialog(context.getString(R.string.delete_category),
                                    context.getString(R.string.delete_category_confirmation), new FTDialogFactory.OnAlertDialogShownListener() {
                                        @Override
                                        public void onPositiveClick(DialogInterface dialog, int which) {
                                            mCategoryOnActionsLister.removeItem(childIndex, currentCollection);
                                        }

                                        @Override
                                        public void onNegativeClick(DialogInterface dialog, int which) {
                                            //This method not used
                                        }
                                    });
                        }
                    }
                });
            }
        });

        window.getContentView().findViewById(R.id.category_options_rename_text_view).setOnClickListener(v -> {
            window.dismiss();
            String currentName = currentCollection.getDisplayTitle(context);
            FTRenameDialog.newInstance(FTRenameDialog.RenameType.RENAME_CATEGORY, currentName, getBindingAdapterPosition(),
                    new FTRenameDialog.RenameListener() {
                        @Override
                        public void renameShelfItem(String updatedName, int position, DialogFragment dialogFragment) {
                            if (!currentName.equals(updatedName))
                                mCategoryOnActionsLister.renameItem(updatedName, childIndex, currentCollection);
                        }

                        @Override
                        public void dialogActionCancel() {
                        }
                    }).show(((AppCompatActivity) context).getSupportFragmentManager(), "FTRenameDialog");
        });
    }

    private void showRecentCollectionOptions(Context context, View view) {
        PopupWindow window = FTPopupFactory.create(view.getContext(), view, R.layout.category_recent_options, R.dimen._320dp, 0);

        window.getContentView().findViewById(R.id.category_recent_options_pin_text_view).setOnClickListener(v -> {
            window.dismiss();
            mCategoryOnActionsLister.pinNotebook(currentCollection.getFileURL());
        });

        window.getContentView().findViewById(R.id.category_recent_options_remove_text_view).setOnClickListener(v -> {
            window.dismiss();
            mCategoryOnActionsLister.removeFromRecents(currentCollection.getFileURL());
        });
    }

    private void showPinnedCollectionOptions(Context context, View view) {
        PopupWindow window = FTPopupFactory.create(view.getContext(), view, R.layout.category_pin_options, R.dimen._320dp, 0);

        window.getContentView().findViewById(R.id.category_pin_options_unpin_text_view).setOnClickListener(v -> {
            window.dismiss();
            mCategoryOnActionsLister.unpinNotebook(currentCollection.getFileURL());
        });
    }

    enum FTCategoryGroupType {
        COLLECTION, RECENT, PINNED
    }
}
