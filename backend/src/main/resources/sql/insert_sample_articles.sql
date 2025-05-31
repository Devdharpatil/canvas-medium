-- Insert sample articles for testing the article feed
-- Ensure that templates and users exist before inserting articles

-- Sample Article 1: Blog Post
INSERT INTO article (title, content, preview_text, thumbnail_url, template_id, user_id, status, created_at, updated_at, published_at)
SELECT
    'Exploring the Digital Frontier',
    '{"type": "container", "elements": [{"type": "HEADER", "text": "My First Blog Post"}, {"type": "TEXT", "text": "This is the content of my very first blog post. It''s exciting!"}]}',
    'A deep dive into the latest trends in digital technology and their impact on society.',
    'https://picsum.photos/seed/digitalfrontier/600/400',
    (SELECT id FROM template WHERE name = 'Blog Post'),
    (SELECT id FROM users WHERE username = 'uitestuser'), -- Assuming 'uitestuser' exists from authentication flow
    'PUBLISHED',
    CURRENT_TIMESTAMP - INTERVAL '5 days',
    CURRENT_TIMESTAMP - INTERVAL '5 days',
    CURRENT_TIMESTAMP - INTERVAL '5 days'
WHERE NOT EXISTS (SELECT 1 FROM article WHERE title = 'Exploring the Digital Frontier');

-- Sample Article 2: Photo Gallery
INSERT INTO article (title, content, preview_text, thumbnail_url, template_id, user_id, status, created_at, updated_at, published_at)
SELECT
    'Journey Through Scenic Vistas',
    '{"type": "container", "elements": [{"type": "HEADER", "text": "Beautiful Landscapes"}, {"type": "IMAGE", "url": "https://via.placeholder.com/600x400?text=Landscape+1"}, {"type": "TEXT", "text": "A stunning view from the mountains."}]}',
    'Capturing the breathtaking beauty of nature through a series of stunning photographs.',
    'https://picsum.photos/seed/scenicvistas/600/400',
    (SELECT id FROM template WHERE name = 'Photo Gallery'),
    (SELECT id FROM users WHERE username = 'uitestuser'),
    'PUBLISHED',
    CURRENT_TIMESTAMP - INTERVAL '10 days',
    CURRENT_TIMESTAMP - INTERVAL '10 days',
    CURRENT_TIMESTAMP - INTERVAL '10 days'
WHERE NOT EXISTS (SELECT 1 FROM article WHERE title = 'Journey Through Scenic Vistas');

-- Sample Article 3: Tutorial
INSERT INTO article (title, content, preview_text, thumbnail_url, template_id, user_id, status, created_at, updated_at, published_at)
SELECT
    'Mastering Java: A Developer''s Guide',
    '{"type": "container", "elements": [{"type": "HEADER", "text": "How to Master Java"}, {"type": "TEXT", "text": "A comprehensive guide to becoming proficient in Java programming."}]}',
    'Unlock your potential as a Java developer with this in-depth tutorial covering core concepts and advanced techniques.',
    'https://picsum.photos/seed/javatutorial/600/400',
    (SELECT id FROM template WHERE name = 'Tutorial'),
    (SELECT id FROM users WHERE username = 'uitestuser'),
    'PUBLISHED',
    CURRENT_TIMESTAMP - INTERVAL '15 days',
    CURRENT_TIMESTAMP - INTERVAL '15 days',
    CURRENT_TIMESTAMP - INTERVAL '15 days'
WHERE NOT EXISTS (SELECT 1 FROM article WHERE title = 'Mastering Java: A Developer''s Guide');

-- Sample Article 4: Quote
INSERT INTO article (title, content, preview_text, thumbnail_url, template_id, user_id, status, created_at, updated_at, published_at)
SELECT
    'Inspirational Quotes for Success',
    '{"type": "container", "elements": [{"type": "QUOTE", "text": "The only way to do great work is to love what you do."}, {"type": "TEXT", "text": "- Steve Jobs"}]}',
    'A collection of powerful quotes to motivate and inspire you on your journey to success.',
    'https://picsum.photos/seed/inspiration/600/400',
    (SELECT id FROM template WHERE name = 'Quote'),
    (SELECT id FROM users WHERE username = 'uitestuser'),
    'PUBLISHED',
    CURRENT_TIMESTAMP - INTERVAL '20 days',
    CURRENT_TIMESTAMP - INTERVAL '20 days',
    CURRENT_TIMESTAMP - INTERVAL '20 days'
WHERE NOT EXISTS (SELECT 1 FROM article WHERE title = 'Inspirational Quotes for Success');

-- Sample Article 5: Another Blog Post
INSERT INTO article (title, content, preview_text, thumbnail_url, template_id, user_id, status, created_at, updated_at, published_at)
SELECT
    'Artificial Intelligence: A Glimpse into Tomorrow',
    '{"type": "container", "elements": [{"type": "HEADER", "text": "The Future of AI"}, {"type": "TEXT", "text": "Exploring the exciting advancements and ethical considerations in artificial intelligence."}]}',
    'Delve into the transformative power of AI and its potential to reshape industries and daily life.',
    'https://picsum.photos/seed/futureai/600/400',
    (SELECT id FROM template WHERE name = 'Blog Post'),
    (SELECT id FROM users WHERE username = 'uitestuser'),
    'PUBLISHED',
    CURRENT_TIMESTAMP - INTERVAL '25 days',
    CURRENT_TIMESTAMP - INTERVAL '25 days',
    CURRENT_TIMESTAMP - INTERVAL '25 days'
WHERE NOT EXISTS (SELECT 1 FROM article WHERE title = 'Artificial Intelligence: A Glimpse into Tomorrow');
