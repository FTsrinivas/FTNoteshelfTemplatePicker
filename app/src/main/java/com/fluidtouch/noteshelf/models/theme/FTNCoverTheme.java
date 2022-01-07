package com.fluidtouch.noteshelf.models.theme;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf.commons.utils.AssetsUtil;
import com.fluidtouch.noteshelf.commons.utils.BitmapUtil;
import com.fluidtouch.noteshelf.documentframework.FTUrl;
import com.fluidtouch.noteshelf.documentframework.Utilities.FTConstants;
import com.fluidtouch.noteshelf.preferences.SystemPref;
import com.fluidtouch.noteshelf.templatepicker.adapters.FTTemplateDetailedInfoAdapter;
import com.fluidtouch.noteshelf.templatepicker.common.plistdatamodel.FTLineTypes;
import com.fluidtouch.noteshelf.templatepicker.common.plistdatamodel.FTTemplateColors;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;

public class FTNCoverTheme extends FTNTheme {

    @Override
    public FTUrl themeTemplateURL() {
        if (this.thumbnailURL == null) {
            this.thumbnailURL = this.themeThumbnailURL();
        }

        return this.thumbnailURL;
    }

    @Override
    public Bitmap themeThumbnail(Context context) {
        /*if (bitmap != null) {
            return bitmap;
        }
        AssetManager assetmanager = context.getAssets();
        InputStream is = null;
        try {
            Log.d("TemplatePicker==>","FTNCoverTheme themeThumbnail coverName isDownloadTheme::-"+
                    isDownloadTheme +" isCustomTheme::-"+isCustomTheme+" coverPath::-"+FTConstants.COVER_FOLDER_NAME + "/" + this.packName + "/thumbnail@2x.png");
            if (isDownloadTheme || isCustomTheme) {
                File file = new File((isDownloadTheme ? FTConstants.DOWNLOADED_COVERS_PATH : FTConstants.CUSTOM_COVERS_PATH) + this.packName + "/thumbnail@2x.png");
                if (!file.exists()) {
                    file = new File((isDownloadTheme ? FTConstants.DOWNLOADED_COVERS_PATH : FTConstants.CUSTOM_COVERS_PATH) + this.packName + "/thumbnail.png");
                }
                is = new FileInputStream(file);
            } else {
                is = assetmanager.open(FTConstants.COVER_FOLDER_NAME + "/" + this.packName + "/thumbnail@2x.png");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.d("TemplatePicker==>","FTNCoverTheme themeThumbnail coverName FileInputStream::-"+is);
        if (is == null)
            return bitmap;
        return BitmapFactory.decodeStream(is);*/

        AssetManager assetmanager = context.getAssets();
        InputStream is = null;
        try {

            if (themeFileURL.getPath().toLowerCase().contains("stockcover")) {
                is = assetmanager.open(FTConstants.COVER_FOLDER_NAME + "/" + this.packName + "/thumbnail@2x.png");
            } else {
                if (isDownloadTheme || isCustomTheme) {
                    File file = new File((isDownloadTheme ? FTConstants.DOWNLOADED_COVERS_PATH : FTConstants.CUSTOM_COVERS_PATH) + this.packName + "/thumbnail@2x.png");
                    if (!file.exists()) {
                        file = new File((isDownloadTheme ? FTConstants.DOWNLOADED_COVERS_PATH : FTConstants.CUSTOM_COVERS_PATH) + this.packName + "/thumbnail.png");
                    }
                    is = new FileInputStream(file);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return BitmapFactory.decodeStream(is);
       /* WeakReference<Bitmap> tempbitmap = new WeakReference<Bitmap>(temp);
        return BitmapUtil.getResizedBitmap(temp,116,146,false);*/
    }

    public Bitmap themeOverlay(Context context) {
        AssetManager assetmanager = context.getAssets();
        InputStream is = null;
        try {
            is = assetmanager.open(FTConstants.COVER_FOLDER_NAME + "/" + this.packName + "/overlay@2x.png");
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (is == null)
            return null;
        return BitmapFactory.decodeStream(is);
    }

    @Override
    public Bitmap themeThumbnailOnCallBack(Context mContext, FTNTheme ftnTheme, FTLineTypes lineInfo, FTTemplateColors colorInfo, boolean isLandscape, FTTemplateDetailedInfoAdapter callBack, FTTemplateDetailedInfoAdapter.ThemeViewHolder childViewHolder) {
        AssetManager assetmanager = mContext.getAssets();
        InputStream is = null;
        try {

            if (themeFileURL.getPath().toLowerCase().contains("stockcover")) {
                is = assetmanager.open(FTConstants.COVER_FOLDER_NAME + "/" + this.packName + "/thumbnail@2x.png");
            } else {
                if (isDownloadTheme || isCustomTheme) {
                    File file = new File((isDownloadTheme ? FTConstants.DOWNLOADED_COVERS_PATH : FTConstants.CUSTOM_COVERS_PATH) + this.packName + "/thumbnail@2x.png");
                    if (!file.exists()) {
                        file = new File((isDownloadTheme ? FTConstants.DOWNLOADED_COVERS_PATH : FTConstants.CUSTOM_COVERS_PATH) + this.packName + "/thumbnail.png");
                    }
                    is = new FileInputStream(file);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        Bitmap scaledBitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeStream(is),
                (int) callBack.dipToPixels(mContext, 116),
                (int) callBack.dipToPixels(mContext, 143),
                true);
        bitmap = addWhiteBorder(scaledBitmap, 2);
        return bitmap;
    }
}
