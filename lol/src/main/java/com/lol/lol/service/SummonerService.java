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
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SummonerService {

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

    /**
     * 계정 정보 조회 - API 호출 예외, URL 인코딩 예외 처리
     */
    public String getAccountUrl(String gameName, String tagLine) throws RuntimeException {
        try {
            // 입력값 검증
            if (gameName == null || gameName.trim().isEmpty()) {
                throw new IllegalArgumentException("게임 이름이 비어있습니다.");
            }
            if (tagLine == null || tagLine.trim().isEmpty()) {
                throw new IllegalArgumentException("태그가 비어있습니다.");
            }

            // URL 인코딩 예외 처리
            String decodedGameName = URLDecoder.decode(gameName, StandardCharsets.UTF_8.toString());
            String decodedTagLine = URLDecoder.decode(tagLine, StandardCharsets.UTF_8.toString());

            String accountUrl = API_USER + decodedGameName + "/" + decodedTagLine + "?api_key=" + API_KEY;
            System.out.println("Request URL: " + accountUrl);

            // API 호출 예외 처리
            String result = restTemplate.getForObject(accountUrl, String.class);

            // 응답 데이터 검증
            if (result == null || result.trim().isEmpty()) {
                throw new RuntimeException("API 응답이 비어있습니다.");
            }

            // 에러 응답 확인
            if (result.contains("\"status\"")) {
                System.err.println("API 에러 응답: " + result);
                throw new RuntimeException("사용자를 찾을 수 없습니다. 이름과 태그를 확인해주세요.");
            }

            System.out.println("Result: " + result);
            return result;

        } catch (UnsupportedEncodingException e) {
            System.err.println("URL 디코딩 오류: " + e.getMessage());
            throw new RuntimeException("잘못된 사용자 이름 형식입니다.", e);
        } catch (HttpClientErrorException e) {
            System.err.println("클라이언트 오류 (4xx): " + e.getStatusCode() + " - " + e.getMessage());
            if (e.getStatusCode().value() == 404) {
                throw new RuntimeException("사용자를 찾을 수 없습니다. 이름과 태그를 확인해주세요.");
            } else if (e.getStatusCode().value() == 403) {
                throw new RuntimeException("API 키가 유효하지 않습니다.");
            } else {
                throw new RuntimeException("잘못된 요청입니다.");
            }
        } catch (HttpServerErrorException e) {
            System.err.println("서버 오류 (5xx): " + e.getStatusCode() + " - " + e.getMessage());
            throw new RuntimeException("서버에 일시적인 문제가 발생했습니다. 잠시 후 다시 시도해주세요.");
        } catch (ResourceAccessException e) {
            System.err.println("네트워크 연결 오류: " + e.getMessage());
            throw new RuntimeException("네트워크 연결에 문제가 있습니다. 인터넷 연결을 확인해주세요.");
        } catch (IllegalArgumentException e) {
            throw e; // 입력값 검증 예외는 그대로 전달
        } catch (Exception e) {
            System.err.println("계정 정보 조회 중 예상치 못한 오류: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("계정 정보를 가져오는 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 소환사 정보 조회 - API 호출 예외 처리
     */
    public String getSummoner(String puuid) throws RuntimeException {
        try {
            // 입력값 검증
            if (puuid == null || puuid.trim().isEmpty()) {
                throw new IllegalArgumentException("PUUID가 비어있습니다.");
            }

            String summoner = API_SUMMONER + puuid + "?api_key=" + API_KEY;
            System.out.println("Summoner API URL: " + summoner);

            String result = restTemplate.getForObject(summoner, String.class);

            if (result == null || result.trim().isEmpty()) {
                throw new RuntimeException("소환사 정보 응답이 비어있습니다.");
            }

            System.out.println("Get Summoner Result: " + result);
            return result;

        } catch (HttpClientErrorException e) {
            System.err.println("소환사 정보 조회 클라이언트 오류: " + e.getStatusCode());
            throw new RuntimeException("소환사 정보를 찾을 수 없습니다.");
        } catch (HttpServerErrorException e) {
            System.err.println("소환사 정보 조회 서버 오류: " + e.getStatusCode());
            throw new RuntimeException("서버에 일시적인 문제가 발생했습니다.");
        } catch (ResourceAccessException e) {
            System.err.println("소환사 정보 조회 네트워크 오류: " + e.getMessage());
            throw new RuntimeException("네트워크 연결에 문제가 있습니다.");
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            System.err.println("소환사 정보 조회 중 예상치 못한 오류: " + e.getMessage());
            throw new RuntimeException("소환사 정보를 가져오는 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 리그 포인트 조회 - JSON 파싱 예외, API 호출 예외 처리
     */
    public List<LeagueDto> getLeaguePoint(String id) {
        try {
            // 입력값 검증
            if (id == null || id.trim().isEmpty()) {
                System.out.println("소환사 ID가 비어있어 빈 리그 정보를 반환합니다.");
                return new ArrayList<>();
            }

            String url = API_LEAGUE_URL + id + "?api_key=" + API_KEY;
            System.out.println("League API URL: " + url);

            String result = restTemplate.getForObject(url, String.class);
            System.out.println("LeagueResult: " + result);

            List<LeagueDto> leagueDtoList = new ArrayList<>();

            // JSON 파싱 예외 처리
            if (result != null && !result.isEmpty() && !result.equals("[]")) {
                try {
                    leagueDtoList = new ObjectMapper().readValue(result, new TypeReference<List<LeagueDto>>() {});
                } catch (JsonProcessingException e) {
                    System.err.println("리그 정보 JSON 파싱 오류: " + e.getMessage());
                    System.out.println("파싱 실패한 JSON: " + result);
                    // 파싱 실패 시 빈 리스트 반환 (언랭으로 처리)
                    return new ArrayList<>();
                }
            }

            return leagueDtoList;

        } catch (HttpClientErrorException e) {
            System.err.println("리그 정보 조회 클라이언트 오류: " + e.getStatusCode());
            return new ArrayList<>(); // 언랭으로 처리
        } catch (HttpServerErrorException e) {
            System.err.println("리그 정보 조회 서버 오류: " + e.getStatusCode());
            return new ArrayList<>();
        } catch (ResourceAccessException e) {
            System.err.println("리그 정보 조회 네트워크 오류: " + e.getMessage());
            return new ArrayList<>();
        } catch (Exception e) {
            System.err.println("리그 정보 조회 중 예상치 못한 오류: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * 매치 히스토리 조회 - API 호출 예외 처리
     */
    public List<String> getMatches(String puuid) {
        try {
            if (puuid == null || puuid.trim().isEmpty()) {
                System.out.println("PUUID가 비어있어 빈 매치 리스트를 반환합니다.");
                return new ArrayList<>();
            }

            String url = API_MATCHES + "/" + puuid + "/ids?start=0&count=20&api_key=" + API_KEY;
            System.out.println("Matches API URL: " + url);

            List<String> matches = restTemplate.getForObject(url, List.class);

            return matches != null ? matches : new ArrayList<>();

        } catch (HttpClientErrorException e) {
            System.err.println("매치 히스토리 조회 클라이언트 오류: " + e.getStatusCode());
            return new ArrayList<>();
        } catch (HttpServerErrorException e) {
            System.err.println("매치 히스토리 조회 서버 오류: " + e.getStatusCode());
            return new ArrayList<>();
        } catch (ResourceAccessException e) {
            System.err.println("매치 히스토리 조회 네트워크 오류: " + e.getMessage());
            return new ArrayList<>();
        } catch (Exception e) {
            System.err.println("매치 히스토리 조회 중 예상치 못한 오류: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * 매치 히스토리 조회 (별칭)
     */
    public List<String> getMatchHistory(String puuid) {
        return getMatches(puuid);
    }

    /**
     * 매치 상세 정보 조회 - API 호출 예외 처리
     */
    public MatchDto getMatchDetails(String matchId) {
        try {
            if (matchId == null || matchId.trim().isEmpty()) {
                System.out.println("매치 ID가 비어있습니다.");
                return null;
            }

            String url = API_MATCHDETAIL + matchId + "?api_key=" + API_KEY;
            System.out.println("Match Detail API URL: " + url);

            MatchDto matchDto = restTemplate.getForObject(url, MatchDto.class);

            // 매치 데이터 검증
            if (matchDto == null) {
                System.out.println("매치 상세 정보가 null입니다. (ID: " + matchId + ")");
                return null;
            }

            return matchDto;

        } catch (HttpClientErrorException e) {
            System.err.println("매치 상세 정보 조회 클라이언트 오류 (ID: " + matchId + "): " + e.getStatusCode());
            return null; // null 반환하여 컨트롤러에서 필터링
        } catch (HttpServerErrorException e) {
            System.err.println("매치 상세 정보 조회 서버 오류 (ID: " + matchId + "): " + e.getStatusCode());
            return null;
        } catch (ResourceAccessException e) {
            System.err.println("매치 상세 정보 조회 네트워크 오류 (ID: " + matchId + "): " + e.getMessage());
            return null;
        } catch (Exception e) {
            System.err.println("매치 상세 정보 조회 중 예상치 못한 오류 (ID: " + matchId + "): " + e.getMessage());
            return null;
        }
    }

    /**
     * 최신 버전 조회 - API 호출 예외 처리
     */
    public String getLatestVersion() {
        try {
            String url = "https://ddragon.leagueoflegends.com/api/versions.json";
            System.out.println("Version API URL: " + url);

            List<String> versions = restTemplate.getForObject(url, List.class);

            if (versions != null && !versions.isEmpty()) {
                String latestVersion = versions.get(0);
                System.out.println("최신 버전: " + latestVersion);
                return latestVersion;
            } else {
                System.out.println("버전 정보가 비어있습니다. 기본값 사용.");
                return "14.23.1"; // 기본값
            }

        } catch (HttpClientErrorException e) {
            System.err.println("버전 정보 조회 클라이언트 오류: " + e.getStatusCode());
            return "14.23.1";
        } catch (HttpServerErrorException e) {
            System.err.println("버전 정보 조회 서버 오류: " + e.getStatusCode());
            return "14.23.1";
        } catch (ResourceAccessException e) {
            System.err.println("버전 정보 조회 네트워크 오류: " + e.getMessage());
            return "14.23.1";
        } catch (Exception e) {
            System.err.println("버전 정보 조회 중 예상치 못한 오류: " + e.getMessage());
            return "14.23.1";
        }
    }
}