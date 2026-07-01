package com.dcom.intranet.global.swagger;

public final class SwaggerExamples {

    private SwaggerExamples() {
    }

    public static final String SUCCESS_200 = """
            {
              "success": true,
              "status": 200,
              "message": "요청이 성공적으로 처리되었습니다.",
              "data": {}
            }
            """;

    public static final String CREATED_201 = """
            {
              "success": true,
              "status": 201,
              "message": "족보가 등록되었습니다.",
              "data": {
                "archiveId": 1,
                "recordIds": [10],
                "createdAt": "2026-06-30T23:59:00"
              }
            }
            """;

    public static final String BAD_REQUEST_400 = """
            {
              "success": false,
              "status": 400,
              "message": "잘못된 요청입니다.",
              "data": null
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

    public static final String FORBIDDEN_403 = """
            {
              "success": false,
              "status": 403,
              "message": "작성자 또는 관리자만 수정/삭제할 수 있습니다.",
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

    public static final String DELETE_SUCCESS_200 = """
            {
              "success": true,
              "status": 200,
              "message": "족보가 삭제되었습니다.",
              "data": null
            }
            """;

    public static final String UPDATE_SUCCESS_200 = """
            {
              "success": true,
              "status": 200,
              "message": "족보가 수정되었습니다.",
              "data": {
                "recordId": 10,
                "updatedAt": "2026-06-30T23:59:00"
              }
            }
            """;

    public static final String ARCHIVE_DETAIL_SUCCESS_200 = """
        {
          "success": true,
          "status": 200,
          "message": "요청이 성공적으로 처리되었습니다.",
          "data": {
            "archiveId": 1,
            "subjectName": "자료구조",
            "professorName": "박교수",
            "records": [
              {
                "recordId": 10,
                "examYear": 2024,
                "semester": "FIRST",
                "examType": "MIDTERM",
                "content": "2024년 1학기 자료구조 중간고사 족보입니다.",
                "createdAt": "2026-06-30T23:59:00",
                "updatedAt": null,
                "author": {
                  "nickname": "하성준"
                },
                "files": [
                  {
                    "fileId": 3,
                    "originalFileName": "자료구조_중간.pdf",
                    "fileUrl": "./uploads/archive/2026/06/sample.pdf"
                  }
                ]
              }
            ]
          }
        }
        """;
}