package com.lol.lol.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lol.lol.dto.MatchDto;
import com.lol.lol.dto.LeagueDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SummonerService {

    // @Value 어노테이션으로 application.properties에서 값 가져오기
    @Value("${riot.api.key}")
    private String API_KEY;

    @Value("${riot.api.user.url}")
    private String API_USER;

    @Value("${riot.api.summoner.url}")
    private String API_SUMMONER;

    @Value("${riot.api.league.url}")
    private String API_LEAGUE_URL;

    @Value("${riot.api.matches.url}")
    private String API_MATCHES;

    @Value("${riot.api.matchdetail.url}")
    private String API_MATCHDETAIL;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public String getAccountUrl(String gameName, String tagLine) throws UnsupportedEncodingException {
        String encodedgameName = URLDecoder.decode(gameName, "UTF-8");
        String encodedtagLine = URLDecoder.decode(tagLine, "UTF-8");

        String decodedGameName = URLDecoder.decode(gameName, StandardCharsets.UTF_8.toString());
        String decodedTagLine = URLDecoder.decode(tagLine, StandardCharsets.UTF_8.toString());

        String accountUrl = API_USER + decodedGameName + "/" + decodedTagLine + "?api_key=" + API_KEY;
        System.out.println("Request URL: " + accountUrl); // 요청 URL을 출력하여 확인

        String result = restTemplate.getForObject(accountUrl, String.class);
        System.out.println("Result: " + result);
        return result;
    }

    public String getSummoner(String puuid) {
        String summoner = API_SUMMONER + puuid + "?api_key=" + API_KEY;
        System.out.println(summoner);
        String result = restTemplate.getForObject(summoner, String.class);
        System.out.println("Get Summoner Result: " + result);
        return result;
    }

    public List<LeagueDto> getLeaguePoint(String id) {
        String url = API_LEAGUE_URL + id + "?api_key=" + API_KEY;
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

    // 최근 20경기의 matchId 가져오기
    public List<String> getMatches(String puuid) {
        String url = API_MATCHES + "/" + puuid + "/ids?start=0&count=20&api_key=" + API_KEY;
        return restTemplate.getForObject(url, List.class);
    }

    public List<String> getMatchHistory(String puuid) {
        String url = API_MATCHES + "/" + puuid + "/ids?start=0&count=20&api_key=" + API_KEY;
        return restTemplate.getForObject(url, List.class);
    }

    public MatchDto getMatchDetails(String matchId) {
        String url = API_MATCHDETAIL + matchId + "?api_key=" + API_KEY;
        System.out.println(url);
        return restTemplate.getForObject(url, MatchDto.class);
    }

    public String getLatestVersion() {
        String url = "https://ddragon.leagueoflegends.com/api/versions.json";
        try {
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