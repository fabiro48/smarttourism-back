package com.smarttourism.backend.experiences.repository;

import com.smarttourism.backend.experiences.entity.Experience;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface ExperienceRepository extends JpaRepository<Experience, UUID>, JpaSpecificationExecutor<Experience> {
}
