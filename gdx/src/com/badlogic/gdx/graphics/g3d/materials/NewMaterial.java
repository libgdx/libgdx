package com.badlogic.gdx.graphics.g3d.materials;

import java.util.Comparator;
import java.util.Iterator;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Array;

public class NewMaterial implements Iterable<NewMaterial.Attribute>, Comparator<NewMaterial.Attribute> {
	/** Extend this class to implement a material attribute.
	 *  Register the attribute type by statically calling the {@link #register(String)} method, 
	 *  whose return value should be used to instantiate the attribute. 
	 *  A class can implement multiple types*/
	public static abstract class Attribute {
		protected static long register(final String type) {
			return NewMaterial.register(type);
		}
		/** The type of this attribute */
		protected long type;
		protected Attribute(final long type) {
			this.type = type;
		}
		/** @return The type of material */
		public final long getType() {
			return type;
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
			if (other.type != other.type) return false; 
			return equals(other);
		}
	}
	
	/** The registered type aliases */
	private final static Array<String> types = new Array<String>();
	
	/** @return The ID of the specified attribute type, or zero if not available */
	protected final static long getAttributeType(final String alias) {
		for (int i = 0; i < types.size; i++)
			if (types.get(i).compareTo(alias)==0)
				return 1L << i;
		return 0;
	}
	
	/** Use {@link Attribute#register(String)} instead */ 
	protected final static long register(final String alias) {
		long result = getAttributeType(alias);
		if (result > 0)
			return result;
		types.add(alias);
		return 1L << (types.size - 1);
	}
	
	protected long mask;
	protected Array<Attribute> attributes = new Array<Attribute>();
	protected boolean sorted = true;
	
	/** Create an empty material */
	public NewMaterial() {	}
	/** Create a material with the specified attributes */
	public NewMaterial(final Attribute... attributes) {
		add(attributes);
	}
	/** Create a material with the specified attributes */
	public NewMaterial(final Array<Attribute> attributes) {
		add(attributes);
	}
	/** Create a material which is an exact copy of the specified material */
	public NewMaterial(final NewMaterial copyFrom) {
		for (Attribute attr : copyFrom)
			add(attr.copy());
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
	
	/** @deprecated Use {@link #has(long)} instead
	 * @return True if this material has the specified attribute, i.e. material.has(BlendingAttribute.class); */
	public final boolean has(final String type) {
		return has(getAttributeType(type));
	}
	
	/** Add one or more attributes to this material */
	public final void add(final Attribute... attributes) {
		for (int i = 0; i < attributes.length; i++) {
			final Attribute attr = attributes[i];
			if (!has(attr.type)) {
				enable(attr.type);
				this.attributes.add(attr);
				sorted = false;
			}
		}
	}

	/** Add an array of attributes to this material */
	public final void add(final Array<Attribute> attributes) {
		for (int i = 0; i < attributes.size; i++) {
			final Attribute attr = attributes.get(i);
			if (!has(attr.type)) {
				enable(attr.type);
				this.attributes.add(attr);
				sorted = false;
			}
		}
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
	
	/** @deprecated Use {@link #remove(long)} instead
	 * Removes the attribute from the material, i.e.: material.remove(BlendingAttribute.class); */
	public final void remove(final String alias) {
		final long type = getAttributeType(alias);
		if (has(type)) {
			for (int i = 0; i < attributes.size; i++)
				if (attributes.get(i).type == type) {
					attributes.removeIndex(i);
					break;
				}
			sorted = false;
			disable(type);
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
	
	/** Create a copy of this material */
	public final NewMaterial copy() {
		return new NewMaterial(this); 
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
	
	/** @return True if this material contains the same attributes as the other, 
	 * use {@link #equals(NewMaterial)} to see if the values are also the same */
	public final boolean same(final NewMaterial other) {
		return mask == other.mask;
	}
	
	/** @return True if this material equals the other material in every aspect */
	public final boolean equals (final NewMaterial other) {
		if (other == null) return false;
		if (other == this) return true;
		if (!same(other)) return false;
		sort();
		other.sort();
		for (int i = 0; i < attributes.size; i++)
			if (!attributes.get(i).equals(other.attributes.get(i)))
				return false;
		return true;
	}
	
	/** @return True if this material equals the other material in every aspect */
	@Override
	public final boolean equals (final Object obj) {
		return obj instanceof NewMaterial ? equals((NewMaterial)obj) : false;
	}
	
	/** Used for iterating through the attributes */
	@Override
	public final Iterator<Attribute> iterator () {
		return attributes.iterator();
	}
}
