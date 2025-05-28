-- Drop tables if they exist to ensure clean initialization
DROP TABLE IF EXISTS article;
DROP TABLE IF EXISTS template;
DROP TABLE IF EXISTS user_roles;
DROP TABLE IF EXISTS users;

-- Create users table
CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    full_name VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    enabled BOOLEAN NOT NULL DEFAULT TRUE
);

-- Create user_roles table
CREATE TABLE IF NOT EXISTS user_roles (
    id SERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    role VARCHAR(20) NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE (user_id, role)
);

-- Create template table
CREATE TABLE IF NOT EXISTS template (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    layout JSONB NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create article table
CREATE TABLE IF NOT EXISTS article (
    id SERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    content JSONB NOT NULL,
    preview_text VARCHAR(500),
    thumbnail_url VARCHAR(255),
    template_id BIGINT REFERENCES template(id),
    user_id BIGINT REFERENCES users(id),
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    published_at TIMESTAMP
);

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_article_template_id ON article(template_id);
CREATE INDEX IF NOT EXISTS idx_article_created_at ON article(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_template_created_at ON template(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_article_user_id ON article(user_id);
CREATE INDEX IF NOT EXISTS idx_article_status ON article(status); 