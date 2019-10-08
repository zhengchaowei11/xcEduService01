package com.xuecheng.auth.service;

import com.alibaba.fastjson.JSON;
import com.xuecheng.framework.client.XcServiceList;
import com.xuecheng.framework.domain.ucenter.ext.AuthToken;
import com.xuecheng.framework.domain.ucenter.response.AuthCode;
import com.xuecheng.framework.exception.ExceptionCast;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.core.token.Token;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class AuthService {
    @Autowired
    private LoadBalancerClient loadBalancerClient;


    @Autowired
    private RestTemplate restTemplate;


    @Autowired
    private StringRedisTemplate stringRedisTemplate;


    @Value("${auth.tokenValiditySeconds}")
    private int  tokenValiditySeconds; //过期时间
    //用户申请令牌将令牌存储到redis 中
    /*
        URI var1, HttpMethod var2, @Nullable HttpEntity<?> var3, Class<T> var4
     */
    public AuthToken login(String clientId, String clientSecret, String username, String password) {
        AuthToken authToken = this.applyToken(clientId, clientSecret, username, password);
        if (authToken == null){
            ExceptionCast.cast(AuthCode.AUTH_LOGIN_APPLYTOKEN_FAIL);
        }
        //得到用户的身份令牌
        String access_token = authToken.getAccess_token();
        //Json的字符串
        String jsonString = JSON.toJSONString(authToken);

        Boolean result = saveToken(access_token, jsonString, tokenValiditySeconds);
        if (!result){
            ExceptionCast.cast(AuthCode.AUTH_LOGIN_TOKEN_SAVEFAIL);
        }

        return authToken;
    }

    private Boolean saveToken(String access_token, String content, long tokenValiditySeconds) {
        String key = "user_token:" + access_token;
        stringRedisTemplate.boundValueOps(key).set(content,tokenValiditySeconds, TimeUnit.SECONDS);
        Long expire = stringRedisTemplate.getExpire(key, TimeUnit.SECONDS);
        return expire >0;
    }


    public AuthToken applyToken(String clientId, String clientSecret, String username, String password){
        ServiceInstance serviceInstance = loadBalancerClient.choose(XcServiceList.XC_SERVICE_UCENTER_AUTH);
        URI uri = serviceInstance.getUri();
        String authUrl = uri+ "/auth/oauth/token";
        MultiValueMap<String,String> body = new LinkedMultiValueMap<>();
        body.add("grant_type","password");
        body.add("username",username);
        body.add("password",password);

        MultiValueMap<String,String> heads = new LinkedMultiValueMap<>();
        String httpBasic = getHttpBasic(clientId, clientSecret);
        heads.add("Authorization",httpBasic);


        HttpEntity<MultiValueMap<String,String>> httpEntity = new HttpEntity<MultiValueMap<String,String>>(body,heads);


        restTemplate.setErrorHandler(new DefaultResponseErrorHandler(){
            @Override
            public void handleError(ClientHttpResponse response) throws IOException {
                if(response.getRawStatusCode()!=400 && response.getRawStatusCode()!=401){
                    super.handleError(response);
                }
            }
        });

        ResponseEntity<Map> responseEntity = restTemplate.exchange(authUrl, HttpMethod.POST, httpEntity, Map.class);
        Map bodyMap = responseEntity.getBody();
        if(bodyMap == null ||
                bodyMap.get("access_token") == null ||
                bodyMap.get("refresh_token") == null ||
                bodyMap.get("jti") == null){
            if (bodyMap != null && bodyMap.get("error_description") != null){
                String error_description = (String) bodyMap.get("error_description");
                if (error_description.indexOf("UserDetailsService returned null") >= 0){
                    ExceptionCast.cast(AuthCode.AUTH_ACCOUNT_NOTEXISTS);
                }else if (error_description.indexOf("坏的凭证") >= 0){
                    ExceptionCast.cast(AuthCode.AUTH_CREDENTIAL_ERROR);
                }
            }
            return null;
        }
        AuthToken authToken = new AuthToken();
        authToken.setAccess_token((String) bodyMap.get("jti"));//用户身份令牌
        authToken.setRefresh_token((String) bodyMap.get("refresh_token"));//刷新令牌
        authToken.setJwt_token((String) bodyMap.get("access_token"));//jwt令牌
        return authToken;
    }

    private String getHttpBasic(String clientId, String clientSecret) {
        String string =  clientId + ":" + clientSecret;
        byte[] encode = Base64Utils.encode(string.getBytes());
        return "Basic " + new String (encode);
    }
    
    //得到用户的Token  从redis中取到数据
    public AuthToken getUserToken(String token) {
        String key = "user_token:" + token;
        //value
        String value = stringRedisTemplate.opsForValue().get(key);
        try {
            AuthToken authToken = JSON.parseObject(value, AuthToken.class);
            return authToken;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public Boolean delToken(String token){
        //如果token 在redis 中就失效了，那么就是删除不成功，但是可能因为数据在redis中失效了，因此直接返回true
        String key = "user_token:" + token;
        Boolean delete = stringRedisTemplate.delete(key);
        return true;
    }
}
