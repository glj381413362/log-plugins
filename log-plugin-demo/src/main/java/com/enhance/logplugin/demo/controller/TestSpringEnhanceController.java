package com.enhance.logplugin.demo.controller;

import com.common.tools.util.exception.BaseException;
import com.common.tools.util.exception.BusinessExceptionAssert;
import com.enhance.logplugin.demo.dao.OrderMapper;
import com.enhance.logplugin.demo.entity.Order;
import com.enhance.logplugin.demo.service.OrderService;
import com.enhance.spring.controller.data.Res;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 *
 * </p>
 *
 * @author gongliangjun 2019/07/01 11:18
 */
@RestController
@RequestMapping(value = "/test")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TestSpringEnhanceController {
  private final OrderMapper orderMapper;

  private final OrderService orderService;
  @GetMapping("/testOne")
  public Order testOne() {
    return orderMapper.findAll().get(0);
  }
  @GetMapping("/testPage")
  public List<Order> testPage() {
    return orderMapper.findAll();
  }
  @GetMapping("/testRes")
  public Res testRes() {
    Res res = Res.successBody(orderService.listOrder(new Order()));
    return res.buildResponse();
  }
  @GetMapping("/testResPartialSucces")
  public Res testResPartialSucces() {
    Res<Object> build = Res.builder().build();
    build.add2Failed(new Order(){{setDesc("失败了");}});
    build.add2Success(new Order(){{setDesc("成功了");}});
    Res res = build.buildResponse();
    return res;
  }

  @GetMapping("/testBaseException")
  public Res testBaseException() {
    Res<Object> build = Res.builder().build();
    build.add2Failed(new Order(){{setDesc("失败了");}});
    build.add2Success(new Order(){{setDesc("成功了");}});
    if (1==1){
      throw new BaseException("测试{}","抛异常");
    }
    return build.buildResponse();
  }
  @GetMapping("/testExceptionAssert")
  public Res testExceptionAssert() {
    Res<Object> build = Res.builder().build();
    build.add2Failed(new Order(){{setDesc("失败了");}});
    build.add2Success(new Order(){{setDesc("成功了");}});
    Order order =null;
    BusinessExceptionAssert.BUSINESS_EXCEPTION.assertNotNull(order);
    return build.buildResponse();
  }

}
