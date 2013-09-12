package com.badlogic.gdx.graphics.g3d.materials;

import java.util.Comparator;
import java.util.Iterator;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

public class Material implements Iterable<Material.Attribute>, Comparator<Material.Attribute> {
	/** Extend this class to implement a material attribute.
	 *  Register the attribute type by statically calling the {@link #register(String)} method, 
	 *  whose return value should be used to instantiate the attribute. 
	 *  A class can implement multiple types */
	public static abstract class Attribute {
		protected static long register(final String type) {
			return Material.register(type);
		}
		/** The type of this attribute */
		public final long type;
		protected Attribute(final long type) {
			this.type = type;
		}
		/** @return An exact copy of this attribute */
		public abstract Attribute copy(); 
		protected abstract boolean equals(Attribute other);
		@Override
		public boolean equals (Object obj) {
			if (obj == null) return false;
			if (obj == this) return true;
			if (!(obj instanceof Attribute)) return false;
			final Attribute other = (Attribute)obj;
			if (this.type != other.type) return false;
			return equals(other);
		}
		@Override
		public String toString () {
			return Material.getAttributeAlias(type);
		}
	}
	
	/** The registered type aliases */
	private final static Array<String> types = new Array<String>();
	
	private static int counter = 0;
	
	/** @return The ID of the specified attribute type, or zero if not available */
	protected final static long getAttributeType(final String alias) {
		for (int i = 0; i < types.size; i++)
			if (types.get(i).compareTo(alias)==0)
				return 1L << i;
		return 0;
	}
	
	/** @return The alias of the specified attribute type, or null if not available. */
	protected final static String getAttributeAlias(final long type) {
		int idx = -1;
		while (type != 0 && ++idx < 63 && (((type >> idx) & 1) == 0));
		return (idx >= 0 && idx < types.size) ? types.get(idx) : null;
	}
	
	/** Use {@link Attribute#register(String)} instead */ 
	protected final static long register(final String alias) {
		long result = getAttributeType(alias);
		if (result > 0)
			return result;
		types.add(alias);
		return 1L << (types.size - 1);
	}
	
	public String id;
	protected long mask;
	protected final Array<Attribute> attributes = new Array<Attribute>();
	protected boolean sorted = true;
	
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
	
	private final void enable(final long mask) {
		this.mask |= mask; 
	}
	private final void disable(final long mask) {
		this.mask &= -1L ^ mask;
	}
	
	/** @return Bitwise mask of the ID's of all the containing attributes */  
	public final long getMask() {
		return mask;
	}
	
	/** @return True if this material has the specified attribute, i.e. material.has(BlendingAttribute.ID); */
	public final boolean has(final long type) {
		return type > 0 && (this.mask & type) == type;
	}
	
	/** @return the index of the attribute with the specified type or negative if not available. */
	protected int indexOf(final long type) {
		if (has(type))
			for (int i = 0; i < attributes.size; i++)
				if (attributes.get(i).type == type)
					return i;
		return -1;
	}

	/** Add a attribute to this material.
	 * If the material already contains an attribute of the same type it is overwritten. */
	public final void set(final Attribute attribute) {
		final int idx = indexOf(attribute.type);
		if (idx < 0) {
			enable(attribute.type);
			attributes.add(attribute);
			sorted = false;
		} else {
			attributes.set(idx, attribute);
		}
	}
	
	/** Add an array of attributes to this material. 
	 * If the material already contains an attribute of the same type it is overwritten. */
	public final void set(final Attribute... attributes) {
		for (final Attribute attr : attributes)
			set(attr);
	}

	/** Add an array of attributes to this material.
	 * If the material already contains an attribute of the same type it is overwritten. */
	public final void set(final Iterable<Attribute> attributes) {
		for (final Attribute attr : attributes)
			set(attr);
	}
	
	/** Removes the attribute from the material, i.e.: material.remove(BlendingAttribute.ID);
	 * Can also be used to remove multiple attributes also, i.e. remove(AttributeA.ID | AttributeB.ID); */
	public final void remove(final long mask) {
		for (int i = 0; i < attributes.size; i++) {
			final long type = attributes.get(i).type;
			if ((mask & type) == type) {
				attributes.removeIndex(i);
				disable(type);
				sorted = false;
			}
		}
	}
	
	/** Example usage: ((BlendingAttribute)material.get(BlendingAttribute.ID)).sourceFunction;
	 * @return The attribute (which can safely be cast) if any, otherwise null */
	public final Attribute get(final long type) {
		if (has(type))
			for (int i = 0; i < attributes.size; i++)
				if (attributes.get(i).type == type)
					return attributes.get(i);
		return null;
	}
	
	/** Get multiple attributes at once.
	 * Example: material.get(out, AttributeA.ID | AttributeB.ID | AttributeC.ID); */
	public final Array<Attribute> get(final Array<Attribute> out, final long type) {
		for (int i = 0; i < attributes.size; i++)
			if ((attributes.get(i).type & type) != 0)
				out.add(attributes.get(i));
		return out;
	}
	
	/** Removes all attributes */
	public final void clear() {
		mask = 0;
		attributes.clear();
	}
	
	/** @return The amount of attributes this material contains. */
	public int size() {
		return attributes.size;
	}
	
	/** Create a copy of this material */
	public final Material copy() {
		return new Material(this); 
	}

	/** Used for sorting attributes */
	@Override
	public final int compare (final Attribute arg0, final Attribute arg1) {
		return (int)(arg0.type - arg1.type);
	}
	
	/** Sort the attributes by their ID */
	public final void sort() {
		if (!sorted) {
			attributes.sort(this);
			sorted = true;
		}
	}
	
	/** Check if this Material has the same attributes as the other Material. 
	 * If compareValues is true, it also compares the values of each attribute.
	 * Use {@link #equals(Material)} to compare Materials including ID.
	 * @param compareValues True to compare attribute values, false to only compare attribute types
	 * @return True if this material contains the same attributes (and optionally attribute values) as the other. */
	public final boolean same(final Material other, boolean compareValues) {
		if (other == this)
			return true;
		if ((other == null) || (mask != other.mask))
			return false;
		if (!compareValues)
			return true;
		sort();
		other.sort();
		for (int i = 0; i < attributes.size; i++)
			if (!attributes.get(i).equals(other.attributes.get(i)))
				return false;
		return true;
	}
	
	/** See {@link #same(Material, boolean)}
	 * @return True if this material contains the same attributes (but not values) as the other. */
	public final boolean same(final Material other) {
		return same(other, false);
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
	
	/** Used for iterating through the attributes */
	@Override
	public final Iterator<Attribute> iterator () {
		return attributes.iterator();
	}
}
