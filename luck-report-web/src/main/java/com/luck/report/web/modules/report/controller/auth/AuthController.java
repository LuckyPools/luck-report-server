package com.luck.report.web.modules.report.controller.auth;

import com.luck.report.infra.modules.servlet.provider.ApiRequest;
import com.luck.report.infra.modules.servlet.utils.HttpUtils;
import com.luck.report.web.common.domain.vo.ResultVO;
import com.luck.report.web.exception.TokenException;
import com.luck.report.web.security.service.TokenService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 报表 Token 申请 controller。
 *
 * @author luck-report
 * @since 1.0.0
 */
@RestController("bean.authController")
@RequestMapping("${luck-report.servletPrefix:}/auth")
public class AuthController {

    private final TokenService tokenService;

    public AuthController(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    /**
     * 申请 token。
     */
    @PostMapping("/get_token")
    public ResultVO<Map<String, Object>> getToken() {
        ApiRequest request = HttpUtils.getRequest();
        String token = tokenService.generateToken(request);
        if (token == null) {
            throw new TokenException("error.token.generateFailed");
        }
        return ResultVO.success(buildTokenData(token));
    }

    private Map<String, Object> buildTokenData(String token) {
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("expiresIn", null);  // 由第三方 TokenService 实现决定过期时间
        data.put("tokenType", "Bearer");
        return data;
    }
}
