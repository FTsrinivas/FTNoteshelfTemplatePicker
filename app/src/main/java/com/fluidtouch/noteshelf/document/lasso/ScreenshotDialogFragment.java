package com.fluidtouch.noteshelf.document.lasso;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf.commons.FTLog;
import com.fluidtouch.noteshelf.commons.ui.FTBaseDialog;
import com.fluidtouch.noteshelf.commons.ui.FTSmartDialog;
import com.fluidtouch.noteshelf.commons.utils.FTFileManagerUtil;
import com.fluidtouch.noteshelf.commons.utils.FileUriUtils;
import com.fluidtouch.noteshelf.documentframework.Utilities.FTConstants;
import com.fluidtouch.noteshelf.preferences.SystemPref;
import com.fluidtouch.noteshelf2.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class ScreenshotDialogFragment extends FTBaseDialog implements View.OnClickListener {

    private Bitmap bitmap;
    private ImageView screenShotPrev, shareViewIcon, closePopupBtn;
    private File imageFilePath;
    private ScreenShotDialogListener mScreenShotDialogListener;

    public static ScreenshotDialogFragment newInstance(Bitmap mBitmap) {
        ScreenshotDialogFragment mFTUserDetailsDialog = new ScreenshotDialogFragment();
        mFTUserDetailsDialog.bitmap = mBitmap;
        return mFTUserDetailsDialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = new Dialog(getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(false);
        Window dialogWindow = dialog.getWindow();
        dialogWindow.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        dialogWindow.setGravity(Gravity.CENTER);
        dialogWindow.setAttributes(lp);
        dialogWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.screenshotprev, container, false);
        screenShotPrev = v.findViewById(R.id.screenshot);
        closePopupBtn = v.findViewById(R.id.close_popup);
        shareViewIcon = v.findViewById(R.id.shareViewID);
        shareViewIcon.setOnClickListener(this);
        closePopupBtn.setOnClickListener(this);
        previewScreenShot(bitmap);
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
        String fileName = "FT_" + timeStamp;
        generateImageFolder(getActivity(), bitmap, fileName);
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    private void previewScreenShot(Bitmap bitmap) {
        screenShotPrev.setImageBitmap(bitmap);
    }

    /*
    private  Bitmap getCropBitmap(Bitmap source, RectF cropRectF) {
        return BitmapUtil.cropBitmap(bitmap, cropRectF);
    }*/

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.shareViewID:
                shareOptionsMenu();
                break;

            case R.id.close_popup:
                closeShareOptionsMenu();
                break;

        }
    }

    private void shareOptionsMenu() {
        //mScreenShotDialogListener.shareScreenShot(bitmap);
        FTLog.crashlyticsLog("UI: Clicked share notebook as .png");
        FTApp.getPref().save(SystemPref.EXPORT_FORMAT, FTConstants.PNG_EXTENSION);
        final FTSmartDialog smartDialog = new FTSmartDialog()
                .setMessage(getActivity().getString(R.string.generating)).show(getActivity().getSupportFragmentManager());

        smartDialog.dismissAllowingStateLoss();

        if (imageFilePath != null) {
            mScreenShotDialogListener.shareScreenShot(FileUriUtils.getUriForFile(getActivity(), imageFilePath));
        } else {
            Toast.makeText(getActivity(), R.string.export_failed, Toast.LENGTH_SHORT).show();
        }
        dismiss();
    }

    private File generateImageFolder(Context context, Bitmap bitmapInfo, String imgFileName) {
        File imageCacheDir = new File(context.getCacheDir() + "/screenshotsExport");
        if (imageCacheDir.exists())
            FTFileManagerUtil.deleteFilesInsideFolder(imageCacheDir);
        File imageDir = new File(context.getCacheDir() + "/screenshotsExport/" + imgFileName);
        if (!imageDir.exists())
            imageDir.mkdirs();
        createPngFileForBitmap(bitmapInfo, imgFileName, imageDir.getPath());
        return imageDir;
    }

    private void createPngFileForBitmap(Bitmap pageBitmap, String imageFileName, String
            parentDirPath) {
        imageFilePath = new File(parentDirPath, imageFileName + FTConstants.PNG_EXTENSION);
        if (pageBitmap != null) {
            try {
                FileOutputStream out = new FileOutputStream(imageFilePath);
                pageBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                out.flush();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void closeShareOptionsMenu() {
        dismiss();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        super.onAttach(context);
        try {
            mScreenShotDialogListener = (ScreenShotDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + e.getMessage());
        }
    }

    public interface ScreenShotDialogListener {
        void shareScreenShot(Uri exportFileUri);
    }

}
