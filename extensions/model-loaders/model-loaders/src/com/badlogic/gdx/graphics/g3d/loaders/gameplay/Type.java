package com.badlogic.gdx.graphics.g3d.loaders.gameplay;

import com.badlogic.gdx.utils.GdxRuntimeException;

/**
 * Type enumeration for object types in a Gameplay Bundle
 * see <a href="https://github.com/blackberry/GamePlay/blob/master/gameplay-encoder/gameplay-bundle.txt#L123">gamebundle.txt</a>
 * @author mzechner
 *
 */
public enum Type {
	Scene(1),
	Node(2),
	Animations(3),
	Animation(4),
	AnimationChannel(5),
	Model(11),
	Material(16),
	Effect(17),
	Camera(32),
	Light(33),
	Mesh(34),
	MeshPart(35),
	MeshSkin(36),
	Font(128);
	
	private final int id;
	
	private Type(int id) {
		this.id = id;
	}
	
	public static Type forId(int id) {
		for(Type type: Type.values()) {
			if(type.id == id) return type;
		}
		throw new GdxRuntimeException("Unknown type id " + id);
	}
}
