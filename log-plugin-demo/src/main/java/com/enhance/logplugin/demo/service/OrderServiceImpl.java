package com.enhance.logplugin.demo.service;

import com.common.tools.util.AopProxy;
import com.common.tools.util.BeanUtil;
import com.common.tools.util.ListUtil;
import com.common.tools.util.exception.CommonException;
import com.common.tools.util.pojo.Msg;
import com.enhance.annotations.EnableProfiler;
import com.enhance.annotations.Log;
import com.enhance.annotations.LogProfiler;
import com.enhance.aspect.LogThreadContext;
import com.enhance.constant.LogConst.Action;
import com.enhance.logplugin.demo.controller.dto.OrderDetailDTO;
import com.enhance.logplugin.demo.controller.dto.OrderDetailDTO.OrderEntryDetail;
import com.enhance.logplugin.demo.controller.dto.OrderDetailDTO.UserDTO;
import com.enhance.logplugin.demo.dao.OrderEntryMapper;
import com.enhance.logplugin.demo.dao.OrderMapper;
import com.enhance.logplugin.demo.dao.SkuMapper;
import com.enhance.logplugin.demo.dao.UserMapper;
import com.enhance.logplugin.demo.entity.Order;
import com.enhance.logplugin.demo.entity.OrderEntry;
import com.enhance.logplugin.demo.entity.Sku;
import com.enhance.logplugin.demo.entity.User;
import com.enhance.logplugin.demo.util.SleepUtil;
import com.enhance.util.LogUtil;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.XSlf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;

/**
 * <p>
 *
 * </p>
 *
 * @author gongliangjun 2019/07/01 11:18
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@XSlf4j
public class OrderServiceImpl implements AopProxy<OrderService>, OrderService {

  private final UserMapper userMapper;
  private final OrderMapper orderMapper;
  private final OrderEntryMapper orderEntryMapper;
  private final SkuMapper skuMapper;


  /**
   * 列表查询
   *
   * @return java.util.List<com.enhance.logplugin.demo.entity.Order>
   * @author gongliangjun 2020-06-02 4:09 PM
   */
  @Log(itemIds = "order.orderCode",itemType = "order",action = Action.Q)
  @Override
  public List<Order> listOrder(Order order) {
    List<Order> orders = orderMapper.findAll(Example.of(order));
    return orders;
  }


  /**
   * 根据订单编码查询订单详情
   *
   * @return com.enhance.logplugin.demo.controller.dto.OrderDetailDTO
   * @author gongliangjun 2020-06-02 4:10 PM
   */
  @LogProfiler(itemIds = "orderCode")
  @Override
  public OrderDetailDTO queryOrderDetail(String orderCode) {
    log.info("开始处理订单数据");

    SleepUtil.threadSleep(2);
    Order order1 = new Order();
    order1.setOrderCode(orderCode);
    LogThreadContext ltc = LogUtil.startProfiler("findOne");
    Optional<Order> orderOptional = orderMapper.findOne(Example.of(order1));
    ltc.stopInstantProfiler();

    LogUtil.startProfiler("复杂逻辑统计");
    log.info("这里需要处理一大段逻辑");
    SleepUtil.threadSleep(4,10);
    ltc.stopInstantProfiler();

    SleepUtil.threadSleep(2);
    OrderDetailDTO detailDTO = new OrderDetailDTO();
    if (orderOptional.isPresent()) {
      Order order = orderOptional.get();
      //===============================================================================
      //  组装订单dto
      //===============================================================================
      self().conversionOrder(order, detailDTO);
      //===============================================================================
      //  组装订单行dto
      //===============================================================================
      try {
        self().conversionOrderEntry(order, detailDTO);
        SleepUtil.threadSleep(2);
      } catch (Exception e) {
        log.throwing(e);
      }
      //===============================================================================
      //  组装用户
      //===============================================================================
      self().conversionUser(order.getUserId(), detailDTO);
      SleepUtil.threadSleep(2);

    }
    log.info("订单数据处理完成");
    return detailDTO;
  }

  @LogProfiler(itemIds = "userId")
  @Override
  public void conversionUser(Long userId, OrderDetailDTO detailDTO) {
    log.info("开始处理用户数据");
    Optional<User> byId = userMapper.findById(userId);
    byId.ifPresent(user -> {
      UserDTO userDTO = new UserDTO();
      detailDTO.setUser(userDTO);
      BeanUtil.copySourceToTarget(user, userDTO);
    });
    log.info("用户数据处理完成");

  }

  @Override
  @Transactional
  @Log(itemIds = {"orderDetailDTO.orderCode"},itemType = "order表",action = Action.U)
  public Order update(OrderDetailDTO orderDetailDTO) {
    log.info("开始修改订单...");
    List<OrderEntryDetail> orderEntryDetails = orderDetailDTO.getOrderEntryDetails();
    self().handOrderEntry(orderEntryDetails);
    UserDTO user = orderDetailDTO.getUser();
    Optional<User> userOptional = userMapper.findById(user.getUserId());
    userOptional.ifPresent(user1 -> {
      BeanUtil.copySourceToTarget(user, user1);
      userMapper.saveAndFlush(user1);
    });
    Optional<Order> orderOptional = orderMapper.findById(orderDetailDTO.getOrderId());
    Order order = orderOptional.orElseThrow(
        () -> new CommonException(new Msg("根据订单id[{}],未查询到相应订单"), orderDetailDTO.getOrderId()));
    BeanUtil.copySourceToTarget(orderDetailDTO, order);
    order = orderMapper.saveAndFlush(order);
    log.info("修改订单结束");
    return order;
  }

  @LogProfiler(excludeInParam = {"arg2","arg1.userId"},includeInParam = "arg0.orderCode")
  @Override
  public Order handelUpdate(OrderDetailDTO orderDetailDTO, UserDTO userDTO,
      List<OrderEntryDetail> orderEntryDetails) {
    return null;
  }

  @Override
  @LogProfiler(itemIds = {"orderEntryDetails[0].orderId"})
  public void handOrderEntry(List<OrderEntryDetail> orderEntryDetails) {
    log.info("开始修改订单行...");
    for (OrderEntryDetail orderEntryDetail : orderEntryDetails) {
      Optional<OrderEntry> byId = orderEntryMapper.findById(orderEntryDetail.getOrderEntryId());
      byId.ifPresent(orderEntry -> {
        BeanUtil.copySourceToTarget(orderEntryDetail, orderEntry);
        orderEntryMapper.saveAndFlush(orderEntry);
      });
      OrderDetailDTO.Sku sku = orderEntryDetail.getSku();
      Optional<Sku> optionalSku = skuMapper.findById(orderEntryDetail.getSkuId());
      optionalSku.ifPresent(sku1 -> {
        BeanUtil.copySourceToTarget(sku, sku1);
        skuMapper.saveAndFlush(sku1);
      });
    }
    log.info("修改订单行完成");
  }

//  @EnableProfiler

  @LogProfiler(itemIds = "order.orderCode")
  @Override
  public void conversionOrder(Order order, OrderDetailDTO detailDTO) {

    SleepUtil.threadSleep(2);


    BeanUtil.copySourceToTarget(order, detailDTO);

//    self().tempA();
  }

//  @EnableProfiler
  @Override
  public void tempA() {
    SleepUtil.threadSleep(2);
    LogThreadContext ltc = LogUtil.startProfiler("A");
    SleepUtil.threadSleep(2);
    ltc.stopInstantProfiler();
//    self().tempB();

  }

//  @EnableProfiler
  @Override
  public void tempB() {
    SleepUtil.threadSleep(2);
    LogThreadContext ltc = LogUtil.startProfiler("B");
    SleepUtil.threadSleep(2);
    ltc.stopInstantProfiler();

  }
  @LogProfiler(itemIds = "order.orderCode")
  @Override
  public void conversionOrderEntry(Order order, OrderDetailDTO detailDTO) {
    log.info("开始处理订单行数据");

    OrderEntry query = new OrderEntry();
    query.setOrderId(order.getOrderId());
    List<OrderEntry> orderEntries = orderEntryMapper.findAll(Example.of(query));
    if (ListUtil.listIsNotEmpty(orderEntries, "不为空")) {
      ArrayList<OrderEntryDetail> orderEntryArrayList = Lists
          .newArrayListWithCapacity(orderEntries.size());
      for (OrderEntry orderEntry : orderEntries) {
        //===============================================================================
        //  测试LogsUtil.printCode(String describe, String separator, String... values)方法
        //===============================================================================
        LogUtil.printCode("orderEntry skuid和数量", "-", orderEntry.getSkuId().toString(),
            orderEntry.getQuantity().toString());
        log.info("开始处理...");
        log.info("处理中");
        log.info("处理结束");

        //===============================================================================
        //  测试LogsUtil.printCode(String describe, String value)方法
        //===============================================================================
        LogUtil.printCode("处理的skuid", orderEntry.getSkuId().toString());
        log.info("开始处理...");
        log.info("处理中");
        log.info("处理结束");
        LogUtil.clearMDC();


        //===============================================================================
        //  测试LogsUtil.printCode(String value)方法
        //===============================================================================
        LogUtil.printCode(orderEntry.getSkuId().toString());
        log.info("开始处理...");
        log.info("处理中");
        log.info("处理结束");
        LogUtil.clearMDC();

        //===============================================================================
        //  测试LogsUtil.printCodeByTemplate(String template, Object... values)方法
        //===============================================================================
        LogUtil.printCodeByTemplate("skuId:{} 数量:{}",orderEntry.getSkuId(),orderEntry.getQuantity());
        log.info("开始处理...");
        log.info("处理中");
        log.info("处理结束");
        LogUtil.clearMDC();




        OrderEntryDetail orderEntryDetail = new OrderEntryDetail();
        BeanUtil.copySourceToTarget(orderEntry, orderEntryDetail);
        orderEntryArrayList.add(orderEntryDetail);
        //===============================================================================
        //  组装商品dto
        //===============================================================================
        OrderDetailDTO.Sku sku = new OrderDetailDTO.Sku();
        orderEntryDetail.setSku(sku);
        try {
          self().conversionSku(orderEntry.getSkuId(), sku);
        } catch (Exception e) {
          log.catching(e);
        }
      }
      detailDTO.setOrderEntryDetails(orderEntryArrayList);
    } else {
      throw new CommonException(new Msg("根据orderCode:{}未查询到订单行数据"), order.getOrderCode());
    }
    log.info("订单行数据处理完成");

  }

  @LogProfiler(itemIds = "skuId",excludeInParam = "arg1")
  @Override
  public void conversionSku(Long skuId, OrderDetailDTO.Sku skuDto) {
    log.info("开始处理商品数据");
    Optional<Sku> skuOptional = skuMapper.findById(skuId);
    Sku sku = skuOptional
        .orElseThrow(() -> new CommonException(new Msg("根据skuId:{}未查询到商品数据"), skuId));
    BeanUtil.copySourceToTarget(sku, skuDto);
    log.info("商品数据处理结束");

  }

}
