package com.unibuc.library.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String name;

    @Email
    @NotBlank
    @Column(unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    private UserRole role;

    @Min(1)
    private int maxBorrowLimit;

    // constructors
    public User() {
    }

    public User(String name, String email, UserRole role, int maxBorrowLimit) {
        this.name = name;
        this.email = email;
        this.role = role;
        this.maxBorrowLimit = maxBorrowLimit;
    }

    public User(long id, String name, String email, UserRole role, int maxBorrowLimit) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
        this.maxBorrowLimit = maxBorrowLimit;
    }

    // getters & setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public int getMaxBorrowLimit() {
        return maxBorrowLimit;
    }

    public void setMaxBorrowLimit(int maxBorrowLimit) {
        this.maxBorrowLimit = maxBorrowLimit;
    }

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", role=" + role +
                ", maxBorrowLimit=" + maxBorrowLimit +
                '}';
    }
}
