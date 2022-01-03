package com.fluidtouch.noteshelf.store.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf.commons.ui.BaseRecyclerAdapter;
import com.fluidtouch.noteshelf.commons.utils.ObservingService;
import com.fluidtouch.noteshelf.store.holder.FTStoreItemViewHolder;
import com.fluidtouch.noteshelf.store.model.FTStorePack;
import com.fluidtouch.noteshelf.store.model.FTStorePackItem;
import com.fluidtouch.noteshelf.store.ui.FTStoreCallbacks;
import com.fluidtouch.noteshelf2.R;
import com.noteshelf.auth.AppAuthentication;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.util.Observable;
import java.util.Observer;

public class FTStoreAdapter extends BaseRecyclerAdapter<FTStorePack, FTStoreItemViewHolder> {
    ObservingService mDownloadStatusObserver;
    private Context mContext;
    private FTStoreCallbacks listener;

    public FTStoreAdapter(Context context, FTStoreCallbacks listener, ObservingService downloadStatusObserver) {
        mContext = context;
        this.listener = listener;
        mDownloadStatusObserver = downloadStatusObserver;
    }

    @NonNull
    @Override
    public FTStoreItemViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, int viewType) {
        if (viewType == 0)
            return new FTStoreItemViewHolder(getView(parent, R.layout.item_store_header), viewType);
        else
            return new FTStoreItemViewHolder(getView(parent, R.layout.list_item_storepack), viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull FTStoreItemViewHolder holder, final int position) {

        final FTStorePack ftStorePack = (FTStorePack) getItem(position);
        if (holder.getItemViewType() == 0) {
            if (ftStorePack.getTitle() == null || ftStorePack.getTitle().isEmpty()) {
                String nonClicableTitle = mContext.getResources().getString(R.string.store_sign_up_text);
                String title = nonClicableTitle + mContext.getResources().getString(R.string.store_sign_up_text2);
                holder.txtSignUp.setVisibility((FTApp.isForSamsungStore() || !AppAuthentication.Companion.isLoginEnabled(mContext) || FTApp.isForHuawei()) ? View.GONE : View.VISIBLE);
                holder.imgProfile.setVisibility(View.GONE);
                SpannableString string = new SpannableString(title);
                ClickableSpan clickableSpan = new ClickableSpan() {
                    @Override
                    public void onClick(View widget) {
                        listener.onSignClick();
                    }

                    @Override
                    public void updateDrawState(final TextPaint textPaint) {
                        textPaint.setColor(Color.parseColor("#0298dc"));
                        textPaint.setUnderlineText(false);
                    }
                };
                string.setSpan(clickableSpan, nonClicableTitle.length(), title.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                holder.txtSignUp.setText(string);
                holder.txtSignUp.setMovementMethod(LinkMovementMethod.getInstance());
            } else if (ftStorePack.isValid()) {
                holder.txtSignUp.setVisibility(View.INVISIBLE);
                holder.imgProfile.setVisibility(FTApp.isForSamsungStore() || FTApp.isForHuawei() ? View.GONE : View.VISIBLE);
                Picasso.get().load(ftStorePack.getProfilePic()).placeholder(R.mipmap.emptyprofile).error(R.mipmap.emptyprofile).transform(new Transformation() {

                    @Override
                    public Bitmap transform(Bitmap source) {
                        int size = Math.min(source.getWidth(), source.getHeight());

                        int x = (source.getWidth() - size) / 2;
                        int y = (source.getHeight() - size) / 2;

                        Bitmap squaredBitmap = Bitmap.createBitmap(source, x, y, size, size);
                        Bitmap.Config config = source.getConfig();
                        if (squaredBitmap != source) {
                            source.recycle();
                        }

                        Bitmap bitmap = Bitmap.createBitmap(size, size, config == null ? Bitmap.Config.ARGB_8888 : config);

                        Canvas canvas = new Canvas(bitmap);
                        Paint paint = new Paint();
                        BitmapShader shader = new BitmapShader(squaredBitmap,
                                BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP);
                        paint.setShader(shader);
                        paint.setAntiAlias(true);

                        float r = size / 2f;
                        canvas.drawCircle(r, r, r, paint);

                        squaredBitmap.recycle();
                        return bitmap;
                    }

                    @Override
                    public String key() {
                        return "rounded_corners";
                    }
                }).into(holder.imgProfile);
                holder.imgProfile.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int[] location = new int[2];
                        v.getLocationInWindow(location);
                        listener.onProfileIconClick(location[1]);
                    }
                });
            } else {
                holder.txtSignUp.setVisibility(FTApp.isForSamsungStore() || FTApp.isForHuawei()? View.GONE : View.VISIBLE);
                holder.imgProfile.setVisibility(View.GONE);
                String nonClicableTitle = "You’re almost done! We’ve sent you an email to ";
                String email = ftStorePack.getTitle() == null ? "your Email" : ftStorePack.getTitle();
                String title = nonClicableTitle + email + ". Open it up to activate your membership.";
                SpannableString string = new SpannableString(title);
                ClickableSpan clickableSpan = new ClickableSpan() {
                    @Override
                    public void onClick(View widget) {
                        AlertDialog.Builder alBuilder = new AlertDialog.Builder(mContext);
                        alBuilder.setTitle(mContext.getResources().getString(R.string.change_login));
                        alBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                listener.onSignClick();
                                dialog.dismiss();
                            }
                        });
                        alBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        alBuilder.show();
                    }

                    @Override
                    public void updateDrawState(final TextPaint textPaint) {
                        textPaint.setColor(Color.parseColor("#0298dc"));
                        textPaint.setUnderlineText(false);
                    }
                };
                string.setSpan(clickableSpan, nonClicableTitle.length(), nonClicableTitle.length() + email.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                holder.txtSignUp.setText(string);
                holder.txtSignUp.setMovementMethod(LinkMovementMethod.getInstance());

            }
            mDownloadStatusObserver.addObserver("storeLogin", new Observer() {
                @Override
                public void update(Observable o, Object arg) {
                    if (arg instanceof FTStorePack) {
                        FTStorePack userDetails = (FTStorePack) arg;
                        ftStorePack.setTitle(userDetails.getTitle());
                        ftStorePack.setProfilePic(userDetails.getProfilePic());
                        ftStorePack.setValid(userDetails.isValid());
                    } else {
                        ftStorePack.setTitle("");
                        ftStorePack.setProfilePic("");
                    }
                    notifyDataSetChanged();
                    mDownloadStatusObserver.removeObserver("storeLogin", this);
                }
            });
        } else {
            int layoutId;
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if (ftStorePack.getSectiontype() == 0) {
                holder.txtTitle.setVisibility(View.GONE);
                layoutId = R.layout.item_storepack_banner;
            } else if (ftStorePack.getSectiontype() == 1) {
                layoutId = R.layout.item_storepack_medium;
                holder.txtTitle.setVisibility(View.VISIBLE);
                holder.txtTitle.setText(ftStorePack.getTitle());
            } else {
                layoutId = R.layout.item_storepack_small;
                holder.txtTitle.setVisibility(View.VISIBLE);
                holder.txtTitle.setText(ftStorePack.getTitle());
            }
            int i = 0;
            int size = ftStorePack.getPacks().size();
            holder.layContainer.removeAllViews();
            while (i < size) {
                RecyclerView recyclerView = new RecyclerView(mContext);
                recyclerView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
                FTStoreItemAdapter ftStoreItemAdapter = new FTStoreItemAdapter(mContext, layoutId, ftStorePack.getSectiontype(), listener, mDownloadStatusObserver);
                for (int j = 0; j < ftStorePack.getNumOfItems(); j++) {
                    FTStorePackItem ftStorePackItem = ftStorePack.getPacks().get(i);
                    ftStoreItemAdapter.add(ftStorePackItem);
                    i++;
                    if (i >= size)
                        break;
                }
                recyclerView.setAdapter(ftStoreItemAdapter);
                holder.layContainer.addView(recyclerView);
            }
        }

    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0)
            return 0;
        else
            return 1;
    }
}