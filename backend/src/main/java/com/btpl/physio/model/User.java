package com.btpl.physio.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    private String contactNumber;

    private boolean emailVerified = false;

    // Patient fields
    private Integer age;
    private String gender;

    // Physio fields
    private String qualification;
    private String specialization;
    private String clinicAddress;
    private Double fees;
}