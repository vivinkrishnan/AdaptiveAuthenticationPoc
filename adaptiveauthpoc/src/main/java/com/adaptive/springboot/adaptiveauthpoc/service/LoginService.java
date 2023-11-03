package com.adaptive.springboot.adaptiveauthpoc.service;

import java.util.Map;

import org.springframework.http.ResponseEntity;

public interface LoginService {

    public ResponseEntity<?> loginUser(Map<String,Object> payload);
    
}
