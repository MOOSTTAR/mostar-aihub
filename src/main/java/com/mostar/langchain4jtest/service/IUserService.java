package com.mostar.langchain4jtest.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mostar.langchain4jtest.entity.dto.UserDTO;
import com.mostar.langchain4jtest.entity.po.User;
import com.mostar.langchain4jtest.entity.vo.UserVO;

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

	/**
	 * 获取用户信息
	 */
	UserVO getUserInfo(Long userId);

	/**
	 * 更新用户信息
	 */
	boolean updateUserInfo(Long userId, UserDTO userDTO);
}
