insert into relationships(id, actor_id, target_actor_id, following, blocking, muting, follow_requesting,
                          muting_follow_request)
VALUES (1, 1, 2, true, false, false, false, false),
       (2, 2, 1, true, false, false, false, false),
       (3, 1, 3, false, true, false, false, false);