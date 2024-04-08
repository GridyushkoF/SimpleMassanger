package net.study.controller.rest;

import lombok.RequiredArgsConstructor;
import net.study.service.CustomizationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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
    @GetMapping("/update-description/{description}")
    public void updateDescription(@PathVariable String description) {
        customizationService.updateDescription(description);
    }
}
