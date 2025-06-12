package com.lol.lol.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class ChampionRecommendationResult {
    private List<ChampionRecommendation> recommendations;
    private Map<String, List<ChampionRecommendation>> recommendationsByRole;
    private PlayerProfile playerProfile;
    private String personalizedMessage;
    private LocalDateTime generatedAt;
    private int totalAnalyzedChampions;
}