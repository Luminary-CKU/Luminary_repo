package com.lol.lol.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PlayerKDA {
    private String matchId;
    private int kills;
    private int deaths;
    private int assists;
    private List<Integer> items;  // 사용한 아이템 (아이템 ID 리스트)
//    private int wardsPlaced;  // 설치한 와드 개수
//    private int wardsKilled;  // 제거한 와드 개수
    private String spell1Id;  // 소환사 주문 1
    private String spell2Id;  // 소환사 주문 2

    public PlayerKDA(String matchId, int kills, int deaths, int assists,
                     List<Integer> items, int wardsPlaced, int wardsKilled,
                     String spell1Id, String spell2Id) {
        this.matchId = matchId;
        this.kills = kills;
        this.deaths = deaths;
        this.assists = assists;
        this.items = items;
//        this.wardsPlaced = wardsPlaced;
//        this.wardsKilled = wardsKilled;
        this.spell1Id = spell1Id;
        this.spell2Id = spell2Id;
    }


    public int getKills() {
        return kills;
    }

    public int getDeaths() {
        return deaths;
    }

    public int getAssists() {
        return assists;
    }

//    public int getWardsKilled() {
//        return wardsKilled;
//    }
//
//    public int getWardsPlaced() {
//        return wardsPlaced;
//    }

    public List<Integer> getItems() {
        return items;
    }

    public String getMatchId() {
        return matchId;
    }

    public String getSpell1Id() {
        return spell1Id;
    }

    public String getSpell2Id() {
        return spell2Id;
    }
}
