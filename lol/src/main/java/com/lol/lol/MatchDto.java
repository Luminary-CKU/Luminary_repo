package com.lol.lol;

import lombok.ToString;
import lombok.Getter;
import lombok.Setter;
import java.util.List;


@Getter
@Setter
public class MatchDto {
    private MetadataDto metadata;
    private InfoDto info;
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

