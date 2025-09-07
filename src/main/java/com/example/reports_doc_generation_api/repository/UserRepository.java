package com.example.reports_doc_generation_api.repository;

import com.example.reports_doc_generation_api.model.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository  extends JpaRepository<UserEntity, Integer> {
    Optional<UserEntity> findByEmail(String email);
}
