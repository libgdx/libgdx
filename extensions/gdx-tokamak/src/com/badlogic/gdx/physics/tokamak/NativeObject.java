package com.badlogic.gdx.physics.tokamak;

/**
 * Base class for all native objects. Stores the address.
 * @author mzechner
 *
 */
public class NativeObject {
	long addr;
	
	NativeObject(long addr) {
		this.addr = addr;
	}
}
