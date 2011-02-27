package com.badlogicgames.superjumper;


public class SuperJumper extends Game {
    boolean firstTimeCreate = true;
    
    @Override
    public Screen getStartScreen() {
        return new MainMenuScreen(this);
    }
    
    @Override
    public void create() {                         
         Settings.load();
         Assets.load();
         super.create();
    }            
}