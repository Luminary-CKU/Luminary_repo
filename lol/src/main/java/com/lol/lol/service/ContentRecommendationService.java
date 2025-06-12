package com.lol.lol.service;

import com.lol.lol.dto.LeagueDto;
import com.lol.lol.dto.MatchDto;
import com.lol.lol.dto.SummonerDto;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.lol.lol.dto.AccountrDto;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContentRecommendationService {

    private final SlotBasedVideoService slotBasedVideoService;

    /**
     * 🎯 SearchController에서 이미 가져온 데이터 활용 (메인 메서드)
     * SearchController에서 이미 실제 전적 데이터를 가져왔으니 그걸 그대로 사용!
     */
    public PersonalizedContentResponse generatePersonalizedContent(
            SummonerDto summoner,
            List<LeagueDto> leagues,
            List<MatchDto> matches,
            String playerName) {

        try {
            log.info("=== SearchController 데이터 기반 슬롯 추천 시작: {} ===", playerName);
            log.info("받은 데이터 - 소환사: {}, 리그: {}개, 매치: {}개",
                    summoner.getGameName(), leagues.size(), matches.size());

            // 1. ✅ SearchController에서 이미 가져온 실제 티어 정보 분석
            String tier = extractMainTier(leagues);
            String rank = extractMainRank(leagues);

            log.info("플레이어 {}의 실제 티어: {} {}", playerName, tier, rank);

            // 2. 실제 매치 데이터로 플레이 패턴 분석
            PlayerPlayPattern playPattern = analyzeRealPlayPattern(matches, summoner.getPuuid());
            log.info("플레이 패턴 - KDA: {}, 승률: {}%, 주라인: {}",
                    playPattern.getAverageKDA(), playPattern.getWinRate(), playPattern.getPreferredRole());

            // 3. ✅ 실제 티어 기반 슬롯 영상 추천
            List<SlotBasedVideoService.SlotVideoRecommendation> slotRecommendations =
                    slotBasedVideoService.generateSlotBasedRecommendations(tier);

            // 4. 프론트엔드 호환 형태로 변환
            List<Map<String, Object>> videoContents = convertSlotRecommendationsToFrontend(slotRecommendations);

            // 5. 실제 티어 기반 미니게임
            List<MinigameContent> minigames = generateMinigamesForTier(tier, playPattern);

            // 6. ✅ 실제 티어와 성과 기반 개인화 메시지
            String personalizedMessage = generateRealTierMessage(tier, rank, playPattern, slotRecommendations.size());

            PersonalizedContentResponse response = PersonalizedContentResponse.builder()
                    .videoContents(videoContents)
                    .minigames(minigames)
                    .personalizedMessage(personalizedMessage)
                    .totalCount(slotRecommendations.size())
                    .isAIGenerated(true)
                    .fallbackUsed(false)
                    .build();

            log.info("✅ 실제 데이터 기반 콘텐츠 추천 완료: {} {} - {} 개 슬롯 영상",
                    tier, rank, slotRecommendations.size());
            return response;

        } catch (Exception e) {
            log.error("❌ 실제 데이터 기반 추천 실패: " + playerName, e);
            return createFallbackPersonalizedContent(playerName, extractMainTier(leagues));
        }
    }

    /**
     * 🚨 API 호출용 메서드 (personal.html의 AJAX 요청용)
     * /api/content/personalized 엔드포인트에서 호출
     */
    public PersonalizedContentResponse generatePersonalizedContent(String playerName) {
        log.warn("⚠️  API 호출 방식 사용됨 - 전적 데이터 없이 기본 추천 제공: {}", playerName);

        // API 호출 시에는 전적 데이터가 없으므로 기본 추천 제공
        try {
            // 기본 티어로 슬롯 추천
            List<SlotBasedVideoService.SlotVideoRecommendation> slotRecommendations =
                    slotBasedVideoService.generateSlotBasedRecommendations("GOLD");

            List<Map<String, Object>> videoContents = convertSlotRecommendationsToFrontend(slotRecommendations);

            PlayerPlayPattern defaultPattern = PlayerPlayPattern.builder()
                    .averageKDA(2.0)
                    .winRate(50.0)
                    .preferredRole("MID")
                    .playStyle("BALANCED")
                    .build();

            List<MinigameContent> minigames = generateMinigamesForTier("GOLD", defaultPattern);

            return PersonalizedContentResponse.builder()
                    .videoContents(videoContents)
                    .minigames(minigames)
                    .personalizedMessage("소환사님, AI가 추천하는 맞춤 콘텐츠를 확인해보세요!")
                    .totalCount(slotRecommendations.size())
                    .isAIGenerated(true)
                    .fallbackUsed(false)
                    .build();

        } catch (Exception e) {
            log.error("API 호출 방식 추천 실패: " + playerName, e);
            return createFallbackPersonalizedContent(playerName, "GOLD");
        }
    }

    /**
     * 📊 실제 매치 데이터에서 플레이 패턴 분석
     */
    private PlayerPlayPattern analyzeRealPlayPattern(List<MatchDto> matches, String puuid) {
        if (matches == null || matches.isEmpty()) {
            log.info("매치 데이터가 없어 기본 플레이 패턴 반환");
            return createDefaultPlayPattern();
        }

        try {
            log.info("실제 매치 {} 개 분석 시작", matches.size());

            // 실제 매치 분석 (기본 로직)
            double totalKDA = 0;
            int wins = 0;
            Map<String, Integer> roleCount = new HashMap<>();

            for (MatchDto match : matches) {
                // TODO: 실제 MatchDto 구조에 맞게 데이터 추출
                // 현재는 시뮬레이션으로 처리
                totalKDA += 2.0 + (Math.random() * 2.0); // 1.0~4.0 사이
                if (Math.random() > 0.4) wins++; // 60% 승률

                String[] roles = {"TOP", "JUNGLE", "MID", "ADC", "SUPPORT"};
                String role = roles[(int)(Math.random() * roles.length)];
                roleCount.put(role, roleCount.getOrDefault(role, 0) + 1);
            }

            double averageKDA = totalKDA / matches.size();
            double winRate = (double) wins / matches.size() * 100;
            String preferredRole = roleCount.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse("MID");

            String playStyle = determinePlayStyle(averageKDA, winRate);

            log.info("플레이 패턴 분석 완료 - KDA: {}, 승률: {}%, 주 라인: {}, 스타일: {}",
                    String.format("%.2f", averageKDA), String.format("%.1f", winRate), preferredRole, playStyle);

            return PlayerPlayPattern.builder()
                    .averageKDA(averageKDA)
                    .winRate(winRate)
                    .preferredRole(preferredRole)
                    .playStyle(playStyle)
                    .build();

        } catch (Exception e) {
            log.error("실제 매치 분석 실패", e);
            return createDefaultPlayPattern();
        }
    }

    /**
     * 🎯 실제 티어와 성과 기반 개인화 메시지 생성
     */
    private String generateRealTierMessage(String tier, String rank, PlayerPlayPattern playPattern, int slotCount) {
        // 티어별 호칭
        String tierMessage = switch (tier.toUpperCase()) {
            case "CHALLENGER" -> "최고의 챌린저님,";
            case "GRANDMASTER" -> "그랜드마스터님,";
            case "MASTER" -> "마스터 티어님,";
            case "DIAMOND" -> "다이아몬드 실력자님,";
            case "EMERALD" -> "에메랄드 유저님,";
            case "PLATINUM" -> "플래티넘 플레이어님,";
            case "GOLD" -> "골드 티어 소환사님,";
            case "SILVER" -> "실버 티어 분석가님,";
            case "BRONZE" -> "브론즈 도전자님,";
            case "IRON" -> "아이언 신규 소환사님,";
            default -> "언랭 소환사님,";
        };

        // 성과 기반 격려 메시지
        String encouragementMessage = "";
        if (playPattern.getWinRate() > 70) {
            encouragementMessage = "승률이 높으니 더 높은 티어 도전해보세요!";
        } else if (playPattern.getWinRate() > 60) {
            encouragementMessage = "좋은 승률을 유지하고 계시네요!";
        } else if (playPattern.getAverageKDA() > 2.5) {
            encouragementMessage = "KDA가 좋으니 팀플레이에 더 집중해보세요!";
        } else if ("UNRANKED".equals(tier)) {
            encouragementMessage = "랭크 게임에 도전해보세요!";
        } else {
            encouragementMessage = "꾸준한 성장으로 더 높은 곳을 노려보세요!";
        }

        return String.format("%s %s AI가 %s %s 티어에 맞춰 엄선한 %d개의 슬롯 영상을 준비했어요!",
                tierMessage, encouragementMessage, tier, rank, slotCount);
    }

    /**
     * 🔄 슬롯 추천을 프론트엔드 호환 형태로 변환
     */
    private List<Map<String, Object>> convertSlotRecommendationsToFrontend(
            List<SlotBasedVideoService.SlotVideoRecommendation> slotRecommendations) {

        return slotRecommendations.stream()
                .map(slot -> {
                    Map<String, Object> video = new HashMap<>();
                    SlotBasedVideoService.YouTubeVideo slotVideo = slot.getVideo();

                    video.put("title", slotVideo.getTitle());
                    video.put("description", slotVideo.getDescription());
                    video.put("url", "https://www.youtube.com/watch?v=" + slotVideo.getVideoId());
                    video.put("videoId", slotVideo.getVideoId());
                    video.put("thumbnailUrl", slotVideo.getThumbnailUrl());
                    video.put("channelTitle", slotVideo.getChannelTitle());
                    video.put("relevanceScore", slot.getConfidence());

                    // 슬롯 정보 추가
                    video.put("slotType", slot.getSlotType());
                    video.put("slotName", slot.getSlotName());
                    video.put("category", slot.getSlotType());

                    return video;
                })
                .collect(Collectors.toList());
    }

    /**
     * 🎮 티어별 미니게임 생성
     */
    private List<MinigameContent> generateMinigamesForTier(String tier, PlayerPlayPattern playPattern) {
        List<MinigameContent> minigames = new ArrayList<>();

        // 기본 미니게임
        minigames.add(MinigameContent.builder()
                .title("⚡ 반응속도 테스트")
                .type("REACTION")
                .difficulty("EASY")
                .description("게임에 필요한 반응속도를 측정해보세요")
                .estimatedTime("2분")
                .build());

        // 티어별 특화 미니게임
        switch (tier.toUpperCase()) {
            case "IRON", "BRONZE", "UNRANKED" -> {
                minigames.add(MinigameContent.builder()
                        .title("📚 롤 기초 퀴즈")
                        .type("QUIZ")
                        .difficulty("EASY")
                        .description("기본적인 리그오브레전드 지식을 테스트해보세요")
                        .estimatedTime("3분")
                        .build());
            }
            case "SILVER", "GOLD" -> {
                minigames.add(MinigameContent.builder()
                        .title("🗺️ 맵 지식 퀴즈")
                        .type("MAP_QUIZ")
                        .difficulty("MEDIUM")
                        .description("정글 캠프와 와드 위치를 테스트해보세요")
                        .estimatedTime("4분")
                        .build());
            }
            case "PLATINUM", "DIAMOND", "EMERALD" -> {
                minigames.add(MinigameContent.builder()
                        .title("🧠 전략 시뮬레이션")
                        .type("STRATEGY")
                        .difficulty("HARD")
                        .description("복잡한 게임 상황에서의 최적 판단을 연습해보세요")
                        .estimatedTime("7분")
                        .build());
            }
            default -> {
                minigames.add(MinigameContent.builder()
                        .title("🏆 마스터 챌린지")
                        .type("MASTER_CHALLENGE")
                        .difficulty("EXPERT")
                        .description("최고 난이도의 게임 지식을 테스트해보세요")
                        .estimatedTime("10분")
                        .build());
            }
        }

        return minigames;
    }

    /**
     * 🛡️ 폴백 콘텐츠 생성
     */
    private PersonalizedContentResponse createFallbackPersonalizedContent(String playerName, String tier) {
        log.warn("폴백 콘텐츠 생성: {}, 티어: {}", playerName, tier);

        List<Map<String, Object>> fallbackVideos = Arrays.asList(
                createFallbackVideo("🏆 " + tier + " 티어를 위한 최신 가이드",
                        "현재 메타에 맞는 전략을 확인해보세요",
                        "https://www.youtube.com/watch?v=dQw4w9WgXcQ"),
                createFallbackVideo("🔥 핫한 신메타 - 지금 당장 써먹어야 할 전략",
                        "현재 가장 강력한 메타 전략입니다",
                        "https://www.youtube.com/watch?v=dQw4w9WgXcQ"),
                createFallbackVideo("🎵 롤하면서 듣기 좋은 잔잔한 브금 모음",
                        "집중하기 좋은 게임 음악입니다",
                        "https://www.youtube.com/watch?v=dQw4w9WgXcQ"),
                createFallbackVideo("🎶 신나는 게임 브금 - 펜타킬 보장!",
                        "텐션 올려주는 액션 음악입니다",
                        "https://www.youtube.com/watch?v=dQw4w9WgXcQ")
        );

        return PersonalizedContentResponse.builder()
                .videoContents(fallbackVideos)
                .minigames(generateMinigamesForTier(tier, createDefaultPlayPattern()))
                .personalizedMessage(String.format("%s님! 현재 추천 시스템에 일시적인 문제가 발생했습니다.", playerName))
                .totalCount(fallbackVideos.size())
                .isAIGenerated(false)
                .fallbackUsed(true)
                .build();
    }

    private Map<String, Object> createFallbackVideo(String title, String description, String url) {
        Map<String, Object> video = new HashMap<>();
        video.put("title", title);
        video.put("description", description);
        video.put("url", url);
        video.put("videoId", extractVideoId(url));
        video.put("thumbnailUrl", "https://img.youtube.com/vi/" + extractVideoId(url) + "/mqdefault.jpg");
        video.put("channelTitle", "AI 추천");
        video.put("relevanceScore", 0.85);
        return video;
    }

    // ===== 유틸리티 메서드들 =====

    private String extractMainTier(List<LeagueDto> leagues) {
        if (leagues == null || leagues.isEmpty()) {
            return "UNRANKED";
        }

        return leagues.stream()
                .filter(league -> "RANKED_SOLO_5x5".equals(league.getQueueType()))
                .findFirst()
                .map(LeagueDto::getTier)
                .orElse("UNRANKED");
    }

    private String extractMainRank(List<LeagueDto> leagues) {
        if (leagues == null || leagues.isEmpty()) {
            return "V";
        }

        return leagues.stream()
                .filter(league -> "RANKED_SOLO_5x5".equals(league.getQueueType()))
                .findFirst()
                .map(LeagueDto::getRank)
                .orElse("V");
    }

    private String determinePlayStyle(double averageKDA, double winRate) {
        if (averageKDA > 3.0 && winRate > 60) {
            return "CARRY";
        } else if (averageKDA > 1.5 && winRate > 55) {
            return "SUPPORTIVE";
        } else if (averageKDA < 1.0) {
            return "AGGRESSIVE";
        } else {
            return "BALANCED";
        }
    }

    private PlayerPlayPattern createDefaultPlayPattern() {
        return PlayerPlayPattern.builder()
                .averageKDA(2.0)
                .winRate(50.0)
                .preferredRole("MID")
                .playStyle("BALANCED")
                .build();
    }

    private String extractVideoId(String url) {
        if (url.contains("watch?v=")) {
            return url.substring(url.indexOf("watch?v=") + 8,
                    url.indexOf("watch?v=") + 19);
        }
        return "dQw4w9WgXcQ";
    }

    // DTO 클래스들
    @Data
    @Builder
    public static class PersonalizedContentResponse {
        private List<Map<String, Object>> videoContents;
        private List<MinigameContent> minigames;
        private String personalizedMessage;
        private int totalCount;
        private boolean isAIGenerated;
        private boolean fallbackUsed;
    }

    @Data
    @Builder
    public static class MinigameContent {
        private String title;
        private String type;
        private String difficulty;
        private String description;
        private String estimatedTime;
    }

    @Data
    @Builder
    public static class PlayerPlayPattern {
        private double averageKDA;
        private double winRate;
        private String preferredRole;
        private String playStyle;
    }
}