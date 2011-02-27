package com.badlogic.gdx.input;

import java.io.DataOutputStream;
import java.net.Socket;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;

/**
 * Sends all inputs from touch, key, accelerometer and compass to a {@link RemoteInput}
 * at the given ip/port. Instantiate this and call sendUpdate() periodically.
 * 
 * @author mzechner
 *
 */
public class RemoteSender implements InputProcessor {
	private DataOutputStream out;
	private boolean connected = false;
	
	public static final int KEY_DOWN = 0;
	public static final int KEY_UP = 1;
	public static final int KEY_TYPED = 2;
	
	public static final int TOUCH_DOWN = 3;
	public static final int TOUCH_UP = 4;
	public static final int TOUCH_DRAGGED = 5;
	
	public static final int ACCEL = 6;
	public static final int COMPASS = 7;
	public static final int SIZE = 8;
	
	public RemoteSender(String ip, int port) {
		try {
			Socket socket = new Socket(ip, port);
			socket.setTcpNoDelay(true);
			socket.setSoTimeout(3000);
			out = new DataOutputStream(socket.getOutputStream());
			out.writeBoolean(Gdx.input.supportsMultitouch());
			connected = true;
			Gdx.input.setInputProcessor(this);
		} catch(Exception e) {
			Gdx.app.log("RemoteSender", "couldn't connect to " + ip + ":" + port);
		}
	}	
	
	public void sendUpdate() {		
		synchronized(this) {
			if(!connected) return;
		}
		try {
			out.writeInt(ACCEL);
			out.writeFloat(Gdx.input.getAccelerometerX());
			out.writeFloat(Gdx.input.getAccelerometerY());
			out.writeFloat(Gdx.input.getAccelerometerZ());
			out.writeInt(COMPASS);
			out.writeFloat(Gdx.input.getAzimuth());
			out.writeFloat(Gdx.input.getPitch());
			out.writeFloat(Gdx.input.getRoll());
			out.writeInt(SIZE);
			out.writeFloat(Gdx.graphics.getWidth());
			out.writeFloat(Gdx.graphics.getHeight());
		} catch(Throwable t) {
			out = null;
			connected = false;
		}
	}

	@Override public boolean keyDown (int keycode) {
		synchronized(this) {
			if(!connected) return false;
		}
		
		try {
			out.writeInt(KEY_DOWN);
			out.writeInt(keycode);
		} catch(Throwable t) {
			synchronized(this) {
				connected = false;
			}
		}
		return false;
	}

	@Override public boolean keyUp (int keycode) {
		synchronized(this) {
			if(!connected) return false;
		}
		
		try {
			out.writeInt(KEY_UP);
			out.writeInt(keycode);
		} catch(Throwable t) {
			synchronized(this) {
				connected = false;
			}
		}
		return false;
	}

	@Override public boolean keyTyped (char character) {
		synchronized(this) {
			if(!connected) return false;
		}
		
		try {
			out.writeInt(KEY_TYPED);
			out.writeChar(character);
		} catch(Throwable t) {
			synchronized(this) {
				connected = false;
			}
		}
		return false;
	}

	@Override public boolean touchDown (int x, int y, int pointer, int button) {
		synchronized(this) {
			if(!connected) return false;
		}
		
		try {
			out.writeInt(TOUCH_DOWN);
			out.writeInt(x);
			out.writeInt(y);
			out.writeInt(pointer);			
		} catch(Throwable t) {
			synchronized(this) {
				connected = false;
			}
		}
		return false;
	}

	@Override public boolean touchUp (int x, int y, int pointer, int button) {
		synchronized(this) {
			if(!connected) return false;
		}
		
		try {
			out.writeInt(TOUCH_UP);
			out.writeInt(x);
			out.writeInt(y);
			out.writeInt(pointer);			
		} catch(Throwable t) {
			synchronized(this) {
				connected = false;
			}
		}
		return false;
	}

	@Override public boolean touchDragged (int x, int y, int pointer) {
		synchronized(this) {
			if(!connected) return false;
		}
		
		try {
			out.writeInt(TOUCH_DRAGGED);
			out.writeInt(x);
			out.writeInt(y);
			out.writeInt(pointer);			
		} catch(Throwable t) {
			synchronized(this) {
				connected = false;
			}
		}
		return false;
	}

	@Override public boolean touchMoved (int x, int y) {	
		return false;
	}

	@Override public boolean scrolled (int amount) {
		return false;
	}
	
	public boolean isConnected() {
		synchronized(this) {
			return connected;
		}
	}
}
