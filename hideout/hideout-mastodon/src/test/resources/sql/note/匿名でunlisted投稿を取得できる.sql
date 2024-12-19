insert into actors (id, name, domain, screen_name, description, inbox, outbox, url, public_key, private_key, created_at,
                    key_id, following, followers, instance, locked, following_count, followers_count, posts_count,
                    last_post_at)
VALUES (2, 'test-user2', 'example.com', 'Im test user2.', 'THis account is test user2.',
        'https://example.com/users/test-user2/inbox',
        'https://example.com/users/test-user2/outbox', 'https://example.com/users/test-user2',
        '-----BEGIN PUBLIC KEY-----...-----END PUBLIC KEY-----',
        '-----BEGIN PRIVATE KEY-----...-----END PRIVATE KEY-----', 12345678,
        'https://example.com/users/test-user2#pubkey', 'https://example.com/users/test-user2/following',
        'https://example.com/users/test-user2/followers', 0, false, 0, 0, 0, null);

insert into POSTS (id, actor_id, overview, content, text, created_at, visibility, url, repost_id, reply_id, sensitive,
                   ap_id,
                   deleted)
VALUES (1235, 2, null, '<p>test post</p>', 'test post', 12345680, 1, 'https://example.com/users/test-user2/posts/1235',
        null, null, false,
        'https://example.com/users/test-user2/posts/1235', false)
