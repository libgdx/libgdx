package com.badlogic.gdx.graphics.g3d.xoppa.materials;

import java.util.Comparator;
import java.util.Iterator;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Array;

public class NewMaterial implements Iterable<NewMaterial.Attribute>, Comparator<NewMaterial.Attribute> {
	/** Extend this class to implement a material attribute.
	 *  Register the attribute by statically calling the {@link #register(Class)} method, 
	 *  whose return value should be returned by {@link #getID()} */
	public static abstract class Attribute {
		protected final static <T extends Attribute> long register(Class<T> type) {
			return NewMaterial.register(type);
		}
		/** @return The ID of this attribute, which is the same for all attributes of the same type */ 
		protected abstract long getID();
		protected abstract boolean equals(Attribute other); // Force to implement equals
		@Override
		public boolean equals (Object obj) {
			if (obj == null) return false;
			if (obj == this) return true;
			if (!(obj instanceof Attribute)) return false;
			final Attribute other = (Attribute)obj;
			if (other.getID() != other.getID()) return false; 
			return equals(other);
		}
	}
	
	private static Array<Class> registrations = new Array<Class>();
	
	/** @return The ID of the specified attribute type, or zero if not available */
	protected final static <T extends Attribute> long getAttributeID(Class<T> type) {
		for (int i = 0; i < registrations.size; i++)
			if (registrations.get(i) == type)
				return 1L << i;
		return 0;
	}
	
	/** Use {@link Attribute#register(Class)} instead */ 
	protected final static <T extends Attribute> long register(Class<T> type) {
		long result = getAttributeID(type);
		if (result > 0)
			return result;
		registrations.add(type);
		return 1L << (registrations.size - 1);
	}
	
	protected long mask;
	protected Array<Attribute> attributes = new Array<Attribute>();
	protected boolean sorted = true;
	
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
	public final boolean has(final long id) {
		return id > 0 && (this.mask & id) == id;
	}
	
	/** @deprecated Use {@link #has(long)} instead
	 * @return True if this material has the specified attribute, i.e. material.has(BlendingAttribute.class); */
	public final <T extends Attribute> boolean has(final Class<T> type) {
		return has(getAttributeID(type));
	}
	
	/** Add one or more attributes to this material */
	public final void add(final Attribute... attributes) {
		for (int i = 0; i < attributes.length; i++) {
			final Attribute attr = attributes[i];
			final long id = attr.getID();
			if (!has(id)) {
				enable(id);
				this.attributes.add(attr);
				sorted = false;
			}
		}
	}

	/** Add an array of attributes to this material */
	public final void add(final Array<Attribute> attributes) {
		for (int i = 0; i < attributes.size; i++) {
			final Attribute attr = attributes.get(i);
			final long id = attr.getID();
			if (!has(id)) {
				enable(id);
				this.attributes.add(attr);
				sorted = false;
			}
		}
	}
	
	/** Removes the attribute from the material, i.e.: material.remove(BlendingAttribute.ID);
	 * Can also be used to remove multiple attributes also, i.e. remove(AttributeA.ID | AttributeB.ID); */
	public final void remove(final long mask) {
		for (int i = 0; i < attributes.size; i++) {
			final long id = attributes.get(i).getID();
			if ((mask & id) == id) {
				attributes.removeIndex(i);
				disable(id);
				sorted = false;
			}
		}
	}
	
	/** @deprecated Use {@link #remove(long)} instead
	 * Removes the attribute from the material, i.e.: material.remove(BlendingAttribute.class); */
	public final <T extends Attribute> void remove(final Class<T> type) {
		final long id = getAttributeID(type);
		if (has(id)) {
			for (int i = 0; i < attributes.size; i++)
				if (attributes.get(i).getID() == id) {
					attributes.removeIndex(i);
					break;
				}
			sorted = false;
			disable(id);
		}
	}
	
	/** Example usage: ((BlendingAttribute)material.get(BlendingAttribute.ID)).sourceFunction;
	 * @return The attribute (which can safely be cast) if any, otherwise null */
	public final Attribute get(final long id) {
		if (has(id))
			for (int i = 0; i < attributes.size; i++)
				if (attributes.get(i).getID() == id)
					return attributes.get(i);
		return null;
	}
	
	/** Get multiple attributes at once.
	 * Example: material.get(out, AttributeA.ID | AttributeB.ID | AttributeC.ID); */
	public final Array<Attribute> get(final Array<Attribute> out, final long id) {
		for (int i = 0; i < attributes.size; i++)
			if ((attributes.get(i).getID() & id) != 0)
				out.add(attributes.get(i));
		return out;
	}
	
	/** @deprecated Use {@link #get(long)} instead
	 * Example usage: material.get(BlendingAttribute.class).sourceFunction;
	 * @return The attribute if available, otherwise null */
	public final <T extends Attribute> T get(final Class<T> type) {
		return (T)get(getAttributeID(type));
	}
	
	/** Removes all attributes */
	public final void clear() {
		mask = 0;
		attributes.clear();
	}

	/** Used for sorting attributes */
	@Override
	public final int compare (final Attribute arg0, final Attribute arg1) {
		return (int)(arg0.getID() - arg1.getID());
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
	
	// Example implementation of an attribute
	public static class BlendingAttribute extends NewMaterial.Attribute {
		// The following two lines are required the be implemented in every attribute
		public final static long ID = register(BlendingAttribute.class);
		protected final long getID () { return ID; }
		// Equals is required to be implemented because of comparing materials, param other is guaranteed to be of the same type 
		@Override
		protected boolean equals (final Attribute other) {
			return ((BlendingAttribute)other).sourceFunction == sourceFunction && 
				((BlendingAttribute)other).destFunction == destFunction; 
		}
		// The actual implementation of the attribute
		public int sourceFunction;
		public int destFunction;
	}
}
