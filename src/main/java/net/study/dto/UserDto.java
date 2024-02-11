package net.study.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
@AllArgsConstructor
@NoArgsConstructor
public @Data class UserDto {
    private String username;
    private String pathToAvatar;
    private Set<String> contactUsernameSet;
}
