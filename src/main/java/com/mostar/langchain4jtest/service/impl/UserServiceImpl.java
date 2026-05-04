package com.mostar.langchain4jtest.service.impl;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mostar.langchain4jtest.entity.dto.UserDTO;
import com.mostar.langchain4jtest.entity.po.User;
import com.mostar.langchain4jtest.entity.vo.UserVO;
import com.mostar.langchain4jtest.mapper.UserMapper;
import com.mostar.langchain4jtest.service.IUserService;

import jakarta.annotation.Resource;

/**
 * <p>
 * 用户登录表 服务实现类
 * </p>
 *
 * @author MOstAr
 * @since 2026-04-05
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

	@Resource
	private PasswordEncoder passwordEncoder;

	/**
	 * 根据用户名查询用户
	 */
	@Override
	public User getByUsername(String username) {
		LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
		wrapper.eq(User::getUsername, username);
		return getOne(wrapper);
	}

	/**
	 * 注册新用户（密码自动加密）
	 */
	@Override
	public boolean register(String username, String phonenum, String rawPassword, String avatarUrl) {
		if (username == null || username.trim().isEmpty() || phonenum == null || phonenum.trim().isEmpty()
				|| rawPassword == null || rawPassword.isEmpty()) {
			throw new IllegalArgumentException("用户名、手机号、密码不能为空");
		}
		// TODO 验证码 前端验证手机号格式，密码格式，用户名格式
		User user = new User();
		user.setUsername(username);
		user.setPhonenum(phonenum);
		user.setPassword(passwordEncoder.encode(rawPassword));
		user.setAvatarUrl(avatarUrl);
		user.setStatus(1);
		return save(user);
	}

	/**
	 * 校验密码
	 */
	@Override
	public boolean checkPassword(String rawPassword, String encodedPassword) {
		return passwordEncoder.matches(rawPassword, encodedPassword);
	}

	/**
	 * 用户登录（内部使用）
	 */
	public User loginByUsernameAndPassword(String username, String rawPassword) {
		User user = getByUsername(username);
		if (user != null && checkPassword(rawPassword, user.getPassword())) {
			return user;
		}
		return null;
	}

	/**
	 * 获取用户信息
	 */
	@Override
	public UserVO getUserInfo(Long userId) {
		User user = getById(userId);
		if (user == null) {
			return null;
		}
		return UserVO.builder()
				.id(user.getId())
				.phonenum(user.getPhonenum())
				.username(user.getUsername())
				.avatarUrl(user.getAvatarUrl())
				.status(user.getStatus())
				.email(user.getEmail())
				.bio(user.getBio())
				.birthday(user.getBirthday())
				.gender(user.getGender())
				.githubUrl(user.getGithubUrl())
				.build();
	}

	/**
	 * 更新用户信息
	 */
	@Override
	public boolean updateUserInfo(Long userId, UserDTO userDTO) {
		User user = getById(userId);
		if (user == null) {
			return false;
		}

		// 更新字段
		if (userDTO.getUsername() != null) {
			user.setUsername(userDTO.getUsername());
		}
		if (userDTO.getPhonenum() != null) {
			user.setPhonenum(userDTO.getPhonenum());
		}
		if (userDTO.getEmail() != null) {
			user.setEmail(userDTO.getEmail());
		}
		if (userDTO.getBio() != null) {
			user.setBio(userDTO.getBio());
		}
		if (userDTO.getBirthday() != null) {
			user.setBirthday(userDTO.getBirthday());
		}
		if (userDTO.getGender() != null) {
			user.setGender(userDTO.getGender());
		}
		if (userDTO.getGithubUrl() != null) {
			user.setGithubUrl(userDTO.getGithubUrl());
		}

		return updateById(user);
	}
}
