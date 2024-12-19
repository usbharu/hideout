insert into "actors" (id, name, domain, screen_name, description, inbox, outbox, url, public_key, private_key,
                      created_at, key_id, following, followers, instance, locked, following_count, followers_count,
                      posts_count, last_post_at)
VALUES (4, 'test-user4', 'example.com', 'Im test user4.', 'THis account is test user4.',
        'https://example.com/users/test-user4/inbox',
        'https://example.com/users/test-user4/outbox', 'https://example.com/users/test-user4',
        '-----BEGIN PUBLIC KEY-----...-----END PUBLIC KEY-----',
        '-----BEGIN PRIVATE KEY-----...-----END PRIVATE KEY-----', 12345678,
        'https://example.com/users/test-user4#pubkey', 'https://example.com/users/test-user4/following',
        'https://example.com/users/test-user4/followers', 0, false, 0, 0, 0, null),
       (5, 'test-user5', 'follower.example.com', 'Im test user5.', 'THis account is test user5.',
        'https://follower.example.com/users/test-user5/inbox',
        'https://follower.example.com/users/test-user5/outbox', 'https://follower.example.com/users/test-user5',
        '-----BEGIN PUBLIC KEY-----...-----END PUBLIC KEY-----',
        null, 12345678,
        'https://follower.example.com/users/test-user5#pubkey',
        'https://follower.example.com/users/test-user5/following',
        'https://follower.example.com/users/test-user5/followers', 0, false, 0, 0, 0, null);

insert into relationships (actor_id, target_actor_id, following, blocking, muting, follow_request,
                           ignore_follow_request)
VALUES (5, 4, true, false, false, false, false);

insert into POSTS (ID, "actor_id", OVERVIEW, CONTENT, TEXT, "created_at", VISIBILITY, URL, "repost_id", "reply_id",
                   SENSITIVE,
                   AP_ID)
VALUES (1237, 4, null, '<p>test post</p>', 'test post', 12345680, 0, 'https://example.com/users/test-user4/posts/1237',
        null, null, false,
        'https://example.com/users/test-user4/posts/1237');
