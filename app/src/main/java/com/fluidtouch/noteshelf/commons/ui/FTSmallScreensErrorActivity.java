package com.fluidtouch.noteshelf.commons.ui;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.fluidtouch.noteshelf2.R;

public class FTSmallScreensErrorActivity extends AppCompatActivity {


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_error);
        ((RelativeLayout) findViewById(R.id.layMain)).addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                int screenSize = getResources().getConfiguration().screenLayout &
                        Configuration.SCREENLAYOUT_SIZE_MASK;
                switch (screenSize) {
                    case Configuration.SCREENLAYOUT_SIZE_XLARGE:
                        finish();
                        break;

                    case Configuration.SCREENLAYOUT_SIZE_LARGE:
                        finish();
                        break;
                }

            }
        });
    }

    @Override
    public void onBackPressed() {

    }
}
