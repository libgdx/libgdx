package com.badlogic.gdx.graphics.g3d.loaders.gameplay;

/**
 * A reference to an object in a Gameplay Bundle file, 
 * see <a href="https://github.com/blackberry/GamePlay/blob/master/gameplay/src/Bundle.h#L102">Bundle.h</a>
 * @author mzechner
 *
 */
public class Reference {
	private final String id;
	private final Type type;
	private final int offset;
	
	public Reference(String id, Type type, int offset) {
		this.id = id;
		this.type = type;
		this.offset = offset;
	}

	public String getId () {
		return id;
	}

	public Type getType () {
		return type;
	}

	public int getOffset () {
		return offset;
	}

	@Override
	public String toString () {
		return "Reference [id=" + id + ", type=" + type + ", offset=" + offset + "]";
	}
}
