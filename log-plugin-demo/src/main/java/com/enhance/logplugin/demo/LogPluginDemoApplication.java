package com.enhance.logplugin.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScans;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.util.Assert;

@EnableAspectJAutoProxy(exposeProxy=true)
@SpringBootApplication
@EnableZuulProxy
public class LogPluginDemoApplication {

  public static void main(String[] args) {
    try {
      SpringApplication.run(LogPluginDemoApplication.class, args);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}
