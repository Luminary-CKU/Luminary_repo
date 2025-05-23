package com.lol.lol.dto;

import lombok.ToString;
import lombok.Getter;
import lombok.Setter;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Getter
@Setter
public class MatchDto {
    private MetadataDto metadata;
    private InfoDto info;

    // SearchController에서 사용할 분석 메서드들
    public MatchAnalysis analyzeMatch(String playerName) {
        if (info == null || info.getParticipants() == null) {
            return new MatchAnalysis();
        }

        for (ParticipantDto participant : info.getParticipants()) {
            if (playerName.equals(participant.getRiotIdGameName())) {
                return new MatchAnalysis(
                        participant.getKills(),
                        participant.getDeaths(),
                        participant.getAssists(),
                        participant.isWin(),
                        participant.getChampionName()
                );
            }
        }
        return new MatchAnalysis();
    }

    // 분석 결과를 담는 내부 클래스
    @Getter
    @Setter
    public static class MatchAnalysis {
        private int kills = 0;
        private int deaths = 0;
        private int assists = 0;
        private boolean win = false;
        private String championName = "";
        private boolean found = false;

        public MatchAnalysis() {
            this.found = false;
        }

        public MatchAnalysis(int kills, int deaths, int assists, boolean win, String championName) {
            this.kills = kills;
            this.deaths = deaths;
            this.assists = assists;
            this.win = win;
            this.championName = championName;
            this.found = true;
        }
    }
}

@Getter
@Setter
class MetadataDto {
    private String matchId;
}

@Getter
@Setter
class InfoDto {
    private List<ParticipantDto> participants;
}

@Getter
@Setter
class ParticipantDto {
    private String riotIdGameName;
    private String summonerName;
    private String championName;
    private int kills;
    private int deaths;
    private int assists;
    private boolean win;

    public List<Integer> getItems() {
        Integer item0;
        Integer item1;
        Integer item2;
        Integer item3;
        Integer item4;
        Integer item5;
        Integer item6;
        return List.of();
    }
//
//    public Object getSpell1Id() {
//
//    }
//
//    public Object getSpell2Id() {
//    }
}