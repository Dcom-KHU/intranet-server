package com.dcom.intranet.info.service;

import com.dcom.intranet.info.domain.InfoComment;
import com.dcom.intranet.info.domain.InfoPost;
import com.dcom.intranet.info.dto.request.InfoCommentCreateRequest;
import com.dcom.intranet.info.dto.request.InfoCommentUpdateRequest;
import com.dcom.intranet.info.dto.response.InfoCommentListResponse;
import com.dcom.intranet.info.dto.response.InfoCommentResponse;
import com.dcom.intranet.info.repository.InfoCommentRepository;
import com.dcom.intranet.info.repository.InfoPostRepository;
import com.dcom.intranet.auth.domain.User;
import com.dcom.intranet.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InfoCommentService {

    private final InfoCommentRepository infoCommentRepository;
    private final InfoPostRepository infoPostRepository;
    private final UserRepository userRepository;

    public InfoCommentListResponse getComments(Long postId) {
        validatePostExists(postId);

        List<InfoComment> comments =
                infoCommentRepository.findByPostIdOrderByCreatedAtAsc(postId);

        return new InfoCommentListResponse(comments);
    }

    @Transactional
    public InfoCommentResponse createComment(
            Long postId,
            InfoCommentCreateRequest request,
            String loginId
    ) {
        InfoPost post = findPost(postId);
        User author = findUser(loginId);

        InfoComment comment = new InfoComment(
                post,
                author,
                request.getContent()
        );

        InfoComment savedComment = infoCommentRepository.saveAndFlush(comment);

        return new InfoCommentResponse(savedComment);
    }

    @Transactional
    public InfoCommentResponse updateComment(
            Long postId,
            Long commentId,
            InfoCommentUpdateRequest request,
            String loginId
    ) {
        validatePostExists(postId);

        InfoComment comment = findCommentInPost(postId, commentId);

        validateAuthor(comment, loginId);

        comment.update(request.getContent());

        infoCommentRepository.flush();

        return new InfoCommentResponse(comment);
    }

    @Transactional
    public void deleteComment(
            Long postId,
            Long commentId,
            String loginId
    ) {
        validatePostExists(postId);

        InfoComment comment = findCommentInPost(postId, commentId);

        validateAuthorOrAdmin(comment, loginId);

        infoCommentRepository.delete(comment);
    }

    private InfoPost findPost(Long postId) {
        return infoPostRepository.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "정보 공유 게시글을 찾을 수 없습니다."
                ));
    }

    private void validatePostExists(Long postId) {
        if (!infoPostRepository.existsById(postId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "정보 공유 게시글을 찾을 수 없습니다."
            );
        }
    }

    private InfoComment findCommentInPost(Long postId, Long commentId) {
        return infoCommentRepository.findByIdAndPostId(commentId, postId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "댓글을 찾을 수 없습니다."
                ));
    }

    private User findUser(String loginId) {
        return userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "사용자를 찾을 수 없습니다."
                ));
    }

    private void validateAuthor(InfoComment comment, String loginId) {
        User user = findUser(loginId);

        if (!comment.isAuthor(user.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "작성자만 댓글을 수정할 수 있습니다."
            );
        }
    }

    private void validateAuthorOrAdmin(InfoComment comment, String loginId) {
        User user = findUser(loginId);

        boolean isAuthor = comment.isAuthor(user.getId());
        boolean isAdmin = user.isAdmin();

        if (!isAuthor && !isAdmin) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "작성자 또는 관리자만 댓글을 삭제할 수 있습니다."
            );
        }
    }
}