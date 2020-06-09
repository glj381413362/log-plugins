package com.enhance.logplugin.demo.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
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
@Slf4j
public class TestLogTest {

  TestLog testLog = new TestLog();

  @Test
  public void testTestA() throws Exception {
    testLog.testA();
  }

  @Test
  public void logTest1() throws Exception {
    testLog.testA();
  }

}

//Generated with love by TestMe :) Please report issues and submit feature requests at: http://weirddev.com/forum#!/testme