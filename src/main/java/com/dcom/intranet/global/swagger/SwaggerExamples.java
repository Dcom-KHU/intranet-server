package com.dcom.intranet.global.swagger;

public class SwaggerExamples {
    public static final String INFO_POST_LIST_SUCCESS_200 = """
        {
          "success": true,
          "status": 200,
          "message": "요청이 성공적으로 처리되었습니다.",
          "data": {
            "postList": [
              {
                "postId": 1,
                "title": "시간 복잡도 Big-O 핵심 정리",
                "authorId": 1,
                "authorName": "표지훈",
                "createdAt": "2026-06-20T10:00:00",
                "hasFiles": true,
                "fileCount": 2,
                "views": 10
              }
            ],
            "pageInfo": {
              "page": 0,
              "size": 10,
              "totalPages": 1,
              "totalElements": 1
            }
          }
        }
        """;

    public static final String INFO_POST_DETAIL_SUCCESS_200 = """
        {
          "success": true,
          "status": 200,
          "message": "요청이 성공적으로 처리되었습니다.",
          "data": {
            "postId": 1,
            "title": "시간 복잡도 Big-O 핵심 정리",
            "content": "시간 복잡도는 알고리즘 성능을 평가하는 가장 기본적인 기준입니다.",
            "authorId": 1,
            "authorName": "표지훈",
            "createdAt": "2026-06-20T10:00:00",
            "updatedAt": null,
            "views": 11,
            "files": [
              {
                "fileId": 1,
                "originalFileName": "big-o-summary.pdf",
                "fileUrl": "./uploads/info/2026/07/sample.pdf",
                "fileSize": 1000,
                "contentType": "application/pdf"
              }
            ]
          }
        }
        """;

    public static final String UNAUTHORIZED_401 = """
        {
          "success": false,
          "status": 401,
          "message": "인증이 필요합니다.",
          "data": null
        }
        """;

    public static final String NOT_FOUND_404 = """
        {
          "success": false,
          "status": 404,
          "message": "요청한 리소스를 찾을 수 없습니다.",
          "data": null
        }
        """;

    public static final String INFO_POST_CREATE_SUCCESS_201 = """
        {
          "success": true,
          "status": 201,
          "message": "게시글이 작성되었습니다.",
          "data": {
            "postId": 1,
            "title": "시간 복잡도 Big-O 핵심 정리",
            "content": "시간 복잡도는 알고리즘 성능을 평가하는 기준입니다.",
            "authorId": 1,
            "authorName": "표지훈",
            "createdAt": "2026-07-01T10:00:00",
            "files": [
              {
                "fileId": 1,
                "originalFileName": "big-o-summary.pdf",
                "fileUrl": "./uploads/info/2026/07/sample.pdf",
                "fileSize": 1000,
                "contentType": "application/pdf"
              }
            ]
          }
        }
        """;

    public static final String INFO_POST_UPDATE_SUCCESS_200 = """
        {
          "success": true,
          "status": 200,
          "message": "게시글이 수정되었습니다.",
          "data": {
            "postId": 1,
            "title": "시간 복잡도 Big-O 핵심 정리 수정",
            "content": "수정된 본문입니다.",
            "updatedAt": "2026-07-01T23:30:00",
            "files": [
              {
                "fileId": 1,
                "originalFileName": "big-o-summary.pdf",
                "fileUrl": "./uploads/info/2026/07/sample.pdf",
                "fileSize": 1000,
                "contentType": "application/pdf"
              }
            ]
          }
        }
        """;

    public static final String INFO_POST_UPDATE_FORBIDDEN_403 = """
        {
          "success": false,
          "status": 403,
          "message": "작성자만 수정할 수 있습니다.",
          "data": null
        }
        """;

    public static final String INFO_POST_DELETE_SUCCESS_200 = """
        {
          "success": true,
          "status": 200,
          "message": "게시글이 삭제되었습니다.",
          "data": null
        }
        """;

    public static final String INFO_POST_DELETE_FORBIDDEN_403 = """
        {
          "success": false,
          "status": 403,
          "message": "작성자 또는 관리자만 삭제할 수 있습니다.",
          "data": null
        }
        """;

    public static final String INFO_POST_BAD_REQUEST_400 = """
        {
          "success": false,
          "status": 400,
          "message": "잘못된 요청입니다.",
          "data": null
        }
        """;

    public static final String INFO_POST_NOT_FOUND_404 = """
        {
          "success": false,
          "status": 404,
          "message": "정보 공유 게시글을 찾을 수 없습니다.",
          "data": null
        }
        """;
}
