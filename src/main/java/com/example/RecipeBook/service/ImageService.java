package com.example.RecipeBook.service;

import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@Slf4j
public class ImageService {
    @Value("${image.upload-dir:uploads/images}")
    private String uploadDir;

    @Value("${image.max-size:5242880}")
    private long maxSize;

    public String saveImage(MultipartFile file){
        validateImage(file);

        try{
            Path uploadPath = Paths.get(uploadDir);

            if(!Files.exists(uploadPath)){
                Files.createDirectories(uploadPath);
            }

            String fileName = UUID.randomUUID().toString() + getExtension(file.getOriginalFilename());
            Path filePath = uploadPath.resolve(fileName);

            file.transferTo(filePath.toFile());

            return "/uploads/images/"+fileName;
        }
        catch(IOException e){
            log.error("Failed to save image" , e);
            throw new RuntimeException("Failed to save image" , e);
        }
    }

    public String[] resizeImage(String originalPath , String imageId){

        try{
            File originalFile = new File(uploadDir + '/' + new File(originalPath).getName());

            String thumbnail = uploadDir + "/thumb_" + imageId + ".jpg";
            String medium = uploadDir + "/med_" + imageId + ".jpg";
            String large = uploadDir + "/large_" + imageId + ".jpg";

            Thumbnails.of(originalFile)
                    .size(150 , 150)
                    .outputFormat("jpg")
                    .toFile(thumbnail);

            Thumbnails.of(originalFile)
                    .size(500 , 500)
                    .outputFormat("jpg")
                    .toFile(medium);

            Thumbnails.of(originalFile)
                    .size(1200 , 1200)
                    .outputFormat("jpg")
                    .toFile(large);

            return new String[]{
                    "/uploads/images/thumb_" + imageId + ".jpg",
                    "/uploads/images/med_" + imageId + ".jpg",
                    "/uploads/images/large_" + imageId + ".jpg"
            };
        }

        catch (IOException e){
            log.error("Failed to resize iamge" , e);
            throw new RuntimeException("Failed to resize image" , e);
        }
    }

    private void validateImage(MultipartFile file){
        if(file.isEmpty()){
            throw new IllegalArgumentException("File is empty");
        }

        if(file.getSize() > maxSize){
            throw new IllegalArgumentException("File size exceeds the maximum allowed size");
        }

        String contentType = file.getContentType();
        if(contentType == null || !contentType.startsWith("/image")){
            throw new IllegalArgumentException("File must be an image");
        }
    }

    private String getExtension(String filename) {
        if (filename == null) return ".jpg";
        int lastDot = filename.lastIndexOf('.');
        return lastDot > 0 ? filename.substring(lastDot) : ".jpg";
    }
}
