package net.study.model;

import jakarta.persistence.*;
import net.study.model.user.User;
@Entity
public class PrivateChat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    private User member1;
    @ManyToOne
    private User member2;
}
