package com.fluidtouch.noteshelf.evernotesync.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.fluidtouch.noteshelf.commons.FTLog;
import com.fluidtouch.noteshelf.commons.ui.FTBaseDialog;
import com.fluidtouch.noteshelf.documentframework.FTDocument.FTDocumentFactory;
import com.fluidtouch.noteshelf.documentframework.FTNoteshelfDocument.FTNoteshelfDocument;
import com.fluidtouch.noteshelf.documentproviders.FTShelfCollectionProvider;
import com.fluidtouch.noteshelf.documentproviders.FTShelfItemCollection;
import com.fluidtouch.noteshelf.evernotesync.FTENSyncRecordUtil;
import com.fluidtouch.noteshelf.models.disk.diskItem.FTDiskItem;
import com.fluidtouch.noteshelf.models.disk.diskItem.shelfItem.FTGroupItem;
import com.fluidtouch.noteshelf.models.disk.diskItem.shelfItem.FTShelfItem;
import com.fluidtouch.noteshelf.shelf.adapters.FTCategoryMoveToAdapter;
import com.fluidtouch.noteshelf.shelf.adapters.FTShelfMoveToAdapter;
import com.fluidtouch.noteshelf.shelf.enums.FTShelfSortOrder;
import com.fluidtouch.noteshelf2.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Vineet on 2/05/2019
 */

public class FTENShelfItemDialog extends FTBaseDialog implements FTCategoryMoveToAdapter.FTCategoryMoveToAdapterCallback, FTShelfMoveToAdapter.FTShelfMoveToAdapterCallback {
    @BindView(R.id.shelf_evernote_shelf_item_recycler_view)
    RecyclerView shelfItemRecyclerView;
    @BindView(R.id.dialog_evernote_notebooks_title)
    TextView dialogTitle;

    private FTDiskItem diskItem;

    public static FTENShelfItemDialog newInstance(FTDiskItem itemType) {
        FTENShelfItemDialog shelfItemFragment = new FTENShelfItemDialog();
        shelfItemFragment.diskItem = itemType;
        return shelfItemFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_evernote_publish_notebooks, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        ButterKnife.bind(this, view);
        if (this.diskItem == null) {
            FTShelfCollectionProvider.getInstance().shelfs(shelfs -> {
                if (!shelfs.isEmpty()) {
                    shelfs.remove(shelfs.size() - 1);
                }
                FTCategoryMoveToAdapter ftCategoryMoveToAdapter = new FTCategoryMoveToAdapter(FTENShelfItemDialog.this, null, true);
                ftCategoryMoveToAdapter.addAll(shelfs);
                shelfItemRecyclerView.setAdapter(ftCategoryMoveToAdapter);
            });
        } else if (this.diskItem instanceof FTShelfItemCollection) {
            FTShelfItemCollection shelfItemCollection = (FTShelfItemCollection) diskItem;

            dialogTitle.setText(shelfItemCollection.getDisplayTitle(getContext()));

            shelfItemCollection.shelfItems(getContext(), FTShelfSortOrder.BY_NAME, null, "", (notebooks, error) -> {
                if (notebooks != null && error == null) {
                    FTShelfMoveToAdapter ftShelfMoveToAdapter = new FTShelfMoveToAdapter(FTENShelfItemDialog.this, null, true);
                    ftShelfMoveToAdapter.addAll(notebooks);
                    shelfItemRecyclerView.setAdapter(ftShelfMoveToAdapter);
                }
            });
        } else if (this.diskItem instanceof FTGroupItem) {
            FTGroupItem groupItem = (FTGroupItem) diskItem;

            dialogTitle.setText(groupItem.getDisplayTitle(getContext()));

            groupItem.getShelfCollection().shelfItems(getContext(), FTShelfSortOrder.BY_NAME, groupItem, "", (notebooks, error) -> {
                if (notebooks != null && error == null) {
                    FTShelfMoveToAdapter ftShelfMoveToAdapter = new FTShelfMoveToAdapter(FTENShelfItemDialog.this, null, true);
                    ftShelfMoveToAdapter.addAll(notebooks);
                    shelfItemRecyclerView.setAdapter(ftShelfMoveToAdapter);
                }
            });
        }
    }

    @Override
    public void showInCategoryPanel(FTShelfItemCollection ftShelfItemCollection) {
        FTENShelfItemDialog.newInstance(ftShelfItemCollection).show(getChildFragmentManager());
    }

    @Override
    public void showInGroupPanel(FTGroupItem ftGroupItem) {
        FTENShelfItemDialog.newInstance(ftGroupItem).show(getChildFragmentManager());
    }

    @Override
    public void onNotebookClicked(FTShelfItem document) {
        FTNoteshelfDocument notebook = FTDocumentFactory.documentForItemAtURL(document.getFileURL());
        if (notebook != null) {
            notebook.openDocument(getContext(), (success, error) -> {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (success) {
                            if (FTENSyncRecordUtil.isSyncEnabledForNotebook(notebook.getDocumentUUID())) {
                                FTLog.crashlyticsLog("UI: Evernote Sync disabled for notebook");
                                FTENSyncRecordUtil.disableSyncForNotebook(notebook.getDocumentUUID());
                                Log.d(FTLog.NS_EVERNOTE, "Evernote UI: sync disabled for " + notebook.getDisplayTitle(getContext()));
                            } else {
                                FTLog.crashlyticsLog("UI: Evernote sync enabled for notebook");
                                FTENSyncRecordUtil.enableEvernoteSyncForNotebook(getContext(), notebook);
                                Log.d(FTLog.NS_EVERNOTE, "Evernote sync enabled for " + notebook.getDisplayTitle(getContext()));
                            }
                        }
                    });
                }
            });
        }
    }

    @OnClick(R.id.dialog_back_button)
    void onBackClicked() {
        dismiss();
    }

    @OnClick(R.id.dialog_done_button)
    void onDoneClicked() {
        dismissAll();
    }
}