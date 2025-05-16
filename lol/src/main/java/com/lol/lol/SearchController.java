package com.lol.lol;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Controller
public class SearchController {

    private final SummonerService SummonerService;
    private final ObjectMapper ObjectMapper;
    private String API_KEY ="RGAPI-5c52c789-49d2-4d3e-bd4b-3723033caef3";

//    public SearchController(SummonerService SummonerService){
//        this.SummonerService = SummonerService;
//    }

    @GetMapping("/")
        String search(Model model){
        return "home.html";
    }
    @PostMapping("/result")

    public String searchName(String  gameName, String tagLine, Model model) throws UnsupportedEncodingException, JsonProcessingException {


        System.out.println("======= 입력값 확인 =======");
        System.out.println("gameName: " + gameName);
        System.out.println("tagLine: " + tagLine);
        System.out.println("==========================");
        System.out.println("gameName: " + gameName + " tagLine: " + tagLine);

        // 최신 버전 가져오기
        String latestVersion = SummonerService.getLatestVersion();
        System.out.println("ㅋㅋㅋ: " + latestVersion);
        model.addAttribute("version", latestVersion);


        //소환사이름과 태그 검색
        String result = SummonerService.getAccountUrl(gameName, tagLine);

        // 입력된 값들을 디코딩 (디코딩이 필요한 경우에만)
        String decodedGameName = URLDecoder.decode(gameName, StandardCharsets.UTF_8.toString());
        String decodedTagLine = URLDecoder.decode(tagLine, StandardCharsets.UTF_8.toString());

        // 디코딩된 값을 모델에 추가
        model.addAttribute("gameName", decodedGameName);
        model.addAttribute("tagLine", decodedTagLine);
        model.addAttribute("result", result);

        AccountrDto accountrDto = ObjectMapper.readValue(result, AccountrDto.class);
        model.addAttribute("apiResult", accountrDto);


        //puuid 가져오기
        String puuid = accountrDto.getPuuid();
        System.out.println("puuid: " + puuid);
        String SummonerResult = SummonerService.getSummoner(puuid);
        SummonerDto summonerDto = ObjectMapper.readValue(SummonerResult, SummonerDto.class);
        model.addAttribute("userdata", summonerDto);
        model.addAttribute("SummonerResult", SummonerResult);


        //티어, 승/패 가져오기
        String id = summonerDto.getId();
        System.out.println("id: " + id);
        List<LeagueDto> leagueDtoList = SummonerService.getLeaguePoint(id);
        // 디버깅용 로그 출력
        System.out.println("LeagueDtoList size: " + leagueDtoList.size());
        // 모델에 추가
        model.addAttribute("LeagueList", leagueDtoList);




        String puuid2 = summonerDto.getPuuid();
        System.out.println("puuid2: " + puuid2);
        String puuid1 = new ObjectMapper().readTree(result).get("puuid").asText();
        System.out.println("puuid1: "+ puuid1);

        List<String> matchIdList = SummonerService.getMatches(puuid);

        model.addAttribute("matchid", matchIdList);


        List<String> matchIds = SummonerService.getMatchHistory(puuid);

        List<MatchDto> matchDataList = new ArrayList<>();

        for (String matchId : matchIds) {
            System.out.println("매치 ID: " + matchId);
            MatchDto matchData = SummonerService.getMatchDetails(matchId);

            if (matchData != null) {
                matchDataList.add(matchData);
            }
        }

// 최적화: 리스트 한 번에 추가
        model.addAttribute("matchDataList", matchDataList);

//        List<PlayerKDA> matchKDAList = new ArrayList<>();


//        List<PlayerKDA> playerKDAList = new ArrayList<>();
//
//        for (String matchId : matchIds) {
//            MatchDto matchData = SummonerService.getMatchDetails(matchId);
//
//            if (matchData != null && matchData.getInfo() != null) {
//                for (ParticipantDto player : matchData.getInfo().getParticipants()) {
//                    if (player.getRiotIdGameName().equalsIgnoreCase(gameName)) {
//                        playerKDAList.add(new PlayerKDA(
//                                matchId,
//                                player.getKills(),
//                                player.getDeaths(),
//                                player.getAssists(),
//                                player.getItems(),
//                                player.getSpell1Id(),
//                                player.getSpell2Id()
//                        ));
//                    }
//                }
//            }
//        }
//
//        model.addAttribute("playerKDAList", playerKDAList);

        return "result";
    }
}
