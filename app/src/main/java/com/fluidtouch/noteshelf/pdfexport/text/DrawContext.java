package com.fluidtouch.noteshelf.pdfexport.text;

import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.pdmodel.PDPage;
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream;

/**
 * Provides the current page and document to draw to.
 */
public interface DrawContext {

    /**
     * @return the document to draw to.
     */
    public PDDocument getPdDocument();

    /**
     * @return the current page to draw to.
     */
    public PDPage getCurrentPage();

    /**
     * @return the current page content stream.
     */
    public PDPageContentStream getCurrentPageContentStream();
}
