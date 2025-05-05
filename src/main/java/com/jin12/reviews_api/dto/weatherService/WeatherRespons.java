package com.jin12.reviews_api.dto.weatherService;

import lombok.Data;

@Data
public class WeatherRespons {
    Weather[] weather;
    String name;
}
