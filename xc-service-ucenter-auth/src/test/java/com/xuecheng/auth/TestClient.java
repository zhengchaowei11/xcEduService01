package com.xuecheng.auth;

import com.xuecheng.framework.client.XcServiceList;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Base64Utils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

//SpringBootTest的类
@SpringBootTest
@RunWith(SpringRunner.class)
public class TestClient {
    @Autowired
    RestTemplate restTemplate;//远程调用的方法

    @Autowired
    LoadBalancerClient loadBalancerClient;



    //测试方法
    /*@Test
    public void testClient(){
        //URI url, HttpMethod method, HttpEntity<?> requestEntity, Class<T> responseType
            // url就是 申请令牌的url /oauth/token
        //method http的方法类型
        //requestEntity请求内容
        //responseType，将响应的结果生成的类型

        //从Ereka中获取服务
        //得到一个服务的实例
        ServiceInstance serviceInstance = loadBalancerClient.choose(XcServiceList.XC_SERVICE_UCENTER_AUTH);
        URI uri = serviceInstance.getUri();
        String authUrl = uri+"/auth/oauth/token";

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type","password");
        body.add("username","itcast");
        body.add("password","123");

        MultiValueMap<String,String> heads = new LinkedMultiValueMap<>();
        String httpbasics = httpbasic("XcWebApp", "XcWebApp");

        heads.add("Authorization",httpbasics);
        //泛型是指定在类的后面
        HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(body,heads);

        //设置restTemplate远程调用时候，对400和401不让报错，正确返回数据
        restTemplate.setErrorHandler(new DefaultResponseErrorHandler(){
            @Override
            public void handleError(ClientHttpResponse response) throws IOException {
                if(response.getRawStatusCode()!=400 && response.getRawStatusCode()!=401){
                    super.handleError(response);
                }
            }
        });
        //使用restTemplate的方法进行远程的调用
        ResponseEntity<Map> responseEntity = restTemplate.exchange(authUrl, HttpMethod.POST, httpEntity, Map.class);
        //返回
        Map bodyMap = responseEntity.getBody();
        System.out.println(bodyMap);
    }



    public String testClient1(){
        //从eureka中获取认证服务的地址（因为spring security在认证服务中）
        //从eureka中获取认证服务的一个实例的地址
        ServiceInstance serviceInstance = loadBalancerClient.choose(XcServiceList.XC_SERVICE_UCENTER_AUTH);
        //此地址就是http://ip:port
        URI uri = serviceInstance.getUri();
        //令牌申请的地址 http://localhost:40400/auth/oauth/token
        String authUrl = uri+ "/auth/oauth/token";
        //定义header
        LinkedMultiValueMap<String, String> header = new LinkedMultiValueMap<>();
        String httpBasicsf = httpbasic("XcWebApp", "XcWebApp");
        header.add("Authorization",httpBasicsf);

        //定义body
        LinkedMultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type","password");
        body.add("username","itcast");
        body.add("password","123");

        HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(body, header);
        //String url, HttpMethod method, @Nullable HttpEntity<?> requestEntity, Class<T> responseType, Object... uriVariables

        //设置restTemplate远程调用时候，对400和401不让报错，正确返回数据
        restTemplate.setErrorHandler(new DefaultResponseErrorHandler(){
            @Override
            public void handleError(ClientHttpResponse response) throws IOException {
                if(response.getRawStatusCode()!=400 && response.getRawStatusCode()!=401){
                    super.handleError(response);
                }
            }
        });

        ResponseEntity<Map> exchange = restTemplate.exchange(authUrl, HttpMethod.POST, httpEntity, Map.class);

        //申请令牌信息
        Map bodyMap = exchange.getBody();
        System.out.println(bodyMap);
    }

    @Test
     public void httpbasic(String clientId,String clientSecret){
        String string = clientId+":"+clientSecret;

        //得到字节数组
        byte[] encode = Base64Utils.encode(string.getBytes());

        //将其他类型的数组转化成其他的类型，可以通过构造方法
        String s = new String("Basic " + encode);
        System.out.println(s);
        return s;


    }

*/
    @Test
    public void testPasswordEncode(){
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        String s = "111111";
        for (int i= 0; i<10 ; i++){
            String encode = bCryptPasswordEncoder.encode(s);
            System.out.println(bCryptPasswordEncoder.encode(s));
            boolean matches = bCryptPasswordEncoder.matches(s, encode);
            System.out.println(matches);
        }
    }



}