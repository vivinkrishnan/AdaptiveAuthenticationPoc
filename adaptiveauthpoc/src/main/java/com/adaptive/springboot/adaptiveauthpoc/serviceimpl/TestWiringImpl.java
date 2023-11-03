package com.adaptive.springboot.adaptiveauthpoc.serviceimpl;

import org.springframework.stereotype.Service;

import com.adaptive.springboot.adaptiveauthpoc.interfaces.TestWiring;

@Service
public class TestWiringImpl implements TestWiring{

    @Override
    public String getStringFrom() {
        return "Hello from Spring Boot Wiring";
    }
    
}
