package com.example.semilio.token;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SmsTokenVerificationRepository extends JpaRepository<SmsTokenVerification, String> {

    Optional<SmsTokenVerification> findByCode(Integer code);
    boolean existsByCode(Integer code);
}
