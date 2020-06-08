package com.enhance.util;

import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.EvaluationException;
import org.springframework.expression.spel.standard.SpelExpression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

/**
 * description
 *
 * @author 龚梁钧 2019/06/28 9:52
 */
public class SPELUtil {
	/**
	 * logger
	 */
	private final SpelExpressionParser parser;
	private final StandardEvaluationContext context;

	public SPELUtil(ProceedingJoinPoint pjp) {
		this.parser = new SpelExpressionParser();
		this.context = new StandardEvaluationContext();
		extractArgments(pjp);
	}

	/**
	 * 得到参数名称和值 放到 spel 上下文
	 *
	 * @param pjp
	 * @return void
	 * @author 龚梁钧 2019-06-28 9:54
	 */
	private void extractArgments(ProceedingJoinPoint pjp) {

		MethodSignature methodSignature = (MethodSignature) pjp.getSignature();
		String[] names = methodSignature.getParameterNames();
		Object[] args = pjp.getArgs();
		for (int i = 0; i < args.length; i++) {
			if (null == names) {
				this.context.setVariable(String.format("arg%s", i), args[i]);
			} else {
				this.context.setVariable(names[i], args[i]);
			}
		}
	}

	/**
	 * 计算表达式
	 *
	 * @param expr
	 * @return java.lang.Object
	 * @author 龚梁钧 2019-06-28 9:54
	 */
	public Object cacl(String expr) {

		if (StringUtils.isBlank(expr)) {
			return null;
		}
		if (!expr.startsWith("#")) {
			expr = "#".concat(expr);
		}
		Object value = null;
		try {
			SpelExpression spelExpression = this.parser.parseRaw(expr);
			value = spelExpression.getValue(this.context);
		} catch (EvaluationException e) {
		}
		return value;
	}
}
