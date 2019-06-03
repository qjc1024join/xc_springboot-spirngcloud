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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@RestController
public class AuthController implements AuthControllerApi {
    @Autowired
    AuthService authService;
    @Value("${auth.clientId}")
    String clientId;
    @Value("${auth.clientSecret}")
    String clientSecret;
    @Value("${auth.cookieDomain}")
    String cookieDomain;
    @Value("${auth.cookieMaxAge}")
    int cookieMaxAge;
    @Value("${auth.tokenValiditySeconds}")
    int tokenValiditySeconds;


    @Override
    @PostMapping("/userlogin")
    public LoginResult login(LoginRequest loginRequest) {

        //校验账号是否输入
        if(loginRequest == null || StringUtils.isEmpty(loginRequest.getUsername())){
            ExceptionCast.cast(AuthCode.AUTH_USERNAME_NONE);
        }
            //校验密码是否输入
        if(StringUtils.isEmpty(loginRequest.getPassword())){
            ExceptionCast.cast(AuthCode.AUTH_PASSWORD_NONE);
        }

        String username = loginRequest.getUsername();
        String password = loginRequest.getPassword();
        //申请令牌
        AuthToken authToken= authService.login(username,password,clientId,clientSecret);
        //用户身份令牌
        String access_token = authToken.getAccess_token();
        //将令牌存储到cookie
        this.saveCookie(access_token);
        //返回成功
        return new LoginResult(CommonCode.SUCCESS,access_token);
    }

    /**
     * 将数据保存到cookie
     * @param token
     */
    private void saveCookie(String token){
        HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
        //添加cookie 认证令牌，最后一个参数设置为false，表示允许浏览器获取
        CookieUtil.addCookie(response, cookieDomain, "/", "uid", token, cookieMaxAge, false);
    }

    /**
     * 退出
     * @return
     */
    @Override
    @PostMapping("/userlogout")
    public ResponseResult logout() {
        //取出用户中的cookie 身份
        String cookieId=getCookieToken();
        //删除redis 中的token
        boolean token=authService.delToken(cookieId);
        if(!token){
            ExceptionCast.cast(AuthCode.AUTH_LOGOUT_APPLYTOKEN_FAIL);
        }
        //删除cookie 中的数据
        delCookie(cookieId);
        return new ResponseResult(CommonCode.SUCCESS);
    }

    /**
     * 从cookie删除用户唯一token
     */
    private void delCookie(String token){
        HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
        //添加cookie 认证令牌，最后一个参数设置为false，表示允许浏览器获取
        CookieUtil.addCookie(response, cookieDomain, "/", "uid", token, 0, false);
    }

    /**
     * 从cookie中获取数据信息
     * @return
     */
    @Override
    @GetMapping("/userjwt")
    public JwtResult userjwt() {
        //取出cookie中的用户身份令牌
        String cookieToken = getCookieToken();
        if(cookieToken==null){
            return new JwtResult(CommonCode.FAIL,null);
        }
        //拿着身份令牌从redis 中查询jwt 令牌 拿着jwt令牌返回给用户
        AuthToken authToken=authService.getUserToken(cookieToken);
        //将jwt 令牌 返回给用户
        if(authToken!=null){
            String jwt_token = authToken.getJwt_token();
            return new JwtResult(CommonCode.SUCCESS,jwt_token);
        }else{
            return null;
        }

    }

    /**
     * 取出cookie 中的身份令牌
     * @return
     */
    private String getCookieToken(){
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        //获取cookie
        Map<String, String> map = CookieUtil.readCookie(request, "uid");
        if(map!=null&&map.get("uid")!=null){
            String uid=map.get("uid");
            return uid;
        }
        return null;
    }
}
