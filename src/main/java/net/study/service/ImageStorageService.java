package net.study.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImageStorageService {
    public String uploadImage(MultipartFile image,String path) {
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
        new File(path).delete();
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
