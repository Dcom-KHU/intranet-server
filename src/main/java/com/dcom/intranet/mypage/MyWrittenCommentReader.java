package com.dcom.intranet.mypage;

import com.dcom.intranet.mypage.dto.MyWrittenCommentDeleteResponse;
import com.dcom.intranet.mypage.dto.MyWrittenCommentListResponse;
import com.dcom.intranet.mypage.dto.MyWrittenCommentTargetResponse;

public interface MyWrittenCommentReader {

    MyWrittenCommentListResponse read(Long userId, int page, int size, String type);

    MyWrittenCommentTargetResponse readTarget(Long userId, Long commentId, String type);

    MyWrittenCommentDeleteResponse delete(Long userId, Long commentId, String type);
}
