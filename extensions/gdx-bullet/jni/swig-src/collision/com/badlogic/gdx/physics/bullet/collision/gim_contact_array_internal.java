/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.11
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package com.badlogic.gdx.physics.bullet.collision;

import com.badlogic.gdx.physics.bullet.BulletBase;
import com.badlogic.gdx.physics.bullet.linearmath.*;

public class gim_contact_array_internal extends BulletBase {
	private long swigCPtr;

	protected gim_contact_array_internal (final String className, long cPtr, boolean cMemoryOwn) {
		super(className, cPtr, cMemoryOwn);
		swigCPtr = cPtr;
	}

	/** Construct a new gim_contact_array_internal, normally you should not need this constructor it's intended for low-level
	 * usage. */
	public gim_contact_array_internal (long cPtr, boolean cMemoryOwn) {
		this("gim_contact_array_internal", cPtr, cMemoryOwn);
		construct();
	}

	@Override
	protected void reset (long cPtr, boolean cMemoryOwn) {
		if (!destroyed) destroy();
		super.reset(swigCPtr = cPtr, cMemoryOwn);
	}

	public static long getCPtr (gim_contact_array_internal obj) {
		return (obj == null) ? 0 : obj.swigCPtr;
	}

	@Override
	protected void finalize () throws Throwable {
		if (!destroyed) destroy();
		super.finalize();
	}

	@Override
	protected synchronized void delete () {
		if (swigCPtr != 0) {
			if (swigCMemOwn) {
				swigCMemOwn = false;
				CollisionJNI.delete_gim_contact_array_internal(swigCPtr);
			}
			swigCPtr = 0;
		}
		super.delete();
	}

	public void setData (GIM_CONTACT value) {
		CollisionJNI.gim_contact_array_internal_data_set(swigCPtr, this, GIM_CONTACT.getCPtr(value), value);
	}

	public GIM_CONTACT getData () {
		long cPtr = CollisionJNI.gim_contact_array_internal_data_get(swigCPtr, this);
		return (cPtr == 0) ? null : new GIM_CONTACT(cPtr, false);
	}

	public void setSize (SWIGTYPE_p_GUINT value) {
		CollisionJNI.gim_contact_array_internal_size_set(swigCPtr, this, SWIGTYPE_p_GUINT.getCPtr(value));
	}

	public SWIGTYPE_p_GUINT getSize () {
		return new SWIGTYPE_p_GUINT(CollisionJNI.gim_contact_array_internal_size_get(swigCPtr, this), true);
	}

	public void setAllocated_size (SWIGTYPE_p_GUINT value) {
		CollisionJNI.gim_contact_array_internal_allocated_size_set(swigCPtr, this, SWIGTYPE_p_GUINT.getCPtr(value));
	}

	public SWIGTYPE_p_GUINT getAllocated_size () {
		return new SWIGTYPE_p_GUINT(CollisionJNI.gim_contact_array_internal_allocated_size_get(swigCPtr, this), true);
	}

	public void destroyData () {
		CollisionJNI.gim_contact_array_internal_destroyData(swigCPtr, this);
	}

	public boolean resizeData (SWIGTYPE_p_GUINT newsize) {
		return CollisionJNI.gim_contact_array_internal_resizeData(swigCPtr, this, SWIGTYPE_p_GUINT.getCPtr(newsize));
	}

	public boolean growingCheck () {
		return CollisionJNI.gim_contact_array_internal_growingCheck(swigCPtr, this);
	}

	public boolean reserve (SWIGTYPE_p_GUINT size) {
		return CollisionJNI.gim_contact_array_internal_reserve(swigCPtr, this, SWIGTYPE_p_GUINT.getCPtr(size));
	}

	public void clear_range (SWIGTYPE_p_GUINT start_range) {
		CollisionJNI.gim_contact_array_internal_clear_range(swigCPtr, this, SWIGTYPE_p_GUINT.getCPtr(start_range));
	}

	public void clear () {
		CollisionJNI.gim_contact_array_internal_clear(swigCPtr, this);
	}

	public void clear_memory () {
		CollisionJNI.gim_contact_array_internal_clear_memory(swigCPtr, this);
	}

	public gim_contact_array_internal () {
		this(CollisionJNI.new_gim_array__SWIG_0(), true);
	}

	public gim_contact_array_internal (SWIGTYPE_p_GUINT reservesize) {
		this(CollisionJNI.new_gim_array__SWIG_1(SWIGTYPE_p_GUINT.getCPtr(reservesize)), true);
	}

	public SWIGTYPE_p_GUINT sizeVal () {
		return new SWIGTYPE_p_GUINT(CollisionJNI.gim_contact_array_internal_sizeVal(swigCPtr, this), true);
	}

	public SWIGTYPE_p_GUINT max_size () {
		return new SWIGTYPE_p_GUINT(CollisionJNI.gim_contact_array_internal_max_size(swigCPtr, this), true);
	}

	public GIM_CONTACT operatorSubscript (long i) {
		return new GIM_CONTACT(CollisionJNI.gim_contact_array_internal_operatorSubscript(swigCPtr, this, i), false);
	}

	public GIM_CONTACT operatorSubscriptConst (long i) {
		return new GIM_CONTACT(CollisionJNI.gim_contact_array_internal_operatorSubscriptConst(swigCPtr, this, i), false);
	}

	public GIM_CONTACT pointer () {
		long cPtr = CollisionJNI.gim_contact_array_internal_pointer(swigCPtr, this);
		return (cPtr == 0) ? null : new GIM_CONTACT(cPtr, false);
	}

	public GIM_CONTACT pointer_const () {
		long cPtr = CollisionJNI.gim_contact_array_internal_pointer_const(swigCPtr, this);
		return (cPtr == 0) ? null : new GIM_CONTACT(cPtr, false);
	}

	public GIM_CONTACT get_pointer_at (SWIGTYPE_p_GUINT i) {
		long cPtr = CollisionJNI.gim_contact_array_internal_get_pointer_at(swigCPtr, this, SWIGTYPE_p_GUINT.getCPtr(i));
		return (cPtr == 0) ? null : new GIM_CONTACT(cPtr, false);
	}

	public GIM_CONTACT get_pointer_at_const (SWIGTYPE_p_GUINT i) {
		long cPtr = CollisionJNI.gim_contact_array_internal_get_pointer_at_const(swigCPtr, this, SWIGTYPE_p_GUINT.getCPtr(i));
		return (cPtr == 0) ? null : new GIM_CONTACT(cPtr, false);
	}

	public GIM_CONTACT at (SWIGTYPE_p_GUINT i) {
		return new GIM_CONTACT(CollisionJNI.gim_contact_array_internal_at(swigCPtr, this, SWIGTYPE_p_GUINT.getCPtr(i)), false);
	}

	public GIM_CONTACT at_const (SWIGTYPE_p_GUINT i) {
		return new GIM_CONTACT(CollisionJNI.gim_contact_array_internal_at_const(swigCPtr, this, SWIGTYPE_p_GUINT.getCPtr(i)),
			false);
	}

	public GIM_CONTACT front () {
		return new GIM_CONTACT(CollisionJNI.gim_contact_array_internal_front(swigCPtr, this), false);
	}

	public GIM_CONTACT front_const () {
		return new GIM_CONTACT(CollisionJNI.gim_contact_array_internal_front_const(swigCPtr, this), false);
	}

	public GIM_CONTACT back () {
		return new GIM_CONTACT(CollisionJNI.gim_contact_array_internal_back(swigCPtr, this), false);
	}

	public GIM_CONTACT back_const () {
		return new GIM_CONTACT(CollisionJNI.gim_contact_array_internal_back_const(swigCPtr, this), false);
	}

	public void swap (SWIGTYPE_p_GUINT i, SWIGTYPE_p_GUINT j) {
		CollisionJNI.gim_contact_array_internal_swap(swigCPtr, this, SWIGTYPE_p_GUINT.getCPtr(i), SWIGTYPE_p_GUINT.getCPtr(j));
	}

	public void push_back (GIM_CONTACT obj) {
		CollisionJNI.gim_contact_array_internal_push_back(swigCPtr, this, GIM_CONTACT.getCPtr(obj), obj);
	}

	public void push_back_mem () {
		CollisionJNI.gim_contact_array_internal_push_back_mem(swigCPtr, this);
	}

	public void push_back_memcpy (GIM_CONTACT obj) {
		CollisionJNI.gim_contact_array_internal_push_back_memcpy(swigCPtr, this, GIM_CONTACT.getCPtr(obj), obj);
	}

	public void pop_back () {
		CollisionJNI.gim_contact_array_internal_pop_back(swigCPtr, this);
	}

	public void pop_back_mem () {
		CollisionJNI.gim_contact_array_internal_pop_back_mem(swigCPtr, this);
	}

	public void erase (SWIGTYPE_p_GUINT index) {
		CollisionJNI.gim_contact_array_internal_erase(swigCPtr, this, SWIGTYPE_p_GUINT.getCPtr(index));
	}

	public void erase_sorted_mem (SWIGTYPE_p_GUINT index) {
		CollisionJNI.gim_contact_array_internal_erase_sorted_mem(swigCPtr, this, SWIGTYPE_p_GUINT.getCPtr(index));
	}

	public void erase_sorted (SWIGTYPE_p_GUINT index) {
		CollisionJNI.gim_contact_array_internal_erase_sorted(swigCPtr, this, SWIGTYPE_p_GUINT.getCPtr(index));
	}

	public void insert_mem (SWIGTYPE_p_GUINT index) {
		CollisionJNI.gim_contact_array_internal_insert_mem(swigCPtr, this, SWIGTYPE_p_GUINT.getCPtr(index));
	}

	public void insert (GIM_CONTACT obj, SWIGTYPE_p_GUINT index) {
		CollisionJNI.gim_contact_array_internal_insert(swigCPtr, this, GIM_CONTACT.getCPtr(obj), obj,
			SWIGTYPE_p_GUINT.getCPtr(index));
	}

	public void resize (SWIGTYPE_p_GUINT size, boolean call_constructor, GIM_CONTACT fillData) {
		CollisionJNI.gim_contact_array_internal_resize__SWIG_0(swigCPtr, this, SWIGTYPE_p_GUINT.getCPtr(size), call_constructor,
			GIM_CONTACT.getCPtr(fillData), fillData);
	}

	public void resize (SWIGTYPE_p_GUINT size, boolean call_constructor) {
		CollisionJNI.gim_contact_array_internal_resize__SWIG_1(swigCPtr, this, SWIGTYPE_p_GUINT.getCPtr(size), call_constructor);
	}

	public void resize (SWIGTYPE_p_GUINT size) {
		CollisionJNI.gim_contact_array_internal_resize__SWIG_2(swigCPtr, this, SWIGTYPE_p_GUINT.getCPtr(size));
	}

	public void refit () {
		CollisionJNI.gim_contact_array_internal_refit(swigCPtr, this);
	}

}
