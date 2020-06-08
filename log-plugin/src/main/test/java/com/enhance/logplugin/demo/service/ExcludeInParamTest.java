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
public class ExcludeInParamTest {

  BDto bDto = new BDto(){{
    setId("0001");
    setName("bdto");
  }};
  UserDto user = new UserDto(){{
    setName("bob");
    setPassword("123456");
  }};

  @Test
  public void testLog(){
    ExcludeInParamTest target = new ExcludeInParamTest();
    AspectJProxyFactory factory = new AspectJProxyFactory(target);
    LogAOP aspect = new LogAOP();
    factory.addAspect(aspect);
    ExcludeInParamTest proxy = factory.getProxy();
    proxy.excludeInParamTest1("aaa", bDto, user);
  }

  /**
   * 排除第二个参数bDto不输出
   *
   * @param a
   * @param bDto
   * @param user
   * @author gongliangjun 2020-06-05 5:24 PM
   * @return void
   */
  @Log(excludeInParam = {"arg2"})
  public void excludeInParamTest1(String a,BDto bDto , UserDto user){


    log.info("excludeInParamTest1 .....");

  }

}
