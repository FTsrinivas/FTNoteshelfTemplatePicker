package com.fluidtouch.noteshelf.pdfexport.text.annotations;

import com.fluidtouch.noteshelf.pdfexport.text.DrawContext;
import com.fluidtouch.noteshelf.pdfexport.text.Position;
import com.fluidtouch.noteshelf.pdfexport.util.CompatibilityHelper;
import com.tom_roush.harmony.awt.AWTColor;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.pdmodel.PDPage;
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle;
import com.tom_roush.pdfbox.pdmodel.interactive.annotation.PDAnnotationLink;
import com.tom_roush.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageXYZDestination;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * This annotation processor handles both {@link Annotations.HyperlinkAnnotation}s and
 * {@link Annotations.AnchorAnnotation}s, and adds the needed hyperlink metadata to the PDF
 * document.
 */
public class HyperlinkAnnotationProcessor implements AnnotationProcessor {

    private Map<String, PageAnchor> anchorMap = new HashMap<String, PageAnchor>();
    private Map<PDPage, List<Hyperlink>> linkMap = new HashMap<PDPage, List<Hyperlink>>();

    @Override
    public void annotatedObjectDrawn(Annotated drawnObject,
                                     DrawContext drawContext, Position upperLeft, float width,
                                     float height) throws IOException {

        if (!(drawnObject instanceof AnnotatedStyledText)) {
            return;
        }
        AnnotatedStyledText annotatedText = (AnnotatedStyledText) drawnObject;
        handleHyperlinkAnnotations(annotatedText, drawContext, upperLeft,
                width, height);
        handleAnchorAnnotations(annotatedText, drawContext, upperLeft);
    }

    protected void handleAnchorAnnotations(AnnotatedStyledText annotatedText,
                                           DrawContext drawContext, Position upperLeft) {
        Iterable<Annotations.AnchorAnnotation> anchorAnnotations = annotatedText
                .getAnnotationsOfType(Annotations.AnchorAnnotation.class);
        for (Annotations.AnchorAnnotation anchorAnnotation : anchorAnnotations) {
            anchorMap.put(
                    anchorAnnotation.getAnchor(),
                    new PageAnchor(drawContext.getCurrentPage(), upperLeft
                            .getX(), upperLeft.getY()));
        }
    }

    protected void handleHyperlinkAnnotations(
            AnnotatedStyledText annotatedText, DrawContext drawContext,
            Position upperLeft, float width, float height) {
        Iterable<Annotations.HyperlinkAnnotation> hyperlinkAnnotations = annotatedText
                .getAnnotationsOfType(Annotations.HyperlinkAnnotation.class);
        for (Annotations.HyperlinkAnnotation hyperlinkAnnotation : hyperlinkAnnotations) {
            List<Hyperlink> links = linkMap.get(drawContext.getCurrentPage());
            if (links == null) {
                links = new ArrayList<Hyperlink>();
                linkMap.put(drawContext.getCurrentPage(), links);
            }
            PDRectangle bounds = new PDRectangle();
            bounds.setLowerLeftX(upperLeft.getX());
            bounds.setLowerLeftY(upperLeft.getY() - height);
            bounds.setUpperRightX(upperLeft.getX() + width);
            bounds.setUpperRightY(upperLeft.getY());

            links.add(new Hyperlink(bounds, annotatedText.getColor(),
                    hyperlinkAnnotation.getLinkStyle(), hyperlinkAnnotation
                    .getHyperlinkURI()));
        }
    }

    @Override
    public void beforePage(DrawContext drawContext) {
        // nothing to do here
    }

    @Override
    public void afterPage(DrawContext drawContext) {
        // nothing to do here
    }

    @Override
    public void afterRender(PDDocument document) throws IOException {
        for (Entry<PDPage, List<Hyperlink>> entry : linkMap.entrySet()) {
            PDPage page = entry.getKey();
            List<Hyperlink> links = entry.getValue();
            for (Hyperlink hyperlink : links) {
                PDAnnotationLink pdLink = null;
                if (hyperlink.getHyperlinkURI().startsWith("#")) {
                    pdLink = createGotoLink(hyperlink);
                } else {
                    pdLink = CompatibilityHelper.createLink(page,
                            hyperlink.getRect(), hyperlink.getColor(),
                            hyperlink.getLinkStyle(),
                            hyperlink.getHyperlinkURI());
                }
                page.getAnnotations().add(pdLink);
            }

        }
    }

    private PDAnnotationLink createGotoLink(Hyperlink hyperlink) {
        String anchor = hyperlink.getHyperlinkURI().substring(1);
        PageAnchor pageAnchor = anchorMap.get(anchor);
        if (pageAnchor == null) {
            throw new IllegalArgumentException(String.format(
                    "anchor named '%s' not found", anchor));
        }
        PDPageXYZDestination xyzDestination = new PDPageXYZDestination();
        xyzDestination.setPage(pageAnchor.getPage());
        xyzDestination.setLeft((int) pageAnchor.getX());
        xyzDestination.setTop((int) pageAnchor.getY());
        return CompatibilityHelper.createLink(pageAnchor.getPage(), hyperlink.getRect(),
                hyperlink.getColor(), hyperlink.getLinkStyle(), xyzDestination);
    }

    private static class PageAnchor {
        private final PDPage page;
        private final float x;
        private final float y;

        public PageAnchor(PDPage page, float x, float y) {
            this.page = page;
            this.x = x;
            this.y = y;
        }

        public PDPage getPage() {
            return page;
        }

        public float getX() {
            return x;
        }

        public float getY() {
            return y;
        }

        @Override
        public String toString() {
            return "PageAnchor [page=" + page + ", x=" + x + ", y=" + y + "]";
        }

    }

    private static class Hyperlink {
        private final PDRectangle rect;
        private final AWTColor color;
        private final String hyperlinkUri;
        private final Annotations.HyperlinkAnnotation.LinkStyle linkStyle;

        public Hyperlink(PDRectangle rect, AWTColor color, Annotations.HyperlinkAnnotation.LinkStyle linkStyle,
                         String hyperlinkUri) {
            this.rect = rect;
            this.color = color;
            this.hyperlinkUri = hyperlinkUri;
            this.linkStyle = linkStyle;
        }

        public PDRectangle getRect() {
            return rect;
        }

        public AWTColor getColor() {
            return color;
        }

        public String getHyperlinkURI() {
            return hyperlinkUri;
        }

        public Annotations.HyperlinkAnnotation.LinkStyle getLinkStyle() {
            return linkStyle;
        }

        @Override
        public String toString() {
            return "Hyperlink [rect=" + rect + ", color=" + color
                    + ", hyperlinkUri=" + hyperlinkUri + ", linkStyle="
                    + linkStyle + "]";
        }

    }

}
