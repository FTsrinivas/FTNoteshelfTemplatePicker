package com.fluidtouch.noteshelf.scandocument;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.fluidtouch.noteshelf.commons.ui.FTBaseActivity;
import com.fluidtouch.noteshelf.commons.utils.FTPermissionManager;
import com.fluidtouch.noteshelf.scandocument.fragments.CameraFragment;
import com.fluidtouch.noteshelf2.R;

public class ScanActivity extends FTBaseActivity {

    private static final int PICK_FROM_CAMERA = 102;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        if (null == savedInstanceState)
            if (FTPermissionManager.checkPermission(this, this, new String[]{Manifest.permission.CAMERA}, PICK_FROM_CAMERA)) {
                scanDoc();
            }
    }

    private void scanDoc() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.scan_camera_container, new CameraFragment())
                .commit();
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            scanDoc();
        } else {
            Toast.makeText(this, R.string.camera_access_error, Toast.LENGTH_LONG).show();
            finish();
        }
    }
}
