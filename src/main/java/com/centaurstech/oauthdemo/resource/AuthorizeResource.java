package com.centaurstech.oauthdemo.resource;

import org.apache.oltu.oauth2.as.request.OAuthAuthzRequest;
import org.apache.oltu.oauth2.as.response.OAuthASResponse;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.OAuthResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
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
     * 授权响应类型，固定为code
     */
    private static final String RESPONSE_TYPE = "code";

    private static final String ACCOUNT_ID = "accountId";

    public static final String AUTHORIZE_URL = "/response/token";

    @Autowired
    AccessTokenResource accessTokenResource;

    /**
     * 向客户端返回授权许可码 code，步骤二
     *
     * @param model
     * @param request
     * @return
     */
    @RequestMapping(value = "/response/code", method = RequestMethod.GET)
    public HttpEntity responseCode(Model model, HttpServletRequest request) {
        try {
            //构建OAuth 授权请求
            OAuthAuthzRequest oauthRequest = null;
            try {
                oauthRequest = new OAuthAuthzRequest(request);
            } catch (OAuthProblemException e) {
                e.printStackTrace();
                return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
            }

            String clientId = oauthRequest.getClientId();
            System.out.println("授权服务器获取的clientID----" + clientId);
            if (validateClientId(clientId)) {

            }
            String type = oauthRequest.getResponseType();
            if (type.equals(RESPONSE_TYPE)) {

            }

            String accountId = oauthRequest.getParam(ACCOUNT_ID);
            if (validateAccountId(accountId)) {

            }

            if (oauthRequest.getClientId() != null && oauthRequest.getClientId() != "") {
                //设置授权码
                int code = (int) ((Math.random() * 9 + 1) * 1000);
                System.out.println(code);
                String authorizationCode = code + "";
                //进行OAuth响应构建
                OAuthASResponse.OAuthAuthorizationResponseBuilder builder =
                        OAuthASResponse.authorizationResponse(request, HttpServletResponse.SC_FOUND);
                //设置授权码
                builder.setCode(authorizationCode);
                String host = accessTokenResource.getHost(request.getRequestURL().toString(), request.getRequestURI());
                builder.setParam("getAccessTokenUrl", host + AUTHORIZE_URL);
                //得到到客户端重定向地址
                String redirectURI = oauthRequest.getParam(OAuth.OAUTH_REDIRECT_URI);
                //构建响应
                final OAuthResponse response = builder.location(redirectURI).
                        buildQueryMessage();
                String responseUri = response.getLocationUri();
                //根据OAuthResponse返回ResponseEntity响应
                HttpHeaders headers = new HttpHeaders();
                try {
                    headers.setLocation(new URI(responseUri));
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
                return new ResponseEntity(headers, HttpStatus.valueOf(response.getResponseStatus()));
            }
        } catch (OAuthSystemException e) {
            e.printStackTrace();
        }
        return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * @param accountId 用户标识->判断用户是否合法-> 合法与授权码绑定->用户与accessToken绑定->accessToken获取用户信息
     * @return 验证结果
     */
    private boolean validateAccountId(String accountId) {
        return true;
    }

    /**
     * @param clientId 验证客户端clientId
     * @return 验证结果
     */
    private boolean validateClientId(String clientId) {

        return true;
    }
}
