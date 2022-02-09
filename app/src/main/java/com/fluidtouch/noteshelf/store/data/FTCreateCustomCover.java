package com.fluidtouch.noteshelf.store.data;

import static com.fluidtouch.noteshelf.documentframework.Utilities.FTConstants.KEY_IS_NOT_SAVED_FUTURE;
import static com.fluidtouch.noteshelf.documentframework.Utilities.FTConstants.KEY_OBJECT_THEME;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;

import androidx.fragment.app.FragmentManager;

import com.fluidtouch.noteshelf.commons.ui.FTSmartDialog;
import com.fluidtouch.noteshelf.commons.utils.BitmapUtil;
import com.fluidtouch.noteshelf.commons.utils.FileUriUtils;
import com.fluidtouch.noteshelf.commons.utils.ObservingService;
import com.fluidtouch.noteshelf.documentframework.Utilities.FTConstants;
import com.fluidtouch.noteshelf.models.theme.FTNCoverTheme;
import com.fluidtouch.noteshelf.models.theme.FTNTheme;
import com.fluidtouch.noteshelf.models.theme.FTNThemeCategory;
import com.fluidtouch.noteshelf.store.ui.FTAddCoverThemeDialog;
import com.fluidtouch.noteshelf2.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;


public class FTCreateCustomCover {

    private Context mContext;
    private Uri mUri;
    private FragmentManager mFragmentManager;
    private Bitmap bitmap = null;
    private String packName = "Sample";

    public FTCreateCustomCover(Context context, Uri uri, FragmentManager fragmentManager) {
        mContext = context;
        mUri = uri;
        mFragmentManager = fragmentManager;
    }

    public void create() {
        try {
            bitmap = MediaStore.Images.Media.getBitmap(mContext.getContentResolver(), mUri);
            int rotation = FileUriUtils.getCapturedImageOrientation(mContext, mUri);
            if (rotation > 0) {
                Matrix mat = new Matrix();
                mat.postRotate(rotation);
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), mat, true);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        FTAddCoverThemeDialog ftAddThemeDialog = FTAddCoverThemeDialog.newInstance(new FTAddCoverThemeDialog.DialogResult() {
            @Override
            public void onDataSubmit(String name, Bitmap bitmap2, boolean isSaved) {
                /*if (!isSaved) {
                    FTNTheme theme = new FTNCoverTheme();
                    theme.themeName = name;
                    theme.setCategoryName(mContext.getString(R.string.custom));
                    theme.packName = name + ".nsc";
                    theme.bitmap = bitmap2;
                    theme.ftThemeType = FTNThemeCategory.FTThemeType.COVER;
                    theme.isCustomTheme = true;
                    theme.isDeleted = true;
                    theme.isSavedForFuture = false;
                    theme.thumbnailURLPath = FTConstants.TEMP_FOLDER_PATH + "TemplatesCache/" + "bitmapMerged.jpg";
                    JSONObject themeObject = new JSONObject();
                    try {
                        themeObject.put(KEY_OBJECT_THEME, theme);
                        themeObject.put(KEY_IS_NOT_SAVED_FUTURE, true);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    ObservingService.getInstance().postNotification("addCustomTheme", theme);
                    return;
                }*/
                packName  = name;
                bitmap = bitmap2;
                FTSmartDialog smartDialog = new FTSmartDialog()
                        .setMode(FTSmartDialog.FTSmartDialogMode.SPINNER)
                        .setMessage(mContext.getString(R.string.creating))
                        .show(mFragmentManager);
                AsyncTask task = new AsyncTask() {
                    @Override
                    protected Boolean doInBackground(Object[] objects) {
                        try {
                            if (!isSaved) {
                                return BitmapUtil.saveBitmap(bitmap, FTConstants.TEMP_FOLDER_PATH+"customcover/" + "Sample" + ".nsc", "thumbnail@2x.png");

                            } else {
                                return BitmapUtil.saveBitmap(bitmap, FTConstants.CUSTOM_COVERS_PATH + name + ".nsc", "thumbnail@2x.png");

                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            return false;
                        }
                    }

                    @Override
                    protected void onPostExecute(Object o) {
                        super.onPostExecute(o);
                        if (smartDialog != null)
                            smartDialog.dismissAllowingStateLoss();
                        if ((boolean) o) {
                            if (!isSaved) {
                                packName = "Sample";
                            }
                            FTNTheme theme = new FTNCoverTheme();
                            theme.themeName = packName;
                            theme.setCategoryName(mContext.getString(R.string.custom));
                            theme.packName = packName + ".nsc";
                            theme.ftThemeType = FTNThemeCategory.FTThemeType.COVER;
                            theme.isCustomTheme = true;

                            if (!isSaved) {
                                theme.isSavedForFuture = false;
                                theme.bitmap = bitmap;
                                theme.thumbnailURLPath = FTConstants.TEMP_FOLDER_PATH + "TemplatesCache/" + "bitmapMerged.jpg";
                            } else {
                                theme.isSavedForFuture = true;
                            }

                            ObservingService.getInstance().postNotification("addCustomTheme", theme);
                        }
                    }
                };
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        }, bitmap);
        if (bitmap != null)
            ftAddThemeDialog.show(mFragmentManager, "ftAddThemeDialog");
    }
}