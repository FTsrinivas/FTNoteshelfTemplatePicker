package com.fluidtouch.dynamicgeneration.templateformats

import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.fluidtouch.dynamicgeneration.FTDynamicTemplateFormat
import com.fluidtouch.dynamicgeneration.FTDynamicTemplateInfo
import com.fluidtouch.dynamicgeneration.enums.FTSupportingDeviceType

class FTCheckedTemplateFormat(templateInfo: FTDynamicTemplateInfo) /*: FTDynamicTemplateFormat()*/ {

    /*init {
        super.templateInfo = templateInfo
    }

    override fun renderTemplate(document: PdfDocument) {
        super.renderTemplate(document)

        val horizLineCount = horizontalLineCount()
        val vertLineCount = verticalLineCount()
        var yPos = templateInfo!!.height - templateInfo!!.codableInfo.bottomMargin
        var xPos = getXPos(vertLineCount)

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

            // This is limit the vertical line in phone templates
            if (this.templateInfo!!.codableInfo.supportingDeviceType == FTSupportingDeviceType.Phone.ordinal) {
                yPos += templateInfo!!.codableInfo.horizontalSpacing + 1
            } else {
                yPos = 0.toFloat()
            }

            for (i in 0..vertLineCount) {
                canvas.drawLine(xPos.toFloat(), yPos.toFloat(), xPos.toFloat(), (templateInfo!!.height - templateInfo!!.codableInfo.bottomMargin).toFloat(), paint)
                xPos += this.templateInfo!!.codableInfo.verticalSpacing + 1
            }

            document.finishPage(currentPage)
        }
    }

    override fun horizontalLineCount(): Int {
        val cellHeight = templateInfo!!.codableInfo.horizontalSpacing + 1
        val consideredPageHeight = templateInfo!!.height - templateInfo!!.codableInfo.bottomMargin
        val actualCount = (consideredPageHeight / cellHeight).toInt()
        val difference = consideredPageHeight - (actualCount * cellHeight)
        return if (difference >= cellHeight - 6) actualCount else actualCount - 1
    }

    override fun verticalLineCount(): Int {
        val cellWidth = templateInfo!!.codableInfo.verticalSpacing + 1
        val actualCount = templateInfo!!.width / cellWidth
        return actualCount.toInt() - 1
    }

    private fun getXPos(lineCount: Int): Float {
        val cellWidth = templateInfo!!.codableInfo.verticalSpacing + 1
        return ((templateInfo!!.width - ((lineCount) * (cellWidth))) / 2).toFloat()
    }*/
}