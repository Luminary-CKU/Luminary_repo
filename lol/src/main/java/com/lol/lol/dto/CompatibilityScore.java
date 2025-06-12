package com.lol.lol.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CompatibilityScore {
    private double styleScore;      // 플레이 스타일 매칭도
    private double skillScore;      // 스킬 요구사항 적합도
    private double phaseScore;      // 게임 페이즈 선호도 매칭
    private double teamplayScore;   // 팀플레이 성향 매칭
    private double overallScore;    // 종합 점수
}