package com.enhance.logplugin.demo.service;

import com.enhance.annotations.Log;
import com.enhance.aspect.LogAOP;
import com.enhance.logplugin.demo.dto.BDto;
import com.enhance.logplugin.demo.dto.UserDto;
import lombok.extern.slf4j.XSlf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * <p>
 *
 * </p>
 *
 * @author gongliangjun 2019/07/01 11:18
 */

@RunWith(SpringRunner.class)
@SpringBootTest
@XSlf4j
public class IncludeInParamTests {

  BDto bDto = new BDto() {{
    setId("0001");
    setName("bdto");
  }};
  UserDto user = new UserDto() {{
    setName("bob");
    setPassword("123456");
  }};

  @Test
  public void testLog() {
    IncludeInParamTests target = new IncludeInParamTests();
    AspectJProxyFactory factory = new AspectJProxyFactory(target);
    LogAOP aspect = new LogAOP();
    factory.addAspect(aspect);
    IncludeInParamTests proxy = factory.getProxy();
    proxy.includeInParamTest1("test1", bDto, user);
    proxy.includeInParamTest2("test2", bDto, user);
    proxy.inludeInParamTest3("test3", bDto, user);

    log.info("a");
    log.info("a");
    log.info("a");
    log.info("a");
    log.info("a");
    log.info("a");
    log.info("a");
    log.info("a");
    log.info("a");
    log.info("a");
    log.info("a");
    log.info("a");
    log.info("a");
    log.info("a");
    log.info("a");
    log.info("a");
    log.info("a");
    log.info("a");
    log.info("a");
    log.info("a");
    log.info("a");
    log.info("a");
    log.info("a");
    log.info("a");
    log.info("a");
    log.info("a");
    log.info("a");
    log.info("a");

    log.info("a");
    log.info("a");
    log.info("a");
    log.info("a");
    log.info("a");
    log.info("a");
    log.info("a");
    log.info("a");
    log.info("a");
    log.info("a");
    log.info("a");
    log.info("a");
    log.info("a");
    log.info("a");
    log.info("a");
    log.info("a");
    log.info("a");
    log.info("a");
    log.info("a");
    log.info("a");
    log.info("a");
    log.info("a");
    log.info("a");
    log.info("a");
    log.info("a");
    log.info("a");
    log.info("a");
    log.info("a");
  }

  /**
   * 其他参数正常打印，第三个参数只打印user的name属性
   *
   * @return void
   * @author gongliangjun 2020-06-05 5:24 PM
   */
  @Log(includeInParam = {"arg2.name"})
  public void includeInParamTest1(String a, BDto bDto, UserDto user) {

    log.info("------------- includeInParamTest1 start------------- ");
    log.info("其他参数正常打印，第三个参数只打印user的name属性");
    log.info("@Log(includeInParam = {\"arg2.name\"})");
    log.info("public void includeInParamTest1(String a, BDto bDto, UserDto user)");
    log.info("------------- includeInParamTest1 end  ------------- ");


  }

  /**
   * 当excludeInParam排除参数的某个属性和includeInParam冲突时，includeInParam生效
   *
   * @return void
   * @author gongliangjun 2020-06-05 5:24 PM
   */
  @Log(excludeInParam = {"arg2.password"},includeInParam = {"arg2.password"})
  public void includeInParamTest2(String a, BDto bDto, UserDto user) {

    log.info("------------- includeInParamTest2 start------------- ");
    log.info("当excludeInParam排除参数的某个属性和includeInParam冲突时，includeInParam生效");
    log.info("@Log(excludeInParam = {\"arg2.password\"},includeInParam = {\"arg2.password\"})");
    log.info("public void includeInParamTest2(String a, BDto bDto, UserDto user)");
    log.info("------------- includeInParamTest2 end  ------------- ");
  }
  /**
   * 当excludeInParam排除参数和includeInParam冲突时，excludeInParam生效
   *
   * @return void
   * @author gongliangjun 2020-06-05 5:24 PM
   */
  @Log(excludeInParam = {"arg2"} , includeInParam = {"arg2.password"})
  public void inludeInParamTest3(String a, BDto bDto, UserDto user) {

    log.info("------------- includeInParamTest3 start------------- ");
    log.info("当excludeInParam排除参数和includeInParam冲突时，excludeInParam生效 ");
    log.info("@Log(excludeInParam = {\"arg2\"} , includeInParam = {\"arg2.password\"})");
    log.info("public void includeInParamTest3(String a, BDto bDto, UserDto user)");
    log.info("------------- includeInParamTest3 end  ------------- ");
  }

}
