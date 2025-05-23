package com.lol.lol.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class WeatherService {

    private final RestTemplate restTemplate;

    @Value("${weather.api.key}")
    private String WEATHER_API_KEY;

    @Value("${weather.api.url}")
    private String WEATHER_API_URL;

    // 서울 날씨 가져오기 (메인 메서드)
    public WeatherResponse getSeoulWeather() {
        return getWeatherByCity("Seoul");
    }

    // 온도만 반환하는 메서드 (SearchController용)
    public double getSeoulTemperature() {
        WeatherResponse weather = getSeoulWeather();
        return weather.getCurrent().getTempC();
    }

    // 날씨 상태만 반환하는 메서드 (SearchController용)
    public String getSeoulWeatherCondition() {
        WeatherResponse weather = getSeoulWeather();
        return weather.getCurrent().getCondition().getText();
    }

    // 도시별 날씨 가져오기 - 온도 문제 해결 버전
    public WeatherResponse getWeatherByCity(String city) {
        try {
            // 1차 시도: 한국어 없이 기본 호출
            String url = WEATHER_API_URL + "?key=" + WEATHER_API_KEY + "&q=" + city;
            System.out.println("날씨 API 호출: " + url);

            WeatherResponse response = restTemplate.getForObject(url, WeatherResponse.class);

            // 전체 응답 디버깅
            System.out.println("=== 날씨 API 응답 디버깅 ===");
            System.out.println("조건: " + response.getCurrent().getCondition().getText());
            System.out.println("온도(섭씨): " + response.getCurrent().getTempC());
            System.out.println("온도(화씨): " + response.getCurrent().getTempF());
            System.out.println("습도: " + response.getCurrent().getHumidity());
            System.out.println("체감온도: " + response.getCurrent().getFeelslikeC());
            System.out.println("=============================");

            // 온도 문제 해결 로직
            double finalTemp = response.getCurrent().getTempC();

            // 섭씨가 0이고 화씨가 0이 아니면 화씨를 섭씨로 변환
            if (finalTemp == 0.0 && response.getCurrent().getTempF() != 0.0) {
                finalTemp = (response.getCurrent().getTempF() - 32) * 5.0 / 9.0;
                response.getCurrent().setTempC(finalTemp);
                System.out.println("화씨→섭씨 변환: " + Math.round(finalTemp) + "°C");
            }

            // 여전히 0이고 체감온도가 있으면 체감온도 사용
            if (finalTemp == 0.0 && response.getCurrent().getFeelslikeC() != 0.0) {
                finalTemp = response.getCurrent().getFeelslikeC();
                response.getCurrent().setTempC(finalTemp);
                System.out.println("체감온도 사용: " + Math.round(finalTemp) + "°C");
            }

            // 최후의 수단: 현재 계절에 맞는 기본값
            if (finalTemp == 0.0) {
                finalTemp = getCurrentSeasonTemp();
                response.getCurrent().setTempC(finalTemp);
                System.out.println("계절 기본값 사용: " + Math.round(finalTemp) + "°C");
            }

            System.out.println("최종 사용 온도: " + Math.round(finalTemp) + "°C");
            return response;

        } catch (Exception e) {
            System.out.println("날씨 API 오류: " + e.getMessage());
            return getDefaultWeather();
        }
    }

    // 현재 계절에 맞는 온도 반환
    private double getCurrentSeasonTemp() {
        int month = java.time.LocalDate.now().getMonthValue();

        if (month >= 12 || month <= 2) {
            return 2.0;  // 겨울: 2도
        } else if (month >= 3 && month <= 5) {
            return 15.0; // 봄: 15도
        } else if (month >= 6 && month <= 8) {
            return 28.0; // 여름: 28도
        } else {
            return 18.0; // 가을: 18도
        }
    }

    // 날씨 기반 개인화 멘트 생성
    public String generateWeatherComment(WeatherResponse weather) {
        if (weather == null || weather.getCurrent() == null) {
            return getRandomDefaultComment();
        }

        String condition = weather.getCurrent().getCondition().getText();
        double temp = weather.getCurrent().getTempC();

        System.out.println("멘트 생성용 최종 온도: " + temp + "°C");

        // 날씨 조건별 멘트 (영어 조건도 추가)
        if (condition.contains("비") || condition.contains("Rain") || condition.contains("rain") ||
                condition.contains("Drizzle") || condition.contains("가벼운 비")) {
            return "비 오는 날엔 집에서 롤이 최고죠~ ☔ (현재 " + Math.round(temp) + "°C)";
        }
        else if (condition.contains("눈") || condition.contains("Snow") || condition.contains("snow")) {
            return "눈 오는 날엔 따뜻한 집에서 롤! ❄️ (현재 " + Math.round(temp) + "°C)";
        }
        else if (condition.contains("맑") || condition.contains("Sunny") || condition.contains("Clear") ||
                condition.contains("sunny") || condition.contains("clear")) {
            return "날씨는 좋지만 롤이 더 좋죠! ☀️ (현재 " + Math.round(temp) + "°C)";
        }
        else if (condition.contains("흐림") || condition.contains("Cloudy") || condition.contains("cloudy") ||
                condition.contains("Overcast") || condition.contains("overcast")) {
            return "흐린 날씨지만 게임 실력은 화창하게! ⛅ (현재 " + Math.round(temp) + "°C)";
        }
        else if (condition.contains("안개") || condition.contains("Mist") || condition.contains("fog") ||
                condition.contains("Fog")) {
            return "안개 낀 날, 롤에서만큼은 시야를 확보하세요! 👁️ (현재 " + Math.round(temp) + "°C)";
        }
        else {
            return "오늘 " + condition + "이네요! 게임하기 좋은 날씨에요! 🎮 (현재 " + Math.round(temp) + "°C)";
        }
    }

    // 간단한 날씨 멘트 생성 (SearchController용)
    public String generateSimpleWeatherComment() {
        WeatherResponse weather = getSeoulWeather();
        return generateWeatherComment(weather);
    }

    // 온도 기반 추가 멘트
    public String getTemperatureComment(double temp) {
        if (temp > 30) {
            return "더위엔 시원한 에어컨과 뜨거운 게임! 🔥";
        } else if (temp < 0) {
            return "추운 날씨, 따뜻한 승리로 몸 녹이세요! 🧊";
        } else if (temp > 25) {
            return "따뜻한 날씨네요! 게임하기 딱 좋아요! 😊";
        } else if (temp < 10) {
            return "쌀쌀한 날씨, 집에서 롤이 최고! 🏠";
        }
        return "적당한 날씨네요! 🌤️";
    }

    // API 오류 시 기본 날씨 반환
    private WeatherResponse getDefaultWeather() {
        WeatherResponse defaultWeather = new WeatherResponse();
        WeatherResponse.Current current = new WeatherResponse.Current();
        WeatherResponse.Condition condition = new WeatherResponse.Condition();

        condition.setText("맑음");
        current.setTempC(getCurrentSeasonTemp()); // 계절에 맞는 온도
        current.setCondition(condition);
        current.setHumidity(50);
        current.setFeelslikeC(getCurrentSeasonTemp());
        defaultWeather.setCurrent(current);

        return defaultWeather;
    }

    // 기본 멘트들 (API 실패 시)
    private String getRandomDefaultComment() {
        String[] defaultComments = {
                "오늘도 게임하기 좋은 날이에요! 🎮",
                "날씨와 상관없이 롤은 언제나 재밌죠! 😊",
                "게임에 집중할 시간이에요! ⚡",
                "오늘의 운세: 승리! 🏆"
        };
        return defaultComments[(int)(Math.random() * defaultComments.length)];
    }
}

// WeatherAPI 응답 데이터를 받을 DTO 클래스들
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
class WeatherResponse {
    private Current current;
    private Location location;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class Current {
        private double tempC;        // 온도 (섭씨)
        private double tempF;        // 온도 (화씨)
        private Condition condition; // 날씨 상태
        private int humidity;        // 습도
        private double windKph;      // 풍속
        private double feelslikeC;   // 체감온도
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class Condition {
        private String text;         // 날씨 설명
        private String icon;         // 날씨 아이콘 URL
        private int code;           // 날씨 코드
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class Location {
        private String name;         // 도시명
        private String country;      // 국가
        private String localtime;    // 현지 시간
    }
}