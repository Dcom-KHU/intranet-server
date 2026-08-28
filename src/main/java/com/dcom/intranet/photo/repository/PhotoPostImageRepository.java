package com.dcom.intranet.photo.repository;

import com.dcom.intranet.photo.domain.PhotoPostImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;

public interface PhotoPostImageRepository extends JpaRepository<PhotoPostImage, Long> {

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(
            value = """
                    DELETE FROM photo_post_images
                    WHERE album_id = :albumId
                      AND image_id IN (:imageIds)
                    """,
            nativeQuery = true
    )
    int deleteByAlbumIdAndImageIds(
            @Param("albumId") Long albumId,
            @Param("imageIds") Collection<Long> imageIds
    );

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(
            value = """
                    UPDATE photo_post_images
                    SET upload_order = upload_order + 10000
                    WHERE album_id = :albumId
                    """,
            nativeQuery = true
    )
    void shiftUploadOrderForReorder(@Param("albumId") Long albumId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(
            value = """
                    UPDATE photo_post_images target
                    JOIN (
                        SELECT image_id,
                               ROW_NUMBER() OVER (ORDER BY upload_order, image_id) - 1 AS new_upload_order
                        FROM photo_post_images
                        WHERE album_id = :albumId
                    ) ordered_images ON target.image_id = ordered_images.image_id
                    SET target.upload_order = ordered_images.new_upload_order
                    WHERE target.album_id = :albumId
                    """,
            nativeQuery = true
    )
    void reorderUploadOrder(@Param("albumId") Long albumId);
}
