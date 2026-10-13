package com.btpl.physio.dto;

import com.btpl.physio.model.Role;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignupRequest {

    private Role role;

    @NotBlank
    private String name;

    @Email
    @NotBlank
    private String email;

    @Size(min = 6)
    private String password;

    private String contactNumber;

    // Patient fields
    private Integer age;
    private String gender;

    // Physio fields
    private String qualification;
    private String specialization;
    private String clinicAddress;
    private Double fees;
}