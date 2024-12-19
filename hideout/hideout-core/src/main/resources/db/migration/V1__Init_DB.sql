/*
 * Copyright (C) 2024 usbharu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Copyright (C) 2024 usbharu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

create table if not exists emojis
(
    id          bigint primary key,
    "name"      varchar(1000) not null,
    domain      varchar(1000) not null,
    instance_id bigint        null,
    url         varchar(255)  not null unique,
    category    varchar(255),
    created_at  timestamp     not null default current_timestamp,
    unique ("name", instance_id)
);

create table if not exists instance
(
    id              bigint primary key,
    "name"          varchar(1000)  not null,
    description     varchar(5000)  not null,
    url             varchar(255)   not null unique,
    icon_url        varchar(255)   not null,
    shared_inbox    varchar(255)   null unique,
    software        varchar(255)   not null,
    version         varchar(255)   not null,
    is_blocked      boolean        not null,
    is_muted        boolean        not null,
    moderation_note varchar(10000) not null,
    created_at      timestamp      not null
);

alter table emojis
    add constraint fk_emojis_instance_id__id foreign key (instance_id) references instance (id) on delete cascade on update cascade;

create table if not exists actors
(
    id              bigint primary key,
    "name"          varchar(300)   not null,
    "domain"        varchar(1000)  not null,
    screen_name     varchar(300)   not null,
    description     varchar(10000) not null,
    inbox           varchar(1000)  not null unique,
    outbox          varchar(1000)  not null unique,
    url             varchar(1000)  not null unique,
    public_key      varchar(10000) not null,
    private_key     varchar(10000) null,
    created_at      timestamp      not null,
    key_id          varchar(1000)  not null,
    "following"     varchar(1000)  null,
    "followers"     varchar(1000)  null,
    "instance"      bigint         not null,
    locked          boolean        not null,
    following_count int            null,
    followers_count int            null,
    posts_count     int            not null,
    last_post_at    timestamp      null     default null,
    last_update_at  timestamp      not null,
    suspend         boolean        not null,
    "move_to"       bigint         null     default null,
    emojis          varchar(3000)  not null default '',
    deleted         boolean        not null default false,
    "icon"          bigint         null,
    "banner"        bigint         null,
    unique ("name", "domain"),
    constraint fk_actors_instance__id foreign key ("instance") references instance (id) on delete restrict on update restrict,
    constraint fk_actors_actors__move_to foreign key ("move_to") references actors (id) on delete restrict on update restrict
);

create table if not exists actor_alsoknownas
(
    "actor_id"      bigint not null,
    "also_known_as" bigint not null,
    constraint fk_actor_alsoknownas_actors__actor_id foreign key ("actor_id") references actors (id) on delete cascade on update cascade,
    constraint fk_actor_alsoknownas_actors__also_known_as foreign key ("also_known_as") references actors (id) on delete cascade on update cascade
);

create table timelines
(
    id             bigint primary key,
    user_detail_id bigint       not null,
    name           varchar(255) not null,
    visibility     varchar(100) not null,
    is_system      boolean      not null default false
);
create table if not exists user_details
(
    id                                  bigint primary key,
    actor_id                            bigint       not null unique,
    password                            varchar(255) not null,
    auto_accept_followee_follow_request boolean      not null,
    last_migration                      timestamp    null default null,
    home_timeline_id                    bigint       null default null,
    constraint fk_user_details_actor_id__id foreign key (actor_id) references actors (id) on delete restrict on update restrict,
    constraint fk_user_details_timelines_id__id foreign key (home_timeline_id) references timelines (id) on delete cascade on update cascade
);

alter table timelines
    add constraint fk_timelines_user_details__user_detail_id foreign key ("user_detail_id") references user_details (id) on delete cascade on update cascade;

create table if not exists media
(
    id            bigint primary key,
    "name"        varchar(255)  not null,
    url           varchar(255)  not null unique,
    remote_url    varchar(255)  null unique,
    thumbnail_url varchar(255)  null unique,
    "type"        varchar(100)  not null,
    blurhash      varchar(255)  null,
    mime_type     varchar(255)  not null,
    description   varchar(4000) null,
    actor_id      bigint        not null,
    constraint fk_media_actor_id__id foreign key (actor_id) references actors (id) on delete restrict on update restrict
);

alter table actors
    add constraint fk_actors_media__icon foreign key ("icon") references media (id) on delete cascade on update cascade;
alter table actors
    add constraint fk_actors_media__banner foreign key ("banner") references media (id) on delete cascade on update cascade;


create table if not exists posts
(
    id          bigint primary key,
    actor_id    bigint                not null,
    instance_id bigint                not null,
    overview    varchar(100)          null,
    content     varchar(5000)         not null,
    text        varchar(3000)         not null,
    created_at  timestamp             not null,
    visibility  varchar(100)          not null,
    url         varchar(500)          not null,
    repost_id   bigint                null,
    reply_id    bigint                null,
    "sensitive" boolean default false not null,
    ap_id       varchar(100)          not null unique,
    deleted     boolean default false not null,
    hide        boolean default false not null,
    move_to     bigint  default null  null
);
alter table posts
    add constraint fk_posts_instance_id__id foreign key (instance_id) references instance (id) on delete cascade on update cascade;
alter table posts
    add constraint fk_posts_actor_id__id foreign key (actor_id) references actors (id) on delete restrict on update restrict;
alter table posts
    add constraint fk_posts_repostid__id foreign key (repost_id) references posts (id) on delete restrict on update restrict;
alter table posts
    add constraint fk_posts_replyid__id foreign key (reply_id) references posts (id) on delete restrict on update restrict;
alter table posts
    add constraint fk_posts_move_to__id foreign key (move_to) references posts (id) on delete CASCADE on update cascade;

create table if not exists posts_media
(
    post_id  bigint,
    media_id bigint,
    constraint pk_postsmedia primary key (post_id, media_id)
);
alter table posts_media
    add constraint fk_posts_media_post_id__id foreign key (post_id) references posts (id) on delete cascade on update cascade;
alter table posts_media
    add constraint fk_posts_media_media_id__id foreign key (media_id) references media (id) on delete cascade on update cascade;

create table if not exists posts_emojis
(
    post_id  bigint not null,
    emoji_id bigint not null,
    constraint pk_postsemoji primary key (post_id, emoji_id)
);

alter table posts_emojis
    add constraint fk_posts_emojis_post_id__id foreign key (post_id) references posts (id) on delete cascade on update cascade;
alter table posts_emojis
    add constraint fk_posts_emojis_emoji_id__id foreign key (emoji_id) references emojis (id) on delete cascade on update cascade;


create table if not exists posts_visible_actors
(
    post_id  bigint not null,
    actor_id bigint not null,
    constraint pk_postsvisibleactors primary key (post_id, actor_id)
);

alter table posts_visible_actors
    add constraint fk_posts_visible_actors_post_id__id foreign key (post_id) references posts (id) on delete cascade on update cascade;
alter table posts_visible_actors
    add constraint fk_posts_visible_actors_actor_id__id foreign key (actor_id) references actors (id) on delete cascade on update cascade;


create table if not exists relationships
(
    id                    bigserial primary key,
    actor_id              bigint  not null,
    target_actor_id       bigint  not null,
    following             boolean not null,
    blocking              boolean not null,
    muting                boolean not null,
    follow_requesting     boolean not null,
    muting_follow_request boolean not null,
    constraint fk_relationships_actor_id__id foreign key (actor_id) references actors (id) on delete restrict on update restrict,
    constraint fk_relationships_target_actor_id__id foreign key (target_actor_id) references actors (id) on delete restrict on update restrict,
    unique (actor_id, target_actor_id)
);

insert into instance (id, "name", description, url, icon_url, shared_inbox, software, version, is_blocked, is_muted,
                      moderation_note, created_at)
values (0, 'system', '', '', '', null, '', '', false, false, '', current_timestamp);

insert into actors (id, "name", "domain", screen_name, description, inbox, outbox, url, public_key, private_key,
                    created_at,
                    key_id, "following", "followers", "instance", locked, following_count, followers_count, posts_count,
                    last_post_at, last_update_at, suspend, "move_to", emojis)
values (0, '', '', '', '', '', '', '', '', null, current_timestamp, '', null, null, 0, true, null, null, 0, null,
        current_timestamp, false, null, '');

create table if not exists applications
(
    id   bigint primary key,
    name varchar(500) not null
);

create table if not exists oauth2_registered_client
(
    id                            varchar(100)                            NOT NULL,
    client_id                     varchar(100)                            NOT NULL,
    client_id_issued_at           timestamp     DEFAULT CURRENT_TIMESTAMP NOT NULL,
    client_secret                 varchar(200)  DEFAULT NULL,
    client_secret_expires_at      timestamp     DEFAULT NULL,
    client_name                   varchar(200)                            NOT NULL,
    client_authentication_methods varchar(1000)                           NOT NULL,
    authorization_grant_types     varchar(1000)                           NOT NULL,
    redirect_uris                 varchar(1000) DEFAULT NULL,
    post_logout_redirect_uris     varchar(1000) DEFAULT NULL,
    scopes                        varchar(1000)                           NOT NULL,
    client_settings               varchar(2000)                           NOT NULL,
    token_settings                varchar(2000)                           NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE if not exists oauth2_authorization_consent
(
    registered_client_id varchar(100)  NOT NULL,
    principal_name       varchar(200)  NOT NULL,
    authorities          varchar(1000) NOT NULL,
    PRIMARY KEY (registered_client_id, principal_name)
);

CREATE TABLE oauth2_authorization
(
    id                            varchar(100) NOT NULL,
    registered_client_id          varchar(100) NOT NULL,
    principal_name                varchar(200) NOT NULL,
    authorization_grant_type      varchar(100) NOT NULL,
    authorized_scopes             varchar(1000) DEFAULT NULL,
    attributes                    varchar(4000) DEFAULT NULL,
    state                         varchar(500)  DEFAULT NULL,
    authorization_code_value      varchar(4000) DEFAULT NULL,
    authorization_code_issued_at  timestamp     DEFAULT NULL,
    authorization_code_expires_at timestamp     DEFAULT NULL,
    authorization_code_metadata   varchar(4000) DEFAULT NULL,
    access_token_value            varchar(4000) DEFAULT NULL,
    access_token_issued_at        timestamp     DEFAULT NULL,
    access_token_expires_at       timestamp     DEFAULT NULL,
    access_token_metadata         varchar(4000) DEFAULT NULL,
    access_token_type             varchar(100)  DEFAULT NULL,
    access_token_scopes           varchar(1000) DEFAULT NULL,
    oidc_id_token_value           varchar(4000) DEFAULT NULL,
    oidc_id_token_issued_at       timestamp     DEFAULT NULL,
    oidc_id_token_expires_at      timestamp     DEFAULT NULL,
    oidc_id_token_metadata        varchar(4000) DEFAULT NULL,
    refresh_token_value           varchar(4000) DEFAULT NULL,
    refresh_token_issued_at       timestamp     DEFAULT NULL,
    refresh_token_expires_at      timestamp     DEFAULT NULL,
    refresh_token_metadata        varchar(4000) DEFAULT NULL,
    user_code_value               varchar(4000) DEFAULT NULL,
    user_code_issued_at           timestamp     DEFAULT NULL,
    user_code_expires_at          timestamp     DEFAULT NULL,
    user_code_metadata            varchar(4000) DEFAULT NULL,
    device_code_value             varchar(4000) DEFAULT NULL,
    device_code_issued_at         timestamp     DEFAULT NULL,
    device_code_expires_at        timestamp     DEFAULT NULL,
    device_code_metadata          varchar(4000) DEFAULT NULL,
    PRIMARY KEY (id)
);

create table if not exists actor_instance_relationships
(
    actor_id            bigint  not null,
    instance_id         bigint  not null,
    blocking            boolean not null,
    muting              boolean not null,
    do_not_send_private boolean not null,
    PRIMARY KEY (actor_id, instance_id),
    constraint fk_actor_instance_relationships_actor_id__id foreign key (actor_id) references actors (id) on delete cascade on update cascade,
    constraint fk_actor_instance_relationships_instance_id__id foreign key (instance_id) references instance (id) on delete cascade on update cascade
);

create table if not exists timeline_relationships
(
    id          bigint primary key,
    timeline_id bigint       not null,
    actor_id    bigint       not null,
    visible     varchar(100) not null,
    constraint fk_timeline_relationships_timeline_id__id foreign key (timeline_id) references timelines (id) on delete cascade on update cascade,
    constraint fk_timeline_relationships_actor_id__id foreign key (actor_id) references actors (id) on delete cascade on update cascade

);

create table if not exists filters
(
    id      bigint primary key,
    user_id bigint       not null,
    name    varchar(255) not null,
    context varchar(500) not null,
    action  varchar(255) not null,
    constraint fk_filters_user_id__id foreign key (user_id) references user_details (id) on delete cascade on update cascade
);

create table if not exists filter_keywords
(
    id        bigint primary key,
    filter_id bigint        not null,
    keyword   varchar(1000) not null,
    mode      varchar(100)  not null,
    constraint fk_filter_keywords_filter_id__id foreign key (filter_id) references filters (id) on delete cascade on update cascade
);

create table if not exists reactions
(
    id              bigint primary key,
    post_id         bigint       not null,
    actor_id        bigint       not null,
    custom_emoji_id bigint       null,
    unicode_emoji   varchar(100) not null,
    created_at      timestamp    not null,
    constraint fk_reactions_post_id__id foreign key (post_id) references posts (id) on delete cascade on update cascade,
    constraint fk_reactions_actor_id__id foreign key (actor_id) references actors (id) on delete cascade on update cascade,
    unique (post_id, actor_id, created_at, unicode_emoji)
);