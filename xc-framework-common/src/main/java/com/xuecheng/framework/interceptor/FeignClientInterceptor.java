package com.xuecheng.framework.interceptor;

import com.sun.org.apache.bcel.internal.generic.I2F;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;

public class FeignClientInterceptor implements RequestInterceptor{
    @Override
    public void apply(RequestTemplate requestTemplate) {
        ServletRequestAttributes requestAttributes = null;
        try {
            requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (requestAttributes!= null){
                HttpServletRequest request = requestAttributes.getRequest();
                Enumeration<String> headerNames = request.getHeaderNames();
                if (headerNames != null){
                    while (headerNames.hasMoreElements()){
                        String name = headerNames.nextElement();
                        String values = request.getHeader(name);
                        if(name.equals("authorization")){
                            requestTemplate.header(name, values);
                        }

                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}
