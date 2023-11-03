package com.adaptive.springboot.adaptiveauthpoc.service;

import java.util.Map;

import org.springframework.http.ResponseEntity;

public interface ResourceRequestService {
    
    public ResponseEntity<?> resourceRequest(Map<String,Object> payload);
    
}
