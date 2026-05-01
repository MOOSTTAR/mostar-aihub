package com.mostar.langchain4jtest;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordEncoderTest {
	public static void main(String[] args) {
		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
		String rawPassword = "12345678";
		String encodedPassword = encoder.encode(rawPassword);
		System.out.println(encodedPassword);
	}
}