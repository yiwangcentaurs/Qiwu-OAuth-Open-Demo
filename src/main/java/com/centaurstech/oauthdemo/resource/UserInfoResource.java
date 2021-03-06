package com.centaurstech.oauthdemo.resource;

import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.error.OAuthError;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.OAuthResponse;
import org.apache.oltu.oauth2.common.message.types.ParameterStyle;
import org.apache.oltu.oauth2.common.utils.OAuthUtils;
import org.apache.oltu.oauth2.rs.request.OAuthAccessResourceRequest;
import org.apache.oltu.oauth2.rs.response.OAuthRSResponse;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
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
public class UserInfoResource {

    public static final String USER_INFO_URL = "/user/info";

    @RequestMapping(value = USER_INFO_URL, method = {RequestMethod.GET, RequestMethod.POST})
    public HttpEntity<String> userInfo(HttpServletRequest request)
            throws OAuthSystemException {
        try {
            //获取客户端传来的OAuth资源请求
            OAuthAccessResourceRequest oauthRequest = new
                    OAuthAccessResourceRequest(request, ParameterStyle.QUERY);
            //获取Access Token
            String accessToken = oauthRequest.getAccessToken();
            System.out.println("从客户端获取的accessToken----" + accessToken);
            //验证Access Token
            if (StringUtils.isEmpty(accessToken) && !validateAccessToken(accessToken)) {
                return new ResponseEntity<String>(HttpStatus.BAD_REQUEST);
            }

            // 如果不存在/过期了，返回未验证错误，需重新验证
            OAuthResponse oauthResponse = OAuthRSResponse
                    .errorResponse(HttpServletResponse.SC_UNAUTHORIZED)
                    .setError(OAuthError.ResourceResponse.INVALID_TOKEN)
                    .buildHeaderMessage();
            HttpHeaders headers = new HttpHeaders();
            headers.add(OAuth.HeaderType.WWW_AUTHENTICATE,
                    oauthResponse.getHeader(OAuth.HeaderType.WWW_AUTHENTICATE));
            //根据accessToken 获取用户信息并返回JSON

            JSONObject jsonObject = new JSONObject();
            try {
                //userId 为服务器唯一ID
                jsonObject.put("userId", "aiqinhai");
                //userPhone 用户手机号
                jsonObject.put("userPhone", "aiqinhai");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return new ResponseEntity<String>(jsonObject.toString(), HttpStatus.OK);
        } catch (OAuthProblemException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            //检查是否设置了错误码
            String errorCode = e.getError();
            if (OAuthUtils.isEmpty(errorCode)) {
                OAuthResponse oauthResponse = OAuthRSResponse
                        .errorResponse(HttpServletResponse.SC_UNAUTHORIZED)
                        .buildHeaderMessage();
                HttpHeaders headers = new HttpHeaders();
                headers.add(OAuth.HeaderType.WWW_AUTHENTICATE,
                        oauthResponse.getHeader(OAuth.HeaderType.WWW_AUTHENTICATE));
                return new ResponseEntity<String>(headers, HttpStatus.UNAUTHORIZED);
            }
            OAuthResponse oauthResponse = OAuthRSResponse
                    .errorResponse(HttpServletResponse.SC_UNAUTHORIZED)
                    .setError(e.getError())
                    .setErrorDescription(e.getDescription())
                    .setErrorUri(e.getUri())
                    .buildHeaderMessage();
            HttpHeaders headers = new HttpHeaders();
            headers.add(OAuth.HeaderType.WWW_AUTHENTICATE,
                    oauthResponse.getHeader(OAuth.HeaderType.WWW_AUTHENTICATE));
            return new ResponseEntity<String>(HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * @param accessToken 验证accessToken
     * @return 返回验证结果
     */
    private boolean validateAccessToken(String accessToken) {
        return true;
    }
}
