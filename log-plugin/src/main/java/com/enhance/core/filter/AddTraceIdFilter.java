package com.enhance.core.filter;

import static com.enhance.constant.LogPluginConst.TRACEID;

import com.enhance.aspect.LogThreadContext;
import java.io.IOException;
import java.util.UUID;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/**
 * 添加traceId
 *
 * @author gongliangjun 2019/07/01 11:18
 */
public class AddTraceIdFilter implements Filter {

  /** logger */
  private static final Logger log = LoggerFactory.getLogger(AddTraceIdFilter.class);


  @Override
  public void init(FilterConfig filterConfig) throws ServletException {}

  @Override
  public void doFilter(
      ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
      throws IOException, ServletException {
    HttpServletRequest request = (HttpServletRequest) servletRequest;
    HttpServletResponse resp = (HttpServletResponse) servletResponse;
    String traceId = request.getHeader(TRACEID);
    if (StringUtils.isEmpty(traceId)) {
      traceId = (String) request.getAttribute(TRACEID);
    }
    if (StringUtils.isEmpty(traceId)) {
      log.info("添加TraceId");
      traceId = UUID.randomUUID().toString();
      request.setAttribute(TRACEID, traceId);
      resp.addHeader(TRACEID, traceId);
    }
    MDC.put(TRACEID, traceId);
    resp.addHeader(TRACEID, traceId);
    try {
      filterChain.doFilter(servletRequest, servletResponse);
    } finally {
//      MDC.remove(TRACEID);
			MDC.clear();
      // 防止内存泄漏
      LogThreadContext.LOG_THREAD_CONTEXT.remove();
    }
  }

  @Override
  public void destroy() {}
}
