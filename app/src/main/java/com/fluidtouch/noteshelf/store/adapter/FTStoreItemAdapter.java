package com.fluidtouch.noteshelf.store.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.fluidtouch.noteshelf.commons.ui.BaseRecyclerAdapter;
import com.fluidtouch.noteshelf.commons.utils.ObservingService;
import com.fluidtouch.noteshelf.store.data.FTDownloadedStorePackData;
import com.fluidtouch.noteshelf.store.holder.FTStoreInternalItemViewHolder;
import com.fluidtouch.noteshelf.store.model.FTDownloadData;
import com.fluidtouch.noteshelf.store.model.FTStorePackItem;
import com.fluidtouch.noteshelf.store.ui.FTStoreCallbacks;
import com.fluidtouch.noteshelf2.R;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.util.Observable;
import java.util.Observer;

public class FTStoreItemAdapter extends BaseRecyclerAdapter<FTStorePackItem, FTStoreInternalItemViewHolder> {
    FTStoreInternalItemViewHolder itemViewHolder = null;
    FTStoreCallbacks callback;
    ObservingService mDownloadStatusObserver;
    private Context mContext;
    private int resourceId = 0;
    private int sectiontype = 0;
    private float cornerRadious = 5f;

    public FTStoreItemAdapter(Context context, int resourceId, int sectiontype, FTStoreCallbacks callback, ObservingService downloadStatusObserver) {
        mContext = context;
        this.resourceId = resourceId;
        this.sectiontype = sectiontype;
        this.callback = callback;
        mDownloadStatusObserver = downloadStatusObserver;
    }

    @NonNull
    @Override
    public FTStoreInternalItemViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, int viewType) {
        return new FTStoreInternalItemViewHolder(getView(parent, resourceId));
    }

    @Override
    public void onBindViewHolder(@NonNull final FTStoreInternalItemViewHolder holder, final int position) {

        final FTStorePackItem ftStorePackItem = (FTStorePackItem) getItem(position);
        holder.txtTitle.setText(ftStorePackItem.getName());
        holder.txtSubTitle.setText(ftStorePackItem.getSubtitle());
        String url;
        if (sectiontype == 0) {
            url = ftStorePackItem.getBannerImage();
            Log.d("TemplatePicker==>","FTStorePackItem onBindViewHolder url::-:: "+url);
            cornerRadious = 4f;
            holder.txtSubTitle.setVisibility(View.GONE);
        } else if (sectiontype == 1) {
            url = ftStorePackItem.getMediumImage();
            cornerRadious = 4f;
        } else {
            url = ftStorePackItem.getSmallImage();
            holder.txtSubTitle.setVisibility(View.VISIBLE);
            cornerRadious = 14f;
        }
        if (!url.contains("null")) {
            Picasso.get().load(url).placeholder(R.drawable.covershadow).error(R.drawable.covershadow).transform(new Transformation() {

                @Override
                public Bitmap transform(Bitmap bitmap) {
                    float pixels = cornerRadious * mContext.getResources().getDisplayMetrics().density;
                    if (bitmap == null) {
                        return null;
                    }
                    Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
                    final Paint paint = new Paint();
                    final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
                    final RectF rectF = new RectF(rect);

                    Canvas canvas = new Canvas(output);
                    canvas.drawRoundRect(rectF, pixels, pixels, paint);
                    paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
                    canvas.drawBitmap(bitmap, rect, rect, paint);
                    if (output != bitmap) {
                        bitmap.recycle();
                    }
                    return output;
                }

                @Override
                public String key() {
                    return "rounded_corners";
                }
            }).into(holder.imageView);
        }


        FTDownloadData ftDownLoadData = FTDownloadedStorePackData.getInstance(mContext).getStorePackData().get(ftStorePackItem.getName());
        if (ftDownLoadData != null) {
            if (ftDownLoadData.version == ftStorePackItem.getVersion()) {
                holder.txtGet.setText(R.string.tickmark);
                ftStorePackItem.setDownloaded(true);
                //holder.txtGet.setEnabled(false);
                holder.txtGet.setBackgroundResource(R.drawable.store_disable_get_bg);
            } else if (ftDownLoadData.version != ftStorePackItem.getVersion()) {
                holder.txtGet.setText(R.string.update);
                holder.txtGet.setAllCaps(true);
                holder.txtGet.setEnabled(true);
                holder.txtGet.setBackgroundResource(R.drawable.store_get_bg);
            }
        } else {
            holder.txtGet.setText(R.string.get);
            holder.txtGet.setAllCaps(true);
            holder.txtGet.setEnabled(true);
            holder.txtGet.setBackgroundResource(R.drawable.store_get_bg);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                itemViewHolder = holder;
                callback.onStoreItemSelected(ftStorePackItem);
            }
        });

        holder.txtGet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                callback.onDownloadButtonClick(ftStorePackItem);
            }
        });

        mDownloadStatusObserver.addObserver(ftStorePackItem.getName(), new Observer() {
            @Override
            public void update(Observable o, Object arg) {
                if (arg instanceof String)
                    holder.onDownloadStart();
                else if (arg instanceof Integer)
                    holder.onProgressUpdate((int) arg);
                else if (arg instanceof Boolean) {
                    if ((boolean) arg)
                        mDownloadStatusObserver.removeObserver(ftStorePackItem.getName(), this);
                    holder.onDownloadFinish((boolean) arg);
                }
            }
        });

    }


}