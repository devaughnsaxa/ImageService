package org.thehoneycomb.imageserving.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.thehoneycomb.imageserving.dao.IStorageResource;
import org.springframework.stereotype.Service;
import org.thehoneycomb.imageserving.model.dto.PredefinedImageType;
import org.thehoneycomb.imageserving.utils.ImageTypeMapper;
import org.thehoneycomb.imageserving.utils.ImageUtils;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Log4j2
public class OptimizeService {

  private final IStorageResource storageResource;
  @Value("${s3-original-path}") private String originalPath;
  @Value("${source-root-url}") private String sourceRootUrl ;

  public boolean hasOriginalImage(String filename) {
    return storageResource.hasImage(originalPath, filename);
  }

  public boolean hasOptimizedImage(String path, String filename, PredefinedImageType type) {
    return storageResource.hasImage(path, filename);
  }

  public boolean hasOriginalImageOnSource(String filename) {
    boolean result = false;
    try {
      URL url = new URL(sourceRootUrl + filename);
      BufferedImage image = ImageIO.read(url);
      result = image != null;
    } catch (MalformedURLException e) {
      log.info(("URL error with image " + filename));
      }catch (IOException e) {
      log.error(("Server error " + filename));
    }
    return result;
  }

  public boolean retrieveOriginalFromSource(String uniqueFileName, PredefinedImageType type){
    boolean result = false;
    try {
      URL url = new URL(sourceRootUrl + uniqueFileName);
      BufferedImage imageOriginal = ImageIO.read(url);

      Optional<URL> urlStored = storageResource.storeImage(originalPath, uniqueFileName, imageOriginal, type);
      if(!hasOriginalImage(uniqueFileName) || urlStored.isEmpty()){
        throw new IOException("Could not store image : " + uniqueFileName);
      }
      result = true;
    } catch (MalformedURLException e){
      log.error("Original file URL is malformed," +
              " file name is incorrect," +
              " no protocol is specified or an unknown protocol is found");
    } catch (IOException e) {
      log.error(e.getMessage());
      log.error("Original file from source is corrupted");
    }
    return result;
  }


  public Optional<byte[]> getOptimizedFile(String referenceName, PredefinedImageType type){
    Optional<BufferedImage> bufferedImage;
    Optional<byte[]> result = Optional.empty();
    try{
      String uniqueFileName = referenceName.replace("/", "_");
      Optional<String> path = createPathFromReference(referenceName, type);
      if(path.isEmpty()){
        throw new MalformedURLException("Malformed filename of file " + referenceName);
      }

      if(hasOptimizedImage(path.get(), uniqueFileName, type)){

        bufferedImage = storageResource.getImage(path.get(), uniqueFileName);
        if(bufferedImage.isEmpty()){
          throw new IOException("Could not get optimized image : " + uniqueFileName);
        }

      } else if(hasOriginalImage(uniqueFileName)
              && generateOptimizedOnResource(path.get(), uniqueFileName, type)){

        bufferedImage = storageResource.getImage(path.get(), uniqueFileName);
        if(bufferedImage.isEmpty()){
          throw new IOException("Could not get original image : " + uniqueFileName);
        }

        log.info("hasOriginal");
      } else {
        boolean retrieved = retrieveOriginalFromSource(uniqueFileName, type);
        generateOptimizedOnResource(path.get(), uniqueFileName, type);
        bufferedImage = storageResource.getImage(path.get(), uniqueFileName);
        if(bufferedImage.isEmpty() || !retrieved){
          throw new IOException("Could not get retrieve Original image : " + uniqueFileName);
        }
      }
      result = ImageUtils.convertBufferedToBytes(bufferedImage.get(), type);
      if(result.isEmpty()){
        throw new IOException("Could not converto image to bytes : " + uniqueFileName);
      }
    } catch (IOException e){
      log.error(e.getMessage());
    }
    return result;
  }

  private boolean generateOptimizedOnResource(String path, String uniqueFileName, PredefinedImageType type)  {
    boolean result = false;
    try {
      Optional<BufferedImage> original = storageResource.getImage(originalPath, uniqueFileName);
      if (original.isEmpty()) {
        throw new IOException("Original not found on Resource Repository");
      }

      Optional<BufferedImage> resize = ImageUtils.resize(original.get(), type);
      if(resize.isEmpty()) {
        throw new IOException("Could not resize image " + uniqueFileName);
      }

      Optional<URL> url = storageResource.storeImage(path, uniqueFileName, resize.get(), type);
      if(!hasOptimizedImage(path, uniqueFileName, type) || url.isEmpty()){
        throw new IOException("Could not store resized image ");
      }
      log.info("File "+ uniqueFileName + "Optimized successufully");
      result = true;
    } catch (IOException e) {
      log.error(e.getMessage());
    }
    return result;
  }

  public void flush(String path, String filename, PredefinedImageType type) throws IOException {
    storageResource.flushImage(createPathFromReference(filename, type).get(), filename);
    if (path.equals(originalPath)) {
      File[] fileNames = new File("src/main/resources/types").listFiles();
      for (File file : fileNames) {
        PredefinedImageType type2 = ImageTypeMapper.getType(file.getName().split("\\.")[0]).get();
        storageResource.flushImage(createPathFromReference(filename, type2).get(), filename);
      }
    }
  }

  public Optional<String> createPathFromReference(String referenceName, PredefinedImageType type){
    StringBuilder path = new StringBuilder("");
    Optional<String> result = Optional.empty();
    try{
      String referenceUnderscore = referenceName.replace("/","_");
      String uniqueFileNameWithoutExtension = referenceUnderscore.split("\\.")[0];
      path.append(type.getName() + "/");
      if(uniqueFileNameWithoutExtension.length() > 8){
        path.append(uniqueFileNameWithoutExtension, 0, 4);
        path.append("/");
        path.append(uniqueFileNameWithoutExtension, 4, 8);
        path.append("/");
      }else if (uniqueFileNameWithoutExtension.length() > 4){
        path.append(uniqueFileNameWithoutExtension, 0, 4);
        path.append("/");
      }
      result = Optional.of(path.toString());
    } catch (IndexOutOfBoundsException e){
      log.error("Malformed Reference name");
    }
    return result;
  }
}
