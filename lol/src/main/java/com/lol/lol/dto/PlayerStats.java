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
    private double aggressionIndex;
    private double visionScore;
    private double objectiveControl;
    private double positioningScore;
    private String mainRole;
    private String mostPlayedChampion;
    private Map<String, Integer> roleDistribution;
    private Map<String, Integer> championStats;
}