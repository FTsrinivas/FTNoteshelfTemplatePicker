package com.fluidtouch.noteshelf.commons.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.fluidtouch.noteshelf.shelf.activities.FTBaseShelfActivity

class FTSaveToDeviceActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FTBaseActivity.isSaveToDeviceSelected = true
        setResult(FTBaseShelfActivity.PICK_EXPORTER, intent)
        finish()
    }
}