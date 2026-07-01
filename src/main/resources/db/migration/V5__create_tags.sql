-- =========================================================================
-- 社内つぶやきボード V5: タグと投稿タグ中間テーブル
-- Oracle XE 21c および H2(MODE=Oracle) の双方で動く DDL
-- =========================================================================

CREATE SEQUENCE tags_seq START WITH 1 INCREMENT BY 1 NOCACHE;

CREATE TABLE tags (
    id    NUMBER(19)        NOT NULL,
    name  VARCHAR2(80 CHAR) NOT NULL,
    CONSTRAINT tags_pk PRIMARY KEY (id),
    CONSTRAINT tags_name_uk UNIQUE (name)
);

CREATE TABLE post_tags (
    post_id  NUMBER(19) NOT NULL,
    tag_id   NUMBER(19) NOT NULL,
    CONSTRAINT post_tags_pk PRIMARY KEY (post_id, tag_id),
    CONSTRAINT post_tags_post_fk FOREIGN KEY (post_id) REFERENCES posts (id),
    CONSTRAINT post_tags_tag_fk FOREIGN KEY (tag_id) REFERENCES tags (id)
);

CREATE INDEX post_tags_tag_id_idx ON post_tags (tag_id);
