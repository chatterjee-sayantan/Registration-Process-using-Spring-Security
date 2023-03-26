package com.sayantan.springsecuritydemo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    @Column(length=60)                        //maximum password length should be 60. If more, it'll throw and error.
    private String password;                  // We are going to use the BCrypt password encoder to encode this password and then store in the database
    private String role;
    private boolean enabled = false;         //by default the user would be disabled
}
