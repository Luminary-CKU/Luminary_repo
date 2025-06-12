package com.lol.lol.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class PlayerProfile {
    private String playerName;
    private PlayerStats stats;
    private PlayStyle playStyle;
    private ChampionPreferences preferences;
    private SkillProgression progression;
    private LocalDateTime lastAnalyzed;
}