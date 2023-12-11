insert into "actors" (ID, NAME, DOMAIN, SCREEN_NAME, DESCRIPTION, INBOX, OUTBOX, URL, PUBLIC_KEY, PRIVATE_KEY,
                      CREATED_AT, KEY_ID, FOLLOWING, FOLLOWERS, INSTANCE, LOCKED)
VALUES (8, 'test-user8', 'example.com', 'Im test-user8.', 'THis account is test-user8.',
        'https://example.com/users/test-user8/inbox',
        'https://example.com/users/test-user8/outbox', 'https://example.com/users/test-user8',
        '-----BEGIN PUBLIC KEY-----...-----END PUBLIC KEY-----',
        '-----BEGIN PRIVATE KEY-----...-----END PRIVATE KEY-----', 12345678,
        'https://example.com/users/test-user8#pubkey', 'https://example.com/users/test-user8/following',
        'https://example.com/users/test-user8/followers', null, false),
       (9, 'test-user9', 'follower.example.com', 'Im test-user9.', 'THis account is test-user9.',
        'https://follower.example.com/users/test-user9/inbox',
        'https://follower.example.com/users/test-user9/outbox', 'https://follower.example.com/users/test-user9',
        '-----BEGIN PUBLIC KEY-----...-----END PUBLIC KEY-----',
        null, 12345678,
        'https://follower.example.com/users/test-user9#pubkey',
        'https://follower.example.com/users/test-user9/following',
        'https://follower.example.com/users/test-user9/followers', null, false);

insert into relationships (actor_id, target_actor_id, following, blocking, muting, follow_request,
                           ignore_follow_request)
VALUES (9, 8, true, false, false, false, false);

insert into POSTS (ID, ACTOR_ID, OVERVIEW, TEXT, CREATED_AT, VISIBILITY, URL, REPLY_ID, REPOST_ID, SENSITIVE, AP_ID)
VALUES (1239, 8, null, 'test post', 12345680, 2, 'https://example.com/users/test-user8/posts/1239', null, null, false,
        'https://example.com/users/test-user8/posts/1239');
