insert into "actors" (id, name, domain, screen_name, description, inbox, outbox, url, public_key, private_key,
                      created_at, key_id, following, followers, instance, locked, following_count, followers_count,
                      posts_count, last_post_at)
VALUES (10, 'test-user10', 'example.com', 'Im test-user10.', 'THis account is test-user10.',
        'https://example.com/users/test-user10/inbox',
        'https://example.com/users/test-user10/outbox', 'https://example.com/users/test-user10',
        '-----BEGIN PUBLIC KEY-----...-----END PUBLIC KEY-----',
        '-----BEGIN PRIVATE KEY-----...-----END PRIVATE KEY-----', 12345678,
        'https://example.com/users/test-user10#pubkey', 'https://example.com/users/test-user10/following',
        'https://example.com/users/test-user10/followers', 0, false, 0, 0, 0, null);

insert into POSTS (id, actor_id, overview, content, text, created_at, visibility, url, repost_id, reply_id, sensitive,
                   ap_id,
                   deleted)
VALUES (1240, 10, null, '<p>test post</p>', 'test post', 12345680, 0,
        'https://example.com/users/test-user10/posts/1240', null, null, false,
        'https://example.com/users/test-user10/posts/1240', false),
       (1241, 10, null, '<p>test post</p>', 'test post', 12345680, 0,
        'https://example.com/users/test-user10/posts/1241', null, 1240, false,
        'https://example.com/users/test-user10/posts/1241', false);
