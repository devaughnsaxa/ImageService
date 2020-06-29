package org.thehoneycomb.imageserving.dao.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.thehoneycomb.imageserving.dao.IStorageResource;
import org.thehoneycomb.imageserving.model.dto.PredefinedImageType;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.thehoneycomb.imageserving.utils.ImageUtils;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import javax.annotation.PostConstruct;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.Optional;



@Component("s3")
@Primary
@Log4j2
@RequiredArgsConstructor
public class S3Resource implements IStorageResource {

  private final S3Client s3Client;
  @Value("${aws-bucket}") private String bucketName;
  @Value("${aws-region}") private String awsRegion;
  @Value("${s3-original-path}") private String originalPath;

  @PostConstruct
  public void init() {
    try{
      CreateBucketRequest createBucketRequest = CreateBucketRequest
              .builder()
              .bucket(bucketName)
              .createBucketConfiguration(CreateBucketConfiguration.builder()
                      .locationConstraint(Region.of(awsRegion).id())
                      .build())
              .build();
    s3Client.createBucket(createBucketRequest);
    }catch (S3Exception e){
      log.error("Could not create bucket or already exists : " + bucketName);
    }
  }

  @Override
  public boolean hasImage(String path, String filename) {
    boolean result = false;
    try {
      final HeadObjectRequest request =
          HeadObjectRequest.builder().bucket(bucketName).key(path + filename).build();
      s3Client.headObject(request);
      result = true;
    } catch (NoSuchKeyException e) {
      log.info("File does not exist : " + path + filename);
      log.info(e.getMessage());
    }
    return result;
  }

  @Override
  public Optional<BufferedImage> getImage(String path, String referenceName) {
    Optional<BufferedImage> result = Optional.empty();
    try {
      final GetObjectRequest request =
          GetObjectRequest.builder().bucket(bucketName).key(path + referenceName).build();

      final ResponseBytes<GetObjectResponse> s3Object =
          s3Client.getObject(request, ResponseTransformer.toBytes());

      result = ImageUtils.convertBytesToBuffered(s3Object.asByteArray());
      if (result.isEmpty()) {
        throw new IOException("Could not convert to Bytes file " + referenceName);
      }
      log.info("Image loaded: " + referenceName);
    } catch (NoSuchKeyException e) {
      log.warn(e.getMessage());
      log.warn("Could not get, File does not exist : " + path + referenceName);
    } catch (S3Exception e) {
      log.warn(e.getMessage());
      log.warn("Error on S3 service");
    } catch (IOException e) {
      log.warn(e.getMessage());
    }
    return result;
  }

  @Override
  public Optional<URL> storeImage( //TODO TRY again after 200ms
      String path, String referenceName, BufferedImage image, PredefinedImageType type) {
    Optional<URL> result = Optional.empty();
    try {
      final Optional<byte[]> imageToUpload = ImageUtils.convertBufferedToBytes(image, type);
      if (imageToUpload.isEmpty()) {
        throw new IOException("Could not convert to Bytes file " + referenceName);
      }

      final RequestBody requestBody = RequestBody.fromByteBuffer(ByteBuffer.wrap(imageToUpload.get()));
      final PutObjectRequest putRequest =
          PutObjectRequest.builder()
              .bucket(bucketName)
              .key(path + referenceName)
              .contentType(type.getFormat().getTypeValue())
              .contentLength((long) imageToUpload.get().length)
              .build();

      PutObjectResponse putObjectResult = s3Client.putObject(putRequest, requestBody);

      final URL reportUrl = s3Client.utilities().
              getUrl(GetUrlRequest.builder().bucket(bucketName).key(path + referenceName).build());

      log.info("File stored to S3 : " + path + referenceName);
      log.info("File stored to S3 url : " + reportUrl.toString());
      result = Optional.of(reportUrl);
    } catch (S3Exception e) {
      log.error(e.getMessage());
      log.error("File does not exist 1: " + path + referenceName);
    } catch (IOException e) {
      log.error(e.getMessage());
      log.error("Could not convert image " + referenceName + " to bytes[] ");
    }
    return result;
  }

  @Override
  public void flushImage(String path, String filename) {
    DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder().bucket(bucketName)
            .key(path + filename).build();
    s3Client.deleteObject(deleteObjectRequest);
    log.info("Successfully flushed image "+ filename + " from " + path);
  }
}
