package com.fluidtouch.dynamicgeneration.templateformats

import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import com.fluidtouch.dynamicgeneration.FTDynamicTemplateFormat
import com.fluidtouch.dynamicgeneration.FTDynamicTemplateInfo

class FTDottedTemplateFormat(templateInfo: FTDynamicTemplateInfo) /*: FTDynamicTemplateFormat()*/ {

   /* init {
        super.templateInfo = templateInfo
    }

    override fun renderTemplate(document: PdfDocument) {
        super.renderTemplate(document)

        val horizLineCount = horizontalLineCount()
        val vertLineCount = verticalLineCount()
        val ovalSize = 3 * scale
        val yPos = getYPos(lineCount = horizLineCount) + (templateInfo!!.codableInfo.verticalSpacing / 2)
        val xPos = getXPos(lineCount = vertLineCount) - (ovalSize / 2)

        var dotRect = RectF(xPos, yPos, xPos + ovalSize, yPos + ovalSize)

        currentPage?.let {
            val canvas = it.canvas
            val paint = Paint()
            paint.strokeWidth = 1f * scale
            paint.color = ColorUtil.getColor(this.templateInfo!!.codableInfo.horizontalLineColor)


            for (i in 0..horizLineCount) {
                for (j in 0..vertLineCount) {
                    canvas.drawOval(dotRect, paint)
                    dotRect.offset(templateInfo!!.codableInfo.verticalSpacing + ovalSize, 0.toFloat())
                }
                dotRect.offsetTo(xPos, dotRect.top)
                dotRect.offset(0.toFloat(), templateInfo!!.codableInfo.horizontalSpacing + ovalSize)
            }

            document.finishPage(currentPage)
        }
    }

    override fun horizontalLineCount(): Int {
        val cellHeight = templateInfo!!.codableInfo.horizontalSpacing + 3 * scale
        val consideredHeight = templateInfo!!.height - templateInfo!!.codableInfo.bottomMargin
        val actualCount = (consideredHeight / cellHeight).toInt()
        return actualCount - 1
    }

    override fun verticalLineCount(): Int {
        val cellWidth = templateInfo!!.codableInfo.verticalSpacing + 3 * scale
        val actualCount = (templateInfo!!.width / cellWidth).toInt()
        return actualCount - 1
    }

    private fun getXPos(lineCount: Int): Float {
        val cellWidth = templateInfo!!.codableInfo.verticalSpacing + 3 * scale
        return ((templateInfo!!.width - ((lineCount) * (cellWidth.toInt()))) / 2).toFloat()
    }

    private fun getYPos(lineCount: Int): Float {
        val cellHeight = templateInfo!!.codableInfo.horizontalSpacing + 3 * scale
        val consideredHeight = templateInfo!!.height - templateInfo!!.codableInfo.bottomMargin
        return ((consideredHeight.toInt() - ((lineCount) * (cellHeight.toInt()))) / 2).toFloat()
    }*/
}
