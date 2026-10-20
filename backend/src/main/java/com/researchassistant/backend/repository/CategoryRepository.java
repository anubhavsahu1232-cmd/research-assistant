package com.researchassistant.backend.repository;
import com.researchassistant.backend.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category,Long> {}
