package com.researchassistant.backend.model;
import jakarta.persistence.*;
import java.time.LocalDateTime;
@Entity @Table(name="users")
public class User {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 @Column(nullable=false,unique=true,length=50) public String username;
 @Column(nullable=false,unique=true,length=100) public String email;
 @Column(name="password_hash",nullable=false) public String passwordHash;
 @Enumerated(EnumType.STRING) @Column(nullable=false) public Role role=Role.USER;
 @Column(name="created_at",insertable=false,updatable=false) public LocalDateTime createdAt;
 public enum Role { USER, ADMIN }
}
