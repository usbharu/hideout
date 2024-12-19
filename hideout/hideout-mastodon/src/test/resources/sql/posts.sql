insert into posts (id, actor_id, instance_id, overview, content, text, created_at, visibility, url, repost_id, reply_id,
                   sensitive, ap_id, deleted, hide, move_to)
values (1, 1, 1, null, 'content', 'text', current_timestamp, 'PUBLIC', 'https://example.com', null, null, false,
        'https://example.com/1', false, false, null),
       (2, 2, 2, null, 'content', 'text', current_timestamp, 'FOLLOWERS', 'https://example.com', null, null, false,
        'https://example.com/2', false, false, null),
       (3, 3, 1, null, 'content', 'text', current_timestamp, 'PUBLIC', 'https://example.com', null, null, false,
        'https://example.com/3', false, false, null),
       (4, 4, 1, null, 'content', 'text', current_timestamp, 'FOLLOWERS', 'https://example.com', null, null, false,
        'https://example.com/4', false, false, null),
       (5, 4, 1, null, 'content', 'text', current_timestamp, 'DIRECT', 'https://example.com', null, null, false,
        'https://example.com/5', false, false, null);

insert into posts_visible_actors(post_id, actor_id)
VALUES (5, 2);