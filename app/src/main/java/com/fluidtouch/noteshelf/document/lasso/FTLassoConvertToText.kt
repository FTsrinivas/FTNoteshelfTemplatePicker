package com.fluidtouch.noteshelf.document.lasso

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.RectF
import android.os.Bundle
import android.text.TextUtils
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.util.SizeF
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.fluidtouch.noteshelf.FTApp
import com.fluidtouch.noteshelf.commons.FTLog
import com.fluidtouch.noteshelf.commons.settingsUI.dialogs.FTSelectLanguageDialog
import com.fluidtouch.noteshelf.commons.ui.FTBaseDialog
import com.fluidtouch.noteshelf.commons.utils.ObservingService
import com.fluidtouch.noteshelf.preferences.SystemPref
import com.fluidtouch.noteshelf.textrecognition.handwriting.FTHandwritingRecognitionResult
import com.fluidtouch.noteshelf.textrecognition.handwriting.FTHandwritingRecognitionTask
import com.fluidtouch.noteshelf.textrecognition.handwriting.FTHandwritingRecognitionTaskProcessor
import com.fluidtouch.noteshelf.textrecognition.handwriting.languageresourcehandlers.FTLanguageResourceManager
import com.fluidtouch.noteshelf.textrecognition.handwriting.languageresourcehandlers.FTRecognitionLangResource
import com.fluidtouch.noteshelf.textrecognition.helpers.backgroundtask.FTBackgroundTaskProtocols.OnCompletion
import com.fluidtouch.noteshelf2.R
import com.fluidtouch.renderingengine.annotation.FTAnnotation
import kotlinx.android.synthetic.main.dialog_ct_textbox_settings.*
import kotlinx.android.synthetic.main.dialog_hwtotext_container.*
import java.util.*


class FTLassoConvertToText : FTBaseDialog() {

    private var convertToTextCallbacks: ConvertToTextCallbacks? = null
    private var taskProcessor: FTHandwritingRecognitionTaskProcessor? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        convertToTextCallbacks = parentFragment as ConvertToTextCallbacks
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_hw_to_text, container, false);
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUi()
    }

    private fun initUi() {
        edit_convert_text.setMovementMethod(ScrollingMovementMethod());
        text_language.setOnClickListener(onClickListener)
        image_close.setOnClickListener(onClickListener)
        button_copy_to_clipboard.setOnClickListener(onClickListener)
        button_convert_to_textbox.setOnClickListener(onClickListener)
        image_ct_settings.setOnClickListener(onClickListener)
        image_ct_back.setOnClickListener(onClickListener)
        text_font_default.setOnClickListener(onClickListener)
        text_ct_font_fit_to_selection.setOnClickListener(onClickListener)
        startRecognition()
    }

    private fun startRecognition() {
        val code = FTApp.getPref().get(SystemPref.CONVERT_TO_TEXT_LANGUAGE, "en_US")
        var availableLanguageResources: List<FTRecognitionLangResource?>? = ArrayList()
        availableLanguageResources = if (FTApp.getPref().get(SystemPref.IS_SHW_ENABLED, false)) {
            FTLanguageResourceManager.getInstance().availableLanguageResourcesForSHW()
        } else {
            FTLanguageResourceManager.getInstance().availableLanguageResources()
        }
        for (language in availableLanguageResources) {
            if (language.languageCode == code) {
                text_language.text = language.displayNameKey
                break
            }
        }
        FTApp.getPref().save(SystemPref.DID_SHW_PREVIOUSLY_SEARCHED, true)
        convertToText(code)
    }

    private var onClickListener = View.OnClickListener { v ->
        when (v?.id) {
            R.id.image_close -> {
                dismissAllowingStateLoss()
            }
            R.id.text_language -> {
                FTSelectLanguageDialog.newInstance(true).show(parentFragmentManager)
                ObservingService.getInstance().addObserver("languageChange", languageChangeObserver)
            }
            R.id.button_copy_to_clipboard -> {
                copyToClipboard()
            }
            R.id.button_convert_to_textbox -> {
                convertToTextBox()
            }
            R.id.image_ct_settings -> {
                openTextboxSettings()
            }
            R.id.image_ct_back -> {
                layout_textbox_settings.visibility = View.GONE
                layout_convert_to_text.visibility = View.VISIBLE
            }
            R.id.text_font_default -> {
                text_font_default.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.check, 0)
                text_ct_font_fit_to_selection.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
                FTApp.getPref().save(SystemPref.CONVERT_TO_TEXTBOX_FONT_TYPE, "default")
            }
            R.id.text_ct_font_fit_to_selection -> {
                text_font_default.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
                text_ct_font_fit_to_selection.setCompoundDrawablesWithIntrinsicBounds(
                    0,
                    0,
                    R.drawable.check,
                    0
                )
                FTApp.getPref().save(SystemPref.CONVERT_TO_TEXTBOX_FONT_TYPE, "fit")
            }
        }
    }

    private val languageChangeObserver = Observer { observable: Observable?, o: Any? ->
        if (activity != null && isVisible) {
            requireActivity().runOnUiThread {
                FTApp.getPref().save(SystemPref.CONVERT_TO_TEXT_LANGUAGE, o as String)
                startRecognition()
            }
        }
    }

    private fun convertToText(languageCode: String) {
        edit_convert_text.setText("Converting...")
        if (taskProcessor == null)
            taskProcessor = FTHandwritingRecognitionTaskProcessor(context, languageCode)
        val task = FTHandwritingRecognitionTask()
        task.languageCode = languageCode
        task.currentDocument = null
        task.pageAnnotations = convertToTextCallbacks?.getSelectedAnnotations()
        val rectF = convertToTextCallbacks?.getSelectedRect()!!
        task.viewSize = SizeF(rectF.width(), rectF.height())
        task.setListener { info: FTHandwritingRecognitionResult?, error: Error? ->
            edit_convert_text.setText("")
            if (error != null) {
                FTLog.error(FTLog.HW_RECOGNITION, error.message)
                return@setListener
            } else if (info == null) {
                FTLog.error(FTLog.HW_RECOGNITION, "recognitionInfo is null")
                return@setListener
            }
            edit_convert_text.setText(info.recognisedString)
        }
        taskProcessor?.startTask(task, OnCompletion { })
    }

    private fun copyToClipboard() {
        if (!TextUtils.isEmpty(edit_convert_text.text)) {
            var clipboardManager =
                requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            if (null != clipboardManager) {
                try {
                    var clipData =
                        ClipData.newPlainText("converted Text", edit_convert_text.text.toString())
                    clipboardManager.setPrimaryClip(clipData)
                    Toast.makeText(
                        context,
                        resources.getText(R.string.text_copied),
                        Toast.LENGTH_LONG
                    ).show()
                } catch (e: Exception) {
                    Log.i(this.javaClass.name, e.message.toString())
                }
            }
        }
    }

    private fun convertToTextBox() {
        if (edit_convert_text.text.toString().isNotEmpty())
            convertToTextCallbacks?.onConvertToTextBoxClicked(edit_convert_text.text.toString())
    }

    private fun openTextboxSettings() {
        layout_textbox_settings.visibility = View.VISIBLE
        layout_convert_to_text.visibility = View.GONE
        var selectedStyle = FTApp.getPref().get(SystemPref.CONVERT_TO_TEXTBOX_FONT_TYPE, "fit")
        if (selectedStyle.equals("default", true)) {
            text_font_default.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.check, 0)
        } else {
            text_ct_font_fit_to_selection.setCompoundDrawablesWithIntrinsicBounds(
                0,
                0,
                R.drawable.check,
                0
            )
        }
    }

    interface ConvertToTextCallbacks {
        fun getSelectedAnnotations(): ArrayList<FTAnnotation>
        fun getSelectedRect(): RectF
        fun onConvertToTextBoxClicked(text: String)
    }
}