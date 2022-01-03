package com.fluidtouch.noteshelf.shelf.adapters;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fluidtouch.noteshelf.commons.FTLog;
import com.fluidtouch.noteshelf.commons.ui.BaseRecyclerAdapter;
import com.fluidtouch.noteshelf.commons.utils.BitmapUtil;
import com.fluidtouch.noteshelf.commons.utils.DateUtil;
import com.fluidtouch.noteshelf.documentframework.FTDocument.FTDocumentFactory;
import com.fluidtouch.noteshelf.documentframework.FTNoteshelfDocument.FTNoteshelfDocument;
import com.fluidtouch.noteshelf.documentframework.Utilities.FTConstants;
import com.fluidtouch.noteshelf.evernotesync.FTENSyncRecordUtil;
import com.fluidtouch.noteshelf.evernotesync.models.FTENNotebook;
import com.fluidtouch.noteshelf.models.disk.diskItem.shelfItem.FTGroupItem;
import com.fluidtouch.noteshelf.models.disk.diskItem.shelfItem.FTShelfItem;
import com.fluidtouch.noteshelf.shelf.enums.RKShelfItemType;
import com.fluidtouch.noteshelf2.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FTShelfMoveToAdapter extends BaseRecyclerAdapter<FTShelfItem, RecyclerView.ViewHolder> {
    private final String NOTE_IMAGE_NAME = FTConstants.COVER_SHELF_IMAGE_NAME;
    private FTShelfMoveToAdapterCallback listener;
    private FTShelfItem currentGroup;
    private boolean isEvernoteView;
    private List<FTENNotebook> enNotebooks = FTENSyncRecordUtil.getAllEnNotebooks();

    public FTShelfMoveToAdapter(FTShelfMoveToAdapterCallback listener, FTShelfItem currentGroup, boolean isEvernoteView) {
        this.listener = listener;
        this.currentGroup = currentGroup;
        this.isEvernoteView = isEvernoteView;
    }

    @Override
    public int getItemViewType(int position) {
        return getItem(position).getType().ordinal();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = super.getView(viewGroup, R.layout.item_move_to_shelf_recycler_view);
        return new ShelfMoveToViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        ShelfMoveToViewHolder holder = (ShelfMoveToViewHolder) viewHolder;
        FTShelfItem ftShelfItem = getItem(position);
        if (ftShelfItem.getType() == RKShelfItemType.GROUP) {
            configureGroup(holder, ftShelfItem, position);
        } else if (ftShelfItem.getType() == RKShelfItemType.DOCUMENT) {
            configureDocument(holder, ftShelfItem, position);
        }
    }

    private void configureGroup(ShelfMoveToViewHolder holder, FTShelfItem ftShelfItem, int position) {
        Context context = holder.itemView.getContext();
        final FTGroupItem ftGroupItem = (FTGroupItem) ftShelfItem;
        holder.secondImageView.setVisibility(View.VISIBLE);
        holder.thirdImageView.setVisibility(View.VISIBLE);
        int notebooksCount = ftGroupItem.getChildren().size();
        if (notebooksCount >= 1)
            holder.firstImageView.setImageBitmap(BitmapUtil.getBitmap(Uri.withAppendedPath(Uri.parse(ftGroupItem.getChildren().get(0).getFileURL().getPath()), NOTE_IMAGE_NAME)));
        if (notebooksCount >= 2) {
            holder.secondImageView.setImageBitmap(BitmapUtil.getBitmap(Uri.withAppendedPath(Uri.parse(ftGroupItem.getChildren().get(1).getFileURL().getPath()), NOTE_IMAGE_NAME)));
            if (notebooksCount >= 3)
                holder.thirdImageView.setImageBitmap(BitmapUtil.getBitmap(Uri.withAppendedPath(Uri.parse(ftGroupItem.getChildren().get(2).getFileURL().getPath()), NOTE_IMAGE_NAME)));
            else
                holder.thirdImageView.setImageResource(R.drawable.covergray);
            holder.descriptionTextView.setText(new StringBuilder().append(notebooksCount).append(" Notebooks"));
        } else {
            holder.descriptionTextView.setText(new StringBuilder().append(notebooksCount).append(" Notebook"));
            holder.secondImageView.setImageResource(R.drawable.covergray);
        }

        if (isSameGroup((FTGroupItem) getItem(position)) && !isEvernoteView) {
            holder.titleTextView.setText(new StringBuilder().append("▸ ").append(ftGroupItem.getDisplayTitle(context)));
        } else {
            if (isEvernoteView) {
                holder.descriptionTextView.setTextColor(Color.BLACK);
            }
            holder.titleTextView.setText(ftGroupItem.getDisplayTitle(context));
        }
        holder.chevronImageView.setVisibility(View.VISIBLE);
    }

    private void configureDocument(ShelfMoveToViewHolder holder, FTShelfItem ftShelfItem, int position) {
        Context context = holder.itemView.getContext();
        holder.firstImageView.setImageBitmap(BitmapUtil.getBitmap(Uri.withAppendedPath(Uri.parse(ftShelfItem.getFileURL().getPath()), NOTE_IMAGE_NAME)));
        holder.secondImageView.setVisibility(View.GONE);
        holder.thirdImageView.setVisibility(View.GONE);

        holder.titleTextView.setText(ftShelfItem.getDisplayTitle(holder.itemView.getContext()));
        holder.descriptionTextView.setVisibility(View.VISIBLE);
        holder.descriptionTextView.setText(DateUtil.getDateFormat(context.getString(R.string.format_dd_mmm_yyyy_hh_mm_a))
                .format(ftShelfItem.getFileModificationDate()));

        //Evernote sync
        if (isEvernoteView && enNotebooks != null && !enNotebooks.isEmpty()) {
            holder.descriptionTextView.setVisibility(View.GONE);
            FTNoteshelfDocument document = FTDocumentFactory.documentForItemAtURL(ftShelfItem.getFileURL());
            document.openDocumentWhileInBackground();
            FTENNotebook enNotebook = FTENSyncRecordUtil.getEnNotebook(document.getDocumentUUID());
            if (enNotebook != null) {
                if (enNotebook.getSyncEnabled()) {
                    holder.enSyncCheck.setVisibility(View.VISIBLE);
                } else {
                    holder.enSyncCheck.setVisibility(View.GONE);
                }
                if (enNotebook.getErrorDescription() != null && !enNotebook.getErrorDescription().isEmpty()) {
                    holder.enErrorTextView.setVisibility(View.VISIBLE);
                    holder.enErrorTextView.setText(context.getString(R.string.sync_failed_with_reason, enNotebook.getErrorDescription()));
                }
            } else {
                holder.enSyncCheck.setVisibility(View.GONE);
            }
        } else {
            holder.enSyncCheck.setVisibility(View.GONE);
        }

        if (isSameNotebook(getItem(position)) && !isEvernoteView) {
            holder.titleTextView.setText(new StringBuilder().append("▸ ").append(ftShelfItem.getDisplayTitle(context)));
        } else {
            if (isEvernoteView) {
                holder.descriptionTextView.setTextColor(Color.BLACK);
            }
            holder.titleTextView.setText(ftShelfItem.getDisplayTitle(context));
        }
        holder.chevronImageView.setVisibility(View.GONE);
    }

    private boolean isSameGroup(FTGroupItem group) {
        return (currentGroup != null && group.getFileURL().equals(currentGroup.getFileURL()));
    }

    private boolean isSameNotebook(FTShelfItem notebook) {
        return (currentGroup != null && notebook.getFileURL().equals(currentGroup.getFileURL()));
    }

    public interface FTShelfMoveToAdapterCallback {
        void showInGroupPanel(FTGroupItem ftGroupItem);

        void onNotebookClicked(FTShelfItem document);
    }

    class ShelfMoveToViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.item_move_to_shelf_first_image_view)
        ImageView firstImageView;
        @BindView(R.id.item_move_to_shelf_second_image_view)
        ImageView secondImageView;
        @BindView(R.id.item_move_to_shelf_third_image_view)
        ImageView thirdImageView;
        @BindView(R.id.item_move_to_shelf_title_text_view)
        TextView titleTextView;
        @BindView(R.id.item_move_to_shelf_description_text_view)
        TextView descriptionTextView;
        @BindView(R.id.en_sync_check)
        ImageView enSyncCheck;
        @BindView(R.id.item_move_to_shelf_evernote_error)
        TextView enErrorTextView;
        @BindView(R.id.item_shelf_layout)
        ViewGroup parentLayout;
        @BindView(R.id.item_move_to_shelf_chevron)
        ImageView chevronImageView;

        ShelfMoveToViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            if (isEvernoteView) parentLayout.setBackgroundResource(R.color.ns_dialog_bg);
        }

        @OnClick(R.id.item_shelf_layout)
        void onItemClicked() {
            FTLog.crashlyticsLog("UI: Selected a group and navigated into notebooks panel");
            FTShelfItem shelfItem = getItem(getAdapterPosition());
            if (shelfItem.getType() == RKShelfItemType.GROUP) {
                listener.showInGroupPanel((FTGroupItem) shelfItem);
            } else if (shelfItem.getType() == RKShelfItemType.DOCUMENT) {
                if (isEvernoteView) {
                    if (enSyncCheck.getVisibility() == View.GONE) {
                        enSyncCheck.setVisibility(View.VISIBLE);
                    } else {
                        enSyncCheck.setVisibility(View.GONE);
                        enErrorTextView.setVisibility(View.GONE);
                    }
                }
                listener.onNotebookClicked(shelfItem);
            }
        }
    }
}