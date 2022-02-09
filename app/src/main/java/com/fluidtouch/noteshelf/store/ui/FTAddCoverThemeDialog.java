package com.fluidtouch.noteshelf.store.ui;

import android.app.Dialog;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.DialogFragment;

import com.fluidtouch.noteshelf.FTApp;
import com.fluidtouch.noteshelf.commons.utils.BitmapUtil;
import com.fluidtouch.noteshelf.commons.utils.DrawableUtil;
import com.fluidtouch.noteshelf.commons.utils.ScreenUtil;
import com.fluidtouch.noteshelf.documentframework.FTNoteshelfDocument.FTDocumentUtils;
import com.fluidtouch.noteshelf.documentframework.FTUrl;
import com.fluidtouch.noteshelf.documentframework.Utilities.FTConstants;
import com.fluidtouch.noteshelf.preferences.SystemPref;
import com.fluidtouch.noteshelf.store.view.BlurringView;
import com.fluidtouch.noteshelf.store.view.FTResizeImageView;
import com.fluidtouch.noteshelf.templatepicker.FTAppConfig;
import com.fluidtouch.noteshelf2.R;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.stream.Stream;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FTAddCoverThemeDialog extends DialogFragment implements View.OnClickListener {

    /* @BindView(R.id.layMain)
     RelativeLayout layMain;*/
    @BindView(R.id.edtThemeName)
    TextView txtEmail;
    @BindView(R.id.layImage)
    RelativeLayout layImage;
    @BindView(R.id.imaTheme)
    FTResizeImageView imaTheme;

    @BindView(R.id.imgMask2)
    ImageView imgMask2;

    /*@BindView(R.id.imgMask3)
    BlurringView imgMask3;*/

    /*@BindView(R.id.imaTheme)
    ImageView imaTheme;*/
    /*@BindView(R.id.imgMask)
    ImageView imgMask;
    @BindView(R.id.imgMask2)
    ImageView imgMask2;
    @BindView(R.id.imgMask3)
    BlurringView imgMask3;*/
    /*@BindView(R.id.color1)
    View color1;*/
    /*@BindView(R.id.layColors)
    View layColors;*/
    @BindView(R.id.check)
    CheckBox check;
    private DialogResult callback;
    private Bitmap bitmap;
    private ImageView selectView;
    private String[] colors = {"#fdc52e", "#0b3a55", "#24cb9e", "#2ad0ca", "#f16a33"};

    public static FTAddCoverThemeDialog newInstance(DialogResult listener, Bitmap bitmap) {
        FTAddCoverThemeDialog ftAddThemeDialog = new FTAddCoverThemeDialog();
        ftAddThemeDialog.callback = listener;
        ftAddThemeDialog.bitmap = bitmap;
        return ftAddThemeDialog;
    }

    @NotNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = new Dialog(getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        Window dialogWindow = dialog.getWindow();
        //dialogWindow.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        dialogWindow.setAttributes(lp);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        int screenSize = getResources().getConfiguration().screenLayout &
                Configuration.SCREENLAYOUT_SIZE_MASK;
        int orientation = getResources().getConfiguration().orientation;
        if ((screenSize == Configuration.SCREENLAYOUT_SIZE_NORMAL && orientation == Configuration.ORIENTATION_PORTRAIT) || screenSize == Configuration.SCREENLAYOUT_SIZE_SMALL) {
            Dialog dialog = getDialog();
            if (dialog != null) {
                if (screenSize == Configuration.SCREENLAYOUT_SIZE_SMALL)
                    dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                else
                    dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                dialog.getWindow().setGravity(Gravity.CENTER);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            }
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_customize_cover_new, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        if (bitmap != null) {
            imaTheme.setImageBitmap(bitmap);
          /*  Bitmap imageRounded = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), bitmap.getConfig());
            Canvas canvas = new Canvas(imageRounded);
            Paint mpaint = new Paint();
            mpaint.setAntiAlias(true);
            mpaint.setShader(new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));
            canvas.drawRoundRect((new RectF(0, 0, bitmap.getWidth(), bitmap.getHeight())), 100, 100, mpaint); // Round Image Corner 100 100 100 100
            imaTheme.setImageBitmap(imageRounded);*/
        } else {
            imaTheme.setVisibility(View.GONE);
        }
        selectView = new ImageView(getContext());
        ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(ScreenUtil.convertDpToPx(getContext(), getResources().getInteger(R.integer.forty_four)), ScreenUtil.convertDpToPx(getContext(), getResources().getInteger(R.integer.thirty)));
        selectView.setLayoutParams(params);
        selectView.setImageResource(R.drawable.customcolorselect);
        int padding = (int) getResources().getDimension(R.dimen._2dp);
        selectView.setPadding(padding, padding, padding, padding);

        check.setOnClickListener(this);
        check.setChecked(true);
        //layMain.addView(selectView);
        /*new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                color1.performClick();
            }
        }, 100);
        int imageResource = 0;
        Log.d("TemplatePicker==>"," Custom Theme SELECTED_COVER_STYLE::-"+FTApp.getPref().get(SystemPref.SELECTED_COVER_STYLE, 0));
        switch (FTApp.getPref().get(SystemPref.SELECTED_COVER_STYLE, 0)) {
            case R.id.customcover1:
                imageResource = 1;
                imgMask.setVisibility(View.GONE);
                imgMask2.setVisibility(View.GONE);
                layColors.setVisibility(View.GONE);
                break;
            case R.id.customcover2:
                imageResource = 2;
                imgMask.setVisibility(View.GONE);
                layColors.setVisibility(View.GONE);
                break;
            case R.id.customcover3:
                imageResource = 3;
                imgMask.setVisibility(View.GONE);
                imgMask2.setVisibility(View.GONE);
                imgMask3.setVisibility(View.VISIBLE);
                layColors.setVisibility(View.GONE);
                imgMask3.setBlurredView(imaTheme);
                break;
            case R.id.customcover4:
                imageResource = R.drawable.custommask1;
                imgMask.setBackgroundResource(imageResource);
                DrawableUtil.setGradientDrawableColor(imgMask, "#fdc52e", 0);
                break;
            case R.id.customcover5:
                imageResource = R.drawable.custommask2;
                imgMask.setBackgroundResource(imageResource);
                DrawableUtil.setGradientDrawableColor(imgMask, "#fdc52e", 0);
                break;
            case R.id.customcover6:
                imageResource = R.drawable.custommask3;
                imgMask.setBackgroundResource(imageResource);
                DrawableUtil.setGradientDrawableColor(imgMask, "#fdc52e", 0);
                imgMask2.setVisibility(View.GONE);
                break;
        }*/

        /*check.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                txtEmail.setVisibility(View.VISIBLE);
            } else {
                check.setBackgroundColor(Color.parseColor("#5377F"));
                txtEmail.setVisibility(View.GONE);
            }
        });*/
    }

    public Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false);
        bm.recycle();
        return resizedBitmap;
    }

    @OnClick(R.id.btnCreateTheme)
    public void onCreate() {
        if (txtEmail.getVisibility() == View.VISIBLE) {
            if (check.isChecked() && txtEmail.getText().toString().trim().length() == 0) {
                txtEmail.setError(getContext().getString(R.string.field_cannot_be_empty));
                return;
            }
        }
        bitmap = imaTheme.crop();

        if (bitmap != null)
            if (imgMask2.getVisibility() == View.VISIBLE) {
                Bitmap bitmapMask = Bitmap.createBitmap(imgMask2.getWidth(), imgMask2.getHeight(), Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmapMask);
                Drawable bgDrawable = imgMask2.getBackground();

                Log.d("TemplatePicker==>", "bgDrawable::- ::-" + bgDrawable);
                Log.d("TemplatePicker==>", "imgMask3 getWidth::- ::-" + imgMask2.getWidth() + " getHeight::- " + imgMask2.getHeight());
                Log.d("TemplatePicker==>", "bitmapMask getWidth::- ::-" + bitmapMask.getWidth() + " getHeight::- " + bitmapMask.getHeight());
                Log.d("TemplatePicker==>", "bitmap getWidth::- ::-" + bitmap.getWidth() + " getHeight::- " + bitmap.getHeight());
                if (bgDrawable != null) {
                    bgDrawable.setAlpha(30);
                    bgDrawable.draw(canvas);
                } else {
                    canvas.drawColor(Color.TRANSPARENT);
                }
                if (check.isChecked()) {
//                    String titleBand = !TextUtils.isEmpty(txtEmail.getText().toString()) ? txtEmail.getText().toString() : "";
                    String titleBand ="";
                    Paint paint = new Paint();
                    float textSize = 12 * getContext().getResources().getDisplayMetrics().density;
                    paint.setTextSize(textSize);
                    paint.setTypeface(Typeface.DEFAULT_BOLD);
                    paint.setColor(Color.BLUE);
                    paint.setTextAlign(Paint.Align.CENTER);
                    canvas.drawText(titleBand, bitmapMask.getWidth() / 2, (bitmapMask.getHeight() / 2) + textSize / 2, paint);
                    imgMask2.draw(canvas);
                }

            /*FTAppConfig.saveImageInDummy(bitmap,"bitmap");
            FTAppConfig.saveImageInDummy(bitmapMask,"bitmapMask");
            bitmap = ProcessingBitmap(bitmap,bitmapMask);*/

           /* imgMask2.buildDrawingCache(true);
            Bitmap  bmap = imgMask2.getDrawingCache(true);
            imgMask2.setImageBitmap(bmap);

            Bitmap bitmap2 = Bitmap.createBitmap(imgMask2.getDrawingCache(true));*/

                bitmap = BitmapUtil.getMergedBitmap(getContext(), bitmap, bitmapMask, ((RelativeLayout.LayoutParams) imgMask2.getLayoutParams()).topMargin);
            }

        //
        //bitmap = imaTheme.crop();
        /*if (imgMask.getVisibility() == View.VISIBLE) {
            Bitmap bitmapMask = Bitmap.createBitmap(imgMask.getWidth(), imgMask.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmapMask);
            Drawable bgDrawable = imgMask.getBackground();
            if (bgDrawable != null) {
                bgDrawable.draw(canvas);
            } else {
                canvas.drawColor(Color.WHITE);
            }
            imgMask.draw(canvas);
            bitmap = BitmapUtil.getMergedBitmap(getContext(), bitmap, bitmapMask, 0);
        }

        if (imgMask2.getVisibility() == View.VISIBLE) {
            Bitmap bitmapMask = Bitmap.createBitmap(imgMask2.getWidth(), imgMask2.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmapMask);
            Drawable bgDrawable = imgMask2.getBackground();
            if (bgDrawable != null) {
                bgDrawable.setAlpha(30);
                bgDrawable.draw(canvas);
            } else {
                canvas.drawColor(Color.WHITE);
            }
            bgDrawable.draw(canvas);
            bitmap = BitmapUtil.getMergedBitmap(getContext(), bitmap, bitmapMask, ((RelativeLayout.LayoutParams) imgMask2.getLayoutParams()).topMargin);
        }*/

        /*if (imgMask3.getVisibility() == View.VISIBLE) {
            Bitmap bitmapMask = Bitmap.createBitmap(imgMask3.getWidth(), imgMask3.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmapMask);
            Drawable bgDrawable = imgMask3.getBackground();
            if (bgDrawable != null) {
                bgDrawable.draw(canvas);
            } else {
                canvas.drawColor(Color.WHITE);
            }
            imgMask3.draw(canvas);
            bitmap = BitmapUtil.getMergedBitmap(getContext(), bitmap, bitmapMask, ((RelativeLayout.LayoutParams) imgMask3.getLayoutParams()).topMargin);
        }*/

        //File customCoversDir = new File(FTConstants.CUSTOM_COVERS_PATH);
        File customCoversDir = new File(FTConstants.TEMP_FOLDER_PATH+"customcover/");
        if (!customCoversDir.exists()) {
            customCoversDir.mkdirs();
        }

        if (customCoversDir.exists()) {
            Stream<File> stream = Arrays.stream(customCoversDir.listFiles());
            if (stream.anyMatch(entry -> FTDocumentUtils.getFileNameWithoutExtension(getContext(), FTUrl.parse(entry.getName())).equals(txtEmail.getText().toString()))) {
                txtEmail.setError(getString(R.string.name_already_exists));
            } else {
                txtEmail.setEllipsize(TextUtils.TruncateAt.END);
                String edtFileName = ScreenUtil.ellipsize(txtEmail.getText().toString(), FTConstants.CUSTOM_TEXT_MAX_SIZE);
                callback.onDataSubmit(edtFileName, bitmap, check.isChecked());
                dismiss();
            }
        }
    }

    private Bitmap ProcessingBitmap(Bitmap source1, Bitmap source2) {
        Bitmap bm1 = null;
        Bitmap bm2 = null;
        Bitmap newBitmap = null;

        //try {
            /*bm1 = BitmapFactory.decodeStream(
                    getContext().getContentResolver().openInputStream(source1));
            bm2 = BitmapFactory.decodeStream(
                    getContext().getContentResolver().openInputStream(source2));
*/
        bm1 = source1;
        bm2 = source2;
        int w;
        if (bm1.getWidth() >= bm2.getWidth()) {
            w = bm1.getWidth();
        } else {
            w = bm2.getWidth();
        }

        int h;
        if (bm1.getHeight() >= bm2.getHeight()) {
            h = bm1.getHeight();
        } else {
            h = bm2.getHeight();
        }

        Bitmap.Config config = bm1.getConfig();
        if (config == null) {
            config = Bitmap.Config.ARGB_8888;
        }

        newBitmap = Bitmap.createBitmap(w, h, config);
        Canvas newCanvas = new Canvas(newBitmap);

        newCanvas.drawBitmap(bm1, 0, 0, null);

        Paint paint = new Paint();
        paint.setAlpha(128);
        newCanvas.drawBitmap(bm2, 0, 0, paint);

        FTAppConfig.saveImageInDummy(newBitmap, "newBitmap");
        /*} catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }*/

        return newBitmap;
    }

    @OnClick(R.id.imgAddDialogClose)
    void onClose() {
        dismiss();
    }

    @OnClick(R.id.laySave)
    void onFutureSave() {
        check.performClick();
    }

    @Override
    public void onClick(View view) {
        Log.d("TemplatePicker==>", "onCheckedChanged onClick");
        switch (view.getId()) {
            case R.id.check:
                if (check.isChecked()) {
                    Log.d("TemplatePicker==>", "onCheckedChanged Checked");
                    txtEmail.setVisibility(View.VISIBLE);
                } else {
                    txtEmail.setVisibility(View.GONE);
                    Log.d("TemplatePicker==>", "onCheckedChanged NOT Checked");
                }
                break;
        }
    }

    /*@OnClick({R.id.color1, R.id.color2, R.id.color3, R.id.color4, R.id.color5})
    void onColorSelect(View view) {
        String imageResource = "";
        switch (view.getId()) {
            case R.id.color1:
                imageResource = colors[0];
                break;
            case R.id.color2:
                imageResource = colors[1];
                break;
            case R.id.color3:
                imageResource = colors[2];
                break;
            case R.id.color4:
                imageResource = colors[3];
                break;
            case R.id.color5:
                imageResource = colors[4];
                break;
        }
        DrawableUtil.setGradientDrawableColor(imgMask, imageResource, 0);
        int[] location = new int[2];
        view.getLocationInWindow(location);
        selectView.setX(location[0]);
        selectView.setY(location[1]);
    }*/

    public interface DialogResult {
        public void onDataSubmit(String name, Bitmap bitmap, boolean isSaved);
    }

}
