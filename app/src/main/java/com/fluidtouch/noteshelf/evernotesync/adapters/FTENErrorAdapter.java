package com.fluidtouch.noteshelf.evernotesync.adapters;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fluidtouch.noteshelf.commons.ui.BaseRecyclerAdapter;
import com.fluidtouch.noteshelf2.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Vineet on 2/05/19
 */

public class FTENErrorAdapter extends BaseRecyclerAdapter<String, FTENErrorAdapter.EnErrorViewHolder> {
    @NonNull
    @Override
    public EnErrorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new EnErrorViewHolder(getView(parent, R.layout.item_back_up_error_recyclerview));
    }

    @Override
    public void onBindViewHolder(@NonNull EnErrorViewHolder holder, int position) {
        String error = getItem(position);
        if (error != null && !error.isEmpty()) {
            holder.mTitleTextView.setText(error);
        }
    }

    class EnErrorViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.back_up_recycler_view_title_text_view)
        TextView mTitleTextView;

        EnErrorViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @OnClick(R.id.back_up_recycler_view_title_text_view)
        protected void showErrorInDetail() {
            //ToDo: Show error in detail
        }
    }
}
