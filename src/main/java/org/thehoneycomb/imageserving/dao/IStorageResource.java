package org.thehoneycomb.imageserving.dao;

import org.thehoneycomb.imageserving.model.dto.PredefinedImageType;

import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.Optional;

public interface IStorageResource {

    boolean hasImage(String path, String filename);

    Optional<BufferedImage> getImage(String path, String referenceName);

    Optional<URL> storeImage(String path, String referenceName, BufferedImage image, PredefinedImageType type);

    void flushImage(String path, String filename);
}
