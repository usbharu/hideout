insert into "actors" (id, name, domain, screen_name, description, inbox, outbox, url, public_key, private_key,
                      created_at, key_id, following, followers, instance, locked, following_count, followers_count,
                      posts_count, last_post_at)
VALUES (11, 'test-user11', 'example.com', 'Im test-user11.', 'THis account is test-user11.',
        'https://example.com/users/test-user11/inbox',
        'https://example.com/users/test-user11/outbox', 'https://example.com/users/test-user11',
        '-----BEGIN PUBLIC KEY-----...-----END PUBLIC KEY-----',
        '-----BEGIN PRIVATE KEY-----...-----END PRIVATE KEY-----', 12345678,
        'https://example.com/users/test-user11#pubkey', 'https://example.com/users/test-user11/following',
        'https://example.com/users/test-user11/followers', 0, false, 0, 0, 0, null);

insert into POSTS (id, actor_id, overview, content, text, created_at, visibility, url, repost_id, reply_id, sensitive,
                   ap_id,
                   deleted)
VALUES (1242, 11, null, '<p>test post</p>', 'test post', 12345680, 0,
        'https://example.com/users/test-user11/posts/1242', null, null, false,
        'https://example.com/users/test-user11/posts/1242', false);

insert into MEDIA (ID, NAME, URL, REMOTE_URL, THUMBNAIL_URL, TYPE, BLURHASH, MIME_TYPE, DESCRIPTION)
VALUES (1, 'test-media', 'https://example.com/media/test-media.png', null, null, 0, null, 'image/png', null),
       (2, 'test-media2', 'https://example.com/media/test-media2.png', null, null, 0, null, 'image/png', null);

insert into POSTS_MEDIA(POST_ID, MEDIA_ID)
VALUES (1242, 1),
       (1242, 2);
