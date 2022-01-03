package com.fluidtouch.noteshelf.globalsearch.models;

import java.util.ArrayList;

/**
 * Create by Vineet 25/09/2019
 **/

public class FTSearchSectionTitle implements FTSearchSection {
    public String title;
    public String type;
    public ArrayList<FTSearchSectionContent> items;
    public int itemCount = 0;
    public String categoryName;
}