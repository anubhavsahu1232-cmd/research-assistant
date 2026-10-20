package com.researchassistant.backend.repository;
import com.researchassistant.backend.model.Topic;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TopicRepository extends JpaRepository<Topic,Long> {}
