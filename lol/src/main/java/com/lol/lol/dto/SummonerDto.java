package com.lol.lol.dto;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SummonerDto {

    private String gameName;
    private String tagLine;
    private String id;
    private String accountId;
    private String puuid;
    private Integer profileIconId;
    private Long revisionDate;
    private Long summonerLevel;

    //    @JsonProperty("profileIconId")
    private String profileImg;

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }

    public void setTagLine(String tagLine) {
        this.tagLine = tagLine;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public void setProfileIconId(Integer profileIconId) {
        this.profileIconId = profileIconId;
    }

    public void setPuuid(String puuid) {
        this.puuid = puuid;
    }

    public void setRevisionDate(Long revisionDate) {
        this.revisionDate = revisionDate;
    }

    public void setSummonerLevel(Long summonerLevel) {
        this.summonerLevel = summonerLevel;
    }

    public Integer getProfileIconId() {
        return profileIconId;
    }

    public Long getRevisionDate() {
        return revisionDate;
    }

    public Long getSummonerLevel() {
        return summonerLevel;
    }

    public String getAccountId() {
        return accountId;
    }

    public String getGameName() {
        return gameName;
    }

    public String getId() {
        return id;
    }

    public String getPuuid() {
        return puuid;
    }

    public String getTagLine() {
        return tagLine;
    }

//    public void setProfileImg(String profileIconId){
//        this.profileImg = "https://ddragon.leagueoflegends.com/cdn/14.16.1/data/ko_KR/profileicon.json"+profileIconId;
//    }
}