package com.fluidtouch.noteshelf.backup.database;

import android.text.TextUtils;
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

public class FTBackupLocationAdapter extends BaseRecyclerAdapter<String, FTBackupLocationAdapter.ViewHolder> {

    private final Listener mListener;
    private String previousPath = "";

    public FTBackupLocationAdapter(Listener listener) {
        mListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(getView(parent, R.layout.item_category_recycler_view));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.categoryNameTextView.setText(getItem(position));
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.category_name)
        TextView categoryNameTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @OnClick(R.id.item_category_layout)
        void onItemClicked() {
            mListener.onLocationSelected(getItem(getBindingAdapterPosition()));
        }
    }

    public interface Listener {
        void onLocationSelected(String path);
    }
}