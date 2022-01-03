package com.noteshelf.vision

import android.graphics.Bitmap
import android.graphics.RectF
import com.fluidtouch.noteshelf.textrecognition.helpers.NSValue
import com.huawei.hmf.tasks.OnSuccessListener
import com.huawei.hmf.tasks.Task
import com.huawei.hms.mlsdk.MLAnalyzerFactory
import com.huawei.hms.mlsdk.common.MLFrame
import com.huawei.hms.mlsdk.text.MLLocalTextSetting
import com.huawei.hms.mlsdk.text.MLText
import com.huawei.hms.mlsdk.text.MLTextAnalyzer
import java.util.*

class AppVisionTextRecognizer {

    private var textAnalyzer: MLTextAnalyzer? = null
    private var mListener: TextRecognizerListener? = null

    fun setTextRecognizerListener(listener: TextRecognizerListener) {
        mListener = listener;
    }

    fun getTextData(bitmap: Bitmap) {
        val setting = MLLocalTextSetting.Factory()
                .setOCRMode(MLLocalTextSetting.OCR_DETECT_MODE)
                .setLanguage("en")
                .create()
        this.textAnalyzer = MLAnalyzerFactory.getInstance()
                .getLocalTextAnalyzer(setting)
        val frame = MLFrame.fromBitmap(bitmap)
        val task: Task<MLText>? = this.textAnalyzer?.asyncAnalyseFrame(frame)
        task?.addOnSuccessListener(OnSuccessListener { text ->
            // Recognition success.
            val characterRects = ArrayList<NSValue>()
            val textBlocks: List<MLText.Block> = text.getBlocks()
            for (textBlock in textBlocks) {
                val lines = textBlock.contents
                for (line in lines) {
                    if (characterRects.size > 0) {
                        characterRects.add(NSValue(RectF()))
                    }
                    val elements = line.contents
                    for (w in elements.indices) {
                        val element = elements[w]
                        val wordRect = element.border
                        if (wordRect != null) {
                            if (w > 0) {
                                characterRects.add(NSValue(RectF()))
                            }
                            if (wordRect.isEmpty) {
                                characterRects.add(NSValue(RectF()))
                            } else {
                                val wordText = element.stringValue
                                if (wordText.length > 0) {
                                    val eachCharWidth = wordRect.width() / wordText.length
                                    var prevChar: RectF? = null
                                    for (i in 0 until wordText.length) {
                                        val charRect = RectF()
                                        if (prevChar == null) {
                                            charRect.left = wordRect.left.toFloat()
                                        } else {
                                            charRect.left = prevChar.right
                                        }
                                        charRect.right = charRect.left + eachCharWidth
                                        charRect.top = wordRect.top.toFloat()
                                        charRect.bottom = wordRect.bottom.toFloat()
                                        characterRects.add(NSValue(charRect))
                                        prevChar = charRect
                                    }
                                } else {
                                    characterRects.add(NSValue(RectF()))
                                }
                            }
                        }
                    }
                }
            }
            mListener?.OnTextRecognized(characterRects, text.stringValue)
        })?.addOnFailureListener { exception -> mListener?.OnFailed(exception) }
    }

    fun closeRecognizer() {

    }

    interface TextRecognizerListener {
        fun OnTextRecognized(characterRects: ArrayList<NSValue>, recognisedString: String)
        fun OnFailed(exception: Exception)
    }
}