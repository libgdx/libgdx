package com.badlogic.gdx.graphics.g3d;

import java.util.Comparator;
import java.util.Iterator;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

public class Material extends Attributes {
	private static int counter = 0;
	
	public String id;
	
	/** Create an empty material */
	public Material() {
		this("mtl"+(++counter));
	}
	/** Create an empty material */
	public Material(final String id) { 
		this.id = id;	
	}
	/** Create a material with the specified attributes */
	public Material(final Attribute... attributes) {
		this();
		set(attributes);
	}
	/** Create a material with the specified attributes */
	public Material(final String id, final Attribute... attributes) {
		this(id);
		set(attributes);
	}
	/** Create a material with the specified attributes */
	public Material(final Array<Attribute> attributes) {
		this();
		set(attributes);
	}
	/** Create a material with the specified attributes */
	public Material(final String id, final Array<Attribute> attributes) {
		this(id);
		set(attributes);
	}
	/** Create a material which is an exact copy of the specified material */
	public Material(final Material copyFrom) {
		this(copyFrom.id, copyFrom);
	}
	/** Create a material which is an exact copy of the specified material */
	public Material(final String id, final Material copyFrom) {
		this(id);
		for (Attribute attr : copyFrom)
			set(attr.copy());
	}
	
	/** Create a copy of this material */
	public final Material copy() {
		return new Material(this); 
	}
	
	/** @return True if this material equals the other material in every aspect (including the ID) */
	public final boolean equals (final Material other) {
		return same(other, true) && id.equals(other.id);
	}
	
	/** @return True if this material equals the other material in every aspect (including the ID) */
	@Override
	public final boolean equals (final Object obj) {
		return obj instanceof Material ? equals((Material)obj) : false;
	}
}
