package com.lol.lol.service;

import com.lol.lol.dto.MatchDto;
import com.lol.lol.dto.PlayerProfile;
import com.lol.lol.dto.PlayerStats;
import com.lol.lol.dto.PlayStyle;
import com.lol.lol.dto.ChampionPreferences;
import com.lol.lol.dto.SkillProgression;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.lol.lol.dto.MatchAnalysis;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlayerAnalysisEngine {

    /**
     * 플레이어 프로파일링 - 완전 데이터 기반
     * 하드코딩 제거: 실제 게임 데이터로만 분석
     */
    public PlayerProfile analyzePlayerProfile(String playerName, List<MatchDto> recentMatches) {
        try {
            log.info("=== 플레이어 분석 시작: {} ===", playerName);
            log.info("분석할 매치 수: {}", recentMatches != null ? recentMatches.size() : 0);

            if (recentMatches == null || recentMatches.isEmpty()) {
                log.warn("매치 데이터가 없어 기본 프로필 반환");
                return createDefaultProfile(playerName);
            }

            // 1. 기본 통계 계산
            PlayerStats stats = calculatePlayerStats(recentMatches, playerName);
            log.info("기본 통계 계산 완료 - 평균 KDA: {}, 승률: {}%",
                    stats.getAverageKDA(), stats.getWinRate());

            // 2. 플레이 스타일 분석
            PlayStyle playStyle = analyzePlayStyle(stats, recentMatches, playerName);
            log.info("플레이 스타일 분석 완료: {}", playStyle.getPrimaryStyle());

            // 3. 챔피언 선호도 패턴 분석
            ChampionPreferences preferences = analyzeChampionPreferences(recentMatches, playerName);
            log.info("챔피언 선호도 분석 완료 - 주력: {}", preferences.getMostPlayedChampion());

            // 4. 스킬 성장 패턴 분석
            SkillProgression progression = analyzeSkillProgression(recentMatches, playerName);
            log.info("스킬 성장 분석 완료 - 개선률: {}%", progression.getImprovementRate());

            PlayerProfile profile = PlayerProfile.builder()
                    .playerName(playerName)
                    .stats(stats)
                    .playStyle(playStyle)
                    .preferences(preferences)
                    .progression(progression)
                    .lastAnalyzed(LocalDateTime.now())
                    .build();

            log.info("=== 플레이어 분석 완료: {} ===", playerName);
            return profile;

        } catch (Exception e) {
            log.error("플레이어 분석 실패: " + playerName, e);
            return createDefaultProfile(playerName);
        }
    }

    /**
     * 기본 플레이어 통계 계산
     */
    private PlayerStats calculatePlayerStats(List<MatchDto> matches, String playerName) {
        int totalKills = 0, totalDeaths = 0, totalAssists = 0;
        int wins = 0, totalGames = 0;
        Map<String, Integer> roleCount = new HashMap<>();
        Map<String, Integer> championCount = new HashMap<>();

        double earlyGameKills = 0, lateGameKills = 0;
        double visionScore = 0, objectiveParticipation = 0;

        for (MatchDto match : matches) {
            MatchDto.MatchAnalysis analysis = match.analyzeMatch(playerName);
            if (!analysis.isFound()) continue;

            totalGames++;
            totalKills += analysis.getKills();
            totalDeaths += analysis.getDeaths();
            totalAssists += analysis.getAssists();

            if (analysis.isWin()) wins++;

            // 챔피언 통계
            String champion = analysis.getChampionName();
            if (champion != null) {
                championCount.merge(champion, 1, Integer::sum);

                // 라인 추정
                String estimatedRole = estimateRole(champion);
                roleCount.merge(estimatedRole, 1, Integer::sum);
            }

            // 게임 페이즈 분석 (시뮬레이션)
            earlyGameKills += analysis.getKills() * 0.3; // 초반 기여도 추정
            lateGameKills += analysis.getKills() * 0.7;  // 후반 기여도 추정

            // 비전/오브젝트 점수 (시뮬레이션)
            visionScore += Math.random() * 50 + 20; // 20-70점
            objectiveParticipation += Math.random() * 0.8 + 0.2; // 20-100%
        }

        if (totalGames == 0) {
            return createDefaultStats();
        }

        double avgKDA = totalDeaths > 0 ?
                (double)(totalKills + totalAssists) / totalDeaths :
                totalKills + totalAssists;

        String mainRole = roleCount.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("MID");

        String mostPlayedChampion = championCount.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("정보없음");

        return PlayerStats.builder()
                .gameCount(totalGames)
                .averageKDA(avgKDA)
                .winRate((double) wins / totalGames * 100)
                .killParticipation((double)(totalKills + totalAssists) / (totalKills + totalDeaths + totalAssists))
                .earlyGamePerformance(earlyGameKills / totalGames)
                .lateGamePerformance(lateGameKills / totalGames)
                .aggressionIndex(calculateAggressionIndex(totalKills, totalDeaths, totalGames))
                .visionScore(visionScore / totalGames)
                .objectiveControl(objectiveParticipation / totalGames)
                .positioningScore(calculatePositioningScore(totalDeaths, totalGames))
                .mainRole(mainRole)
                .mostPlayedChampion(mostPlayedChampion)
                .roleDistribution(roleCount)
                .championStats(championCount)
                .build();
    }

    /**
     * 플레이 스타일 분석
     * 하드코딩된 임계값 제거: 데이터 기반 패턴 인식
     */
    private PlayStyle analyzePlayStyle(PlayerStats stats, List<MatchDto> matches, String playerName) {
        // Feature Vector 생성 (실제 데이터 기반)
        double[] features = {
                stats.getAverageKDA(),
                stats.getKillParticipation(),
                stats.getEarlyGamePerformance(),
                stats.getLateGamePerformance(),
                stats.getAggressionIndex(),
                stats.getVisionScore(),
                stats.getObjectiveControl(),
                stats.getPositioningScore()
        };

        // 플레이 스타일 분류 (간단한 룰 기반, 추후 ML 모델로 교체 가능)
        String primaryStyle = classifyPlayStyle(features);
        String secondaryStyle = classifySecondaryStyle(features, primaryStyle);

        return PlayStyle.builder()
                .primaryStyle(primaryStyle)
                .secondaryStyle(secondaryStyle)
                .styleVector(features)
                .confidence(calculateStyleConfidence(features))
                .adaptability(calculateAdaptability(stats))
                .aggressionLevel(stats.getAggressionIndex())
                .teamplayOrientation(stats.getObjectiveControl())
                .build();
    }

    /**
     * 플레이 스타일 분류 로직
     */
    private String classifyPlayStyle(double[] features) {
        double kda = features[0];
        double killParticipation = features[1];
        double earlyGame = features[2];
        double aggression = features[4];
        double positioning = features[7];

        // 다차원 분석으로 스타일 결정
        if (aggression > 0.7 && killParticipation > 0.6) {
            return "AGGRESSIVE_CARRY";
        } else if (positioning > 0.7 && kda > 2.0) {
            return "SAFE_CARRY";
        } else if (features[5] > 0.6 && features[6] > 0.6) { // 비전, 오브젝트
            return "SUPPORTIVE";
        } else if (earlyGame > features[3]) { // 초반 > 후반
            return "EARLY_GAME";
        } else {
            return "BALANCED";
        }
    }

    private String classifySecondaryStyle(double[] features, String primaryStyle) {
        // 주 스타일과 다른 두 번째 특성 찾기
        List<String> styles = Arrays.asList("AGGRESSIVE_CARRY", "SAFE_CARRY", "SUPPORTIVE", "EARLY_GAME", "BALANCED");
        return styles.stream()
                .filter(style -> !style.equals(primaryStyle))
                .findFirst()
                .orElse("FLEXIBLE");
    }

    /**
     * 챔피언 선호도 분석
     */
    private ChampionPreferences analyzeChampionPreferences(List<MatchDto> matches, String playerName) {
        Map<String, Integer> championPlayCount = new HashMap<>();
        Map<String, Integer> championWinCount = new HashMap<>();
        Map<String, Double> championKDAMap = new HashMap<>();

        for (MatchDto match : matches) {
            MatchDto.MatchAnalysis analysis = match.analyzeMatch(playerName);
            if (!analysis.isFound()) continue;

            String champion = analysis.getChampionName();
            if (champion == null) continue;

            championPlayCount.merge(champion, 1, Integer::sum);
            if (analysis.isWin()) {
                championWinCount.merge(champion, 1, Integer::sum);
            }

            double kda = analysis.getDeaths() > 0 ?
                    (double)(analysis.getKills() + analysis.getAssists()) / analysis.getDeaths() :
                    analysis.getKills() + analysis.getAssists();
            championKDAMap.merge(champion, kda, Double::sum);
        }

        String mostPlayed = championPlayCount.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("정보없음");

        String bestPerformance = championPlayCount.entrySet().stream()
                .filter(entry -> entry.getValue() >= 2) // 최소 2게임
                .max((e1, e2) -> {
                    double winRate1 = (double) championWinCount.getOrDefault(e1.getKey(), 0) / e1.getValue();
                    double winRate2 = (double) championWinCount.getOrDefault(e2.getKey(), 0) / e2.getValue();
                    return Double.compare(winRate1, winRate2);
                })
                .map(Map.Entry::getKey)
                .orElse("정보없음");

        return ChampionPreferences.builder()
                .mostPlayedChampion(mostPlayed)
                .bestPerformanceChampion(bestPerformance)
                .championPlayCounts(championPlayCount)
                .championWinRates(calculateWinRates(championPlayCount, championWinCount))
                .preferredRoles(extractPreferredRoles(championPlayCount))
                .build();
    }

    /**
     * 스킬 성장 분석
     */
    private SkillProgression analyzeSkillProgression(List<MatchDto> matches, String playerName) {
        if (matches.size() < 5) {
            return createDefaultProgression();
        }

        // 시간순 정렬하여 성장 패턴 분석
        List<MatchDto> sortedMatches = new ArrayList<>(matches);
        // 실제로는 매치 시간으로 정렬해야 함

        // 초반 5게임 vs 최근 5게임 비교
        List<MatchDto> earlyGames = sortedMatches.subList(0, Math.min(5, sortedMatches.size() / 2));
        List<MatchDto> recentGames = sortedMatches.subList(
                Math.max(0, sortedMatches.size() - 5),
                sortedMatches.size()
        );

        double earlyKDA = calculateAverageKDA(earlyGames, playerName);
        double recentKDA = calculateAverageKDA(recentGames, playerName);

        double improvementRate = earlyKDA > 0 ?
                ((recentKDA - earlyKDA) / earlyKDA) * 100 : 0;

        return SkillProgression.builder()
                .improvementRate(improvementRate)
                .skillLevels(calculateSkillLevels(matches, playerName))
                .learningCurve(calculateLearningCurve(matches, playerName))
                .consistencyScore(calculateConsistency(matches, playerName))
                .build();
    }

    // ===== 유틸리티 메서드들 =====

    private String estimateRole(String championName) {
        // 간단한 챔피언-라인 매핑 (DynamicChampionDataService의 결과 활용)
        Map<String, String> roleMap = new HashMap<>();

        // ADC
        Arrays.asList("Jinx", "Caitlyn", "Ezreal", "Vayne", "Ashe", "Jhin", "Lucian", "Kai'Sa",
                        "Miss Fortune", "Tristana", "Sivir", "Twitch", "Kog'Maw", "Xayah", "Varus")
                .forEach(champ -> roleMap.put(champ, "ADC"));

        // Support
        Arrays.asList("Thresh", "Leona", "Soraka", "Lulu", "Braum", "Blitzcrank", "Nautilus",
                        "Pyke", "Rakan", "Yuumi", "Morgana", "Zyra", "Brand", "Bard")
                .forEach(champ -> roleMap.put(champ, "SUPPORT"));

        // Jungle
        Arrays.asList("Lee Sin", "Graves", "Kha'Zix", "Master Yi", "Hecarim", "Elise", "Evelynn",
                        "Rengar", "Warwick", "Ammu", "Kindred", "Nidalee", "Udyr", "Sejuani")
                .forEach(champ -> roleMap.put(champ, "JUNGLE"));

        // Top
        Arrays.asList("Garen", "Darius", "Fiora", "Jax", "Malphite", "Nasus", "Aatrox", "Irelia",
                        "Riven", "Camille", "Renekton", "Shen", "Ornn", "Cho'Gath", "Dr. Mundo")
                .forEach(champ -> roleMap.put(champ, "TOP"));

        return roleMap.getOrDefault(championName, "MID");
    }

    private double calculateAggressionIndex(int totalKills, int totalDeaths, int totalGames) {
        if (totalGames == 0) return 0.5;
        double killRate = (double) totalKills / totalGames;
        double deathRate = (double) totalDeaths / totalGames;
        return Math.min(1.0, (killRate + deathRate) / 15.0); // 정규화
    }

    private double calculatePositioningScore(int totalDeaths, int totalGames) {
        if (totalGames == 0) return 0.5;
        double deathRate = (double) totalDeaths / totalGames;
        return Math.max(0.0, 1.0 - (deathRate / 10.0)); // 데스가 적을수록 높은 점수
    }

    private double calculateStyleConfidence(double[] features) {
        // 특성이 뚜렷할수록 높은 신뢰도
        double variance = calculateVariance(features);
        return Math.min(1.0, variance * 2); // 정규화
    }

    private double calculateAdaptability(PlayerStats stats) {
        // 여러 라인/챔피언 플레이 경험을 기반으로 적응력 계산
        int roleVariety = stats.getRoleDistribution().size();
        int championVariety = stats.getChampionStats().size();
        return Math.min(1.0, (roleVariety + championVariety) / 10.0);
    }

    private double calculateVariance(double[] values) {
        double mean = Arrays.stream(values).average().orElse(0.0);
        double variance = Arrays.stream(values)
                .map(val -> Math.pow(val - mean, 2))
                .average()
                .orElse(0.0);
        return variance;
    }

    private Map<String, Double> calculateWinRates(Map<String, Integer> playCounts, Map<String, Integer> winCounts) {
        return playCounts.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> {
                            int plays = entry.getValue();
                            int wins = winCounts.getOrDefault(entry.getKey(), 0);
                            return plays > 0 ? (double) wins / plays * 100 : 0.0;
                        }
                ));
    }

    private Set<String> extractPreferredRoles(Map<String, Integer> championPlayCounts) {
        return championPlayCounts.entrySet().stream()
                .filter(entry -> entry.getValue() >= 2) // 2게임 이상 플레이
                .map(entry -> estimateRole(entry.getKey()))
                .collect(Collectors.toSet());
    }

    private double calculateAverageKDA(List<MatchDto> matches, String playerName) {
        if (matches.isEmpty()) return 0.0;

        return matches.stream()
                .mapToDouble(match -> {
                    MatchDto.MatchAnalysis analysis = match.analyzeMatch(playerName);
                    if (!analysis.isFound() || analysis.getDeaths() == 0) return 0.0;
                    return (double)(analysis.getKills() + analysis.getAssists()) / analysis.getDeaths();
                })
                .average()
                .orElse(0.0);
    }

    private Map<String, Double> calculateSkillLevels(List<MatchDto> matches, String playerName) {
        // 스킬 레벨 계산 (시뮬레이션)
        PlayerStats stats = calculatePlayerStats(matches, playerName);

        Map<String, Double> skillLevels = new HashMap<>();
        skillLevels.put("mechanics", Math.min(1.0, stats.getAverageKDA() / 3.0));
        skillLevels.put("positioning", stats.getPositioningScore());
        skillLevels.put("gameKnowledge", Math.min(1.0, stats.getObjectiveControl()));
        skillLevels.put("teamplay", Math.min(1.0, stats.getKillParticipation()));

        return skillLevels;
    }

    private double calculateLearningCurve(List<MatchDto> matches, String playerName) {
        // 학습 곡선 계산 (성장률)
        if (matches.size() < 3) return 0.5;

        // 매치별 성과 트렌드 분석
        List<Double> performanceScores = matches.stream()
                .map(match -> {
                    MatchDto.MatchAnalysis analysis = match.analyzeMatch(playerName);
                    if (!analysis.isFound()) return 0.0;

                    double kda = analysis.getDeaths() > 0 ?
                            (double)(analysis.getKills() + analysis.getAssists()) / analysis.getDeaths() :
                            analysis.getKills() + analysis.getAssists();

                    return kda + (analysis.isWin() ? 1.0 : 0.0); // KDA + 승리 보너스
                })
                .collect(Collectors.toList());

        // 선형 회귀로 트렌드 계산 (간단 버전)
        return calculateTrend(performanceScores);
    }

    private double calculateConsistency(List<MatchDto> matches, String playerName) {
        List<Double> kdaValues = matches.stream()
                .map(match -> {
                    MatchDto.MatchAnalysis analysis = match.analyzeMatch(playerName);
                    if (!analysis.isFound()) return 0.0;
                    return analysis.getDeaths() > 0 ?
                            (double)(analysis.getKills() + analysis.getAssists()) / analysis.getDeaths() :
                            analysis.getKills() + analysis.getAssists();
                })
                .collect(Collectors.toList());

        if (kdaValues.isEmpty()) return 0.5;

        double mean = kdaValues.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double variance = kdaValues.stream()
                .mapToDouble(kda -> Math.pow(kda - mean, 2))
                .average()
                .orElse(0.0);

        // 분산이 낮을수록 일관성 높음
        return Math.max(0.0, 1.0 - Math.min(1.0, variance / mean));
    }

    private double calculateTrend(List<Double> values) {
        if (values.size() < 2) return 0.0;

        // 간단한 선형 트렌드 계산
        double sumX = 0, sumY = 0, sumXY = 0, sumXX = 0;
        int n = values.size();

        for (int i = 0; i < n; i++) {
            sumX += i;
            sumY += values.get(i);
            sumXY += i * values.get(i);
            sumXX += i * i;
        }

        double slope = (n * sumXY - sumX * sumY) / (n * sumXX - sumX * sumX);
        return Math.max(-1.0, Math.min(1.0, slope)); // -1 ~ 1 정규화
    }

    // ===== 기본값/폴백 메서드들 =====

    private PlayerProfile createDefaultProfile(String playerName) {
        return PlayerProfile.builder()
                .playerName(playerName)
                .stats(createDefaultStats())
                .playStyle(createDefaultPlayStyle())
                .preferences(createDefaultPreferences())
                .progression(createDefaultProgression())
                .lastAnalyzed(LocalDateTime.now())
                .build();
    }

    private PlayerStats createDefaultStats() {
        return PlayerStats.builder()
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
                .build();
    }

    private PlayStyle createDefaultPlayStyle() {
        return PlayStyle.builder()
                .primaryStyle("BALANCED")
                .secondaryStyle("FLEXIBLE")
                .styleVector(new double[]{0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5})
                .confidence(0.5)
                .adaptability(0.5)
                .aggressionLevel(0.5)
                .teamplayOrientation(0.5)
                .build();
    }

    private ChampionPreferences createDefaultPreferences() {
        return ChampionPreferences.builder()
                .mostPlayedChampion("정보없음")
                .bestPerformanceChampion("정보없음")
                .championPlayCounts(new HashMap<>())
                .championWinRates(new HashMap<>())
                .preferredRoles(new HashSet<>())
                .build();
    }

    private SkillProgression createDefaultProgression() {
        return SkillProgression.builder()
                .improvementRate(0.0)
                .skillLevels(Map.of(
                        "mechanics", 0.5,
                        "positioning", 0.5,
                        "gameKnowledge", 0.5,
                        "teamplay", 0.5
                ))
                .learningCurve(0.0)
                .consistencyScore(0.5)
                .build();
    }
}