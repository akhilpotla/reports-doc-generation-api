package com.example.reports_doc_generation_api.repository;

import com.example.reports_doc_generation_api.model.ApiEntity;
import com.example.reports_doc_generation_api.model.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ApiRepository extends JpaRepository<ApiEntity, Integer> {
    public Optional<ApiEntity> findByApiTokenAndStatus(String apiToken, Status status);
}
