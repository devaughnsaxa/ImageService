package org.thehoneycomb.imageserving.model;

public enum ImageFormat {
    JPG("image/jpeg"),
    PNG("image/png"),
    GIF("image/gif");

    private final String typeValue;

    ImageFormat(String typeValue) {
        this.typeValue = typeValue;
    }

    public String getTypeValue(){
        return typeValue;
    }
}
