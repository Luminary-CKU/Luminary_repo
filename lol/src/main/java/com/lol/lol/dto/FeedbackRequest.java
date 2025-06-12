package com.lol.lol.dto;

import lombok.Data;

@Data
public class FeedbackRequest {
    private String puuid;
    private String championName;
    private FeedbackType feedbackType;      // LIKE, DISLIKE, PLAYED, AVOIDED
    private GameResult gameResult;          // WON, LOST, NOT_PLAYED
}