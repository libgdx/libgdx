package com.badlogic.gdx.graphics.g3d.loaders.gameplay;

/**
 * A reference to an internal or external object.
 * see see <a href="https://github.com/blackberry/GamePlay/blob/master/gameplay-encoder/gameplay-bundle.txt#L40">Bundle.h</a>
 * @author mzechner
 *
 */
public class XRef {
	private final String id;
	
	public XRef(String id) {
		this.id = id;
	}

	public String getId () {
		return id;
	}
}
