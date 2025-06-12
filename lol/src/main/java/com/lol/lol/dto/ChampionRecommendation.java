package com.lol.lol.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class ChampionRecommendation {
    private String championName;
    private String primaryRole;
    private double confidenceScore;
    private double metaStrength;
    private String difficulty;
    private List<String> reasons;
    private Object personalStats; // 일단 Object로
    private double styleMatch;
    private double skillMatch;
    private String recommendationType;
}