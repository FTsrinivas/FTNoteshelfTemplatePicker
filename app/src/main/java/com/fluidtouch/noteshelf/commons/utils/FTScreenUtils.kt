package com.fluidtouch.noteshelf.commons.utils

import android.content.Context
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.Log
import android.util.SizeF
import android.util.TypedValue
import androidx.core.content.res.ResourcesCompat
import com.dd.plist.NSArray
import com.dd.plist.NSDictionary
import com.dd.plist.PropertyListParser
import com.fluidtouch.noteshelf.generator.formats.dayandnight_journal.QuoteItem
import com.fluidtouch.noteshelf2.R
import kotlin.random.Random

object FTScreenUtils {
    var quotesList = ArrayList<QuoteItem>()

    fun drawCenterText(
        context: Context,
        xPosition: Float,
        yPosition: Float,
        canvas: Canvas?,
        text: String,
        textSize: Int,
        scaleFactor : Float
    ) {
        val rect = Rect()
        /*
         * I have had some problems with Canvas.getHeight() if API < 16. That's why I use Canvas.getClipBounds(Rect) instead. (Do not use Canvas.getClipBounds().getHeight() as it allocates memory for a Rect.)
         * */canvas!!.getClipBounds(rect)
        val paint = Paint()
        paint.textAlign = Paint.Align.CENTER
        /* paint.textSize = dairyTextSize*/
        paint.textSize = textSize *  scaleFactor
        paint.color = Color.GRAY
        paint.style = Paint.Style.FILL
        paint.typeface = ResourcesCompat.getFont(context, R.font.lora_italic)
        paint.getTextBounds(text, 0, text.length, rect)
        canvas.drawText(text, xPosition, yPosition, paint)
    }

    fun getQuotesList(context: Context): ArrayList<QuoteItem> {
        val res: Resources = context.resources
        val istream = res.assets.open("quotes.plist")
        val rootDict = PropertyListParser.parse(istream)

        (rootDict as NSArray).array.forEachIndexed { index, nsObject ->
            val item = QuoteItem(
                (nsObject as NSDictionary).get("Quote").toString(),
                nsObject.get("Author").toString()
            )
            quotesList.add(item)
        }
        return quotesList
    }

    fun pickRandomQuote(context: Context, mQuotesList: ArrayList<QuoteItem>): QuoteItem {
        return try {
            var randomIndex: Int = Random.nextInt(mQuotesList.size)
            val randomElement = mQuotesList[randomIndex]
            mQuotesList.removeAt(randomIndex)
            println(randomElement)
            randomElement
        } catch (e: Exception) {
            quotesList = getQuotesList(context)
            QuoteItem(
                "The place to be happy is here. The time to be happy is now.",
                "-Robert G. Ingersoll"
            )
        }
    }

    fun findColumnCount(isLandScape: Boolean): Int {
        return if (!isLandScape/*orientation == Configuration.ORIENTATION_PORTRAIT*/) 3 else 4
    }

    fun findRowCount(isLandScape: Boolean): Int {
        return if (!isLandScape/*orientation == Configuration.ORIENTATION_PORTRAIT*/) 4 else 3
    }

    fun findMaxLines(isLandScape: Boolean): Int {
        return if (!isLandScape/*orientation == Configuration.ORIENTATION_PORTRAIT*/) 3 else 2
    }

    fun aspectSize(size: SizeF, maxSize: SizeF): SizeF {
        val originalAspectRatio = size.width / size.height
        val maxAspectRatio = maxSize.width / maxSize.height
        var width = maxSize.width
        var height = maxSize.height
        if (originalAspectRatio > maxAspectRatio) {
            height = maxSize.width / originalAspectRatio
        } else {
            width = maxSize.height * originalAspectRatio
        }
        return SizeF(width, height)
    }

    fun getDayOfMonthSuffix(n: Int): String? {
        return try {
            if (n >= 11 && n <= 13) {
                return n.toString() + "th"
            }
            when (n % 10) {
                1 -> n.toString() + "st"
                2 -> n.toString() + "nd"
                3 -> n.toString() + "rd"
                else -> n.toString() + "th"
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            n.toString() + ""
        }
    }

    public fun getStatusBarHeight(context: Context): Int {
        var result = 0
        val resourceId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = context.resources.getDimensionPixelSize(resourceId)
        }
        return result
    }

    public fun getToolbarHeight(context: Context, isLandscape: Boolean, isTablet: Boolean): Int {
        var actionBarHeight = 0f

        if (isTablet) {
            val tv = TypedValue()
            if (context.theme.resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
                actionBarHeight = TypedValue.complexToDimensionPixelSize(
                    tv.data,
                    context.resources.displayMetrics
                ).toFloat()
            }
        } else {
            val scale = Resources.getSystem().displayMetrics.density
            actionBarHeight = if (!isLandscape) 56 * scale else 48 * scale
        }

        return actionBarHeight.toInt()
    }

    public fun getNavigationBarHeight(context: Context): Int {
        val resources: Resources = context.getResources()
        val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
        return if (resourceId > 0) resources.getDimensionPixelSize(resourceId) else 0
    }

    public fun isNotchDisplay(statusBarHeight: Int): Boolean {
        return statusBarHeight > 24 * Resources.getSystem().displayMetrics.density
    }


}