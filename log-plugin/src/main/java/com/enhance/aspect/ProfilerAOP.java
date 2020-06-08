package com.enhance.aspect;


import com.enhance.annotations.EnableProfiler;
import com.enhance.annotations.Log;
import com.enhance.annotations.LogProfiler;
import com.enhance.aspect.LogThreadContext.LogContext;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import lombok.SneakyThrows;
import lombok.extern.slf4j.XSlf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.profiler.Profiler;
import org.slf4j.profiler.ProfilerRegistry;
import org.slf4j.profiler.TimeInstrument;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;


/**
 * description
 *
 *
 * @author 龚梁钧 2019/06/27 18:27
 */
@Aspect
@Component
@XSlf4j
@Order(10) //order最小最先执行  保证ProfilerAOP 后于LogAop执行
public class ProfilerAOP {

  /**
   * 打印方法执行耗时统计
   *
   *
   * @param ltc
   * @author gongliangjun 2020-06-04 2:28 PM
   * @return void
   */
  static void printProfiler(LogThreadContext ltc) {
    Profiler parent = ltc.getFirstProfiler();
    //===============================================================================
    //  当第一个方法是
    //===============================================================================
    if (ltc.isFirstMethod() && null != parent) {
      TimeInstrument timeInstrument = parent.stop();
      timeInstrument.print();
      ProfilerRegistry profilerRegistry = ProfilerRegistry.getThreadContextInstance();
      profilerRegistry.clear();
      ltc.removeCurrentProfiler();
      ltc.removeCurrentLogContext();
    } else {
      ltc.removeCurrentProfiler();
      try {
        ltc.changeCurrentLogContext();
      } catch (Exception e) {
        log.warn("changeCurrentLogContext error:{}", e);
      }
    }
  }
  /**
   * 处理方法执行耗时统计
   *
   * @param logger
   * @param ltc
   * @param logContext
   * @param profilerName
   * @author gongliangjun 2020-06-04 2:28 PM
   * @return void
   */
  static void handleProfiler(Logger logger, LogThreadContext ltc, LogContext logContext,
      String profilerName) {
    // 在线程上下文的探查器注册表中注册此探查器
    ProfilerRegistry profilerRegistry = ProfilerRegistry.getThreadContextInstance();


    if (ltc.isFirstProfiler()) {
      Profiler parent = new Profiler(profilerName);
      ltc.put(profilerName, parent);
      parent.registerWith(profilerRegistry);
      parent.setLogger(logger);
      parent.start(profilerName);
    } else {
      String name = logContext.profilerName();
      if (StringUtils.isNotBlank(name)) {
        profilerName = name;
      }
      Profiler parent = ltc.getPrevProfiler();
      parent.setLogger(logger);
      Profiler childProfiler = parent.startNested(profilerName);
      // 获取注册的分析器
      childProfiler.start(profilerName);
      //===============================================================================
      //  将childProfiler放入log上下文中
      //===============================================================================
      ltc.putProfiler(childProfiler);
    }
  }

  @Pointcut("@annotation(com.enhance.annotations.EnableProfiler)")
  public void profilerPoint() {
  }

  @SneakyThrows
  @Around("profilerPoint()")
  public Object handlerProfilerMethod(ProceedingJoinPoint joinPoint) {
    MethodSignature signature = (MethodSignature) joinPoint.getSignature();
    Method method = signature.getMethod();
    LogProfiler logProfiler = method.getAnnotation(LogProfiler.class);
    if (logProfiler != null) {
      return joinPoint.proceed();
    } else {
      Object target = joinPoint.getTarget();
      Class<?> targetClass = target.getClass();
      Logger logger = LoggerFactory.getLogger(targetClass);
      StringBuilder methodName = new StringBuilder();
      if (target instanceof Proxy) {
        methodName.append(signature.getDeclaringTypeName())
            .append(".");
      }
      methodName.append(signature.getName());
      LogThreadContext ltc = LogThreadContext.getLogThreadContext();

      initLogContext(method, ltc);

      LogContext logContext = ltc.getCurrentLogContext();
      String profilerName = methodName.toString();
      Object result = null;
      try {
        try {
          handleProfiler(logger, ltc, logContext, profilerName);
        } catch (Exception e) {
          log.warn("handlerProfilerMethod error:{}", e);
        }
        result = joinPoint.proceed();
      } finally {
        printProfiler(ltc);
      }
      return result;
    }
  }
  private void initLogContext(Method method, LogThreadContext ltc) {
    EnableProfiler enableProfiler = method.getAnnotation(EnableProfiler.class);
    Log logAnnotation = method.getAnnotation(Log.class);
    LogContext logContext;
    if (logAnnotation != null) {
      //===============================================================================
      //  说明该方法已经在LogThreadContext 的logContexts里存过值了，这里只需取出来setProfilerName、setEnableProfiler
      //===============================================================================
      logContext = ltc.peekContext();
      logContext.setEnableLog(true);
    } else {
      logContext = new LogContext();

      ltc.putContext(logContext);
    }
    logContext.setEnableProfiler(true);
    String profilerName = enableProfiler.profilerName();
    logContext.setProfilerName(profilerName);
    ltc.setCurrentLogContext(logContext);
  }
}
