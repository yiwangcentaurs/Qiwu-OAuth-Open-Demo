package com.centaurstech.oauthdemo.resource;

import org.apache.oltu.oauth2.as.issuer.MD5Generator;
import org.apache.oltu.oauth2.as.issuer.OAuthIssuer;
import org.apache.oltu.oauth2.as.issuer.OAuthIssuerImpl;
import org.apache.oltu.oauth2.as.request.OAuthTokenRequest;
import org.apache.oltu.oauth2.as.response.OAuthASResponse;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.OAuthResponse;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author zhazhahui
 * Created on 2019/6/5
 */
@RestController
public class AccessTokenResource {

    /**
     * 获取客户端的code码，向客户端返回access token
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/responseAccessToken", method = RequestMethod.POST)
    public HttpEntity token(HttpServletRequest request) {
        OAuthIssuer oauthIssuerImpl = null;
        OAuthResponse response = null;
        //构建OAuth请求
        try {
            OAuthTokenRequest oauthRequest = new OAuthTokenRequest(request);
            String authCode = oauthRequest.getParam(OAuth.OAUTH_CODE);
            System.out.println("客户端传过来的授权码是----" + authCode);
            String clientSecret = oauthRequest.getClientSecret();
            if (clientSecret != null || clientSecret != "") {
                //生成Access Token
                oauthIssuerImpl = new OAuthIssuerImpl(new MD5Generator());
                final String accessToken = oauthIssuerImpl.accessToken();
                System.out.println(accessToken);
                //生成OAuth响应
                response = OAuthASResponse
                        .tokenResponse(HttpServletResponse.SC_OK)
                        .setAccessToken(accessToken)
                        .buildJSONMessage();
            }
            //根据OAuthResponse生成ResponseEntity
            return new ResponseEntity<String>(response.getBody(),
                    HttpStatus.valueOf(response.getResponseStatus()));
        } catch (OAuthSystemException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (OAuthProblemException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }
}
