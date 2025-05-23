package com.lol.lol.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lol.lol.dto.MatchDto;
import com.lol.lol.dto.AccountrDto;
import com.lol.lol.dto.LeagueDto;
import com.lol.lol.dto.SummonerDto;
import com.lol.lol.service.SummonerService;
import com.lol.lol.service.WeatherService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Controller
public class SearchController {

    private final SummonerService summonerService;
    private final ObjectMapper objectMapper;
    private final WeatherService weatherService;

    @GetMapping("/")
    public String intro() {
        return "intro";
    }

    // 개인화 페이지 - 서울 날씨 기준
    @GetMapping("/personal")
    public String personalPage(@RequestParam String gameName,
                               @RequestParam String tagLine,
                               Model model) throws UnsupportedEncodingException, JsonProcessingException {

        System.out.println("======= 개인화 페이지 접속 =======");
        System.out.println("gameName: " + gameName + ", tagLine: " + tagLine);

        // 기존 데이터 가져오기
        String latestVersion = summonerService.getLatestVersion();
        model.addAttribute("version", latestVersion);

        String result = summonerService.getAccountUrl(gameName, tagLine);
        String decodedGameName = URLDecoder.decode(gameName, StandardCharsets.UTF_8.toString());
        String decodedTagLine = URLDecoder.decode(tagLine, StandardCharsets.UTF_8.toString());

        model.addAttribute("gameName", decodedGameName);
        model.addAttribute("tagLine", decodedTagLine);

        AccountrDto accountrDto = objectMapper.readValue(result, AccountrDto.class);
        String puuid = accountrDto.getPuuid();

        String summonerResult = summonerService.getSummoner(puuid);
        SummonerDto summonerDto = objectMapper.readValue(summonerResult, SummonerDto.class);
        model.addAttribute("userdata", summonerDto);

        List<LeagueDto> leagueDtoList = summonerService.getLeaguePoint(summonerDto.getId());
        model.addAttribute("LeagueList", leagueDtoList);

        List<String> matchIds = summonerService.getMatchHistory(puuid);
        List<MatchDto> matchDataList = new ArrayList<>();

        for (String matchId : matchIds) {
            MatchDto matchData = summonerService.getMatchDetails(matchId);
            if (matchData != null) {
                matchDataList.add(matchData);
            }
        }
        model.addAttribute("matchDataList", matchDataList);

        // 개인화 분석 수행
        performPersonalizedAnalysis(summonerDto, leagueDtoList, matchDataList, decodedGameName, model);

        // 날씨 정보 가져오기 (수정된 버전)
        String weatherComment = weatherService.generateSimpleWeatherComment();
        double currentTemp = weatherService.getSeoulTemperature();
        String weatherCondition = weatherService.getSeoulWeatherCondition();

        model.addAttribute("weatherComment", weatherComment);
        model.addAttribute("currentTemp", Math.round(currentTemp));
        model.addAttribute("weatherCondition", weatherCondition);

        return "personal";
    }

    // 개인화 분석 수행 메서드
    private void performPersonalizedAnalysis(SummonerDto summoner, List<LeagueDto> leagues,
                                             List<MatchDto> matches, String playerName, Model model) {

        // 1. 플레이 시간 분석
        long estimatedHours = calculateEstimatedPlayTime(summoner.getSummonerLevel());
        String playTimeComment = generatePlayTimeComment(estimatedHours);
        model.addAttribute("playTimeComment", playTimeComment);
        model.addAttribute("estimatedHours", estimatedHours);

        // 2. 랭크 & 승률 분석
        if (!leagues.isEmpty()) {
            LeagueDto mainRank = leagues.get(0);
            double winRate = (double) mainRank.getWins() / (mainRank.getWins() + mainRank.getLosses()) * 100;

            model.addAttribute("winRate", Math.round(winRate * 10) / 10.0);
            model.addAttribute("winRateComment", generateWinRateComment(winRate));
            model.addAttribute("rankComment", generateRankComment(mainRank.getTier(), mainRank.getRank()));
            model.addAttribute("tier", mainRank.getTier());
            model.addAttribute("rank", mainRank.getRank());
            model.addAttribute("lp", mainRank.getLeaguePoints());
            model.addAttribute("wins", mainRank.getWins());
            model.addAttribute("losses", mainRank.getLosses());
        }

        // 3. 최근 경기 분석 (7경기)
        if (!matches.isEmpty()) {
            analyzeRecentMatches(matches, playerName, model);
        }
    }

    @PostMapping("/result")
    public String searchName(String gameName, String tagLine, Model model) throws UnsupportedEncodingException, JsonProcessingException {

        System.out.println("======= 입력값 확인 =======");
        System.out.println("gameName: " + gameName);
        System.out.println("tagLine: " + tagLine);

        String latestVersion = summonerService.getLatestVersion();
        model.addAttribute("version", latestVersion);

        String result = summonerService.getAccountUrl(gameName, tagLine);
        String decodedGameName = URLDecoder.decode(gameName, StandardCharsets.UTF_8.toString());
        String decodedTagLine = URLDecoder.decode(tagLine, StandardCharsets.UTF_8.toString());

        model.addAttribute("gameName", decodedGameName);
        model.addAttribute("tagLine", decodedTagLine);
        model.addAttribute("result", result);

        AccountrDto accountrDto = objectMapper.readValue(result, AccountrDto.class);
        model.addAttribute("apiResult", accountrDto);

        String puuid = accountrDto.getPuuid();
        String summonerResult = summonerService.getSummoner(puuid);
        SummonerDto summonerDto = objectMapper.readValue(summonerResult, SummonerDto.class);
        model.addAttribute("userdata", summonerDto);
        model.addAttribute("SummonerResult", summonerResult);

        String id = summonerDto.getId();
        List<LeagueDto> leagueDtoList = summonerService.getLeaguePoint(id);
        model.addAttribute("LeagueList", leagueDtoList);

        List<String> matchIdList = summonerService.getMatches(puuid);
        model.addAttribute("matchid", matchIdList);

        List<String> matchIds = summonerService.getMatchHistory(puuid);
        List<MatchDto> matchDataList = new ArrayList<>();

        for (String matchId : matchIds) {
            MatchDto matchData = summonerService.getMatchDetails(matchId);
            if (matchData != null) {
                matchDataList.add(matchData);
            }
        }
        model.addAttribute("matchDataList", matchDataList);

        return "result";
    }

    // 플레이 시간 계산
    private long calculateEstimatedPlayTime(long level) {
        long estimatedGames;

        if (level <= 10) {
            estimatedGames = level * 3;
        } else if (level <= 30) {
            estimatedGames = 30 + (level - 10) * 5;
        } else if (level <= 100) {
            estimatedGames = 130 + (level - 30) * 8;
        } else if (level <= 200) {
            estimatedGames = 690 + (level - 100) * 12;
        } else {
            estimatedGames = 1890 + (level - 200) * 15;
        }

        long estimatedHours = (estimatedGames * 30) / 60;
        System.out.println("레벨 " + level + " → 추정게임수 " + estimatedGames + "게임 → " + estimatedHours + "시간");
        return estimatedHours;
    }

    // 멘트 생성 메서드들
    private String generatePlayTimeComment(long hours) {
        if (hours > 2000) {
            return "총 추정 플레이 시간 " + hours + "시간... 진짜 프로게이머 하셔도 될 것 같은데요? 🎮";
        } else if (hours > 1000) {
            return "총 추정 플레이 시간 " + hours + "시간... 진짜 게임 좋아하시는군요! 🎮";
        } else if (hours > 500) {
            return hours + "시간이면 정말 롤을 사랑하시는군요! 💖";
        } else if (hours < 100) {
            return "아직 " + hours + "시간밖에... 바깥세상이 그립지 않으세요? 🌞";
        }
        return hours + "시간... 적당한 플레이 타임이네요! 👍";
    }

    private String generateWinRateComment(double winRate) {
        if (winRate > 70) {
            return "승률 " + String.format("%.1f", winRate) + "%... 혹시 대리인가요? 🤨";
        } else if (winRate > 60) {
            return "승률이 좋으시네요! 부럽습니다 😊";
        } else if (winRate < 45) {
            return "승률이... 음... 롤은 재미로 하는 거니까요! 😅";
        }
        return "적당한 승률이네요. 밸런스가 좋아요! ⚖️";
    }

    private String generateRankComment(String tier, String rank) {
        if ("CHALLENGER".equals(tier) || "GRANDMASTER".equals(tier) || "MASTER".equals(tier)) {
            return "와... 고수시네요! 존경합니다! 🏆";
        } else if ("DIAMOND".equals(tier)) {
            return "다이아몬드... 진짜 잘하시네요! 💎";
        } else if ("PLATINUM".equals(tier)) {
            return "플래티넘! 상위 유저시네요! ✨";
        } else if ("GOLD".equals(tier)) {
            return "골드 티어! 평균 이상이시네요! 🥇";
        } else if ("SILVER".equals(tier)) {
            return "실버 티어! 꾸준히 하시면 금방 올라가실 거예요! 🥈";
        } else if ("BRONZE".equals(tier)) {
            return "브론즈도 좋아요! 시작이 반이니까요! 🥉";
        }
        return "언랭이시군요! 랭크 게임 도전해보세요! 💪";
    }

    // 최근 경기 분석 (수정된 버전)
    private void analyzeRecentMatches(List<MatchDto> matchDataList, String playerName, Model model) {
        int totalKills = 0, totalDeaths = 0, totalAssists = 0, wins = 0;
        Map<String, Integer> championCount = new HashMap<>();

        int matchCount = Math.min(7, matchDataList.size());

        for (int i = 0; i < matchCount; i++) {
            MatchDto match = matchDataList.get(i);
            MatchDto.MatchAnalysis analysis = match.analyzeMatch(playerName);

            if (analysis.isFound()) {
                totalKills += analysis.getKills();
                totalDeaths += analysis.getDeaths();
                totalAssists += analysis.getAssists();

                if (analysis.isWin()) wins++;

                championCount.put(analysis.getChampionName(),
                        championCount.getOrDefault(analysis.getChampionName(), 0) + 1);
            }
        }

        double kda = totalDeaths > 0 ? (double)(totalKills + totalAssists) / totalDeaths : totalKills + totalAssists;
        String mainChampion = championCount.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("정보없음");

        model.addAttribute("averageKDA", Math.round(kda * 100) / 100.0);
        model.addAttribute("kdaComment", generateKDAComment(kda));
        model.addAttribute("mainChampion", mainChampion);
        model.addAttribute("championComment", generateChampionComment(mainChampion));
        model.addAttribute("recentWins", wins);
        model.addAttribute("recentTotal", matchCount);
        model.addAttribute("recentPerformanceComment", generateRecentPerformanceComment(wins, matchCount));
    }

    private String generateKDAComment(double kda) {
        if (kda > 3.0) {
            return "KDA " + String.format("%.2f", kda) + "... 고수시네요! 🔥";
        } else if (kda < 1.0) {
            return "KDA " + String.format("%.2f", kda) + "... 게임은 재미가 중요하죠! 😊";
        }
        return "적당한 KDA네요. 균형잡힌 플레이! ✨";
    }

    private String generateChampionComment(String championName) {
        Map<String, String> championComments = new HashMap<>();
        championComments.put("야스오", "야스오 장인... 혹시 현실에서도 위험한 스타일이신가요? 😏");
        championComments.put("소라카", "소라카를 주로... 천사시네요! 현실에서도 착하실 것 같아요 😇");
        championComments.put("티모", "티모... 악마의 속삭임을 들으시는군요... 😈");
        championComments.put("진", "진을 좋아하시는군요. 예술가의 기질이 있으시네요 🎭");
        championComments.put("말파이트", "말파이트로 안정적인 플레이를 하시는군요! 🗿");

        return championComments.getOrDefault(championName, championName + " 장인이시군요! 멋있어요! 👍");
    }

    private String generateRecentPerformanceComment(int wins, int totalGames) {
        if (wins >= totalGames * 0.8) {
            return "최근 " + totalGames + "경기 중 " + wins + "승! 컨디션이 좋으시네요! 🔥";
        } else if (wins >= totalGames * 0.6) {
            return "최근 성과가 괜찮으시네요! 이 기세 유지하세요! 👍";
        } else {
            return "최근 경기가 아쉬우셨네요... 다음엔 더 잘하실 거예요! 💪";
        }
    }
}