package com.enhance.core.service;

import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

/**
 * <p>
 *
 * </p>
 *
 * @author gongliangjun 2019/07/01 11:18
 */
@Slf4j
public abstract class FilterResultService<T, U> {


	public abstract boolean shouldFilter(U u);

	public Optional<T> filterResult(U u) {
		if (shouldFilter(u)) {
			log.info("Start dealing with the results");
			return Optional.ofNullable(filter(u));
		}
		return null;
	}

	public abstract T filter(U u);
}
