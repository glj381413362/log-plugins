package com.enhance.core.filter;

import static com.enhance.constant.LogPluginConst.TRACEID;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import java.util.Enumeration;
import javax.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * fegin traceId 传递
 *
 * @author gongliangjun 2020/06/14 11:26 AM
 */
public class FeignTraceRequestInterceptor implements RequestInterceptor {

  @Override
  public void apply(RequestTemplate requestTemplate) {
    ServletRequestAttributes attributes =
        (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
    if (attributes != null) {
      HttpServletRequest request = attributes.getRequest();
      Enumeration<String> headerNames = request.getHeaderNames();
      if (headerNames != null) {
        while (headerNames.hasMoreElements()) {
          String name = headerNames.nextElement();
          if (name.equals(TRACEID) || name.contains(TRACEID)) {
            String traceIdValue = request.getHeader(name);
            requestTemplate.header(TRACEID, traceIdValue);
          }
        }
      }
    }
  }
}
