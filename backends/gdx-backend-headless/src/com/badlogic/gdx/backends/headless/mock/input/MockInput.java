package com.badlogic.gdx.backends.headless.mock.input;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Pixmap;

public class MockInput implements Input {

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
	public int getX() {
		return 0;
	}

	@Override
	public int getX(int pointer) {
		return 0;
	}

	@Override
	public int getDeltaX() {
		return 0;
	}

	@Override
	public int getDeltaX(int pointer) {
		return 0;
	}

	@Override
	public int getY() {
		return 0;
	}

	@Override
	public int getY(int pointer) {
		return 0;
	}

	@Override
	public int getDeltaY() {
		return 0;
	}

	@Override
	public int getDeltaY(int pointer) {
		return 0;
	}

	@Override
	public boolean isTouched() {
		return false;
	}

	@Override
	public boolean justTouched() {
		return false;
	}

	@Override
	public boolean isTouched(int pointer) {
		return false;
	}

	@Override
	public boolean isButtonPressed(int button) {
		return false;
	}

	@Override
	public boolean isKeyPressed(int key) {
		return false;
	}

	@Override
	public void getTextInput(TextInputListener listener, String title, String text) {

	}

	@Override
	public void getPlaceholderTextInput(TextInputListener listener, String title, String placeholder) {

	}

	@Override
	public void setOnscreenKeyboardVisible(boolean visible) {

	}

	@Override
	public void vibrate(int milliseconds) {

	}

	@Override
	public void vibrate(long[] pattern, int repeat) {

	}

	@Override
	public void cancelVibrate() {

	}

	@Override
	public float getAzimuth() {
		return 0;
	}

	@Override
	public float getPitch() {
		return 0;
	}

	@Override
	public float getRoll() {
		return 0;
	}

	@Override
	public void getRotationMatrix(float[] matrix) {

	}

	@Override
	public long getCurrentEventTime() {
		return 0;
	}

	@Override
	public void setCatchBackKey(boolean catchBack) {

	}

	@Override
	public void setCatchMenuKey(boolean catchMenu) {

	}

	@Override
	public void setInputProcessor(InputProcessor processor) {

	}

	private InputProcessor mockInputProcessor;

	@Override
	public InputProcessor getInputProcessor() {
		if (mockInputProcessor == null) {
			mockInputProcessor = new InputAdapter();
		}
		return mockInputProcessor;
	}

	@Override
	public boolean isPeripheralAvailable(Peripheral peripheral) {
		return false;
	}

	@Override
	public int getRotation() {
		return 0;
	}

	@Override
	public Orientation getNativeOrientation() {
		return Orientation.Landscape;
	}

	@Override
	public void setCursorCatched(boolean catched) {

	}

	@Override
	public boolean isCursorCatched() {
		return false;
	}

	@Override
	public void setCursorPosition(int x, int y) {

	}

	@Override
	public void setCursorImage(Pixmap pixmap, int xHotspot, int yHotspot) {

	}
}
