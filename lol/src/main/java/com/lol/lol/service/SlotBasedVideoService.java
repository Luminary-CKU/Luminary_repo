package com.lol.lol.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class SlotBasedVideoService {

    private final GeminiAIService geminiAIService;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${youtube.api.key}")
    private String youtubeApiKey;

    /**
     * 🎯 슬롯 기반 영상 추천 메인 메서드
     */
    public List<SlotVideoRecommendation> generateSlotBasedRecommendations(String playerTier) {
        log.info("=== 슬롯 기반 영상 추천 시작: {} ===", playerTier);

        List<SlotVideoRecommendation> recommendations = new ArrayList<>();

        try {
            // 4개 고정 슬롯
            recommendations.add(searchSlot("TIERLIST", getTierlistPrompt()));
            recommendations.add(searchSlot("META", getMetaPrompt()));
            recommendations.add(searchSlot("CALM_MUSIC", getCalmMusicPrompt()));
            recommendations.add(searchSlot("ENERGETIC_MUSIC", getEnergeticMusicPrompt()));

            // 2개 티어별 슬롯
            String tierCategory = getTierCategory(playerTier);
            recommendations.add(searchSlot("TIER_GUIDE", getTierGuidePrompt(tierCategory)));
            recommendations.add(searchSlot("TIER_CONTENT", getTierContentPrompt(tierCategory)));

            log.info("슬롯 기반 추천 완료: {} 개", recommendations.size());
            return recommendations;

        } catch (Exception e) {
            log.error("슬롯 기반 추천 실패", e);
            return createFallbackSlots();
        }
    }

    /**
     * 🔍 개별 슬롯 검색
     */
    private SlotVideoRecommendation searchSlot(String slotType, String aiPrompt) {
        try {
            // 1. AI가 검색어 3개 생성
            List<String> searchTerms = generateSearchTerms(aiPrompt);

            // 2. YouTube API로 영상 검색
            List<YouTubeVideo> candidates = new ArrayList<>();
            for (String term : searchTerms) {
                candidates.addAll(searchYouTube(term, 3));
            }

            // 3. 슬롯에 맞는 영상 필터링
            List<YouTubeVideo> validVideos = candidates.stream()
                    .filter(video -> isValidForSlot(video, slotType))
                    .toList();

            // 4. 최적 영상 선택
            YouTubeVideo bestVideo = selectBestVideo(validVideos, slotType);

            return SlotVideoRecommendation.builder()
                    .slotType(slotType)
                    .slotName(getSlotDisplayName(slotType))
                    .video(bestVideo)
                    .confidence(0.85)
                    .build();

        } catch (Exception e) {
            log.error("슬롯 {} 검색 실패", slotType, e);
            return createFallbackSlot(slotType);
        }
    }

    /**
     * 🤖 AI 검색어 생성
     */
    private List<String> generateSearchTerms(String prompt) {
        try {
            String aiResponse = geminiAIService.completion(prompt);
            JsonNode jsonNode = objectMapper.readTree(aiResponse);

            List<String> terms = new ArrayList<>();
            if (jsonNode.isArray()) {
                for (JsonNode term : jsonNode) {
                    terms.add(term.asText());
                }
            }

            return terms;
        } catch (Exception e) {
            log.error("AI 검색어 생성 실패", e);
            return Arrays.asList("리그오브레전드", "롤 가이드", "LOL");
        }
    }

    /**
     * 📺 YouTube API 검색
     */
    private List<YouTubeVideo> searchYouTube(String searchTerm, int maxResults) {
        try {
            String encodedQuery = URLEncoder.encode(searchTerm, StandardCharsets.UTF_8);
            String url = String.format(
                    "https://www.googleapis.com/youtube/v3/search?part=snippet&q=%s&type=video&maxResults=%d&order=relevance&regionCode=KR&relevanceLanguage=ko&key=%s",
                    encodedQuery, maxResults, youtubeApiKey
            );

            String response = restTemplate.getForObject(url, String.class);
            JsonNode jsonResponse = objectMapper.readTree(response);

            List<YouTubeVideo> videos = new ArrayList<>();
            for (JsonNode item : jsonResponse.get("items")) {
                JsonNode snippet = item.get("snippet");
                videos.add(YouTubeVideo.builder()
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
            log.error("YouTube 검색 실패: {}", searchTerm, e);
            return Collections.emptyList();
        }
    }

    /**
     * ✅ 슬롯별 영상 유효성 검증
     */
    private boolean isValidForSlot(YouTubeVideo video, String slotType) {
        String title = video.getTitle().toLowerCase();

        return switch (slotType) {
            case "TIERLIST" ->
                    (title.contains("티어리스트") || title.contains("랭킹") || title.contains("최강")) &&
                            (title.contains("롤") || title.contains("리그오브레전드"));

            case "META" ->
                    (title.contains("메타") || title.contains("op") || title.contains("신빌드") || title.contains("핫")) &&
                            (title.contains("롤") || title.contains("리그오브레전드"));

            case "CALM_MUSIC" ->
                    (title.contains("브금") || title.contains("음악") || title.contains("bgm") || title.contains("ost")) &&
                            (title.contains("잔잔") || title.contains("집중") || title.contains("lofi"));

            case "ENERGETIC_MUSIC" ->
                    (title.contains("브금") || title.contains("음악") || title.contains("bgm")) &&
                            (title.contains("신나는") || title.contains("액션") || title.contains("하이라이트"));

            case "TIER_GUIDE" ->
                    (title.contains("가이드") || title.contains("강의") || title.contains("튜토리얼")) &&
                            (title.contains("롤") || title.contains("리그오브레전드"));

            case "TIER_CONTENT" ->
                    title.contains("롤") || title.contains("리그오브레전드");

            default -> true;
        };
    }

    /**
     * 🎯 최적 영상 선택
     */
    private YouTubeVideo selectBestVideo(List<YouTubeVideo> videos, String slotType) {
        if (videos.isEmpty()) {
            return createDefaultVideo(slotType);
        }

        // 첫 번째 유효한 영상 선택 (추후 AI 평가로 개선 가능)
        return videos.get(0);
    }

    /**
     * 📋 AI 프롬프트들
     */
    private String getTierlistPrompt() {
        return """
            리그오브레전드 최신 티어리스트 영상을 찾기 위한 검색어 3개를 JSON 배열로 반환해줘.
            조건:
            - 제목에 '티어리스트' 또는 '랭킹' 포함
            - 최근 2주 이내 업로드
            - 한국어 영상
            
            예시: ["롤 티어리스트 2024", "리그오브레전드 최강 챔피언", "14.24 패치 랭킹"]
            """;
    }

    private String getMetaPrompt() {
        return """
            현재 핫한 리그오브레전드 메타 영상을 찾기 위한 검색어 3개를 JSON 배열로 반환해줘.
            조건:
            - 제목에 '메타', 'OP', '신빌드' 중 하나 포함
            - 최근 1주 이내 업로드
            - 화제가 된 전략
            
            예시: ["롤 신메타 2024", "OP 빌드 조합", "핫한 픽 전략"]
            """;
    }

    private String getCalmMusicPrompt() {
        return """
            리그오브레전드하면서 듣기 좋은 잔잔한 음악을 찾기 위한 검색어 3개를 JSON 배열로 반환해줘.
            조건:
            - 1시간 이상 영상
            - 차분하고 집중되는 분위기
            - 인스트루멘털 우선
            
            예시: ["롤 브금 잔잔한", "집중하기 좋은 게임음악", "lofi 리그오브레전드"]
            """;
    }

    private String getEnergeticMusicPrompt() {
        return """
            리그오브레전드 플레이할 때 텐션 올려주는 신나는 음악을 찾기 위한 검색어 3개를 JSON 배열로 반환해줘.
            조건:
            - 업비트하고 에너지 넘치는 곡
            - 액션에 어울리는 분위기
            - 30분 이상 영상
            
            예시: ["신나는 게임 브금", "액션 하이라이트 음악", "펜타킬 브금"]
            """;
    }

    private String getTierGuidePrompt(String tierCategory) {
        return switch (tierCategory) {
            case "LOW_TIER" -> """
                아이언~실버 티어를 위한 롤 기초 강의 영상을 찾기 위한 검색어 3개를 JSON 배열로 반환해줘.
                예시: ["롤 초보 완벽가이드", "기초부터 배우는 리그오브레전드", "롤 입문자 필수시청"]
                """;
            case "MID_TIER" -> """
                골드~플래티넘 티어 실력 향상을 위한 심화 전략 영상을 찾기 위한 검색어 3개를 JSON 배열로 반환해줘.
                예시: ["골드 탈출 확실한 방법", "플래티넘 승급 꿀팁", "숨겨진 고급 테크닉"]
                """;
            case "HIGH_TIER" -> """
                다이아 이상 고티어를 위한 예능성 롤 콘텐츠를 찾기 위한 검색어 3개를 JSON 배열로 반환해줘.
                예시: ["롤 예능 레전드", "웃긴 하이라이트 모음", "재밌는 스트리머 방송"]
                """;
            default -> getTierGuidePrompt("LOW_TIER");
        };
    }

    private String getTierContentPrompt(String tierCategory) {
        return switch (tierCategory) {
            case "LOW_TIER" -> """
                초보자를 위한 쉬운 챔피언 가이드 영상을 찾기 위한 검색어 3개를 JSON 배열로 반환해줘.
                예시: ["쉬운 챔피언 추천", "초보 챔프 가이드", "롤 입문 챔피언"]
                """;
            case "MID_TIER" -> """
                중급자를 위한 고급 테크닉 영상을 찾기 위한 검색어 3개를 JSON 배열로 반환해줘.
                예시: ["프로 선수 꿀팁", "숨겨진 테크닉", "고수들의 플레이"]
                """;
            case "HIGH_TIER" -> """
                고수를 위한 프로 경기 분석 영상을 찾기 위한 검색어 3개를 JSON 배열로 반환해줘.
                예시: ["LCK 하이라이트", "프로 경기 분석", "월드챔피언십"]
                """;
            default -> getTierContentPrompt("LOW_TIER");
        };
    }

    /**
     * 🏷️ 티어 카테고리 분류
     */
    private String getTierCategory(String tier) {
        if (tier == null) return "LOW_TIER";

        return switch (tier.toUpperCase()) {
            case "IRON", "BRONZE", "SILVER" -> "LOW_TIER";
            case "GOLD", "PLATINUM", "DIAMOND" -> "MID_TIER";
            case "MASTER", "GRANDMASTER", "CHALLENGER" -> "HIGH_TIER";
            default -> "LOW_TIER";
        };
    }

    /**
     * 🎨 슬롯 표시명
     */
    private String getSlotDisplayName(String slotType) {
        return switch (slotType) {
            case "TIERLIST" -> "🏆 최신 티어리스트";
            case "META" -> "🔥 핫한 메타";
            case "CALM_MUSIC" -> "🎵 잔잔한 음악";
            case "ENERGETIC_MUSIC" -> "🎶 신나는 음악";
            case "TIER_GUIDE" -> "📚 실력 향상 가이드";
            case "TIER_CONTENT" -> "💡 맞춤 콘텐츠";
            default -> "📺 추천 영상";
        };
    }

    /**
     * 🛡️ 폴백 시스템
     */
    private List<SlotVideoRecommendation> createFallbackSlots() {
        return Arrays.asList(
                createFallbackSlot("TIERLIST"),
                createFallbackSlot("META"),
                createFallbackSlot("CALM_MUSIC"),
                createFallbackSlot("ENERGETIC_MUSIC"),
                createFallbackSlot("TIER_GUIDE"),
                createFallbackSlot("TIER_CONTENT")
        );
    }

    private SlotVideoRecommendation createFallbackSlot(String slotType) {
        YouTubeVideo fallbackVideo = createDefaultVideo(slotType);

        return SlotVideoRecommendation.builder()
                .slotType(slotType)
                .slotName(getSlotDisplayName(slotType))
                .video(fallbackVideo)
                .confidence(0.5)
                .build();
    }

    private YouTubeVideo createDefaultVideo(String slotType) {
        return switch (slotType) {
            case "TIERLIST" -> YouTubeVideo.builder()
                    .videoId("fallback_tierlist")
                    .title("리그오브레전드 티어리스트 - 최강 챔피언 순위")
                    .description("현재 메타 최강 챔피언들을 확인해보세요")
                    .channelTitle("LoL Guide")
                    .thumbnailUrl("data:image/svg+xml;base64,...")
                    .build();
            case "META" -> YouTubeVideo.builder()
                    .videoId("fallback_meta")
                    .title("핫한 신메타 - 지금 당장 써먹어야 할 전략")
                    .description("현재 가장 강력한 메타 전략입니다")
                    .channelTitle("Meta Master")
                    .thumbnailUrl("data:image/svg+xml;base64,...")
                    .build();
            default -> YouTubeVideo.builder()
                    .videoId("fallback_default")
                    .title("리그오브레전드 가이드")
                    .description("롤 플레이에 도움이 되는 영상입니다")
                    .channelTitle("LoL Helper")
                    .thumbnailUrl("data:image/svg+xml;base64,...")
                    .build();
        };
    }

    // DTO 클래스들
    @Data
    @Builder
    public static class SlotVideoRecommendation {
        private String slotType;
        private String slotName;
        private YouTubeVideo video;
        private double confidence;
    }

    @Data
    @Builder
    public static class YouTubeVideo {
        private String videoId;
        private String title;
        private String description;
        private String channelTitle;
        private String publishedAt;
        private String thumbnailUrl;
    }
}