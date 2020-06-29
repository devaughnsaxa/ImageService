package org.thehoneycomb.imageserving.utils;

import lombok.extern.log4j.Log4j2;
import org.thehoneycomb.imageserving.model.dto.PredefinedImageType;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Optional;

@Log4j2
public class ImageUtils {

    public static Optional<BufferedImage> resize(BufferedImage original, PredefinedImageType type){
        return Optional.of(original); //TODO Resize image
    }

    public static Optional<BufferedImage> convertBytesToBuffered(byte[] imageData) {
        final ByteArrayInputStream bais = new ByteArrayInputStream(imageData);
        Optional<BufferedImage> result = Optional.empty();
        try {
            result = Optional.of(ImageIO.read(bais));
        } catch (IOException e) {
            log.info(e.getMessage());
            log.info("Could not convert image to BufferedImage");
        }
        return result;
    }

    public static Optional<byte[]> convertBufferedToBytes(BufferedImage imageData, PredefinedImageType type) {
        Optional<byte[]> result = Optional.empty();
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(imageData, type.getFormat().name(), baos);
            baos.flush();
            byte[] imageInByte = baos.toByteArray();
            baos.close();
            result = Optional.of(imageInByte);
        } catch (IOException e) {
            log.info(e.getMessage());
            log.info("Could not convert image to bytes");
        }
        return result;
    }
}
