package com.badlogic.gdx.controllers.gwt.support;

public interface GamepadSupportListener {

	public void onGamepadConnected(int index);
	public void onGamepadDisconnected(int index);
	public void onGamepadUpdated(int index);
}
