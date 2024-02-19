package net.study.model.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;
import java.util.Set;

@Entity
@Table(name = "users")
public @Data class User {
    @Column(name = "id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
    @ElementCollection
    private Set<String> contactUsernameSet;
}

