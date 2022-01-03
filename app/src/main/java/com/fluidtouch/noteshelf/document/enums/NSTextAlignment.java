package com.fluidtouch.noteshelf.document.enums;

public enum NSTextAlignment {
    NSTextAlignmentLeft,        // Visually left aligned
    NSTextAlignmentCenter,      // Visually centered
    NSTextAlignmentRight,       // Visually right aligned
    NSTextAlignmentJustified,   // Fully-justified. The last line in a paragraph is natural-aligned.
    NSTextAlignmentNatural;     // Indicates the default alignment for script

    public static NSTextAlignment initWithRawValue(int rawValue) {
        NSTextAlignment type;
        switch (rawValue) {
            case 0:
                type = NSTextAlignmentLeft;
                break;
            case 1:
                type = NSTextAlignmentCenter;
                break;
            case 2:
                type = NSTextAlignmentRight;
                break;
            case 3:
                type = NSTextAlignmentJustified;
                break;
            case 4:
                type = NSTextAlignmentNatural;
                break;
            default:
                type = NSTextAlignmentLeft;
                break;
        }
        return type;
    }

    public int toInt() {
        switch (this) {
            case NSTextAlignmentLeft:
                return 0;
            case NSTextAlignmentCenter:
                return 1;
            case NSTextAlignmentRight:
                return 2;
            case NSTextAlignmentJustified:
                return 3;
            case NSTextAlignmentNatural:
                return 4;
        }
        return 0;
    }
}
