package com.jin12.reviews_api.service;

import com.jin12.reviews_api.dto.weatherService.MainResponse;
import com.jin12.reviews_api.dto.weatherService.Weather;
import com.jin12.reviews_api.dto.weatherService.WeatherResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class WeatherServiceTest {

    private WeatherService weatherService;
    private RestTemplate restTemplate;

    @BeforeEach
    void setUp() {
        weatherService = new WeatherService();

        // Inject mock RestTemplate
        restTemplate = mock(RestTemplate.class);
        ReflectionTestUtils.setField(weatherService, "restTemplate", restTemplate);

        // Inject dummy API URL and key
        ReflectionTestUtils.setField(weatherService, "WEATHER_API_URL", "https://mock-api.com/weather");
        ReflectionTestUtils.setField(weatherService, "WEATHER_API_KEY", "dummy-key");

        // Reset static fields (needed for clean state)
        ReflectionTestUtils.setField(WeatherService.class, "latestWeather", null);
        ReflectionTestUtils.setField(WeatherService.class, "latestWeatherTimestamp", 0L);
    }

    @Test
    void testGetWeather_ReturnsWeatherDescription() {
        WeatherResponse mockResponse = new WeatherResponse();
        Weather mockWeather = new Weather();
        mockWeather.setDescription("Clear sky");

        MainResponse mockMain = new MainResponse();
        mockMain.setTemp(293.15); // 20°C

        mockResponse.setWeather(new Weather[]{mockWeather});
        mockResponse.setMain(mockMain);

        when(restTemplate.getForObject(anyString(), eq(WeatherResponse.class)))
                .thenReturn(mockResponse);

        String result = weatherService.getWeather();

        assertNotNull(result);
        assertTrue(result.contains("Clear sky"));
        assertTrue(result.contains("Temperature"));
    }

    @Test
    void testGetWeather_ReturnsCachedValueWhenCalledTooSoon() {
        // First call - returns real value
        WeatherResponse mockResponse = new WeatherResponse();
        Weather mockWeather = new Weather();
        mockWeather.setDescription("Sunny");

        MainResponse mockMain = new MainResponse();
        mockMain.setTemp(300.15); // 27°C

        mockResponse.setWeather(new Weather[]{mockWeather});
        mockResponse.setMain(mockMain);

        when(restTemplate.getForObject(anyString(), eq(WeatherResponse.class)))
                .thenReturn(mockResponse);

        String firstCall = weatherService.getWeather();
        String secondCall = weatherService.getWeather(); // Should return cached value

        // Same value since second call is within cache timeout
        assertEquals(firstCall, secondCall);
        verify(restTemplate, times(1)).getForObject(anyString(), eq(WeatherResponse.class));
    }

    @Test
    void testGetWeather_ReturnsNullIfApiReturnsNull() {
        when(restTemplate.getForObject(anyString(), eq(WeatherResponse.class)))
                .thenReturn(null);

        String result = weatherService.getWeather();

        // Null means fallback to latestWeather, which should be null in this case
        assertNull(result);
    }

    @Test
    void testWeatherToStringFormatting() {
        WeatherResponse response = new WeatherResponse();

        Weather w = new Weather();
        w.setDescription("Cloudy");
        response.setWeather(new Weather[]{w});

        MainResponse m = new MainResponse();
        m.setTemp(280.15); // 7°C
        response.setMain(m);

        String formatted = response.toString();

        assertEquals("Cloudy, Temperature: 7.0 degrees Celsius", formatted);
    }
}
