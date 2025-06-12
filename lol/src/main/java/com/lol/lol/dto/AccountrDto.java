package com.lol.lol.dto;

public class AccountrDto {
    private String puuid;
    private String gameName;
    private String tagLine;

    // 기본 생성자
    public AccountrDto() {}

    // Getters and Setters
    public String getPuuid() {
        return puuid;
    }

    public void setPuuid(String puuid) {
        this.puuid = puuid;
    }

    public String getGameName() {
        return gameName;
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }

    public String getTagLine() {
        return tagLine;
    }

    public void setTagLine(String tagLine) {
        this.tagLine = tagLine;
    }
}