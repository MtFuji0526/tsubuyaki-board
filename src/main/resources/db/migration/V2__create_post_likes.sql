-- =========================================================================
-- 社内つぶやきボード V2: POST_LIKES テーブル
-- Oracle XE 21c および H2(MODE=Oracle) の双方で動く DDL
-- =========================================================================

CREATE SEQUENCE post_likes_seq START WITH 1 INCREMENT BY 1 NOCACHE;

CREATE TABLE post_likes (
    id           NUMBER(19)        NOT NULL,
    post_id      NUMBER(19)        NOT NULL,
    client_hash  VARCHAR2(8 CHAR)  NOT NULL,
    CONSTRAINT post_likes_pk PRIMARY KEY (id),
    CONSTRAINT post_likes_post_fk FOREIGN KEY (post_id) REFERENCES posts (id),
    CONSTRAINT post_likes_post_client_uk UNIQUE (post_id, client_hash)
);

CREATE INDEX post_likes_post_id_idx ON post_likes (post_id);
