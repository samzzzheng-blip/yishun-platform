package com.kiss.yishun.auth;

import com.alibaba.fastjson.JSON;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.kiss.yishun.common.Constants;
import com.kiss.yishun.common.Result;
import com.kiss.yishun.common.ResultGenerator;
import com.kiss.yishun.utils.StrUtils;
import lombok.extern.log4j.Log4j2;
import org.apache.shiro.web.filter.authc.BasicHttpAuthenticationFilter;
import org.apache.shiro.web.util.WebUtils;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@Log4j2
public class JwtFilter extends BasicHttpAuthenticationFilter {

    /**
     * 检测Header里Authorization字段
     * 判断是否登录
     */
    @Override
    protected boolean isLoginAttempt(ServletRequest request, ServletResponse response) {
        return !StrUtils.isEmpty(getAuthorization(request));
    }

    /**
     * 返回token
     * @param request
     * @return
     */
    private String getAuthorization(ServletRequest request) {
        HttpServletRequest req = (HttpServletRequest) request;
        return req.getHeader(Constants.HEADER_TOKEN_NAME);
    }

    /**
     * 登录验证
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    @Override
    protected boolean executeLogin(ServletRequest request, ServletResponse response)  {

        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        String authorization = httpServletRequest.getHeader(Constants.HEADER_TOKEN_NAME);

        JwtToken token = new JwtToken(authorization);
        // 提交给realm进行登入，如果错误他会抛出异常并被捕获
        getSubject(request, response).login(token);

        // 如果没有抛出异常则代表登入成功，返回true
        return true;
    }


    /**
     * 是否允许访问
     * @param request
     * @param response
     * @param mappedValue
     * @return
     */
    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
        try {
            if (!isLoginAttempt(request, response)) {
                throw new Exception("请先登录");
            }
            this.executeLogin(request, response);
        } catch (Exception e) {
            String msg = e.getMessage();
            Throwable throwable = e.getCause();
            if (throwable != null && throwable instanceof SignatureVerificationException) {
                msg = "Token或者密钥不正确(" + throwable.getMessage() + ")";
            } else if (throwable != null && throwable instanceof TokenExpiredException) {
                msg = "Token已过期";
            } else {
                if (throwable != null) {
                    msg = throwable.getMessage();
                }
            }
            try {
                this.response401(request, response, msg);
            } catch (IOException e1) {
                log.error("返回Response信息出现IOException异常:" + e1.getMessage());
                e1.printStackTrace();
            }
            return false;
        }
        return true;
    }

    /**
     * 401非法请求
     * @param req
     * @param resp
     */
    private void response401(ServletRequest req, ServletResponse resp, String message) throws IOException {
        HttpServletResponse response = WebUtils.toHttp(resp);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=utf-8");
        PrintWriter out = null;
        out = response.getWriter();

        Result result = ResultGenerator.genFailureResult(Constants.UNLOGIN, message);
        out.write(JSON.toJSONString(result));

    }

    @Override
    protected boolean onAccessDenied(ServletRequest servletRequest, ServletResponse servletResponse) throws Exception {
        // 过滤链终止
        return false;
    }

}