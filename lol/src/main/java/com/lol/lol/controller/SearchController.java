package com.lol.lol.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lol.lol.dto.MatchDto;
import com.lol.lol.dto.AccountrDto;
import com.lol.lol.dto.LeagueDto;
import com.lol.lol.dto.SummonerDto;
import com.lol.lol.dto.ChampionRecommendationResult;
import com.lol.lol.service.SummonerService;
import com.lol.lol.service.WeatherService;
import com.lol.lol.service.ContentRecommendationService;
import com.lol.lol.service.ContentRecommendationService.PersonalizedContentResponse; // ✅ 올바른 클래스 import
import com.lol.lol.service.ChampionRecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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
    private final ContentRecommendationService contentRecommendationService;
    private final ChampionRecommendationService championRecommendationService;

    @GetMapping("/")
    public String intro() {
        return "intro";
    }

    /**
     * 개인화 페이지 - 콘텐츠 추천 + 미니게임 + 챔피언 추천 포함
     */
    @GetMapping("/personal")
    public String personalPage(@RequestParam String gameName,
                               @RequestParam String tagLine,
                               Model model) {
        try {
            // 입력값 검증
            if (gameName == null || gameName.trim().isEmpty()) {
                model.addAttribute("error", "게임 이름을 입력해주세요.");
                return "intro";
            }
            if (tagLine == null || tagLine.trim().isEmpty()) {
                model.addAttribute("error", "태그를 입력해주세요.");
                return "intro";
            }

            System.out.println("======= 개인화 페이지 접속 =======");
            System.out.println("gameName: " + gameName + ", tagLine: " + tagLine);

            // 기본 정보 설정
            String latestVersion = summonerService.getLatestVersion();
            model.addAttribute("version", latestVersion);

            // 사용자 데이터 조회
            try {
                String result = summonerService.getAccountUrl(gameName, tagLine);
                String decodedGameName = URLDecoder.decode(gameName, StandardCharsets.UTF_8);
                String decodedTagLine = URLDecoder.decode(tagLine, StandardCharsets.UTF_8);

                model.addAttribute("gameName", decodedGameName);
                model.addAttribute("tagLine", decodedTagLine);

                AccountrDto accountrDto = objectMapper.readValue(result, AccountrDto.class);

                if (accountrDto.getPuuid() == null || accountrDto.getPuuid().isEmpty()) {
                    throw new RuntimeException("사용자 정보에서 PUUID를 찾을 수 없습니다.");
                }

                String puuid = accountrDto.getPuuid();
                String summonerResult = summonerService.getSummoner(puuid);
                SummonerDto summonerDto = objectMapper.readValue(summonerResult, SummonerDto.class);

                if (summonerDto.getId() == null) {
                    throw new RuntimeException("소환사 ID를 찾을 수 없습니다.");
                }

                model.addAttribute("userdata", summonerDto);

                // 리그 정보 조회
                List<LeagueDto> leagueDtoList = summonerService.getLeaguePoint(summonerDto.getId());
                model.addAttribute("LeagueList", leagueDtoList);

                // 매치 히스토리 조회
                List<String> matchIds = summonerService.getMatchHistory(puuid);
                List<MatchDto> matchDataList = new ArrayList<>();

                for (String matchId : matchIds) {
                    try {
                        MatchDto matchData = summonerService.getMatchDetails(matchId);
                        if (matchData != null) {
                            matchDataList.add(matchData);
                        }
                    } catch (Exception e) {
                        System.err.println("매치 상세 정보 처리 중 오류 (ID: " + matchId + "): " + e.getMessage());
                    }
                }
                model.addAttribute("matchDataList", matchDataList);

                // 개인화 분석 수행
                performPersonalizedAnalysis(summonerDto, leagueDtoList, matchDataList, decodedGameName, model);

                // ✨ 챔피언 추천 시스템 추가 ✨
                try {
                    ChampionRecommendationResult championRecommendations =
                            championRecommendationService.generateChampionRecommendations(
                                    summonerDto, leagueDtoList, matchDataList, decodedGameName);

                    model.addAttribute("championRecommendations", championRecommendations);
                    System.out.println("챔피언 추천 생성 완료: " +
                            championRecommendations.getRecommendationsByRole().size() + " 라인");

                } catch (Exception e) {
                    System.err.println("챔피언 추천 생성 중 오류: " + e.getMessage());
                    e.printStackTrace();
                    // 추천 실패 시에도 페이지는 정상 작동하도록 null 설정
                    model.addAttribute("championRecommendations", null);
                }

                // ✅ 티어별 개인화 콘텐츠 추천 (수정된 코드)
                PersonalizedContentResponse contentRecommendation = contentRecommendationService
                        .generatePersonalizedContent(summonerDto, leagueDtoList, matchDataList, decodedGameName);

                model.addAttribute("contentRecommendation", contentRecommendation);

                // 날씨 정보 가져오기
                try {
                    String weatherComment = weatherService.generateSimpleWeatherComment();
                    double currentTemp = weatherService.getSeoulTemperature();
                    String weatherCondition = weatherService.getSeoulWeatherCondition();

                    model.addAttribute("weatherComment", weatherComment);
                    model.addAttribute("currentTemp", Math.round(currentTemp));
                    model.addAttribute("weatherCondition", weatherCondition);
                } catch (Exception e) {
                    System.err.println("날씨 정보 처리 중 오류: " + e.getMessage());
                    model.addAttribute("weatherComment", "오늘도 게임하기 좋은 날이에요! 🎮");
                    model.addAttribute("currentTemp", 20);
                    model.addAttribute("weatherCondition", "맑음");
                }

                return "personal";

            } catch (JsonProcessingException e) {
                System.err.println("JSON 파싱 오류: " + e.getMessage());
                model.addAttribute("error", "사용자 데이터 처리 중 오류가 발생했습니다.");
                return "intro";
            }

        } catch (RuntimeException e) {
            System.err.println("개인화 페이지 RuntimeException: " + e.getMessage());
            model.addAttribute("error", e.getMessage());
            return "intro";
        } catch (Exception e) {
            System.err.println("개인화 페이지 예상치 못한 오류: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "서비스 이용 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
            return "intro";
        }
    }

    /**
     * 검색 결과 페이지
     */
    @PostMapping("/result")
    public String searchName(@RequestParam String gameName,
                             @RequestParam String tagLine,
                             Model model) {
        try {
            // 입력값 검증
            if (gameName == null || gameName.trim().isEmpty()) {
                model.addAttribute("error", "게임 이름을 입력해주세요.");
                return "intro";
            }
            if (tagLine == null || tagLine.trim().isEmpty()) {
                model.addAttribute("error", "태그를 입력해주세요.");
                return "intro";
            }

            System.out.println("======= 입력값 확인 =======");
            System.out.println("gameName: " + gameName);
            System.out.println("tagLine: " + tagLine);

            // 기본 정보 설정
            String latestVersion = summonerService.getLatestVersion();
            model.addAttribute("version", latestVersion);

            try {
                String result = summonerService.getAccountUrl(gameName, tagLine);
                String decodedGameName = URLDecoder.decode(gameName, StandardCharsets.UTF_8);
                String decodedTagLine = URLDecoder.decode(tagLine, StandardCharsets.UTF_8);

                model.addAttribute("gameName", decodedGameName);
                model.addAttribute("tagLine", decodedTagLine);
                model.addAttribute("result", result);

                AccountrDto accountrDto = objectMapper.readValue(result, AccountrDto.class);
                model.addAttribute("apiResult", accountrDto);

                if (accountrDto.getPuuid() == null || accountrDto.getPuuid().isEmpty()) {
                    throw new RuntimeException("사용자 정보에서 PUUID를 찾을 수 없습니다.");
                }

                String puuid = accountrDto.getPuuid();
                String summonerResult = summonerService.getSummoner(puuid);
                SummonerDto summonerDto = objectMapper.readValue(summonerResult, SummonerDto.class);

                if (summonerDto.getId() == null) {
                    throw new RuntimeException("소환사 ID를 찾을 수 없습니다.");
                }

                model.addAttribute("userdata", summonerDto);
                model.addAttribute("SummonerResult", summonerResult);

                // 리그 정보 조회
                List<LeagueDto> leagueDtoList = summonerService.getLeaguePoint(summonerDto.getId());
                model.addAttribute("LeagueList", leagueDtoList);

                // 매치 히스토리 조회
                List<String> matchIdList = summonerService.getMatches(puuid);
                model.addAttribute("matchid", matchIdList);

                List<String> matchIds = summonerService.getMatchHistory(puuid);
                List<MatchDto> matchDataList = new ArrayList<>();

                for (String matchId : matchIds) {
                    try {
                        MatchDto matchData = summonerService.getMatchDetails(matchId);
                        if (matchData != null) {
                            matchDataList.add(matchData);
                        }
                    } catch (Exception e) {
                        System.err.println("매치 상세 정보 처리 중 오류 (ID: " + matchId + "): " + e.getMessage());
                    }
                }
                model.addAttribute("matchDataList", matchDataList);

                return "result";

            } catch (JsonProcessingException e) {
                System.err.println("JSON 파싱 오류: " + e.getMessage());
                model.addAttribute("error", "사용자 데이터 처리 중 오류가 발생했습니다.");
                return "intro";
            }

        } catch (RuntimeException e) {
            System.err.println("검색 결과 RuntimeException: " + e.getMessage());
            model.addAttribute("error", e.getMessage());
            return "intro";
        } catch (Exception e) {
            System.err.println("검색 결과 예상치 못한 오류: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "검색 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
            return "intro";
        }
    }

    /**
     * 개인화 분석 수행
     */
    private void performPersonalizedAnalysis(SummonerDto summoner, List<LeagueDto> leagues,
                                             List<MatchDto> matches, String playerName, Model model) {
        try {
            // 1. 플레이 시간 분석
            if (summoner != null && summoner.getSummonerLevel() != null) {
                long estimatedHours = calculateEstimatedPlayTime(summoner.getSummonerLevel());
                String playTimeComment = generatePlayTimeComment(estimatedHours);
                model.addAttribute("playTimeComment", playTimeComment);
                model.addAttribute("estimatedHours", estimatedHours);
            } else {
                model.addAttribute("playTimeComment", "플레이 시간 정보를 가져올 수 없습니다.");
                model.addAttribute("estimatedHours", 0);
            }

            // 2. 랭크 & 승률 분석
            if (leagues != null && !leagues.isEmpty()) {
                try {
                    LeagueDto mainRank = leagues.get(0);

                    if (mainRank.getWins() != null && mainRank.getLosses() != null &&
                            (mainRank.getWins() + mainRank.getLosses()) > 0) {

                        double winRate = (double) mainRank.getWins() / (mainRank.getWins() + mainRank.getLosses()) * 100;

                        model.addAttribute("winRate", Math.round(winRate * 10) / 10.0);
                        model.addAttribute("winRateComment", generateWinRateComment(winRate));
                        model.addAttribute("rankComment", generateRankComment(
                                mainRank.getTier() != null ? mainRank.getTier() : "UNRANKED",
                                mainRank.getRank() != null ? mainRank.getRank() : ""
                        ));
                        model.addAttribute("tier", mainRank.getTier() != null ? mainRank.getTier() : "UNRANKED");
                        model.addAttribute("rank", mainRank.getRank() != null ? mainRank.getRank() : "");
                        model.addAttribute("lp", mainRank.getLeaguePoints() != null ? mainRank.getLeaguePoints() : "0");
                        model.addAttribute("wins", mainRank.getWins() != null ? mainRank.getWins() : 0);
                        model.addAttribute("losses", mainRank.getLosses() != null ? mainRank.getLosses() : 0);
                    } else {
                        setUnrankedAttributes(model);
                    }
                } catch (Exception e) {
                    System.err.println("랭크 정보 분석 중 오류: " + e.getMessage());
                    setUnrankedAttributes(model);
                }
            } else {
                setUnrankedAttributes(model);
            }

            // 3. 최근 경기 분석
            if (matches != null && !matches.isEmpty()) {
                try {
                    analyzeRecentMatches(matches, playerName, model);
                } catch (Exception e) {
                    System.err.println("최근 경기 분석 중 오류: " + e.getMessage());
                    setDefaultMatchAnalysis(model);
                }
            } else {
                setDefaultMatchAnalysis(model);
            }

        } catch (Exception e) {
            System.err.println("개인화 분석 전체 오류: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("playTimeComment", "분석 중 오류가 발생했습니다.");
            model.addAttribute("estimatedHours", 0);
            setUnrankedAttributes(model);
            setDefaultMatchAnalysis(model);
        }
    }

    /**
     * 언랭 속성 설정
     */
    private void setUnrankedAttributes(Model model) {
        model.addAttribute("winRate", 0);
        model.addAttribute("winRateComment", "랭크 게임을 플레이해보세요!");
        model.addAttribute("rankComment", "언랭이시군요! 랭크 게임 도전해보세요! 💪");
        model.addAttribute("tier", "UNRANKED");
        model.addAttribute("rank", "");
        model.addAttribute("lp", "0");
        model.addAttribute("wins", 0);
        model.addAttribute("losses", 0);
    }

    /**
     * 기본 매치 분석 설정
     */
    private void setDefaultMatchAnalysis(Model model) {
        model.addAttribute("averageKDA", 0.0);
        model.addAttribute("kdaComment", "최근 경기 기록이 없습니다.");
        model.addAttribute("mainChampion", "정보없음");
        model.addAttribute("championComment", "경기를 플레이해보세요!");
        model.addAttribute("recentWins", 0);
        model.addAttribute("recentTotal", 0);
        model.addAttribute("recentPerformanceComment", "최근 경기 기록이 없습니다.");
    }

    /**
     * 플레이 시간 계산
     */
    private long calculateEstimatedPlayTime(long level) {
        try {
            if (level < 0) level = 1;
            if (level > 1000) level = 1000;

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

            if (estimatedHours < 0) estimatedHours = 0;
            if (estimatedHours > 50000) estimatedHours = 50000;

            System.out.println("레벨 " + level + " → 추정게임수 " + estimatedGames + "게임 → " + estimatedHours + "시간");
            return estimatedHours;

        } catch (Exception e) {
            System.err.println("플레이 시간 계산 오류: " + e.getMessage());
            return 100;
        }
    }

    /**
     * 최근 경기 분석
     */
    private void analyzeRecentMatches(List<MatchDto> matchDataList, String playerName, Model model) {
        try {
            int totalKills = 0, totalDeaths = 0, totalAssists = 0, wins = 0;
            Map<String, Integer> championCount = new HashMap<>();

            int matchCount = Math.min(7, matchDataList.size());
            int validMatches = 0;

            for (int i = 0; i < matchCount; i++) {
                try {
                    MatchDto match = matchDataList.get(i);
                    if (match == null) {
                        System.out.println("매치 데이터가 null입니다. 인덱스: " + i);
                        continue;
                    }

                    MatchDto.MatchAnalysis analysis = match.analyzeMatch(playerName);

                    if (analysis != null && analysis.isFound()) {
                        totalKills += analysis.getKills();
                        totalDeaths += analysis.getDeaths();
                        totalAssists += analysis.getAssists();

                        if (analysis.isWin()) wins++;

                        String championName = analysis.getChampionName();
                        if (championName != null && !championName.isEmpty()) {
                            championCount.put(championName,
                                    championCount.getOrDefault(championName, 0) + 1);
                        }

                        validMatches++;
                    }
                } catch (Exception e) {
                    System.err.println("개별 매치 분석 중 오류 (인덱스 " + i + "): " + e.getMessage());
                }
            }

            if (validMatches > 0) {
                double kda = totalDeaths > 0 ? (double)(totalKills + totalAssists) / totalDeaths : totalKills + totalAssists;

                if (Double.isNaN(kda) || Double.isInfinite(kda) || kda < 0) {
                    kda = 0.0;
                }

                String mainChampion = championCount.entrySet().stream()
                        .max(Map.Entry.comparingByValue())
                        .map(Map.Entry::getKey)
                        .orElse("정보없음");

                model.addAttribute("averageKDA", Math.round(kda * 100) / 100.0);
                model.addAttribute("kdaComment", generateKDAComment(kda));
                model.addAttribute("mainChampion", mainChampion);
                model.addAttribute("championComment", generateChampionComment(mainChampion));
                model.addAttribute("recentWins", wins);
                model.addAttribute("recentTotal", validMatches);
                model.addAttribute("recentPerformanceComment", generateRecentPerformanceComment(wins, validMatches));
            } else {
                setDefaultMatchAnalysis(model);
            }

        } catch (Exception e) {
            System.err.println("최근 경기 분석 전체 오류: " + e.getMessage());
            setDefaultMatchAnalysis(model);
        }
    }

    // 멘트 생성 메서드들
    private String generatePlayTimeComment(long hours) {
        try {
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
        } catch (Exception e) {
            return "플레이 시간 분석 중 오류가 발생했습니다.";
        }
    }

    private String generateWinRateComment(double winRate) {
        try {
            if (Double.isNaN(winRate) || winRate < 0 || winRate > 100) {
                return "승률 정보를 확인할 수 없습니다.";
            }

            if (winRate > 70) {
                return "승률 " + String.format("%.1f", winRate) + "%... 혹시 대리인가요? 🤨";
            } else if (winRate > 60) {
                return "승률이 좋으시네요! 부럽습니다 😊";
            } else if (winRate < 45) {
                return "승률이... 음... 롤은 재미로 하는 거니까요! 😅";
            }
            return "적당한 승률이네요. 밸런스가 좋아요! ⚖️";
        } catch (Exception e) {
            return "승률 분석 중 오류가 발생했습니다.";
        }
    }

    private String generateRankComment(String tier, String rank) {
        try {
            if (tier == null) tier = "UNRANKED";

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
        } catch (Exception e) {
            return "랭크 분석 중 오류가 발생했습니다.";
        }
    }

    private String generateKDAComment(double kda) {
        try {
            if (Double.isNaN(kda) || Double.isInfinite(kda) || kda < 0) {
                return "KDA 정보를 확인할 수 없습니다.";
            }

            if (kda > 3.0) {
                return "KDA " + String.format("%.2f", kda) + "... 고수시네요! 🔥";
            } else if (kda < 1.0) {
                return "KDA " + String.format("%.2f", kda) + "... 게임은 재미가 중요하죠! 😊";
            }
            return "적당한 KDA네요. 균형잡힌 플레이! ✨";
        } catch (Exception e) {
            return "KDA 분석 중 오류가 발생했습니다.";
        }
    }

    private String generateChampionComment(String championName) {
        try {
            if (championName == null || championName.isEmpty() || "정보없음".equals(championName)) {
                return "최근 플레이한 챔피언 정보가 없습니다.";
            }

            Map<String, String> championComments = new HashMap<>();
            // 리그 오브 레전드 챔피언 개성 멘트 (샘플)
            championComments.put("Aatrox", "다르킨의 힘을... 혹시 평소에도 파워풀하신가요? ⚔️");
            championComments.put("Ahri", "매혹적인 플레이를 하시는군요! 현실에서도 인기가 많으실 것 같아요 ✨");
            championComments.put("Akali", "닌자 플레이... 혹시 평소에도 조용하신 편인가요? 🥷");
            championComments.put("Akshan", "파멸자... 정의를 위해서라면 뭐든 하시는 타입이군요 ⚔️✨");
            championComments.put("Alistar", "황소의 힘! 현실에서도 든든한 분이시겠네요 🐂");
            championComments.put("Ambessa", "녹서스의 전쟁영주... 카리스마와 야망이 넘치시는군요 ⚔️👑");
            championComments.put("Ammu", "외로운 미라... 친구가 필요하시겠어요 🤗");
            championComments.put("Anivia", "얼음불사조... 차가운 지혜를 가지고 계시네요 🧊🦅");
            championComments.put("Annie", "불을 다루는 소녀... 화끈한 성격이시겠네요! 🔥");
            championComments.put("Aphelios", "달의 무기고... 말보다는 행동으로 표현하시는 타입이군요 🌙🔫");
            championComments.put("Ashe", "얼음 여왕이시군요! 차가운 판단력의 소유자시겠어요 🏹❄️");
            championComments.put("Aurelion Sol", "별을 다루시는군요! 우주적 스케일의 사고를 하시나요? ⭐");
            championComments.put("Aurora", "바스타야의 영혼... 신비로운 마법과 자연을 사랑하시는군요 🌸✨");
            championComments.put("Azir", "사막의 황제... 리더십과 카리스마가 뛰어나시겠어요 👑🦅");
            championComments.put("Bel'Veth", "공허의 여제... 압도적인 존재감을 가지고 계시네요 👑👹");
            championComments.put("Blitzcrank", "로봇이 좋으신가요? 현실에서도 후킹 실력이 좋으실 것 같은데요? 🤖");
            championComments.put("Brand", "불의 정령... 열정적인 삶을 사시는군요! 🔥");
            championComments.put("Braum", "따뜻한 마음의 소유자시군요! 주변 사람들의 든든한 버팀목이시겠어요 💪❄️");
            championComments.put("Briar", "혈기왕성한 피의 사냥꾼... 열정적이시겠어요! 🩸⚔️");
            championComments.put("Caitlyn", "필트오버의 보안관! 정확하고 공정한 분이시겠어요 🔫👮‍♀️");
            championComments.put("Camille", "정밀함의 극치! 완벽주의자시겠어요 ⚙️");
            championComments.put("Cassiopeia", "독사의 포옹... 치명적인 아름다움을 가지고 계시네요 🐍💚");
            championComments.put("Cho'Gath", "공허의 공포... 성장 욕구가 끝이 없으시군요 👹🦷");
            championComments.put("Corki", "대담한 폭격수... 하늘을 나는 꿈을 가지고 계시네요 ✈️💣");
            championComments.put("Darius", "녹서스의 힘! 카리스마가 넘치시겠어요 ⚔️");
            championComments.put("Diana", "달의 여신... 신비로운 밤을 좋아하시나요? 🌙");
            championComments.put("Dr. Mundo", "미친 의사... 독특한 치료법을 가지고 계시군요 💉🧪");
            championComments.put("Draven", "화려함의 극치! 관심받는 걸 좋아하시는군요 ✨");
            championComments.put("Ekko", "시간의 파괴자... 과거를 바꾸고 싶어 하시는군요 ⏰");
            championComments.put("Elise", "거미 여왕... 치명적인 아름다움을 가지고 계시네요 🕷️");
            championComments.put("Evelynn", "매혹의 악마... 치명적인 매력을 가지고 계시군요 😈💋");
            championComments.put("Ezreal", "탐험가의 정신! 모험을 좋아하시는 분이시겠어요 🗺️");
            championComments.put("Fiddlesticks", "공포 그 자체... 무서운 이야기 좋아하시나요? 😱");
            championComments.put("Fiora", "결투의 대가! 1대1에 자신이 있으시겠어요 ⚔️");
            championComments.put("Fizz", "물속의 장난꾸러기! 유쾌한 성격이시겠어요 🐟");
            championComments.put("Galio", "정의의 거상! 든든한 수호자 역할을 하시는군요 🗿✨");
            championComments.put("Gangplank", "해적의 삶! 자유로운 영혼을 가지고 계시네요 🏴‍☠️");
            championComments.put("Garen", "데마시아의 정의! 정의감이 강하고 올곧은 성격이시겠어요 ⚔️🛡️");
            championComments.put("Gnar", "작은 거인! 평소엔 순하다가 화나면 무서우신 타입인가요? 🦖💙");
            championComments.put("Gragas", "술의 달인... 인생을 즐기며 사시는군요! 🍺");
            championComments.put("Graves", "거친 총잡이! 남성적 매력이 넘치시겠어요 🔫🚬");
            championComments.put("Gwen", "성스러운 재봉사... 세심하고 따뜻한 마음을 가지고 계시군요 ✂️💙");
            championComments.put("Hecarim", "전쟁의 망령... 돌진하는 스타일이시군요! 🐎👻");
            championComments.put("Heimerdinger", "천재 발명가! 창의력이 뛰어나시겠어요 🔧");
            championComments.put("Hwei", "감정의 화가... 예술혼이 넘치시는 분이시군요 🎨✨");
            championComments.put("Illaoi", "크라켄의 사제... 바다의 힘을 가지고 계시네요 🐙");
            championComments.put("Irelia", "칼날 춤의 달인! 우아함과 강함을 동시에 가지고 계시네요 ⚔️💃");
            championComments.put("Ivern", "자연을 사랑하시는군요! 환경보호에 관심이 많으실 것 같아요 🌳");
            championComments.put("Janna", "바람의 정령... 온화하고 보호하는 마음을 가지고 계시네요 🌪️");
            championComments.put("Jarvan IV", "데마시아의 왕자... 왕족다운 품격을 가지고 계시네요 👑⚔️");
            championComments.put("Jax", "무기의 달인... 뭐든 무기로 만들어버리시는군요! 🏮");
            championComments.put("Jayce", "미래의 수호자... 과학기술에 대한 열정이 있으시군요 🔨⚡");
            championComments.put("Jhin", "잔혹극의 거장... 예술적 감각이 뛰어나시겠어요 🎭🔫");
            championComments.put("Jinx", "미친 천재... 예측 불가능한 매력이 있으시군요! 🎪💥");
            championComments.put("K'Sante", "나즈마아니의 자부심... 자신감과 실력을 모두 갖추셨군요 🛡️💪");
            championComments.put("Kai'Sa", "공허의 딸... 강인한 생존력을 가지고 계시네요 👽");
            championComments.put("Kalista", "복수의 창... 배신은 용서 못하시는 타입인가요? 👻");
            championComments.put("Karma", "깨달음의 소울... 정신적 성숙함을 가지고 계시네요 🧘‍♀️✨");
            championComments.put("Karthus", "죽음의 성가대... 끝까지 최선을 다하시는군요 💀🎵");
            championComments.put("Kassadin", "공허를 걷는 자... 차원이 다른 플레이를 하시는군요! 🌌");
            championComments.put("Katarina", "암살자의 춤! 화려한 플레이를 좋아하시는군요 🗡️💃");
            championComments.put("Kayle", "정의로운 천사... 완벽함을 추구하시는 분이시군요 😇⚔️");
            championComments.put("Kayn", "그림자의 힘... 이중적인 매력이 있으시군요 😈👼");
            championComments.put("Kennen", "폭풍의 심장... 작지만 번개처럼 빠르시겠어요 ⚡🐿️");
            championComments.put("Kha'Zix", "진화하는 사냥꾼... 성장 욕구가 강하시겠어요! 🦗");
            championComments.put("Kindred", "죽음의 쌍둥이... 철학적인 분이시군요 🏹💀");
            championComments.put("Kled", "미친 기사... 용감무쌍한 성격이시겠어요! ⚔️🦎");
            championComments.put("Kog'Maw", "공허의 입... 먹는 걸 좋아하시나요? 👹");
            championComments.put("LeBlanc", "기만의 마녀... 신비로운 매력을 가지고 계시네요 🎭");
            championComments.put("Lee Sin", "눈먼 수도승... 내면의 힘이 강하시겠어요 👊");
            championComments.put("Leona", "태양의 화신! 밝고 따뜻한 성격이시겠어요 ☀️");
            championComments.put("Lillia", "수줍은 꽃... 내성적이지만 꿈이 많으신 분이시겠어요 🌸💤");
            championComments.put("Lissandra", "얼음 마녀... 차가운 카리스마를 가지고 계시네요 ❄️");
            championComments.put("Lucian", "빛의 사도... 정의로운 마음을 가지고 계시군요 🔫✨");
            championComments.put("Lulu", "요들 마법사... 귀여운 매력이 있으시겠어요! 🧚‍♀️");
            championComments.put("Lux", "빛의 소녀... 밝고 긍정적인 에너지를 가지고 계시네요 ✨💫");
            championComments.put("Malphite", "거대한 바위... 든든하고 믿음직한 분이시겠어요 🗿💪");
            championComments.put("Malzahar", "공허의 예언자... 미래를 내다보는 혜안이 있으시군요 👁️");
            championComments.put("Maokai", "생명의 나무... 자연을 사랑하시는 분이시겠어요 🌳");
            championComments.put("Master Yi", "검의 달인! 수행자의 마음을 가지고 계시네요 ⚔️🧘‍♂️");
            championComments.put("Mel", "필트오버의 의원... 우아함과 지혜를 동시에 가지고 계시네요 ✨🏛️");
            championComments.put("Milio", "따뜻한 마음의 치유사... 모든 걸 보듬어주시는 타입이군요 🔥💚");
            championComments.put("Miss Fortune", "현상금 사냥꾼... 매력과 실력을 모두 갖추셨군요 🔫💋");
            championComments.put("Mordekaiser", "철의 망령... 무겁고 강력한 카리스마를 가지고 계시네요 ⚔️👻");
            championComments.put("Morgana", "타락한 천사... 자유를 소중히 여기시는군요 😈🔗");
            championComments.put("Naafiri", "사막의 사냥개 무리... 팀워크를 중시하시는군요! 🐺⚔️");
            championComments.put("Nami", "바다의 정령... 유연하고 치유의 마음을 가지고 계시네요 🌊");
            championComments.put("Nasus", "사막의 큐레이터... 지식과 인내의 소유자시군요 📚🐕");
            championComments.put("Nautilus", "바다의 파수꾼... 깊이 있는 성격이시겠어요 ⚓");
            championComments.put("Neeko", "호기심 많은 카멜레온... 변화를 두려워하지 않으시는군요 🦎🌈");
            championComments.put("Nidalee", "야생의 사냥꾼... 자유로운 영혼을 가지고 계시네요 🐆");
            championComments.put("Nilah", "기쁨의 전사... 항상 긍정적이시겠어요! 😊⚔️");
            championComments.put("Nocturne", "악몽의 화신... 꿈에서도 무서우실 것 같아요 👻");
            championComments.put("Nunu & Willump", "소년과 설인... 순수한 우정을 믿으시는군요! ⛄❄️");
            championComments.put("Olaf", "광전사의 힘! 열정적으로 사시는 분이시겠어요 ⚔️");
            championComments.put("Orianna", "태엽 인형... 정교함과 우아함을 동시에 가지고 계시네요 ⚙️💃");
            championComments.put("Ornn", "산의 화염... 장인정신이 뛰어나시겠어요 🔨🔥");
            championComments.put("Pantheon", "전쟁의 성좌... 불굴의 의지를 가지고 계시네요 🛡️⚔️");
            championComments.put("Poppy", "망치의 수호자... 겸손하지만 강인한 분이시군요 🔨💙");
            championComments.put("Pyke", "핏빛 항구의 도살자... 배신은 용서 못하시는 타입인가요? 🗡️🩸");
            championComments.put("Qiyana", "원소 여왕... 당당하고 자신감 넘치시겠어요 👑💎");
            championComments.put("Quinn", "데마시아의 날개... 자유롭게 날고 싶어 하시는군요 🦅");
            championComments.put("Rakan", "매혹적인 춤꾼... 화려한 무대를 좋아하시는군요 💃✨");
            championComments.put("Rammus", "갑옷을 두른 아르마딜로... 'OK' 🦔👍");
            championComments.put("Rek'Sai", "공허 여왕... 본능적인 사냥꾼이시군요 🦂");
            championComments.put("Rell", "철의 처녀... 강철 같은 의지를 가지고 계시네요 ⚔️🛡️");
            championComments.put("Renata Glasc", "화학 남작... 비즈니스 수완이 뛰어나시겠어요 💼⚗️");
            championComments.put("Renekton", "사막의 도살자... 격렬한 성격이시겠어요 🐊⚔️");
            championComments.put("Rengar", "사냥의 자긍심... 목표를 향한 집중력이 대단하시겠어요 🦁");
            championComments.put("Riven", "부러진 검... 과거를 딛고 일어서는 강인함이 있으시네요 ⚔️💔");
            championComments.put("Rumble", "기계광 요들... 엔지니어 기질이 있으시겠어요 🤖");
            championComments.put("Ryze", "룬 마법사... 고전적인 지혜를 가지고 계시군요 📜");
            championComments.put("Samira", "사막의 장미... 스타일리시한 액션을 좋아하시는군요 🌹🔫");
            championComments.put("Sejuani", "혹독한 추위의 전사... 강인한 리더십을 가지고 계시네요 🐗❄️");
            championComments.put("Senna", "구원자... 희망을 잃지 않는 분이시겠어요 🔫💡");
            championComments.put("Seraphine", "무대의 스타... 사람들을 하나로 만드는 매력이 있으시네요 🎵✨");
            championComments.put("Sett", "보스... 리더십과 주먹이 강하시겠어요 👊");
            championComments.put("Shaco", "광대 악마... 장난을 좋아하시는 위험한 분이군요 🤡💀");
            championComments.put("Shen", "균형의 눈... 조화로운 삶을 추구하시는군요 ⚖️🥷");
            championComments.put("Shyvana", "하프 드래곤... 이중적인 매력을 가지고 계시네요 🐉");
            championComments.put("Singed", "미친 화학자... 독특한 아이디어의 소유자시군요 ☠️");
            championComments.put("Sion", "언데드 거신... 불굴의 의지를 가지고 계시네요 💀");
            championComments.put("Sivir", "사막의 장미... 실용적이고 현실적이시겠어요 ⚔️💰");
            championComments.put("Skarner", "수정 전갈... 고향을 그리워하는 마음이 있으시겠어요 🦂💎");
            championComments.put("Smolder", "귀여운 어린 용... 앞으로 더 크게 성장하실 거예요! 🐲🔥");
            championComments.put("Sona", "현의 달인... 음악으로 마음을 움직이시는군요 🎵");
            championComments.put("Soraka", "소라카를 주로... 천사시네요! 현실에서도 착하실 것 같아요 😇");
            championComments.put("Swain", "녹서스의 지배자... 전략적 사고에 뛰어나시겠어요 🐦‍⬛");
            championComments.put("Sylas", "마법 도둑... 혁명가의 기질이 있으시군요 ⛓️");
            championComments.put("Syndra", "어둠 구체의 여왕... 강력한 자존감을 가지고 계시네요 ⚫");
            championComments.put("Tahm Kench", "강의 악마... 탐욕스러운 면이 있으시겠네요 👹");
            championComments.put("Taliyah", "바위 같이 단단함... 견고한 신념을 가지고 계시군요 🪨");
            championComments.put("Talon", "그림자 암살자... 조용하고 치명적이시겠어요 🗡️");
            championComments.put("Taric", "보석 기사... 아름다움을 추구하시는 분이시겠어요 💎✨");
            championComments.put("Teemo", "티모... 악마의 속삭임을 들으시는군요... 😈");
            championComments.put("Thresh", "감옥소장... 수집 욕구가 강하시겠어요 ⛓️👻");
            championComments.put("Tristana", "요들 포병... 작지만 강한 의지를 가지고 계시네요 💥");
            championComments.put("Trundle", "트롤 왕... 자신만의 영역을 중요시하시는군요 🧊👑");
            championComments.put("Tryndamere", "야만전사의 분노... 격렬한 전투를 좋아하시는군요 ⚔️💢");
            championComments.put("Twisted Fate", "카드의 달인... 운이 좋으시거나 실력이 뛰어나시거나 🃏");
            championComments.put("Twitch", "역병 쥐... 뒤에서 기회를 노리시는 타입이군요 🐀💀");
            championComments.put("Udyr", "정령 주술사... 야성적인 본능을 가지고 계시네요 🐻");
            championComments.put("Urgot", "공포의 기계... 무서운 외모 뒤에 슬픈 이야기가 있으실 것 같아요 🤖💀");
            championComments.put("Varus", "복수의 화살... 목표 달성력이 뛰어나시겠네요 🏹");
            championComments.put("Vayne", "밤의 사냥꾼... 정의감이 강한 외로운 늑대시군요 🏹🌙");
            championComments.put("Veigar", "작지만 강한 마법사! 알맹이가 꽉 찬 분이시군요 🔮");
            championComments.put("Vel'Koz", "지식을 추구하시는군요! 학구열이 대단하시겠어요 👁️");
            championComments.put("Vex", "우울한 요들... 세상이 다 귀찮으신가요? 😒💜");
            championComments.put("Vi", "필트오버의 집행관... 주먹으로 해결하는 스타일이시군요 👊💙");
            championComments.put("Viego", "몰락한 왕... 사랑에 목숨 거는 타입이시군요 👑💔");
            championComments.put("Viktor", "기계 진화... 완벽함을 추구하시는 분이시겠어요 🤖⚡");
            championComments.put("Vladimir", "혈액 마도사... 생명력이 넘치시겠어요 🩸");
            championComments.put("Volibear", "원시 폭풍... 자연의 힘을 가지고 계시네요 🐻⚡");
            championComments.put("Warwick", "자운의 분노... 본능에 충실하시겠어요 🐺");
            championComments.put("Wukong", "원숭이 왕... 장난기와 영리함을 모두 가지고 계시네요 🐒⚔️");
            championComments.put("Xayah", "반란군 소속... 자유를 위해 싸우는 정신이 있으시군요 🪶");
            championComments.put("Xerath", "마그너스의 승천자... 지식에 목마른 분이시군요 ⚡📚");
            championComments.put("Xin Zhao", "데마시아의 장군... 충성심이 강하시겠어요 🛡️");
            championComments.put("Yasuo", "바람검객... 자유로운 영혼이지만 무거운 짐을 지고 계시네요 🌪️⚔️");
            championComments.put("Yone", "잊혀진 자... 과거와 화해하려는 마음이 있으시군요 👻⚔️");
            championComments.put("Yorick", "무덤 파는 자... 외로움을 잘 견디시는 분이시겠어요 ⚰️");
            championComments.put("Yuumi", "마법 고양이... 누군가에게 의지하고 싶어 하시는군요 🐱✨");
            championComments.put("Zac", "비밀 무기... 유연한 사고방식을 가지고 계시네요 💚");
            championComments.put("Zed", "그림자의 주인... 쿨한 매력을 가지고 계시네요 🥷");
            championComments.put("Zeri", "번개 소녀... 에너지가 넘치시는 분이시겠어요! ⚡💨");
            championComments.put("Ziggs", "폭탄 전문가! 폭발적인 성격이시겠어요 💣");
            championComments.put("Zilean", "시간 마법사... 세월의 지혜를 가지고 계시네요 ⏰");
            championComments.put("Zoe", "우주의 전령... 천진난만한 면이 있으시겠어요 ✨");
            championComments.put("Zyra", "가시덩굴의 여왕... 자연의 야성적인 면을 가지고 계시네요 🌹🌿");

            return championComments.getOrDefault(championName, championName + " 장인이시군요! 멋있어요! 👍");
        } catch (Exception e) {
            return "챔피언 분석 중 오류가 발생했습니다.";
        }
    }

    private String generateRecentPerformanceComment(int wins, int totalGames) {
        try {
            if (totalGames <= 0) {
                return "최근 경기 기록이 없습니다.";
            }

            double winRate = (double) wins / totalGames;

            if (winRate >= 0.8) {
                return "최근 " + totalGames + "경기 중 " + wins + "승! 컨디션이 좋으시네요! 🔥";
            } else if (winRate >= 0.6) {
                return "최근 성과가 괜찮으시네요! 이 기세 유지하세요! 👍";
            } else {
                return "최근 경기가 아쉬우셨네요... 다음엔 더 잘하실 거예요! 💪";
            }
        } catch (Exception e) {
            return "최근 성과 분석 중 오류가 발생했습니다.";
        }
    }

    /**
     * 전역 예외 핸들러들
     */
    @ExceptionHandler(RuntimeException.class)
    public String handleRuntimeException(RuntimeException e, Model model) {
        System.err.println("RuntimeException 발생: " + e.getMessage());
        e.printStackTrace();
        model.addAttribute("error", e.getMessage());
        return "intro";
    }

    @ExceptionHandler(JsonProcessingException.class)
    public String handleJsonException(JsonProcessingException e, Model model) {
        System.err.println("JSON 파싱 오류: " + e.getMessage());
        model.addAttribute("error", "데이터 처리 중 오류가 발생했습니다. 사용자 이름과 태그를 확인해주세요.");
        return "intro";
    }

    @ExceptionHandler(Exception.class)
    public String handleGeneralException(Exception e, Model model) {
        System.err.println("예상치 못한 오류 발생: " + e.getMessage());
        e.printStackTrace();
        model.addAttribute("error", "서비스 이용 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
        return "intro";
    }
}