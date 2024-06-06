package net.study.model.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NonNull;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "users", indexes = {
        @Index(name = "username_FK", columnList = "name")
})
public @Data class User implements Comparable<User> {
    @Column(name = "id")
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;
    @Column(name = "name")
    private String username;
    @Column(name = "password")
    private String password;
    @Column(name = "avatar_name")
    private String avatarName;
    @OneToMany(fetch = FetchType.EAGER)
    @JsonProperty(namespace = "role_list")
    private List<Role> roleList;
    @JsonProperty(namespace = "contact_list")
    @ElementCollection(fetch = FetchType.EAGER)
    private Set<String> contactUsernameSet;
    private String description;
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime creationDateTime;

    public User(String username, String password, String avatarName, List<Role> roleList, Set<String> contactUsernameSet, String description) {
        this.username = username;
        this.password = password;
        this.avatarName = avatarName;
        this.roleList = roleList;
        this.contactUsernameSet = contactUsernameSet;
        this.description = description;
        this.creationDateTime = LocalDateTime.now();
    }
    public User() {
        this.creationDateTime = LocalDateTime.now();
    }
    @Override
    public int compareTo(@NonNull User user) {
        return this.creationDateTime.compareTo(user.creationDateTime);
    }
}

