package com.lol.lol.service;

import com.lol.lol.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChampionRecommendationService {

    private final DynamicRecommendationEngine dynamicRecommendationEngine;
    private final PlayerAnalysisEngine playerAnalysisEngine;
    private final DynamicChampionDataService championDataService;

    /**
     * 🎯 메인 추천 생성 메서드 (SearchController에서 호출)
     * PersonalizedChampionRecommendation을 ChampionRecommendationResult로 변환
     */
    public ChampionRecommendationResult generateChampionRecommendations(
            SummonerDto summoner,
            List<LeagueDto> leagues,
            List<MatchDto> matches,
            String playerName) {

        try {
            log.info("=== 챔피언 추천 생성 시작: {} ===", playerName);

            // 1. 전적 데이터 기반 AI 추천 생성
            ChampionRecommendationResult result = dynamicRecommendationEngine
                    .generateRecommendations(playerName, matches);

            // 2. 리그 정보를 활용한 추가 보정
            if (leagues != null && !leagues.isEmpty()) {
                result = enhanceWithLeagueData(result, leagues);
            }

            // 3. 소환사 레벨 기반 난이도 조정
            if (summoner != null && summoner.getSummonerLevel() != null) {
                result = adjustForSummonerLevel(result, summoner.getSummonerLevel());
            }

            log.info("챔피언 추천 생성 완료: {} 개 챔피언",
                    result.getRecommendations().size());

            return result;

        } catch (Exception e) {
            log.error("챔피언 추천 생성 실패: " + playerName, e);
            return createFallbackRecommendations(playerName);
        }
    }

    /**
     * 리그 정보를 활용한 추천 보정
     */
    private ChampionRecommendationResult enhanceWithLeagueData(
            ChampionRecommendationResult result, List<LeagueDto> leagues) {

        try {
            LeagueDto mainRank = leagues.get(0);
            String tier = mainRank.getTier();

            // 티어별 메타 강도 가중치 조정
            double metaWeight = calculateMetaWeight(tier);

            List<ChampionRecommendation> enhanced = result.getRecommendations().stream()
                    .map(rec -> enhanceRecommendationWithTier(rec, tier, metaWeight))
                    .sorted(Comparator.comparing(ChampionRecommendation::getConfidenceScore).reversed())
                    .collect(Collectors.toList());

            // 라인별 재그룹핑
            Map<String, List<ChampionRecommendation>> byRole = enhanced.stream()
                    .collect(Collectors.groupingBy(
                            ChampionRecommendation::getPrimaryRole,
                            Collectors.collectingAndThen(
                                    Collectors.toList(),
                                    list -> list.stream()
                                            .sorted(Comparator.comparing(ChampionRecommendation::getConfidenceScore).reversed())
                                            .limit(6) // 라인당 최대 6개
                                            .collect(Collectors.toList())
                            )
                    ));

            return ChampionRecommendationResult.builder()
                    .recommendations(enhanced)
                    .recommendationsByRole(byRole)
                    .playerProfile(result.getPlayerProfile())
                    .personalizedMessage(result.getPersonalizedMessage())
                    .generatedAt(result.getGeneratedAt())
                    .totalAnalyzedChampions(result.getTotalAnalyzedChampions())
                    .build();

        } catch (Exception e) {
            log.error("리그 데이터 보정 실패", e);
            return result;
        }
    }

    /**
     * 소환사 레벨 기반 난이도 조정
     */
    private ChampionRecommendationResult adjustForSummonerLevel(
            ChampionRecommendationResult result, Long summonerLevel) {

        try {
            // 레벨별 추천 전략
            String strategy = determineRecommendationStrategy(summonerLevel);

            List<ChampionRecommendation> adjusted = result.getRecommendations().stream()
                    .map(rec -> adjustDifficultyForLevel(rec, summonerLevel))
                    .collect(Collectors.toList());

            String enhancedMessage = result.getPersonalizedMessage() +
                    " " + generateLevelBasedMessage(summonerLevel, strategy);

            return ChampionRecommendationResult.builder()
                    .recommendations(adjusted)
                    .recommendationsByRole(result.getRecommendationsByRole())
                    .playerProfile(result.getPlayerProfile())
                    .personalizedMessage(enhancedMessage)
                    .generatedAt(result.getGeneratedAt())
                    .totalAnalyzedChampions(result.getTotalAnalyzedChampions())
                    .build();

        } catch (Exception e) {
            log.error("레벨 기반 조정 실패", e);
            return result;
        }
    }

    /**
     * 티어별 메타 가중치 계산
     */
    private double calculateMetaWeight(String tier) {
        if (tier == null) return 0.3;

        switch (tier.toUpperCase()) {
            case "CHALLENGER":
            case "GRANDMASTER":
            case "MASTER":
                return 0.9; // 고랭에선 메타가 매우 중요
            case "DIAMOND":
            case "EMERALD":
                return 0.7;
            case "PLATINUM":
            case "GOLD":
                return 0.5;
            default:
                return 0.3; // 저랭에선 메타보다 개인 실력이 중요
        }
    }

    /**
     * 티어 기반 추천 강화
     */
    private ChampionRecommendation enhanceRecommendationWithTier(
            ChampionRecommendation rec, String tier, double metaWeight) {

        try {
            // 메타 강도에 따른 신뢰도 재계산
            double baseConfidence = rec.getConfidenceScore();
            double metaBonus = rec.getMetaStrength() / 10.0 * metaWeight * 0.2;
            double newConfidence = Math.min(1.0, baseConfidence + metaBonus);

            // 티어별 추천 이유 추가
            List<String> enhancedReasons = new ArrayList<>(rec.getReasons());
            enhancedReasons.add(generateTierSpecificReason(tier, rec.getMetaStrength()));

            return ChampionRecommendation.builder()
                    .championName(rec.getChampionName())
                    .primaryRole(rec.getPrimaryRole())
                    .confidenceScore(newConfidence)
                    .metaStrength(rec.getMetaStrength())
                    .difficulty(rec.getDifficulty())
                    .reasons(enhancedReasons)
                    .personalStats(rec.getPersonalStats())
                    .styleMatch(rec.getStyleMatch())
                    .skillMatch(rec.getSkillMatch())
                    .recommendationType(rec.getRecommendationType())
                    .build();

        } catch (Exception e) {
            log.error("티어 기반 강화 실패: " + rec.getChampionName(), e);
            return rec;
        }
    }

    /**
     * 레벨 기반 난이도 조정
     */
    private ChampionRecommendation adjustDifficultyForLevel(
            ChampionRecommendation rec, Long level) {

        try {
            String adjustedDifficulty = rec.getDifficulty();

            // 초보자(레벨 30 이하) 난이도 상향 조정
            if (level < 30) {
                switch (rec.getDifficulty()) {
                    case "쉬움": adjustedDifficulty = "쉬움"; break;
                    case "보통": adjustedDifficulty = "어려움"; break;
                    case "어려움": adjustedDifficulty = "매우 어려움"; break;
                }
            }
            // 고수(레벨 200 이상) 난이도 하향 조정
            else if (level > 200) {
                switch (rec.getDifficulty()) {
                    case "어려움": adjustedDifficulty = "보통"; break;
                    case "보통": adjustedDifficulty = "쉬움"; break;
                }
            }

            return ChampionRecommendation.builder()
                    .championName(rec.getChampionName())
                    .primaryRole(rec.getPrimaryRole())
                    .confidenceScore(rec.getConfidenceScore())
                    .metaStrength(rec.getMetaStrength())
                    .difficulty(adjustedDifficulty)
                    .reasons(rec.getReasons())
                    .personalStats(rec.getPersonalStats())
                    .styleMatch(rec.getStyleMatch())
                    .skillMatch(rec.getSkillMatch())
                    .recommendationType(rec.getRecommendationType())
                    .build();

        } catch (Exception e) {
            log.error("레벨 기반 난이도 조정 실패", e);
            return rec;
        }
    }

    /**
     * 레벨 기반 추천 전략 결정
     */
    private String determineRecommendationStrategy(Long level) {
        if (level < 30) {
            return "BEGINNER"; // 초보자 친화적
        } else if (level < 100) {
            return "INTERMEDIATE"; // 중급자
        } else if (level < 300) {
            return "ADVANCED"; // 고급자
        } else {
            return "EXPERT"; // 전문가
        }
    }

    /**
     * 티어 전용 추천 이유 생성
     */
    private String generateTierSpecificReason(String tier, double metaStrength) {
        if (tier == null) tier = "UNRANKED";

        switch (tier.toUpperCase()) {
            case "CHALLENGER":
            case "GRANDMASTER":
            case "MASTER":
                if (metaStrength >= 8.0) {
                    return "고랭크에서 현재 최강 메타 챔피언입니다";
                } else {
                    return "마스터리를 통해 고랭크에서도 충분히 활용 가능합니다";
                }

            case "DIAMOND":
            case "EMERALD":
                if (metaStrength >= 7.0) {
                    return "다이아+ 구간에서 높은 성능을 보이는 챔피언입니다";
                } else {
                    return "숙련도를 쌓으면 상위 구간 돌파에 도움이 될 챔피언입니다";
                }

            case "PLATINUM":
            case "GOLD":
                return "현재 티어에서 균형잡힌 성능을 보이는 믿을만한 픽입니다";

            default:
                return "기본기 습득에 도움이 되는 챔피언입니다";
        }
    }

    /**
     * 레벨 기반 메시지 생성
     */
    private String generateLevelBasedMessage(Long level, String strategy) {
        switch (strategy) {
            case "BEGINNER":
                return "초보자에게 친화적인 쉬운 챔피언들을 우선 추천해드렸어요! 🌱";
            case "INTERMEDIATE":
                return "기본기를 다지면서 새로운 도전도 할 수 있는 챔피언들이에요! 📈";
            case "ADVANCED":
                return "실력을 더욱 발전시킬 수 있는 도전적인 챔피언들도 포함했어요! ⚡";
            case "EXPERT":
                return "고급 플레이가 가능한 챔피언들로 메타를 선도해보세요! 🏆";
            default:
                return "여러분의 실력에 맞는 다양한 챔피언들을 준비했어요! 🎮";
        }
    }

    /**
     * 폴백 추천 생성
     */
    private ChampionRecommendationResult createFallbackRecommendations(String playerName) {
        log.warn("폴백 추천 생성: {}", playerName);

        // 기본 안전한 추천들
        List<ChampionRecommendation> fallbackRecs = Arrays.asList(
                createBasicRecommendation("Jinx", "ADC", 0.85, "쉬움", "초보자에게 추천하는 안전한 원거리 딜러"),
                createBasicRecommendation("Garen", "TOP", 0.80, "쉬움", "단순하면서도 강력한 탱커"),
                createBasicRecommendation("Annie", "MID", 0.75, "쉬움", "기본기 연습에 좋은 마법사"),
                createBasicRecommendation("Warwick", "JUNGLE", 0.70, "쉬움", "정글링을 배우기 좋은 챔피언"),
                createBasicRecommendation("Soraka", "SUPPORT", 0.65, "쉬움", "힐링으로 팀을 돕는 서포터"),
                createBasicRecommendation("Ashe", "ADC", 0.75, "쉬움", "유틸리티가 뛰어난 원거리 딜러")
        );

        Map<String, List<ChampionRecommendation>> byRole = fallbackRecs.stream()
                .collect(Collectors.groupingBy(ChampionRecommendation::getPrimaryRole));

        return ChampionRecommendationResult.builder()
                .recommendations(fallbackRecs)
                .recommendationsByRole(byRole)
                .playerProfile(createFallbackProfile(playerName))
                .personalizedMessage("기본 추천 챔피언들을 준비했습니다! 더 많은 게임을 플레이하면 정확한 맞춤 추천을 받을 수 있어요! 🎮")
                .generatedAt(LocalDateTime.now())
                .totalAnalyzedChampions(6)
                .build();
    }

    /**
     * 기본 추천 생성 유틸리티
     */
    private ChampionRecommendation createBasicRecommendation(
            String name, String role, double confidence, String difficulty, String reason) {

        return ChampionRecommendation.builder()
                .championName(name)
                .primaryRole(role)
                .confidenceScore(confidence)
                .metaStrength(6.0)
                .difficulty(difficulty)
                .reasons(Arrays.asList(reason, "현재 메타에서 안정적", "학습하기 좋은 챔피언"))
                .personalStats(null)
                .styleMatch(0.7)
                .skillMatch(0.8)
                .recommendationType("RECOMMENDED")
                .build();
    }

    /**
     * 폴백 플레이어 프로필 생성
     */
    private PlayerProfile createFallbackProfile(String playerName) {
        return PlayerProfile.builder()
                .playerName(playerName)
                .stats(PlayerStats.builder()
                        .gameCount(0)
                        .averageKDA(1.0)
                        .winRate(50.0)
                        .killParticipation(0.5)
                        .earlyGamePerformance(0.5)
                        .lateGamePerformance(0.5)
                        .aggressionIndex(0.5)
                        .visionScore(30.0)
                        .objectiveControl(0.5)
                        .positioningScore(0.5)
                        .mainRole("MID")
                        .mostPlayedChampion("정보없음")
                        .roleDistribution(new HashMap<>())
                        .championStats(new HashMap<>())
                        .build())
                .playStyle(PlayStyle.builder()
                        .primaryStyle("BALANCED")
                        .secondaryStyle("FLEXIBLE")
                        .styleVector(new double[]{0.5, 0.5, 0.5, 0.5, 0.5})
                        .confidence(0.5)
                        .adaptability(0.5)
                        .aggressionLevel(0.5)
                        .teamplayOrientation(0.5)
                        .build())
                .preferences(ChampionPreferences.builder()
                        .mostPlayedChampion("정보없음")
                        .bestPerformanceChampion("정보없음")
                        .championPlayCounts(new HashMap<>())
                        .championWinRates(new HashMap<>())
                        .preferredRoles(new HashSet<>())
                        .build())
                .progression(SkillProgression.builder()
                        .improvementRate(0.0)
                        .skillLevels(Map.of(
                                "mechanics", 0.5,
                                "positioning", 0.5,
                                "gameKnowledge", 0.5,
                                "teamplay", 0.5
                        ))
                        .learningCurve(0.0)
                        .consistencyScore(0.5)
                        .build())
                .lastAnalyzed(LocalDateTime.now())
                .build();
    }

    /**
     * 🌌 Cards Galaxy용 간단한 추천 API
     */
    public Map<String, Object> generateSimpleRecommendationsForGalaxy(
            String playerName, String preferredRole, int count) {

        try {
            // 기본 추천 생성
            ChampionRecommendationResult fullResult = generateChampionRecommendations(
                    null, null, new ArrayList<>(), playerName != null ? playerName : "GalaxyUser"
            );

            // 간단한 형태로 변환
            List<Map<String, Object>> simpleRecs = fullResult.getRecommendations().stream()
                    .filter(rec -> preferredRole == null || "ALL".equals(preferredRole) ||
                            rec.getPrimaryRole().equals(preferredRole))
                    .limit(count)
                    .map(this::convertToGalaxyFormat)
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("champions", simpleRecs);
            response.put("totalCount", simpleRecs.size());
            response.put("playerStyle", fullResult.getPlayerProfile().getPlayStyle().getPrimaryStyle());
            response.put("message", fullResult.getPersonalizedMessage());
            response.put("timestamp", System.currentTimeMillis());

            return response;

        } catch (Exception e) {
            log.error("Galaxy 추천 생성 실패", e);
            return createErrorResponse("추천 시스템에 일시적인 문제가 발생했습니다.");
        }
    }

    /**
     * Galaxy 형태로 변환
     */
    private Map<String, Object> convertToGalaxyFormat(ChampionRecommendation rec) {
        Map<String, Object> galaxy = new HashMap<>();
        galaxy.put("name", rec.getChampionName());
        galaxy.put("role", rec.getPrimaryRole());
        galaxy.put("confidence", Math.round(rec.getConfidenceScore() * 100));
        galaxy.put("difficulty", rec.getDifficulty());
        galaxy.put("tier", getMetaTier(rec.getMetaStrength()));
        galaxy.put("tags", generateTags(rec));
        galaxy.put("reasons", rec.getReasons());
        galaxy.put("styleMatch", Math.round(rec.getStyleMatch() * 100));
        galaxy.put("skillMatch", Math.round(rec.getSkillMatch() * 100));
        galaxy.put("type", rec.getRecommendationType());
        return galaxy;
    }

    private String getMetaTier(double metaStrength) {
        if (metaStrength >= 8.5) return "S";
        if (metaStrength >= 7.0) return "A";
        if (metaStrength >= 5.5) return "B";
        if (metaStrength >= 4.0) return "C";
        return "D";
    }

    private List<String> generateTags(ChampionRecommendation rec) {
        List<String> tags = new ArrayList<>();

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

    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> error = new HashMap<>();
        error.put("champions", Collections.emptyList());
        error.put("totalCount", 0);
        error.put("playerStyle", "BALANCED");
        error.put("message", message);
        error.put("timestamp", System.currentTimeMillis());
        return error;
    }
}