package com.mostar.langchain4jtest.entity.po;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

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
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("user")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID（无符号整数，支持42.9亿用户量）
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 手机号（11位数字）
     */
    @TableField("phonenum")
    private String phonenum;

    /**
     * 用户名，最大10个字符（可包含汉字/字母/数字）
     */
    @TableField("username")
    private String username;

    /**
     * 密码（8-20位数字字母组合，仅数字和字母，加盐哈希）
     */
    @TableField("password")
    private String password;

    /**
     * 用户头像的阿里云OSS访问地址
     */
    @TableField("avatar_url")
    private String avatarUrl;

    /**
     * 1正常 0禁用
     */
    @TableField("status")
    private Integer status;


}
