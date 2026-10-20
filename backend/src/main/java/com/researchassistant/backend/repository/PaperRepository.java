package com.researchassistant.backend.repository;
import com.researchassistant.backend.model.Paper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
public interface PaperRepository extends JpaRepository<Paper,Long>, JpaSpecificationExecutor<Paper> {}
