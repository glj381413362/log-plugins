package com.enhance.config.properties;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
/**
 * 日志插件配置类
 *
 * @author gongliangjun 2020/06/16 2:12 PM
 */
@Data
public class LogTraceProperty {
  public static final String LOG_TRACE_PREFIX = "plugin.log.trace";
  /**
   * 是否开启feign 请求头里的traceID传递
   * 默认开启
   */
  private boolean enableFeign;
  /**
   * 是否开启zuul 请求头里的traceID传递
   * zuul网关默认请求头里的数据不向下传递
   */
  private boolean enableZuul;
  /**
   * 需要添加traceID的uri规则
   * 默认所有请求都添加 /*
   */
  private List<String> uri;

  public LogTraceProperty() {
    this.enableFeign = true;
    this.uri =
        new ArrayList<String>() {
          {
            add("/*");
          }
        };
  }
}
