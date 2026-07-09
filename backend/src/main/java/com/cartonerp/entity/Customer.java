package com.cartonerp.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "customers")
public class Customer {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 120)
    private String name;
    @Column(length = 60) private String contact;
    @Column(length = 30) private String phone;
    @Column(length = 120) private String email;
    @Column(length = 300) private String address;
    @Column(columnDefinition = "TEXT") private String notes;
    private LocalDateTime createdAt = LocalDateTime.now();

    public Customer() {}
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getContact() { return contact; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public String getAddress() { return address; }
    public String getNotes() { return notes; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setId(Long id) { this.id = id; }
    public void setName(String n) { this.name = n; }
    public void setContact(String c) { this.contact = c; }
    public void setPhone(String p) { this.phone = p; }
    public void setEmail(String e) { this.email = e; }
    public void setAddress(String a) { this.address = a; }
    public void setNotes(String n) { this.notes = n; }
    public void setCreatedAt(LocalDateTime c) { this.createdAt = c; }
}
