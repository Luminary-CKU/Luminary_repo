package com.lol.lol.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccountrDto {
    private String accountId;
    private int profileIconId;
    private long revisionDate;
    private String gameName;
    private String id;
    private String puuid;
    private long summonerLevel;
    private String profileImg;


    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getAccountId() {
        return accountId;
    }


    public void setProfileIconId(int profileIconId) {
        this.profileIconId = profileIconId;
    }

    public int getProfileIconId() {
        return profileIconId;
    }

    public void setRevisionDate(long revisionDate) {
        this.revisionDate = revisionDate;
    }

    public long getRevisionDate() {
        return revisionDate;
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }

    public String getGameName() {
        return gameName;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setPuuid(String puuid) {
        this.puuid = puuid;
    }

    public String getPuuid() {
        return puuid;
    }

    public void setSummonerLevel(long summonerLevel) {
        this.summonerLevel = summonerLevel;
    }

    public long getSummonerLevel() {
        return summonerLevel;
    }

    public void setProfileImg(String profileImg) {
        this.profileImg = profileImg;
    }

    public String getProfileImg() {
        return profileImg;
    }
}


