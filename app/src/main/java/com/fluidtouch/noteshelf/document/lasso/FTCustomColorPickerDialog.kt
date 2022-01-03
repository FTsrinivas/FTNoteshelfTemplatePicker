package com.fluidtouch.noteshelf.document.lasso

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import com.fluidtouch.noteshelf.commons.ui.FTBaseDialog
import com.fluidtouch.noteshelf.document.penracks.FTCustomColorPickerFragment
import com.fluidtouch.noteshelf2.R
import com.google.android.material.bottomsheet.BottomSheetDialog

class FTCustomColorPickerDialog : FTBaseDialog(), FTCustomColorPickerFragment.ColorPickerContainerCallback {

    private lateinit var mParentCallback: FTCustomColorPickerFragment.ColorPickerContainerCallback
    override fun onAttach(context: Context) {
        super.onAttach(context)
        mParentCallback = parentFragment as FTCustomColorPickerFragment.ColorPickerContainerCallback
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_custom_color_picker, container, false);
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        childFragmentManager!!.beginTransaction()
                .add(R.id.layout_color_picker_container,
                        FTCustomColorPickerFragment.newInstance("#000000", 0, false, true, this))
                .commit()
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog
        if (dialog != null && !(dialog is BottomSheetDialog)) {
            val size = getDialogSizeByPercentage(0.75f, 0.50f)
            dialog.window!!.setLayout(size.width, WRAP_CONTENT)
            dialog.window!!.setGravity(Gravity.CENTER)
            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        } else if (dialog != null) {
            val bottomSheetDialog = dialog as BottomSheetDialog
            val bottomSheet = bottomSheetDialog.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.setBackgroundColor(Color.TRANSPARENT)
            bottomSheet?.layoutParams?.height = WRAP_CONTENT
        }
    }

    override fun onBackClicked() {
        dismissAllowingStateLoss()
    }

    override fun isColorExistsInRack(color: String?): Boolean {
        return false
    }

    override fun addColorToRack(color: String?, position: Int) {
        mParentCallback.addColorToRack(color, position)
    }
}