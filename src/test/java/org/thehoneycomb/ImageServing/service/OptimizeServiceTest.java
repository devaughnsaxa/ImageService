package org.thehoneycomb.ImageServing.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.thehoneycomb.imageserving.dao.IStorageResource;
import org.thehoneycomb.imageserving.model.dto.PredefinedImageType;
import org.thehoneycomb.imageserving.service.OptimizeService;
import org.thehoneycomb.imageserving.utils.ImageTypeMapper;

import java.io.IOException;


@RunWith(MockitoJUnitRunner.class)
public class OptimizeServiceTest {

    @Mock
    IStorageResource storageResource;

    OptimizeService optimizeService;


    @BeforeEach
    public void setup(){
        optimizeService = new OptimizeService(storageResource);
    }

    @Test
    public void testsplitPathReference() {


        PredefinedImageType type = null;
        try {
            type = ImageTypeMapper.getType("thumbnail").get();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String test = "1.jpg";
        Assertions.assertEquals("thumbnail/",optimizeService.createPathFromReference(test, type).get());
        String test1 = "12.jpg";
        Assertions.assertEquals("thumbnail/",optimizeService.createPathFromReference(test1, type).get());
        String test2 = "123.jpg";
        Assertions.assertEquals("thumbnail/",optimizeService.createPathFromReference(test2, type).get());
        String test3 = "1234.jpg";
        Assertions.assertEquals("thumbnail/",optimizeService.createPathFromReference(test3, type).get());
        String test4 = "12345.jpg";
        Assertions.assertEquals("thumbnail/1234/",optimizeService.createPathFromReference(test4, type).get());
        String test5 = "123456.jpg";
        Assertions.assertEquals("thumbnail/1234/",optimizeService.createPathFromReference(test5, type).get());
        String test6 = "1234567.jpg";
        Assertions.assertEquals("thumbnail/1234/",optimizeService.createPathFromReference(test6, type).get());
        String test7 = "12345678.jpg";
        Assertions.assertEquals("thumbnail/1234/",optimizeService.createPathFromReference(test7, type).get());
        String test8 = "123456789.jpg";
        Assertions.assertEquals("thumbnail/1234/5678/",optimizeService.createPathFromReference(test8, type).get());
        String test9 = "1234567890.jpg";
        Assertions.assertEquals("thumbnail/1234/5678/",optimizeService.createPathFromReference(test9, type).get());
        String test10 = "abcdefghi";
        Assertions.assertEquals("thumbnail/abcd/efgh/",optimizeService.createPathFromReference(test10, type).get());
        String test11 = "/abcd.jpg";
        Assertions.assertEquals("thumbnail/_abc/",optimizeService.createPathFromReference(test11, type).get());
        String test12 = "ab/cde/fg.jpg";
        Assertions.assertEquals("thumbnail/ab_c/de_f/",optimizeService.createPathFromReference(test12, type).get());
    }
}
