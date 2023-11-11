insert into "USERS" (ID, NAME, DOMAIN, SCREEN_NAME, DESCRIPTION, PASSWORD, INBOX, OUTBOX, URL, PUBLIC_KEY, PRIVATE_KEY,
                     CREATED_AT, KEY_ID, FOLLOWING, FOLLOWERS)
VALUES (2, 'test-user2', 'example.com', 'Im test user2.', 'THis account is test user2.',
        '5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8',
        'https://example.com/users/test-user2/inbox',
        'https://example.com/users/test-user2/outbox', 'https://example.com/users/test-user2',
        '-----BEGIN PUBLIC KEY-----...-----END PUBLIC KEY-----',
        '-----BEGIN PRIVATE KEY-----...-----END PRIVATE KEY-----', 12345678,
        'https://example.com/users/test-user2#pubkey', 'https://example.com/users/test-user2/following',
        'https://example.com/users/test-user2/followers');

insert into POSTS (ID, "userId", OVERVIEW, TEXT, "createdAt", VISIBILITY, URL, "repostId", "replyId", SENSITIVE, AP_ID)
VALUES (1235, 2, null, 'test post', 12345680, 1, 'https://example.com/users/test-user2/posts/1235', null, null, false,
        'https://example.com/users/test-user2/posts/1235')
