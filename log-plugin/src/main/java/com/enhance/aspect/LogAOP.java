package com.enhance.aspect;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SimplePropertyPreFilter;
import com.alibaba.fastjson.support.config.FastJsonConfig;
import com.alibaba.fastjson.support.spring.PropertyPreFilters;
import com.alibaba.fastjson.support.spring.PropertyPreFilters.MySimplePropertyPreFilter;
import com.enhance.annotations.Log;
import com.enhance.annotations.LogProfiler;
import com.enhance.aspect.LogThreadContext.LogContext;
import com.enhance.constant.LogConst;
import com.enhance.core.service.FilterResultService;
import com.enhance.core.service.LogService;
import com.enhance.util.SPELUtil;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.slf4j.ext.XLoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;


/**
 * description
 *
 * @author 龚梁钧 2019/06/27 18:27
 */
@Aspect
@Component
@Order(1)  //order最小最先执行  保证ProfilerAOP 后于LogAop执行
public class LogAOP implements ApplicationContextAware {
  /**
   * logger
   */
  private static final Logger LOG = XLoggerFactory.getXLogger(LogAOP.class);
  private static final String LOG_JSON = "logjson";
  private static final String USER = "user";
  private static final String CODE = "code";
  private static final String DOT_NOTATION = ".";

  @Autowired
  private List<FilterResultService> filterResultServices;
  private LogService logService;

  @Pointcut("@annotation(com.enhance.annotations.Log) || @annotation(com.enhance.annotations.LogProfiler)")
  public void logPoint() {
  }

  @SneakyThrows
  @Around("logPoint()")
  public Object handlerLogMethod(ProceedingJoinPoint joinPoint) {
    //
    //  得到方法上的注解
    // ------------------------------------------------------------------------------
    MethodSignature signature = (MethodSignature) joinPoint.getSignature();
    Method method = signature.getMethod();
    LogProfiler logProfiler = method.getAnnotation(LogProfiler.class);
    Log logAnnotation = method.getAnnotation(Log.class);
    Object result = null;
    if (logAnnotation != null && null != logProfiler) {
      LOG.warn("@LogProfiler 和 @Log 不允许一起使用");
      result = joinPoint.proceed();
    } else {
      LogThreadContext ltc = LogThreadContext.getLogThreadContext();
      try {
        try {
          initLogContext(method, ltc);
          this.handBeforeMethodLog(joinPoint, ltc);
        } catch (Exception e) {
          LOG.warn("handlerLogMethod before:{}", e);
        }
        result = joinPoint.proceed();
      } finally {
        try {
          if (null != logProfiler) {
            ProfilerAOP.printProfiler(ltc);
          }
          this.handAfterMethodLog(joinPoint, ltc, result);
          this.handleMDCValue();
        } catch (Exception e) {
          LOG.warn("handlerLogMethod finally:{}", e);
        }
      }
    }

    return result;
  }

  /**
   * 初始化日志上下文
   *
   * @return void
   * @author gongliangjun 2020-06-04 2:30 PM
   */
  private void initLogContext(Method method, LogThreadContext ltc) {
    LogContext logContext = new LogContext();
    LogProfiler logProfiler = method.getAnnotation(LogProfiler.class);
    Log logAnnotation = method.getAnnotation(Log.class);
    if (logProfiler != null) {
      logContext.setEnableLog(true);
      logContext.setAction(logProfiler.action());
      logContext.setEnableProfiler(true);
      logContext.setExcludeInParam(logProfiler.excludeInParam());
      logContext.setIncludeValue(logProfiler.includeInParam());
      logContext.setItemIds(logProfiler.itemIds());
      logContext.setItemType(logProfiler.itemType());
      logContext.setParam(logProfiler.param());
      logContext.setPrintInfoLog(logProfiler.printInfoLog());
      logContext.setPrintOutParamSize(logProfiler.printOutParamSize());
      logContext.setProfilerName(logProfiler.profilerName());
    } else if (logAnnotation != null) {
      logContext.setEnableLog(true);
      logContext.setAction(logAnnotation.action());
      logContext.setExcludeInParam(logAnnotation.excludeInParam());
      logContext.setIncludeValue(logAnnotation.includeInParam());
      logContext.setItemIds(logAnnotation.itemIds());
      logContext.setItemType(logAnnotation.itemType());
      logContext.setParam(logAnnotation.param());
      logContext.setPrintInfoLog(logAnnotation.printInfoLog());
      logContext.setPrintOutParamSize(logAnnotation.printOutParamSize());
    }
    ltc.putContext(logContext);
    ltc.setCurrentLogContext(logContext);
  }

  /**
   * 方法执行完成后 处理MDC里的值
   *
   * @return void
   * @author gongliangjun 2020-06-04 2:25 PM
   */
  private void handleMDCValue() {
    LogThreadContext ltc = LogThreadContext.getLogThreadContext();
    try {
      //===============================================================================
      //  先对自己的code出栈 并且清除MDC
      //===============================================================================
      Map<String, String> popCallStack = ltc.popCallStack();
      clearMDC(popCallStack.keySet());

      if (!ltc.isEmptyCallStack()) {
        Map<String, String> currentMethodStack = ltc.peekCallStack();
        //===============================================================================
        //  把之前方法的数据存入MDC
        //===============================================================================
        for (Entry<String, String> entry : currentMethodStack.entrySet()) {
          String key = entry.getKey();
          String value = entry.getValue();
          MDC.put(key, value);
        }
      } else {
        // 因为Tomcat线程重用
        clearMDC(new String[]{CODE, USER, LOG_JSON});
      }
    } catch (Exception e) {
      LOG.warn("handleMDCValue error: {} ", e);
    }
  }

  /**
   * 打印调用方法后的日志
   *
   * @return void
   * @author 龚梁钧 2019-06-28 13:15
   */
  private void handAfterMethodLog(ProceedingJoinPoint joinPoint, LogThreadContext ltc,
      Object result) {
    LogContext logContext = ltc.getCurrentLogContext();
    // 是否需要打印日志
    boolean printLog = logContext.printInfoLog();
    // 如果是数组 是否打印出参大小，不打印对象值
    boolean printOutParamSize = logContext.printOutParamSize();
    MethodSignature signature = (MethodSignature) joinPoint.getSignature();
    StringBuffer endString = new StringBuffer();
    Object target = joinPoint.getTarget();
    Logger logger = LoggerFactory.getLogger(target.getClass());
    if (target instanceof Proxy) {
      endString.append(signature.getDeclaringTypeName())
          .append(".")
          .append(signature.getName());
    }
    endString.append(signature.getName());

    endString.append(" end ");
    if (logger.isDebugEnabled() || printLog) {
      if (result instanceof Collection && printOutParamSize) {
        logger.info(endString.append(" output parameters size:{}").toString(),
            Collection.class.cast(result).size());
        return;
      }
      String responseStr = null;
      try {
        if (CollectionUtils.isNotEmpty(filterResultServices)) {
          for (FilterResultService filterResultService : filterResultServices) {
            Optional filterResult = filterResultService.filterResult(result);
            // 返回null，说明不需要处理  返回Optional.empty(),说明处理了，处理结果为null
            if (filterResult != null) {
              LOG.debug("返回结果处理成功");
              if (filterResult.isPresent()) {
                Object fr = filterResult.get();
                if (fr instanceof Collection && printOutParamSize) {
                  logger.info(endString.append(" output parameters size:{}").toString(),
                      Collection.class.cast(fr).size());
                  return;
                } else {
                  responseStr = JSON.toJSONString(filterResult);
                }
                break;
              } else {
                LOG.debug("处理结果为空");
              }
            }
          }
        }
      } catch (Exception e) {
        LOG.warn("handAfterMethodLog error:{}", e);
      }
      if (StringUtils.isEmpty(responseStr)) {
        responseStr = result == null ? "void" : JSON.toJSONString(result);
      }
      endString.append("output parameters:").append(responseStr);
      if (printLog) {
        logger.info(endString.toString());
      } else {
        logger.debug(endString.toString());
      }
    } else {
      logger.info(endString.toString());
    }
  }

  /**
   * 打印调用方法前的日志
   *
   * @return org.slf4j.Logger
   * @author 龚梁钧 2019-06-28 13:13
   */
  private Logger handBeforeMethodLog(ProceedingJoinPoint joinPoint, LogThreadContext ltc) {
    LogContext logContext = ltc.getCurrentLogContext();
    String[] params = logContext.param();
    Object target = joinPoint.getTarget();
    Class<?> targetClass = target.getClass();
    // 是否需要打印日志
    boolean printLog = logContext.printInfoLog();
    MethodSignature signature = (MethodSignature) joinPoint.getSignature();

    Logger logger = LoggerFactory.getLogger(targetClass);
    if (logger.isDebugEnabled() || printLog) {
      //===============================================================================
      //  保存action、itemType、itemId到MDC
      //===============================================================================
      saveLogInfo2MDC(joinPoint, ltc);

      StringBuilder mesInfo = new StringBuilder();
      if (target instanceof Proxy) {
        mesInfo.append(signature.getDeclaringTypeName())
            .append(".");
      }
      mesInfo.append(signature.getName());
      String profilerName = new StringBuilder(targetClass.getSimpleName()).append("#")
          .append(mesInfo.toString()).toString();
      //===============================================================================
      //  方法调用耗时统计
      //===============================================================================
      if (Boolean.TRUE.equals(logContext.enableProfiler())) {
        ProfilerAOP.handleProfiler(logger, ltc, logContext, profilerName);
      }
      mesInfo.append(" start parameters:{}");
      //
      // 如果params大于0，说明存在用户想打印的参数
      // ------------------------------------------------------------------------------
      if (params.length > 0) {
        SPELUtil spel = new SPELUtil(joinPoint);
        JSONObject json = new JSONObject();
        for (String param : params) {
          // 其他参数，spel表达式
          json.put(param, spel.cacl(param));
        }
        if (printLog) {
          logger.info(mesInfo.toString(), json.toJSONString());
        } else {
          logger.debug(mesInfo.toString(), json.toJSONString());
        }
      } else {
        // 所在的类.方法
        String methodStr = this.getMethodParam(joinPoint, logContext);
//        String methodStr = this.getMethodParam(joinPoint, excludeInParam);
        if (printLog) {
          logger.info(mesInfo.toString(), methodStr);
        } else {
          logger.debug(mesInfo.toString(), methodStr);
        }
      }
    }
    return logger;
  }
  private void saveLogInfo2MDC(ProceedingJoinPoint pjp, LogThreadContext ltc) {
    //===============================================================================
    //  保存用户信息到MDC
    //===============================================================================
    if (this.logService != null) {
      String userInfo = logService.getUserInfo();
      if (StringUtils.isNotEmpty(userInfo)) {
        MDC.put(USER, userInfo);
      }
    }

    JSONObject json = this.getLogJson(pjp, ltc);
    if (!json.isEmpty()) {
      String jsonString = json.toJSONString();
      MDC.put(LOG_JSON, jsonString);
      ltc.putCallStack(new HashMap(3) {{
        put(LOG_JSON, jsonString);
      }});
    } else {
      ltc.putCallStack(new HashMap(1));
    }
  }
  /**
   * 获取注解Log的值,构造成json，供sf4j使用
   *
   * @return com.alibaba.fastjson.JSONObject
   * @author 龚梁钧 2019-06-28 11:24
   */
  private JSONObject getLogJson(ProceedingJoinPoint pjp, LogThreadContext ltc) {
    LogContext logContext = ltc.getCurrentLogContext();
    LogConst.Action action = logContext.action();
    String itemType = logContext.itemType();
    String[] itemIds = logContext.itemIds();
    SPELUtil spel = new SPELUtil(pjp);
    JSONObject json = new JSONObject();
    // 操作
    if (!LogConst.Action.NULL.equals(action)) {
      json.put("A", action.toString());
    }
    // 对象类型
    if (StringUtils.isNotEmpty(itemType)) {
      json.put("T", itemType);
    }
    // 对象类型
    if (itemIds.length > 0) {
      for (String itemId : itemIds) {
        if (itemId.contains(DOT_NOTATION)) {
          String substring = itemId.substring(itemId.indexOf(DOT_NOTATION) + 1);
          json.put(substring, spel.cacl(itemId));
        } else {
          json.put(itemId, spel.cacl(itemId));
        }
      }
    }
    return json;
  }

  private void clearMDC(String[] clears) {
    for (String clear : clears) {
      MDC.remove(clear);
    }
  }

  private void clearMDC(Set<String> clears) {
    for (String clear : clears) {
      MDC.remove(clear);
    }
  }

  /**
   * 获取方法入参
   *
   * @return java.lang.String
   * @author 龚梁钧 2019-06-28 13:35
   */
  private String getMethodParam(ProceedingJoinPoint point, LogContext logContext) {
    Object[] methodArgs = point.getArgs();
    Parameter[] parameters = ((MethodSignature) point.getSignature()).getMethod().getParameters();
    String requestStr;
    try {

      requestStr = logParam(parameters, methodArgs, logContext);
    } catch (Exception e) {
      LOG.warn("failed to get parameters: {}", e);
      requestStr = "failed to get parameters";
    }
    return requestStr;
  }

  /**
   * 拼接入参
   *
   * @param paramsArgsName
   * @param paramsArgsValue
   * @param logContext
   * @author gongliangjun 2020-06-05 3:16 PM
   * @return java.lang.String
   */
  private String logParam(Parameter[] paramsArgsName, Object[] paramsArgsValue,
      LogContext logContext) {
    if (ArrayUtils.isEmpty(paramsArgsName) || ArrayUtils.isEmpty(paramsArgsValue)) {
      return "";
    }
    String[] excludeInParam = logContext.getExcludeInParam();
    String[] includeInParam = logContext.getIncludeInParam();
    Map<String, List<String>> excludeCollect = null;
    Map<String, List<String>> includeCollect = null;
    if (ArrayUtils.isNotEmpty(includeInParam)) {
      includeCollect = Arrays.stream(includeInParam)
          .filter(a -> a.startsWith("arg") && a.contains("."))
          .collect(Collectors.groupingBy(e -> {
            String substring = e.substring(0, 4);
            return substring;
          }));
    }
    if (ArrayUtils.isNotEmpty(excludeInParam)) {
      excludeCollect = Arrays.stream(excludeInParam)
          .filter(a -> a.startsWith("arg") && a.contains("."))
          .collect(Collectors.groupingBy(e -> {
            String substring = e.substring(0, 4);
            return substring;
          }));
    }
    StringBuffer buffer = new StringBuffer();
    Flag:
    for (int i = 0; i < paramsArgsValue.length; i++) {
      //参数名
      String name = paramsArgsName[i].getName();
      //参数值
      Object value = paramsArgsValue[i];
      //
      // 判断当前参数值是否属于excludeInParam，如果属于，则跳过不进行拼接
      // ------------------------------------------------------------------------------
      for (String exclude : excludeInParam) {
        if (name.equals(exclude)) {
          continue Flag;
        }
        Class<?> aClass = value.getClass();
        if (exclude.equals(aClass.getSimpleName()) || exclude.equals(aClass.getName())) {
          continue Flag;
        }
      }
      buffer.append(name + "=");
      if (value instanceof String) {
        buffer.append(value + ",");
      } else {
        PropertyPreFilters filter = new PropertyPreFilters();
        if (null != includeCollect) {
          List<String> list = includeCollect.get(name);
          if (null != list) {
            for (String e : list) {
              filter.addFilter(e.substring(e.indexOf(".")+1));
            }
          }
        }
        if (null != excludeCollect) {
          List<String> list = excludeCollect.get(name);
          if (null != list) {
            for (String e : list) {
              filter.addFilter().addExcludes(e.substring(e.indexOf(".")+1));
            }
          }
        }
        List<MySimplePropertyPreFilter> filters = filter.getFilters();
        if (null != filters && filters.size() > 0) {
          SimplePropertyPreFilter[] simplePropertyPreFilters = new SimplePropertyPreFilter[filters
              .size()];
          for (int i1 = 0; i1 < filters.size(); i1++) {
            simplePropertyPreFilters[i1] = filters.get(i1);
          }
          buffer.append(JSON.toJSONString(value, simplePropertyPreFilters) + ",");
        }else {
          buffer.append(JSON.toJSONString(value) + ",");
        }
      }
    }
    return buffer.toString();
  }
  @Override
  public void setApplicationContext(ApplicationContext applicationContext) {
    try {
      this.logService = applicationContext.getBean(LogService.class);
    } catch (BeansException e) {
      LOG.info("logService is null");
    }
  }
}
