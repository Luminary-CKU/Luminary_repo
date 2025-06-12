package com.lol.lol.dto;

import lombok.Builder;
import lombok.Data;
import java.util.Map;

@Data
@Builder
public class SkillProgression {
    private double improvementRate;
    private Map<String, Double> skillLevels;
    private double learningCurve;
    private double consistencyScore;
}