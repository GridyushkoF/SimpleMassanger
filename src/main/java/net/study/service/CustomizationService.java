package net.study.service;

import lombok.RequiredArgsConstructor;
import net.study.model.user.User;
import net.study.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class CustomizationService {
    private final ImageStorageService imageStorageService;
    private final UserRepository userRepository;
    private final MyUserDataStorageService myUserData;
    @Value("${avatars-path}")
    private String avatarsFolderPath;
    public String updateProfileAvatar(MultipartFile avatar) {
        String imageName = imageStorageService.uploadImageAndReturnFileName(avatar,avatarsFolderPath);
        User myUser = myUserData.getMyUser();
        imageStorageService.deleteImage(avatarsFolderPath + "/" + myUser.getAvatarName());
        myUser.setAvatarName(imageName);
        userRepository.save(myUser);

        return imageName;
    }
    public void updateDescription(String description) {
        User myUser = myUserData.getMyUser();
        myUser.setDescription(description);
        userRepository.save(myUser);
    }
}