package com.fluidtouch.dynamicgeneration.templateformats

import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.fluidtouch.dynamicgeneration.FTDynamicTemplateFormat
import com.fluidtouch.dynamicgeneration.FTDynamicTemplateInfo

class FTRuledTemplateFormat(templateInfo: FTDynamicTemplateInfo) /*: FTDynamicTemplateFormat()*/ {
    /*init {
        super.templateInfo = templateInfo
    }

    override fun renderTemplate(document: PdfDocument) {
        super.renderTemplate(document)

        val horizLineCount = horizontalLineCount()
        var yPos = templateInfo!!.height - templateInfo!!.codableInfo.bottomMargin

        //Drawing for horizontal lines
        currentPage?.let {
            val canvas = it.canvas
            val paint = Paint()
            paint.strokeWidth = 1f * scale
            paint.color = ColorUtil.getColor(this.templateInfo!!.codableInfo.horizontalLineColor)

            for (i in 0..horizLineCount) {
                canvas.drawLine(0.toFloat(), yPos.toFloat(), templateInfo!!.width.toFloat(), yPos.toFloat(), paint)
                yPos -= templateInfo!!.codableInfo.horizontalSpacing + 1
            }

            document.finishPage(currentPage)
        }
    }

    override fun horizontalLineCount(): Int {
        val cellHeight = templateInfo!!.codableInfo.horizontalSpacing + 1
        val consideredPageHeight = templateInfo!!.height - templateInfo!!.codableInfo.bottomMargin
        val actualCount = (consideredPageHeight / cellHeight).toInt()
        val difference = consideredPageHeight - (actualCount * cellHeight)
        return if (difference > cellHeight - 3) actualCount else actualCount - 1
    }*/
}