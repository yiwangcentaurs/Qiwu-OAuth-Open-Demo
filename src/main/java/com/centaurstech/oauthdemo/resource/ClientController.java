package com.centaurstech.oauthdemo.resource;

import org.apache.oltu.oauth2.client.OAuthClient;
import org.apache.oltu.oauth2.client.URLConnectionClient;
import org.apache.oltu.oauth2.client.request.OAuthBearerClientRequest;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.client.response.OAuthAccessTokenResponse;
import org.apache.oltu.oauth2.client.response.OAuthResourceResponse;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.types.GrantType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author zhazhahui
 * Created on 2019/6/5
 */
@RestController
public class ClientController {

    String clientId = null;
    String clientSecret = null;
    String accessTokenUrl = null;
    String userInfoUrl = null;
    String redirectUrl = null;
    String response_type = null;
    String code = null;

    /**
     * 提交申请code的请求,对应上图中的步骤一
     *
     * @param request
     * @param response
     * @return
     * @throws OAuthProblemException
     */
    @RequestMapping("/requestServerCode")
    public String requestServerCode(HttpServletRequest request, HttpServletResponse response)
            throws OAuthProblemException {
        clientId = "clientId";
        accessTokenUrl = "responseCode";
        redirectUrl = "http://localhost:8081/oauthclient01/clientController/callbackCode";
        response_type = "code";
        String requestUrl = null;
        try {
            //构建oauth的请求。设置授权服务地址（accessTokenUrl）、clientId、response_type、redirectUrl
            OAuthClientRequest accessTokenRequest = OAuthClientRequest
                    .authorizationLocation(accessTokenUrl)
                    .setResponseType(response_type)
                    .setClientId(clientId)
                    .setRedirectURI(redirectUrl)
                    .buildQueryMessage();
            requestUrl = accessTokenRequest.getLocationUri();
            System.out.println("获取授权码方法中的requestUrl的值----" + requestUrl);
        } catch (OAuthSystemException e) {
            e.printStackTrace();
        }
        return "redirect:http://localhost:8082/oauthserver/" + requestUrl;
    }

    /**
     * 接受客户端返回的code，提交申请access token的请求，对应上图中的步骤三
     *
     * @param request
     * @return
     * @throws OAuthProblemException
     */
    @RequestMapping("/callbackCode")
    public Object toLogin(HttpServletRequest request) throws OAuthProblemException {
        clientId = "clientId";
        clientSecret = "clientSecret";
        accessTokenUrl = "http://localhost:8082/oauthserver/responseAccessToken";
        userInfoUrl = "userInfoUrl";
        redirectUrl = "http://localhost:8081/oauthclient01/clientController/accessToken";
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        OAuthClient oAuthClient = new OAuthClient(new URLConnectionClient());
        try {
            OAuthClientRequest accessTokenRequest = OAuthClientRequest
                    .tokenLocation(accessTokenUrl)
                    .setGrantType(GrantType.AUTHORIZATION_CODE)
                    .setClientId(clientId)
                    .setClientSecret(clientSecret)
                    .setCode(httpRequest.getParameter("code"))
                    .setRedirectURI(redirectUrl)
                    .buildQueryMessage();
            //去服务端请求access token，并返回响应
            OAuthAccessTokenResponse oAuthResponse = oAuthClient.accessToken(accessTokenRequest, OAuth.HttpMethod.POST);
            //获取服务端返回过来的access token
            String accessToken = oAuthResponse.getAccessToken();
            //查看access token是否过期
            //Long expiresIn = oAuthResponse.getExpiresIn();
            return "redirect:http://localhost:8081/oauthclient01/clientController/accessToken?accessToken=" + accessToken;
        } catch (OAuthSystemException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 接受服务端传回来的access token，由此token去请求服务端的资源（用户信息等），对应上图中的步骤五
     *
     * @param accessToken
     * @return
     */
    @RequestMapping("/accessToken")
    public ModelAndView accessToken(String accessToken) {
        userInfoUrl = "http://localhost:8082/oauthserver/userInfo";
        OAuthClient oAuthClient = new OAuthClient(new URLConnectionClient());
        try {
            OAuthClientRequest userInfoRequest = new OAuthBearerClientRequest(userInfoUrl)
                    .setAccessToken(accessToken).buildQueryMessage();
            OAuthResourceResponse resourceResponse = oAuthClient.resource(userInfoRequest, OAuth.HttpMethod.GET, OAuthResourceResponse.class);
            String username = resourceResponse.getBody();
            ModelAndView modelAndView = new ModelAndView("usernamePage");
            modelAndView.addObject("username", username);
            return modelAndView;
        } catch (OAuthSystemException e) {
            e.printStackTrace();
        } catch (OAuthProblemException e) {
            e.printStackTrace();
        }
        return null;
    }
}
