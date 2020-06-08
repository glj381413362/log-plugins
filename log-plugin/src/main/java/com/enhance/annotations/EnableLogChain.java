package com.enhance.annotations;

import com.enhance.config.EnableLogChainConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * <p>
 *
 * </p>
 *
 * @author gongliangjun 2019/07/01 11:18
 */

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({EnableLogChainConfiguration.class})
public @interface EnableLogChain {

}
