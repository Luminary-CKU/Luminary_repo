package com.lol.lol.dto;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)

public class LeagueDto {
    private String leagueId;
    private String queueType;
    private String tier;
    private String rank;
    private String summonerId;
    private String leaguePoints;
    private Integer wins;
    private Integer losses;
    private boolean veteran;
    private boolean inactive;
    private boolean freshBlood;
    private boolean hotStreak;

    public String getLeagueId() {
        return leagueId;
    }


    public String getQueueType() {
        return queueType;
    }


    public String getTier() {
        return tier;
    }

    public String getRank() {
        return rank;
    }

    public String getSummonerId() {
        return summonerId;
    }


    public String getLeaguePoints() {
        return leaguePoints;
    }


    public Integer getWins() {
        return wins;
    }


    public Integer getLosses() {
        return losses;
    }

    public boolean isVeteran() {
        return veteran;
    }

    public boolean isFreshBlood() {
        return freshBlood;
    }

    public boolean isHotStreak() {
        return hotStreak;
    }

    public boolean isInactive() {
        return inactive;
    }

    public void setLeagueId(String leagueId) {
        this.leagueId = leagueId;
    }


    public void setQueueType(String queueType) {
        this.queueType = queueType;
    }


    public void setTier(String tier) {
        this.tier = tier;
    }


    public void setRank(String rank) {
        this.rank = rank;
    }

    public void setSummonerId(String summonerId) {
        this.summonerId = summonerId;
    }


    public void setLeaguePoints(String leaguePoints) {
        this.leaguePoints = leaguePoints;
    }


    public void setWins(Integer wins) {
        this.wins = wins;
    }


    public void setLosses(Integer losses) {
        this.losses = losses;
    }

    public void setVeteran(boolean veteran) {
        this.veteran = veteran;
    }

    public void setInactive(boolean inactive) {
        this.inactive = inactive;
    }

    public void setHotStreak(boolean hotStreak) {
        this.hotStreak = hotStreak;
    }

    public void setFreshBlood(boolean freshBlood) {
        this.freshBlood = freshBlood;
    }
}

