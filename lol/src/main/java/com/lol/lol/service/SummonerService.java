package com.lol.lol.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lol.lol.dto.LeagueDto;
import com.lol.lol.dto.MatchDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SummonerService {

    public String API_USER = "https://asia.api.riotgames.com/riot/account/v1/accounts/by-riot-id/";
    public String API_KEY="RGAPI-5c52c789-49d2-4d3e-bd4b-3723033caef3";
    public String API_SUMMONER ="https://kr.api.riotgames.com/lol/summoner/v4/summoners/by-puuid/";
    public String API_LEAGUE_URL = "https://kr.api.riotgames.com/lol/league/v4/entries/by-summoner/";
    public String API_MATCHES ="https://asia.api.riotgames.com/lol/match/v5/matches/by-puuid/{puuid}/ids?start=0&count=20&api_key=";
    public String API_MATCHDETAIL = "https://asia.api.riotgames.com/lol/match/v5/matches/";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public String getAccountUrl(String gameName, String tagLine) throws UnsupportedEncodingException {
        String encodedgameName = URLDecoder.decode(gameName, "UTF-8");
        String encodedtagLine = URLDecoder.decode(tagLine, "UTF-8");

        String decodedGameName = URLDecoder.decode(gameName, StandardCharsets.UTF_8.toString());
        String decodedTagLine = URLDecoder.decode(tagLine, StandardCharsets.UTF_8.toString());

//         String accountUrl = API_USER + encodedGameName + "/" + encodedTagLine + "?api_key=" + API_KEY;
        String accountUrl = API_USER + decodedGameName + "/" + decodedTagLine  + "?api_key=" + API_KEY;
        System.out.println("Request URL: " + accountUrl); // 요청 URL을 출력하여 확인

        String result = restTemplate.getForObject(accountUrl, String.class);
        System.out.println("Result: "+ result);
        return result;
    }

    public String getSummoner(String puuid){
        String Summoner = API_SUMMONER+puuid+"?api_key="+API_KEY;
        System.out.println(Summoner);
        String result = restTemplate.getForObject(Summoner, String.class);
        System.out.println("Get Summoner Result: "+ result);
        return result;
    }

    public List<LeagueDto> getLeaguePoint(String id) {
        String url = "https://kr.api.riotgames.com/lol/league/v4/entries/by-summoner/" + id + "?api_key=" + API_KEY;
        System.out.println("Request URL: " + url);

        String result = restTemplate.getForObject(url, String.class);
        System.out.println("LeagueResult: " + result);

        // JSON을 List<LeagueDto>로 변환
        List<LeagueDto> leagueDtoList = new ArrayList<>();
        try {
            if (result != null && !result.isEmpty() && !result.equals("[]")) {
                leagueDtoList = new ObjectMapper().readValue(result, new TypeReference<List<LeagueDto>>() {});
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return leagueDtoList; // JSON 변환 후 리스트 반환
    }

    public List<String> getMatches(String puuid) {
        String url = "https://asia.api.riotgames.com/lol/match/v5/matches/by-puuid/"
                + puuid + "/ids?start=0&count=20&api_key=" + API_KEY;
        @SuppressWarnings("unchecked")
        List<String> result = restTemplate.getForObject(url, List.class);
        return result != null ? result : new ArrayList<>();
    }

    public List<String> getMatchHistory(String puuid){
        String url = "https://asia.api.riotgames.com/lol/match/v5/matches/by-puuid/"
                + puuid + "/ids?start=0&count=20&api_key=" + API_KEY;
        @SuppressWarnings("unchecked")
        List<String> result = restTemplate.getForObject(url, List.class);
        return result != null ? result : new ArrayList<>();
    }

    public MatchDto getMatchDetails(String matchId) {
        String url = "https://asia.api.riotgames.com/lol/match/v5/matches/"
                + matchId + "?api_key=" + API_KEY;
        System.out.println(url);
        return restTemplate.getForObject(url, MatchDto.class);
    }

//    public String getLatestVersion(){
//        String url = "https://ddragon.leagueoflegends.com/api/versions.json";
//        System.out.println(url);
//        String result = restTemplate.getForObject(url, String.class);
//        System.out.println("result: z "+result);
    ////        String reuslt1 =result[0];
//        return result;
//     }

    public String getLatestVersion() {
        String url = "https://ddragon.leagueoflegends.com/api/versions.json";
        try {
            @SuppressWarnings("unchecked")
            List<String> versions = restTemplate.getForObject(url, List.class);
            if (versions != null && !versions.isEmpty()) {
                return versions.get(0); // 최신 버전 (0번째)
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Unknown"; // 오류 발생 시 기본값 반환
    }

}