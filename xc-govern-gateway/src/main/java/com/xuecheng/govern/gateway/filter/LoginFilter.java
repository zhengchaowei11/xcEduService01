package com.xuecheng.govern.gateway.filter;

import com.alibaba.fastjson.JSON;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.govern.gateway.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class LoginFilter extends ZuulFilter{
    @Autowired
    AuthService authService;
    @Override
    public String filterType() {
        return "pre";
    }
    //执行过滤器的顺序
    @Override
    public int filterOrder() {
        return 0;
    }

    @Override
    //设置过滤器是不是有效
    public boolean shouldFilter() {
        return true;
    }

    @Override
    public Object run() throws ZuulException {
        RequestContext requestContext = RequestContext.getCurrentContext();
        HttpServletResponse response = requestContext.getResponse();
        HttpServletRequest request = requestContext.getRequest();

        String jwt = authService.getJwtFromHeader(request);
        if (StringUtils.isEmpty(jwt)){
            access_denied();
            return null;
        }
        String token = authService.getTokenFromCookie(request);
        if (StringUtils.isEmpty(token)){
            access_denied();
            return null;
        }
        long expire = authService.getExpire(token);
        if (expire<0){
            access_denied();
            return null;
        }
        return null;
    }

    private void access_denied(){
        RequestContext requestContext = RequestContext.getCurrentContext();
        HttpServletResponse response = requestContext.getResponse();
        requestContext.setSendZuulResponse(false);
        requestContext.setResponseStatusCode(200);
        ResponseResult responseResult = new ResponseResult(CommonCode.UNAUTHENTICATED);
        String jsonString = JSON.toJSONString(responseResult);
        requestContext.setResponseBody(jsonString);
        response.setContentType("application/json;charset=utf-8");
    }
}
