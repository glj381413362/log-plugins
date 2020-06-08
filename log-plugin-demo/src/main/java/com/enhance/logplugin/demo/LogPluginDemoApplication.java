package com.enhance.logplugin.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScans;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableAspectJAutoProxy(exposeProxy=true)
@SpringBootApplication
public class LogPluginDemoApplication {

  public static void main(String[] args) {
    SpringApplication.run(LogPluginDemoApplication.class, args);
  }

}
