package com.fluidtouch.noteshelf.document.textedit;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.AlignmentSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.TypefaceSpan;
import android.text.style.UnderlineSpan;

import com.fluidtouch.noteshelf.commons.utils.ScreenUtil;
import com.fluidtouch.noteshelf.document.enums.NSTextAlignment;
import com.fluidtouch.noteshelf.document.textedit.texttoolbar.FTFontFamily;
import com.fluidtouch.noteshelf.documentframework.Utilities.FTConstants;

public class FTStyledTextBitmapGenerator {
    private Canvas mCanvas;
    private FTStyledText mInputText;

    public Bitmap getBitmap(Context context, final float width, float height, FTStyledText inputText, final float scaleFactor) {
        if (context == null)
            throw new NullPointerException("Context cannot be null");
        if (width <= 0 || height <= 0)
            return null;
        if (inputText == null) {
            inputText = new FTStyledText();
        }
        this.mInputText = inputText;
        float margin = ScreenUtil.convertDpToPx(context, inputText.getPadding());
        margin = (margin * scaleFactor);

        int intMargin = (int) margin;

        Bitmap bitmap = Bitmap.createBitmap((int) width, (int) height, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(bitmap);
        mCanvas.translate(intMargin, intMargin);
        drawTextWithLayout(context, width - (2 * intMargin), context.getResources().getDisplayMetrics().scaledDensity, scaleFactor);
        return bitmap;
    }

    private void drawTextWithLayout(Context context, float width, float density, float scaleFactor) {
        mCanvas.scale(scaleFactor, scaleFactor);
        getStaticLayout(mInputText, context, width, scaleFactor, density).draw(mCanvas);
    }

    public StaticLayout getStaticLayout(FTStyledText inputText, Context context, float width, float scaleFactor, float density) {
        int start = 0;
        int end = Math.max(inputText.getPlainText().length(), 0);
        Spannable spannable = new SpannableStringBuilder(inputText.getPlainText());
        spannable.setSpan(new ForegroundColorSpan(inputText.getColor()), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannable.setSpan(new AbsoluteSizeSpan(inputText.getSize(), false), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//        try {
//                inputText.setDefaultFont(false);
//            spannable.setSpan(new FTCustomTypefaceSpan(inputText.getFontFamily(), Typeface.createFromFile(getFullFontFamily(inputText))), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//        } catch (Exception e) {
//            if (inputText.getFontFamily().equals(FTConstants.TEXT_DEFAULT_FONT_FAMILY) && inputText.getStyle() == -1) {
//                inputText.setStyle(0);
//            }
        inputText.setDefaultFont(true);
        spannable.setSpan(new FTCustomTypefaceSpan(inputText.getFontFamily(), Typeface.createFromAsset(context.getAssets(), getFullFontFamily(inputText))), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//        }
        //spannable.setSpan(new FTCustomTypefaceSpan(inputText.getFontFamily(), inputText.isDefaultFont() ? Typeface.createFromAsset(context.getAssets(), getFullFontFamily(inputText)) : Typeface.createFromFile(getFullFontFamily(inputText))), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannable.setSpan(new AlignmentSpan.Standard(getLayoutAlignment(inputText.getAlignment())), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        if (inputText.isUnderline()) {
            spannable.setSpan(new UnderlineSpan(), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        spannable.setSpan(new RelativeSizeSpan(density), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        return StaticLayout.Builder.obtain(spannable, 0, Math.max(inputText.getPlainText().length(), 0), new TextPaint(), Math.max((int) (width / (scaleFactor)), 0)).build();
    }

    private String getFullFontFamily(FTStyledText inputText) {
        if (inputText.isDefaultFont()) {
            if (inputText.getStyle() != -1)
                return "fonts/" + inputText.getFontFamily() + "_" + FTFontFamily.getStyleForInt(inputText.getStyle()).toLowerCase() + ".ttf";
            else {
                inputText.setStyle(0);
                return "fonts/" + inputText.getFontFamily() + "_regular.ttf";
            }
        } else {
            if (inputText.getStyle() != -1)
                return FTConstants.SYSTEM_FONTS_PATH + inputText.getFontFamily() + "-" + (inputText.isDefaultFont() ? FTFontFamily.getStyleForInt(inputText.getStyle()).toLowerCase() : FTFontFamily.getStyleForInt(inputText.getStyle())) + ".ttf";
            else
                return FTConstants.SYSTEM_FONTS_PATH + inputText.getFontFamily() + ".ttf";
        }
    }

    private String getFontStyle(FTStyledText inputText) {
        int style = inputText.getStyle();
        /*if (style == Typeface.ITALIC) {
            return "italic";
        } else if (style == Typeface.BOLD) {
            return "bold";
        } else if (style == Typeface.BOLD_ITALIC) {
            return "bold_italic";
        } else {
            return "regular";
        }*/
        if (style == Typeface.ITALIC) {
            return "Italic";
        } else if (style == Typeface.BOLD) {
            return "Bold";
        } else if (style == Typeface.BOLD_ITALIC) {
            return "BoldItalic";
        } else if (style == Typeface.NORMAL) {
            return "Regular";
        } else {
            return "";
        }
    }

    private Layout.Alignment getLayoutAlignment(NSTextAlignment alignment) {
        if (alignment == NSTextAlignment.NSTextAlignmentLeft) {
            return Layout.Alignment.ALIGN_NORMAL;
        } else if (alignment == NSTextAlignment.NSTextAlignmentCenter) {
            return Layout.Alignment.ALIGN_CENTER;
        } else if (alignment == NSTextAlignment.NSTextAlignmentRight) {
            return Layout.Alignment.ALIGN_OPPOSITE;
        }

        return Layout.Alignment.ALIGN_NORMAL;
    }

    private class FTCustomTypefaceSpan extends TypefaceSpan {

        private final Typeface newTypeface;

        public FTCustomTypefaceSpan(String family, Typeface type) {
            super(family);
            newTypeface = type;
        }

        @Override
        public void updateDrawState(TextPaint ds) {
            applyCustomTypeFace(ds, newTypeface);
        }

        @Override
        public void updateMeasureState(TextPaint paint) {
            applyCustomTypeFace(paint, newTypeface);
        }

        private void applyCustomTypeFace(Paint paint, Typeface tf) {
            int oldStyle;
            Typeface old = paint.getTypeface();
            if (old == null) {
                oldStyle = 0;
            } else {
                oldStyle = old.getStyle();
            }

            int fake = oldStyle & ~tf.getStyle();
            if ((fake & Typeface.BOLD) != 0) {
                paint.setFakeBoldText(true);
            }

            if ((fake & Typeface.ITALIC) != 0) {
                paint.setTextSkewX(-0.25f);
            }

            paint.setTypeface(tf);
        }
    }
}
