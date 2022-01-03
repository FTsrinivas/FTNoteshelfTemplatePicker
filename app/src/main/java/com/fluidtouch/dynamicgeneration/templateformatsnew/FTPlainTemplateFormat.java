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

public class FTPlainTemplateFormat extends FTDynamicTemplateFormat implements TemplatesGeneratorInterface {

   public FTPlainTemplateFormat(FTNDynamicTemplateTheme theme) {
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
            //contentStream.setNonStrokingColor(bgClrRGB.getRedClrValue(),bgClrRGB.getGreenClrValue(),bgClrRGB.getBlueClrValue());
            Log.d("TemplatePicker==>","FTDynamicTemplateFormat FTPlainTemplateFormat " +
                    "getThemeBgClrHexCode::-"+themeBgRedClrValue+
                    " themeBgGreenClrValue::-"+themeBgGreenClrValue+
                    " themeBgBlueClrValue::-"+themeBgBlueClrValue+
                    " mPageWidth::-"+mPageWidth+
                    " mPageHeight::-"+mPageHeight+
                    " getMediaBox_getWidth::-"+page.getMediaBox().getWidth()+
                    " getHeight::-"+page.getMediaBox().getHeight());

            contentStream.setNonStrokingColor(themeBgRedClrValue,themeBgGreenClrValue,themeBgBlueClrValue);
            contentStream.fill();

            contentStream.close();

            File pdfsFile = new File(tempPagesPath);
            if (!pdfsFile.exists()) {
                pdfsFile.mkdir();
            }

            pdfsFilePath = tempPagesPath + "Template_Plain.pdf";

            doc.save(pdfsFilePath);
            doc.close();

            /*if (!checkImageinCache(theme.thumbnailURLPath)) {
                pdfToBitmap(theme,new File(pdfsFilePath), "Plain",
                        tempPagesPath,
                        ftTemplateColorsInfo.getColorName(),
                        ftLineTypesInfo.getLineType(),mContext);
            }*/

        } catch (IOException e) {
            e.printStackTrace();
        }
        return pdfsFilePath;
    }
}
