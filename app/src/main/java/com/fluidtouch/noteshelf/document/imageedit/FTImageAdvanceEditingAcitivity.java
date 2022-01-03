package com.fluidtouch.noteshelf.document.imageedit;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.fluidtouch.noteshelf.commons.ui.FTBaseActivity;
import com.fluidtouch.noteshelf.document.imageedit.view.FTImageEraseView;
import com.fluidtouch.noteshelf.document.imageedit.view.FTImageLassoView;
import com.fluidtouch.noteshelf.document.undomanager.UndoManager;
import com.fluidtouch.noteshelf2.R;

import java.io.FileInputStream;
import java.io.FileOutputStream;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FTImageAdvanceEditingAcitivity extends FTBaseActivity implements FTImageCrop.onImageCropCallbacks, FTImageEraseView.onImageEraseCallbacks, FTImageLassoView.onImageLassoCallbacks {

    //region Member Variables
    public static final int IMAGE_ACTIVITY = 205;
    public static final int DONE = 0;
    public static final int CROP = 1;
    public static final int ERASE = 2;
    public static final int LASSO = 3;
    public static final int APPLY = 4;
    @BindView(R.id.layImage)
    LinearLayout layImage;
    @BindView(R.id.checkCrop)
    CheckBox checkCrop;
    @BindView(R.id.checkErase)
    CheckBox checkErase;
    @BindView(R.id.checkLasso)
    CheckBox checkLasso;
    @BindView(R.id.btnUndo)
    TextView btnUndo;
    @BindView(R.id.btnRedo)
    TextView btnRedo;
    @BindView(R.id.btnReset)
    TextView btnReset;
    @BindView(R.id.btnApply)
    TextView btnApply;
    FTImageCrop imageCrop;
    FTImageLassoView ftImageLassoView;
    FTImageEraseView ftImageEraseView;
    FileInputStream is = null;
    boolean isFirstTime = true;
    int width = 0;
    int height = 0;
    int layWidth = 0;
    int layHeight = 0;
    float scale = 1;
    boolean isEdited = false;
    UndoManager undoManager;
    private Bitmap bitmap;

    public static void start(Context context, String bitmap) {
        Intent intent = new Intent(context, FTImageAdvanceEditingAcitivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("image", bitmap);
        intent.putExtras(bundle);
        ((AppCompatActivity) context).startActivityForResult(intent, IMAGE_ACTIVITY);
    }
    //endregion

    //region life cycle methods
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_edit);
        ButterKnife.bind(this);
        undoManager = new UndoManager();
        String filename = getIntent().getStringExtra("image");
        try {
            is = openFileInput(filename);
            bitmap = BitmapFactory.decodeStream(is);
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        layImage.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (!isFirstTime || bitmap == null)
                    return;
                isFirstTime = false;
                layWidth = v.getWidth();
                layHeight = v.getHeight();
                setUp(bitmap);
            }
        });


    }

    //region initialize views
    public void setUp(Bitmap bitmapParam) {
        if (bitmapParam == null)
            return;
        float actualHeight = bitmapParam.getHeight();
        float actualWidth = bitmapParam.getWidth();
        float imgRatio = actualWidth / actualHeight;
        float maxRatio = (float) layWidth / layHeight;

        if (imgRatio != maxRatio) {
            if (imgRatio < maxRatio) {
                imgRatio = layHeight / actualHeight;
                actualWidth = imgRatio * actualWidth;
                actualHeight = layHeight;
            } else {
                imgRatio = this.layWidth / actualWidth;
                actualHeight = imgRatio * actualHeight;
                actualWidth = this.layWidth;
            }
        }
        width = (int) actualWidth;
        height = (int) actualHeight;
        scale = (float) width / bitmapParam.getWidth();
        bitmapParam = Bitmap.createScaledBitmap(bitmapParam, width, height, true);
        bitmap = bitmapParam;

        if (bitmap != null) {
            layImage.removeAllViews();
            if (checkCrop.isChecked()) {
                addCropMode();
            } else if (checkErase.isChecked()) {
                addEraseMode();
            } else if (checkLasso.isChecked()) {
                addLassoMode();
            }
        }
    }
    //endregion

    private void addCropMode() {
        imageCrop = new FTImageCrop(FTImageAdvanceEditingAcitivity.this);
        imageCrop.setBitmap(bitmap);
        imageCrop.setScale(scale);
        imageCrop.setListener(FTImageAdvanceEditingAcitivity.this);
        imageCrop.setUP();
        btnApply.setVisibility(View.VISIBLE);
    }

    private void addEraseMode() {
        ftImageEraseView = new FTImageEraseView(this);
        ftImageEraseView.setListener(FTImageAdvanceEditingAcitivity.this);
        ftImageEraseView.setBitmap(bitmap);
        ftImageEraseView.setUp();
        btnApply.setVisibility(View.GONE);
    }

    private void addLassoMode() {
        ftImageLassoView = new FTImageLassoView(this);
        ftImageLassoView.setListener(FTImageAdvanceEditingAcitivity.this);
        ftImageLassoView.setImageBitmap(bitmap);
        ftImageLassoView.setUp();
        btnApply.setVisibility(View.VISIBLE);
    }

    public void addView(View view) {
        layImage.removeAllViews();
        layImage.addView(view);
    }

    //region interface methods
    @Override
    public void onEraseTouch() {
        addUndo(ftImageEraseView.getUndoBitmap());
    }
    //endregion

    @Override
    public void onEdited() {
        isEdited = true;
        btnApply.setEnabled(true);
        btnApply.setAlpha(1f);
        btnApply.setTextColor(getResources().getColor(R.color.colorAccent, null));
    }

    //region onclick events
    @OnClick(R.id.checkCrop)
    public void onCrop() {
        if (checkCrop.isChecked()) {
            onToolSelectionChanged(CROP);
        } else {
            checkCrop.setChecked(true);
        }
    }
    //endregion

    @OnClick(R.id.checkErase)
    public void onErase() {
        if (checkErase.isChecked()) {
            btnUndo.setEnabled(false);
            btnUndo.setAlpha(0.5f);
            btnRedo.setEnabled(false);
            btnRedo.setAlpha(0.5f);
            undoManager = new UndoManager();
            onToolSelectionChanged(ERASE);
        } else {
            checkErase.setChecked(true);
        }
    }

    @OnClick(R.id.checkLasso)
    public void onLasso() {
        if (checkLasso.isChecked()) {
            onToolSelectionChanged(LASSO);
        } else {
            checkLasso.setChecked(true);
        }
    }

    @OnClick(R.id.btnUndo)
    public void onUndo() {
        if (undoManager.canUndo()) {
            undoManager.undo();
            if (!undoManager.canUndo()) {
                btnUndo.setEnabled(false);
                btnUndo.setAlpha(0.5f);
            }
            if (undoManager.canRedo()) {
                btnRedo.setEnabled(true);
                btnRedo.setAlpha(1f);
            }
        }
    }

    @OnClick(R.id.btnRedo)
    public void onRedo() {
        if (undoManager.canRedo()) {
            undoManager.redo();
            if (!undoManager.canRedo()) {
                btnRedo.setEnabled(false);
                btnRedo.setAlpha(0.5f);
            }
            if (undoManager.canUndo()) {
                btnUndo.setEnabled(true);
                btnUndo.setAlpha(1f);
            }
        }
    }

    @OnClick(R.id.btnReset)
    public void onReset() {
        String filename = getIntent().getStringExtra("image");
        try {
            is = openFileInput(filename);
            bitmap = BitmapFactory.decodeStream(is);
            is.close();
            setUp(bitmap);
            btnUndo.setEnabled(false);
            btnUndo.setAlpha(0.5f);
            btnRedo.setEnabled(false);
            btnRedo.setAlpha(0.5f);
            btnApply.setEnabled(false);
            btnApply.setAlpha(0.5f);
            btnApply.setTextColor(getResources().getColor(R.color.white, null));
            isEdited = false;
            btnReset.setEnabled(false);
            btnReset.setAlpha(0.5f);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @OnClick(R.id.btnApply)
    public void onApply() {
        setChangedBitmap(APPLY, false, false);
        isEdited = false;
        btnApply.setEnabled(false);
        btnApply.setAlpha(0.5f);
        btnApply.setTextColor(getResources().getColor(R.color.white, null));
    }

    @OnClick(R.id.imageEditDone)
    public void onSaveClicked() {
        Bitmap editedBitmap = setChangedBitmap(DONE, false, true);
        try {
            String filename = "bitmap.png";
            FileOutputStream stream = openFileOutput(filename, Context.MODE_PRIVATE);
            editedBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);

            //Cleanup
            stream.close();
            Intent intent = new Intent();
            intent.putExtra("image", filename);
            setResult(IMAGE_ACTIVITY, intent);
            finish();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    @OnClick(R.id.btnCancel)
    public void onCancelClicked() {
        finish();
    }

    //region undoRedo
    void addUndo(Bitmap bitmap) {
        btnReset.setEnabled(true);
        btnReset.setAlpha(1);
        btnUndo.setEnabled(true);
        btnUndo.setAlpha(1f);
        undoManager.addUndo(FTImageAdvanceEditingAcitivity.class, "undoRedo", 1, new Object[]{bitmap}, FTImageAdvanceEditingAcitivity.this);
    }
    //endregion

    void addRedo(Bitmap bitmap) {
        btnRedo.setEnabled(true);
        btnRedo.setAlpha(1f);
        undoManager.addUndo(FTImageAdvanceEditingAcitivity.class, "undoRedo", 1, new Object[]{bitmap}, FTImageAdvanceEditingAcitivity.this);
    }

    public void undoRedo(Bitmap bitmap1) {
        if (checkCrop.isChecked()) {
            addRedo(bitmap);
        } else if (checkErase.isChecked()) {
            addRedo(ftImageEraseView.getErasedBitmap());
        } else if (checkLasso.isChecked()) {
            addRedo(bitmap);
        }
        setUp(bitmap1);
    }

    //region ConfirmationAlert
    private void showConfirmationAlert(ConfirmationResponse listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.would_you_like_to_apply_the_changes_to_the_image_before_proceeding_to_another_option));
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                listener.onConfirmed();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                listener.onCanceled();
                dialog.dismiss();
            }
        });
        builder.setCancelable(false);
        builder.show();
    }
    //endregion

    //region getChanged image
    private void onToolSelectionChanged(int toolType) {
        if (isEdited)
            showConfirmationAlert(new ConfirmationResponse() {
                @Override
                public void onConfirmed() {
                    setChangedBitmap(toolType, false, false);
                }

                @Override
                public void onCanceled() {
                    setChangedBitmap(toolType, true, false);
                }
            });
        else
            setChangedBitmap(toolType, true, false);
    }
    //endregion

    private Bitmap setChangedBitmap(int toolType, boolean isCanceled, boolean isDone) {
        Bitmap editedBitmap = null;
        if (isCanceled) {
            if (toolType != ERASE) {
                if (toolType == CROP) {
                    checkLasso.setChecked(false);
                    checkErase.setChecked(false);
                } else {
                    btnUndo.setEnabled(false);
                    btnUndo.setAlpha(0.5f);
                    btnRedo.setEnabled(false);
                    btnRedo.setAlpha(0.5f);
                    undoManager = new UndoManager();
                    checkCrop.setChecked(false);
                    checkErase.setChecked(false);
                }
                editedBitmap = checkErase.isChecked() ? ftImageEraseView.getErasedBitmap() : bitmap;
            } else {
                checkCrop.setChecked(false);
                checkLasso.setChecked(false);
                editedBitmap = bitmap;
            }
            setUp(editedBitmap);
            return null;
        }

        if (checkCrop.isChecked() && toolType != CROP) {
            if (toolType == APPLY)
                addUndo(bitmap);
            else {
                btnUndo.setEnabled(false);
                btnUndo.setAlpha(0.5f);
                btnRedo.setEnabled(false);
                btnRedo.setAlpha(0.5f);
                undoManager = new UndoManager();
                checkCrop.setChecked(false);
            }
            editedBitmap = imageCrop.getCropedBitmap();
            editedBitmap = Bitmap.createScaledBitmap(editedBitmap, (int) (editedBitmap.getWidth() / scale), (int) (editedBitmap.getHeight() / scale), true);
        } else if (checkErase.isChecked() && toolType != ERASE) {
            editedBitmap = ftImageEraseView.getErasedBitmap();
            editedBitmap = Bitmap.createScaledBitmap(editedBitmap, (int) (editedBitmap.getWidth() / scale), (int) (editedBitmap.getHeight() / scale), true);
            checkErase.setChecked(false);
        } else if (checkLasso.isChecked() && toolType != LASSO) {
            if (toolType == APPLY)
                addUndo(bitmap);
            else {
                btnUndo.setEnabled(false);
                btnUndo.setAlpha(0.5f);
                btnRedo.setEnabled(false);
                btnRedo.setAlpha(0.5f);
                undoManager = new UndoManager();
                checkLasso.setChecked(false);
            }
            if (ftImageLassoView.getRegion() != null) {
                try {
                    editedBitmap = ftImageLassoView.getlasooBitmap();
                    editedBitmap = Bitmap.createScaledBitmap(editedBitmap, (int) (editedBitmap.getWidth() / scale), (int) (editedBitmap.getHeight() / scale), true);
                } catch (Exception e) {
                    editedBitmap = bitmap;
                }
            } else {
                editedBitmap = bitmap;
                editedBitmap = Bitmap.createScaledBitmap(editedBitmap, (int) (editedBitmap.getWidth() / scale), (int) (editedBitmap.getHeight() / scale), true);
            }
        }
        if (!isDone)
            setUp(editedBitmap);
        isEdited = false;
        return editedBitmap;
    }

    interface ConfirmationResponse {
        void onConfirmed();

        void onCanceled();
    }
    //endregion
}
