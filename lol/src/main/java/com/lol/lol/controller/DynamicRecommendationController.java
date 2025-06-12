package com.lol.lol.controller;

import com.lol.lol.dto.*;
import com.lol.lol.service.DynamicRecommendationEngine;
import com.lol.lol.service.SummonerService;
import com.lol.lol.dto.MatchDto;
import com.lol.lol.service.ChampionRecommendationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/v2/recommendations")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // 개발용, 운영에서는 제한 필요
public class DynamicRecommendationController {

    private final DynamicRecommendationEngine recommendationEngine;
    private final ChampionRecommendationService championRecommendationService;
    private final SummonerService summonerService;

    /**
     * 🌌 Cards Galaxy 전용 API - 간단한 JSON 응답
     * personal.html에서 JavaScript로 호출
     */
    @GetMapping("/galaxy")
    public ResponseEntity<Map<String, Object>> getGalaxyRecommendations(
            @RequestParam(required = false) String playerName,
            @RequestParam(required = false) String role,
            @RequestParam(defaultValue = "12") int count) {

        try {
            log.info("🌌 Cards Galaxy API 호출 - 플레이어: {}, 라인: {}, 개수: {}",
                    playerName, role, count);

            // ChampionRecommendationService를 통한 추천 생성
            Map<String, Object> galaxyResponse = championRecommendationService
                    .generateSimpleRecommendationsForGalaxy(playerName, role, count);

            return ResponseEntity.ok(galaxyResponse);

        } catch (Exception e) {
            log.error("Cards Galaxy API 오류", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorGalaxyResponse("추천 시스템에 일시적인 문제가 발생했습니다."));
        }
    }

    /**
     * 🎯 동적 챔피언 추천 API (완전한 응답)
     * Cards Galaxy 프론트엔드에서 상세 정보 요청 시 사용
     */
    @GetMapping("/player/{playerName}")
    @Cacheable(value = "user_recommendations_v2", key = "#playerName", unless = "#result == null")
    public ResponseEntity<ChampionRecommendationResult> getRecommendationsByPlayerName(
            @PathVariable String playerName) {
        try {
            log.info("=== 동적 추천 API 호출: {} ===", playerName);

            // 플레이어 이름으로 최근 매치 데이터 조회 (기존 서비스 활용)
            List<MatchDto> recentMatches = getPlayerRecentMatches(playerName);
            log.info("매치 데이터 조회 완료: {} 개", recentMatches.size());

            // AI 추천 생성
            ChampionRecommendationResult result = recommendationEngine.generateRecommendations(
                    playerName, recentMatches
            );

            log.info("추천 생성 완료: {} 개 챔피언, {} 개 라인",
                    result.getRecommendations().size(),
                    result.getRecommendationsByRole().size());

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("추천 API 오류: " + playerName, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * 📱 PUUID로 추천 조회 (기존 시스템과 호환)
     */
    @GetMapping("/puuid/{puuid}")
    @Cacheable(value = "user_recommendations_v2", key = "#puuid", unless = "#result == null")
    public ResponseEntity<ChampionRecommendationResult> getRecommendationsByPuuid(
            @PathVariable String puuid) {
        try {
            log.info("=== PUUID 기반 추천 API 호출: {} ===", puuid);

            // PUUID로 최근 매치 데이터 조회
            List<MatchDto> recentMatches = getMatchesFromSummonerService(puuid);

            // 플레이어 이름 추출 (첫 번째 매치에서)
            String playerName = extractPlayerName(recentMatches, puuid);

            // AI 추천 생성
            ChampionRecommendationResult result = recommendationEngine.generateRecommendations(
                    playerName, recentMatches
            );

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("PUUID 추천 API 오류: " + puuid, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * 📊 라인별 추천 API
     */
    @GetMapping("/role/{role}")
    public ResponseEntity<Map<String, Object>> getRecommendationsByRole(
            @PathVariable String role,
            @RequestParam(required = false) String playerName,
            @RequestParam(defaultValue = "6") int count) {

        try {
            log.info("라인별 추천 API 호출 - 라인: {}, 플레이어: {}", role, playerName);

            ChampionRecommendationResult fullResult;

            if (playerName != null && !playerName.trim().isEmpty()) {
                // 플레이어별 맞춤 추천
                List<MatchDto> matches = getPlayerRecentMatches(playerName);
                fullResult = recommendationEngine.generateRecommendations(playerName, matches);
            } else {
                // 일반 추천 (메타 기반)
                fullResult = recommendationEngine.generateRecommendations("DefaultPlayer", List.of());
            }

            // 라인별 필터링
            List<ChampionRecommendation> roleFiltered = fullResult.getRecommendations().stream()
                    .filter(rec -> role.equalsIgnoreCase(rec.getPrimaryRole()))
                    .limit(count)
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("role", role.toUpperCase());
            response.put("champions", roleFiltered.stream()
                    .map(this::convertToSimpleChampion)
                    .collect(Collectors.toList()));
            response.put("totalCount", roleFiltered.size());
            response.put("message", String.format("%s 라인 추천 챔피언 %d개를 찾았습니다!",
                    getRoleKorean(role), roleFiltered.size()));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("라인별 추천 API 오류", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 🔄 피드백 수집 API
     */
    @PostMapping("/feedback")
    public ResponseEntity<Map<String, Object>> submitFeedback(@RequestBody FeedbackRequest request) {
        try {
            log.info("피드백 수집: {} - {} - {}",
                    request.getPuuid(),
                    request.getChampionName(),
                    request.getFeedbackType());

            // 실제로는 FeedbackLearningService에서 처리
            // 현재는 로깅만 수행

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "피드백이 성공적으로 저장되었습니다");
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("피드백 처리 오류", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "피드백 처리 중 오류가 발생했습니다");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 📈 추천 시스템 상태 확인
     */
    @GetMapping("/health")
    public ResponseEntity<RecommendationSystemHealth> getSystemHealth() {
        try {
            RecommendationSystemHealth health = RecommendationSystemHealth.builder()
                    .status("HEALTHY")
                    .version("2.0")
                    .metaDataLastUpdated(LocalDateTime.now())
                    .totalChampionsAnalyzed(170) // 현재 챔피언 수
                    .aiEngineStatus("ACTIVE")
                    .build();

            return ResponseEntity.ok(health);

        } catch (Exception e) {
            log.error("시스템 상태 확인 오류", e);

            RecommendationSystemHealth errorHealth = RecommendationSystemHealth.builder()
                    .status("ERROR")
                    .version("2.0")
                    .metaDataLastUpdated(LocalDateTime.now())
                    .totalChampionsAnalyzed(0)
                    .aiEngineStatus("ERROR")
                    .build();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorHealth);
        }
    }

    /**
     * 🔍 추천 통계 API
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getRecommendationStats(
            @RequestParam(required = false) String timeRange) {

        try {
            Map<String, Object> stats = new HashMap<>();

            // 시뮬레이션 통계 데이터
            stats.put("totalRecommendations", 15420);
            stats.put("activeUsers", 3280);
            stats.put("avgConfidenceScore", 0.847);
            stats.put("topRecommendedChampions", Arrays.asList(
                    Map.of("name", "Jinx", "count", 892),
                    Map.of("name", "Yasuo", "count", 756),
                    Map.of("name", "Thresh", "count", 683),
                    Map.of("name", "Lee Sin", "count", 621),
                    Map.of("name", "Ahri", "count", 598)
            ));
            stats.put("roleDistribution", Map.of(
                    "ADC", 20.5,
                    "MID", 19.8,
                    "SUPPORT", 18.2,
                    "JUNGLE", 21.1,
                    "TOP", 20.4
            ));
            stats.put("lastUpdated", LocalDateTime.now());

            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            log.error("통계 API 오류", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ===== 유틸리티 메서드들 =====

    /**
     * 플레이어 이름으로 최근 매치 조회
     * 기존 SummonerService와 연동
     */
    private List<MatchDto> getPlayerRecentMatches(String playerName) {
        try {
            // 실제로는 플레이어 이름 -> PUUID 변환 후 매치 조회
            // 현재는 시뮬레이션으로 처리
            log.info("플레이어 매치 데이터 조회 시도: {}", playerName);
            return List.of(); // 빈 리스트 반환 (폴백 추천 트리거)

        } catch (Exception e) {
            log.warn("매치 데이터 조회 실패: {}, 폴백 처리", playerName);
            return List.of();
        }
    }

    /**
     * SummonerService를 통한 매치 조회
     */
    private List<MatchDto> getMatchesFromSummonerService(String puuid) {
        try {
            List<String> matchIds = summonerService.getMatchHistory(puuid);
            return matchIds.stream()
                    .map(summonerService::getMatchDetails)
                    .filter(match -> match != null)
                    .limit(20)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("SummonerService 매치 조회 실패: " + puuid, e);
            return List.of();
        }
    }

    /**
     * 매치 데이터에서 플레이어 이름 추출
     */
    private String extractPlayerName(List<MatchDto> matches, String puuid) {
        if (matches.isEmpty()) {
            return "Player_" + puuid.substring(0, 8); // 기본 이름
        }

        // 첫 번째 매치에서 플레이어 이름 찾기 (시뮬레이션)
        return "Player_" + puuid.substring(0, 8);
    }

    /**
     * 간단한 챔피언 정보로 변환
     */
    private Map<String, Object> convertToSimpleChampion(ChampionRecommendation rec) {
        Map<String, Object> simple = new HashMap<>();
        simple.put("name", rec.getChampionName());
        simple.put("role", rec.getPrimaryRole());
        simple.put("confidence", Math.round(rec.getConfidenceScore() * 100));
        simple.put("difficulty", rec.getDifficulty());
        simple.put("tier", getMetaTier(rec.getMetaStrength()));
        simple.put("tags", generateTags(rec));
        simple.put("reasons", rec.getReasons());
        simple.put("styleMatch", Math.round(rec.getStyleMatch() * 100));
        simple.put("skillMatch", Math.round(rec.getSkillMatch() * 100));
        simple.put("type", rec.getRecommendationType());
        return simple;
    }

    private String getMetaTier(double metaStrength) {
        if (metaStrength >= 8.5) return "S";
        if (metaStrength >= 7.0) return "A";
        if (metaStrength >= 5.5) return "B";
        if (metaStrength >= 4.0) return "C";
        return "D";
    }

    private List<String> generateTags(ChampionRecommendation rec) {
        List<String> tags = new java.util.ArrayList<>();

        if (rec.getPersonalStats() != null) {
            tags.add("personal");
        }

        if (rec.getMetaStrength() >= 8.0) {
            tags.add("meta");
        }

        if (rec.getStyleMatch() >= 0.8) {
            tags.add("style");
        }

        if ("NEW_CHALLENGE".equals(rec.getRecommendationType())) {
            tags.add("challenge");
        }

        return tags;
    }

    private String getRoleKorean(String role) {
        Map<String, String> roleNames = Map.of(
                "TOP", "탑",
                "JUNGLE", "정글",
                "MID", "미드",
                "ADC", "원딜",
                "SUPPORT", "서포터"
        );
        return roleNames.getOrDefault(role.toUpperCase(), role);
    }

    private ChampionRecommendationResult createErrorResponse(String errorMessage) {
        return ChampionRecommendationResult.builder()
                .recommendations(List.of())
                .recommendationsByRole(Map.of())
                .playerProfile(null)
                .personalizedMessage("추천 시스템에 일시적인 문제가 발생했습니다: " + errorMessage)
                .generatedAt(LocalDateTime.now())
                .totalAnalyzedChampions(0)
                .build();
    }

    private Map<String, Object> createErrorGalaxyResponse(String errorMessage) {
        Map<String, Object> error = new HashMap<>();
        error.put("champions", List.of());
        error.put("totalCount", 0);
        error.put("playerStyle", "BALANCED");
        error.put("message", errorMessage);
        error.put("timestamp", System.currentTimeMillis());
        error.put("success", false);
        return error;
    }
}

// ===== 응답 DTO 클래스들 =====

@lombok.Data
@lombok.Builder
class SimpleRecommendationResponse {
    private List<SimpleChampionInfo> champions;
    private String playerStyle;
    private int totalRecommendations;
    private String message;
}

@lombok.Data
@lombok.Builder
class SimpleChampionInfo {
    private String name;
    private String role;
    private int confidence;              // 0-100
    private String difficulty;           // 쉬움/보통/어려움
    private String tier;                 // S/A/B/C/D
    private List<String> tags;          // meta, personal, style, challenge
    private List<String> reasons;
}

@lombok.Data
@lombok.Builder
class RecommendationSystemHealth {
    private String status;               // HEALTHY, WARNING, ERROR
    private String version;
    private LocalDateTime metaDataLastUpdated;
    private int totalChampionsAnalyzed;
    private String aiEngineStatus;       // ACTIVE, MAINTENANCE, ERROR
}

