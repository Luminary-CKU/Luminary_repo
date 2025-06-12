package com.lol.lol.dto;

import lombok.Builder;
import lombok.Data;
import java.util.Map;
import java.util.Set;

@Data
@Builder
public class ChampionPreferences {
    private String mostPlayedChampion;
    private String bestPerformanceChampion;
    private Map<String, Integer> championPlayCounts;   // 챔피언별 플레이 횟수
    private Map<String, Double> championWinRates;      // 챔피언별 승률
    private Set<String> preferredRoles;                // 선호 라인들
}
