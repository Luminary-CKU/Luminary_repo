package com.lol.lol.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class ChampionRecommendation {
    private String championName;
    private String primaryRole;
    private double confidenceScore;          // AI 기반 신뢰도 (0~1)
    private double metaStrength;            // 현재 메타 강도 (1~10)
    private String difficulty;              // 개인화된 난이도 (쉬움/보통/어려움)
    private List<String> reasons;           // 동적 생성된 추천 이유
    private PersonalChampionStats personalStats; // 개인 통계 (있는 경우)
    private double styleMatch;              // 스타일 매칭도 (0~1)
    private double skillMatch;              // 스킬 매칭도 (0~1)
    private String recommendationType;      // PROVEN, PERFECT_MATCH, NEW_CHALLENGE, RECOMMENDED
}
