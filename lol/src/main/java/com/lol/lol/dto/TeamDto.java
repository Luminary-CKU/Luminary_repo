package com.lol.lol.dto;

import java.util.List;

public class TeamDto {
    private List<BanDto> bans;
    private ObjectivesDto objectives;
    private int teamId;
    private boolean win;

    // 기본 생성자
    public TeamDto() {}

    // Getters and Setters
    public List<BanDto> getBans() {
        return bans;
    }

    public void setBans(List<BanDto> bans) {
        this.bans = bans;
    }

    public ObjectivesDto getObjectives() {
        return objectives;
    }

    public void setObjectives(ObjectivesDto objectives) {
        this.objectives = objectives;
    }

    public int getTeamId() {
        return teamId;
    }

    public void setTeamId(int teamId) {
        this.teamId = teamId;
    }

    public boolean isWin() {
        return win;
    }

    public void setWin(boolean win) {
        this.win = win;
    }

    // 밴 정보 클래스
    public static class BanDto {
        private int championId;
        private int pickTurn;

        public int getChampionId() {
            return championId;
        }

        public void setChampionId(int championId) {
            this.championId = championId;
        }

        public int getPickTurn() {
            return pickTurn;
        }

        public void setPickTurn(int pickTurn) {
            this.pickTurn = pickTurn;
        }
    }

    // 오브젝트 정보 클래스
    public static class ObjectivesDto {
        private ObjectiveDto baron;
        private ObjectiveDto champion;
        private ObjectiveDto dragon;
        private ObjectiveDto inhibitor;
        private ObjectiveDto riftHerald;
        private ObjectiveDto tower;

        public ObjectiveDto getBaron() {
            return baron;
        }

        public void setBaron(ObjectiveDto baron) {
            this.baron = baron;
        }

        public ObjectiveDto getChampion() {
            return champion;
        }

        public void setChampion(ObjectiveDto champion) {
            this.champion = champion;
        }

        public ObjectiveDto getDragon() {
            return dragon;
        }

        public void setDragon(ObjectiveDto dragon) {
            this.dragon = dragon;
        }

        public ObjectiveDto getInhibitor() {
            return inhibitor;
        }

        public void setInhibitor(ObjectiveDto inhibitor) {
            this.inhibitor = inhibitor;
        }

        public ObjectiveDto getRiftHerald() {
            return riftHerald;
        }

        public void setRiftHerald(ObjectiveDto riftHerald) {
            this.riftHerald = riftHerald;
        }

        public ObjectiveDto getTower() {
            return tower;
        }

        public void setTower(ObjectiveDto tower) {
            this.tower = tower;
        }
    }

    // 개별 오브젝트 클래스
    public static class ObjectiveDto {
        private boolean first;
        private int kills;

        public boolean isFirst() {
            return first;
        }

        public void setFirst(boolean first) {
            this.first = first;
        }

        public int getKills() {
            return kills;
        }

        public void setKills(int kills) {
            this.kills = kills;
        }
    }
}