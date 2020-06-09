# 日志插件想解决那些问题
1. 不能面向debug编程，过于依赖debug，应该多依赖日志输出;
2. 代码开发测试完成之后不要急着提交，先跑一遍看看日志是否看得懂;
3. 日志必须包含哪些信息:
   - 每次请求的唯一id(便于从海量的日志里区分某次请求)；
   - 每次请求的用户信息（从海量日志里快速找到该用户做了什么);
4. 某些地方必须打印日志：
   - 分支语句的变量必须打印日志，重要参数必须打印（比如订单code等）;
   - 修改（包括新增）操作必须打印日志（出问题，做到有证可查）；
   - 数据量大的时候需要打印数据量，及耗时（用于分析性能。比如查询一个列表，要打印结果列表大小）;
# 日志插件安装

下载源码，然后`mvn install`到自己的maven仓库，最后在项目中添加如下依赖:

```xml
 <dependency>
      <groupId>com.enhance</groupId>
      <artifactId>log-plugin</artifactId>
      <version>1.0-SNAPSHOT</version>
 </dependency>
```

项目添加日志配置文件，日志配置文件在日志插件里有案例，详情可参考日志插件的logback.xml文件，主要对日志输出格式做如下配置:

```xml
<encoder class="ch.qos.logback.classic.encoder.PatternLayoutEncoder">
            <!--格式化输出：%d表示日期，%thread表示线程名，%-5level：级别从左显示5个字符宽度%msg：日志消息，%n是换行符-->
            <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread %X{X-B3-TraceId}] %-5level[%X{user} %X{logjson} %X{code}]%logger{50} - %msg%n
            </pattern>
            <charset>UTF-8</charset>
        </encoder>
```

- %X{X-B3-TraceId} ：一次请求的唯一traceID
- %X{user} : 一次请求的用户信息，通过实现接口LogService获取用户信息，然后存入MDC
- %X{logjson} : 存放action、itemType和itemIds值
- %X{code}: 方法内一些想存放入MDC的值

# 日志平台（ELK）安装

下载源码去到nodes-stand-alone目录执行`init.sh`脚本即可,具体搭建参考[CSDN](https://blog.csdn.net/qq_21239913/article/details/103888765)或[博客](http://jessica.glj-site.com:8888/?p=211)。这里搭建的ELK是在单台服务器上，不适合生产环境使用。

# 功能介绍

## 打印方法入参出参

方法添加注解`@Log`，所有入参、出参都打印

## 过滤掉不需要打印的入参

1. 方法添加`@Log(excludeInParam = {"arg1"})`:排除第二个参数不打印
2. `@Log(excludeInParam = {"arg0", "arg1"})`:多个参数时，忽略掉多个参数不打印
3. `@Log(excludeInParam = {"arg2.password"})`:忽略第二个参数的password属性不打印

## 指定打印某些参数或属性

1. `@Log(includeInParam = {"arg2.name"})`:其他参数正常打印，第三个参数只打印user的name属性
2. `@Log(excludeInParam = {"arg2.password"},includeInParam = {"arg2.password"})`:当excludeInParam排除参数的某个属性和includeInParam冲突时，includeInParam生效
3. `@Log(excludeInParam = {"arg2"} , includeInParam = {"arg2.password"})`:当excludeInParam排除参数和includeInParam冲突时，excludeInParam生效

## 日志包含用户信息

实现LogService接口,然后在方法上添加@Log注解即可让日志信息包含用户信息

## 日志信息包含唯一的key

1. `@LogProfiler(itemIds = "order.orderCode")`:比如订单修改，希望每条日志都包含订单编号
2. `@Log(itemIds = {"order.user.userCode","order.orderCode"})`:比如订单修改，希望每条日志都包含订单编号和订单所属用户
3. `@Log(itemIds = {"orderEntryDetails[0].orderId"})`: 比如订单修改时，订单行处理时，希望每条日志信息包含订单id

## 日志信息包含被操作表信息及操作类型

`@Log(itemIds = {"orderDetailDTO.orderCode"},itemType = "order表",action = Action.U)`:当有海量日志时，可根据一条日志信息就能知道，具体用户对表的某条记录进行的具体的操作，及具体的操作内容。

## 方法执行耗时统计

实际项目可能需要对一个方法执行耗时进行统计，以便找到效率比较低的方法，后续好做系统优化。通过日志插件的`@EnableProfiler`和`@LogProfiler`注解，可以轻松实现对一个方法执行时间进行统计。`@EnableProfiler`只对方法进行耗时统计，不具备打印日志功能；`@LogProfiler`具备日志打印和耗时统计功能，相当于@Log和`@EnableProfiler`。

1. 方法调用执行耗时统计
2. 方法内代码块执行耗时统计

## 日志工具

1. `printCode(String describe, String separator, String... values)`:后续日志信息打印自定义描述和多个code值
2. `printCode(String describe, String value)`:后续日志打印自定义描述和code值
3. `printCodeByTemplate(String template, Object... values) `:后续日志打印自定义模板日志



# 日志插件使用

具体使用案例可参考:[CSDN](https://blog.csdn.net/qq_21239913/article/details/106624875)  、[博客](http://jessica.glj-site.com:8888/?p=251)  

# 日志插件核心类

1. LogThreadContext日志上下文，存放了方法相关的日志信息

   ```java
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
   
   ```

   

2. LogAop

   ```java
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
   
   
   ```

3. ProfilerAOP，方法执行耗时统计切面

   ```java
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
   
   ```

4. LogUtil工具类

   ```java
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
   
   ```

   