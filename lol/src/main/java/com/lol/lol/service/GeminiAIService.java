package com.lol.lol.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class GeminiAIService {

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    @Value("${gemini.api.url:https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent}")
    private String geminiApiUrl;

    private final RestTemplate restTemplate;

    public GeminiAIService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * 🤖 감정 기반 콘텐츠 카테고리 생성
     */
    public String generateEmotionBasedCategories(String emotionPrompt) {
        try {
            log.info("🤖 Gemini AI 감정 기반 카테고리 생성 요청");

            String enhancedPrompt = """
                다음 롤 플레이어의 감정 상태를 분석하고, 맞춤 YouTube 콘텐츠 카테고리를 JSON 형태로 추천해주세요.
                
                %s
                
                응답 형식:
                {
                  "categories": [
                    {
                      "searchKeywords": "검색할 키워드",
                      "category": "카테고리 타입",
                      "mood": "감정 상태",
                      "priority": 1,
                      "description": "추천 이유"
                    }
                  ]
                }
                
                카테고리 타입: MUSIC, EDUCATIONAL, ENTERTAINMENT, MOTIVATIONAL, TUTORIAL, META
                감정 상태: healing, energetic, focus, epic, calm 등
                우선순위: 1(높음) ~ 5(낮음)
                """.formatted(emotionPrompt);

            String response = callGeminiAPI(enhancedPrompt);
            log.info("✅ Gemini AI 카테고리 생성 완료");

            return response;

        } catch (Exception e) {
            log.error("❌ Gemini AI 카테고리 생성 실패", e);
            throw new RuntimeException("AI 카테고리 생성 실패", e);
        }
    }

    /**
     * 🎯 영상과 감정 매칭도 평가
     */
    public double evaluateEmotionMatch(String videoTitle, String videoDescription,
                                       String emotionType, String targetMood) {
        try {
            log.debug("🎯 영상 감정 매칭도 평가: {}", videoTitle);

            String prompt = """
                다음 YouTube 영상이 롤 플레이어의 현재 감정 상태에 얼마나 적합한지 0.0~1.0 점수로 평가해주세요.
                
                영상 정보:
                - 제목: %s
                - 설명: %s
                
                플레이어 상태:
                - 감정 타입: %s
                - 원하는 분위기: %s
                
                평가 기준:
                1. 롤 관련성 (0.3)
                2. 감정 상태 적합성 (0.4)
                3. 콘텐츠 품질 (0.3)
                
                응답 형식: 숫자만 (예: 0.85)
                """.formatted(videoTitle, videoDescription, emotionType, targetMood);

            String response = callGeminiAPI(prompt);

            // 숫자 추출
            String cleanResponse = response.replaceAll("[^0-9.]", "");
            double score = Double.parseDouble(cleanResponse);

            log.debug("✅ 매칭 점수: {}", score);
            return Math.max(0.0, Math.min(1.0, score)); // 0.0~1.0 범위 보장

        } catch (Exception e) {
            log.warn("❌ 영상 매칭도 평가 실패: {}", videoTitle, e);
            // 기본 점수 반환
            return evaluateBasicMatch(videoTitle, emotionType);
        }
    }

    /**
     * 📊 기본 매칭도 평가 (AI 실패 시 폴백)
     */
    private double evaluateBasicMatch(String videoTitle, String emotionType) {
        String title = videoTitle.toLowerCase();
        double score = 0.5; // 기본 점수

        // 롤 관련성 체크
        if (title.contains("롤") || title.contains("lol") || title.contains("리그") ||
                title.contains("league") || title.contains("legends")) {
            score += 0.2;
        }

        // 감정 타입별 키워드 매칭
        switch (emotionType) {
            case "DEEP_SLUMP", "SLUMP":
                if (title.contains("힐링") || title.contains("차분") || title.contains("극복") ||
                        title.contains("멘탈") || title.contains("음악")) {
                    score += 0.2;
                }
                break;
            case "HOT_STREAK", "WINNING_MOOD":
                if (title.contains("매드무비") || title.contains("하이라이트") || title.contains("신나") ||
                        title.contains("epic") || title.contains("bgm")) {
                    score += 0.2;
                }
                break;
            case "CS_STRUGGLE":
                if (title.contains("cs") || title.contains("라스트힛") || title.contains("연습") ||
                        title.contains("마스터")) {
                    score += 0.3;
                }
                break;
            case "KDA_STRUGGLE":
                if (title.contains("포지셔닝") || title.contains("생존") || title.contains("안전") ||
                        title.contains("한타")) {
                    score += 0.3;
                }
                break;
        }

        return Math.min(score, 1.0);
    }

    /**
     * 🤖 일반적인 텍스트 완성 메서드 (SlotBasedVideoService용)
     */
    public String completion(String prompt) {
        try {
            log.info("🤖 Gemini AI 텍스트 완성 요청");
            return callGeminiAPI(prompt);
        } catch (Exception e) {
            log.error("❌ Gemini AI 텍스트 완성 실패", e);
            return "AI 응답을 생성할 수 없습니다.";
        }
    }

    /**
     * 🌟 기존 메서드들 (호환성 유지)
     */
    public String generateYouTubeRecommendations(String tier, String mainRole, String mainChampion,
                                                 double averageKDA, double winRate) {
        try {
            String prompt = """
                롤 플레이어 정보 기반 YouTube 콘텐츠 추천:
                - 티어: %s
                - 주 포지션: %s  
                - 주 챔피언: %s
                - 평균 KDA: %.2f
                - 승률: %.1f%%
                
                이 플레이어에게 맞는 YouTube 검색 키워드 5개를 JSON으로 추천해주세요.
                """.formatted(tier, mainRole, mainChampion, averageKDA, winRate * 100);

            return callGeminiAPI(prompt);
        } catch (Exception e) {
            log.error("기존 YouTube 추천 실패", e);
            return createFallbackRecommendations();
        }
    }

    public double evaluateVideoQuality(String title, String description, String tier, String targetSkill) {
        try {
            String prompt = """
                YouTube 영상 품질 평가:
                - 제목: %s
                - 설명: %s
                - 대상 티어: %s
                - 목표 스킬: %s
                
                이 영상이 해당 플레이어에게 얼마나 도움이 될지 0.0~1.0 점수로 평가해주세요.
                숫자만 응답하세요.
                """.formatted(title, description, tier, targetSkill);

            String response = callGeminiAPI(prompt);
            String cleanResponse = response.replaceAll("[^0-9.]", "");
            return Double.parseDouble(cleanResponse);
        } catch (Exception e) {
            log.warn("영상 품질 평가 실패", e);
            return 0.7; // 기본 점수
        }
    }

    /**
     * 🔧 Gemini API 호출 핵심 메서드
     */
    private String callGeminiAPI(String prompt) {
        try {
            String url = geminiApiUrl + "?key=" + geminiApiKey;

            // 요청 바디 구성
            Map<String, Object> requestBody = new HashMap<>();
            Map<String, Object> content = new HashMap<>();
            Map<String, Object> part = new HashMap<>();

            part.put("text", prompt);
            content.put("parts", new Object[]{part});
            requestBody.put("contents", new Object[]{content});

            // 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            // API 호출
            ResponseEntity<Map> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, Map.class
            );

            // 응답 파싱
            Map<String, Object> responseBody = response.getBody();
            if (responseBody != null && responseBody.containsKey("candidates")) {
                Object[] candidates = (Object[]) responseBody.get("candidates");
                if (candidates.length > 0) {
                    Map<String, Object> candidate = (Map<String, Object>) candidates[0];
                    Map<String, Object> content_result = (Map<String, Object>) candidate.get("content");
                    Object[] parts = (Object[]) content_result.get("parts");
                    if (parts.length > 0) {
                        Map<String, Object> part_result = (Map<String, Object>) parts[0];
                        return (String) part_result.get("text");
                    }
                }
            }

            throw new RuntimeException("Gemini API 응답 파싱 실패");

        } catch (Exception e) {
            log.error("Gemini API 호출 실패", e);
            throw new RuntimeException("Gemini API 호출 실패", e);
        }
    }

    /**
     * 🛡️ 폴백 추천 생성
     */
    private String createFallbackRecommendations() {
        return """
            {
              "queries": [
                {
                  "searchTerm": "롤 실력 향상 팁",
                  "category": "EDUCATIONAL",
                  "priority": "HIGH",
                  "targetSkill": "general",
                  "maxResults": 3,
                  "description": "기본 실력 향상 콘텐츠"
                },
                {
                  "searchTerm": "리그오브레전드 가이드",
                  "category": "TUTORIAL", 
                  "priority": "MEDIUM",
                  "targetSkill": "basic",
                  "maxResults": 2,
                  "description": "기초 가이드 콘텐츠"
                }
              ]
            }
            """;
    }
}