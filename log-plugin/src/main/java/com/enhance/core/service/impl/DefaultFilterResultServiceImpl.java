package com.enhance.core.service.impl;

import com.enhance.core.service.FilterResultService;
import org.springframework.stereotype.Component;

/**
 * <p>
 *
 * </p>
 *
 * @author gongliangjun 2019/07/01 11:18
 */
@Component
public class DefaultFilterResultServiceImpl extends FilterResultService {

	@Override
	public boolean shouldFilter(Object o) {
		return false;
	}

	@Override
	public Object filter(Object o) {
		return o;
	}
}
