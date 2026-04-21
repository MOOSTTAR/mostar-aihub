-- MOstAr AI Hub 数据库初始化脚本
-- 创建数据库（如未创建）
CREATE DATABASE IF NOT EXISTS langchain4j_test
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE langchain4j_test;

-- 用户表
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键 ID（无符号整数，支持 42.9 亿用户量）',
  `phonenum` VARCHAR(11) DEFAULT NULL COMMENT '手机号（11 位数字）',
  `username` VARCHAR(20) NOT NULL COMMENT '用户名，最大 20 个字符',
  `password` VARCHAR(60) NOT NULL COMMENT '密码（加盐哈希，BCrypt）',
  `avatar_url` VARCHAR(500) DEFAULT NULL COMMENT '用户头像的阿里云 OSS 访问地址',
  `status` TINYINT DEFAULT 1 COMMENT '1 正常 0 禁用',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_phonenum` (`phonenum`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 插入默认测试用户（用户名：test，密码：test123456）
-- BCrypt 加密后的密码：$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
INSERT INTO `user` (`phonenum`, `username`, `password`, `avatar_url`, `status`)
VALUES ('13800138000', 'test', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', NULL, 1);

-- LangChain4j 聊天记忆表（可选，如果使用 MySQL 存储对话历史）
DROP TABLE IF EXISTS `chat_memory`;
CREATE TABLE `chat_memory` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `memory_id` VARCHAR(100) NOT NULL COMMENT '会话 ID',
  `msg_index` INT NOT NULL COMMENT '消息索引',
  `role` VARCHAR(20) NOT NULL COMMENT '角色：USER/ASSISTANT',
  `content` TEXT NOT NULL COMMENT '消息内容',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_memory_id` (`memory_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI 聊天记忆表';

-- 会话表（可选，如果使用 MySQL 存储会话元数据）
DROP TABLE IF EXISTS `chat_session`;
CREATE TABLE `chat_session` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `memory_id` VARCHAR(100) NOT NULL COMMENT '会话 ID',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户 ID',
  `title` VARCHAR(100) DEFAULT NULL COMMENT '会话标题',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_memory_id` (`memory_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='聊天会话表';
