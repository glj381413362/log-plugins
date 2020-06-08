package com.enhance.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * <p>
 *
 * </p>
 *
 * @author gongliangjun 2019/07/01 11:18
 */
@Configuration
@ComponentScan({"com.enhance.aspect","com.enhance.core.service","com.enhance.config"})
public class BeanAutoConfig {

}
