package com.dcom.intranet.archive.dto.response;

import com.dcom.intranet.auth.domain.User;
import lombok.Getter;

@Getter
public class ArchiveAuthorResponse {

    private final Long userId;
    private final String studentId;
    private final String name;

    public ArchiveAuthorResponse(User user) {
        this.userId = user.getId();
        this.studentId = user.getStudentId();
        this.name = user.getName();
    }
}