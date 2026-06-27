package com.dcom.intranet.archive.dto.response;

import com.dcom.intranet.user.User;
import lombok.Getter;

@Getter
public class ArchiveAuthorResponse {

    private final Long userId;
    private final String studentNumber;
    private final String nickname;

    public ArchiveAuthorResponse(User user) {
        this.userId = user.getId();
        this.studentNumber = user.getStudentNumber();
        this.nickname = user.getNickname();
    }
}