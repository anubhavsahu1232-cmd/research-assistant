package com.researchassistant.backend.model;
import jakarta.persistence.*;
@Entity @Table(name="categories")
public class Category {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 @Column(nullable=false,unique=true) public String name;
 @Column(columnDefinition="TEXT") public String description;
}
