package com.mojang.metagun.screen;

import com.badlogic.gdx.Gdx;
import com.mojang.metagun.Art;
import com.mojang.metagun.Input;

public class SignReadScreen extends Screen {
    private Screen parent;
    
    private String[][] signs = {
            {
                "READING",
                "", 
                "PRESS UP TO READ SIGNS"
            },
            {
                "JUMPING",
                "", 
                "PRESS Z TO JUMP",
                "YOU CAN JUMP HIGHER BY",
                "GETTING A RUNNING START",
                "OR HOLDING DOWN Z",
            },
            {
                "PROGRESSING",
                "", 
                "LEAVE A ROOM THROUGH ANY",
                "EXIT TO CONTINUE YOUR",
                "ADVENTURE",
            },
            {
                "DYING",
                "", 
                "IF YOU DIE, YOU RESTART",
                "AT THE BEGINNING OF THE",
                "CURRENT ROOM",
            },
            {
                "DODGING",
                "", 
                "THE GUNNERS DON'T LIKE YOU",
                "AND SHOOT AT YOU.",
                "IT WOULD BE WISE TO STAY AWAY",
            },    
            {
                "THE LAUNCHER",
                "", 
                "AS YOU PICK UP THE LAUNCHER,",
                "YOU REALIZE IT'S NOT YOUR",
                "AVERAGE LAUNCHER.",
                "",
                "PRESS UP AND DOWN TO AIM",
                "PRESS X TO FIRE THE LAUNCHER",
            },      
            {
                "JONESING",
                "", 
                "DON'T FORGET YOUR FEDORA!",
            },
            {
                "EXPLODING",
                "", 
                "TNT BLOCKS ARE HIGHLY",
                "EXPLOSIVE, AND WILL",
                "REACT POORLY TO BEING",
                "SHOT.",
            },              
            {
                "PUSHING",
                "", 
                "THE CAMARADERIE BOX IS",
                "SOMETHING SOMETHING",
                "",
                "IT'S FROM PORTAL.",
            },              
            {
                "BATTLING",
                "", 
                "THE GREMLIN IS LARGE",
                "AND IN YOUR WAY.",
                "OVERHEAT IT TO DESTROY",
                "IT AND CLAIM YOUR PRIZE",
            },      
            {
                "EVADING",
                "", 
                "THE GUNNERS SHOTS WILL",
                "PASS THROUGH GLASS.",
                "YOU, HOWEVER, WILL NOT",
            },         
            {
                "SWEATING",
                "", 
                "THESE SLIGHTLY MORE",
                "SOPHISTICATED GREMLINS",
                "HAVE LEARNED A NEW",
                "TRICK",
            },
            {
                "CONVEYING",
                "", 
                "TIME TO BURN OFF SOME",
                "FAT AND HAVE FUN WHILE",
                "DOING IT!",
            },          
            {
                "BOSSFIGHTING",
                "", 
                "BEHIND THIS DOOR, MEGAN",
                "AWAITS! WHO IS MEGAN?",
                "ARE YOU MEGAN?",
            },            
            {
                "THE NEW LAUNCHER",
                "",
                "WELL, THIS IS BAD."
            },               
            {
                "FEEDING",
                "",
                "THE JABBERWOCKY IS",
                "HUNGRY, AND WILL EAT",
                "WAY MORE THAN IT SHOULD",
                "",
                "PLEASE DO NOT FEED!",
            },               
            {
                "HOVERING",
                "",
                "THE RECOIL ON THE NEW",
                "LAUNCHER SURE IS",
                "POWERFUL!",
            },
            {
                "FLYING",
                "",
                "SERIOUSLY, THE RECOIL",
                "IS OUT OF THIS WORLD!",
            },             
            {
                "WINNING",
                "",
                "YOUR FINAL CHALLENGE",
                "IS RIGHT DOWN THIS",
                "HALLWAY.",
            }, 
            {
                "FRESHERERST",
                "",
                "BIG ADAM, GIANT SISTER.",
                "IT IS KNOWN BY MANY NAMES",
                "BUT JUDITH 4HRPG BLUEBERRY.",
                "",
                "FISSION MAILED!",
            }, 
    };
    
    private int delay = 15;
    private int id;
    public SignReadScreen(Screen parent, int id) {
        this.parent = parent;
        this.id = id;
    }
    
    public void render() {
        parent.render();
        spriteBatch.begin();
        int xs = 0;
        int ys = signs[id].length+3;
        for (int y=0; y<signs[id].length; y++) {
            int s = signs[id][y].length();
            if (s>xs) xs = s;
        }
        int xp = 160-xs*3;
        int yp = 120-ys*3;
        for (int x=0-1; x<xs+1; x++) {
            for (int y=0-1; y<ys+1; y++) {
                int xf = 1;
                int yf = 12;
                if (x<0) xf--;
                if (y<0) yf--;
                if (x>=xs) xf++;
                if (y>=ys) yf++;
                draw(Art.guys[xf][yf], xp+x*6, yp+y*6);
            }
        }
        for (int y=0; y<signs[id].length; y++) {
            drawString(signs[id][y], xp, yp+y*6);
        }
        if (delay==0)
        drawString("PRESS X", xp+(xs-8)*6, yp+(signs[id].length+2)*6);
        spriteBatch.end();
    }
    
    public void tick(Input input) {
        if (!input.oldButtons[Input.ESCAPE] && input.buttons[Input.ESCAPE] || Gdx.input.isTouched()) {
            setScreen(parent);
            return;
        }
        if (delay>0) delay--;
        if (delay==0 && input.buttons[Input.SHOOT] && !input.oldButtons[Input.SHOOT]) {
            setScreen(parent);
        }
    }    
}
