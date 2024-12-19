insert into "actors" (id, name, domain, screen_name, description, inbox, outbox, url, public_key, private_key,
                      created_at, key_id, following, followers, instance, locked, following_count, followers_count,
                      posts_count, last_post_at)
VALUES (8, 'test-user8', 'example.com', 'Im test-user8.', 'THis account is test-user8.',
        'https://example.com/users/test-user8/inbox',
        'https://example.com/users/test-user8/outbox', 'https://example.com/users/test-user8',
        '-----BEGIN PUBLIC KEY-----...-----END PUBLIC KEY-----',
        '-----BEGIN PRIVATE KEY-----...-----END PRIVATE KEY-----', 12345678,
        'https://example.com/users/test-user8#pubkey', 'https://example.com/users/test-user8/following',
        'https://example.com/users/test-user8/followers', 0, false, 0, 0, 0, null),
       (9, 'test-user9', 'follower.example.com', 'Im test-user9.', 'THis account is test-user9.',
        'https://follower.example.com/users/test-user9/inbox',
        'https://follower.example.com/users/test-user9/outbox', 'https://follower.example.com/users/test-user9',
        '-----BEGIN PUBLIC KEY-----...-----END PUBLIC KEY-----',
        null, 12345678,
        'https://follower.example.com/users/test-user9#pubkey',
        'https://follower.example.com/users/test-user9/following',
        'https://follower.example.com/users/test-user9/followers', 0, false, 0, 0, 0, null);

insert into relationships (actor_id, target_actor_id, following, blocking, muting, follow_request,
                           ignore_follow_request)
VALUES (9, 8, true, false, false, false, false);

insert into POSTS (ID, ACTOR_ID, OVERVIEW, CONTENT, TEXT, CREATED_AT, VISIBILITY, URL, REPLY_ID, REPOST_ID, SENSITIVE,
                   AP_ID)
VALUES (1239, 8, null, '<p>test post</p>', 'test post', 12345680, 2, 'https://example.com/users/test-user8/posts/1239',
        null, null, false,
        'https://example.com/users/test-user8/posts/1239');
