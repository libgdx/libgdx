
package com.badlogic.gdx.scenes.scene2d;


public class ActorEvent extends Event {
	private Type type;
	private float stageX, stageY;
	private int pointer, button, keyCode, scrollAmount;
	private char character;
	private Actor relatedActor;

	public void reset () {
		super.reset();
		relatedActor = null;
	}

	public float getStageX () {
		return stageX;
	}

	public void setStageX (float stageX) {
		this.stageX = stageX;
	}

	public float getStageY () {
		return stageY;
	}

	public void setStageY (float stageY) {
		this.stageY = stageY;
	}

	public Type getType () {
		return type;
	}

	public void setType (Type type) {
		this.type = type;
	}

	public int getPointer () {
		return pointer;
	}

	public void setPointer (int pointer) {
		this.pointer = pointer;
	}

	public int getButton () {
		return button;
	}

	public void setButton (int button) {
		this.button = button;
	}

	public int getKeyCode () {
		return keyCode;
	}

	public void setKeyCode (int keyCode) {
		this.keyCode = keyCode;
	}

	public char getCharacter () {
		return character;
	}

	public void setCharacter (char character) {
		this.character = character;
	}

	public int getScrollAmount () {
		return scrollAmount;
	}

	public void setScrollAmount (int scrollAmount) {
		this.scrollAmount = scrollAmount;
	}

	public Actor getRelatedActor () {
		return relatedActor;
	}

	public void setRelatedActor (Actor relatedActor) {
		this.relatedActor = relatedActor;
	}

	public String toString () {
		return type.toString();
	}

	static public enum Type {
		touchDown, touchUp, touchDragged, touchMoved, enter, exit, scrolled, keyDown, keyUp, keyTyped
	}
}
