package com.enhance.util;

import com.enhance.aspect.LogThreadContext;
import com.enhance.aspect.LogThreadContext.LogContext;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.slf4j.profiler.Profiler;


/**
 * description: job的日志打印
 *
 * @author roman 2019/09/05 10:57
 */
@Slf4j
public class LogUtil {

  private final static String CODE = "code";
  private final static String OLD_CODE = "oldCode";
  private final static String MSG_FORMAT = "(%s:%s)";

  /**
   * 开始方法耗时统计,需要配合注解@Log使用
   *
   * @return void
   * @author gongliangjun 2020-06-01 11:26 AM
   */
  public static LogThreadContext startProfiler(String name) {
    LogThreadContext ltc = LogThreadContext.getLogThreadContext();
    if (ltc.isEnableProfiler()) {
      LogContext logContext = ltc.getCurrentLogContext();
      String profilerName = logContext.getProfilerName();
      Profiler parent = ltc.get(profilerName);
      Profiler childProfiler = parent.startNested(name);
      // 获取注册的分析器
      String names = new StringBuilder(profilerName).append("-->").append(name).toString();
      childProfiler.start(names);
      ltc.setInstantProfiler(childProfiler);
      return ltc;
    } else {
      log.warn("method startProfiler(name) 需要配合注解@LogProfiler或者@EnableProfiler使用,并且开启enableProfiler");
    }
    return ltc;
  }

  /**
   * 打印自定义描述和多个code值 需要配合@log或@LogProfiler注解一起使用，MDC才能清空 一般在循环内使用,循环结束要调用clearMDC()
   *
   * 例子：
   * for (OrderEntry orderEntry : orderEntries) {
   *         LogUtil.printCode("orderEntry skuid和数量", "-", orderEntry.getSkuId().toString(),orderEntry.getQuantity().toString());
   *         log.info("开始处理...");
   *         log.info("处理中");
   *         log.info("处理结束");
   *         LogUtil.clearMDC();
   * }
   * 结果：
   * INFO [(orderEntry skuid和数量:1-21)]c.enhance.logplugin.demo.service.OrderServiceImpl - 开始处理...
   * INFO [(orderEntry skuid和数量:1-21)]c.enhance.logplugin.demo.service.OrderServiceImpl - 处理中
   * INFO [(orderEntry skuid和数量:1-21)]c.enhance.logplugin.demo.service.OrderServiceImpl - 处理结束
   *
   * @return void
   * @author gongliangjun 2019-10-30 11:04 AM
   */
  public static void printCode(String describe, String separator, String... values) {
    LogThreadContext ltc = LogThreadContext.getLogThreadContext();
    if (ltc.isEnableLog()) {
      StringBuilder msg = new StringBuilder("(");
      if (StringUtils.isNotEmpty(describe)) {
        msg.append(describe).append(":");
      }
      if (StringUtils.isEmpty(separator)) {
        separator = " ";
      }
      String msgs = Arrays.stream(values).collect(Collectors.joining(separator));
      msg = msg.append(msgs).append(")");
      Map<String, String> callStack = ltc.peekCallStack();
      String codeValue = callStack.get(CODE);
      if (StringUtils.isNotBlank(codeValue)) {
        callStack.put(OLD_CODE, codeValue);
      }
      callStack.put(CODE,msg.toString());
      MDC.put(CODE, msg.toString());
    } else {
      log.warn("method printCode() 需要配合注解@Log或@LogProfiler使用");
    }
  }

  /**
   * 打印自定义描述和code值 需要配合@log或@LogProfiler注解一起使用，MDC才能清空 一般在循环内使用,循环结束要调用clearMDC()
   * 例子：orderEntries 有两条数据
   * for (OrderEntry orderEntry : orderEntries) {
   *         LogUtil.printCode("处理的skuid", orderEntry.getSkuId().toString());
   *         log.info("开始处理...");
   *         log.info("处理中");
   *         log.info("处理结束");
   *         LogUtil.clearMDC();
   * }
   * 结果：
   * INFO [(处理的skuid:1)]c.enhance.logplugin.demo.service.OrderServiceImpl - 开始处理...
   * INFO [(处理的skuid:1)]c.enhance.logplugin.demo.service.OrderServiceImpl - 处理中
   * INFO [(处理的skuid:1)]c.enhance.logplugin.demo.service.OrderServiceImpl - 处理结束
   * INFO [(处理的skuid:2)]c.enhance.logplugin.demo.service.OrderServiceImpl - 开始处理...
   * INFO [(处理的skuid:2)]c.enhance.logplugin.demo.service.OrderServiceImpl - 处理中
   * INFO [(处理的skuid:2)]c.enhance.logplugin.demo.service.OrderServiceImpl - 处理结束
   * @return void
   * @author gongliangjun 2019-10-30 11:03 AM
   */
  public static void printCode(String describe, String value) {

    LogThreadContext ltc = LogThreadContext.getLogThreadContext();
    if (ltc.isEnableLog()) {
      String format = String.format(MSG_FORMAT, describe, value);
      Map<String, String> callStack = ltc.peekCallStack();
      String codeValue = callStack.get(CODE);
      if (StringUtils.isNotBlank(codeValue)) {
        callStack.put(OLD_CODE, codeValue);
      }
      callStack.put(CODE,format);
      MDC.put(CODE, format);
    } else {
      log.warn("method printCode() 需要配合注解@Log或@LogProfiler使用");
    }
  }

  /**
   * 打印特定code值 需要配合@log注解一起使用，MDC才能清空 一般在循环内使用,循环结束要调用clearMDC()
   * 例子：orderEntries 有两条数据
   * for (OrderEntry orderEntry : orderEntries) {
   *         LogUtil.printCode(orderEntry.getSkuId().toString());
   *         log.info("开始处理...");
   *         log.info("处理中");
   *         log.info("处理结束");
   *         LogUtil.clearMDC();
   * }
   * 结果：
   * INFO [1]c.enhance.logplugin.demo.service.OrderServiceImpl - 开始处理...
   * INFO [1]c.enhance.logplugin.demo.service.OrderServiceImpl - 处理中
   * INFO [1]c.enhance.logplugin.demo.service.OrderServiceImpl - 处理结束
   * INFO [2]c.enhance.logplugin.demo.service.OrderServiceImpl - 开始处理...
   * INFO [2]c.enhance.logplugin.demo.service.OrderServiceImpl - 处理中
   * INFO [2]c.enhance.logplugin.demo.service.OrderServiceImpl - 处理结束
   * @return void
   * @author gongliangjun 2019-10-30 11:03 AM
   */
  public static void printCode(String value) {
    LogThreadContext ltc = LogThreadContext.getLogThreadContext();
    if (ltc.isEnableLog()) {
      Map<String, String> callStack = ltc.peekCallStack();
      String codeValue = callStack.get(CODE);
      if (StringUtils.isNotBlank(codeValue)) {
        callStack.put(OLD_CODE, codeValue);
      }
      callStack.put(CODE,value);
      MDC.put(CODE, value);
    } else {
      log.warn("method printCode() 需要配合注解@Log或@LogProfiler使用");
    }
  }

  /**
   * 自定义模板打印日志 需要配合@log注解一起使用，MDC才能清空 一般在循环内使用,循环结束要调用clearMDC()
   * 例子：orderEntries 有两条数据
   * for (OrderEntry orderEntry : orderEntries) {
   *         LogUtil.printCodeByTemplate("skuId[{}] 数量[{}]",orderEntry.getSkuId(),orderEntry.getQuantity());
   *         log.info("开始处理...");
   *         log.info("处理中");
   *         log.info("处理结束");
   *         LogUtil.clearMDC();
   * }
   * 结果：
   * INFO [skuId:1 数量:21]c.enhance.logplugin.demo.service.OrderServiceImpl - 开始处理...
   * INFO [skuId:1 数量:21]c.enhance.logplugin.demo.service.OrderServiceImpl - 处理中
   * INFO [skuId:1 数量:21]c.enhance.logplugin.demo.service.OrderServiceImpl - 处理结束
   * INFO [skuId:2 数量:11]c.enhance.logplugin.demo.service.OrderServiceImpl - 开始处理...
   * INFO [skuId:2 数量:11]c.enhance.logplugin.demo.service.OrderServiceImpl - 处理中
   * INFO [skuId:2 数量:11]c.enhance.logplugin.demo.service.OrderServiceImpl - 处理结束
   *
   *
   * @param template 和log使用方式一样，使用{}作为占位符
   * @param values 需要替换占位符{}的值
   * @return void
   * @author gongliangjun 2019-10-30 10:47 AM
   */
  public static void printCodeByTemplate(String template, Object... values) {
    LogThreadContext ltc = LogThreadContext.getLogThreadContext();
    if (ltc.isEnableLog()) {
      String msgFormat = template.replace("{}", "%s");
      String format = String.format(msgFormat, values);
      Map<String, String> callStack = ltc.peekCallStack();
      String value = callStack.get(CODE);
      if (StringUtils.isNotBlank(value)) {
        callStack.put(OLD_CODE, value);
      }
      callStack.put(CODE,format);
      MDC.put(CODE, format);
    } else {
      log.warn("method printCode() 需要配合注解@Log或@LogProfiler使用");
    }
  }

  public static void clearMDC() {
    LogThreadContext ltc = LogThreadContext.getLogThreadContext();
    if (ltc.isEnableLog()) {
      Map<String, String> callStack = ltc.peekCallStack();
      String old = callStack.get(OLD_CODE);
      if (StringUtils.isNotBlank(old)) {
        MDC.put(CODE,old);
        callStack.put(CODE,old);
        callStack.remove(OLD_CODE);
      }else {
        MDC.remove(CODE);
      }
    }else {
      log.warn("method clearMDC() 需要配合注解@Log或@LogProfiler使用");
    }
  }
}
