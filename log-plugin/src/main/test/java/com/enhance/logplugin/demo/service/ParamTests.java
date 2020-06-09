package com.enhance.logplugin.demo.service;

import com.enhance.annotations.Log;
import com.enhance.aspect.LogAOP;
import com.enhance.logplugin.demo.dto.BDto;
import com.enhance.logplugin.demo.dto.UserDto;
import com.enhance.logplugin.demo.entity.User;
import java.util.ArrayList;
import java.util.List;
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
public class ParamTests {

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
    ParamTests target = new ParamTests();
    AspectJProxyFactory factory = new AspectJProxyFactory(target);
    LogAOP aspect = new LogAOP();
    factory.addAspect(aspect);
    ParamTests proxy = factory.getProxy();
    proxy.paramTest1("test1", bDto, user,new ArrayList<UserDto>(){{
      add(new UserDto() {{
        setName("jack");
        setPassword("888888");
      }});
      add(new UserDto() {{
        setName("bob");
        setPassword("666666");
      }});
    }});
  }

  /**
   * 当配置了param，就只会打印param配置的参数，此时excludeInParam和includeInParam不会生效
   *
   * @return void
   * @author gongliangjun 2020-06-05 5:24 PM
   */
  @Log(excludeInParam = {"arg2.name"}, includeInParam = {"arg2.password"}, param = {"user.name", "a", "bDto.id","userDtos[1].password"})
  public void paramTest1(String a, BDto bDto, UserDto user, List<UserDto> userDtos) {

    log.info("------------- paramTest1 start------------- ");
    log.info("当配置了param，就只会打印param配置的参数，此时excludeInParam和includeInParam不会生效");
    log.info("@Log(excludeInParam = {\"arg2.name\"}, includeInParam = {\"arg2.password\"}, param = {\"user.name\", \"a\", \"bDto.id\"})");
    log.info("public void includeInParamTest1(String a, BDto bDto, UserDto user)");
    log.info("------------- paramTest1 end  ------------- ");


  }

}
