package com.noteshelf.vision

import android.graphics.Bitmap
import android.graphics.RectF
import com.fluidtouch.noteshelf.textrecognition.helpers.NSValue
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer
import java.util.*

class AppVisionTextRecognizer {
    private var textDetector: FirebaseVisionTextRecognizer? = null
    private var mListener: TextRecognizerListener? = null

    fun setTextRecognizerListener(listener: TextRecognizerListener) {
        mListener = listener;
    }

    fun getTextData(bitmap: Bitmap) {
        val image = FirebaseVisionImage.fromBitmap(bitmap)
        this.textDetector = FirebaseVision.getInstance().onDeviceTextRecognizer
        this.textDetector?.processImage(image)?.addOnSuccessListener { visionText ->
            val characterRects = ArrayList<NSValue>()
            val textBlocks = visionText.textBlocks
            for (textBlock in textBlocks) {
                val lines = textBlock.lines
                for (line in lines) {
                    if (characterRects.size > 0) {
                        characterRects.add(NSValue(RectF()))
                    }
                    val elements = line.elements
                    for (w in elements.indices) {
                        val element = elements[w]
                        val wordRect = element.boundingBox
                        if (wordRect != null) {
                            if (w > 0) {
                                characterRects.add(NSValue(RectF()))
                            }
                            if (wordRect.isEmpty) {
                                characterRects.add(NSValue(RectF()))
                            } else {
                                val wordText = element.text
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
                            }
                        }
                    }
                }
            }
            mListener?.OnTextRecognized(characterRects, visionText.getText())
        }?.addOnFailureListener { exception -> mListener?.OnFailed(exception) }
    }

    fun closeRecognizer() {
        this.textDetector?.close()
    }

    interface TextRecognizerListener {
        fun OnTextRecognized(characterRects: ArrayList<NSValue>, recognisedString: String)
        fun OnFailed(exception: Exception)
    }
}