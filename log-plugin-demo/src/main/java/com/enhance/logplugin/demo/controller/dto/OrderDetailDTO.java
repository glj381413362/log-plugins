package com.enhance.logplugin.demo.controller.dto;

import java.math.BigDecimal;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.groups.Default;
import lombok.Data;

/**
 * <p>
 *
 * </p>
 *
 * @author gongliangjun 2019/07/01 11:18
 */
@Data
public class OrderDetailDTO {

  @NotNull(groups = Update.class)
  private Long orderId;
  @NotEmpty(groups = Update.class)
  private String orderCode;
  @NotNull(groups = Update.class)
  private Long userId;
  private String status;
  private String desc;

  @NotNull
  private UserDTO user;

  @NotNull
  @Valid
  private List<OrderEntryDetail> orderEntryDetails;


  @Data
  public static class OrderEntryDetail {

    /**
     * 订单头ID
     */
    @NotNull(groups = Update.class)
    private Long orderId;
    private Long orderEntryId;

    /**
     * sku商品ID
     */
    @NotNull(groups = Update.class)
    private Long skuId;

    private Sku sku;
    /**
     * 数量
     */
    private Long quantity;
  }

  @Data
  public static class Sku {

    @NotNull(groups = Update.class)
    private Long skuId;
    @NotEmpty
    private String skuCode;
    private String skuName;
    private BigDecimal price;
  }

  @Data
  public static class UserDTO {
    @NotNull(groups = Update.class)
    private Long userId;
    @NotEmpty
    private String userCode;
    private String userName;
  }

  public interface Update extends Default {
  }

  public interface Insert extends Default {
  }
}
