package com.enhance.config;

import static javax.servlet.DispatcherType.REQUEST;

import com.enhance.core.filter.AddTraceIdFilter;
import com.enhance.core.filter.FeignTraceRequestInterceptor;
import com.enhance.core.filter.ZuulHeaderFilter;
import com.netflix.zuul.ZuulFilter;
import feign.RequestInterceptor;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

/**
 *  请求traceID相关bean配置类
 *  启动类使用@EnableLogChain注解开启
 *
 * @author gongliangjun 2019/07/01 11:18
 */
@Slf4j
@Configuration
public class EnableLogChainConfiguration {
	private final static String logPluginsBanner=" \r\n"
			+ " =======================================================================================================================================================================\n"
			+ "||.   .  .          ,.             .  .       .                            .    ,--. ,--.   ,    ,    ,  ,--. ,--. ,--. ,--.     .                 .                    ||\n"
			+ "|||-. |- |- ,-. ,-. `'   , , ,-. . |- |-. . . |-.    ,-. ,-. ,-,-.   , ,-. |  .  __| \\__/  /|   /|   /|   __|  __| |__     /   , |  ,-. ,-.    ,-. |  . . ,-. . ,-. ,-. ||\n"
			+ "||| | |  |  | | `-. ,.  / /  | | | |  | | | | | | ,. |   | | | | |  /  | | |  |    | /  \\ ' |  '-+- ' |     |    | |  \\ ,-'   /  |  | | | | -- | | |  | | | | | | | `-. ||\n"
			+ "||' ' `' `' |-' `-' `' ' '   `-| ' `' ' ' `-' `-' `' `-' `-' ' ' ' '   `-| `' | `--' `--'  -^-   `   -^- `--' `--' `--' `--- '   `' `-' `-|    |-' `' `-' `-| ' ' ' `-' ||\n"
			+ "||          |                 ,|                                        ,|    |                                                          ,|    |           ,|           ||\n"
			+ "||          '                 `'                                        `'   `'                                                          `'    '           `'           ||\n"
			+ " =======================================================================================================================================================================";

	@Value("#{'${log-plugin.addTraceId.uri: /* }'.trim().split(',')}")
	private List<String> traceUris;


	/**
	 * 使用zuul做网关时,traceId通过header传递，需要手动添加
	 *
	 *
	 * @author gongliangjun 2020-06-14 11:19 AM
	 * @return com.netflix.zuul.ZuulFilter
	 */
	@ConditionalOnProperty(
			prefix = "log-plugin.zuul",
			name = {"enable"},
			havingValue = "true"
	)
	@ConditionalOnBean(name = "zuulProxyMarkerBean")
	@Bean
	public ZuulFilter zuulTraceIdFilter(){
		// 使用的zuul网关\c\u000d log.info("注册bean zuulTraceIdFilter");
		return new ZuulHeaderFilter();
	}
	/**
	 * 开启fegin traceID传递
	 *
	 *
	 * @author gongliangjun 2020-06-14 11:41 AM
	 * @return feign.RequestInterceptor
	 */
	@ConditionalOnProperty(
			prefix = "log-plugin.feignTrace",
			name = {"enable"},
			havingValue = "true"
	)
	@Bean("feignTraceRequestInterceptora")
	public RequestInterceptor feignTraceRequestInterceptor(){
		// 进行fegin 调用时 traceID传递 \c\u000d log.info("注册bean feignTraceRequestInterceptor");
		return new FeignTraceRequestInterceptor();
	}




	@Bean
	public FilterRegistrationBean<AddTraceIdFilter> addTraceIdFilterRegistration() {
		FilterRegistrationBean<AddTraceIdFilter> filterRegistrationBean = new FilterRegistrationBean<>();
		filterRegistrationBean.setFilter(addTraceIdFilter());
		filterRegistrationBean.setName("addTraceIdFilter");
		filterRegistrationBean.setOrder(Ordered.LOWEST_PRECEDENCE);
		filterRegistrationBean.setDispatcherTypes(REQUEST);
		//指定需要添加traceId的url路径
		filterRegistrationBean.setUrlPatterns(traceUris);
//		filterRegistrationBean.setUrlPatterns(Collections.singleton("/*"));
		return filterRegistrationBean;
	}
	@Bean
	public AddTraceIdFilter addTraceIdFilter() {
	  // 打印日志项目地址\a\u000d log.info(logPluginsBanner);
		// 定义使用教程地址\b\u000d String a = " \r\n==================================================================\r\n                     log-plugin 使用教程 \r\n CSDN: https://blog.csdn.net/qq_21239913/article/details/106624875 \r\n 博客: http://jessica.glj-site.com:8888/?p=251 \r\n==================================================================";
		// 打印教程地址\c\u000d log.info(a);
		return new AddTraceIdFilter();
	}
}
