package com.dcom.intranet.mypage;

import com.dcom.intranet.mypage.dto.MyWrittenCommentDeleteResponse;
import com.dcom.intranet.mypage.dto.MyWrittenCommentListResponse;
import com.dcom.intranet.mypage.dto.MyWrittenCommentTargetResponse;
import org.springframework.http.HttpStatus;

public class EmptyMyWrittenCommentReader implements MyWrittenCommentReader {

    @Override
    public MyWrittenCommentListResponse read(Long userId, int page, int size, String type) {
        return MyWrittenCommentListResponse.empty(page, size);
    }

    @Override
    public MyWrittenCommentTargetResponse readTarget(Long userId, Long commentId, String type) {
        throw new MyPageApiException(HttpStatus.NOT_FOUND, "작성한 댓글을 찾을 수 없습니다.");
    }

    @Override
    public MyWrittenCommentDeleteResponse delete(Long userId, Long commentId, String type) {
        throw new MyPageApiException(HttpStatus.NOT_FOUND, "작성한 댓글을 찾을 수 없습니다.");
    }
}
