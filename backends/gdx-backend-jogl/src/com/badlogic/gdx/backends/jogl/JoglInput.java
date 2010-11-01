package com.badlogic.gdx.backends.jogl;

import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.media.opengl.GLCanvas;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pool.PoolObjectFactory;

public class JoglInput implements Input, MouseMotionListener, MouseListener,
		KeyListener {
	class KeyEvent {
		static final int KEY_DOWN = 0;
		static final int KEY_UP = 1;
		static final int KEY_TYPED = 2;

		int type;
		int keyCode;
		char keyChar;
	}

	class TouchEvent {
		static final int TOUCH_DOWN = 0;
		static final int TOUCH_UP = 1;
		static final int TOUCH_DRAGGED = 2;

		int type;
		int x;
		int y;
		int pointer;
	}

	Pool<KeyEvent> freeKeyEvents = new Pool<KeyEvent>(
			new PoolObjectFactory<KeyEvent>() {

				@Override
				public KeyEvent createObject() {
					return new KeyEvent();
				}
			}, 1000);

	Pool<TouchEvent> freeTouchEvents = new Pool<TouchEvent>(
			new PoolObjectFactory<TouchEvent>() {

				@Override
				public TouchEvent createObject() {
					return new TouchEvent();
				}
			}, 1000);

	List<KeyEvent> keyEvents = new ArrayList<KeyEvent>();
	List<TouchEvent> touchEvents = new ArrayList<TouchEvent>();
	int touchX = 0;
	int touchY = 0;
	boolean touchDown = false;
	Set<Integer> keys = new HashSet<Integer>();	

	public JoglInput(GLCanvas canvas) {
		canvas.addMouseListener(this);
		canvas.addMouseMotionListener(this);
		canvas.addKeyListener(this);		
	}

	@Override
	public float getAccelerometerX() {
		return 0;
	}

	@Override
	public float getAccelerometerY() {
		return 0;
	}

	@Override
	public float getAccelerometerZ() {
		return 0;
	}

	@Override
	public void getTextInput(final TextInputListener listener, final String title,
			final String text) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override public void run () {
				String output = JOptionPane.showInputDialog(null, title, text);
				if (output != null) listener.input(output);
			}
		});
	}

	@Override
	public int getX() {
		return touchX;
	}

	@Override
	public int getX(int pointer) {
		if (pointer == 0)
			return touchX;
		else
			return 0;
	}

	@Override
	public int getY() {
		return touchY;
	}

	@Override
	public int getY(int pointer) {
		if (pointer == 0)
			return touchY;
		else
			return 0; 				
	}

	@Override
	public boolean isAccelerometerAvailable() {
		return false;
	}

	@Override
	public boolean isKeyPressed(int key) {
		synchronized(this) {
			if( key == Input.Keys.ANY_KEY )
				return keys.size() > 0;
			else
				return keys.contains(key);
		}
	}

	@Override
	public boolean isTouched() {
		return touchDown;
	}

	@Override
	public boolean isTouched(int pointer) {
		if (pointer == 0)
			return touchDown;
		else
			return false;
	}

	@Override
	public void processEvents(InputProcessor listener) {
		synchronized(this) {
			if(listener!=null) {						
				for(KeyEvent e: keyEvents) {
					switch(e.type) {
					case KeyEvent.KEY_DOWN:
						listener.keyDown(e.keyCode);
						break;
					case KeyEvent.KEY_UP:
						listener.keyUp(e.keyCode);
						break;
					case KeyEvent.KEY_TYPED:
						listener.keyTyped(e.keyChar);
					}
					freeKeyEvents.free(e);
				}					
				
				for(TouchEvent e: touchEvents) {
					switch(e.type) {
					case TouchEvent.TOUCH_DOWN:
						listener.touchDown(e.x, e.y, e.pointer);
						break;
					case TouchEvent.TOUCH_UP:
						listener.touchUp(e.x, e.y, e.pointer);
						break;
					case TouchEvent.TOUCH_DRAGGED:
						listener.touchDragged(e.x, e.y, e.pointer);
					}
					freeTouchEvents.free(e);
				}
			}
			
			keyEvents.clear();
			touchEvents.clear();
		}
	}

	@Override
	public void setCatchBackKey(boolean catchBack) {

	}

	@Override
	public void setOnscreenKeyboardVisible(boolean visible) {

	}

	@Override
	public boolean supportsMultitouch() {
		return false;
	}

	@Override
	public boolean supportsOnscreenKeyboard() {
		return false;
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		synchronized(this) {
			TouchEvent event = freeTouchEvents.newObject();
			event.pointer = 0;
			event.x = e.getX();
			event.y = e.getY();
			event.type = TouchEvent.TOUCH_DRAGGED;			
			touchEvents.add(event);
			
			touchX = event.x;
			touchY = event.y;
			touchDown = true;
		}
	}

	@Override
	public void mouseMoved(MouseEvent arg0) {

	}

	@Override
	public void mouseClicked(MouseEvent arg0) {

	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mousePressed(MouseEvent e) {
		synchronized(this) {
			TouchEvent event = freeTouchEvents.newObject();
			event.pointer = 0;
			event.x = e.getX();
			event.y = e.getY();
			event.type = TouchEvent.TOUCH_DOWN;			
			touchEvents.add(event);
			
			touchX = event.x;
			touchY = event.y;
			touchDown = true;
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		synchronized(this) {
			TouchEvent event = freeTouchEvents.newObject();
			event.pointer = 0;
			event.x = e.getX();
			event.y = e.getY();
			event.type = TouchEvent.TOUCH_UP;			
			touchEvents.add(event);
			
			touchX = event.x;
			touchY = event.y;
			touchDown = false;
		}
	}

	@Override
	public void keyPressed(java.awt.event.KeyEvent e) {
		synchronized(this) {
			KeyEvent event = freeKeyEvents.newObject();
			event.keyChar = 0;
			event.keyCode = translateKeyCode(e.getKeyCode());
			event.type = KeyEvent.KEY_DOWN;
			keyEvents.add(event);			
			keys.add(event.keyCode);
		}
	}

	@Override
	public void keyReleased(java.awt.event.KeyEvent e) {
		synchronized(this) {
			KeyEvent event = freeKeyEvents.newObject();
			event.keyChar = 0;
			event.keyCode = translateKeyCode(e.getKeyCode());
			event.type = KeyEvent.KEY_UP;
			keyEvents.add(event);			
			keys.remove(event.keyCode);
		}
	}

	@Override
	public void keyTyped(java.awt.event.KeyEvent e) {
		synchronized(this) {
			KeyEvent event = freeKeyEvents.newObject();
			event.keyChar = e.getKeyChar();
			event.keyCode = 0;
			event.type = KeyEvent.KEY_TYPED;	
			keyEvents.add(event);
		}
	}

	protected static int translateKeyCode (int keyCode) {
		if (keyCode == java.awt.event.KeyEvent.VK_0) return Input.Keys.KEYCODE_0;
		if (keyCode == java.awt.event.KeyEvent.VK_1) return Input.Keys.KEYCODE_1;
		if (keyCode == java.awt.event.KeyEvent.VK_2) return Input.Keys.KEYCODE_2;
		if (keyCode == java.awt.event.KeyEvent.VK_3) return Input.Keys.KEYCODE_3;
		if (keyCode == java.awt.event.KeyEvent.VK_4) return Input.Keys.KEYCODE_4;
		if (keyCode == java.awt.event.KeyEvent.VK_5) return Input.Keys.KEYCODE_5;
		if (keyCode == java.awt.event.KeyEvent.VK_6) return Input.Keys.KEYCODE_6;
		if (keyCode == java.awt.event.KeyEvent.VK_7) return Input.Keys.KEYCODE_7;
		if (keyCode == java.awt.event.KeyEvent.VK_8) return Input.Keys.KEYCODE_8;
		if (keyCode == java.awt.event.KeyEvent.VK_9) return Input.Keys.KEYCODE_9;
		if (keyCode == java.awt.event.KeyEvent.VK_A) return Input.Keys.KEYCODE_A;
		if (keyCode == java.awt.event.KeyEvent.VK_B) return Input.Keys.KEYCODE_B;
		if (keyCode == java.awt.event.KeyEvent.VK_C) return Input.Keys.KEYCODE_C;
		if (keyCode == java.awt.event.KeyEvent.VK_D) return Input.Keys.KEYCODE_D;
		if (keyCode == java.awt.event.KeyEvent.VK_E) return Input.Keys.KEYCODE_E;
		if (keyCode == java.awt.event.KeyEvent.VK_F) return Input.Keys.KEYCODE_F;
		if (keyCode == java.awt.event.KeyEvent.VK_G) return Input.Keys.KEYCODE_G;
		if (keyCode == java.awt.event.KeyEvent.VK_H) return Input.Keys.KEYCODE_H;
		if (keyCode == java.awt.event.KeyEvent.VK_I) return Input.Keys.KEYCODE_I;
		if (keyCode == java.awt.event.KeyEvent.VK_J) return Input.Keys.KEYCODE_J;
		if (keyCode == java.awt.event.KeyEvent.VK_K) return Input.Keys.KEYCODE_K;
		if (keyCode == java.awt.event.KeyEvent.VK_L) return Input.Keys.KEYCODE_L;
		if (keyCode == java.awt.event.KeyEvent.VK_M) return Input.Keys.KEYCODE_M;
		if (keyCode == java.awt.event.KeyEvent.VK_N) return Input.Keys.KEYCODE_N;
		if (keyCode == java.awt.event.KeyEvent.VK_O) return Input.Keys.KEYCODE_O;
		if (keyCode == java.awt.event.KeyEvent.VK_P) return Input.Keys.KEYCODE_P;
		if (keyCode == java.awt.event.KeyEvent.VK_Q) return Input.Keys.KEYCODE_Q;
		if (keyCode == java.awt.event.KeyEvent.VK_R) return Input.Keys.KEYCODE_R;
		if (keyCode == java.awt.event.KeyEvent.VK_S) return Input.Keys.KEYCODE_S;
		if (keyCode == java.awt.event.KeyEvent.VK_T) return Input.Keys.KEYCODE_T;
		if (keyCode == java.awt.event.KeyEvent.VK_U) return Input.Keys.KEYCODE_U;
		if (keyCode == java.awt.event.KeyEvent.VK_V) return Input.Keys.KEYCODE_V;
		if (keyCode == java.awt.event.KeyEvent.VK_W) return Input.Keys.KEYCODE_W;
		if (keyCode == java.awt.event.KeyEvent.VK_X) return Input.Keys.KEYCODE_X;
		if (keyCode == java.awt.event.KeyEvent.VK_Y) return Input.Keys.KEYCODE_Y;
		if (keyCode == java.awt.event.KeyEvent.VK_Z) return Input.Keys.KEYCODE_Z;
		if (keyCode == java.awt.event.KeyEvent.VK_ALT) return Input.Keys.KEYCODE_ALT_LEFT;
		if (keyCode == java.awt.event.KeyEvent.VK_ALT_GRAPH) return Input.Keys.KEYCODE_ALT_RIGHT;
		if (keyCode == java.awt.event.KeyEvent.VK_BACK_SLASH) return Input.Keys.KEYCODE_BACKSLASH;
		if (keyCode == java.awt.event.KeyEvent.VK_COMMA) return Input.Keys.KEYCODE_COMMA;
		if (keyCode == java.awt.event.KeyEvent.VK_DELETE) return Input.Keys.KEYCODE_DEL;
		if (keyCode == java.awt.event.KeyEvent.VK_LEFT) return Input.Keys.KEYCODE_DPAD_LEFT;
		if (keyCode == java.awt.event.KeyEvent.VK_RIGHT) return Input.Keys.KEYCODE_DPAD_RIGHT;
		if (keyCode == java.awt.event.KeyEvent.VK_UP) return Input.Keys.KEYCODE_DPAD_UP;
		if (keyCode == java.awt.event.KeyEvent.VK_DOWN) return Input.Keys.KEYCODE_DPAD_DOWN;
		if (keyCode == java.awt.event.KeyEvent.VK_ENTER) return Input.Keys.KEYCODE_ENTER;
		if (keyCode == java.awt.event.KeyEvent.VK_HOME) return Input.Keys.KEYCODE_HOME;
		if (keyCode == java.awt.event.KeyEvent.VK_MINUS) return Input.Keys.KEYCODE_MINUS;
		if (keyCode == java.awt.event.KeyEvent.VK_PERIOD) return Input.Keys.KEYCODE_PERIOD;
		if (keyCode == java.awt.event.KeyEvent.VK_PLUS) return Input.Keys.KEYCODE_PLUS;
		if (keyCode == java.awt.event.KeyEvent.VK_SEMICOLON) return Input.Keys.KEYCODE_SEMICOLON;
		if (keyCode == java.awt.event.KeyEvent.VK_SHIFT) return Input.Keys.KEYCODE_SHIFT_LEFT;
		if (keyCode == java.awt.event.KeyEvent.VK_SLASH) return Input.Keys.KEYCODE_SLASH;
		if (keyCode == java.awt.event.KeyEvent.VK_SPACE) return Input.Keys.KEYCODE_SPACE;
		if (keyCode == java.awt.event.KeyEvent.VK_TAB) return Input.Keys.KEYCODE_TAB;

		return Input.Keys.KEYCODE_UNKNOWN;
	}
}
