package com.fluidtouch.dynamicgeneration.templateformatsnew;

import android.content.Context;
import android.util.Log;

import com.fluidtouch.dynamicgeneration.inteface.TemplatesGeneratorInterface;
import com.fluidtouch.noteshelf.documentframework.Utilities.FTConstants;
import com.fluidtouch.noteshelf.models.theme.FTNDynamicTemplateTheme;
import com.fluidtouch.noteshelf.templatepicker.common.util.FTTemplateMoreDetailsInfo;
import com.tom_roush.pdfbox.multipdf.Overlay;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.pdmodel.PDPage;
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream;
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class FTContentStretchPDF extends FTDynamicTemplateFormat implements TemplatesGeneratorInterface {

    public FTContentStretchPDF(FTNDynamicTemplateTheme theme) {
        super(theme);
    }

    @Override
    public String generateTemplate(FTNDynamicTemplateTheme ftnTheme, Context mContext) {

        String pdfsFilePath = null;

        Log.d("TemplatePicker==>","Device Size::- FTContentStretchPDF generateTemplate::-" +
                " mPageWidth::-"+mPageWidth+
                " mPageHeight::-"+mPageHeight);

        Log.d("TemplatePicker==>"," FTContentStretchPDF generateTemplate mPageWidth::-"+mPageWidth+" mPageHeight::-"+mPageHeight);
        PDPage importedPDFPage;
        PDDocument importedPDFDocument = null;

        try {

            PDDocument baseDocument = new PDDocument();
            PDPage basePDFPage = new PDPage();
            PDRectangle mRectangle = new PDRectangle(mPageWidth,mPageHeight);
            basePDFPage.setMediaBox(mRectangle);
            baseDocument.addPage(basePDFPage);

            FTTemplateMoreDetailsInfo ftTemplateMoreDetailsInfo = new FTTemplateMoreDetailsInfo();
            bgClrRGB = ftTemplateMoreDetailsInfo.getRGBValue(ftnTheme.themeBgClr);
            PDPageContentStream contentStream = new PDPageContentStream(baseDocument, basePDFPage);

            contentStream.addRect(0, 0, mPageWidth, mPageHeight);
            contentStream.setNonStrokingColor(themeBgRedClrValue,themeBgGreenClrValue,themeBgBlueClrValue);
            contentStream.fill();
            contentStream.close();

            File template_output_file = null;

            File rootFile = new File(basePDFPath);
            if (!rootFile.exists()) {
                rootFile.mkdirs();
            }

            File template_basePDF_file = new File(basePDFPath + "/template_basePDF_file.pdf");
            baseDocument.save(template_basePDF_file);
            Log.d("TemplatePicker==>"," FTContentStretchPDF DOWNLOADED_PAPERS_PATH2 ftnTheme.isLandscape::-"+ftnTheme.isLandscape);
            if (ftnTheme.isDownloadTheme) {
                if (!ftnTheme.isLandscape) {
                    File file = new File((FTConstants.DOWNLOADED_PAPERS_PATH2) + ftnTheme.packName + "/template_port.pdf");
                    importedPDFDocument = PDDocument.load(file);
                } else {
                    File file = new File((FTConstants.DOWNLOADED_PAPERS_PATH2) + ftnTheme.packName + "/template_land.pdf");
                    importedPDFDocument = PDDocument.load(file);
                }
            } else if (ftnTheme.isCustomTheme) {
                //TODO
            } else {
                if (!ftnTheme.isLandscape) {
                    importedPDFDocument = PDDocument.load(mContext.getResources().getAssets().open(
                            FTConstants.PAPER_FOLDER_NAME + "/" + ftnTheme.packName + "/"+"template_port.pdf"));
                } else {
                    importedPDFDocument = PDDocument.load(mContext.getResources().getAssets().open(
                            FTConstants.PAPER_FOLDER_NAME + "/" + ftnTheme.packName + "/"+"template_land.pdf"));
                }
            }

            File template_modified_file = new File(basePDFPath+"/template_modified_file.pdf");
            importedPDFDocument.save(template_modified_file);
            template_output_file = new File(basePDFPath+"/template_output_file.pdf");

            overlayDocuments(template_basePDF_file,template_modified_file,template_output_file);

            pdfsFilePath = template_output_file.getAbsolutePath();

            //String themeNameTrunc = ftnTheme.packName.substring(0, ftnTheme.packName.lastIndexOf("."));
            /*pdfToBitmap(ftnTheme,new File(pdfsFilePath), themeNameTrunc, tempPagesPath,
                    ftTemplateColorsInfo.getColorName(),
                    ftLineTypesInfo.getLineType(), mContext);*/

        } catch (IOException e) {
            e.printStackTrace();
        }

        return pdfsFilePath;
    }

    public static void overlayDocuments(File contentFile, File templateFile, File outputFile) {
        HashMap<Integer, String> overlayGuide = new HashMap<>();
        Overlay overlay = new Overlay();
        Log.e("TemplatePicker==>"," contentFile::-"+contentFile+" templateFile::-"+" outputFile::-"+outputFile);
        Log.e("TemplatePicker==>"," contentFile exists::-"+contentFile.exists()+" templateFile exists::-"
                +templateFile.exists()+" outputFile exists::-"+outputFile.exists());
        try {
            overlay.setInputFile(contentFile.getAbsolutePath());
            overlay.setAllPagesOverlayFile(templateFile.getAbsolutePath());
            overlay.setOutputFile(outputFile.getAbsolutePath());
            overlay.setOverlayPosition(Overlay.Position.FOREGROUND);
            overlay.overlay(overlayGuide);
        } catch (IOException e) {
            e.getStackTrace();
        }
    }
}
