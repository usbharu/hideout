insert into "actors" (id, name, domain, screen_name, description, inbox, outbox, url, public_key, private_key,
                      created_at, key_id, following, followers, instance, locked, following_count, followers_count,
                      posts_count, last_post_at)
VALUES (6, 'test-user6', 'example.com', 'Im test-user6.', 'THis account is test-user6.',
        'https://example.com/users/test-user6/inbox',
        'https://example.com/users/test-user6/outbox', 'https://example.com/users/test-user6',
        '-----BEGIN PUBLIC KEY-----...-----END PUBLIC KEY-----',
        '-----BEGIN PRIVATE KEY-----...-----END PRIVATE KEY-----', 12345678,
        'https://example.com/users/test-user6#pubkey', 'https://example.com/users/test-user6/following',
        'https://example.com/users/test-user6/followers', 0, false, 0, 0, 0, null),
       (7, 'test-user7', 'follower.example.com', 'Im test-user7.', 'THis account is test-user7.',
        'https://follower.example.com/users/test-user7/inbox',
        'https://follower.example.com/users/test-user7/outbox', 'https://follower.example.com/users/test-user7',
        '-----BEGIN PUBLIC KEY-----...-----END PUBLIC KEY-----',
        null, 12345678,
        'https://follower.example.com/users/test-user7#pubkey',
        'https://follower.example.com/users/test-user7/following',
        'https://follower.example.com/users/test-user7/followers', 0, false, 0, 0, 0, null);

insert into relationships (actor_id, target_actor_id, following, blocking, muting, follow_request,
                           ignore_follow_request)
VALUES (7, 6, true, false, false, false, false);

insert into POSTS (ID, "actor_ID", OVERVIEW, CONTENT, TEXT, "CREATED_AT", VISIBILITY, URL, "REPOST_ID", "REPLY_ID",
                   SENSITIVE,
                   AP_ID)
VALUES (1238, 6, null, '<p>test post</p>', 'test post', 12345680, 1, 'https://example.com/users/test-user6/posts/1238',
        null, null, false,
        'https://example.com/users/test-user6/posts/1238');
