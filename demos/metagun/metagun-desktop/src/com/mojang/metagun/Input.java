package com.mojang.metagun;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputProcessor;

public class Input implements InputProcessor {
    public static final int UP = 0;
    public static final int DOWN = 1;
    public static final int LEFT = 2;
    public static final int RIGHT = 3;

    public static final int JUMP = 4;
    public static final int SHOOT = 5;

    public static final int ESCAPE = 6;

    public boolean[] buttons = new boolean[64];
    public boolean[] oldButtons = new boolean[64];

    public void set(int key, boolean down) {
        int button = -1;

        if (key == Keys.KEYCODE_DPAD_UP) button = UP;
        if (key == Keys.KEYCODE_DPAD_LEFT) button = LEFT;
        if (key == Keys.KEYCODE_DPAD_DOWN) button = DOWN;
        if (key == Keys.KEYCODE_DPAD_RIGHT) button = RIGHT;

        if (key == Keys.KEYCODE_Y) button = JUMP;
        if (key == Keys.KEYCODE_Z) button = JUMP;
        if (key == Keys.KEYCODE_X) button = SHOOT;
        if (key == Keys.KEYCODE_C) button = JUMP;
        if (key == Keys.KEYCODE_A) button = JUMP;
        if (key == Keys.KEYCODE_S) button = SHOOT;
        if (key == Keys.KEYCODE_D) button = JUMP;

        if (key == Keys.KEYCODE_ESCAPE) button = ESCAPE;

        if (button >= 0) {
            buttons[button] = down;
        }
    }

    public void tick() {
   	 if(Gdx.input.getAccelerometerY() < -1.2f)
   		 set(Keys.KEYCODE_DPAD_LEFT, true);
   	 else if(Gdx.input.getAccelerometerY() > 1.2f)
   		 set(Keys.KEYCODE_DPAD_RIGHT, true);
   	 else {
   		 set(Keys.KEYCODE_DPAD_LEFT, false);
   		 set(Keys.KEYCODE_DPAD_RIGHT, false);
   	 }
   	 
        for (int i = 0; i < buttons.length; i++) {
            oldButtons[i] = buttons[i];
        }               
    }


    public void releaseAllKeys() {
        for (int i = 0; i < buttons.length; i++) {
            buttons[i] = false;
        }
    }

	@Override public boolean keyDown (int keycode) {
		set(keycode, true);
		return false;
	}

	@Override public boolean keyUp (int keycode) {
		set(keycode, false);
		return false;
	}

	@Override public boolean keyTyped (char character) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override public boolean touchDown (int x, int y, int pointer, int button) {
		if(x < Gdx.graphics.getWidth() / 5) 
			set(Keys.KEYCODE_Z, true);
		if(x > Gdx.graphics.getWidth() - Gdx.graphics.getWidth() / 5)
			set(Keys.KEYCODE_Y, true);
		return false;
	}

	@Override public boolean touchUp (int x, int y, int pointer, int button) {		
		set(Keys.KEYCODE_Z, false);		
		set(Keys.KEYCODE_Y, false);
		return false;
	}

	@Override public boolean touchDragged (int x, int y, int pointer) {		
		return false;
	}

	@Override public boolean touchMoved (int x, int y) {
		return false;
	}

	@Override public boolean scrolled (int amount) {
		return false;
	}	
}
