package com.lol.lol.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 🚨 AI API 관련 예외 처리
     */
    @ExceptionHandler({RestClientException.class, ResourceAccessException.class})
    public ResponseEntity<Map<String, Object>> handleApiException(Exception e) {
        log.error("API 호출 실패", e);

        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", "외부 API 연결에 실패했습니다");
        response.put("fallback", true);

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }

    /**
     * 🔑 API 키 관련 예외 처리
     */
    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<Map<String, Object>> handleSecurityException(SecurityException e) {
        log.error("보안 관련 오류", e);

        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", "API 키 설정을 확인해주세요");

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    /**
     * 🎮 개인 페이지 관련 예외 처리
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ModelAndView handleIllegalArgumentException(IllegalArgumentException e) {
        log.error("잘못된 파라미터", e);

        ModelAndView mav = new ModelAndView("intro");
        mav.addObject("error", "올바르지 않은 사용자 정보입니다. 다시 검색해주세요.");

        return mav;
    }

    /**
     * 🌐 일반적인 예외 처리
     */
    @ExceptionHandler(Exception.class)
    public ModelAndView handleGeneralException(Exception e) {
        log.error("예상치 못한 오류 발생", e);

        ModelAndView mav = new ModelAndView("intro");
        mav.addObject("error", "서비스 이용 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");

        return mav;
    }
}