package com.enhance.annotations;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import com.enhance.config.EnableLogChainConfiguration;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.context.annotation.Import;

/**
 * <p>
 *   开启方法调用耗时分析
 * </p>
 *
 * @author gongliangjun 2019/07/01 11:18
 */

@Retention(RUNTIME)
@Target({ElementType.METHOD,ElementType.TYPE})
public @interface EnableProfiler  {
  //分线器名称，不传默认为方法名称
  String profilerName() default "";

}
