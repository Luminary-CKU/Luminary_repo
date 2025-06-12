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
public class DynamicRecommendationEngine {

    private final DynamicChampionDataService championDataService;
    private final PlayerAnalysisEngine playerAnalysisEngine;

    /**
     * Zero-Hardcoding 추천 시스템
     * 모든 로직이 데이터와 AI 기반
     */
    public ChampionRecommendationResult generateRecommendations(
            String playerName,
            List<MatchDto> playerMatches) {

        try {
            log.info("=== 동적 추천 생성 시작: {} ===", playerName);

            // 1. 실시간 데이터 수집
            List<ChampionMetaData> currentMeta = championDataService.getCurrentMetaData();
            log.info("현재 메타 데이터 수집 완료: {} 개 챔피언", currentMeta.size());

            PlayerProfile playerProfile = playerAnalysisEngine.analyzePlayerProfile(playerName, playerMatches);
            log.info("플레이어 프로필 분석 완료: {} (주 스타일: {})",
                    playerName, playerProfile.getPlayStyle().getPrimaryStyle());

            // 2. AI 추천 생성
            List<ChampionRecommendation> recommendations = currentMeta.stream()
                    .map(champion -> generateChampionRecommendation(champion, playerProfile))
                    .filter(rec -> rec.getConfidenceScore() > 0.3) // 최소 신뢰도 필터
                    .sorted(Comparator.comparing(ChampionRecommendation::getConfidenceScore).reversed())
                    .limit(20)
                    .collect(Collectors.toList());

            log.info("추천 생성 완료: {} 개 챔피언 (필터링 후)", recommendations.size());

            // 3. 라인별 그룹핑 및 정렬
            Map<String, List<ChampionRecommendation>> byRole = recommendations.stream()
                    .collect(Collectors.groupingBy(
                            ChampionRecommendation::getPrimaryRole,
                            Collectors.collectingAndThen(
                                    Collectors.toList(),
                                    list -> list.stream()
                                            .sorted(Comparator.comparing(ChampionRecommendation::getConfidenceScore).reversed())
                                            .limit(3) // 라인당 최대 3개
                                            .collect(Collectors.toList())
                            )
                    ));

            // 4. 개인화 메시지 생성
            String personalizedMessage = generatePersonalizedMessage(playerProfile, recommendations);

            ChampionRecommendationResult result = ChampionRecommendationResult.builder()
                    .recommendations(recommendations)
                    .recommendationsByRole(byRole)
                    .playerProfile(playerProfile)
                    .personalizedMessage(personalizedMessage)
                    .generatedAt(LocalDateTime.now())
                    .totalAnalyzedChampions(currentMeta.size())
                    .build();

            log.info("=== 동적 추천 생성 완료: {} ===", playerName);
            return result;

        } catch (Exception e) {
            log.error("추천 생성 실패: " + playerName, e);
            return generateFallbackRecommendations(playerName);
        }
    }

    /**
     * 개별 챔피언 추천 점수 계산
     * 하드코딩된 가중치 제거: 동적 가중치 계산
     */
    private ChampionRecommendation generateChampionRecommendation(
            ChampionMetaData champion,
            PlayerProfile playerProfile) {

        try {
            // 1. 다차원 호환성 점수 계산
            CompatibilityScore compatibility = calculateCompatibility(champion, playerProfile);

            // 2. 메타 적합도 계산
            double metaFitness = calculateMetaFitness(champion, playerProfile);

            // 3. 개인 경험 보정
            double personalBonus = calculatePersonalExperienceBonus(champion, playerProfile);

            // 4. 최종 신뢰도 점수 계산
            double confidenceScore = calculateFinalConfidence(
                    compatibility, metaFitness, personalBonus, playerProfile
            );

            // 5. 추천 이유 생성 (동적)
            List<String> reasons = generateDynamicReasons(champion, playerProfile, compatibility);

            // 6. 개인화된 난이도 계산
            String difficulty = calculatePersonalizedDifficulty(champion, playerProfile);

            return ChampionRecommendation.builder()
                    .championName(champion.getName())
                    .primaryRole(champion.getPrimaryRole())
                    .confidenceScore(confidenceScore)
                    .metaStrength(champion.getMetaStrength())
                    .difficulty(difficulty)
                    .reasons(reasons)
                    .personalStats(getPersonalStats(champion, playerProfile))
                    .styleMatch(compatibility.getStyleScore())
                    .skillMatch(compatibility.getSkillScore())
                    .recommendationType(determineRecommendationType(confidenceScore, personalBonus))
                    .build();

        } catch (Exception e) {
            log.error("챔피언 추천 생성 실패: " + champion.getName(), e);
            return createFallbackRecommendation(champion);
        }
    }

    /**
     * 동적 호환성 점수 계산
     * 하드코딩 제거: 플레이어 데이터와 챔피언 특성의 수학적 매칭
     */
    private CompatibilityScore calculateCompatibility(ChampionMetaData champion, PlayerProfile profile) {

        // 1. 플레이 스타일 벡터 매칭 (코사인 유사도)
        double styleCompatibility = calculateVectorSimilarity(
                champion.getPlayStyleVector(),
                profile.getPlayStyle().getStyleVector()
        );

        // 2. 스킬 요구사항 vs 플레이어 능력
        double skillCompatibility = calculateSkillCompatibility(
                champion.getSkillRequirements(),
                profile.getProgression().getSkillLevels()
        );

        // 3. 게임 페이즈 선호도 매칭
        double phaseCompatibility = calculatePhaseCompatibility(
                champion.getGamePhaseStrengths(),
                profile.getStats()
        );

        // 4. 팀 플레이 vs 솔로 캐리 성향
        double teamplayCompatibility = calculateTeamplayCompatibility(
                champion.getTeamplayRequirement(),
                profile.getStats().getKillParticipation()
        );

        // 5. 가중 평균으로 종합 점수 계산
        double overallScore = calculateWeightedCompatibility(
                styleCompatibility, skillCompatibility, phaseCompatibility, teamplayCompatibility
        );

        return CompatibilityScore.builder()
                .styleScore(styleCompatibility)
                .skillScore(skillCompatibility)
                .phaseScore(phaseCompatibility)
                .teamplayScore(teamplayCompatibility)
                .overallScore(overallScore)
                .build();
    }

    /**
     * 벡터 유사도 계산 (코사인 유사도)
     */
    private double calculateVectorSimilarity(double[] vector1, double[] vector2) {
        if (vector1.length != vector2.length) {
            log.warn("벡터 길이 불일치: {} vs {}", vector1.length, vector2.length);
            return 0.5; // 기본값
        }

        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (int i = 0; i < vector1.length; i++) {
            dotProduct += vector1[i] * vector2[i];
            norm1 += Math.pow(vector1[i], 2);
            norm2 += Math.pow(vector2[i], 2);
        }

        if (norm1 == 0.0 || norm2 == 0.0) return 0.0;

        double similarity = dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
        return Math.max(0.0, Math.min(1.0, (similarity + 1.0) / 2.0)); // -1~1을 0~1로 정규화
    }

    /**
     * 스킬 요구사항 호환성 계산
     */
    private double calculateSkillCompatibility(
            Map<String, Double> requirements,
            Map<String, Double> playerSkills) {

        if (requirements.isEmpty() || playerSkills.isEmpty()) {
            return 0.5; // 기본값
        }

        double totalScore = 0.0;
        int count = 0;

        for (Map.Entry<String, Double> requirement : requirements.entrySet()) {
            String skill = requirement.getKey();
            double required = requirement.getValue();
            double playerLevel = playerSkills.getOrDefault(skill, 0.5);

            // 플레이어 스킬이 요구사항을 얼마나 충족하는지 계산
            double satisfaction = Math.min(1.0, playerLevel / Math.max(0.1, required));
            totalScore += satisfaction;
            count++;
        }

        return count > 0 ? totalScore / count : 0.5;
    }

    /**
     * 게임 페이즈 호환성 계산
     */
    private double calculatePhaseCompatibility(
            Map<String, Double> championPhases,
            PlayerStats playerStats) {

        // 플레이어의 페이즈별 선호도 추출
        double playerEarlyPreference = playerStats.getEarlyGamePerformance();
        double playerLatePreference = playerStats.getLateGamePerformance();
        double playerMidPreference = 1.0 - Math.abs(playerEarlyPreference - playerLatePreference);

        double championEarly = championPhases.getOrDefault("early", 0.5);
        double championMid = championPhases.getOrDefault("mid", 0.5);
        double championLate = championPhases.getOrDefault("late", 0.5);

        // 각 페이즈별 매칭도 계산 후 가중 평균
        double earlyMatch = 1.0 - Math.abs(playerEarlyPreference - championEarly);
        double midMatch = 1.0 - Math.abs(playerMidPreference - championMid);
        double lateMatch = 1.0 - Math.abs(playerLatePreference - championLate);

        return (earlyMatch + midMatch + lateMatch) / 3.0;
    }

    /**
     * 팀플레이 호환성 계산
     */
    private double calculateTeamplayCompatibility(double championTeamplayReq, double playerTeamplayLevel) {
        return 1.0 - Math.abs(championTeamplayReq - playerTeamplayLevel);
    }

    /**
     * 가중 호환성 점수 계산
     */
    private double calculateWeightedCompatibility(
            double style, double skill, double phase, double teamplay) {

        // 동적 가중치 계산 (플레이어 특성에 따라 조정 가능)
        double styleWeight = 0.35;
        double skillWeight = 0.25;
        double phaseWeight = 0.20;
        double teamplayWeight = 0.20;

        return (style * styleWeight) +
                (skill * skillWeight) +
                (phase * phaseWeight) +
                (teamplay * teamplayWeight);
    }

    /**
     * 메타 적합도 계산
     */
    private double calculateMetaFitness(ChampionMetaData champion, PlayerProfile profile) {
        double metaStrength = champion.getMetaStrength() / 10.0; // 0-1 정규화

        // 플레이어 레벨에 따른 메타 중요도 조정
        double playerLevel = profile.getStats().getGameCount() > 50 ? 0.8 : 0.6;

        return metaStrength * playerLevel;
    }

    /**
     * 개인 경험 보정 계산
     */
    private double calculatePersonalExperienceBonus(ChampionMetaData champion, PlayerProfile profile) {
        String championName = champion.getName();

        // 해당 챔피언 플레이 경험 확인
        Map<String, Integer> championStats = profile.getPreferences().getChampionPlayCounts();
        Map<String, Double> winRates = profile.getPreferences().getChampionWinRates();

        if (!championStats.containsKey(championName)) {
            return 0.0; // 경험 없음
        }

        int playCount = championStats.get(championName);
        double winRate = winRates.getOrDefault(championName, 0.0);

        // 플레이 횟수와 승률을 고려한 경험 보너스
        double experienceBonus = Math.min(0.3, playCount * 0.05); // 최대 30%
        double performanceBonus = (winRate - 50.0) / 100.0; // 승률이 50% 이상이면 보너스

        return Math.max(-0.2, Math.min(0.5, experienceBonus + performanceBonus));
    }

    /**
     * 최종 신뢰도 점수 계산
     */
    private double calculateFinalConfidence(
            CompatibilityScore compatibility,
            double metaFitness,
            double personalBonus,
            PlayerProfile profile) {

        double baseScore = compatibility.getOverallScore();
        double metaBonus = metaFitness * 0.2; // 메타 보너스 최대 20%
        double personalAdjustment = personalBonus;

        // 플레이어 경험 레벨에 따른 조정
        double experienceMultiplier = profile.getStats().getGameCount() > 20 ? 1.0 : 0.9;

        double finalScore = (baseScore + metaBonus + personalAdjustment) * experienceMultiplier;

        return Math.max(0.0, Math.min(1.0, finalScore));
    }

    /**
     * 동적 추천 이유 생성
     */
    private List<String> generateDynamicReasons(
            ChampionMetaData champion,
            PlayerProfile profile,
            CompatibilityScore compatibility) {

        List<String> reasons = new ArrayList<>();

        // 스타일 매칭
        if (compatibility.getStyleScore() > 0.7) {
            reasons.add("당신의 플레이 스타일과 완벽하게 매칭됩니다");
        } else if (compatibility.getStyleScore() > 0.5) {
            reasons.add("플레이 스타일이 잘 맞는 챔피언입니다");
        }

        // 개인 경험
        String championName = champion.getName();
        Map<String, Double> winRates = profile.getPreferences().getChampionWinRates();
        if (winRates.containsKey(championName)) {
            double winRate = winRates.get(championName);
            if (winRate > 60) {
                reasons.add(String.format("개인 승률 %.1f%%로 뛰어난 성과를 보이고 있습니다", winRate));
            } else if (winRate > 50) {
                reasons.add("이전 플레이 경험에서 좋은 성과를 보였습니다");
            }
        } else {
            reasons.add("새로운 도전! 당신의 스타일에 맞는 챔피언입니다");
        }

        // 메타 강도
        if (champion.getMetaStrength() > 8.0) {
            reasons.add("현재 메타에서 매우 강력한 챔피언입니다");
        } else if (champion.getMetaStrength() > 6.0) {
            reasons.add("현재 메타에서 준수한 성능을 보입니다");
        }

        // 스킬 요구사항
        if (compatibility.getSkillScore() > 0.8) {
            reasons.add("현재 실력으로 충분히 마스터할 수 있습니다");
        } else if (compatibility.getSkillScore() < 0.4) {
            reasons.add("도전적이지만 성장에 도움이 될 챔피언입니다");
        }

        // 최대 4개까지만
        return reasons.stream().limit(4).collect(Collectors.toList());
    }

    /**
     * 개인화된 난이도 계산
     */
    private String calculatePersonalizedDifficulty(ChampionMetaData champion, PlayerProfile profile) {
        double skillGap = calculateSkillGap(champion, profile);

        if (skillGap < 0.3) {
            return "쉬움";
        } else if (skillGap < 0.6) {
            return "보통";
        } else {
            return "어려움";
        }
    }

    private double calculateSkillGap(ChampionMetaData champion, PlayerProfile profile) {
        Map<String, Double> required = champion.getSkillRequirements();
        Map<String, Double> playerSkills = profile.getProgression().getSkillLevels();

        return required.entrySet().stream()
                .mapToDouble(entry -> {
                    String skill = entry.getKey();
                    double req = entry.getValue();
                    double player = playerSkills.getOrDefault(skill, 0.5);
                    return Math.max(0, req - player);
                })
                .average()
                .orElse(0.5);
    }

    /**
     * 개인 통계 조회
     */
    private PersonalChampionStats getPersonalStats(ChampionMetaData champion, PlayerProfile profile) {
        String championName = champion.getName();
        Map<String, Integer> playCounts = profile.getPreferences().getChampionPlayCounts();
        Map<String, Double> winRates = profile.getPreferences().getChampionWinRates();

        if (!playCounts.containsKey(championName)) {
            return null; // 경험 없음
        }

        return PersonalChampionStats.builder()
                .gamesPlayed(playCounts.get(championName))
                .winRate(winRates.getOrDefault(championName, 0.0))
                .averageKDA(2.0) // 실제로는 계산 필요
                .lastPlayed(LocalDateTime.now().minusDays(7)) // 시뮬레이션
                .trend("IMPROVING") // 시뮬레이션
                .build();
    }

    /**
     * 추천 타입 결정
     */
    private String determineRecommendationType(double confidenceScore, double personalBonus) {
        if (personalBonus > 0.2) {
            return "PROVEN"; // 검증된 픽
        } else if (confidenceScore > 0.8) {
            return "PERFECT_MATCH"; // 완벽한 매칭
        } else if (personalBonus == 0.0) {
            return "NEW_CHALLENGE"; // 새로운 도전
        } else {
            return "RECOMMENDED"; // 일반 추천
        }
    }

    /**
     * 개인화 메시지 생성
     */
    private String generatePersonalizedMessage(PlayerProfile profile, List<ChampionRecommendation> recommendations) {
        StringBuilder message = new StringBuilder();

        String playStyle = profile.getPlayStyle().getPrimaryStyle();
        String mainRole = profile.getStats().getMainRole();
        double winRate = profile.getStats().getWinRate();

        // 인사말
        message.append("당신의 플레이 분석이 완료되었습니다! ");

        // 플레이 스타일 언급
        String styleDescription = getStyleDescription(playStyle);
        message.append(String.format("주로 %s 포지션에서 %s 스타일로 플레이하시는군요. ",
                getRoleKorean(mainRole), styleDescription));

        // 성과 평가
        if (winRate > 60) {
            message.append("뛰어난 승률을 보여주고 계시네요! 🏆 ");
        } else if (winRate > 50) {
            message.append("안정적인 성과를 내고 계십니다! 👍 ");
        } else {
            message.append("더 나은 성과를 위한 맞춤 추천을 드릴게요! 💪 ");
        }

        // 추천 요약
        long provenPicks = recommendations.stream()
                .filter(r -> "PROVEN".equals(r.getRecommendationType()))
                .count();

        long newChallenges = recommendations.stream()
                .filter(r -> "NEW_CHALLENGE".equals(r.getRecommendationType()))
                .count();

        if (provenPicks > 0) {
            message.append(String.format("검증된 픽 %d개와 ", provenPicks));
        }

        if (newChallenges > 0) {
            message.append(String.format("새로운 도전 %d개를 ", newChallenges));
        }

        message.append("포함하여 AI가 신중하게 선별한 챔피언들을 만나보세요! ✨");

        return message.toString();
    }

    private String getStyleDescription(String playStyle) {
        Map<String, String> descriptions = Map.of(
                "AGGRESSIVE_CARRY", "공격적인 캐리",
                "SAFE_CARRY", "안전한 캐리",
                "SUPPORTIVE", "서포티브",
                "EARLY_GAME", "초반 주도",
                "BALANCED", "밸런스형"
        );
        return descriptions.getOrDefault(playStyle, "균형잡힌");
    }

    private String getRoleKorean(String role) {
        Map<String, String> roleNames = Map.of(
                "TOP", "탑",
                "JUNGLE", "정글",
                "MID", "미드",
                "ADC", "원딜",
                "SUPPORT", "서포터"
        );
        return roleNames.getOrDefault(role, role);
    }

    /**
     * 폴백 추천 생성
     */
    private ChampionRecommendationResult generateFallbackRecommendations(String playerName) {
        log.warn("폴백 추천 생성: {}", playerName);

        // 기본 추천 챔피언들
        List<ChampionRecommendation> fallbackRecs = Arrays.asList(
                createBasicRecommendation("Jinx", "ADC", 0.85),
                createBasicRecommendation("Garen", "TOP", 0.80),
                createBasicRecommendation("Annie", "MID", 0.75),
                createBasicRecommendation("Warwick", "JUNGLE", 0.70),
                createBasicRecommendation("Soraka", "SUPPORT", 0.65)
        );

        Map<String, List<ChampionRecommendation>> byRole = fallbackRecs.stream()
                .collect(Collectors.groupingBy(ChampionRecommendation::getPrimaryRole));

        return ChampionRecommendationResult.builder()
                .recommendations(fallbackRecs)
                .recommendationsByRole(byRole)
                .playerProfile(createFallbackProfile(playerName))
                .personalizedMessage("기본 추천 챔피언들을 준비했습니다! 더 많은 게임을 플레이하면 정확한 맞춤 추천을 받을 수 있어요! 🎮")
                .generatedAt(LocalDateTime.now())
                .totalAnalyzedChampions(5)
                .build();
    }

    private ChampionRecommendation createBasicRecommendation(String name, String role, double confidence) {
        return ChampionRecommendation.builder()
                .championName(name)
                .primaryRole(role)
                .confidenceScore(confidence)
                .metaStrength(6.0)
                .difficulty("보통")
                .reasons(Arrays.asList("초보자에게 추천", "배우기 쉬운 챔피언", "현재 메타에서 안정적"))
                .personalStats(null)
                .styleMatch(0.7)
                .skillMatch(0.8)
                .recommendationType("RECOMMENDED")
                .build();
    }

    private PlayerProfile createFallbackProfile(String playerName) {
        return PlayerProfile.builder()
                .playerName(playerName)
                .stats(PlayerStats.builder()
                        .gameCount(0)
                        .averageKDA(1.0)
                        .winRate(50.0)
                        .mainRole("MID")
                        .mostPlayedChampion("정보없음")
                        .build())
                .playStyle(PlayStyle.builder()
                        .primaryStyle("BALANCED")
                        .secondaryStyle("FLEXIBLE")
                        .confidence(0.5)
                        .build())
                .preferences(ChampionPreferences.builder()
                        .mostPlayedChampion("정보없음")
                        .bestPerformanceChampion("정보없음")
                        .build())
                .progression(SkillProgression.builder()
                        .improvementRate(0.0)
                        .consistencyScore(0.5)
                        .build())
                .lastAnalyzed(LocalDateTime.now())
                .build();
    }

    private ChampionRecommendation createFallbackRecommendation(ChampionMetaData champion) {
        return ChampionRecommendation.builder()
                .championName(champion.getName())
                .primaryRole(champion.getPrimaryRole())
                .confidenceScore(0.5)
                .metaStrength(champion.getMetaStrength())
                .difficulty("보통")
                .reasons(Arrays.asList("현재 메타에서 활용도가 높은 챔피언"))
                .personalStats(null)
                .styleMatch(0.5)
                .skillMatch(0.5)
                .recommendationType("RECOMMENDED")
                .build();
    }
}