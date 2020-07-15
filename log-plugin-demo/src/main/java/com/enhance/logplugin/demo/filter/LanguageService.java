package com.enhance.logplugin.demo.filter;

import com.enhance.spring.controller.GetLanguageService;
import java.util.Locale;
import org.apache.commons.lang3.LocaleUtils;
import org.springframework.stereotype.Component;

/**
 * <p>
 *
 * </p>
 *
 * @author gongliangjun 2019/07/01 11:18
 */
@Component
public class LanguageService implements GetLanguageService {

  @Override
  public Locale getLanguage() {
    return LocaleUtils.toLocale("en_US");
  }
}
