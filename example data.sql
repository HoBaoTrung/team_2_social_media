use social_media;

-- Chèn dữ liệu mẫu vào bảng admin_role
INSERT INTO admin_roles (name, description) VALUES
('ADMIN', 'Quyền quản trị cao nhất, quản lý tất cả các chức năng'),
('MODERATOR', 'Quyền kiểm duyệt nội dung và quản lý người dùng'),
('ANALYST', 'Quyền phân tích dữ liệu và tạo báo cáo');

-- Chèn dữ liệu mẫu vào bảng admin
INSERT INTO admin (username, email, password_hash, full_name, role_id, created_at, updated_at, is_active, last_login) VALUES
('admin1', 'admin1@example.com', '$2a$10$iKhoJT9s34MM8QyX.mJIqOCHj83Om3KfQVMO0B3XB4O.eAw8Dwpau', 'Nguyen Van A', 1, '2025-08-01 10:00:00', '2025-08-01 10:00:00', TRUE, '2025-08-09 08:00:00'),
('moderator1', 'moderator1@example.com', '$2a$10$iKhoJT9s34MM8QyX.mJIqOCHj83Om3KfQVMO0B3XB4O.eAw8Dwpau', 'Tran Thi B', 2, '2025-08-02 12:00:00', '2025-08-02 12:00:00', TRUE, '2025-08-09 09:00:00'),
('analyst1', 'analyst1@example.com', '$2a$10$iKhoJT9s34MM8QyX.mJIqOCHj83Om3KfQVMO0B3XB4O.eAw8Dwpau', 'Le Van C', 3, '2025-08-03 14:00:00', '2025-08-03 14:00:00', FALSE, '2025-08-09 09:00:00');



INSERT INTO users (
    username, email, password_hash, first_name, last_name,
    profile_picture, bio, date_of_birth, gender, phone,
    login_method, is_active, is_verified, account_status,
	 created_at
)
VALUES
('john_doe', 'john@example.com', '$2a$10$oNtne4qiFdVyKsFC.S.xx.Pl9Zxhl65krYilyBrvzk4HeHLE1ExkK', 'John', 'Doe',
 null, 'Hello, I am John.', '1990-05-15', 'MALE', '0123456789',
 'EMAIL', true, true, 'ACTIVE',   '2025-08-01'),

('jane_smith', 'jane@example.com', '$2a$10$oNtne4qiFdVyKsFC.S.xx.Pl9Zxhl65krYilyBrvzk4HeHLE1ExkK', 'Jane', 'Smith',
 null, 'Photographer & blogger.', '1992-08-25', 'FEMALE', '0987654321',
 'GOOGLE', true, true, 'ACTIVE',  '2025-08-01'),

('alex_taylor', 'alex@example.com', '$2a$10$oNtne4qiFdVyKsFC.S.xx.Pl9Zxhl65krYilyBrvzk4HeHLE1ExkK', 'Alex', 'Taylor',
 NULL, 'Love coding and coffee.', '1995-12-03', 'OTHER', NULL,
 'FACEBOOK', false, false, 'PENDING', '2025-08-01');
 
 
 INSERT INTO users (
    username, email, password_hash, first_name, last_name,
    gender, login_method, is_active, is_verified, account_status,
    created_at
)
VALUES
   ('mike_jones', 'mike.jones@example.com', '$2a$10$DEF789', 'Mike', 'Jones', 'MALE', 'FACEBOOK', true, false, 'ACTIVE', '2023-03-05 14:45:00'),
    ('sarah_wilson', 'sarah.wilson@example.com', '$2a$10$GHI012', 'Sarah', 'Wilson', 'FEMALE', 'email', true, true, 'ACTIVE', '2023-04-10 09:20:00'),
    ('david_brown', 'david.brown@example.com', '$2a$10$JKL345', 'David', 'Brown', 'MALE', 'EMAIL', false, true, 'ACTIVE', '2023-05-15 16:30:00'),
    ('robert_garcia', 'robert.garcia@example.com', '$2a$10$PQR901', 'Robert', 'Garcia', 'MALE', 'EMAIL', true, false, 'ACTIVE', '2023-07-25 13:10:00'),
    ('emily_martin', 'emily.martin@example.com', '$2a$10$STU234', 'Emily', 'Martin', 'FEMALE', 'FACEBOOK', true, true, 'ACTIVE', '2023-08-30 15:25:00'),
    ('thomas_clark', 'thomas.clark@example.com', '$2a$10$VWX567', 'Thomas', 'Clark', 'MALE', 'EMAIL', false, false, 'ACTIVE', '2023-09-05 18:40:00'),
    ('amanda_lee', 'amanda.lee@example.com', '$2a$10$YZA890', 'Amanda', 'Lee', 'FEMALE', 'GOOGLE', true, true, 'ACTIVE', '2023-10-10 20:55:00');
 
 INSERT INTO users (
    username, email, password_hash, first_name, last_name, 
    gender, login_method, is_active, is_verified, account_status, 
    created_at
) VALUES
('user1', 'user1@example.com', '$2a$10$abc123hashedpassword1', 'Nguyen', 'Van A', 'MALE', 'EMAIL', 1, 1, 'ACTIVE', '2025-08-07 19:00:00'),
('user2', 'user2@example.com', '$2a$10$def456hashedpassword2', 'Tran', 'Thi B', 'FEMALE', 'GOOGLE', 1, 0, 'ACTIVE', '2025-08-07 19:01:00'),
('user3', 'user3@example.com', '$2a$10$ghi789hashedpassword3', 'Le', 'Van C', 'MALE', 'EMAIL', 1, 1, 'ACTIVE', '2025-08-07 19:02:00'),
('user4', 'user4@example.com', '$2a$10$jkl012hashedpassword4', 'Pham', 'Thi D', 'FEMALE', 'EMAIL', 0, 0, 'ACTIVE', '2025-08-07 19:03:00'),
('user5', 'user5@example.com', '$2a$10$mno345hashedpassword5', 'Ho', 'Van E', 'MALE', 'EMAIL', 1, 1, 'ACTIVE', '2025-08-07 19:04:00'),
('user6', 'user6@example.com', '$2a$10$pqr678hashedpassword6', 'Vo', 'Thi F', 'FEMALE', 'GOOGLE', 1, 1, 'ACTIVE', '2025-08-07 19:05:00'),
('user7', 'user7@example.com', '$2a$10$stu901hashedpassword7', 'Bui', 'Van G', 'MALE', 'EMAIL', 0, 0, 'ACTIVE', '2025-08-07 19:06:00'),
('user8', 'user8@example.com', '$2a$10$vwx234hashedpassword8', 'Dang', 'Thi H', 'FEMALE', 'facebook', 1, 0, 'ACTIVE', '2025-08-07 19:07:00'),
('user9', 'user9@example.com', '$2a$10$yza567hashedpassword9', 'Ngo', 'Van I', 'MALE', 'EMAIL', 1, 1, 'ACTIVE', '2025-08-07 19:08:00'),
('user10', 'user10@example.com', '$2a$10$bcd890hashedpassword10', 'Do', 'Thi K', 'FEMALE', 'GOOGLE', 1, 1, 'ACTIVE', '2025-08-07 19:09:00');
 
 UPDATE users 
SET profile_picture = 'https://res.cloudinary.com/dryyvmkwo/image/upload/v1748588721/samples/landscapes/nature-mountains.jpg'
WHERE id BETWEEN 1 AND 20;
 
 INSERT INTO friendships (`status`, addressee_id, requester_id) VALUES
 ('ACCEPTED',1,2),
 ('ACCEPTED',1,3),
 ('ACCEPTED',3,2);
 
 INSERT INTO user_privacy_settings (
    user_id,
    show_profile,
    show_friend_list,
    show_full_name,
    show_address,
    show_phone,
    show_email,
    show_avatar,
    show_bio,
    show_dob,
    allow_send_message,
    allow_search_by_email,
    allow_search_by_phone,
    can_be_found,
    allow_friend_requests
) VALUES 
(
    1, -- User 1
    'PUBLIC',
    'PUBLIC',
    'PUBLIC',
    'PRIVATE',
    'PRIVATE',
    'PRIVATE',
    'PUBLIC',
    'PUBLIC',
    'PRIVATE',
    'FRIENDS',
    true,
    true,
    true,
    true
),
(
    2, -- User 2
    'FRIENDS',
    'FRIENDS',
    'PUBLIC',
    'PRIVATE',
    'PRIVATE',
    'FRIENDS',
    'PUBLIC',
    'FRIENDS',
    'PRIVATE',
    'FRIENDS',
    false,
    true,
    true,
    false
),
(
    3, -- User 3
    'FRIENDS',
    'FRIENDS',
    'PUBLIC',
    'PRIVATE',
    'PRIVATE',
    'FRIENDS',
    'PUBLIC',
    'FRIENDS',
    'PRIVATE',
    'FRIENDS',
    false,
    true,
    true,
    false
);



INSERT INTO user_privacy_settings (
    user_id,show_profile,
    show_friend_list,show_full_name,
    show_address,show_phone,show_email,show_avatar,show_bio,
    show_dob, allow_send_message,
    allow_search_by_email,allow_search_by_phone,
    can_be_found,allow_friend_requests
) VALUES 
(4, 'PUBLIC', 'FRIENDS', 'PUBLIC', 'PRIVATE', 'PRIVATE', 'PRIVATE', 'PUBLIC', 'PUBLIC', 'FRIENDS', 'FRIENDS', true, false, true, true),
(5, 'FRIENDS', 'FRIENDS', 'FRIENDS', 'PRIVATE', 'PRIVATE', 'PRIVATE', 'FRIENDS', 'FRIENDS', 'PRIVATE', 'PUBLIC', false, false, true, true),
(12, 'PUBLIC', 'FRIENDS', 'PUBLIC', 'PRIVATE', 'PRIVATE', 'PRIVATE', 'PUBLIC', 'PUBLIC', 'FRIENDS', 'FRIENDS', true, false, true, true),
(13, 'FRIENDS', 'FRIENDS', 'FRIENDS', 'PRIVATE', 'PRIVATE', 'PRIVATE', 'FRIENDS', 'FRIENDS', 'PRIVATE', 'PUBLIC', false, false, true, true),
(6, 'PUBLIC', 'FRIENDS', 'PUBLIC', 'PRIVATE', 'FRIENDS', 'PRIVATE', 'PUBLIC', 'FRIENDS', 'FRIENDS', 'FRIENDS', true, true, true, true),
(7, 'FRIENDS', 'PRIVATE', 'FRIENDS', 'PRIVATE', 'PRIVATE', 'FRIENDS', 'PUBLIC', 'PUBLIC', 'PRIVATE', 'PUBLIC', false, true, true, false),
(8, 'PUBLIC', 'PUBLIC', 'PUBLIC', 'FRIENDS', 'PRIVATE', 'PRIVATE', 'FRIENDS', 'FRIENDS', 'FRIENDS', 'FRIENDS', true, false, true, true),
(9, 'PRIVATE', 'FRIENDS', 'PRIVATE', 'PRIVATE', 'PRIVATE', 'PRIVATE', 'PRIVATE', 'PRIVATE', 'PRIVATE', 'PRIVATE', false, false, false, false),
(10, 'PUBLIC', 'FRIENDS', 'FRIENDS', 'PRIVATE', 'FRIENDS', 'FRIENDS', 'PUBLIC', 'PUBLIC', 'FRIENDS', 'PUBLIC', true, true, true, true),
(11, 'FRIENDS', 'PUBLIC', 'PUBLIC', 'PRIVATE', 'PRIVATE', 'PRIVATE', 'FRIENDS', 'FRIENDS', 'PRIVATE', 'FRIENDS', false, true, true, false),
  (14, 'PUBLIC', 'PRIVATE', 'PUBLIC', 'PRIVATE', 'PRIVATE', 'FRIENDS', 'PUBLIC', 'PUBLIC', 'PRIVATE', 'PUBLIC', true, false, true, true),
    (15, 'PRIVATE', 'PRIVATE', 'FRIENDS', 'PRIVATE', 'PRIVATE', 'PRIVATE', 'FRIENDS', 'PRIVATE', 'PRIVATE', 'FRIENDS', false, false, false, false),
    (16, 'PUBLIC', 'FRIENDS', 'PUBLIC', 'FRIENDS', 'PRIVATE', 'FRIENDS', 'PUBLIC', 'PUBLIC', 'FRIENDS', 'FRIENDS', true, true, true, true),
    (17, 'FRIENDS', 'FRIENDS', 'FRIENDS', 'PRIVATE', 'PRIVATE', 'PRIVATE', 'FRIENDS', 'FRIENDS', 'PRIVATE', 'FRIENDS', false, true, true, true),
    (18, 'PUBLIC', 'PUBLIC', 'PUBLIC', 'PRIVATE', 'FRIENDS', 'PUBLIC', 'PUBLIC', 'PUBLIC', 'FRIENDS', 'PUBLIC', true, true, true, true),
    (19, 'PRIVATE', 'PRIVATE', 'PRIVATE', 'PRIVATE', 'PRIVATE', 'PRIVATE', 'PRIVATE', 'PRIVATE', 'PRIVATE', 'PUBLIC', false, false, false, false),
    (20, 'PUBLIC', 'PUBLIC', 'PUBLIC', 'FRIENDS', 'FRIENDS', 'FRIENDS', 'PUBLIC', 'PUBLIC', 'FRIENDS', 'PUBLIC', true, true, true, true);


INSERT INTO posts (user_id, content, image_urls, privacy_level, created_at,is_deleted) VALUES
(1, 'Chào ngày mới! Mọi người có kế hoạch gì cho hôm nay không?', NULL, 'PUBLIC', '2023-10-01 08:15:00',false),
(1, 'Beautiful pictures', '["https://res.cloudinary.com/dryyvmkwo/image/upload/v1748588729/cld-sample-4.jpg", "https://res.cloudinary.com/dryyvmkwo/image/upload/v1748588729/cld-sample-2.jpg"]', 'FRIENDS','2023-10-01 10:30:00',false),
(1, 'Check out my new video!', NULL,  'PUBLIC',  '2023-10-02 15:45:00',false),
(1, 'Cảm ơn mọi người đã chúc mừng sinh nhật tôi!', '["https://res.cloudinary.com/dryyvmkwo/image/upload/v1748588728/samples/dessert-on-a-plate.jpg"]', 'FRIENDS', '2023-10-03 09:20:00',false);


INSERT INTO post_comments (post_id, user_id, content, is_deleted, created_at, updated_at)
VALUES (2, 1, 'Great post! Really enjoyed reading this.', false, '2023-05-10 09:15:22', '2023-05-10 09:15:22'),
 (2, 2, 'Could you elaborate more on the second point?', false, '2023-05-10 11:30:45', '2023-05-10 11:30:45'),
 (2, 3, 'Good', false, '2023-05-11 14:20:10', '2023-05-11 14:25:33'),
(2, 1, 'Thanks for sharing this information!', false, '2023-05-12 08:45:12', '2023-05-12 08:45:12'),
 (2, 4, 'I disagree with some points but appreciate the effort.', false, '2023-05-12 16:30:00', '2023-05-12 16:30:00');
 
INSERT INTO notifications (id, sender_id, receiver_id, notification_type, reference_id, created_at, is_read, reference_type) VALUES
(1, 2, 1, 'LIKE_POST',      1, '2025-08-18 09:10:00', false, 'POST'), -- referenceId=postId
(2, 3, 1, 'COMMENT_POST',   1, '2025-08-18 09:12:00', false, 'COMMENT'), -- comment vào postId=101
(3, 4, 1, 'FRIEND_REQUEST', 2,  '2025-08-18 09:15:00', false, 'FRIENDSHIP'), -- referenceId=friendshipId
(4, 5, 1, 'LIKE_COMMENT',   2, '2025-08-18 09:20:00', true, 'COMMENT'),  -- referenceId=commentId
(5, 6, 1, 'REPLY_COMMENT',  3, '2025-08-18 09:25:00', false, 'COMMENT');










