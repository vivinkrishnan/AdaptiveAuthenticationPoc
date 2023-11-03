package com.adaptive.springboot.adaptiveauthpoc.weight;

public enum Match {

    AppMatch(4),
    TimeOfLoginMatch(2),
    LocationMatch(6);

    private int weight;

    Match(){}
    
    Match(int weight){
        this.weight = weight;
    }

    public int getWeight(){
        return  this.weight;
    }
}
