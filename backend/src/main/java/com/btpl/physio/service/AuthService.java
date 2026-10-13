package com.btpl.physio.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.btpl.physio.dto.LoginRequest;
import com.btpl.physio.dto.LoginResponse;
import com.btpl.physio.dto.SignupRequest;
import com.btpl.physio.model.Role;
import com.btpl.physio.model.User;
import com.btpl.physio.model.VerificationToken;
import com.btpl.physio.repository.UserRepository;
import com.btpl.physio.repository.VerificationTokenRepository;
import com.btpl.physio.security.JwtUtil;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final VerificationTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    public AuthService(UserRepository userRepository,
                       VerificationTokenRepository tokenRepository,
                       PasswordEncoder passwordEncoder,
                       EmailService emailService,
                       AuthenticationManager authenticationManager,
                       JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    public void signup(SignupRequest req) {
        if (req.getRole() == null) {
            throw new IllegalArgumentException("Role is required");
        }
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }

        User user = new User();
        user.setName(req.getName());
        user.setEmail(req.getEmail());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setRole(req.getRole());
        user.setContactNumber(req.getContactNumber());
        user.setEmailVerified(false);

        if (req.getRole() == Role.PHYSIO) {
            user.setQualification(req.getQualification());
            user.setSpecialization(req.getSpecialization());
            user.setClinicAddress(req.getClinicAddress());
            user.setFees(req.getFees());
        } else {
            user.setAge(req.getAge());
            user.setGender(req.getGender());
        }
        userRepository.save(user);

        VerificationToken vt = new VerificationToken();
        vt.setToken(UUID.randomUUID().toString());
        vt.setUser(user);
        vt.setExpiryDate(LocalDateTime.now().plusHours(24));
        tokenRepository.save(vt);

        String link = frontendUrl + "/verify?token=" + vt.getToken();
        emailService.sendVerificationEmail(user.getEmail(), link);
    }

    public void verifyEmail(String token) {
        VerificationToken vt = tokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("This verification link is invalid or already used."));

        if (vt.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("This verification link has expired. Please sign up again.");
        }

        User user = vt.getUser();
        user.setEmailVerified(true);
        userRepository.save(user);
        tokenRepository.delete(vt);
    }

    public LoginResponse login(LoginRequest req) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword()));

        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
        return new LoginResponse(token, user.getRole().name(), user.getName(), user.getId());
    }
}