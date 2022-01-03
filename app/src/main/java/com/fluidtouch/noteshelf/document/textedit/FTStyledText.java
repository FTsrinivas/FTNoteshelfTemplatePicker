package com.fluidtouch.noteshelf.document.textedit;

import com.fluidtouch.noteshelf.document.enums.InputTextSpanType;
import com.fluidtouch.noteshelf.document.enums.NSTextAlignment;
import com.fluidtouch.noteshelf.documentframework.Utilities.FTConstants;

import java.io.Serializable;

public class FTStyledText implements Serializable {
    private String plainText = "";
    private InputTextSpanType type;
    private boolean isDefaultFont;

    private int padding = FTConstants.TEXT_DEFAULT_PADDING;
    private int size = FTConstants.TEXT_DEFAULT_SIZE;
    private int style = FTConstants.TEXT_DEFAULT_STYLE;
    private String fontFamily = FTConstants.TEXT_DEFAULT_FONT_FAMILY;
    private int color = FTConstants.TEXT_DEFAULT_COLOR;
    private boolean isUnderline = FTConstants.TEXT_DEFAULT_UNDERLINE;
    private NSTextAlignment alignment = FTConstants.TEXT_DEFAULT_ALIGNMENT;

    public FTStyledText(InputTextSpanType type, Object value) {
        this.type = type;
        if (this.type.equals(InputTextSpanType.SIZE)) {
            this.size = (int) value;
        } else if (this.type.equals(InputTextSpanType.STYLE)) {
            this.style = (int) value;
        } else if (this.type.equals(InputTextSpanType.FONT_FAMILY)) {
            this.fontFamily = (String) value;
        } else if (this.type.equals(InputTextSpanType.COLOR)) {
            this.color = (int) value;
        } else if (this.type.equals(InputTextSpanType.UNDERLINE)) {
            this.isUnderline = (boolean) value;
        } else if (this.type.equals(InputTextSpanType.ALIGNMENT)) {
            this.alignment = (NSTextAlignment) value;
        }
    }

    public FTStyledText() {
    }

    public static FTStyledText instance(FTStyledText initialText) {
        FTStyledText styledText = new FTStyledText();
        styledText.setPlainText(initialText.getPlainText());
        styledText.setType(initialText.getType());
        styledText.setSize(initialText.getSize());
        styledText.setStyle(initialText.getStyle());
        styledText.setFontFamily(initialText.getFontFamily());
        styledText.setColor(initialText.getColor());
        styledText.setUnderline(initialText.isUnderline());
        styledText.setAlignment(initialText.getAlignment());
        styledText.setPadding(initialText.getPadding());
        return styledText;
    }

    public int getPadding() {
        return padding;
    }

    public void setPadding(int padding) {
        this.padding = padding;
    }

    public String getPlainText() {
        return plainText;
    }

    public void setPlainText(String plainText) {
        this.plainText = plainText;
    }

    public InputTextSpanType getType() {
        return type;
    }

    public void setType(InputTextSpanType type) {
        this.type = type;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getStyle() {
        return style;
    }

    public void setStyle(int style) {
        this.style = style;
    }

    public String getFontFamily() {
        return fontFamily;
    }

    public void setFontFamily(String fontFamily) {
        this.fontFamily = fontFamily;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public boolean isUnderline() {
        return isUnderline;
    }

    public void setUnderline(boolean underline) {
        isUnderline = underline;
    }

    public NSTextAlignment getAlignment() {
        return alignment;
    }

    public void setAlignment(NSTextAlignment alignment) {
        this.alignment = alignment;
    }

    public boolean isDefaultFont() {
        return isDefaultFont;
    }

    public void setDefaultFont(boolean defaultFont) {
        isDefaultFont = defaultFont;
    }
}
