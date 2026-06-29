package com.dcom.intranet.mypage;

import com.dcom.intranet.mypage.dto.MyWrittenPostListResponse;

public class EmptyMyWrittenPostReader implements MyWrittenPostReader {

    @Override
    public MyWrittenPostListResponse read(Long userId, int page, int size, String type) {
        return MyWrittenPostListResponse.empty(page, size);
    }
}
