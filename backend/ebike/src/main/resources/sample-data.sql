-- Sample Data for E-Bike Rental System

-- Insert Sample Users
INSERT INTO users (email, password, first_name, last_name, phone, address, role, is_active, created_at, updated_at) VALUES
('admin@ebike.com', 'admin123', 'Admin', 'User', '9999999999', 'Admin Office', 'ADMIN', true, NOW(), NOW()),
('john.doe@example.com', 'password123', 'John', 'Doe', '9876543210', '123 Main St', 'USER', true, NOW(), NOW()),
('jane.smith@example.com', 'password123', 'Jane', 'Smith', '9876543211', '456 Oak Ave', 'USER', true, NOW(), NOW()),
('mike.johnson@example.com', 'password123', 'Mike', 'Johnson', '9876543212', '789 Pine Rd', 'USER', true, NOW(), NOW()),
('sarah.williams@example.com', 'password123', 'Sarah', 'Williams', '9876543213', '321 Elm St', 'USER', true, NOW(), NOW());

-- Insert Sample Bikes
INSERT INTO bikes (bike_code, model, brand, color, year, type, price_per_hour, price_per_day, status, description, image_url, condition, battery_level, location, created_at, updated_at) VALUES
('BIKE001', 'Mountain Pro X', 'Trek', 'Red', 2024, 'MOUNTAIN', 5.00, 30.00, 'AVAILABLE', 'High-performance mountain bike', 'https://via.placeholder.com/300', 'EXCELLENT', 100, 'Downtown Station', NOW(), NOW()),
('BIKE002', 'City Commute', 'Giant', 'Blue', 2023, 'HYBRID', 3.50, 20.00, 'AVAILABLE', 'Perfect for city commuting', 'https://via.placeholder.com/300', 'EXCELLENT', 95, 'West Park', NOW(), NOW()),
('BIKE003', 'E-Power Plus', 'Specialized', 'Black', 2024, 'ELECTRIC', 7.00, 40.00, 'AVAILABLE', 'Electric assisted bike', 'https://via.placeholder.com/300', 'EXCELLENT', 100, 'Downtown Station', NOW(), NOW()),
('BIKE004', 'Street Rider', 'Scott', 'Green', 2023, 'STANDARD', 2.50, 15.00, 'AVAILABLE', 'Lightweight street bike', 'https://via.placeholder.com/300', 'GOOD', 85, 'East Market', NOW(), NOW()),
('BIKE005', 'Terrain Beast', 'Cannondale', 'Orange', 2024, 'MOUNTAIN', 6.00, 35.00, 'AVAILABLE', 'Rugged mountain explorer', 'https://via.placeholder.com/300', 'EXCELLENT', 98, 'North Ridge', NOW(), NOW()),
('BIKE006', 'Urban Flex', 'Trek', 'White', 2023, 'HYBRID', 3.50, 20.00, 'AVAILABLE', 'Urban mobility solution', 'https://via.placeholder.com/300', 'GOOD', 90, 'Downtown Station', NOW(), NOW()),
('BIKE007', 'Swift Sprint', 'Giant', 'Silver', 2024, 'STANDARD', 2.50, 15.00, 'AVAILABLE', 'Fast and agile', 'https://via.placeholder.com/300', 'EXCELLENT', 100, 'West Park', NOW(), NOW()),
('BIKE008', 'Eco Cruiser', 'Specialized', 'Green', 2023, 'ELECTRIC', 6.50, 38.00, 'AVAILABLE', 'Eco-friendly electric bike', 'https://via.placeholder.com/300', 'EXCELLENT', 92, 'East Market', NOW(), NOW()),
('BIKE009', 'Peak Explorer', 'Scott', 'Purple', 2024, 'MOUNTAIN', 5.50, 32.00, 'AVAILABLE', 'Mountain exploration bike', 'https://via.placeholder.com/300', 'GOOD', 88, 'North Ridge', NOW(), NOW()),
('BIKE010', 'Daily Commuter', 'Cannondale', 'Gray', 2023, 'HYBRID', 3.00, 18.00, 'AVAILABLE', 'Reliable daily commute bike', 'https://via.placeholder.com/300', 'GOOD', 80, 'South Gateway', NOW(), NOW());

-- Insert Sample Bookings
INSERT INTO bookings (user_id, bike_id, start_time, end_time, status, total_price, created_at, updated_at) VALUES
(2, 1, DATE_ADD(NOW(), INTERVAL 2 HOUR), DATE_ADD(NOW(), INTERVAL 4 HOUR), 'CONFIRMED', 10.00, NOW(), NOW()),
(3, 3, DATE_ADD(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 2 DAY), 'PENDING', 40.00, NOW(), NOW()),
(4, 5, DATE_ADD(NOW(), INTERVAL 3 HOUR), DATE_ADD(NOW(), INTERVAL 5 HOUR), 'CONFIRMED', 12.00, NOW(), NOW()),
(5, 2, DATE_ADD(NOW(), INTERVAL 1 HOUR), DATE_ADD(NOW(), INTERVAL 3 HOUR), 'PENDING', 7.00, NOW(), NOW()),
(2, 4, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 12 HOUR), 'COMPLETED', 15.00, NOW(), NOW());

-- Insert Sample Payments
INSERT INTO payments (booking_id, amount, payment_method, payment_status, transaction_id, created_at, updated_at) VALUES
(1, 10.00, 'CREDIT_CARD', 'PENDING', 'TXN20240115001', NOW(), NOW()),
(2, 40.00, 'PAYPAL', 'PENDING', 'TXN20240115002', NOW(), NOW()),
(3, 12.00, 'DEBIT_CARD', 'COMPLETED', 'TXN20240115003', NOW(), NOW()),
(4, 7.00, 'WALLET', 'PENDING', 'TXN20240115004', NOW(), NOW()),
(5, 15.00, 'CREDIT_CARD', 'COMPLETED', 'TXN20240115005', NOW(), NOW());

