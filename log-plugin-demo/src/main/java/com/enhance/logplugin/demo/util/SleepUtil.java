package com.enhance.logplugin.demo.util;

import lombok.extern.slf4j.XSlf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.ext.XLoggerFactory;

/**
 * <p>
 *
 * </p>
 *
 * @author gongliangjun 2019/07/01 11:18
 */
@XSlf4j
public class SleepUtil {

  public static void threadSleep(int first ,int second) {
    try {
      Thread.sleep((first + (int) (Math.random() * (second - first))) * 1000);
    } catch (InterruptedException e) {
      log.catching(e);
    }
  }
  public static void threadSleep(int first) {
    try {
      Thread.sleep(first * 1000);
    } catch (InterruptedException e) {
      log.catching(e);
    }
  }

}
