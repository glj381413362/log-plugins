# 日常开发日志打印需要注意哪些地方

1. 不能面向debug编程，过于依赖debug，应该多依赖日志输出;
2. 代码开发测试完成之后不要急着提交，先跑一遍看看日志是否看得懂;
3. 日志必须包含哪些信息：
   – 每次请求的唯一id(便于从海量的日志里区分某次请求)；
   – 每次请求的用户信息（从海量日志里快速找到该用户做了什么);
4. 某些地方必须打印日志：
   - 分支语句的变量必须打印日志，重要参数必须打印（比如订单code等）;
   -  修改（包括新增）操作必须打印日志（出问题，做到有证可查）；
   -  数据量大的时候需要打印数据量，及耗时（用于分析性能。比如查询一个列表，要打印结果列表大小）;

# 开发过程中如何优雅解决以上问题

## 日志插件安装

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

## 日志插件使用

### 打印方法的入参和出参

经常出现正式环境出问题了，然后发现没有打印方法的入参和出参，然后重新发版就为了添加一条日志，虽然这种做法很low，但是是可能发生的。只需要添加一个@Log注解就能轻松解决。

#### 打印方法入参出参

添加注解@Log，@Log默认是debug级别才会打印入参和出参的，可通过printInfoLog来控制是否打印

1. printInfoLog为true，默认会打印INFO级别日志，但是会导致日志里增加

   ![](https://user-images.githubusercontent.com/19701761/84008646-b3883400-a9a4-11ea-9ad5-e7e93b7a8cd3.png)

2. printInfoLog默认值为false，可通过动态修改某个类的日志级别为DEBUG，达到打印方法入参和出参的目的

   ![](https://user-images.githubusercontent.com/19701761/84008648-b420ca80-a9a4-11ea-8b74-6b9ae9922ff5.png)

#### 过滤掉不需要打印的入参

当一个入参很大，某些数据打印出来没意义，或者属于敏感信息不能打印，可以通过excludeInParam过滤掉不打印。

1. 多个参数时，忽略掉某个参数不打印

   ![](https://user-images.githubusercontent.com/19701761/84008655-b5ea8e00-a9a4-11ea-9d3f-7afb6e81510c.png)

2. 多个参数时，忽略掉多个参数不打印

   ![](https://user-images.githubusercontent.com/19701761/84008662-b7b45180-a9a4-11ea-92d8-8e6d56ff93d0.png)

3. 忽略某个参数的某些属性不打印

   ![](https://user-images.githubusercontent.com/19701761/84008669-b97e1500-a9a4-11ea-9c48-3b8f3002c2c0.png)

4. 参数时集合类型，忽略掉不打印

   ![](https://user-images.githubusercontent.com/19701761/84009747-51c8c980-a9a6-11ea-998a-4f78349cf8c6.png)

#### 指定打印某些参数或属性

当一个方法有多个入参，可以通过includeInParam或param指定打印某些参数，或某些参数的指定属性，从而只打印自己关心部分的日志，减少日志量。

1. 打印某个参数的指定属性，其它参数正常打印

   ![](https://user-images.githubusercontent.com/19701761/84009756-555c5080-a9a6-11ea-9f55-b4a96c47885c.png)

2. 当excludeInParam排除参数的某个属性和includeInParam冲突时，includeInParam生效

   ![](https://user-images.githubusercontent.com/19701761/84009760-568d7d80-a9a6-11ea-8c92-88216c204ca4.png)

3. 当excludeInParam排除参数和includeInParam冲突时，excludeInParam生效

   ![](https://user-images.githubusercontent.com/19701761/84009765-57261400-a9a6-11ea-8ddd-f8471db487d5.png)

4. 当配置了param，就只会打印param配置的参数，此时excludeInParam和includeInParam不会生效

   ![](https://user-images.githubusercontent.com/19701761/84009767-57beaa80-a9a6-11ea-8193-cf58d7624e10.png)

#### 出参只打印数组大小

当方法的出参是一个集合时，可通过printOutParamSize控制是打印集合详情，还是只打印集合大小；在controller层，往往出参是包了一层，真正的数据只是出参对象的一个属性值，此时可通过继承FilterResultService类，实现shouldFilter方法和filter方法，对结果进行处理，已达到只打印集合大小和真正的数据详情。

```java
public abstract class FilterResultService<T, U> {
	public abstract boolean shouldFilter(U u);

	public Optional<T> filterResult(U u) {
		if (shouldFilter(u)) {
			log.info("Start dealing with the results");
			return Optional.ofNullable(filter(u));
		}
		return null;
	}
	public abstract T filter(U u);
}
```

默认了一个DefaultFilterResultServiceImpl对结果进行处理：

```java
@Component
public class DefaultFilterResultServiceImpl extends FilterResultService {
	@Override
	public boolean shouldFilter(Object o) {
		return false;
	}
	@Override
	public Object filter(Object o) {
		return o;
	}
}
```



### 日志尽可能多的包含有用信息

在尽可能包含更多有效信息的同时，不能增加太多的额外工作；

#### 日志包含用户信息

实现LogService接口,然后再方法上添加@Log注解即可

```java
@Component
public class UserInfoService implements LogService {

  /**
   * 获取用户信息，用于放入日志框架的MDC里
   *
   * @return java.lang.String
   * @author gongliangjun 2019-12-19 3:33 PM
   */
  @Override
  public String getUserInfo() {
    // 这里为了测试，模拟从请求中获取用户信息，可根据自己实际情况修改
    return "admin001";
  }
```

在查询订单列表上添加@Log注解

![](https://user-images.githubusercontent.com/19701761/84008610-ac612600-a9a4-11ea-962a-b43cda87f838.png)

#### 日志信息包含唯一的key

1. 比如订单修改，希望每条日志都包含订单编号

   ![](https://user-images.githubusercontent.com/19701761/84008625-aec38000-a9a4-11ea-9379-91deb9c10a5c.png)

2. 比如订单修改，希望每条日志都包含订单编号和订单所属用户

   这里的用户在请求pojo的orderDetailDTO.user.userCode里，所以注解是 `@Log(itemIds = {"orderDetailDTO.user.userCode"})`

![](https://user-images.githubusercontent.com/19701761/84008633-b08d4380-a9a4-11ea-858c-1cfaf3b23dcb.png)

3. 比如订单修改时，订单行处理时，希望每条日志信息包含订单id

   处理订单行方法入参是一个集合，所以注解是`@Log(itemIds = {"orderEntryDetails[0].orderId"})`

   ![](https://user-images.githubusercontent.com/19701761/84008638-b08d4380-a9a4-11ea-80ac-c902d6d2e751.png)

#### 日志信息包含被操作表信息及操作类型

比如在修改或者删除时，希望日志信息包含被操作的表和具体操作类型，以备出现问题时有证可查，这里需要用@Log的itemType和action，itemType表示表类型，action表示操作类型，操作有6种：

- A 新增操作
- D 删除操作
- U 更新操作
- Q 查询操作
- LQ 查询列表操作
- NULL 其他操作

添加注解`@Log(itemIds = {"orderDetailDTO.orderCode"},itemType = "order表",action = Action.U)`，效果如下:

![](https://user-images.githubusercontent.com/19701761/84008643-b2570700-a9a4-11ea-98c9-cd33f2cceb22.png)

当有海量日志时，我们根据上图一条日志信息就能知道，用户admin001对order表orderCode为MO001的订单进行了更新操作，已经具体的更新内容。

#### 多个方法使用@Log注解

这里我以一个尽可能真实的案例来举例。一个订单详情查询接口，其中包含订单头详情、订单行详情、产品详情和用户详情4个方法，大概调用情况如下：

![](https://user-images.githubusercontent.com/19701761/84009785-5b523180-a9a6-11ea-8245-d2fd7ad25016.jpg)

`queryOrderDetail`、`conversionOrder` 、 `conversionOrderEntry`、 `conversionSku`和 `conversionUser`几个方法都加@Log注解，@Log主要是基于MDC实现的，所以在`queryOrderDetail`方法调用方法`conversionOrder`，进入`conversionOrder`方法时，MDC里存入的就是`conversionOrder`方法相关的值，再回到前方法`queryOrderDetail`时，MDC中应该切换为方法`queryOrderDetail`的值。所以这里存在一个方法调用栈，也需要一个上下文来存储方法相关的MDC值。所以这里我使用了一个`LogThreadContext`上下文来存储方法相关的MDC值，和管理方法调用关系的一个stack，然后把LogThreadContext放入到`InheritableThreadLocal`中。

具体调用时，日志打印效果如下:

![](https://user-images.githubusercontent.com/19701761/84009768-57beaa80-a9a6-11ea-941b-85409d551eb0.png)以上每条日志信息都包含一个唯一的tranceID,配合日志框架，比如ELK(项目也有基于docker 快速搭建ELK日志平台的脚本)，便可以从海量日志里快速筛选出一次请求的所有日志；也包含了此次请求的用户(实现接口LogService获取用户信息)；也包含了每个方法特定的key，方便程序员追溯问题。

### 日志工具使用

通过LogUtil提供的一下方法，可以在项目开发中使打印的日志信息更有意义，简便了日志打印，从而提高工作效率。LogUtil中简便打印日志的方法，主要使用场景还是在实际项目中，当遇到循环处理逻辑时，循环体逻辑复杂，这时候需要循环体里的日志每条都包含具体处理记录的信息。LogUtil主要提供了如下几个方法：

1.  printCode(String describe, String separator, String... values)

   打印自定义描述和多个code值 需要配合@log或@LogProfiler注解一起使用，MDC才能清空 一般在循环内使用,循环结

   束要调用clearMDC()

   ```java
   # 例子：
   for (OrderEntry orderEntry : orderEntries) {
           LogUtil.printCode("orderEntry skuid和数量", "-", orderEntry.getSkuId().toString(),orderEntry.getQuantity().toString());
           log.info("开始处理...");
           log.info("处理中");
           log.info("处理结束");
           LogUtil.clearMDC();
   }
   结果：
   INFO [(orderEntry skuid和数量:1-21)]c.enhance.logplugin.demo.service.OrderServiceImpl - 开始处理...
   INFO [(orderEntry skuid和数量:1-21)]c.enhance.logplugin.demo.service.OrderServiceImpl - 处理中
   INFO [(orderEntry skuid和数量:1-21)]c.enhance.logplugin.demo.service.OrderServiceImpl - 处理结束
   ```

2.  printCode(String describe, String value)

   打印自定义描述和code值 需要配合@log或@LogProfiler注解一起使用，MDC才能清空 一般在循环内使用,循环结束要调用clearMDC()

   ```java
   # 例子：orderEntries 有两条数据
   for (OrderEntry orderEntry : orderEntries) {
           LogUtil.printCode("处理的skuid", orderEntry.getSkuId().toString());
           log.info("开始处理...");
           log.info("处理中");
           log.info("处理结束");
           LogUtil.clearMDC();
   }
   结果：
   INFO [(处理的skuid:1)]c.enhance.logplugin.demo.service.OrderServiceImpl - 开始处理...
   INFO [(处理的skuid:1)]c.enhance.logplugin.demo.service.OrderServiceImpl - 处理中
   INFO [(处理的skuid:1)]c.enhance.logplugin.demo.service.OrderServiceImpl - 处理结束
   INFO [(处理的skuid:2)]c.enhance.logplugin.demo.service.OrderServiceImpl - 开始处理...
   INFO [(处理的skuid:2)]c.enhance.logplugin.demo.service.OrderServiceImpl - 处理中
   INFO [(处理的skuid:2)]c.enhance.logplugin.demo.service.OrderServiceImpl - 处理结束
   ```

3.  printCode(String value) 

    打印特定code值 需要配合@log注解一起使用，MDC才能清空 一般在循环内使用,循环结束要调用clearMDC()

   ```java
   例子：orderEntries 有两条数据
   for (OrderEntry orderEntry : orderEntries) {
           LogUtil.printCode(orderEntry.getSkuId().toString());
           log.info("开始处理...");
           log.info("处理中");
           log.info("处理结束");
           LogUtil.clearMDC();
   }
   结果：
   INFO [1]c.enhance.logplugin.demo.service.OrderServiceImpl - 开始处理...
   INFO [1]c.enhance.logplugin.demo.service.OrderServiceImpl - 处理中
   INFO [1]c.enhance.logplugin.demo.service.OrderServiceImpl - 处理结束
   INFO [2]c.enhance.logplugin.demo.service.OrderServiceImpl - 开始处理...
   INFO [2]c.enhance.logplugin.demo.service.OrderServiceImpl - 处理中
   INFO [2]c.enhance.logplugin.demo.service.OrderServiceImpl - 处理结束
   ```

4.  printCodeByTemplate(String template, Object... values) 

   自定义模板打印日志 需要配合@log注解一起使用，MDC才能清空 一般在循环内使用,循环结束要调用clearMDC()

   ```java
   # 例子：orderEntries 有两条数据
   for (OrderEntry orderEntry : orderEntries) {
           LogUtil.printCodeByTemplate("skuId[{}] 数量[{}]",orderEntry.getSkuId(),orderEntry.getQuantity());
           log.info("开始处理...");
           log.info("处理中");
           log.info("处理结束");
           LogUtil.clearMDC();
   }
   结果：
   INFO [skuId:1 数量:21]c.enhance.logplugin.demo.service.OrderServiceImpl - 开始处理...
   INFO [skuId:1 数量:21]c.enhance.logplugin.demo.service.OrderServiceImpl - 处理中
   INFO [skuId:1 数量:21]c.enhance.logplugin.demo.service.OrderServiceImpl - 处理结束
   INFO [skuId:2 数量:11]c.enhance.logplugin.demo.service.OrderServiceImpl - 开始处理...
   INFO [skuId:2 数量:11]c.enhance.logplugin.demo.service.OrderServiceImpl - 处理中
   INFO [skuId:2 数量:11]c.enhance.logplugin.demo.service.OrderServiceImpl - 处理结束
   
   ```

### 方法执行耗时统计

实际项目可能需要对一个方法执行耗时进行统计，以便找到效率比较低的方法，后续好做系统优化。通过日志插件的`@EnableProfiler`和`@LogProfiler`注解，可以轻松实现对一个方法执行时间进行统计。`@EnableProfiler`只对方法进行耗时统计，不具备打印日志功能；`@LogProfiler`具备日志打印和耗时统计功能，相当于@Log和@`@EnableProfiler`。

#### 方法调用执行耗时统计

这里还是以订单详情查询作为例子，在每个需要统计的方法上添加`@EnableProfiler`或`@LogProfiler`注解

![](https://user-images.githubusercontent.com/19701761/84009775-59886e00-a9a6-11ea-9745-27fbd111f040.png)

#### 方法内代码块执行耗时统计

这里还是以订单详情查询接口举例，在queryOrderDetail方法内单独统计了查询一个订单所用时长，和模拟一段复杂逻辑执行所用时长。通过LogUtil的`startProfiler(name)`方法对方法内的代码块执行耗时进行统计，然后在整个执行耗时统计里打印出来。

```java
  @LogProfiler(itemIds = "orderCode")
  @Override
  public OrderDetailDTO queryOrderDetail(String orderCode) {
    log.info("开始处理订单数据");
    SleepUtil.threadSleep(2);
    Order order1 = new Order();
    order1.setOrderCode(orderCode); 
    // 想统计查询一个订单所用时间
    LogThreadContext ltc = LogUtil.startProfiler("findOne");
    Optional<Order> orderOptional = orderMapper.findOne(Example.of(order1));
    ltc.stopInstantProfiler();

    //统计一段复杂逻辑耗时
    LogUtil.startProfiler("复杂逻辑统计");
    log.info("模拟这里需要处理一大段逻辑");
    SleepUtil.threadSleep(4,10);
    ltc.stopInstantProfiler();
    ......
    ......
    log.info("订单数据处理完成");
    return detailDTO;
  }

```

![](https://user-images.githubusercontent.com/19701761/84009777-5a210480-a9a6-11ea-9eb5-c0a2b012e354.png)

## 日志插件核心类

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

   