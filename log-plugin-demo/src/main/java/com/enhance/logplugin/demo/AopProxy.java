package com.enhance.logplugin.demo;

import org.springframework.aop.framework.AopContext;

/**
 * <p>
 * 封装self()方法便于获取自身接口代理类
 * 在启动类需要配置@EnableAspectJAutoProxy(exposeProxy=true)
 *   @param <T> 代理接口类型
 * </p>
 *
 * @author gongliangjun 2020/06/03 11:29 PM
 */
public interface AopProxy<T> {

  @SuppressWarnings("unchecked")
  default T self() {
    return (T) AopContext.currentProxy();
  }

}
