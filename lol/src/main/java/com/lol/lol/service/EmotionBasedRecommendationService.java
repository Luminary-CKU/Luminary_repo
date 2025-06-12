package com.lol.lol.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lol.lol.dto.LeagueDto;
import com.lol.lol.dto.MatchDto;
import com.lol.lol.dto.SummonerDto;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmotionBasedRecommendationService {

    private final GeminiAIService geminiAIService;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${youtube.api.key}")
    private String youtubeApiKey;

    /**
     * 🎯 감정 기반 AI 영상 추천 메인 메서드
     */
    public List<SmartVideoRecommendation> generateEmotionBasedRecommendations(
            SummonerDto summoner, List<MatchDto> matches, String playerName) {

        try {
            log.info("=== 감정 기반 AI 추천 시작: {} ===", playerName);

            // 1. 플레이어 감정 상태 분석
            PlayerEmotionState emotionState = analyzePlayerEmotion(matches, playerName);
            log.info("플레이어 감정 상태: {}", emotionState.getEmotionType());

            // 2. 감정에 맞는 콘텐츠 카테고리 생성
            List<ContentCategory> categories = generateEmotionBasedCategories(emotionState);

            // 3. 각 카테고리별 YouTube 검색 & 필터링
            List<SmartVideoRecommendation> allRecommendations = new ArrayList<>();

            for (ContentCategory category : categories) {
                List<SmartVideoRecommendation> categoryVideos = searchAndFilterByCategory(category, emotionState);
                allRecommendations.addAll(categoryVideos);
            }

            // 4. 최종 정렬
            return finalizeRecommendations(allRecommendations, emotionState);

        } catch (Exception e) {
            log.error("감정 기반 추천 실패: " + playerName, e);
            return createEmotionFallbackRecommendations(playerName);
        }
    }

    /**
     * 🧠 플레이어 감정 상태 분석
     */
    private PlayerEmotionState analyzePlayerEmotion(List<MatchDto> matches, String playerName) {
        List<MatchDto> recentMatches = matches.stream().limit(10).collect(Collectors.toList());

        // 경기 데이터가 없는 경우 기본값 설정
        if (recentMatches.isEmpty()) {
            log.info("경기 데이터가 없어 기본 상태로 설정: {}", playerName);
            return PlayerEmotionState.builder()
                    .playerName(playerName)
                    .emotionType(EmotionType.STABLE)
                    .winRate(0.5)
                    .averageKDA(1.5)
                    .averageCS(150.0)
                    .currentStreak(0)
                    .streakLength(0)
                    .isWinStreak(false)
                    .recentPerformance(0.0)
                    .intensityLevel(1)
                    .build();
        }

        // 경기 분석
        int wins = 0;
        double totalKDA = 0;
        int validMatches = 0;

        for (MatchDto match : recentMatches) {
            MatchDto.MatchAnalysis analysis = match.analyzeMatch(playerName);
            if (analysis.isFound()) {
                if (analysis.isWin()) wins++;

                double kills = analysis.getKills();
                double deaths = Math.max(analysis.getDeaths(), 1);
                double assists = analysis.getAssists();
                totalKDA += (kills + assists) / deaths;
                validMatches++;
            }
        }

        double winRate = validMatches > 0 ? (double) wins / validMatches : 0.5;
        double avgKDA = validMatches > 0 ? totalKDA / validMatches : 1.0;
        double avgCS = 150.0; // 기본값

        // 연속 승/패 계산
        int currentStreak = calculateStreak(recentMatches, playerName);
        boolean isWinStreak = currentStreak > 0;
        int streakLength = Math.abs(currentStreak);

        // 최근 성과 트렌드
        double recentPerformance = avgKDA > 1.5 ? 0.2 : (avgKDA < 1.0 ? -0.2 : 0.0);

        // 감정 상태 결정
        EmotionType emotionType = determineEmotionType(winRate, avgKDA, avgCS, streakLength, isWinStreak, recentPerformance);

        return PlayerEmotionState.builder()
                .playerName(playerName)
                .emotionType(emotionType)
                .winRate(winRate)
                .averageKDA(avgKDA)
                .averageCS(avgCS)
                .currentStreak(currentStreak)
                .streakLength(streakLength)
                .isWinStreak(isWinStreak)
                .recentPerformance(recentPerformance)
                .intensityLevel(calculateIntensityLevel(streakLength, recentPerformance))
                .build();
    }

    /**
     * 🎭 감정 타입 결정
     */
    private EmotionType determineEmotionType(double winRate, double avgKDA, double avgCS,
                                             int streakLength, boolean isWinStreak, double recentPerformance) {

        // 극심한 슬럼프
        if (!isWinStreak && streakLength >= 4 && avgKDA < 1.2 && recentPerformance < -0.2) {
            return EmotionType.DEEP_SLUMP;
        }

        // 일반 슬럼프
        if ((!isWinStreak && streakLength >= 3) || (winRate < 0.4)) {
            return EmotionType.SLUMP;
        }

        // 폭주 상태
        if (isWinStreak && streakLength >= 4 && avgKDA > 2.5 && winRate > 0.7) {
            return EmotionType.HOT_STREAK;
        }

        // 승승장구
        if ((isWinStreak && streakLength >= 3) || (winRate > 0.6)) {
            return EmotionType.WINNING_MOOD;
        }

        // CS 부족
        if (avgCS < 130 && avgKDA > 1.5) {
            return EmotionType.CS_STRUGGLE;
        }

        // KDA 문제
        if (avgKDA < 1.3 && avgCS > 150) {
            return EmotionType.KDA_STRUGGLE;
        }

        return EmotionType.STABLE;
    }

    /**
     * 🎨 감정 기반 카테고리 생성
     */
    private List<ContentCategory> generateEmotionBasedCategories(PlayerEmotionState emotionState) {
        try {
            String aiPrompt = buildEmotionPrompt(emotionState);
            String aiResponse = geminiAIService.generateEmotionBasedCategories(aiPrompt);
            return parseAICategories(aiResponse, emotionState);
        } catch (Exception e) {
            log.warn("AI 카테고리 생성 실패, 기본 로직 사용", e);
            return createDefaultCategories(emotionState);
        }
    }

    /**
     * 📝 AI 프롬프트 생성
     */
    private String buildEmotionPrompt(PlayerEmotionState state) {
        return String.format("""
            롤 플레이어 감정 분석:
            - 감정 상태: %s
            - 승률: %.1f%%
            - 평균 KDA: %.2f
            - 연속 기록: %s %d경기
            
            이 플레이어에게 맞는 YouTube 콘텐츠 카테고리 5개를 JSON으로 추천해주세요.
            """,
                state.getEmotionType(),
                state.getWinRate() * 100,
                state.getAverageKDA(),
                state.isWinStreak() ? "승리" : "패배",
                state.getStreakLength()
        );
    }

    /**
     * 🤖 AI 응답 파싱
     */
    private List<ContentCategory> parseAICategories(String aiResponse, PlayerEmotionState state) {
        try {
            JsonNode jsonResponse = objectMapper.readTree(aiResponse);
            List<ContentCategory> categories = new ArrayList<>();

            JsonNode categoriesNode = jsonResponse.get("categories");
            if (categoriesNode != null && categoriesNode.isArray()) {
                for (JsonNode categoryNode : categoriesNode) {
                    categories.add(ContentCategory.builder()
                            .searchKeywords(categoryNode.get("searchKeywords").asText())
                            .category(categoryNode.get("category").asText())
                            .mood(categoryNode.get("mood").asText())
                            .priority(categoryNode.get("priority").asInt())
                            .description(categoryNode.get("description").asText())
                            .build());
                }
            }

            return categories.isEmpty() ? createDefaultCategories(state) : categories;

        } catch (Exception e) {
            log.warn("AI 응답 파싱 실패", e);
            return createDefaultCategories(state);
        }
    }

    /**
     * 🎯 기본 카테고리 생성
     */
    private List<ContentCategory> createDefaultCategories(PlayerEmotionState state) {
        List<ContentCategory> categories = new ArrayList<>();

        switch (state.getEmotionType()) {
            case DEEP_SLUMP:
                categories.addAll(Arrays.asList(
                        ContentCategory.of("롤 슬럼프 극복", "MOTIVATIONAL", "healing", 1),
                        ContentCategory.of("차분한 로파이 음악", "MUSIC", "calm", 2),
                        ContentCategory.of("멘탈 관리 팁", "EDUCATIONAL", "recovery", 3),
                        ContentCategory.of("기초 가이드", "TUTORIAL", "restart", 4)
                ));
                break;

            case SLUMP:
                categories.addAll(Arrays.asList(
                        ContentCategory.of("롤 실력 향상", "EDUCATIONAL", "improvement", 1),
                        ContentCategory.of("차분한 음악", "MUSIC", "focus", 2),
                        ContentCategory.of("프로게이머 강의", "TUTORIAL", "learning", 3),
                        ContentCategory.of("랭크 승급 가이드", "EDUCATIONAL", "ranking", 4)
                ));
                break;

            case HOT_STREAK:
                categories.addAll(Arrays.asList(
                        ContentCategory.of("롤 매드무비", "ENTERTAINMENT", "epic", 1),
                        ContentCategory.of("신나는 게임 BGM", "MUSIC", "energetic", 2),
                        ContentCategory.of("프로 경기 명장면", "ENTERTAINMENT", "pro", 3),
                        ContentCategory.of("Epic Gaming Music", "MUSIC", "hype", 4)
                ));
                break;

            case WINNING_MOOD:
                categories.addAll(Arrays.asList(
                        ContentCategory.of("롤 재밌는 플레이", "ENTERTAINMENT", "fun", 1),
                        ContentCategory.of("업템포 음악", "MUSIC", "upbeat", 2),
                        ContentCategory.of("상위티어 플레이", "EDUCATIONAL", "advanced", 3),
                        ContentCategory.of("메타 분석", "META", "trending", 4)
                ));
                break;

            case CS_STRUGGLE:
                categories.addAll(Arrays.asList(
                        ContentCategory.of("CS 연습법", "TUTORIAL", "cs", 1),
                        ContentCategory.of("라스트힛 가이드", "EDUCATIONAL", "farming", 2),
                        ContentCategory.of("집중력 음악", "MUSIC", "concentration", 3),
                        ContentCategory.of("웨이브 관리", "TUTORIAL", "wave", 4)
                ));
                break;

            case KDA_STRUGGLE:
                categories.addAll(Arrays.asList(
                        ContentCategory.of("포지셔닝 가이드", "TUTORIAL", "positioning", 1),
                        ContentCategory.of("안전한 플레이", "EDUCATIONAL", "safe", 2),
                        ContentCategory.of("한타 기여도", "TUTORIAL", "teamfight", 3),
                        ContentCategory.of("생존 플레이", "EDUCATIONAL", "survival", 4)
                ));
                break;

            default:
                categories.addAll(Arrays.asList(
                        ContentCategory.of("롤 꿀팁", "EDUCATIONAL", "tips", 1),
                        ContentCategory.of("메타 분석", "META", "current", 2),
                        ContentCategory.of("재밌는 컨텐츠", "ENTERTAINMENT", "variety", 3),
                        ContentCategory.of("배경 음악", "MUSIC", "ambient", 4)
                ));
        }

        return categories;
    }

    /**
     * 🔍 카테고리별 검색 & 필터링
     */
    private List<SmartVideoRecommendation> searchAndFilterByCategory(
            ContentCategory category, PlayerEmotionState emotionState) {

        try {
            List<YouTubeVideoInfo> videos = searchYouTubeByCategory(category);
            List<SmartVideoRecommendation> filtered = new ArrayList<>();

            for (YouTubeVideoInfo video : videos) {
                double relevanceScore = evaluateVideoRelevance(video, category, emotionState);

                if (relevanceScore > 0.5) {
                    filtered.add(SmartVideoRecommendation.builder()
                            .videoId(video.getVideoId())
                            .title(video.getTitle())
                            .description(video.getDescription())
                            .channelTitle(video.getChannelTitle())
                            .thumbnailUrl(video.getThumbnailUrl())
                            .videoUrl("https://www.youtube.com/watch?v=" + video.getVideoId())
                            .category(category.getCategory())
                            .relevanceScore(relevanceScore)
                            .emotionMatch(category.getMood())
                            .personalizedReason(generateEmotionReason(category, emotionState))
                            .build());
                }
            }

            return filtered.stream()
                    .sorted(Comparator.comparing(SmartVideoRecommendation::getRelevanceScore).reversed())
                    .limit(2)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("카테고리 검색 실패: " + category.getSearchKeywords(), e);
            return Collections.emptyList();
        }
    }

    /**
     * 🤖 영상 관련성 평가
     */
    private double evaluateVideoRelevance(YouTubeVideoInfo video, ContentCategory category, PlayerEmotionState emotionState) {
        try {
            return geminiAIService.evaluateEmotionMatch(
                    video.getTitle(),
                    video.getDescription(),
                    emotionState.getEmotionType().toString(),
                    category.getMood()
            );
        } catch (Exception e) {
            return evaluateVideoRelevanceBasic(video, category);
        }
    }

    /**
     * 📊 기본 관련성 평가
     */
    private double evaluateVideoRelevanceBasic(YouTubeVideoInfo video, ContentCategory category) {
        String title = video.getTitle().toLowerCase();
        String description = video.getDescription().toLowerCase();
        String keywords = category.getSearchKeywords().toLowerCase();

        double score = 0.5;

        String[] keywordArray = keywords.split(" ");
        for (String keyword : keywordArray) {
            if (title.contains(keyword)) score += 0.15;
            if (description.contains(keyword)) score += 0.1;
        }

        if (title.contains("롤") || title.contains("lol") || title.contains("리그") ||
                title.contains("league") || title.contains("legends")) {
            score += 0.2;
        }

        return Math.min(score, 1.0);
    }

    /**
     * 📺 YouTube 검색
     */
    private List<YouTubeVideoInfo> searchYouTubeByCategory(ContentCategory category) {
        try {
            String searchQuery = category.getSearchKeywords();
            String encodedQuery = URLEncoder.encode(searchQuery, StandardCharsets.UTF_8);

            String url = String.format(
                    "https://www.googleapis.com/youtube/v3/search?part=snippet&q=%s&type=video&maxResults=10&order=relevance&regionCode=KR&relevanceLanguage=ko&key=%s",
                    encodedQuery, youtubeApiKey
            );

            String response = restTemplate.getForObject(url, String.class);
            JsonNode jsonResponse = objectMapper.readTree(response);

            List<YouTubeVideoInfo> videos = new ArrayList<>();
            for (JsonNode item : jsonResponse.get("items")) {
                JsonNode snippet = item.get("snippet");
                videos.add(YouTubeVideoInfo.builder()
                        .videoId(item.get("id").get("videoId").asText())
                        .title(snippet.get("title").asText())
                        .description(snippet.get("description").asText())
                        .channelTitle(snippet.get("channelTitle").asText())
                        .publishedAt(snippet.get("publishedAt").asText())
                        .thumbnailUrl(snippet.get("thumbnails").get("medium").get("url").asText())
                        .build());
            }

            return videos;

        } catch (Exception e) {
            log.error("YouTube 검색 실패: " + category.getSearchKeywords(), e);
            return Collections.emptyList();
        }
    }

    /**
     * ✨ 최종 추천 정렬
     */
    private List<SmartVideoRecommendation> finalizeRecommendations(
            List<SmartVideoRecommendation> allRecommendations, PlayerEmotionState emotionState) {

        return allRecommendations.stream()
                .distinct()
                .sorted(Comparator.comparing(SmartVideoRecommendation::getRelevanceScore).reversed())
                .limit(8)
                .collect(Collectors.toList());
    }

    /**
     * 🛡️ 폴백 추천
     */
    private List<SmartVideoRecommendation> createEmotionFallbackRecommendations(String playerName) {
        List<SmartVideoRecommendation> fallback = new ArrayList<>();

        fallback.add(SmartVideoRecommendation.builder()
                .videoId("fallback1")
                .title("롤 기초 가이드 - 초보자를 위한 완벽 정리")
                .description("리그오브레전드를 처음 시작하는 분들을 위한 핵심 가이드입니다.")
                .channelTitle("LoL Guide")
                .thumbnailUrl("https://via.placeholder.com/320x180?text=LoL+Guide")
                .videoUrl("https://youtube.com/watch?v=fallback1")
                .category("EDUCATIONAL")
                .relevanceScore(0.8)
                .personalizedReason("기본 추천 콘텐츠입니다")
                .build());

        return fallback;
    }

    // === 유틸리티 메서드들 ===

    /**
     * 🔄 연속 승/패 계산
     */
    private int calculateStreak(List<MatchDto> matches, String playerName) {
        if (matches.isEmpty()) return 0;

        MatchDto.MatchAnalysis firstAnalysis = matches.get(0).analyzeMatch(playerName);
        if (!firstAnalysis.isFound()) return 0;

        boolean firstResult = firstAnalysis.isWin();
        int streak = firstResult ? 1 : -1;

        for (int i = 1; i < matches.size(); i++) {
            MatchDto.MatchAnalysis analysis = matches.get(i).analyzeMatch(playerName);
            if (!analysis.isFound()) break;

            if (analysis.isWin() == firstResult) {
                streak = firstResult ? streak + 1 : streak - 1;
            } else {
                break;
            }
        }

        return streak;
    }

    private int calculateIntensityLevel(int streakLength, double performance) {
        int intensity = Math.min(streakLength / 2, 5);
        if (Math.abs(performance) > 0.5) intensity += 2;
        return Math.min(intensity, 10);
    }

    private String generateEmotionReason(ContentCategory category, PlayerEmotionState state) {
        return String.format("%s 상태에 맞는 %s 콘텐츠입니다",
                state.getEmotionType().getDisplayName(),
                category.getCategory());
    }

    // === 데이터 클래스들 ===

    @Data
    @Builder
    public static class PlayerEmotionState {
        private String playerName;
        private EmotionType emotionType;
        private double winRate;
        private double averageKDA;
        private double averageCS;
        private int currentStreak;
        private int streakLength;
        private boolean isWinStreak;
        private double recentPerformance;
        private int intensityLevel;
    }

    public enum EmotionType {
        DEEP_SLUMP("극심한 슬럼프"),
        SLUMP("슬럼프"),
        HOT_STREAK("폭주 모드"),
        WINNING_MOOD("승승장구"),
        CS_STRUGGLE("CS 고민"),
        KDA_STRUGGLE("생존 고민"),
        STABLE("안정 상태");

        private final String displayName;

        EmotionType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    @Data
    @Builder
    public static class ContentCategory {
        private String searchKeywords;
        private String category;
        private String mood;
        private int priority;
        private String description;

        public static ContentCategory of(String keywords, String category, String mood, int priority) {
            return ContentCategory.builder()
                    .searchKeywords(keywords)
                    .category(category)
                    .mood(mood)
                    .priority(priority)
                    .description(keywords + " 관련 콘텐츠")
                    .build();
        }
    }

    @Data
    @Builder
    public static class SmartVideoRecommendation {
        private String videoId;
        private String title;
        private String description;
        private String channelTitle;
        private String thumbnailUrl;
        private String videoUrl;
        private String category;
        private double relevanceScore;
        private String emotionMatch;
        private String personalizedReason;
    }

    @Data
    @Builder
    public static class YouTubeVideoInfo {
        private String videoId;
        private String title;
        private String description;
        private String channelTitle;
        private String publishedAt;
        private String thumbnailUrl;
    }
}