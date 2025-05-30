-- Insert test user for development and testing
INSERT INTO users (username, email, password, full_name, created_at, updated_at, enabled)
SELECT 
    'uitestuser', 
    'uitest@example.com', 
    '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', -- password123 encoded with BCrypt
    'UI Test User',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    true
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'uitestuser');

-- Insert role for test user
INSERT INTO user_roles (user_id, role)
SELECT 
    id,
    'ROLE_USER'
FROM users
WHERE username = 'uitestuser'
AND NOT EXISTS (SELECT 1 FROM user_roles WHERE user_id = (SELECT id FROM users WHERE username = 'uitestuser') AND role = 'ROLE_USER'); 