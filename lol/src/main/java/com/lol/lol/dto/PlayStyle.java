package com.lol.lol.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PlayStyle {
    private String primaryStyle;     // AGGRESSIVE_CARRY, SAFE_CARRY, SUPPORTIVE, EARLY_GAME, BALANCED
    private String secondaryStyle;   // 보조 스타일
    private double[] styleVector;    // 스타일 특성 벡터
    private double confidence;       // 스타일 분류 신뢰도
    private double adaptability;     // 적응력
    private double aggressionLevel;  // 공격성 레벨
    private double teamplayOrientation; // 팀플레이 성향
}