package com.xuecheng.govern.gateway.filter;

import com.alibaba.fastjson.JSON;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

//@Component
public class LoginFilterTest extends ZuulFilter{
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
        RequestContext currentContext = RequestContext.getCurrentContext();
        HttpServletResponse response = currentContext.getResponse();//得到页面的返回路径
        HttpServletRequest request = currentContext.getRequest();//得到页面的请求路径
        String authorization = request.getHeader("Authorization");
        if (StringUtils.isEmpty(authorization)){
            //拒绝访问
            currentContext.setSendZuulResponse(false);
            //设置响应的代码
            currentContext.setResponseStatusCode(200);
            //设置响应体
            ResponseResult responseResult = new ResponseResult(CommonCode.UNAUTHENTICATED);
            String toJSONString = JSON.toJSONString(responseResult);
            currentContext.setResponseBody(toJSONString);

            response.setContentType("application/json;charset=utf-8");
            return null;
        }
        return null;
    }
}
