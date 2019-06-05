package com.centaurstech.oauthdemo.resource;

import org.apache.oltu.oauth2.as.request.OAuthAuthzRequest;
import org.apache.oltu.oauth2.as.response.OAuthASResponse;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.OAuthResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author zhazhahui
 * Created on 2019/6/5
 */
@RestController
public class AuthorizeResource {

    /**
     * 向客户端返回授权许可码 code，步骤二
     *
     * @param model
     * @param request
     * @return
     */
    @RequestMapping("/responseCode")
    public Object responseCode(Model model, HttpServletRequest request) {
        try {
            //构建OAuth 授权请求
            OAuthAuthzRequest oauthRequest = null;
            try {
                oauthRequest = new OAuthAuthzRequest(request);
            } catch (OAuthProblemException e) {
                e.printStackTrace();
            }
            System.out.println("授权服务器获取的clientID----" + oauthRequest.getClientId());
            System.out.println("返回类型----" + oauthRequest.getResponseType());
            System.out.println("重定向地址---" + oauthRequest.getRedirectURI());
            if (oauthRequest.getClientId() != null && oauthRequest.getClientId() != "") {
                //设置授权码
                String authorizationCode = "authorizationCode";
                //进行OAuth响应构建
                OAuthASResponse.OAuthAuthorizationResponseBuilder builder =
                        OAuthASResponse.authorizationResponse(request, HttpServletResponse.SC_FOUND);
                //设置授权码
                builder.setCode(authorizationCode);
                //得到到客户端重定向地址
                String redirectURI = oauthRequest.getParam(OAuth.OAUTH_REDIRECT_URI);
                //构建响应
                final OAuthResponse response = builder.location(redirectURI).
                        buildQueryMessage();
                String responceUri = response.getLocationUri();
                System.out.println("redirectURI是----" + redirectURI);
                System.out.println("responceUri是----" + responceUri);
                //根据OAuthResponse返回ResponseEntity响应
                HttpHeaders headers = new HttpHeaders();
                try {
                    headers.setLocation(new URI(responceUri));
                } catch (URISyntaxException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                return "redirect:" + responceUri;
            }
        } catch (OAuthSystemException e) {
            e.printStackTrace();
        }
//        catch (OAuthProblemException e) {
//            e.printStackTrace();
//        }
        return null;
    }
}
