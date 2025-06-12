package com.lol.lol.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

@Data
@Builder
public class ChampionMetaData {
    private String name;
    private String primaryRole;
    private Set<String> alternativeRoles;
    private double metaStrength;              // 현재 메타에서의 강도 (동적 계산)
    private double[] playStyleVector;         // 플레이 스타일 벡터 [공격성, 기동성, 생존력, 유틸리티, 복잡성]
    private Map<String, Double> skillRequirements; // 스킬 요구사항
    private Map<String, Double> gamePhaseStrengths; // 게임 페이즈별 강도
    private double teamplayRequirement;       // 팀플레이 의존도
    private LocalDateTime lastUpdated;
}