package com.mostar.langchain4jtest.entity.vo;

import java.io.Serializable;
import java.util.Date;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 用户信息视图对象（返回给前端，不含密码）
 * </p>
 *
 * @author MOstAr
 * @since 2026-04-05
 */
@Data
@Builder
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class UserVO implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * 主键 ID
	 */
	private Long id;

	/**
	 * 手机号
	 */
	private String phonenum;

	/**
	 * 用户名
	 */
	private String username;

	/**
	 * 头像 URL
	 */
	private String avatarUrl;

	/**
	 * 状态 1 正常 0 禁用
	 */
	private Integer status;

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
