package com.fluidtouch.dynamicgeneration.templateformatsnew;

import android.content.Context;

import com.fluidtouch.dynamicgeneration.inteface.TemplatesGeneratorInterface;
import com.fluidtouch.noteshelf.models.theme.FTNDynamicTemplateTheme;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.pdmodel.PDPage;
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream;
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle;

import java.io.File;
import java.io.IOException;

public class FTLegalTemplateFormat extends FTDynamicTemplateFormat implements TemplatesGeneratorInterface {

    public FTLegalTemplateFormat(FTNDynamicTemplateTheme theme) {
        super(theme);
    }

    @Override
    public String generateTemplate(FTNDynamicTemplateTheme theme, Context mContext) {
        String pdfsFilePath = null;

        float verticalSpacing   = 24.0f;

        try {

            PDDocument doc = new PDDocument();
            PDPage page = new PDPage();
            PDRectangle mRectangle = new PDRectangle(mPageWidth,mPageHeight);
            page.setMediaBox(mRectangle);
            doc.addPage(page);

            PDPageContentStream contentStream = new PDPageContentStream(doc, page);
            contentStream.addRect(0, 0, page.getMediaBox().getWidth(), page.getMediaBox().getHeight());
            contentStream.setNonStrokingColor(themeBgRedClrValue,themeBgGreenClrValue,themeBgBlueClrValue);
            contentStream.fill();

            float yCordinate = mTheme.bottomMargin * scale;
            float startX = page.getCropBox().getLowerLeftX();;
            float endX = mPageWidth;

            /*
             * Code snippet to draw horizontal lines
             */
            for (int i= 0; i < horizontalLineCount() + 2 ; i++) {
                contentStream.setLineWidth(3);
                contentStream.setLineCapStyle(1);
                contentStream.setLineJoinStyle(1);
                contentStream.moveTo(startX, yCordinate);
                contentStream.lineTo(endX, yCordinate);

                contentStream.setStrokingColor(horizontalLineRedClrValue,
                        horizontalLineGreenClrValue,horizontalLineBlueClrValue);
                contentStream.stroke();
                yCordinate += horizontalSpacing + 3;
            }

            /*
             * Code snippet to draw Vertical  lines
             */

            float getLowerLeftY  = 0.0f;
            float getLowerLeftX  = 0.0f;
            float getUpperRightX = 342.5f;
            float getUpperRightY = mPageHeight;

            for (int i= 0; i < 2; i++) {
                contentStream.moveTo(getLowerLeftX, getLowerLeftY);
                contentStream.lineTo(getLowerLeftX, getUpperRightY);
                contentStream.setStrokingColor(verticalLineRedClrValue,
                        verticalLineGreenClrValue,verticalLineBlueClrValue);
                contentStream.stroke();

                getLowerLeftX  += verticalSpacing+35;
                getUpperRightX += verticalSpacing+35;
                getUpperRightY += verticalSpacing+35;
            }

            /*code snippet started for second line*/
            contentStream.moveTo(getLowerLeftX-62, getLowerLeftY);
            contentStream.lineTo(getLowerLeftX-62, getUpperRightY-62);
            contentStream.setStrokingColor(verticalLineRedClrValue,
                    verticalLineGreenClrValue,verticalLineBlueClrValue);
            contentStream.stroke();
            /*code snippet ended for second line*/

            contentStream.close();

            File pdfsFile = new File(tempPagesPath);
            if (!pdfsFile.exists()) {
                pdfsFile.mkdir();
            }

            pdfsFilePath = tempPagesPath + "Template_Legal.pdf";

            doc.save(pdfsFilePath);
            doc.close();

            /*if (!checkImageinCache(theme.thumbnailURLPath)) {
                pdfToBitmap(theme,new File(pdfsFilePath), "Legal",tempPagesPath,
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
        float cellHeight = horizontalSpacing + 3;
        float consideredPageHeight = (mPageHeight - (mTheme.bottomMargin * scale));
        int actualCount = (int) Math.floor((consideredPageHeight / cellHeight));
        float effectiveHeight = mPageHeight - (mTheme.bottomMargin*scale) - (actualCount * cellHeight);
        if (2 * cellHeight - effectiveHeight >= 10) {
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