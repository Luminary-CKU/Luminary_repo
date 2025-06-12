package com.lol.lol.dto;

import lombok.Builder;
import lombok.Data;
import java.util.Map;

@Data
@Builder
public class PlayerStats {
    private int gameCount;
    private double averageKDA;
    private double winRate;
    private double killParticipation;
    private double earlyGamePerformance;
    private double lateGamePerformance;
    private double aggressionIndex;          // 공격성 지수
    private double visionScore;              // 평균 비전 점수
    private double objectiveControl;         // 오브젝트 관여도
    private double positioningScore;         // 포지셔닝 점수
    private String mainRole;
    private String mostPlayedChampion;
    private Map<String, Integer> roleDistribution;    // 라인별 플레이 횟수
    private Map<String, Integer> championStats;       // 챔피언별 플레이 횟수
}