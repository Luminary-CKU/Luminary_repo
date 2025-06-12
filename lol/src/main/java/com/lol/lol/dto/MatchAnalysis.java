package com.lol.lol.dto;

import com.lol.lol.dto.MatchDto;
import com.lol.lol.dto.ParticipantDto;

public class MatchAnalysis {
    private String matchId;
    private String performance; // "Excellent", "Good", "Average", "Poor"
    private double winRate;
    private int kills;
    private int deaths;
    private int assists;
    private double kda;
    private String championName;
    private String emotion; // "Happy", "Frustrated", "Neutral", "Confident"
    private double confidence;
    private String recommendation;
    private boolean isWin;
    private long gameDuration;
    private int totalDamage;
    private int goldEarned;
    private String gameMode;

    // 기본 생성자
    public MatchAnalysis() {}

    public MatchAnalysis(String matchId) {
        this.matchId = matchId;
        this.performance = "Average";
        this.winRate = 0.5;
        this.emotion = "Neutral";
        this.confidence = 0.7;
    }

    // 정적 분석 메서드 - 실제 매치 데이터 분석
    public static MatchAnalysis analyzeMatch(String matchId) {
        // 실제 구현에서는 MatchDto와 ParticipantDto를 사용해 분석
        MatchAnalysis analysis = new MatchAnalysis();
        analysis.setMatchId(matchId);

        // 기본값 설정 (실제로는 매치 데이터를 바탕으로 계산)
        analysis.setPerformance(calculatePerformance(5, 3, 7)); // 임시 KDA
        analysis.setKills(5);
        analysis.setDeaths(3);
        analysis.setAssists(7);
        analysis.setKda(calculateKDA(5, 3, 7));
        analysis.setChampionName("Unknown");
        analysis.setEmotion(determineEmotion(analysis.getKda(), true));
        analysis.setConfidence(0.8);
        analysis.setRecommendation(generateRecommendation(analysis));
        analysis.setWin(true);
        analysis.setGameDuration(1800); // 30분
        analysis.setTotalDamage(25000);
        analysis.setGoldEarned(12000);
        analysis.setGameMode("CLASSIC");

        return analysis;
    }

    // KDA 계산
    private static double calculateKDA(int kills, int deaths, int assists) {
        if (deaths == 0) {
            return (kills + assists);
        }
        return (double)(kills + assists) / deaths;
    }

    // 퍼포먼스 평가
    private static String calculatePerformance(int kills, int deaths, int assists) {
        double kda = calculateKDA(kills, deaths, assists);
        if (kda >= 3.0) return "Excellent";
        if (kda >= 2.0) return "Good";
        if (kda >= 1.0) return "Average";
        return "Poor";
    }

    // 감정 분석
    private static String determineEmotion(double kda, boolean isWin) {
        if (isWin && kda >= 2.0) return "Happy";
        if (isWin && kda >= 1.0) return "Confident";
        if (!isWin && kda < 1.0) return "Frustrated";
        return "Neutral";
    }

    // 추천 생성
    private static String generateRecommendation(MatchAnalysis analysis) {
        if ("Excellent".equals(analysis.getPerformance())) {
            return "훌륭한 플레이였습니다! 이 챔피언으로 계속 플레이해보세요.";
        } else if ("Poor".equals(analysis.getPerformance())) {
            return "연습이 필요합니다. 더 안전한 플레이를 시도해보세요.";
        }
        return "꾸준한 성장을 보이고 있습니다. 계속해서 발전해나가세요!";
    }

    // Getters and Setters
    public String getMatchId() {
        return matchId;
    }

    public void setMatchId(String matchId) {
        this.matchId = matchId;
    }

    public String getPerformance() {
        return performance;
    }

    public void setPerformance(String performance) {
        this.performance = performance;
    }

    public double getWinRate() {
        return winRate;
    }

    public void setWinRate(double winRate) {
        this.winRate = winRate;
    }

    public int getKills() {
        return kills;
    }

    public void setKills(int kills) {
        this.kills = kills;
    }

    public int getDeaths() {
        return deaths;
    }

    public void setDeaths(int deaths) {
        this.deaths = deaths;
    }

    public int getAssists() {
        return assists;
    }

    public void setAssists(int assists) {
        this.assists = assists;
    }

    public double getKda() {
        return kda;
    }

    public void setKda(double kda) {
        this.kda = kda;
    }

    public String getChampionName() {
        return championName;
    }

    public void setChampionName(String championName) {
        this.championName = championName;
    }

    public String getEmotion() {
        return emotion;
    }

    public void setEmotion(String emotion) {
        this.emotion = emotion;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    public String getRecommendation() {
        return recommendation;
    }

    public void setRecommendation(String recommendation) {
        this.recommendation = recommendation;
    }

    public boolean isWin() {
        return isWin;
    }

    public void setWin(boolean win) {
        isWin = win;
    }

    public long getGameDuration() {
        return gameDuration;
    }

    public void setGameDuration(long gameDuration) {
        this.gameDuration = gameDuration;
    }

    public int getTotalDamage() {
        return totalDamage;
    }

    public void setTotalDamage(int totalDamage) {
        this.totalDamage = totalDamage;
    }

    public int getGoldEarned() {
        return goldEarned;
    }

    public void setGoldEarned(int goldEarned) {
        this.goldEarned = goldEarned;
    }

    public String getGameMode() {
        return gameMode;
    }

    public void setGameMode(String gameMode) {
        this.gameMode = gameMode;
    }
}