package com.enhance.annotations;


import static java.lang.annotation.RetentionPolicy.RUNTIME;

import com.enhance.constant.LogConst;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * <p>
 * 日志信息注解
 * </p>
 *
 * @author 龚梁钧 2019/06/27 18:18
 */
@Retention(RUNTIME)
@Target(ElementType.METHOD)
public @interface LogProfiler {
	// 操作类型
	LogConst.Action action() default LogConst.Action.NULL;

	// 对象类型
	String itemType() default "";

	// 对象ID
	String[] itemIds() default {};

	// 对象类型
	boolean printInfoLog() default true;

	//分线器名称，不传默认为方法名称
	String profilerName() default "";

	// 如果是数组 是否打印出参大小，不打印对象值
	boolean printOutParamSize() default true;

	// 需要排除的入参
	String[] excludeInParam() default {};

	//需要打印的入参
	String[] includeInParam() default {};

	// （其他）参数
	String[] param() default {};

}
