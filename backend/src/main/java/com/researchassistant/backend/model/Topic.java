package com.researchassistant.backend.model;
import jakarta.persistence.*;
@Entity @Table(name="topics")
public class Topic {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 @Column(nullable=false,unique=true) public String name;
 @Column(columnDefinition="TEXT") public String description;
}
