insert into "actors" (id, name, domain, screen_name, description, inbox, outbox, url, public_key, private_key,
                      created_at, key_id, following, followers, instance, locked, following_count, followers_count,
                      posts_count, last_post_at)
VALUES (3, 'test-user3', 'example.com', 'Im test user3.', 'THis account is test user3.',
        'https://example.com/users/test-user3/inbox',
        'https://example.com/users/test-user3/outbox', 'https://example.com/users/test-user3',
        '-----BEGIN PUBLIC KEY-----...-----END PUBLIC KEY-----',
        '-----BEGIN PRIVATE KEY-----...-----END PRIVATE KEY-----', 12345678,
        'https://example.com/users/test-user3#pubkey', 'https://example.com/users/test-user3/following',
        'https://example.com/users/test-user3/followers', 0, false, 0, 0, 0, null);

insert into POSTS (id, actor_id, overview, content, text, created_at, visibility, url, repost_id, reply_id, sensitive,
                   ap_id,
                   deleted)
VALUES (1236, 3, null, '<p>test post</p>', 'test post', 12345680, 2, 'https://example.com/users/test-user3/posts/1236',
        null, null, false,
        'https://example.com/users/test-user3/posts/1236', false)
