package com.adaptive.springboot.adaptiveauthpoc.service;

import java.util.Map;

import org.springframework.http.ResponseEntity;

public interface RegisterService {

    public ResponseEntity<?> registerUser(Map<String,Object> payload);
    
}
