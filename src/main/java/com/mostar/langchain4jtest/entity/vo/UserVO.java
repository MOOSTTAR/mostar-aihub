package com.mostar.langchain4jtest.entity.vo;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 用户登录表
 * </p>
 *
 * @author MOstAr
 * @since 2026-04-05
 */
@Data
@Builder
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("user")
public class UserVO implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * 主键ID（无符号整数，支持42.9亿用户量）
	 */
	@TableId(value = "id", type = IdType.AUTO)
	private Long id;

	/**
	 * 用户名，最大10个字符（可包含汉字/字母/数字）
	 */
	@TableField("username")
	private String username;

	/**
	 * jwt令牌
	 */
	@TableField("status")
	private String token;

}
