package com.mostar.langchain4jtest.handler;

import java.sql.SQLIntegrityConstraintViolationException;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.mostar.langchain4jtest.entity.Result;
import com.mostar.langchain4jtest.exception.BaseException;

import lombok.extern.slf4j.Slf4j;

import static com.mostar.langchain4jtest.constants.MessageConstant.ALREADY_EXISTS;
import static com.mostar.langchain4jtest.constants.MessageConstant.UNKNOWN_ERROR;

/**
 * 全局异常处理器，处理项目中抛出的业务异常
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

	/**
	 * 捕获业务异常
	 *
	 * @param ex
	 * @return
	 */
	@ExceptionHandler
	public Result<?> exceptionHandler(BaseException ex) {
		log.error("异常信息：{}", ex.getMessage());
		return Result.error(ex.getMessage());
	}

	/**
	 * 处理SQL异常
	 *
	 * @param ex
	 * @return
	 */
	@ExceptionHandler
	public Result<?> exceptionHandler(SQLIntegrityConstraintViolationException ex) {
		String message = ex.getMessage();
		if (message.contains("Duplicate entry")) { // 有重复键值对
			String[] split = message.split(" ");
			String username = split[2];
			String meg = username + ALREADY_EXISTS;
			return Result.error(meg);
		} else {
			return Result.error(UNKNOWN_ERROR);
		}
	}
}
