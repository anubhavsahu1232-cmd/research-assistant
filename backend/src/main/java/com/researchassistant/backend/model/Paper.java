package com.researchassistant.backend.model;
import jakarta.persistence.*;
import java.time.LocalDateTime;
@Entity @Table(name="papers") public class Paper {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 @Column(nullable=false) public String title;
 @Column(name="abstract",columnDefinition="TEXT") public String abstractText;
 public String authors;
 @Column(name="publication_year") public Integer publicationYear;
 public String keywords;
 @Column(name="pdf_file_path") public String pdfFilePath;
 @Column(name="ai_summary",columnDefinition="TEXT") public String aiSummary;
 @Column(name="vector_id") public String vectorId;
 @ManyToOne @JoinColumn(name="uploader_id") public User uploader;
 @ManyToOne @JoinColumn(name="category_id") public Category category;
 @ManyToOne @JoinColumn(name="topic_id") public Topic topic;
 @Column(name="created_at",insertable=false,updatable=false) public LocalDateTime createdAt;
}
