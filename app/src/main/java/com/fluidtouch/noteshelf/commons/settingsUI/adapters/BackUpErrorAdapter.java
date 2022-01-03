package com.fluidtouch.noteshelf.commons.settingsUI.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fluidtouch.noteshelf.backup.database.FTBackupItem;
import com.fluidtouch.noteshelf.commons.ui.BaseRecyclerAdapter;
import com.fluidtouch.noteshelf2.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Sreenu on 12/02/19
 */
public class BackUpErrorAdapter extends BaseRecyclerAdapter<FTBackupItem, BackUpErrorAdapter.BackUpErrorViewHolder> {
    @NonNull
    @Override
    public BackUpErrorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new BackUpErrorViewHolder(getView(parent, R.layout.item_back_up_error_recyclerview));
    }

    @Override
    public void onBindViewHolder(@NonNull BackUpErrorViewHolder holder, int position) {
        holder.mTitleTextView.setText(getItem(position).getError());
    }

    class BackUpErrorViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.back_up_recycler_view_title_text_view)
        TextView mTitleTextView;

        BackUpErrorViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @OnClick(R.id.back_up_recycler_view_title_text_view)
        protected void showErrorInDetail() {
            FTBackupItem item = getItem(getAdapterPosition());
            Context context = itemView.getContext();
            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
            alertBuilder.setTitle(R.string.backup_error_details);
            alertBuilder.setMessage(item.getError());
            alertBuilder.setPositiveButton(context.getString(R.string.ok), (dialog, which) -> dialog.dismiss());
            AlertDialog alert = alertBuilder.create();
            alert.show();
        }
    }
}
