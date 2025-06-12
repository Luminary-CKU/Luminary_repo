package com.lol.lol.dto;

import java.util.List;

public class MatchDto {
    private MetadataDto metadata;
    private InfoDto info;

    // 기본 생성자
    public MatchDto() {}

    // Getters and Setters
    public MetadataDto getMetadata() {
        return metadata;
    }

    public void setMetadata(MetadataDto metadata) {
        this.metadata = metadata;
    }

    public InfoDto getInfo() {
        return info;
    }

    public void setInfo(InfoDto info) {
        this.info = info;
    }

    // 메타데이터 클래스
    public static class MetadataDto {
        private String dataVersion;
        private String matchId;
        private List<String> participants;

        // Getters and Setters
        public String getDataVersion() {
            return dataVersion;
        }

        public void setDataVersion(String dataVersion) {
            this.dataVersion = dataVersion;
        }

        public String getMatchId() {
            return matchId;
        }

        public void setMatchId(String matchId) {
            this.matchId = matchId;
        }

        public List<String> getParticipants() {
            return participants;
        }

        public void setParticipants(List<String> participants) {
            this.participants = participants;
        }
    }

    // 게임 정보 클래스
    public static class InfoDto {
        private long gameCreation;
        private long gameDuration;
        private long gameEndTimestamp;
        private long gameId;
        private String gameMode;
        private String gameName;
        private long gameStartTimestamp;
        private String gameType;
        private String gameVersion;
        private int mapId;
        private List<ParticipantDto> participants;
        private String platformId;
        private int queueId;
        private List<TeamDto> teams;
        private String tournamentCode;

        // Getters and Setters
        public long getGameCreation() {
            return gameCreation;
        }

        public void setGameCreation(long gameCreation) {
            this.gameCreation = gameCreation;
        }

        public long getGameDuration() {
            return gameDuration;
        }

        public void setGameDuration(long gameDuration) {
            this.gameDuration = gameDuration;
        }

        public long getGameEndTimestamp() {
            return gameEndTimestamp;
        }

        public void setGameEndTimestamp(long gameEndTimestamp) {
            this.gameEndTimestamp = gameEndTimestamp;
        }

        public long getGameId() {
            return gameId;
        }

        public void setGameId(long gameId) {
            this.gameId = gameId;
        }

        public String getGameMode() {
            return gameMode;
        }

        public void setGameMode(String gameMode) {
            this.gameMode = gameMode;
        }

        public String getGameName() {
            return gameName;
        }

        public void setGameName(String gameName) {
            this.gameName = gameName;
        }

        public long getGameStartTimestamp() {
            return gameStartTimestamp;
        }

        public void setGameStartTimestamp(long gameStartTimestamp) {
            this.gameStartTimestamp = gameStartTimestamp;
        }

        public String getGameType() {
            return gameType;
        }

        public void setGameType(String gameType) {
            this.gameType = gameType;
        }

        public String getGameVersion() {
            return gameVersion;
        }

        public void setGameVersion(String gameVersion) {
            this.gameVersion = gameVersion;
        }

        public int getMapId() {
            return mapId;
        }

        public void setMapId(int mapId) {
            this.mapId = mapId;
        }

        public List<ParticipantDto> getParticipants() {
            return participants;
        }

        public void setParticipants(List<ParticipantDto> participants) {
            this.participants = participants;
        }

        public String getPlatformId() {
            return platformId;
        }

        public void setPlatformId(String platformId) {
            this.platformId = platformId;
        }

        public int getQueueId() {
            return queueId;
        }

        public void setQueueId(int queueId) {
            this.queueId = queueId;
        }

        public List<TeamDto> getTeams() {
            return teams;
        }

        public void setTeams(List<TeamDto> teams) {
            this.teams = teams;
        }

        public String getTournamentCode() {
            return tournamentCode;
        }

        public void setTournamentCode(String tournamentCode) {
            this.tournamentCode = tournamentCode;
        }
    }

    // 매치 분석 메서드 추가 (팀원들이 사용하는 형태)
    public MatchAnalysis analyzeMatch(String playerName) {
        MatchAnalysis analysis = new MatchAnalysis();
        analysis.setMatchId(metadata != null ? metadata.getMatchId() : "unknown");

        if (info == null || info.getParticipants() == null) {
            analysis.setFound(false);
            return analysis;
        }

        // 플레이어 찾기
        ParticipantDto player = info.getParticipants().stream()
                .filter(p -> playerName.equals(p.getRiotIdGameName()))
                .findFirst()
                .orElse(null);

        if (player == null) {
            analysis.setFound(false);
            return analysis;
        }

        // 플레이어 데이터 설정
        analysis.setFound(true);
        analysis.setKills(player.getKills());
        analysis.setDeaths(player.getDeaths());
        analysis.setAssists(player.getAssists());
        analysis.setWin(player.isWin());
        analysis.setChampionName(player.getChampionName());

        double kda = player.getDeaths() > 0 ?
                (double)(player.getKills() + player.getAssists()) / player.getDeaths() :
                player.getKills() + player.getAssists();
        analysis.setKda(kda);

        return analysis;
    }

    // 매치 분석 결과 클래스
    public static class MatchAnalysis {
        private String matchId;
        private boolean found;
        private int kills;
        private int deaths;
        private int assists;
        private double kda;
        private boolean win;
        private String championName;

        public MatchAnalysis() {}

        // Getters and Setters
        public String getMatchId() { return matchId; }
        public void setMatchId(String matchId) { this.matchId = matchId; }

        public boolean isFound() { return found; }
        public void setFound(boolean found) { this.found = found; }

        public int getKills() { return kills; }
        public void setKills(int kills) { this.kills = kills; }

        public int getDeaths() { return deaths; }
        public void setDeaths(int deaths) { this.deaths = deaths; }

        public int getAssists() { return assists; }
        public void setAssists(int assists) { this.assists = assists; }

        public double getKda() { return kda; }
        public void setKda(double kda) { this.kda = kda; }

        public boolean isWin() { return win; }
        public void setWin(boolean win) { this.win = win; }

        public String getChampionName() { return championName; }
        public void setChampionName(String championName) { this.championName = championName; }
    }
}