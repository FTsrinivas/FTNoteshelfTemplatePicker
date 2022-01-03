package com.fluidtouch.noteshelf.store.adapter;

import android.content.Context;
import android.util.Log;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.fluidtouch.noteshelf.commons.ui.BaseRecyclerAdapter;
import com.fluidtouch.noteshelf.store.holder.FTImagePreviewViewHolder;
import com.fluidtouch.noteshelf.store.model.FTStorePackItem;
import com.fluidtouch.noteshelf2.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

public class FTStorePreviewAdapter extends BaseRecyclerAdapter<String, FTImagePreviewViewHolder> {
    private Context mContext;
    private FTStorePackItem ftStorePackItem;
    private int loadedItems = 0;

    public FTStorePreviewAdapter(Context context, FTStorePackItem ftStorePackItem) {
        mContext = context;
        this.ftStorePackItem = ftStorePackItem;
    }

    @NonNull
    @Override
    public FTImagePreviewViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, int viewType) {
        if (viewType > 0)
            return new FTImagePreviewViewHolder(getView(parent, R.layout.item_store_image), viewType);
        else
            return new FTImagePreviewViewHolder(getView(parent, R.layout.item_store_dialog_title), viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull FTImagePreviewViewHolder holder, final int position) {

        if (position == 0) {
            holder.txtDes.setText(ftStorePackItem.getDialogDescription());
            Picasso.get().load(ftStorePackItem.getSmallImage())
                    .into(holder.imageDes, new Callback() {
                        @Override
                        public void onSuccess() {
                            loadedItems++;
                        }

                        @Override
                        public void onError(Exception e) {

                        }
                    });
        } else {
            Picasso.get().load(ftStorePackItem.getPreviewImage(position)).error(R.drawable.covershadow)
                    .into(holder.imagePreview, new Callback() {
                        @Override
                        public void onSuccess() {
                            loadedItems++;
                        }

                        @Override
                        public void onError(Exception e) {

                        }

                    });
        }
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }
}