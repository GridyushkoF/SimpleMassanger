package net.study.service;

import lombok.RequiredArgsConstructor;
import net.study.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class CustomizationService {
    private final ImageStorageService imageStorageService;
    private final UserService userService;
    private final UserRepository userRepository;
    @Value("${avatars-path}")
    private String avatarsFolderPath;
    public String updateProfileAvatar(MultipartFile avatar) {
        String imageName = imageStorageService.uploadImage(avatar,avatarsFolderPath);
        userService.getMyUserOptional().ifPresent(myUser -> {
            imageStorageService.deleteImage(avatarsFolderPath + "/" + myUser.getAvatarName());
            myUser.setAvatarName(imageName);
            userRepository.save(myUser);
        });
        return imageName;
    }
    public String getMyAvatar() {
        return userService.getMyUserOptional().get().getAvatarName();
    }
}
