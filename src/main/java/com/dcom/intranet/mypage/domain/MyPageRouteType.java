package com.dcom.intranet.mypage.domain;

public final class MyPageRouteType {

    private MyPageRouteType() {
    }

    public static String normalize(String type) {
        if (type == null) {
            return null;
        }

        return switch (type) {
            case "INFO_POST" -> "info-posts";
            case "ARCHIVE" -> "archives";
            case "PHOTO_ALBUM", "PHOTO_COMMENT" -> "photo-posts";
            case "NOTICE" -> "notices";
            default -> type;
        };
    }
}
