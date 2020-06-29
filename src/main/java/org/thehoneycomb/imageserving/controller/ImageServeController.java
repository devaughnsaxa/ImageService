package org.thehoneycomb.imageserving.controller;

import javassist.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.thehoneycomb.imageserving.model.dto.PredefinedImageType;
import org.thehoneycomb.imageserving.service.OptimizeService;
import org.thehoneycomb.imageserving.utils.ImageTypeMapper;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.io.*;
import java.util.Optional;

@RestController
@Log4j2
@RequiredArgsConstructor
@RequestMapping("/image")
public class ImageServeController {
  private final OptimizeService optimizeService;

  @GetMapping({"/flush/{predefined-type-name}"})
  public ResponseEntity<String> flush(
      @PathVariable("predefined-type-name") String type,
      @RequestParam("reference") String filename) {

    Optional<PredefinedImageType> predefinedImageType;
    ResponseEntity<String> response = ResponseEntity.ok().body("The file " + filename + " has been flushed");
    try {
      predefinedImageType = ImageTypeMapper.getType(type);
      if(predefinedImageType.isEmpty()){
        throw new NotFoundException("Could not load predefined type " + type);
      }

      optimizeService.flush(type + "/", filename, predefinedImageType.get());
    } catch (IOException e) {
      log.error("Error on flushing  " + type);
      response = ResponseEntity.notFound().build();
    } catch (NotFoundException e) {
      log.error(e.getMessage());
      response = ResponseEntity.notFound().build();
    }
    return response;

  }

  @GetMapping({"/show/{predefined-type-name}", "/show/{predefined-type-name}/{dummy-seo-name}"})
  public ResponseEntity<byte[]> showImage(
      @PathVariable("predefined-type-name") String typeName,
      @PathVariable(name = "dummy-seo-name", required = false) String seoName,
      @RequestParam("reference") String referenceName) {

    ResponseEntity<byte[]> response;
    Optional<byte[]> imageOptimized;
    Optional<PredefinedImageType> predefinedImageType;

    try {
      predefinedImageType = ImageTypeMapper.getType(typeName); 
      if(predefinedImageType.isEmpty()){
        throw new NotFoundException("Predefined type " + typeName + "could not be found");
      }

      if(!optimizeService.hasOriginalImageOnSource(referenceName)){
        throw new NotFoundException("Orignal Image not found on Source");
      }

      imageOptimized = optimizeService.getOptimizedFile(referenceName, predefinedImageType.get());
      if (imageOptimized.isEmpty()){
        throw new IOException("Could not get optimized file " + referenceName);
      }

      response =
          ResponseEntity.ok()
              .contentType(MediaType.valueOf(predefinedImageType.get().getFormat().getTypeValue()))
              .body(imageOptimized.get());

    } catch (IOException | NotFoundException e) {
      log.info(e.getMessage());
      response = ResponseEntity.notFound().build();
    }
    return response;
  }
}
