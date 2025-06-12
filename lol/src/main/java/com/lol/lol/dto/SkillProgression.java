package com.lol.lol.dto;

import lombok.Builder;
import lombok.Data;
import java.util.Map;

@Data
@Builder
public class SkillProgression {
    private double improvementRate;          // 개선율 (%)
    private Map<String, Double> skillLevels; // 스킬별 레벨 (mechanics, positioning, gameKnowledge, teamplay)
    private double learningCurve;            // 학습 곡선 (-1: 하락, 0: 정체, 1: 상승)
    private double consistencyScore;         // 일관성 점수 (0~1)
}