package com.lol.lol.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PlayStyle {
    private String primaryStyle;
    private String secondaryStyle;
    private double[] styleVector;
    private double confidence;
    private double adaptability;
    private double aggressionLevel;
    private double teamplayOrientation;
}