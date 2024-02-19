package net.study.controller.rest;

import lombok.RequiredArgsConstructor;
import net.study.service.CustomizationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class CustomizationController {
    private final CustomizationService customizationService;
    @PostMapping("/update-profile-avatar")
    public ResponseEntity<Map<String,String>> updateProfileAvatar(@RequestParam("profile-avatar") MultipartFile avatarMultipartFile) {
        String avatarPath = customizationService.updateProfileAvatar(avatarMultipartFile);
        return ResponseEntity.ok(Map.of("avatar_path",avatarPath));
    }
    @GetMapping("/get-my-avatar")
    public ResponseEntity<Map<String,String>> getMyAvatar() {
        return ResponseEntity.ok(Map.of("my_avatar",customizationService.getMyAvatar()));
    }
}
