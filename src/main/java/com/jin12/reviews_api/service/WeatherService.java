package com.jin12.reviews_api.service;

import com.jin12.reviews_api.dto.weatherService.Weather;
import com.jin12.reviews_api.dto.weatherService.WeatherRespons;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Random;

//https://northwaddleapp.niceisland-4f7272b3.northeurope.azurecontainerapps.io/swagger/index.html

@Service
public class WeatherService {
    private static final Dotenv dotenv = Dotenv.configure()
            .directory("./")
            .filename(".env")
            .load();
    private static final String WEATHER_API_URL = dotenv.get("WEATHER_API_URL");
    private static final String WEATHER_API_KEY = dotenv.get("WEATHER_API_KEY");
    private static final int SECONDS_BETWEEN_REQUESTS = 20;

    private static final Random random = new Random();

    private static Weather latestWeather;
    private static long latestWeatherTimestamp = 0;


    public Weather getWeather() {
        if (latestWeatherTimestamp + SECONDS_BETWEEN_REQUESTS > currentTimeSeconds() ) {
            return latestWeather;
        }

//        String url = WEATHER_API_URL + "?lat=" + 63.18 + "&lon=" + 14.64 + "&appid=" + WEATHER_API_KEY;
        String url = WEATHER_API_URL + "?lat=" + getLat() + "&lon=" + getLon() + "&appid=" + WEATHER_API_KEY;


        RestTemplate restTemplate = new RestTemplate();
        WeatherRespons weatherRespons = restTemplate.getForObject(url, WeatherRespons.class);
        assert weatherRespons != null;
        latestWeather = weatherRespons.getWeather()[0];
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

//    public static void main(String[] args) throws InterruptedException {
//        WeatherService weatherService = new WeatherService();
//        System.out.println(weatherService.getWeather());
//        System.out.println(weatherService.getWeather());
//        System.out.println(weatherService.getWeather());
//        Thread.sleep(SECONDS_BETWEEN_REQUESTS * 1000);
//        System.out.println(weatherService.getWeather());
//        System.out.println(weatherService.getWeather());
//        System.out.println(weatherService.getWeather());
//    }
}
