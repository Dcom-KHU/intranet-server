package com.dcom.intranet.mypage;

import com.dcom.intranet.common.ApiResponse;
import com.dcom.intranet.mypage.dto.MyProfileApiResponse;
import com.dcom.intranet.mypage.dto.MyProfileResponse;
import com.dcom.intranet.mypage.dto.UnauthorizedApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class MyPageController {

    private final MyPageService myPageService;

    public MyPageController(MyPageService myPageService) {
        this.myPageService = myPageService;
    }

    @Operation(
            summary = "회원정보 조회",
            description = "인증된 사용자의 회원정보를 조회한다.",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "회원정보 조회 성공",
                            content = @Content(schema = @Schema(implementation = MyProfileApiResponse.class))
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "401",
                            description = "인증 실패",
                            content = @Content(schema = @Schema(implementation = UnauthorizedApiResponse.class))
                    )
            }
    )
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<MyProfileResponse>> getMyProfile(Authentication authentication) {
        MyProfileResponse response = myPageService.getMyProfile(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
