package com.xuecheng.auth.controller;

import com.xuecheng.api.auth.AuthControllerApi;
import com.xuecheng.auth.service.AuthService;
import com.xuecheng.framework.domain.ucenter.ext.AuthToken;
import com.xuecheng.framework.domain.ucenter.request.LoginRequest;
import com.xuecheng.framework.domain.ucenter.response.AuthCode;
import com.xuecheng.framework.domain.ucenter.response.JwtResult;
import com.xuecheng.framework.domain.ucenter.response.LoginResult;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.framework.utils.CookieUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@RestController
@RequestMapping("/")
public class AuthController implements AuthControllerApi{
    @Value("${auth.clientId}")
    private String clientId;  //客户端的id

    @Value("${auth.clientSecret}")
    private String clientSecret;

    @Value("${auth.cookieDomain}")
    private String cookieDomain;


    @Value("${auth.cookieMaxAge}")
    int cookieMaxAge;

    @Autowired
    private AuthService authService;

    @Override
    @PostMapping("/userlogin")
    public LoginResult login(LoginRequest loginRequest) {
        if (loginRequest == null || StringUtils.isEmpty(loginRequest.getUsername())){
            ExceptionCast.cast(AuthCode.AUTH_ACCOUNT_NOTEXISTS);
        }
        if (StringUtils.isEmpty(loginRequest.getPassword())){
            ExceptionCast.cast(AuthCode.AUTH_PASSWORD_NONE);
        }
        AuthToken token = authService.login(clientId, clientSecret, loginRequest.getUsername(), loginRequest.getPassword());
        String access_token = token.getAccess_token();
        saveCookie(access_token);
        return new LoginResult(CommonCode.SUCCESS,access_token);
    }

    private void saveCookie(String token) {
        HttpServletResponse response = ((ServletRequestAttributes)
                RequestContextHolder.getRequestAttributes()).getResponse();
        CookieUtil.addCookie(response,cookieDomain,"/","uid",token,cookieMaxAge,false);
    }

    @Override
    @PostMapping("/userlogout")
    public ResponseResult logout() {
        String token = this.getTokenFromCookie();
        authService.delToken(token);
        this.deleteToken(token);
        return new ResponseResult(CommonCode.SUCCESS) ;
    }

    @Override
    @GetMapping("/userjwt")
    public JwtResult userjwt() {

        //1.从用户中取到用户的id
        String uid = this.getTokenFromCookie();
        if (uid == null){
            //不能抛出异常，如果抛出异常将展示给用户看，如果得不到用户的cookie,就说明没有登录用户，有不用展示
            return new JwtResult(CommonCode.FAIL,null);
        }
        //2.从中取到用户的redis
        AuthToken userToken = authService.getUserToken(uid);
        if (userToken != null){
            String jwt_token = userToken.getJwt_token();
            return new JwtResult(CommonCode.SUCCESS,jwt_token);
        }
        return null;
    }

    //从cookie中得到的令牌
    private String getTokenFromCookie(){
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        Map<String, String> map = CookieUtil.readCookie(request, "uid");
        if (map!=null && map.get("uid") != null){
            String uid = map.get("uid");
            return uid;
        }
        return null;
    }

    private void  deleteToken(String token){
        HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
        CookieUtil.addCookie(response,cookieDomain,"/","uid",token,0,false);
    }
}
