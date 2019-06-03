package com.xuecheng.auth.service.impl;

import com.alibaba.fastjson.JSON;
import com.xuecheng.auth.service.AuthService;
import com.xuecheng.framework.client.XcServiceList;
import com.xuecheng.framework.domain.ucenter.ext.AuthToken;
import com.xuecheng.framework.domain.ucenter.response.AuthCode;
import com.xuecheng.framework.exception.ExceptionCast;
import org.bouncycastle.util.encoders.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class AuthServiceImpl implements AuthService {


    @Autowired
    LoadBalancerClient loadBalancerClient;
    @Autowired
    RestTemplate restTemplate;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Value("${auth.tokenValiditySeconds}")
    int tokenValiditySeconds;
    //用户验证
    @Override
    public AuthToken login(String username, String password, String clientId, String clientSecret) {
        //申请令牌
        AuthToken authToken = this.applyToken(username, password, clientId, clientSecret);
        if(authToken==null){
            ExceptionCast.cast(AuthCode.AUTH_LOGIN_AUTHSERVER_NOTFOUND);
        }
        //用户身份的令牌
        String access_token = authToken.getAccess_token();
        //获取用户令牌的内容
        String jsonString = JSON.toJSONString(authToken);
        //将令牌保存到redis中
        boolean saveToken = this.saveToken(access_token, jsonString, tokenValiditySeconds);
        if(!saveToken){
            ExceptionCast.cast(AuthCode.AUTH_LOGIN_TOKEN_SAVEFAIL);
        }
        return authToken;
    }



    /**
     * 将数据存储到redis中
     * @param access_token 用户身份令牌
     * @param content authTonke 对象的数据
     * @param ttl  保存时间
     * @return  成功与否
     */
    private boolean saveToken(String access_token,String content,long ttl){
        //令牌名称
        String name = "user_token:" + access_token;
        //保存到令牌到redis
        stringRedisTemplate.boundValueOps(name).set(content,ttl, TimeUnit.SECONDS);
        //获取过期时间
        Long expire = stringRedisTemplate.getExpire(name);
        return expire>0;
    }

    /**
     *申请令牌
     * @param username 用户名
     * @param password 用户密码
     * @param clientId 客户端id
     * @param clientSecret 客户端密码
     * @return
     */
    private AuthToken applyToken(String username, String password, String clientId, String clientSecret){
        //请求spring serectret
        //采用客户端负载均衡，从eureka获取认证服务的ip 和端口
        ServiceInstance serviceInstance =
                loadBalancerClient.choose(XcServiceList.XC_SERVICE_UCENTER_AUTH);
        URI uri = serviceInstance.getUri();
        String authUrl = uri+"/auth/oauth/token";

        //URI url, HttpMethod method, HttpEntity<?> requestEntity, Class<T> responseType
        // url就是 申请令牌的url /oauth/token
        //method http的方法类型
        //requestEntity请求内容
        //responseType，将响应的结果生成的类型
        //请求的内容分两部分
        //1、header信息，包括了http basic认证信息
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
        String httpbasic = httpbasic(clientId, clientSecret);
        //"Basic WGNXZWJBcHA6WGNXZWJBcHA="
        headers.add("Authorization", httpbasic);
        //2、包括：grant_type、username、passowrd
        MultiValueMap<String, String> body = new LinkedMultiValueMap<String, String>();
        body.add("grant_type","password");
        body.add("username",username);
        body.add("password",password);
        HttpEntity<MultiValueMap<String, String>> multiValueMapHttpEntity = new
                HttpEntity<MultiValueMap<String, String>>(body, headers);
        //指定 restTemplate当遇到400或401响应时候也不要抛出异常，也要正常返回值
        restTemplate.setErrorHandler(new DefaultResponseErrorHandler(){
            @Override
            public void handleError(ClientHttpResponse response) throws IOException {
                //当响应的值为400或401时候也要正常响应，不要抛出异常
                if(response.getRawStatusCode()!=400 && response.getRawStatusCode()!=401){
                    super.handleError(response);
                }
            }
        });
        //远程调用申请令牌
        ResponseEntity<Map> exchange = restTemplate.exchange(authUrl, HttpMethod.POST,
                multiValueMapHttpEntity, Map.class);
        Map body1 = exchange.getBody();
        if(body1==null || body1.get("access_token")==null || body1.get("refresh_token")==null||body1.get("jti")==null){
            //解析screct  返回的错误信息
            if(body1!=null && (String)body1.get("error_description")!=null){
                //账号错误
                String error_description = (String) body1.get("error_description");

                    if(error_description.equals("坏的凭证")){
                        ExceptionCast.cast(AuthCode.AUTH_CREDENTIAL_ERROR);
                    }else if(error_description.indexOf("UserDetailsService returned null")>=0){
                        ExceptionCast.cast(AuthCode.AUTH_ACCOUNT_NOTEXISTS);
                    }
                }
                ExceptionCast.cast(AuthCode.AUTH_LOGIN_APPLYTOKEN_FAIL);

        }

        AuthToken authToken=new AuthToken();
        //用户身份令牌
        authToken.setAccess_token((String) body1.get("jti"));
       //用户jwt  令牌
        authToken.setJwt_token((String) body1.get("access_token"));
        //刷新令牌
        authToken.setRefresh_token((String) body1.get("refresh_token"));
        return authToken;
    }




    /**
     * 生成base64编码
     * @param clientId
     * @param clientSecret
     * @return
     */
    private String httpbasic(String clientId,String clientSecret){
        //将客户端id和客户端密码拼接，按“客户端id:客户端密码”
        String string = clientId+":"+clientSecret;
        //进行base64编码
        byte[] encode = Base64.encode(string.getBytes());
        return "Basic "+new String(encode);
    }


    /**
     * 根据用户唯一认证id 从redis中查询
     * @param cookieToken 用户认证唯一id
     * @return
     */
    @Override
    public AuthToken getUserToken(String cookieToken) {
        String name = "user_token:" + cookieToken;
        //从redis 中取出的令牌信息
        String value = stringRedisTemplate.opsForValue().get(name);
        //转成对象
        try {
            AuthToken authToken = JSON.parseObject(value, AuthToken.class);
            return authToken;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 根据用户唯一标识id  删除从redis用户
     * @param cookieId  用户认证的唯一id
     * @return
     */
    @Override
    public boolean delToken(String cookieId) {
        //令牌名称
        String name = "user_token:" + cookieId;
        //删除 数据
        Boolean delete = stringRedisTemplate.delete(name);
        //根据id 查询数据 是否存在
        Long expire = stringRedisTemplate.getExpire(name, TimeUnit.SECONDS);
        return expire<0;
    }
}
