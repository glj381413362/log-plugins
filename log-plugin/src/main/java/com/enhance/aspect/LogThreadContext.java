package com.enhance.aspect;

import com.enhance.constant.LogConst;
import com.enhance.constant.LogConst.Action;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import lombok.Getter;
import lombok.extern.slf4j.XSlf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.slf4j.profiler.Profiler;

/**
 * <p>
 * log上下文
 * </p>
 *
 * @author gongliangjun 2019/07/01 11:18
 */
@XSlf4j
public final class LogThreadContext {

  public final static ThreadLocal<LogThreadContext> LOG_THREAD_CONTEXT = new InheritableThreadLocal<>();
  private final static String TRACEID = "X-B3-TraceId";

  /**
   * 当前方法的日志上下文
   */
  private LogContext currentLogContext;
  //瞬间统计器  通过LogsUtil.startProfiler 产生的Profiler
  private Profiler instantProfiler;

  /**
   * 方法调用时日志上下文存放的一个栈
   */
  private Stack<LogContext> logContexts = new Stack<>();

  /**
   * 方法调用时MDC相关信息存放的一个栈
   */
  private Stack<Map<String, String>> callStack = new Stack<>();

  /**
   * 方法耗时统计器存放
   */
  private Map<String, Profiler> profilerMap = new HashMap<String, Profiler>();

  /**
   * 设置当前方法内的耗时统计器  每调用一次 {@link LogUtil#startProfiler}就会设置方法内的耗时统计器
   *
   * @param instantProfiler
   * @author gongliangjun 2020-06-08 1:31 PM
   * @return void
   */
  public void setInstantProfiler(Profiler instantProfiler) {
    this.instantProfiler = instantProfiler;
  }
  /**
   * 停止当前方法内的耗时统计器
   *
   * @author gongliangjun 2020-06-08 1:31 PM
   * @return void
   */
  public void stopInstantProfiler() {
    if (null != instantProfiler) {
      instantProfiler.stop();
    }
  }

  /**
   * 获取当前线程的日志上下文
   *
   *
   * @author gongliangjun 2020-06-08 1:33 PM
   * @return com.enhance.aspect.LogThreadContext
   */
  public final static LogThreadContext getLogThreadContext() {
    LogThreadContext logThreadContext = LOG_THREAD_CONTEXT.get();
    if (logThreadContext == null) {
      logThreadContext = new LogThreadContext();
      LOG_THREAD_CONTEXT.set(logThreadContext);
    }
    return logThreadContext;
  }
  /**
   * 存放方法耗时统计器到profilerMap
   *
   * @param profiler
   * @author gongliangjun 2020-06-08 1:34 PM
   * @return void
   */
  protected void putProfiler(Profiler profiler) {
    put(profiler.getName(), profiler);
  }

  /**
   * 移除当前方法耗时统计器
   *
   * @author gongliangjun 2020-06-08 1:36 PM
   * @return void
   */
  protected void removeCurrentProfiler() {
    if (profilerMap.size() < 2) {
      profilerMap.clear();
    } else {
      String profilerName = currentLogContext.getProfilerName();
      profilerMap.remove(profilerName);
    }
  }
  /**
   * 指定名称存放方法耗时统计器到profilerMap
   *
   * @param profiler
   * @author gongliangjun 2020-06-08 1:34 PM
   * @return void
   */
  protected void put(String name, Profiler profiler) {
    if (profilerMap.isEmpty()) {
      String key = MDC.get(TRACEID);
      if (StringUtils.isBlank(key)) {
        key = "parent";
      }
      //===============================================================================
      //  设置耗时统计器名称
      //===============================================================================
      this.currentLogContext.setProfilerName(key);
      profilerMap.put(key, profiler);
    } else {
      //===============================================================================
      //  设置耗时统计器名称
      //===============================================================================
      this.currentLogContext.setProfilerName(name);
      profilerMap.put(name, profiler);
    }
  }
  /**
   * 从profilerMap获取指定名称的耗时统计器
   *
   * @param name
   * @author gongliangjun 2020-06-08 1:34 PM
   * @return void
   */
  public Profiler get(String name) {
    return profilerMap.get(name);
  }

  /**
   * 获取第一个方法的方法耗时统计器
   * 第一个方法耗时统计器名称为标记一次请求的唯一traceID
   *
   * @author gongliangjun 2020-06-08 1:34 PM
   * @return void
   */
  public Profiler getFirstProfiler() {
    String key = MDC.get(TRACEID);
    if (StringUtils.isBlank(key)) {
      key = "parent";
    }
    return profilerMap.get(key);
  }
  /**
   * 获取上一个方法的耗时统计器
   *
   *
   * @author gongliangjun 2020-06-08 1:40 PM
   * @return org.slf4j.profiler.Profiler
   */
  public Profiler getPrevProfiler() {
    LogContext currentContext = logContexts.pop();
    LogContext prevContext = logContexts.peek();
    logContexts.push(currentContext);
    return profilerMap.get(prevContext.getProfilerName());
  }
  /**
   * 判断是否为第一个耗时统计器
   *
   *
   * @author gongliangjun 2020-06-08 1:40 PM
   * @return org.slf4j.profiler.Profiler
   */
  public boolean isFirstProfiler() {
    return profilerMap.isEmpty();
  }
  /**
   * 判断是否为第一个方法调用
   *
   *
   * @author gongliangjun 2020-06-08 1:40 PM
   * @return org.slf4j.profiler.Profiler
   */
  public boolean isFirstMethod() {
    return profilerMap.size() == 1;
  }
  protected void clear() {
    //防止内存泄漏  改成在AddTraceIdFilter 进行删除
    LOG_THREAD_CONTEXT.remove();
  }
  public Stack<Map<String, String>> getCallStack() {
    return callStack;
  }
  /**
   * 保存方法调用上下文到栈
   *
   * @param logContext
   * @author gongliangjun 2020-06-08 1:42 PM
   * @return java.util.Stack<com.enhance.aspect.LogThreadContext.LogContext>
   */
  protected Stack<LogContext> putContext(LogContext logContext) {
    logContexts.push(logContext);
    return logContexts;
  }
  protected LogContext peekContext() {
    return logContexts.peek();
  }
  public LogContext getCurrentLogContext() {
    return currentLogContext;
  }
  protected void setCurrentLogContext(LogContext currentLogContext) {
    this.currentLogContext = currentLogContext;
  }
  protected LogContext changeCurrentLogContext() {
    if (!logContexts.isEmpty()) {
      //拿到栈顶元素
      LogContext peek = logContexts.peek();
      String currentLogContextProfilerName = currentLogContext.getProfilerName();
      String profilerName = peek.getProfilerName();
      //对比当前log上下文是否为同一个
      if (currentLogContextProfilerName.equals(profilerName)) {
        //===============================================================================
        //  移除栈顶元素，并且修改当前log上下文
        //===============================================================================
        logContexts.pop();
        this.currentLogContext = logContexts.peek();
      }
    }
    return currentLogContext;
  }
  protected void removeCurrentLogContext() {
    if (logContexts.empty()) {
      return;
    } else {
      logContexts.pop();
    }
  }
  protected Stack<Map<String, String>> putCallStack(Map<String, String> callStackMap) {
    callStack.push(callStackMap);
    return callStack;
  }
  protected Map<String, String> popCallStack() {
    return callStack.pop();
  }
  public boolean isEmptyCallStack() {
    return callStack.isEmpty();
  }
  public boolean isEnableLog() {
    if (null != currentLogContext) {
      return currentLogContext.isEnableLog();
    } else {
      return false;
    }
  }
  public boolean isEnableProfiler() {
    if (null != currentLogContext) {
      return currentLogContext.isEnableProfiler();
    } else {
      return false;
    }
  }
  public Map<String, String> peekCallStack() {
    return callStack.peek();
  }
  @Getter
  public static class LogContext {
    private LogConst.Action action;

    // 对象类型
    private String itemType;

    // 对象ID
    private String[] itemIds;

    // 对象类型
    private boolean printInfoLog;
    // 开启方法调用耗时统计
    private boolean enableProfiler;
    //分线器名称，不传默认为方法名称
    private String profilerName;

    // 如果是数组 是否打印出参大小，不打印对象值
    private boolean printOutParamSize;

    // 需要排除不打印的入参
    private String[] excludeInParam;
    //需要打印的入参
    private String[] includeInParam;
    // （其他）参数
    private String[] param;

    private boolean enableLog;

    protected boolean isEnableLog() {
      return enableLog;
    }
    protected void setEnableLog(boolean enableLog) {
      this.enableLog = enableLog;
    }
    protected Action action() {
      return action;
    }
    protected String itemType() {
      return itemType;
    }
    protected String[] itemIds() {
      return itemIds;
    }
    protected boolean printInfoLog() {
      return printInfoLog;
    }
    protected boolean enableProfiler() {
      return enableProfiler;
    }
    protected String profilerName() {
      return profilerName;
    }
    protected boolean printOutParamSize() {
      return printOutParamSize;
    }
    protected String[] excludeInParam() {
      return excludeInParam;
    }
    protected String[] includeInParam() {
      return includeInParam;
    }
    protected String[] param() {
      return param;
    }
    protected void setAction(Action action) {
      this.action = action;
    }
    protected void setItemType(String itemType) {
      this.itemType = itemType;
    }
    protected void setItemIds(String[] itemIds) {
      this.itemIds = itemIds;
    }
    protected void setPrintInfoLog(boolean printInfoLog) {
      this.printInfoLog = printInfoLog;
    }
    protected void setEnableProfiler(boolean enableProfiler) {
      this.enableProfiler = enableProfiler;
    }
    protected void setProfilerName(String profilerName) {
      this.profilerName = profilerName;
    }
    protected void setPrintOutParamSize(boolean printOutParamSize) {
      this.printOutParamSize = printOutParamSize;
    }
    protected void setExcludeInParam(String[] excludeInParam) {
      this.excludeInParam = excludeInParam;
    }
    protected void setIncludeValue(String[] includeInParam) {
      this.includeInParam = includeInParam;
    }
    protected void setParam(String[] param) {
      this.param = param;
    }
  }
}

