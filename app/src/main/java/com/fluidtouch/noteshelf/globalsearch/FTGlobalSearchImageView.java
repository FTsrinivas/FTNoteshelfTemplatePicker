package com.fluidtouch.noteshelf.globalsearch;

import android.content.Context;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatImageView;

import com.fluidtouch.noteshelf.documentframework.FTNoteshelfDocument.FTNoteshelfPage;

public class FTGlobalSearchImageView extends AppCompatImageView {
    public boolean isContent = false;

    private FTNoteshelfPage page;
    private Context mContext;

    public FTGlobalSearchImageView(Context context) {
        super(context);
        mContext = context;
    }

    public FTGlobalSearchImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    public FTGlobalSearchImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (isContent) {
            setImageBitmap(null);
            setBackground(null);
            if (page != null) {
                if (mContext instanceof FTGlobalSearchActivity)
                    page.setIsinUse(false);
                page.thumbnail().removeThumbnail();
            }
        }
    }

    public void setPage(FTNoteshelfPage page) {
        this.page = page;
    }
}