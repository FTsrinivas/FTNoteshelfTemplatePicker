package com.fluidtouch.dynamicgeneration.templateformats

import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.fluidtouch.dynamicgeneration.FTDynamicTemplateFormat
import com.fluidtouch.dynamicgeneration.FTDynamicTemplateInfo
import com.fluidtouch.noteshelf.commons.utils.ScreenUtil

class FTLegalTemplateFormat(templateInfo: FTDynamicTemplateInfo) /*: FTDynamicTemplateFormat()*/ {

    /*init {
        super.templateInfo = templateInfo
    }

    override fun renderTemplate(document: PdfDocument) {
        super.renderTemplate(document)

        val horizLineCount = horizontalLineCount()
        var xPos = templateInfo!!.codableInfo.leftMargin + 1
        if (!ScreenUtil.isTablet()) {
            xPos -= 40 * scale
        }
        var yPos = templateInfo!!.height - templateInfo!!.codableInfo.bottomMargin

        //Drawing for horizontal lines
        currentPage?.let {
            val canvas = it.canvas
            val paint = Paint()
            paint.strokeWidth = 1f * scale
            paint.color = ColorUtil.getColor(this.templateInfo!!.codableInfo.horizontalLineColor)

            for (i in 0..horizLineCount - 2) {
                canvas.drawLine(0.toFloat(), yPos, templateInfo!!.width.toFloat(), yPos, paint)
                yPos -= templateInfo!!.codableInfo.horizontalSpacing + 1
            }

            //Drawing for vertical lines
            paint.color = ColorUtil.getColor(this.templateInfo!!.codableInfo.verticalLineColor)

            for (i in 0..1) {
                canvas.drawLine(xPos, 0.toFloat(), xPos, templateInfo!!.height - templateInfo!!.codableInfo.bottomMargin, paint)
                xPos += templateInfo!!.codableInfo.verticalSpacing + 1
            }

            document.finishPage(currentPage)
        }
    }

    override fun horizontalLineCount(): Int {
        val cellHeight = templateInfo!!.codableInfo.horizontalSpacing + 1
        val consideredPageHeight = templateInfo!!.height - templateInfo!!.codableInfo.bottomMargin
        val actualCount = (consideredPageHeight / cellHeight).toInt()
        return actualCount
    }*/
}