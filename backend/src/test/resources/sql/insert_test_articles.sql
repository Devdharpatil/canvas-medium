-- Insert test articles
INSERT INTO articles (id, title, content, preview_text, thumbnail_url, template_id, created_at, updated_at)
VALUES 
(1, 'Test Article 1', '{"title":"Article Title 1","body":"Article content 1 goes here"}', 'Preview text for article 1', 'thumbnail1.jpg', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'Test Article 2', '{"title":"Article Title 2","body":"Article content 2 goes here"}', 'Preview text for article 2', 'thumbnail2.jpg', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 'Test Article 3', '{"title":"Article Title 3","body":"Article content 3 goes here"}', 'Preview text for article 3', 'thumbnail3.jpg', 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP); 