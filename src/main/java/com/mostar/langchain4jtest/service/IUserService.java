package com.mostar.langchain4jtest.service;

import com.mostar.langchain4jtest.entity.dto.UserDTO;
import com.mostar.langchain4jtest.entity.po.User;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 用户登录表 服务类
 * </p>
 *
 * @author MOstAr
 * @since 2026-04-05
 */
public interface IUserService extends IService<User> {

    User getByUsername(String username);

    boolean register(String username, String phonenum, String rawPassword, String avatarUrl);

    boolean checkPassword(String rawPassword, String encodedPassword);

    User login(UserDTO userDTO);
}
