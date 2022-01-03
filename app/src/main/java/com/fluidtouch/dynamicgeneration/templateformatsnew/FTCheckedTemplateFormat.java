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

public class FTCheckedTemplateFormat extends FTDynamicTemplateFormat implements TemplatesGeneratorInterface {

    public FTCheckedTemplateFormat(FTNDynamicTemplateTheme theme) {
        super(theme);
    }

    @Override
    public String generateTemplate(FTNDynamicTemplateTheme theme, Context mContext) {
        String pdfsFilePath = null;

        try {

            /*Log.d("::TemplatePickerV2","Device Size::- FTCheckedTemplateFormat generateTemplate::-" +
                    " mTheme.width::-"+mTheme.width+
                    " mTheme.height::-"+mTheme.height+
                    " mPageWidth::-"+mPageWidth+
                    " mPageHeight::-"+mPageHeight);*/

            PDDocument doc = new PDDocument();
            PDPage page = new PDPage();
            PDRectangle mRectangle = new PDRectangle(mPageWidth, mPageHeight);
            page.setMediaBox(mRectangle);
            doc.addPage(page);

            PDPageContentStream contentStream = new PDPageContentStream(doc, page);
            contentStream.addRect(0, 0, page.getMediaBox().getWidth(), page.getMediaBox().getHeight());
            contentStream.setNonStrokingColor(themeBgRedClrValue,themeBgGreenClrValue ,themeBgBlueClrValue );
            contentStream.fill();

//            float yCordinate = mTheme.bottomMargin * scale;
            float yCordinate = mPageHeight-1 * scale;
            float startX = page.getCropBox().getLowerLeftX();;
            float endX = mPageWidth;

            /*
             * Code snippet to draw horizontal lines
             */
            for (int i= horizontalLineCount(); i >0; i--) {
                contentStream.setLineWidth(3);
                contentStream.setLineCapStyle(1);
                contentStream.setLineJoinStyle(1);
                contentStream.moveTo(startX, yCordinate);
                contentStream.lineTo(endX, yCordinate);

                contentStream.setStrokingColor(horizontalLineRedClrValue,
                        horizontalLineGreenClrValue,horizontalLineBlueClrValue);
                contentStream.stroke();
                yCordinate -= horizontalSpacing+3;
            }

            /*
             * Code snippet to draw Vertical  lines
             */
            float getLowerLeftY  = page.getCropBox().getLowerLeftY();
            float getLowerLeftX  = page.getCropBox().getLowerLeftX();
            float getUpperRightX = page.getCropBox().getUpperRightX();
            float getUpperRightY = page.getCropBox().getUpperRightY();

            for (int i = 0; i < verticalLineCount() +1; i++) {
                contentStream.setLineWidth(3);
                contentStream.moveTo(getLowerLeftX, getLowerLeftY);
                contentStream.lineTo(getLowerLeftX, getUpperRightY);
                contentStream.setStrokingColor(verticalLineRedClrValue,
                        verticalLineGreenClrValue, verticalLineBlueClrValue);

                contentStream.stroke();

                getLowerLeftX  += verticalSpacing + 3;
                getUpperRightX += verticalSpacing + 3;
                getUpperRightY += verticalSpacing + 3;
            }

            contentStream.close();

            File pdfsFile = new File(tempPagesPath);
            if (!pdfsFile.exists()) {
                pdfsFile.mkdir();
            }

            pdfsFilePath = tempPagesPath + "Template_Checked.pdf";

            doc.save(pdfsFilePath);
            doc.close();

            /*if (!checkImageinCache(theme.thumbnailURLPath)) {
                pdfToBitmap(theme,new File(pdfsFilePath), "Checked", tempPagesPath,
                        ftTemplateColorsInfo.getColorName(),
                        ftLineTypesInfo.getLineType(), mContext);
            }*/

        } catch (IOException e) {
            e.printStackTrace();
        }
        return pdfsFilePath;
    }

    private int horizontalLineCount() {
        int horizontalLineCount;
        float cellHeight = horizontalSpacing + 3;
        float consideredPageHeight = (mPageHeight /*- (mTheme.bottomMargin * scale)*/);
        int actualCount = (int) Math.floor((consideredPageHeight / cellHeight));
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