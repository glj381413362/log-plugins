package com.enhance.logplugin.demo.dao;

import com.enhance.logplugin.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * <p>
 *
 * </p>
 *
 * @author gongliangjun 2019/07/01 11:18
 */
public interface UserMapper extends JpaRepository<User,Long> {

}
