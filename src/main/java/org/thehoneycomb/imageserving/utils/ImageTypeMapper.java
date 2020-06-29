package org.thehoneycomb.imageserving.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.thehoneycomb.imageserving.model.dto.PredefinedImageType;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

public class ImageTypeMapper {

    private static ObjectMapper mapper;

    public static Optional<PredefinedImageType> getType(String typeName) throws IOException {
        Optional<PredefinedImageType> result = Optional.empty();
        mapper = new ObjectMapper(new YAMLFactory());
        PredefinedImageType type =
                mapper.readValue(new File("src/main/resources/types/"+ typeName +".yml"), PredefinedImageType.class);
        result = Optional.of(type);
        return result;
    }
}
