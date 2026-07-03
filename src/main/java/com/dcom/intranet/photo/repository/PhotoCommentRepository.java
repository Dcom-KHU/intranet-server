package com.dcom.intranet.photo.repository;

import com.dcom.intranet.photo.domain.PhotoComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PhotoCommentRepository extends JpaRepository<PhotoComment, Long> {

    List<PhotoComment> findByAuthorId(Long authorId);

    Optional<PhotoComment> findByCommentIdAndPhotoPostAlbumId(Long commentId, Long albumId);
}
