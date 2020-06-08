package com.enhance.logplugin.demo.service;

import com.enhance.annotations.Log;
import com.enhance.aspect.LogAOP;
import com.enhance.logplugin.demo.dto.BDto;
import com.enhance.logplugin.demo.dto.UserDto;
import java.util.HashMap;
import java.util.Map;
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
public class ExcludeInParamTests {

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
    ExcludeInParamTests target = new ExcludeInParamTests();
    AspectJProxyFactory factory = new AspectJProxyFactory(target);
    LogAOP aspect = new LogAOP();
    factory.addAspect(aspect);
    ExcludeInParamTests proxy = factory.getProxy();
    proxy.excludeInParamTest1("test1", bDto, user);
    proxy.excludeInParamTest2("test2", bDto, user);
    proxy.excludeInParamTest3("test3", bDto, user);
    proxy.excludeInParamTest4("test4", bDto, user,new HashMap(){{put("a","a");}});

  }

  /**
   * 排除第二个参数不输出
   *
   * @return void
   * @author gongliangjun 2020-06-05 5:24 PM
   */
  @Log(excludeInParam = {"arg1"})
  public void excludeInParamTest1(String a, BDto bDto, UserDto user) {

    log.info("------------- excludeInParamTest1 start------------- ");
    log.info("排除第二个参数不输出");
    log.info("@Log(excludeInParam = {\"arg1\"})");
    log.info("public void excludeInParamTest1(String a, BDto bDto, UserDto user)");
    log.info("------------- excludeInParamTest1 end  ------------- ");
  }

  /**
   * 排除第一、二个参数不输出
   *
   * @return void
   * @author gongliangjun 2020-06-05 5:24 PM
   */
  @Log(excludeInParam = {"arg0", "arg1"})
  public void excludeInParamTest2(String a, BDto bDto, UserDto user) {

    log.info("------------- excludeInParamTest2 start------------- ");
    log.info("排除第一、二个参数不输出");
    log.info("@Log(excludeInParam = {\"arg0\", \"arg1\"})");
    log.info("public void excludeInParamTest2(String a, BDto bDto, UserDto user)");
    log.info("------------- excludeInParamTest2 end  ------------- ");

  }
  /**
   * 排除user的password属性不输出
   *
   * @return void
   * @author gongliangjun 2020-06-05 5:24 PM
   */
  @Log(excludeInParam = {"arg2.password"})
  public void excludeInParamTest3(String a, BDto bDto, UserDto user) {

    log.info("------------- excludeInParamTest3 start------------- ");
    log.info("排除user的password属性不输出");
    log.info("@Log(excludeInParam = {\"arg2.password\"})");
    log.info("public void excludeInParamTest3(String a, BDto bDto, UserDto user)");
    log.info("------------- excludeInParamTest3 end  ------------- ");

  }
  /**
   * 参数是集合或者map时,使用 'arg+数字' 来进行排除
   *
   * @return void
   * @author gongliangjun 2020-06-05 5:24 PM
   */
  @Log(excludeInParam = {"arg3"})
  public void excludeInParamTest4(String a, BDto bDto, UserDto user, Map map) {

    log.info("------------- excludeInParamTest4 start------------- ");
    log.info("参数是集合或者map时,使用 'arg+数字' 来进行排除");
    log.info("@Log(excludeInParam = {\"arg2.password\"})");
    log.info("public void excludeInParamTest4(String a, BDto bDto, UserDto user, Map map)");
    log.info("------------- excludeInParamTest4 end  ------------- ");

  }

}
