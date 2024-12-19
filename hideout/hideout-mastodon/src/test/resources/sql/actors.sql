insert into instance(id, name, description, url, icon_url, shared_inbox, software, version, is_blocked, is_muted,
                     moderation_note, created_at)
VALUES (1, 'instance', 'description', 'https://example.com', 'https://example.com', 'https://example.com', 'software',
        'version', false, false, 'note', current_timestamp)
     , (2, 'instance', 'description', 'https://remote.example.com', 'https://example.com', 'https://remote.example.com',
        'software',
        'version', false, false, 'note', current_timestamp)
     , (3, 'instance', 'description', 'https://remote2.example.com', 'https://example.com',
        'https://remote2.example.com', 'software',
        'version', false, false, 'note', current_timestamp);

insert into actors(id, name, domain, screen_name, description, inbox, outbox, url, public_key, private_key, created_at,
                   key_id, following, followers, instance, locked, following_count, followers_count, posts_count,
                   last_post_at, last_update_at, suspend, move_to, icon, banner)
VALUES (1, 'test', 'example.com', 'test-actor', 'actor_description', 'https://example.com/test/inbox',
        'https://example.com/outbox', 'https://example.com/test', '---BEGIN PUBLIC KEY---', '---BEGIN PRIVATE KEY---',
        current_timestamp, 'https://example.com/test#main-key', 'https://example.com/test/following',
        'https://example.com/test/followers', 1, false, 1, 0, 0, null, current_timestamp, false, null, null, null),
       (2, 'test', 'remote.example.com', 'test-actor', 'actor_description', 'https://remote.example.com/test/inbox',
        'https://remote.example.com/outbox', 'https://remote.example.com', '---BEGIN PUBLIC KEY---',
        '---BEGIN PRIVATE KEY---',
        current_timestamp, 'https://remote.example.com/test#main-key', 'https://remote.example.com/test/following',
        'https://remote.example.com/test/followers', 2, false, 1, 0, 0, null, current_timestamp, false, null, null,
        null),
       (3, 'test', 'remote2.example.com', 'test-actor', 'actor_description', 'https://remote2.example.com/test/inbox',
        'https://remote2.example.com/test/outbox', 'https://remote2.example.com/test', '---BEGIN PUBLIC KEY---',
        '---BEGIN PRIVATE KEY---',
        current_timestamp, 'https://remote2.example.com/test#main-key', 'https://remote2.example.com/test/following',
        'https://example.com/followers', 3, false, 1, 0, 0, null, current_timestamp, false, null, null, null),
       (4, 'test2', 'remote2.example.com', 'test-actor', 'actor_description', 'https://example.com/inbox',
        'https://remote2.example.com/test2/outbox', 'https://remote2.example.com/test2', '---BEGIN PUBLIC KEY---',
        '---BEGIN PRIVATE KEY---',
        current_timestamp, 'https://remote2.example.com/test2#main-key', 'https://remote2.example.com/test2/following',
        'https://remote2.example.com/test2/followers', 3, false, 1, 0, 0, null, current_timestamp, false, null, null,
        null);