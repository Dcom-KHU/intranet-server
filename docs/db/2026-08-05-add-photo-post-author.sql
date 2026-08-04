ALTER TABLE photo_posts
    ADD COLUMN author_id BIGINT NULL AFTER album_id;

CREATE INDEX idx_photo_posts_author_id
    ON photo_posts (author_id);

ALTER TABLE photo_posts
    ADD CONSTRAINT fk_photo_posts_author
        FOREIGN KEY (author_id) REFERENCES users (id)
        ON DELETE RESTRICT;
