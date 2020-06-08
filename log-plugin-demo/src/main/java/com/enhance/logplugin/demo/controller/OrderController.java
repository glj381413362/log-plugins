package com.enhance.logplugin.demo.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.serializer.SimplePropertyPreFilter;
import com.alibaba.fastjson.support.spring.PropertyPreFilters;
import com.alibaba.fastjson.support.spring.PropertyPreFilters.MySimplePropertyPreFilter;
import com.enhance.annotations.EnableProfiler;
import com.enhance.annotations.Log;
import com.enhance.annotations.LogProfiler;
import com.enhance.constant.LogConst.Action;
import com.enhance.logplugin.demo.controller.dto.OrderDetailDTO;
import com.enhance.logplugin.demo.controller.dto.OrderDetailDTO.OrderEntryDetail;
import com.enhance.logplugin.demo.controller.dto.OrderDetailDTO.UserDTO;
import com.enhance.logplugin.demo.entity.Order;
import com.enhance.logplugin.demo.service.OrderService;
import com.enhance.logplugin.demo.util.SleepUtil;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.XSlf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 *
 * </p>
 *
 * @author gongliangjun 2019/07/01 11:18
 */
@XSlf4j
@RestController
@RequestMapping(value = "/orders")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class OrderController {

  private final OrderService orderService;



  /**
   * 获取订单列表
   *
   * @param order
   * @author gongliangjun 2020-06-05 9:40 AM
   * @return java.util.List<com.enhance.logplugin.demo.entity.Order>
   */
  @Log(itemIds = "order.orderCode",itemType = "order",action = Action.Q,printOutParamSize = false)
  @GetMapping()
  public List<Order> queryOrder(Order order) {
    log.info("开始查询订单列表...");
    return orderService.listOrder(order);
  }


  @LogProfiler(itemIds = "orderCode",itemType = "order",action = Action.Q)
  @GetMapping("/{orderCode}")
  public OrderDetailDTO queryOrder(@PathVariable("orderCode") String orderCode) {

    OrderDetailDTO orderDetailDTO = orderService.queryOrderDetail(orderCode);
    return orderDetailDTO;
  }

  /**
   * 订单修改
   *
   * @param orderDetailDTO
   * @author gongliangjun 2020-06-05 9:56 AM
   * @return com.enhance.logplugin.demo.entity.Order
   */
  @PutMapping()
  @Log(excludeInParam = {"OrderDetailDTO","UserDTO"})
  public Order updateOrder(@RequestBody @Validated(value = {OrderDetailDTO.Update.class}) OrderDetailDTO orderDetailDTO) {
    return null;
//    return orderService.update(orderDetailDTO);
  }
  @PostMapping("/batch-update")
  @Log(excludeInParam = {"OrderDetailDTO","UserDTO"})
  public Order batchUpdateOrder(@RequestBody @Validated(value = {OrderDetailDTO.Update.class}) List<OrderDetailDTO> detailDTOS) {
    return null;
//    return orderService.update(orderDetailDTO);
  }
  @PostMapping("/update")
  public Order update(@RequestBody OrderDetailDTO detailDTO) {
    @NotNull UserDTO user = detailDTO.getUser();
    @NotNull @Valid List<OrderEntryDetail> orderEntryDetails = detailDTO.getOrderEntryDetails();
    OrderDetailDTO dto = detailDTO;
    dto.setUser(null);
    dto.setOrderEntryDetails(null);
    return orderService.handelUpdate(dto, user, orderEntryDetails);
  }

}
