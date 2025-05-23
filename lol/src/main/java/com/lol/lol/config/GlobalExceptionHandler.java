package com.lol.lol.config;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

/**
 * 전역 예외 처리 클래스
 * 애플리케이션 전체에서 발생하는 예외를 통합 관리
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * HTTP 클라이언트 오류 (4xx) 처리
     * - 404: 사용자 없음
     * - 403: API 키 문제
     * - 400: 잘못된 요청
     */
    @ExceptionHandler(HttpClientErrorException.class)
    public String handleHttpClientError(HttpClientErrorException e, Model model) {
        System.err.println("HTTP 클라이언트 오류: " + e.getStatusCode() + " - " + e.getMessage());

        String errorMessage;
        switch (e.getStatusCode().value()) {
            case 404:
                errorMessage = "사용자를 찾을 수 없습니다. 이름과 태그를 다시 확인해주세요.";
                break;
            case 403:
                errorMessage = "서비스에 일시적인 문제가 있습니다. 잠시 후 다시 시도해주세요.";
                break;
            case 400:
                errorMessage = "잘못된 요청입니다. 입력 형식을 확인해주세요.";
                break;
            case 429:
                errorMessage = "요청이 너무 많습니다. 잠시 후 다시 시도해주세요.";
                break;
            default:
                errorMessage = "요청 처리 중 오류가 발생했습니다.";
        }

        model.addAttribute("error", errorMessage);
        return "intro";
    }

    /**
     * HTTP 서버 오류 (5xx) 처리
     * - 500: 내부 서버 오류
     * - 502, 503: 서비스 이용 불가
     */
    @ExceptionHandler(HttpServerErrorException.class)
    public String handleHttpServerError(HttpServerErrorException e, Model model) {
        System.err.println("HTTP 서버 오류: " + e.getStatusCode() + " - " + e.getMessage());

        String errorMessage;
        switch (e.getStatusCode().value()) {
            case 500:
                errorMessage = "서버에 일시적인 문제가 발생했습니다. 잠시 후 다시 시도해주세요.";
                break;
            case 502:
            case 503:
                errorMessage = "서비스가 일시적으로 이용 불가능합니다. 잠시 후 다시 시도해주세요.";
                break;
            default:
                errorMessage = "서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.";
        }

        model.addAttribute("error", errorMessage);
        return "intro";
    }

    /**
     * 네트워크 연결 오류 처리
     * - 타임아웃
     * - 연결 실패
     */
    @ExceptionHandler(ResourceAccessException.class)
    public String handleResourceAccessException(ResourceAccessException e, Model model) {
        System.err.println("네트워크 연결 오류: " + e.getMessage());

        String errorMessage = "네트워크 연결에 문제가 있습니다. 인터넷 연결을 확인하고 다시 시도해주세요.";
        model.addAttribute("error", errorMessage);
        return "intro";
    }

    /**
     * 입력값 검증 오류 처리
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public String handleIllegalArgumentException(IllegalArgumentException e, Model model) {
        System.err.println("입력값 검증 오류: " + e.getMessage());

        model.addAttribute("error", e.getMessage());
        return "intro";
    }

    /**
     * 일반적인 런타임 예외 처리
     */
    @ExceptionHandler(RuntimeException.class)
    public String handleRuntimeException(RuntimeException e, Model model) {
        System.err.println("RuntimeException 발생: " + e.getMessage());
        e.printStackTrace();

        // 사용자 친화적인 메시지로 변환
        String userMessage = e.getMessage();
        if (userMessage == null || userMessage.contains("Exception") || userMessage.contains("Error")) {
            userMessage = "요청 처리 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.";
        }

        model.addAttribute("error", userMessage);
        return "intro";
    }

    /**
     * 모든 예외의 최종 처리
     */
    @ExceptionHandler(Exception.class)
    public String handleGeneralException(Exception e, Model model) {
        System.err.println("예상치 못한 오류 발생: " + e.getClass().getSimpleName() + " - " + e.getMessage());
        e.printStackTrace();

        model.addAttribute("error", "서비스 이용 중 예상치 못한 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
        return "intro";
    }
}