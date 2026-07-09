package com.cartonerp.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false, length = 60)
    private String username;
    @Column(nullable = false, length = 200)
    private String password;
    @Column(length = 60)
    private String displayName;
    @Column(length = 30)
    private String role = "user";

    public User() {}
    public User(String username, String password, String displayName, String role) {
        this.username = username;
        this.password = password;
        this.displayName = displayName;
        this.role = role;
    }

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getDisplayName() { return displayName; }
    public String getRole() { return role; }
    public void setId(Long id) { this.id = id; }
    public void setUsername(String u) { this.username = u; }
    public void setPassword(String p) { this.password = p; }
    public void setDisplayName(String d) { this.displayName = d; }
    public void setRole(String r) { this.role = r; }
}
