package com.enhance.logplugin.demo.filter;

import com.enhance.core.service.LogService;
import org.springframework.stereotype.Component;

/**
 * <p>
 * 获取请求中的用户信息
 *
 * </p>
 *
 * @author gongliangjun 2019/07/01 11:18
 */
@Component
public class UserInfoService implements LogService {

  /**
   * 获取用户信息，用于放入日志框架的MD里
   *
   * @return java.lang.String
   * @author gongliangjun 2019-12-19 3:33 PM
   */
  @Override
  public String getUserInfo() {
    // TODO: 2020/6/2 这里为了测试，模拟从请求中获取用户信息，可根据自己实际情况修改
    //===============================================================================
    //
    //===============================================================================
    return "admin001";
  }

  /**
   * 通过token获取用户信息
   *
   * @param request
   * @return java.lang.String
   * @author 龚梁钧 2019-06-27 15:43
   */
  /*private String getUserFromToken(HttpServletRequest request) {
    String authorization = request.getHeader(AUTHORIZATION);
    if (StringUtils.isNotEmpty(authorization)) {
      String token = authorization.substring(7);
      JsonParser objectMapper = JsonParserFactory.create();
      String tokenJson = JwtHelper.decode(token).getClaims();
      Map<String, Object> claims = objectMapper.parseMap(tokenJson);
      String commpanyID = (String) claims.get(COMMPANY_ID);
      String userName = (String) claims.get(USER_NAME);
      String clientId = (String) claims.get(CLIENT_ID);
      StringBuilder res = new StringBuilder();
      if (StringUtils.isNotEmpty(commpanyID)) {
        res.append(commpanyID).append("-");
      }
      if (StringUtils.isNotEmpty(userName)) {
        res.append(userName).append("-");
      }
      if (StringUtils.isNotEmpty(clientId)) {
        res.append(clientId);
      }
      return res.toString();
    }
    return null;
  }*/
}
