package com.dcom.intranet.mypage;

import com.dcom.intranet.mypage.dto.MyWrittenCommentListResponse;

public class EmptyMyWrittenCommentReader implements MyWrittenCommentReader {

    @Override
    public MyWrittenCommentListResponse read(Long userId, int page, int size, String type) {
        return MyWrittenCommentListResponse.empty(page, size);
    }
}
