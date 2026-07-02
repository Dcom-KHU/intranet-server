package com.dcom.intranet.info.repository;

import com.dcom.intranet.info.domain.InfoComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InfoCommentRepository extends JpaRepository<InfoComment, Long> {

    List<InfoComment> findByPostIdOrderByCreatedAtAsc(Long postId);

    Optional<InfoComment> findByIdAndPostId(Long commentId, Long postId);
}