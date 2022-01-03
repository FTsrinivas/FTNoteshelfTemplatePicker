package com.fluidtouch.noteshelf.templatepicker.common.supporteddevicesplistdatamodel;

public class ItemModel /*implements Comparable<ItemModel>*/{

    String dimension;
    String dimension_land;
    String dimension_port;
    String displayName = "A4 8.3 x 11.7\"\"";
    String identifier;

    public String getDimension() {
        return dimension;
    }

    public void setDimension(String dimension) {
        this.dimension = dimension;
    }

    public String getDimension_land() {
        return dimension_land;
    }

    public void setDimension_land(String dimension_land) {
        this.dimension_land = dimension_land;
    }

    public String getDimension_port() {
        return dimension_port;
    }

    public void setDimension_port(String dimension_port) {
        this.dimension_port = dimension_port;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getHeaderTitle() {
        return headerTitle;
    }

    public void setHeaderTitle(String headerTitle) {
        this.headerTitle = headerTitle;
    }

    String headerTitle;

    public ItemModel(String dimension,
                        String dimension_land, String dimension_port,
                            String displayName, String identifier) {
        //this.deviceTitle = deviceTitle;
        this.dimension = dimension;
        this.dimension_land = dimension_land;
        this.dimension_port = dimension_port;
        this.displayName = displayName;
        this.identifier = identifier;
        this.headerTitle = headerTitle;
    }


    /*public boolean isSectionHeader() {
        return isSectionHeader;
    }*/

    /*@Override
    public int compareTo(ItemModel itemModel) {
        return this.deviceTitle.compareTo(itemModel.deviceTitle);
    }*/

    /*public void setToSectionHeader() {
        isSectionHeader = true;
    }*/
}
