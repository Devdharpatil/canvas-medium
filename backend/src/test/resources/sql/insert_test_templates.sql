-- Insert test templates
INSERT INTO templates (id, name, layout, created_at, updated_at)
VALUES 
(1, 'Test Template 1', '{"type":"container","children":[{"type":"text","content":"Sample Text 1"}]}', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'Test Template 2', '{"type":"container","children":[{"type":"text","content":"Sample Text 2"}]}', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 'Test Template 3', '{"type":"container","children":[{"type":"text","content":"Sample Text 3"}]}', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP); 