package com.enhance.logplugin.demo.dao;

import static com.enhance.logplugin.demo.util.SleepUtil.threadSleep;

import com.enhance.logplugin.demo.entity.OrderEntry;
import com.enhance.logplugin.demo.entity.Sku;
import com.google.common.collect.Lists;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

/**
 * <p>
 *
 * </p>
 *
 * @author gongliangjun 2019/07/01 11:18
 */
public interface SkuMapper  extends JpaRepository<Sku,Long> {
/*
  *//**
   * 模拟数据库
   *//*
  private static final List<Sku> DATABASES = Lists.newArrayList();

  *//**
   * 初始化数据
   *//*
  static {
    DATABASES.add(new Sku() {{
      setSkuCode("sku001");
      setSkuName("华为meta30");
      setPrice(new BigDecimal(5000));
    }});
    DATABASES.add(new Sku() {{
      setSkuCode("sku002");
      setSkuName("华为meta30Pro");
      setPrice(new BigDecimal(8000));
    }});
  }

  public Optional<Sku> selectSkuBySkuCode(String skuCode) {
    //随机休眠几秒
    threadSleep(1, 3);
    Optional<Sku> first = DATABASES.stream().filter(sku -> sku.getSkuCode().equals(skuCode))
        .findFirst();
    return first;
  }*/

}
