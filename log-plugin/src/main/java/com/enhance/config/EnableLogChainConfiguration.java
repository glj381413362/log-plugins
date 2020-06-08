package com.enhance.config;

import com.enhance.core.filter.AddTraceIdFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

import java.util.Collections;

import static javax.servlet.DispatcherType.REQUEST;

/**
 * <p>
 *
 * </p>
 *
 * @author gongliangjun 2019/07/01 11:18
 */
@Configuration
public class EnableLogChainConfiguration {
	@Bean
	public FilterRegistrationBean<AddTraceIdFilter> addTraceIdFilterRegistration() {
		FilterRegistrationBean<AddTraceIdFilter> filterRegistrationBean = new FilterRegistrationBean<>();
		filterRegistrationBean.setFilter(addTraceIdFilter());
		filterRegistrationBean.setName("addTraceIdFilter");
		filterRegistrationBean.setOrder(Ordered.LOWEST_PRECEDENCE);
		filterRegistrationBean.setDispatcherTypes(REQUEST);
		filterRegistrationBean.setUrlPatterns(Collections.singleton("/*"));
		return filterRegistrationBean;
	}

	@Bean
	public AddTraceIdFilter addTraceIdFilter() {
		return new AddTraceIdFilter();
	}

}
