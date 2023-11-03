package com.adaptive.springboot.adaptiveauthpoc.beans;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;

@Component
public class PostConstructBean {
    
    
    
    @PostConstruct
    public void init() {
        //LOG.info(Arrays.asList(environment.getDefaultProfiles()));

        //System.out.println(">>>>>>>>>");
        //AuthMethods.stream().forEach(wt -> System.out.println(wt + "--" + wt.getStrength()));
    }
}
 