package com.mostar.langchain4jtest.entity.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

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
public class UserDTO implements Serializable {

    private static final long serialVersionUID = 1L;

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
     * 1正常 0禁用
     */
    @TableField("status")
    private Integer status;
}
