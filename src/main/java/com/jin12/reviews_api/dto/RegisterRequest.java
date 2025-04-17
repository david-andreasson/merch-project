package com.jin12.reviews_api.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    private String username;
    private String password;
    private String authType;
}