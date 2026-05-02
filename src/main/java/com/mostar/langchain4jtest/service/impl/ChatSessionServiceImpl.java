package com.mostar.langchain4jtest.service.impl;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.mostar.langchain4jtest.entity.dto.ChatSessionDTO;
import com.mostar.langchain4jtest.service.ChatSessionService;
import com.mostar.langchain4jtest.utils.JwtUtil;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ChatSessionServiceImpl implements ChatSessionService {

	@Resource
	private StringRedisTemplate stringRedisTemplate;

	@Resource
	private JwtUtil jwtUtil;

	private static final String SESSION_KEY_PREFIX = "chat:sessions:";
	private static final String SESSION_INFO_PREFIX = "chat:session:info:";

	@Override
	public List<ChatSessionDTO> getUserSessions(HttpServletRequest request) {
		Long userId = getUserIdFromRequest(request);
		if (userId == null) {
			return List.of();
		}

		String sessionKey = SESSION_KEY_PREFIX + userId;
		Set<String> memoryIds = stringRedisTemplate.opsForSet().members(sessionKey);

		if (memoryIds == null || memoryIds.isEmpty()) {
			return List.of();
		}

		return memoryIds.stream().map(memoryId -> {
			String infoKey = SESSION_INFO_PREFIX + memoryId;
			Map<Object, Object> info = stringRedisTemplate.opsForHash().entries(infoKey);

			ChatSessionDTO dto = new ChatSessionDTO();
			dto.setMemoryId(memoryId);
			dto.setTitle((String) info.getOrDefault("title", "新会话"));
			Object createTime = info.get("createTime");
			if (createTime != null) {
				dto.setCreateTime(Long.parseLong(createTime.toString()));
			} else {
				dto.setCreateTime(System.currentTimeMillis());
			}
			// 新增：读取 isPinned 字段，默认为 false
			Object isPinnedObj = info.get("isPinned");
			dto.setIsPinned(isPinnedObj != null && "1".equals(isPinnedObj.toString()));
			// 新增：读取 pinnedAt 字段
			Object pinnedAtObj = info.get("pinnedAt");
			if (pinnedAtObj != null) {
				dto.setPinnedAt(Long.parseLong(pinnedAtObj.toString()));
			}
			return dto;
		}).sorted((a, b) -> {
			boolean aPinned = Boolean.TRUE.equals(a.getIsPinned());
			boolean bPinned = Boolean.TRUE.equals(b.getIsPinned());

			if (aPinned && !bPinned) return -1;  // a 置顶，b 未置顶，a 在前
			if (!aPinned && bPinned) return 1;   // b 置顶，a 未置顶，b 在前
			if (aPinned && bPinned) {
				// 都置顶，按 pinnedAt 降序
				return Long.compare(b.getPinnedAt() != null ? b.getPinnedAt() : 0L, a.getPinnedAt() != null ? a.getPinnedAt() : 0L);
			}
			// 都未置顶，按 createTime 降序
			return Long.compare(b.getCreateTime(), a.getCreateTime());
		}).collect(Collectors.toList());
	}

	@Override
	public void deleteSession(String memoryId, HttpServletRequest request) {
		Long userId = getUserIdFromRequest(request);
		if (userId == null) {
			throw new RuntimeException("未登录");
		}

		String sessionKey = SESSION_KEY_PREFIX + userId;
		String infoKey = SESSION_INFO_PREFIX + memoryId;
		String contentKey = "chat:session:content:" + memoryId;
		String historyKey = "chat:session:history:" + memoryId;

		// 从用户会话列表中移除
		stringRedisTemplate.opsForSet().remove(sessionKey, memoryId);
		// 删除会话信息
		stringRedisTemplate.delete(infoKey);
		// 删除会话内容（AI 记忆）
		stringRedisTemplate.delete(contentKey);
		// 删除历史记录
		stringRedisTemplate.delete(historyKey);

		log.info("用户 {} 删除会话 {}", userId, memoryId);
	}

	@Override
	public void createSession(String memoryId, String firstMessage, Long userId) {
		if (userId == null) {
			return;
		}

		String sessionKey = SESSION_KEY_PREFIX + userId;
		String infoKey = SESSION_INFO_PREFIX + memoryId;

		// 添加到用户会话列表
		stringRedisTemplate.opsForSet().add(sessionKey, memoryId);

		// 检查会话是否已存在
		Boolean exists = stringRedisTemplate.hasKey(infoKey);

		if (exists != null && exists) {
			// 会话已存在，不更新标题（保持第一个问题的标题）
			log.info("用户 {} 在会话 {} 中继续对话", userId, memoryId);
		} else {
			// 新会话，保存会话信息
			Map<String, String> info = new HashMap<>();
			String title = truncateMessage(firstMessage, 20);
			info.put("title", title);
			info.put("createTime", String.valueOf(System.currentTimeMillis()));
			info.put("userId", String.valueOf(userId));
			info.put("isPinned", "0");  // 新增：默认不置顶

			stringRedisTemplate.opsForHash().putAll(infoKey, info);
			log.info("用户 {} 创建新会话 {}，标题：{}", userId, memoryId, title);
		}
	}

	@Override
	@SuppressWarnings("null")
	public void updateSessionTitle(String memoryId, String title, Long userId) {
		if (userId == null || memoryId == null) {
			return;
		}

		String infoKey = SESSION_INFO_PREFIX + memoryId;
		stringRedisTemplate.opsForHash().put(infoKey, "title", truncateMessage(title, 20));
	}

	@Override
	public void deleteSessions(List<String> memoryIds, Long userId) {
		if (memoryIds == null || memoryIds.isEmpty()) {
			return;
		}

		String sessionKey = SESSION_KEY_PREFIX + userId;

		for (String memoryId : memoryIds) {
			String infoKey = SESSION_INFO_PREFIX + memoryId;
			String contentKey = "chat:session:content:" + memoryId;
			String historyKey = "chat:session:history:" + memoryId;

			// 从用户会话列表中移除
			stringRedisTemplate.opsForSet().remove(sessionKey, memoryId);
			// 删除会话信息
			stringRedisTemplate.delete(infoKey);
			// 删除会话内容（AI 记忆）
			stringRedisTemplate.delete(contentKey);
			// 删除历史记录
			stringRedisTemplate.delete(historyKey);

			log.info("用户 {} 批量删除会话 {}", userId, memoryId);
		}

		log.info("用户 {} 批量删除 {} 个会话完成", userId, memoryIds.size());
	}

	@Override
	public void updateSessionPinStatus(String memoryId, Boolean isPinned, Long userId) {
		if (userId == null || memoryId == null) {
			return;
		}

		String infoKey = SESSION_INFO_PREFIX + memoryId;

		// 检查会话是否存在且属于当前用户
		Object sessionUserId = stringRedisTemplate.opsForHash().get(infoKey, "userId");
		if (sessionUserId == null || !userId.toString().equals(sessionUserId.toString())) {
			throw new RuntimeException("会话不存在或无权操作");
		}

		if (Boolean.TRUE.equals(isPinned)) {
			// 置顶：设置 isPinned=1 和 pinnedAt=当前时间戳
			stringRedisTemplate.opsForHash().put(infoKey, "isPinned", "1");
			stringRedisTemplate.opsForHash().put(infoKey, "pinnedAt", String.valueOf(System.currentTimeMillis()));
			log.info("用户 {} 置顶会话 {}", userId, memoryId);
		} else {
			// 取消置顶：设置 isPinned=0 并删除 pinnedAt
			stringRedisTemplate.opsForHash().put(infoKey, "isPinned", "0");
			stringRedisTemplate.opsForHash().delete(infoKey, "pinnedAt");
			log.info("用户 {} 取消置顶会话 {}", userId, memoryId);
		}
	}

	@Override
	public void updateSessionTitleById(String memoryId, String title, Long userId) {
		if (userId == null || memoryId == null || title == null) {
			return;
		}

		String infoKey = SESSION_INFO_PREFIX + memoryId;

		// 检查会话是否存在且属于当前用户
		Object sessionUserId = stringRedisTemplate.opsForHash().get(infoKey, "userId");
		if (sessionUserId == null || !userId.toString().equals(sessionUserId.toString())) {
			throw new RuntimeException("会话不存在或无权操作");
		}

		// 更新标题
		stringRedisTemplate.opsForHash().put(infoKey, "title", truncateMessage(title, 20));
		log.info("用户 {} 更新会话 {} 标题为：{}", userId, memoryId, truncateMessage(title, 20));
	}

	private Long getUserIdFromRequest(HttpServletRequest request) {
		String authHeader = request.getHeader("Authorization");
		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			return null;
		}

		String token = authHeader.substring(7);
		try {
			return jwtUtil.getUserIdFromToken(token);
		} catch (Exception e) {
			log.error("解析 token 失败", e);
			return null;
		}
	}

	private String truncateMessage(String message, int maxLength) {
		if (message == null || message.isEmpty()) {
			return "新会话";
		}
		// 去除换行符和多余空格
		String cleaned = message.replaceAll("\\s+", " ").trim();
		if (cleaned.length() > maxLength) {
			return cleaned.substring(0, maxLength) + "...";
		}
		return cleaned;
	}
}
