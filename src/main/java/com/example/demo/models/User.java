package com.example.demo.models;

import java.time.LocalDate;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;

@Entity
@Table(name = "app_user",
  uniqueConstraints = {
    @UniqueConstraint(name="uk_user_email", columnNames = "email"),
    @UniqueConstraint(name="uk_user_username", columnNames = "username")
})
@Data
public class User {
    @Id
    @GeneratedValue
    @org.hibernate.annotations.UuidGenerator
    private UUID id;

    @Column(name="username", length=50, nullable=false)
    private String username;

    @Column(name="email", length=254, nullable=false)
    private String email;

    @Column(name="password_hash", nullable=false, length=72)
    private String passwordHash;

    @Column(name="display_name", length=100)
    private String displayName;

    @Column(name="birth_date")
    private LocalDate birthDate;

    @Column(name="phone", length=30)
    private String phone;
}

