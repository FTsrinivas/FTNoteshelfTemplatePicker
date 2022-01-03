package com.fluidtouch.noteshelf.shelf.adapters;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.RecyclerView;

import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf.commons.FTLog;
import com.fluidtouch.noteshelf.commons.ui.BaseRecyclerAdapter;
import com.fluidtouch.noteshelf.commons.utils.BitmapUtil;
import com.fluidtouch.noteshelf.commons.utils.FTPopupFactory;
import com.fluidtouch.noteshelf.documentframework.FTUrl;
import com.fluidtouch.noteshelf.documentframework.Utilities.FTConstants;
import com.fluidtouch.noteshelf.documentproviders.FTShelfCollectionProvider;
import com.fluidtouch.noteshelf.documentproviders.FTShelfItemCollection;
import com.fluidtouch.noteshelf.models.disk.diskItem.shelfItem.FTGroupItem;
import com.fluidtouch.noteshelf.models.disk.diskItem.shelfItem.FTShelfItem;
import com.fluidtouch.noteshelf.models.theme.FTNThemeCategory;
import com.fluidtouch.noteshelf.preferences.SystemPref;
import com.fluidtouch.noteshelf.shelf.enums.RKShelfItemType;
import com.fluidtouch.noteshelf.shelf.fragments.FTRenameDialog;
import com.fluidtouch.noteshelf.shelf.listeners.ShelfOnEditModeChangedListener;
import com.fluidtouch.noteshelf.shelf.viewholders.BaseShelfViewHolder;
import com.fluidtouch.noteshelf.shelf.viewholders.ShelfDocumentViewHolder;
import com.fluidtouch.noteshelf.shelf.viewholders.ShelfGroupViewHolder;
import com.fluidtouch.noteshelf.store.ui.FTChooseCoverPaperDialog;
import com.fluidtouch.noteshelf.templatepicker.FTChoosePaperTemplate;
import com.fluidtouch.noteshelf.templatepicker.FTTemplateMode;
import com.fluidtouch.noteshelf.templatepicker.models.FTTemplatepickerInputInfo;
import com.fluidtouch.noteshelf2.R;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class FTBaseShelfAdapter extends BaseRecyclerAdapter<FTShelfItem, RecyclerView.ViewHolder> implements FTRenameDialog.RenameListener {
    static final int ALPHA_DEFAULT_POSITION = -1;
    public static final float SHADOW_ALPHA = 0.5f;
    public static final float FINAL_ALPHA = 1.0f;
    private static final String NOTE_IMAGE_NAME = FTConstants.COVER_SHELF_IMAGE_NAME;
    public Map<String, String> mSelectedMap = new HashMap<>();
    public boolean isShowingDate = FTApp.getPref().get(SystemPref.IS_SHOWING_DATE, FTConstants.DEFAULT_IS_SHOWING_DATE);
    public boolean mIsInEditMode = false;
    int mDraggingPosition = -1;
    public int mAlphaPosition = ALPHA_DEFAULT_POSITION;
    //region class variables
    private Context mContext;
    private ShelfOnEditModeChangedListener mShelfOnEditModeChangedListener;
    private ShelfAdapterToActivityListener mShelfAdapterToActivityListener;
    //This will be set when long tap notebook options are displayed
    private int optionsPosition = -1;
    //endregion
    private DocumentOnActionsListener mDocumentOnActionsListener = new DocumentOnActionsListener() {
        boolean isLongTapDetected = false;

        @Override
        public void showRenameDialog(int adapterPosition) {
            mShelfAdapterToActivityListener.showRenameDialog(FTRenameDialog.RenameType.RENAME_NOTEBOOK, getItem(adapterPosition).getDisplayTitle(getContext()), adapterPosition, FTBaseShelfAdapter.this);
            showKeyboard();
        }

        @Override
        public void parentOnClick(int adapterPosition) {
            if (mIsInEditMode) {
                if (mSelectedMap.containsKey(getItem(adapterPosition).getUuid())) {
                    mSelectedMap.remove(getItem(adapterPosition).getUuid());
                } else {
                    FTShelfItem item = getItem(adapterPosition);
                    mSelectedMap.put(item.getUuid(), item.getUuid());
                }
                notifyItemChanged(adapterPosition);
                updateSelectedShelfItemsCount();
            } else {
                //Opening the document
                openDocument(adapterPosition);
            }
        }

        private void openDocument(int adapterPosition) {
            if (!mShelfAdapterToActivityListener.isOpeningDocument() && adapterPosition >= 0) {
                Log.i("OPEN_DOC_BASE", "true");
                final FTShelfItem bookItem = getItem(adapterPosition);
                int from = 0;
                if (!bookItem.shelfCollection.isTrash(getContext())) {
                    FTLog.crashlyticsLog("UI: Opened notebook");
                    if (!mShelfAdapterToActivityListener.isInsideGroup()) {
                        FTApp.getPref().save("lastDocumentPos", adapterPosition);
                    } else {
                        FTApp.getPref().save("lastGroupDocumentPos", adapterPosition);
                        from = 1;
                    }
                }
                mShelfAdapterToActivityListener.openSelectedDocument(bookItem.getFileURL(), from);
            }
        }

        @Override
        public void parentOnLongClick(int adapterPosition, View view, ImageView documentImageView) {
            isLongTapDetected = true;
            showNotebookOptions(view, adapterPosition, getItem(adapterPosition).shelfCollection.isTrash(view.getContext()));
        }

        @Override
        public boolean isLongTapDetected() {
            return isLongTapDetected;
        }

        @Override
        public void startDragging(View view, int position) {
            if (!mShelfAdapterToActivityListener.isInsideGroup()) {
                startDraggingItem(view, position);
            }
        }

        @Override
        public void dismissNotebookOptions() {
            if (window != null) {
                window.dismiss();
            }
        }

        private PopupWindow window;

        private void showNotebookOptions(View view, int adapterPosition, boolean isTrash) {
            optionsPosition = adapterPosition;

            window = new PopupWindow(getContext());
            View popUpView = View.inflate(getContext(), R.layout.popup_shelf_notebook_options, null);
            window.setWidth(LinearLayout.LayoutParams.WRAP_CONTENT);
            window.setHeight(LinearLayout.LayoutParams.WRAP_CONTENT);
            window.setContentView(popUpView);
            window.setFocusable(true);
            window.setBackgroundDrawable(null);

            int[] location = new int[2];
            view.getLocationOnScreen(location);
            int x = location[0];
            int y = location[1];
            int screenHeight = getContext().getResources().getDisplayMetrics().heightPixels;
            View layout = popUpView.findViewById(R.id.shelf_notebook_options_layout);
            if (screenHeight - (y + view.getHeight()) < layout.getLayoutParams().height) {
                window.showAtLocation(view, Gravity.TOP | Gravity.START, x, (y - layout.getLayoutParams().height));
            } else {
                window.showAtLocation(view, Gravity.TOP | Gravity.START, x, y + view.getHeight());
            }

            if (isTrash) {
                window.getContentView().findViewById(R.id.hide_for_trash_layout).setVisibility(View.GONE);
                window.getContentView().findViewById(R.id.hide_for_trash_layout2).setVisibility(View.GONE);
                window.getContentView().findViewById(R.id.hide_for_trash_layout3).setVisibility(View.GONE);
            }

            boolean isPinned = mShelfAdapterToActivityListener.isPinned(getItem(adapterPosition).getFileURL());
            window.getContentView().findViewById(R.id.popup_shelf_notebook_pin_selected_image_view).setVisibility(isPinned ? View.VISIBLE : View.GONE);

            window.setOnDismissListener(() -> isLongTapDetected = false);

            window.getContentView().findViewById(R.id.popup_shelf_notebook_open_text_view).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    window.dismiss();
                    openDocument(adapterPosition);
                }
            });

            window.getContentView().findViewById(R.id.popup_shelf_notebook_pin_text_view).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    window.dismiss();
                    if (isPinned) {
                        FTShelfCollectionProvider.getInstance().pinnedShelfProvider.removePinned(getItem(adapterPosition).getFileURL().getPath());
                    } else {
                        FTShelfCollectionProvider.getInstance().recentShelfProvider.removeRecent(getItem(adapterPosition).getFileURL().getPath());
                        FTShelfCollectionProvider.getInstance().pinnedShelfProvider.pinNotbook((getItem(adapterPosition).getFileURL().getPath()));
                    }
                }
            });

            window.getContentView().findViewById(R.id.popup_shelf_notebook_change_cover_text_view).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    window.dismiss();
                    //FTChooseCoverPaperDialog.newInstance(FTNThemeCategory.FTThemeType.COVER).show(((AppCompatActivity) getContext()).getSupportFragmentManager(), FTChooseCoverPaperDialog.class.getName());
                    //FTChoosePaperTemplate.newInstance(FTNThemeCategory.FTThemeType.COVER).show(((AppCompatActivity) getContext()).getSupportFragmentManager(), FTChooseCoverPaperDialog.class.getName());

                    FTTemplatepickerInputInfo _ftTemplatepickerInputInfo = new FTTemplatepickerInputInfo();
                    _ftTemplatepickerInputInfo.set_baseShelfActivity(null);
                    _ftTemplatepickerInputInfo.set_ftTemplateOpenMode(null);
                    _ftTemplatepickerInputInfo.set_ftThemeType(FTNThemeCategory.FTThemeType.COVER);
                    _ftTemplatepickerInputInfo.set_notebookTitle(null);
                    FTChoosePaperTemplate.newInstance1(_ftTemplatepickerInputInfo).show(((AppCompatActivity) getContext()).getSupportFragmentManager(), FTChooseCoverPaperDialog.class.getName());

                }
            });

            window.getContentView().findViewById(R.id.popup_shelf_notebook_rename_text_view).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    window.dismiss();
                    showRenameDialog(adapterPosition);
                }
            });

            window.getContentView().findViewById(R.id.popup_shelf_notebook_duplicate_text_view).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    window.dismiss();
                    mShelfAdapterToActivityListener.duplicateInEditMode();
                }
            });

            window.getContentView().findViewById(R.id.popup_shelf_notebook_move_text_view).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    window.dismiss();
                    mShelfAdapterToActivityListener.moveInEditMode();
                }
            });

            window.getContentView().findViewById(R.id.popup_shelf_notebook_share_text_view).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    window.dismiss();
                    mShelfAdapterToActivityListener.shareInEditMode(view);
                }
            });

            window.getContentView().findViewById(R.id.popup_shelf_notebook_move_to_trash_text_view).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    window.dismiss();
                    mShelfAdapterToActivityListener.moveToTrash();
                }
            });
        }

        @Override
        public void setUpDragActions(DragEvent event, View view, int position, BaseShelfViewHolder holder) {
            FTBaseShelfAdapter.this.setUpDragActions(event, view, position, holder);
        }
    };

    private GroupOnActionsListener mGroupOnActionsListener = new GroupOnActionsListener() {
        @Override
        public void showRenameDialog(int adapterPosition) {
            FTLog.crashlyticsLog("UI: Clicked rename notebook");
            mShelfAdapterToActivityListener.showRenameDialog(FTRenameDialog.RenameType.RENAME_GROUP, getItem(adapterPosition).getDisplayTitle(getContext()), adapterPosition, FTBaseShelfAdapter.this);
        }

        @Override
        public void setUpDragActions(DragEvent event, View view, int position, BaseShelfViewHolder holder) {
            FTBaseShelfAdapter.this.setUpDragActions(event, view, position, holder);
        }

        @Override
        public void showGroupItems(int adapterPosition) {
            if (mIsInEditMode) {
                mShelfOnEditModeChangedListener.onEditModeChanged(false, 0);
            }
            FTApp.getPref().saveLastDocumentPosition(adapterPosition);
            FTApp.getPref().saveGroupDocumentUrl(getItem(adapterPosition).getFileURL().relativePathWRTCollection());
            mShelfAdapterToActivityListener.openGroup((FTGroupItem) getItem(adapterPosition));
            ((Activity) getContext()).overridePendingTransition(R.anim.group_reveal, R.anim.terminate);
            doneWithChanges();
        }

        @Override
        public void parentOnLongClick(int adapterPosition, View view, ImageView mImageView) {
            showGroupOptions(view, adapterPosition);
        }

        private void showGroupOptions(View view, int adapterPosition) {
            optionsPosition = adapterPosition;
            PopupWindow window = FTPopupFactory.create(view.getContext(), view, R.layout.popup_shelf_group_options, 0, 0);

            window.getContentView().findViewById(R.id.popup_shelf_notebook_open_text_view).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    window.dismiss();
                    showGroupItems(adapterPosition);
                }
            });

            window.getContentView().findViewById(R.id.popup_shelf_notebook_rename_text_view).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    window.dismiss();
                    showRenameDialog(adapterPosition);
                }
            });
        }
    };

    FTBaseShelfAdapter(Context context, ShelfOnEditModeChangedListener shelfOnEditModeChangedListener, boolean isShowingGroup, ShelfAdapterToActivityListener shelfAdapterToActivityListener) {
        this.mShelfOnEditModeChangedListener = shelfOnEditModeChangedListener;
        this.mShelfAdapterToActivityListener = shelfAdapterToActivityListener;
        this.mContext = context;
    }

    public static void showKeyboard() {
        InputMethodManager imm = (InputMethodManager) FTApp.getInstance().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
    }

    protected abstract void startDraggingItem(View view, int position);

    protected abstract void setUpDragActions(DragEvent event, View view, int position, BaseShelfViewHolder holder);

    @Override
    public int getItemViewType(int position) {
        if (getItem(position).getType() == RKShelfItemType.GROUP) {
            return RKShelfItemType.GROUP.ordinal();
        } else {
            return RKShelfItemType.DOCUMENT.ordinal();
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        if (viewType == RKShelfItemType.GROUP.ordinal()) {
            return new ShelfGroupViewHolder(getView(viewGroup, R.layout.item_shelf_group_recycler_view), mGroupOnActionsListener);
        } else {
            return new ShelfDocumentViewHolder(getView(viewGroup, R.layout.item_shelf_document_recycler_view), mDocumentOnActionsListener);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == RKShelfItemType.GROUP.ordinal()) {
            ((ShelfGroupViewHolder) holder).setView(position);
        } else {
            ((ShelfDocumentViewHolder) holder).setView(position);
        }
    }

    public void setUpDateView(TextView dateView) {
        dateView.setVisibility(isShowingDate && !mIsInEditMode ? View.VISIBLE : View.GONE);
    }

    public void setUpRenameView(TextView renameView) {
//        renameView.setVisibility(mIsInEditMode ? View.VISIBLE : View.GONE);
    }

    public void setUpImageView(ImageView imageView, ImageView selectionImageView, FTShelfItem item) {
        Context context = imageView.getContext();
        selectionImageView.setVisibility(mIsInEditMode ? View.VISIBLE : View.GONE);
        if (mSelectedMap.containsKey(item.getUuid())) {
            imageView.setBackgroundResource(R.drawable.thumbnail_select_bg);
            selectionImageView.setImageResource(R.drawable.circle_selected_new);
        } else {
            imageView.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
            selectionImageView.setImageResource(R.drawable.circle_unselected);
        }
        imageView.setImageBitmap(BitmapUtil.getRoundedCornerBitmap(context, BitmapUtil.getBitmap(Uri.withAppendedPath(Uri.parse(item.getFileURL().getPath()), NOTE_IMAGE_NAME)), getContext().getResources().getInteger(R.integer.three)));
    }

    @Override
    public void renameShelfItem(String updatedName, final int position, DialogFragment dialogFragment) {
        mShelfAdapterToActivityListener.renameShelfItem(getItem(position), updatedName, (movedBook, error) -> {
            new Handler(Looper.getMainLooper()).post(() -> {
                if (mShelfAdapterToActivityListener.isItemExistsInSearch(movedBook)) {
                    update(position, movedBook);
                } else {
                    mSelectedMap.remove(getItem(position).getUuid());
                    remove(position);
                    updateSelectedShelfItemsCount();
                }

                updateAll(getAll());
            });
        });
    }

    @Override
    public void dialogActionCancel() {
    }

    void refreshAdapter(List<FTShelfItem> list) {
        updateAll(getConfiguredList(getContext(), list));
    }

    private ArrayList<FTShelfItem> getConfiguredList(final Context context, List<FTShelfItem> list) {
        ArrayList<FTShelfItem> items = new ArrayList<>(list);
        if (FTApp.getPref().isSortingWithDate()) {
            items.sort((first, second) -> second.getFileModificationDate().compareTo(first.getFileModificationDate()));
        } else {
            items.sort(new AlphaNumericComparator(context));
        }
        return items;
    }

    public void deleteInEditMode(List<FTShelfItem> selectedItems) {
        clearSelected(selectedItems);
    }

    public void duplicateInEditMode(List<FTShelfItem> selectedItems) {
        getAll().addAll(0, selectedItems);
        refreshAdapter(getAll());
    }

    public void moveInEditMode(List<FTShelfItem> selectedItems) {
        if (mShelfAdapterToActivityListener.isInsideGroup()) {
            clearSelected(selectedItems);
        } else {
            mShelfAdapterToActivityListener.getShelfItems((notebooks, error) -> {
                mSelectedMap.clear();
                updateSelectedShelfItemsCount();
                updateAll(notebooks);
            });
        }
    }

    private void clearSelected(List<FTShelfItem> selectedItems) {
        mSelectedMap.clear();
        updateSelectedShelfItemsCount();
        remove(selectedItems);
    }

    public void coverStyleInEditMode(List<Integer> selectedPositions, List<FTShelfItem> selectedItems) {
        for (int i = 0; i < selectedPositions.size(); i++) {
            update(selectedPositions.get(i), selectedItems.get(i));
        }
    }

    void updateSelectedShelfItemsCount() {
        mShelfOnEditModeChangedListener.onSelectedItemsCountChanged(mIsInEditMode, mSelectedMap.size());
    }

    public void doneWithChanges() {
        mIsInEditMode = false;
        mSelectedMap.clear();
        notifyDataSetChanged();
    }

    @Override
    public void updateAll(List<FTShelfItem> items) {
        try {
            super.updateAll(getConfiguredList(getContext(), items));
        } catch (Exception e) {
            e.printStackTrace();
            FTLog.crashlyticsLog("FTBaseShelfAdapter " + items.size());
            FTLog.logCrashException(e);
        }
    }

    public void addAll(List<FTShelfItem> shelfItems) {
        super.addAll(getConfiguredList(getContext(), shelfItems));
    }

    private void updateEditModeChange(int position, ImageView documentImageView) {
        FTShelfItem item = getItem(position);
        mShelfOnEditModeChangedListener.onEditModeChanged(mIsInEditMode, 1);
        mSelectedMap.put(item.getUuid(), item.getUuid());
        documentImageView.setBackgroundResource(R.drawable.thumbnail_select_bg);
        notifyDataSetChanged();
    }

    private Context getContext() {
        return mContext;
    }

    public void addOptionsItem(List<FTShelfItem> selectedItems) {
        if (optionsPosition != -1) {
            selectedItems.add(getItem(optionsPosition));
        }
    }

    public void selectAll() {
        List<FTShelfItem> items = getAll();
        mSelectedMap.clear();
        for (int i = 0; i < items.size(); i++) {
            if (!(items.get(i) instanceof FTGroupItem)) {
                mSelectedMap.put(items.get(i).getUuid(), items.get(i).getUuid());
            }
        }
        mShelfOnEditModeChangedListener.onSelectedItemsCountChanged(mIsInEditMode, mSelectedMap.size());
        notifyDataSetChanged();
    }

    public void selectNone() {
        mSelectedMap.clear();
        mShelfOnEditModeChangedListener.onSelectedItemsCountChanged(mIsInEditMode, mSelectedMap.size());
        notifyDataSetChanged();
    }

    private static class AlphaNumericComparator implements Comparator<FTShelfItem> {

        private final Collator collator;
        private Context context;

        public AlphaNumericComparator(Context context) {
            this.context = context;
            this.collator = Collator.getInstance();
        }

        @Override
        public int compare(FTShelfItem notebook1, FTShelfItem notebook2) {
            String s1 = notebook1.getDisplayTitle(context);
            String s2 = notebook2.getDisplayTitle(context);
            if ((s1 == null || s1.trim().isEmpty()) && (s2 != null && !s2.trim().isEmpty())) {
                return -1;
            }
            if ((s2 == null || s2.trim().isEmpty()) && (s1 != null && !s1.trim().isEmpty())) {
                return 1;
            }
            if ((s1 == null || s1.trim().isEmpty()) && (s2 == null || s2.trim().isEmpty())) {
                return 0;
            }

            s1 = s1.trim();
            s2 = s2.trim();
            int s1Index = 0;
            int s2Index = 0;
            while (s1Index < s1.length() && s2Index < s2.length()) {
                int result = 0;
                String s1Slice = this.slice(s1, s1Index);
                String s2Slice = this.slice(s2, s2Index);
                s1Index += s1Slice.length();
                s2Index += s2Slice.length();
                if (Character.isDigit(s1Slice.charAt(0)) && Character.isDigit(s2Slice.charAt(0))) {
                    result = this.compareDigits(s1Slice, s2Slice);
                } else {
                    result = this.compareCollatedStrings(s1Slice, s2Slice);
                }
                if (result != 0) {
                    return result;
                }
            }
            return Integer.signum(s1.length() - s2.length());
        }

        private String slice(String s, int index) {
            StringBuilder result = new StringBuilder();
            if (Character.isDigit(s.charAt(index))) {
                while (index < s.length() && Character.isDigit(s.charAt(index))) {
                    result.append(s.charAt(index));
                    index++;
                }
            } else {
                result.append(s.charAt(index));
            }
            return result.toString();
        }

        private int compareDigits(String s1, String s2) {
            try {
                return (int) (Long.parseLong(s1) - Long.parseLong(s2));
            } catch (Exception e) {
                return 1;
            }
        }

        private int compareCollatedStrings(String s1, String s2) {
            return collator.compare(s1, s2);
        }
    }

    public interface ShelfOnActionsListener {
        /**
         * Shows a dialog to input new name/title for the group.
         *
         * @param adapterPosition Position of the selected item in the given list.
         */
        void showRenameDialog(int adapterPosition);

        void setUpDragActions(DragEvent event, View view, int position, BaseShelfViewHolder holder);
    }

    public interface DocumentOnActionsListener extends ShelfOnActionsListener {

        void parentOnClick(int adapterPosition);

        void parentOnLongClick(int adapterPosition, View view, ImageView documentImageView);

        boolean isLongTapDetected();

        void startDragging(View view, int position);

        void dismissNotebookOptions();
    }

    public interface GroupOnActionsListener extends ShelfOnActionsListener {

        void showGroupItems(int adapterPosition);

        void parentOnLongClick(int adapterPosition, View view, ImageView mImageView);
    }

    public interface ShelfAdapterToActivityListener {
        void openSelectedDocument(FTUrl fileUrl, final int from);

        void renameShelfItem(FTShelfItem shelfItem, String updatedName, FTShelfItemCollection.ShelfNotebookAndErrorBlock shelfNotebookAndErrorBlock);

        void getShelfItems(FTShelfItemCollection.ShelfNotebookItemsAndErrorBlock shelfNotebookItemsAndErrorBlock);

        void showRenameDialog(FTRenameDialog.RenameType type, String name, final int position, FTRenameDialog.RenameListener listener);

        boolean isItemExistsInSearch(FTShelfItem shelfItem);

        void duplicateInEditMode();

        void moveInEditMode();

        void shareInEditMode(View v);

        void moveToTrash();

        boolean isPinned(FTUrl fileURL);

        boolean isOpeningDocument();

        FTShelfItemCollection getCurrentShelfItemCollection();

        void openGroup(FTGroupItem groupItem);

        FTGroupItem getCurrentGroupItem();

        boolean isInsideGroup();
    }
}
