package com.jin12.reviews_api.dto;

import lombok.Data;

@Data
public class AuthenticationRequest {
    private String username;
    private String password;
    private String apiKey;
    private String authType;
}