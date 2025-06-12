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
    private Map<String, Integer> championPlayCounts;
    private Map<String, Double> championWinRates;
    private Set<String> preferredRoles;
}