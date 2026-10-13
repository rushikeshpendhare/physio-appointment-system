package com.btpl.physio.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.btpl.physio.model.VerificationToken;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {

    Optional<VerificationToken> findByToken(String token);
}