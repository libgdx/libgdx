package com.mojang.metagun;

import com.badlogic.gdx.Gdx;

public class Sound {
    
    public static com.badlogic.gdx.audio.Sound boom;
    public static com.badlogic.gdx.audio.Sound hit;
    public static com.badlogic.gdx.audio.Sound splat;
    public static com.badlogic.gdx.audio.Sound launch;
    public static com.badlogic.gdx.audio.Sound pew;
    public static com.badlogic.gdx.audio.Sound oof;
    public static com.badlogic.gdx.audio.Sound gethat;
    public static com.badlogic.gdx.audio.Sound death;
    public static com.badlogic.gdx.audio.Sound startgame;
    public static com.badlogic.gdx.audio.Sound jump;

    public static void load() {
   	 boom = load("res/boom.wav");
   	 hit = load("res/hit.wav");
   	 splat = load("res/splat.wav");
   	 launch = load("res/launch.wav");
   	 pew = load("res/pew.wav");
   	 oof = load("res/oof.wav");
   	 gethat = load("res/gethat.wav");
   	 death = load("res/death.wav");
   	 startgame = load("res/startgame.wav");
   	 jump = load("res/jump.wav");
    }
    
    private static com.badlogic.gdx.audio.Sound load(String name) {
   	 return Gdx.audio.newSound(Gdx.files.internal(name));
    }       
}
