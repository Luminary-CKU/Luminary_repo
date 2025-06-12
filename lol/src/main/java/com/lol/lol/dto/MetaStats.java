package com.lol.lol.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MetaStats {
    private String championName;
    private double winRate;
    private double pickRate;
    private double banRate;
    private String tier;                    // S, A, B, C, D
    private double averageKDA;
}