package com.fluidtouch.dynamicgeneration.templateformatsnew;

import android.content.Context;
import android.util.Log;

import com.fluidtouch.dynamicgeneration.inteface.TemplatesGeneratorInterface;
import com.fluidtouch.noteshelf.models.theme.FTNDynamicTemplateTheme;
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

        try {

            PDDocument doc = new PDDocument();
            PDPage page = new PDPage();
            PDRectangle mRectangle = new PDRectangle(mPageWidth,mPageHeight);
            page.setMediaBox(mRectangle);
            doc.addPage(page);

            PDPageContentStream contentStream = new PDPageContentStream(doc, page);
            contentStream.addRect(0, 0, page.getMediaBox().getWidth(), page.getMediaBox().getHeight());
            Log.d("TemplatePicker==>","Line Selected FTRuledTemplateFormat getRedClrValue::-"+themeBgRedClrValue+
                    " getGreenClrValue::-"+ themeBgGreenClrValue+" getBlueClrValue::-"+themeBgBlueClrValue+" themeBgClr::-"+theme.themeBgClr);
            contentStream.setNonStrokingColor(themeBgRedClrValue,themeBgGreenClrValue,themeBgBlueClrValue);
            contentStream.fill();

            float yCordinate = mPageHeight-1*scale ;
            float startX = page.getCropBox().getLowerLeftX();;
            float endX = mPageWidth;

            Log.d("TemplatePicker==>","Line Selected FTRuledTemplateFormat getRedClrValue::-"+horizontalLineRedClrValue+
                    " getGreenClrValue::-"+ horizontalLineGreenClrValue+" getBlueClrValue::-"+horizontalLineBlueClrValue);
            /*
             * Code snippet to draw horizontal lines
             */
            for (int i= horizontalLineCount(); i >0; i--) {
                contentStream.setLineWidth(1);
                contentStream.setLineCapStyle(1);
                contentStream.setLineJoinStyle(1);
                contentStream.moveTo(startX, yCordinate);
                contentStream.lineTo(endX, yCordinate);

                contentStream.setStrokingColor(horizontalLineRedClrValue,
                        horizontalLineGreenClrValue,horizontalLineBlueClrValue);
                contentStream.stroke();
                yCordinate -= horizontalSpacing+1;
            }

            contentStream.close();

            File pdfsFile = new File(tempPagesPath);
            if (!pdfsFile.exists()) {
                pdfsFile.mkdir();
            }

            pdfsFilePath = tempPagesPath + "Template_Ruled.pdf";
            doc.save(pdfsFilePath);
            doc.close();

            //TODO:
            // comment below code
            /*if (!checkImageinCache(theme.thumbnailURLPath)) {
                pdfToBitmap(theme,new File(pdfsFilePath), "Ruled",tempPagesPath,
                        ftTemplateColorsInfo.getColorName(),
                        ftLineTypesInfo.getLineType(),mContext);
            }*/

        } catch (IOException e) {
            e.printStackTrace();
        }
        return pdfsFilePath;
    }

    private int horizontalLineCount() {
        int horizontalLineCount;
        float cellHeight = horizontalSpacing + 1;
        float consideredPageHeight = (mPageHeight /*- (mTheme.bottomMargin * scale)*/);
        int actualCount = (int) Math.round((consideredPageHeight / cellHeight));
        float effectiveHeight = mPageHeight - (mTheme.bottomMargin*scale) - (actualCount * cellHeight);
        if (2 * cellHeight - effectiveHeight >= cellHeight) {
            actualCount++;
        }
        return actualCount;
    }

    private int verticalLineCount() {
        int verticalLineCount;
        float cellWidth = verticalSpacing + 3;
        float consideredPageWidth = mPageWidth;
        int actualCount = (int) Math.floor((consideredPageWidth / cellWidth));
        verticalLineCount = actualCount - 1;
        return verticalLineCount;
    }
}