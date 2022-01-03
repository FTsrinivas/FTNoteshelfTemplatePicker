package com.fluidtouch.noteshelf.shelf.enums;

import java.io.Serializable;

public enum RKShelfItemType implements Serializable {
    GROUP, DOCUMENT, DUMMY;

    public static RKShelfItemType getType(int ordinal) {
        switch (ordinal) {
            case 0:
                return RKShelfItemType.GROUP;
            case 1:
                return RKShelfItemType.DOCUMENT;
            default:
                return RKShelfItemType.DUMMY;
        }
    }
}
