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
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.centaurstech.oauthdemo.resource.UserInfoResource.USER_INFO_URL;

/**
 * @author zhazhahui
 * Created on 2019/6/5
 */
@RestController
public class AccessTokenResource {

    /**
     * accessToken 有效期可自由设置
     */
    private static final String EXPIRES_IN = "8400";


    /**
     * 获取客户端的code码，向客户端返回access token
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/response/token", method = RequestMethod.POST)
    public HttpEntity token(HttpServletRequest request) {
        OAuthIssuer oauthIssuerImpl = null;
        OAuthResponse response = null;
        //构建OAuth请求
        try {
            OAuthTokenRequest oauthRequest = null;
            try {
                oauthRequest = new OAuthTokenRequest(request);
            } catch (OAuthProblemException e) {
                e.printStackTrace();
                return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
            }
            String authCode = oauthRequest.getParam(OAuth.OAUTH_CODE);
            System.out.println("客户端传过来的授权码是----" + authCode);
            String clientSecret = oauthRequest.getClientSecret();
            if (!StringUtils.isEmpty(clientSecret) && validateParam(authCode, clientSecret)) {
                //生成Access Token
                oauthIssuerImpl = new OAuthIssuerImpl(new MD5Generator());
                final String accessToken = oauthIssuerImpl.accessToken();
                final String refreshToken = oauthIssuerImpl.refreshToken();
                String host = getHost(request.getRequestURL().toString(), request.getRequestURI());
                System.out.println(accessToken);
                //生成OAuth响应
                response = OAuthASResponse
                        .tokenResponse(HttpServletResponse.SC_OK)
                        .setAccessToken(accessToken)
                        .setRefreshToken(refreshToken)

                        .setExpiresIn(EXPIRES_IN)
                        .setParam("getUserInfoUrl", host + USER_INFO_URL)
                        .buildJSONMessage();
                //根据OAuthResponse生成ResponseEntity
                return new ResponseEntity<String>(response.getBody(),
                        HttpStatus.valueOf(response.getResponseStatus()));
            }
        } catch (OAuthSystemException e) {
            e.printStackTrace();
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * 验证客户端
     *
     * @param authCode     用户授权码，处理成一次可用
     * @param clientSecret 客户端验证信息
     * @return 验证结果
     */
    private boolean validateParam(String authCode, String clientSecret) {

        return true;
    }

    /**
     * @param url
     * @param uri
     * @return 获取HOST
     */
    public String getHost(String url, String uri) {

        return url.substring(0, url.indexOf(uri));
    }
}
