CREATE DATABASE IF NOT EXISTS research_assistant_db;
USE research_assistant_db;

CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role ENUM('USER', 'ADMIN') DEFAULT 'USER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT
);

CREATE TABLE IF NOT EXISTS topics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT
);

CREATE TABLE IF NOT EXISTS papers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    abstract TEXT,
    authors VARCHAR(255),
    publication_year INT,
    keywords VARCHAR(255),
    pdf_file_path VARCHAR(255),
    ai_summary TEXT,
    vector_id VARCHAR(255),
    uploader_id BIGINT,
    category_id BIGINT,
    topic_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (uploader_id) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL,
    FOREIGN KEY (topic_id) REFERENCES topics(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS bookmarks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    paper_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (paper_id) REFERENCES papers(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS notes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    paper_id BIGINT NOT NULL,
    content TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (paper_id) REFERENCES papers(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS reading_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    paper_id BIGINT NOT NULL,
    viewed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (paper_id) REFERENCES papers(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS research_drafts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS draft_sections (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    draft_id BIGINT NOT NULL,
    section_name VARCHAR(100),
    content TEXT,
    FOREIGN KEY (draft_id) REFERENCES research_drafts(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS references_table (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    draft_id BIGINT NOT NULL,
    author VARCHAR(255),
    title VARCHAR(255),
    year INT,
    source VARCHAR(255),
    citation_text TEXT,
    FOREIGN KEY (draft_id) REFERENCES research_drafts(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS paper_qa_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    paper_id BIGINT NOT NULL,
    question TEXT NOT NULL,
    ai_answer TEXT NOT NULL,
    asked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (paper_id) REFERENCES papers(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS ai_writing_feedback (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    section_id BIGINT NOT NULL,
    original_text TEXT,
    suggested_text TEXT,
    feedback_reason VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (section_id) REFERENCES draft_sections(id) ON DELETE CASCADE
);


INSERT IGNORE INTO users (id, username, email, password_hash, role) 
VALUES 
(1, 'admin_siddharth', 'admin@college.edu', 'hash_admin_123', 'ADMIN'),
(2, 'student_user', 'student@college.edu', 'hash_student_123', 'USER');

-- 2. Sample Category & Topic
INSERT IGNORE INTO categories (id, name, description) 
VALUES (1, 'Artificial Intelligence', 'Machine Learning, Deep Learning and NLP');

INSERT IGNORE INTO topics (id, name, description) 
VALUES (1, 'Natural Language Processing', 'Research papers on LLMs and Attention Models');

-- 3. Sample Research Paper
INSERT IGNORE INTO papers (id, title, abstract, authors, publication_year, keywords, category_id, topic_id, uploader_id, ai_summary)
VALUES (
    1,
    'Attention Is All You Need',
    'The dominant sequence transduction models are based on complex recurrent or convolutional neural networks...',
    'Vaswani et al.',
    2017,
    'Transformers, Self-Attention, Deep Learning',
    1, 1, 1,
    'This foundational paper introduces the Transformer architecture based entirely on attention mechanisms.'
);