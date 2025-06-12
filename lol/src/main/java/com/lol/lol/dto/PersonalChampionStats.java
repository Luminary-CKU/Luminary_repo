package com.lol.lol.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class PersonalChampionStats {
    private int gamesPlayed;
    private double winRate;
    private double averageKDA;
    private LocalDateTime lastPlayed;
    private String trend;                   // IMPROVING, DECLINING, STABLE
}