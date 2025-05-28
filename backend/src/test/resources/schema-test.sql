-- Drop tables if they exist to ensure clean initialization
DROP TABLE IF EXISTS article;
DROP TABLE IF EXISTS template;

-- Create template table
CREATE TABLE IF NOT EXISTS template (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    layout VARCHAR NOT NULL, -- Changed from JSONB for H2 compatibility
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create article table
CREATE TABLE IF NOT EXISTS article (
    id SERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    content VARCHAR NOT NULL, -- Changed from JSONB for H2 compatibility
    preview_text VARCHAR(500),
    thumbnail_url VARCHAR(255),
    template_id BIGINT REFERENCES template(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_article_template_id ON article(template_id);
CREATE INDEX IF NOT EXISTS idx_article_created_at ON article(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_template_created_at ON template(created_at DESC);
