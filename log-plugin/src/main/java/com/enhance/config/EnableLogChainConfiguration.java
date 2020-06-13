package com.enhance.config;

import static javax.servlet.DispatcherType.REQUEST;

import com.enhance.core.filter.AddTraceIdFilter;
import java.util.HashSet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

/**
 * <p>
 *
 * </p>
 *
 * @author gongliangjun 2019/07/01 11:18
 */
@Slf4j
@Configuration
public class EnableLogChainConfiguration {

	private final String logPluginsBanner=" \r\n"
			+ " =======================================================================================================================================================================\n"
			+ "||.   .  .          ,.             .  .       .                            .    ,--. ,--.   ,    ,    ,  ,--. ,--. ,--. ,--.     .                 .                    ||\n"
			+ "|||-. |- |- ,-. ,-. `'   , , ,-. . |- |-. . . |-.    ,-. ,-. ,-,-.   , ,-. |  .  __| \\__/  /|   /|   /|   __|  __| |__     /   , |  ,-. ,-.    ,-. |  . . ,-. . ,-. ,-. ||\n"
			+ "||| | |  |  | | `-. ,.  / /  | | | |  | | | | | | ,. |   | | | | |  /  | | |  |    | /  \\ ' |  '-+- ' |     |    | |  \\ ,-'   /  |  | | | | -- | | |  | | | | | | | `-. ||\n"
			+ "||' ' `' `' |-' `-' `' ' '   `-| ' `' ' ' `-' `-' `' `-' `-' ' ' ' '   `-| `' | `--' `--'  -^-   `   -^- `--' `--' `--' `--- '   `' `-' `-|    |-' `' `-' `-| ' ' ' `-' ||\n"
			+ "||          |                 ,|                                        ,|    |                                                          ,|    |           ,|           ||\n"
			+ "||          '                 `'                                        `'   `'                                                          `'    '           `'           ||\n"
			+ " =======================================================================================================================================================================";
	@Value("#{'${need.addTraceId.uris: /* }'.trim().split(',')}")
	private HashSet<String> traceUris;

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
