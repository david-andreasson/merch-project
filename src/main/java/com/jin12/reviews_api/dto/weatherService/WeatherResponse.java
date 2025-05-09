package com.jin12.reviews_api.dto.weatherService;

import lombok.Data;

@Data
public class WeatherResponse {
    Weather[] weather;
    String name;
    MainResponse main;

    @Override
    public String toString() {
        return weather[0].getDescription() + ", Temperature: " + (main.getTemp() - 273.15) + " degrees Celsius";
    }
}
