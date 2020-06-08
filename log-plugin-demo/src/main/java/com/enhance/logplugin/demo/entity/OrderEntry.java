package com.enhance.logplugin.demo.entity;

import java.math.BigDecimal;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Data;

/**
 * <p>
 *
 * </p>
 *
 * @author gongliangjun 2019/07/01 11:18
 */
@Entity
@Table(name = "mall_order_entry")
@Data
public class OrderEntry {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long orderEntryId;

  /**
   * 订单头ID
   */
  private Long orderId;

  /**
   * sku商品ID
   */
  private Long skuId;
  /**
   * 数量
   */
  private Long quantity;

}
