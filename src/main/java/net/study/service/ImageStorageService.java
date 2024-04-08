package net.study.service;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.UUID;


@Service
@Log4j2
public class ImageStorageService {
    public String uploadImageAndReturnFileName(MultipartFile image, String path) {
        String imageName = generateFilename(getMultipartFileExtension(image));
        File newImageFile = new File(path + "/" + imageName);
        try {
            image.transferTo(newImageFile);
        }catch (Exception e) {
            e.printStackTrace();
        }
        return imageName;
    }
    public void deleteImage(String path) {
        boolean isSuccessfulDeleted = new File(path).delete();
        if(!isSuccessfulDeleted) {
            log.error("avatar with a path: " + path + " was not saved successful!");
        }
    }
    public String getMultipartFileExtension(MultipartFile multipartFile) {
        String fileExtension = "";
        String contentType = multipartFile.getContentType();
        assert contentType != null;
        int slashIndex = contentType.lastIndexOf('/');
        if (slashIndex >= 0 && slashIndex < contentType.length() - 1) {
            fileExtension = contentType.substring(slashIndex + 1);
        }
        return fileExtension;
    }
    public String generateFilename(String extension) {
        return UUID.randomUUID() + "." + extension;
    }
}
