package com.jin12.reviews_api.service;

import com.jin12.reviews_api.dto.weatherService.WeatherResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Random;

@Service
public class WeatherService {

    private RestTemplate restTemplate;

    @Value("${WEATHER_API_URL}")
    private String WEATHER_API_URL;

    @Value("${WEATHER_API_KEY}")
    private String WEATHER_API_KEY;
    private static final int SECONDS_BETWEEN_REQUESTS = 3;

    private static final Random random = new Random();

    private static String latestWeather;
    private static long latestWeatherTimestamp = 0;

    public WeatherService() {
        restTemplate = new RestTemplate();
    }
     public WeatherService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
     }

    public String getWeather() {
        //Limit amount of calls to API
        if (latestWeatherTimestamp + SECONDS_BETWEEN_REQUESTS > currentTimeSeconds() ) {
            return latestWeather;
        }

        String url = WEATHER_API_URL + "?lat=" + getLat() + "&lon=" + getLon() + "&appid=" + WEATHER_API_KEY;


        WeatherResponse weatherResponse = restTemplate.getForObject(url, WeatherResponse.class);
        if (weatherResponse == null) {
            return latestWeather;
        }
        latestWeather = weatherResponse.toString();

        latestWeatherTimestamp = currentTimeSeconds();

        return latestWeather;
    }

    private double getLat() {
        return random.nextDouble(-90, 90);
    }

    private double getLon() {
        return random.nextDouble(-180, 180);
    }

    private long currentTimeSeconds() {
        return System.currentTimeMillis() / 1000;
    }
}
