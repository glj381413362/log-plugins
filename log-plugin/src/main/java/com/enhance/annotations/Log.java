package com.enhance.annotations;


import com.alibaba.fastjson.support.spring.annotation.FastJsonFilter;
import com.enhance.constant.LogConst;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * <p>
 * 日志信息注解
 * </p>
 *
 * @author 龚梁钧 2019/06/27 18:18
 */
@Retention(RUNTIME)
@Target(ElementType.METHOD)
public @interface Log  {
	// 操作类型
	LogConst.Action action() default LogConst.Action.NULL;

	// 对象类型
	String itemType() default "";

	// 对象ID
	String[] itemIds() default {};

	// 对象类型
	boolean printInfoLog() default false;

	// 如果是数组 是否打印出参大小，不打印对象值
	boolean printOutParamSize() default true;

	/**
	 * 需要排除的入参,arg0代表第一个参数,arg1代表第二个参数
	 * 例如：方法test(String a,BDto b,UserDto user)
	 * 1、排除b 参数不输出 {@link ExcludeInParamTest#excludeInParamTest1}
	 * 		excludeInParam = {"arg1"} 或者 excludeInParam = {"BDto"}
	 * 2、排除a,b 参数不输出 {@link ExcludeInParamTest#excludeInParamTest2}
	 * 		excludeInParam = {"arg0","arg1"}
	 * 3、排除user的password属性不输出 {@link ExcludeInParamTest#excludeInParamTest3}
	 * 		excludeInParam = {"arg2.password"}
	 * 4、参数是集合或者map时,使用 'arg+数字' 来进行排除
	 *
	 */
	String[] excludeInParam() default {};

	/**
	 * 需要打印的入参,arg0代表第一个参数,arg1代表第二个参数
	 * 例如：方法test(String a,BDto b,UserDto user)
	 * 因为默认是所有的参数都会打印，所以这里是指只打印某个参数dto的属性值
	 * 1、打印参数user的name属性
	 * 		includeInParam = {"arg2.name"}
	 * 	会打印出参数arg0,arg1的所有属性和arg2的name属性
	 *
	 * 	注意:
	 * 	    1、当excludeInParam排除参数的某个属性和includeInParam冲突时，includeInParam生效
	 * 	       excludeInParam = {"arg2.password"} 和 includeInParam = {"arg2.password"} 会打印user的password值
	 * 	    2、当excludeInParam排除参数和includeInParam冲突时，excludeInParam生效
	 * 			   excludeInParam = {"arg2"} 和 includeInParam = {"arg2.password"} 不会打印user的password值
	 */
	String[] includeInParam() default {};

	/**
	 * （其他）参数
	 * 当配置了param，就只会打印param配置的参数，此时excludeInParam和includeInParam不会生效
	 * 例如：方法test(String a,BDto b,UserDto user)
	 *  @Log ( excludeInParam = {"arg2"} ,includeInParam = {"arg2.password"}, param={"user.name","a","b.id"})
	 *  此时只会打印 arg2的name 和 arg0 和 arg1的id excludeInParam和includeInParam不会生效
	 *
	 */
	String[] param() default {};

}
