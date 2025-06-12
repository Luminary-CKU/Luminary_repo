package com.lol.lol.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lol.lol.dto.LeagueDto;
import com.lol.lol.dto.MatchDto;
import com.lol.lol.dto.SummonerDto;
import com.lol.lol.service.EmotionBasedRecommendationService;
import com.lol.lol.service.EmotionBasedRecommendationService.SmartVideoRecommendation;
import com.lol.lol.service.SummonerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 🎯 감정 기반 개인화 콘텐츠 API 컨트롤러
 * AI 영상 추천 시스템을 위한 REST API 제공
 */
@Slf4j
@RestController
@RequestMapping("/api/content")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ContentRecommendationController {

    private final EmotionBasedRecommendationService emotionBasedService;
    private final SummonerService summonerService;

    /**
     * 🤖 AI 기반 감정 분석 개인화 콘텐츠 추천 API
     * personal.html에서 JavaScript로 호출하는 메인 엔드포인트
     */
    @GetMapping("/personalized")
    public ResponseEntity<Map<String, Object>> getPersonalizedContent(
            @RequestParam String playerName) {
        try {
            log.info("🤖 감정 기반 개인화 콘텐츠 요청: {}", playerName);

            // 테스트용 간단한 설정
            SummonerDto summoner = new SummonerDto();
            List<MatchDto> matches = new ArrayList<>();

            // 감정 기반 AI 영상 추천 생성
            List<SmartVideoRecommendation> videoRecommendations = emotionBasedService
                    .generateEmotionBasedRecommendations(summoner, matches, playerName);

            // 감정 상태 메시지 생성
            String personalizedMessage = generatePersonalizedMessage(matches, playerName);

            // 프론트엔드가 기대하는 형태로 응답 구성
            Map<String, Object> response = new HashMap<>();
            response.put("videoContents", convertToVideoContents(videoRecommendations));
            response.put("minigames", new ArrayList<>());
            response.put("personalizedMessage", personalizedMessage);
            response.put("totalCount", videoRecommendations.size());
            response.put("isAIGenerated", true);
            response.put("emotionAnalysis", createEmotionAnalysis(matches, playerName));

            log.info("✅ 감정 기반 추천 완료: {} ({} 개 영상)", playerName, videoRecommendations.size());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ 개인화 콘텐츠 생성 실패: " + playerName, e);
            return createFallbackResponse(playerName);
        }
    }

    /**
     * 💬 개인화 메시지 생성
     */
    private String generatePersonalizedMessage(List<MatchDto> matches, String playerName) {
        if (matches.isEmpty()) {
            return playerName + "님을 위한 맞춤 콘텐츠를 준비했어요! 🎮";
        }

        // 실제 경기 데이터가 있을 때의 로직은 나중에 구현
        return String.format("🎯 %s님의 플레이 스타일을 분석해서 맞춤 콘텐츠를 추천드려요!", playerName);
    }

    /**
     * 📊 감정 분석 결과 생성
     */
    private Map<String, Object> createEmotionAnalysis(List<MatchDto> matches, String playerName) {
        Map<String, Object> analysis = new HashMap<>();

        if (matches.isEmpty()) {
            analysis.put("emotionType", "STABLE");
            analysis.put("description", "안정 상태 - 기본 추천");
            analysis.put("winRate", 0.5);
            analysis.put("averageKDA", 1.5);
            analysis.put("currentStreak", 0);
            return analysis;
        }

        // 실제 경기 데이터 분석 로직은 나중에 구현
        analysis.put("emotionType", "STABLE");
        analysis.put("description", "안정 상태");
        analysis.put("winRate", 0.5);
        analysis.put("averageKDA", 1.5);
        analysis.put("currentStreak", 0);

        return analysis;
    }

    /**
     * 🎬 SmartVideoRecommendation을 프론트엔드 형태로 변환
     */
    private List<Map<String, Object>> convertToVideoContents(List<SmartVideoRecommendation> recommendations) {
        List<Map<String, Object>> videoContents = new ArrayList<>();

        for (SmartVideoRecommendation rec : recommendations) {
            Map<String, Object> video = new HashMap<>();
            video.put("title", rec.getTitle());
            video.put("description", rec.getDescription());
            video.put("url", rec.getVideoUrl());
            video.put("thumbnailUrl", rec.getThumbnailUrl());
            video.put("channelTitle", rec.getChannelTitle());
            video.put("category", rec.getCategory());
            video.put("relevanceScore", rec.getRelevanceScore());
            video.put("personalizedReason", rec.getPersonalizedReason());
            video.put("emotionMatch", rec.getEmotionMatch());
            videoContents.add(video);
        }

        return videoContents;
    }

    /**
     * ❌ 에러 응답 생성
     */
    private ResponseEntity<Map<String, Object>> createErrorResponse(String message) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", true);
        errorResponse.put("message", message);
        errorResponse.put("videoContents", new ArrayList<>());
        errorResponse.put("minigames", new ArrayList<>());
        errorResponse.put("personalizedMessage", "잠시 후 다시 시도해주세요.");
        errorResponse.put("totalCount", 0);
        errorResponse.put("isAIGenerated", false);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * 🛡️ 폴백 응답 생성 (서비스 실패 시)
     */
    private ResponseEntity<Map<String, Object>> createFallbackResponse(String playerName) {
        List<Map<String, Object>> fallbackVideos = new ArrayList<>();

        // 기본 폴백 영상들
        Map<String, Object> video1 = new HashMap<>();
        video1.put("title", "롤 기초 가이드 - 초보자를 위한 완벽 정리");
        video1.put("description", "리그오브레전드를 처음 시작하는 분들을 위한 핵심 가이드입니다.");
        video1.put("url", "https://youtube.com/watch?v=guide1");
        video1.put("thumbnailUrl", "https://via.placeholder.com/320x180?text=LoL+Guide");
        video1.put("channelTitle", "LoL Guide");
        video1.put("category", "EDUCATIONAL");
        video1.put("relevanceScore", 0.8);
        video1.put("personalizedReason", "기본 추천 콘텐츠입니다");
        fallbackVideos.add(video1);

        Map<String, Object> video2 = new HashMap<>();
        video2.put("title", "랭크 승급을 위한 필수 팁 모음");
        video2.put("description", "상위 티어로 올라가기 위한 실전 노하우를 공개합니다.");
        video2.put("url", "https://youtube.com/watch?v=tips1");
        video2.put("thumbnailUrl", "https://via.placeholder.com/320x180?text=Rank+Tips");
        video2.put("channelTitle", "Pro Gamer");
        video2.put("category", "EDUCATIONAL");
        video2.put("relevanceScore", 0.75);
        video2.put("personalizedReason", "랭크 게임 향상을 위한 콘텐츠");
        fallbackVideos.add(video2);

        Map<String, Object> response = new HashMap<>();
        response.put("videoContents", fallbackVideos);
        response.put("minigames", new ArrayList<>());
        response.put("personalizedMessage", playerName + "님을 위한 기본 추천 콘텐츠를 준비했어요!");
        response.put("totalCount", fallbackVideos.size());
        response.put("isAIGenerated", false);
        response.put("isFallback", true);

        return ResponseEntity.ok(response);
    }

    /**
     * 🧪 디버깅용 엔드포인트
     */
    @GetMapping("/debug")
    public ResponseEntity<Map<String, Object>> debugRecommendation(
            @RequestParam String playerName) {
        try {
            Map<String, Object> debugInfo = new HashMap<>();
            debugInfo.put("playerName", playerName);
            debugInfo.put("serviceStatus", "EmotionBasedRecommendationService Available");
            debugInfo.put("emotionAnalysis", createEmotionAnalysis(new ArrayList<>(), playerName));
            debugInfo.put("personalizedMessage", generatePersonalizedMessage(new ArrayList<>(), playerName));
            debugInfo.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(debugInfo);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            error.put("playerName", playerName);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}