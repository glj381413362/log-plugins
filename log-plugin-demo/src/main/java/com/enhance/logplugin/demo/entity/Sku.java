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
@Table(name = "mall_sku")
@Data
public class Sku {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long skuId;
  private String skuCode;
  private String skuName;
  private BigDecimal price;
}
