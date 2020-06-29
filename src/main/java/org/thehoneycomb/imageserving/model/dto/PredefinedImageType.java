package org.thehoneycomb.imageserving.model.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.thehoneycomb.imageserving.model.ImageFormat;
import org.thehoneycomb.imageserving.model.Scale;


@Getter
@RequiredArgsConstructor
public class PredefinedImageType {
    String name;
    int height;
    int width;
    int quality;
    Scale scale;
    String fillColor;
    ImageFormat format;

    @Override
    public String toString() {
        return "PredefinedImageType{" +
                "name='" + name + '\'' +
                ", height=" + height +
                ", width=" + width +
                ", quality=" + quality +
                ", scale=" + scale +
                ", fillColor='" + fillColor + '\'' +
                ", format=" + format +
                '}';
    }
}
