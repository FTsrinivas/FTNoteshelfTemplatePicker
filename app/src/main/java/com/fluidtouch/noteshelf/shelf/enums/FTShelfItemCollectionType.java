package com.fluidtouch.noteshelf.shelf.enums;

import java.io.Serializable;

public enum FTShelfItemCollectionType implements Serializable {
    RECENT, DEFAULT, SYSTEM, MIGRATED;

    public static FTShelfItemCollectionType getType(int ordinal) {
        switch (ordinal) {
            case 0:
                return FTShelfItemCollectionType.RECENT;
            case 1:
                return FTShelfItemCollectionType.DEFAULT;
            case 2:
                return FTShelfItemCollectionType.SYSTEM;
            case 3:
                return FTShelfItemCollectionType.MIGRATED;
            default:
                return FTShelfItemCollectionType.DEFAULT;
        }
    }
}