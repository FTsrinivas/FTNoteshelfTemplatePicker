package com.fluidtouch.noteshelf.document.enums;

public enum FTEraserSizes {

    small(20), medium(34), large(44), auto(30), max_size(46), min_size(20);


    int value;

    FTEraserSizes(int i) {
        value = i;
    }

    public static int[] getSizes() {
        return new int[]{small.getValue(), medium.getValue(), large.getValue(), auto.getValue()};
    }

    public int getValue() {
        return this.value;
    }
}
