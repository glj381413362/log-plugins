/*
 *
 *  *  Copyright (C) HAND Enterprise Solutions Company Ltd.
 *  *  All Rights Reserved
 *
 */

package com.enhance.core.filter;

import static com.enhance.constant.LogPluginConst.TRACEID;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;

/**
 * 用于转发请求头中添加X-B3-TraceId
 * 当配置了属性log-plugin.enable.zuul=true才会加载改配置类
 *
 * @author gongliangjun 2020/06/13 4:46 PM
 */
@Slf4j
public class ZuulHeaderFilter extends ZuulFilter {
  public static final String PRE_TYPE = "pre";

  @Override
  public String filterType() {
    return PRE_TYPE;
  }

  @Override
  public int filterOrder() {
    return Integer.MAX_VALUE;
  }

  @Override
  public boolean shouldFilter() {
    return true;
  }

  @Override
  public Object run() {
    try {
      RequestContext ctx = RequestContext.getCurrentContext();
      String traceId = MDC.get(TRACEID);
      if (StringUtils.isNotEmpty(traceId)) {
        ctx.getZuulRequestHeaders().put(TRACEID, traceId);
      }
    } catch (Exception e) {
      log.error("X-B3-TraceId 传递失败", e);
    }
    return null;
  }
}
