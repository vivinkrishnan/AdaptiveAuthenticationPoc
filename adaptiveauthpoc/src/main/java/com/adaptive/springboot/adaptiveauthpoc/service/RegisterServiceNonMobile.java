package com.adaptive.springboot.adaptiveauthpoc.service;

import java.util.Map;

import org.springframework.http.ResponseEntity;

public interface RegisterServiceNonMobile {
    
    public ResponseEntity<?> registerNonMobileUser(Map<String,Object> payload);

}
