-- 用户表新增字段迁移脚本
-- 执行时间：2026-05-04

USE langchain4j_test;

-- 添加邮箱字段（可空）
ALTER TABLE `user`
ADD COLUMN `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱';

-- 添加个性签名字段（可空）
ALTER TABLE `user`
ADD COLUMN `bio` VARCHAR(200) DEFAULT NULL COMMENT '个性签名';

-- 添加生日字段
ALTER TABLE `user`
ADD COLUMN `birthday` DATE DEFAULT NULL COMMENT '生日';

-- 添加性别字段 0 未知 1 男 2 女
ALTER TABLE `user`
ADD COLUMN `gender` TINYINT NOT NULL DEFAULT 0 COMMENT '性别 0 未知 1 男 2 女';

-- 添加 GitHub 链接字段
ALTER TABLE `user`
ADD COLUMN `github_url` VARCHAR(200) DEFAULT NULL COMMENT 'GitHub 空间链接';
