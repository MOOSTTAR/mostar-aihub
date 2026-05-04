package com.mostar.langchain4jtest.entity.dto;

import java.io.Serializable;
import java.util.Date;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 用户信息数据传输对象（接收前端提交的修改数据）
 * </p>
 *
 * @author MOstAr
 * @since 2026-04-05
 */
@Data
@Builder
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class UserDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * 手机号
	 */
	private String phonenum;

	/**
	 * 用户名
	 */
	private String username;

	/**
	 * 邮箱
	 */
	private String email;

	/**
	 * 个性签名
	 */
	private String bio;

	/**
	 * 生日
	 */
	private Date birthday;

	/**
	 * 性别 0 未知 1 男 2 女
	 */
	private Integer gender;

	/**
	 * GitHub 空间链接
	 */
	private String githubUrl;

}
