package com.fluidtouch.dynamicgeneration.templateformatsnew;

import android.content.Context;
import android.util.Log;

import com.fluidtouch.dynamicgeneration.inteface.TemplatesGeneratorInterface;
import com.fluidtouch.noteshelf.models.theme.FTNDynamicTemplateTheme;
import com.tom_roush.harmony.awt.AWTColor;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.pdmodel.PDPage;
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream;
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle;

import java.io.File;
import java.io.IOException;

public class FTRuledTemplateFormat extends FTDynamicTemplateFormat implements TemplatesGeneratorInterface {

    public FTRuledTemplateFormat(FTNDynamicTemplateTheme theme) {
        super(theme);
    }

    @Override
    public String generateTemplate(FTNDynamicTemplateTheme theme, Context mContext) {
        String pdfsFilePath = null;
        //Landscape values W_H 137 *94 portrait 136_170

        try {

            PDDocument doc = new PDDocument();
            PDPage page = new PDPage();
            Log.d("FTDynamicTemplateFormat==> ","mPageWidth::- "+mPageWidth+"  mPageHeight::- "+mPageHeight);
            PDRectangle mRectangle = new PDRectangle(mPageWidth,mPageHeight);
//            PDRectangle mRectangle = new PDRectangle(272,340);
//            PDRectangle mRectangle = new PDRectangle(137,94);
            page.setMediaBox(mRectangle);
            doc.addPage(page);

            PDPageContentStream contentStream = new PDPageContentStream(doc, page);
            contentStream.addRect(0, 0, page.getMediaBox().getWidth(), page.getMediaBox().getHeight());
            Log.d("TemplatePicker==>","Line Selected FTRuledTemplateFormat getRedClrValue::-"+themeBgRedClrValue+
                    " getGreenClrValue::-"+ themeBgGreenClrValue+" getBlueClrValue::-"+themeBgBlueClrValue+" themeBgClr::-"+theme.themeBgClr);
            contentStream.setNonStrokingColor(themeBgRedClrValue,themeBgGreenClrValue,themeBgBlueClrValue);
            contentStream.fill();

            float yCordinate = page.getCropBox().getHeight()-(mTheme.bottomMargin*scale);
            float startX = page.getCropBox().getLowerLeftX();;
            float endX = page.getCropBox().getUpperRightX();

            float offset = Math.round((page.getCropBox().getHeight() / mPageHeight) * horizontalSpacing);


            Log.d("TemplatePicker==>","Line Selected FTRuledTemplateFormat getRedClrValue::-"+horizontalLineRedClrValue+
                    " getGreenClrValue::-"+ horizontalLineGreenClrValue+" getBlueClrValue::-"+horizontalLineBlueClrValue);
            /*
             * Code snippet to draw horizontal lines
             */
            for (int i= horizontalLineCount(); i >0; i--) {
                contentStream.setStrokingColor(new AWTColor(horizontalLineRedClrValue,
                        horizontalLineGreenClrValue,horizontalLineBlueClrValue));
                contentStream.setLineWidth(2);
//                contentStream.setLineCapStyle(1);
//                contentStream.setLineJoinStyle(1);
                contentStream.moveTo(startX, yCordinate);
                contentStream.lineTo(endX, yCordinate);

                /*contentStream.setStrokingColor(horizontalLineRedClrValue,
                        horizontalLineGreenClrValue,horizontalLineBlueClrValue);*/
                contentStream.stroke();
                yCordinate -= offset;
            }

            contentStream.close();

            File pdfsFile = new File(tempPagesPath);
            if (!pdfsFile.exists()) {
                pdfsFile.mkdir();
            }

            pdfsFilePath = tempPagesPath + "Template_Ruled.pdf";
            doc.save(pdfsFilePath);
            doc.close();


        } catch (IOException e) {
            e.printStackTrace();
        }
        return pdfsFilePath;
    }

    private int horizontalLineCount() {
        int horizontalLineCount;
        float cellHeight = Math.round(horizontalSpacing/5);
        float consideredPageHeight = (mPageHeight /*- (mTheme.bottomMargin * scale)*/);
        int actualCount = (int) Math.round((consideredPageHeight / cellHeight));
        float effectiveHeight = mPageHeight - (mTheme.bottomMargin*scale) - (actualCount * cellHeight);
        if (2 * cellHeight - effectiveHeight >= cellHeight) {
            actualCount++;
        }
        return actualCount;
    }

}