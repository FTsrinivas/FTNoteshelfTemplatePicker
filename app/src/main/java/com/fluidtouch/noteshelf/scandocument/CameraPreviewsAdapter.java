package com.fluidtouch.noteshelf.scandocument;

import android.graphics.BitmapFactory;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fluidtouch.noteshelf.commons.ui.BaseRecyclerAdapter;
import com.fluidtouch.noteshelf2.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Sreenu on 21/05/19
 */
public class CameraPreviewsAdapter extends BaseRecyclerAdapter<ScannedImageModel, CameraPreviewsAdapter.CameraPreviewViewHolder> {
    private CameraPreviewsContainerCallback mContainerCallback;

    public CameraPreviewsAdapter(CameraPreviewsContainerCallback containerCallback) {
        mContainerCallback = containerCallback;
    }

    @NonNull
    @Override
    public CameraPreviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new CameraPreviewViewHolder(getView(parent, R.layout.item_camera_preview_recycler_view));
    }

    @Override
    public void onBindViewHolder(@NonNull CameraPreviewViewHolder holder, int position) {
        holder.mImageView.setImageBitmap(BitmapFactory.decodeFile(getItem(position).croppedImagePath));
    }

    public interface CameraPreviewsContainerCallback {
        void cropImage(ScannedImageModel imageModel);
    }

    class CameraPreviewViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.item_camera_preview_image_view)
        protected ImageView mImageView;

        public CameraPreviewViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @OnClick(R.id.item_camera_preview_image_view)
        protected void openCropView() {
            mContainerCallback.cropImage(getItem(getAdapterPosition()));
        }
    }
}
