/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

 /* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 2.0.10
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package com.badlogic.gdx.physics.bullet;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;

public class ContactCache extends BulletBase {
	private long swigCPtr;
	
	protected ContactCache(final String className, long cPtr, boolean cMemoryOwn) {
		super(className, cPtr, cMemoryOwn);
		swigCPtr = cPtr;
	}
	
	protected ContactCache(long cPtr, boolean cMemoryOwn) {
		this("ContactCache", cPtr, cMemoryOwn);
		construct();
	}
	
	public static long getCPtr(ContactCache obj) {
		return (obj == null) ? 0 : obj.swigCPtr;
	}

	@Override
	protected void finalize() throws Throwable {
		if (!destroyed)
			destroy();
		super.finalize();
	}

  @Override protected synchronized void delete() {
		if (swigCPtr != 0) {
			if (swigCMemOwn) {
				swigCMemOwn = false;
				gdxBulletJNI.delete_ContactCache(swigCPtr);
			}
			swigCPtr = 0;
		}
		super.delete();
	}

  protected void swigDirectorDisconnect() {
    swigCMemOwn = false;
    delete();
  }

  public void swigReleaseOwnership() {
    swigCMemOwn = false;
    gdxBulletJNI.ContactCache_change_ownership(this, swigCPtr, false);
  }

  public void swigTakeOwnership() {
    swigCMemOwn = true;
    gdxBulletJNI.ContactCache_change_ownership(this, swigCPtr, true);
  }

	public ContactCache() {
		this(false);
		enable();
	}

  public void setCacheTime(float value) {
    gdxBulletJNI.ContactCache_cacheTime_set(swigCPtr, this, value);
  }

  public float getCacheTime() {
    return gdxBulletJNI.ContactCache_cacheTime_get(swigCPtr, this);
  }

  private ContactCache(boolean dummy) {
    this(gdxBulletJNI.new_ContactCache(dummy), true);
    gdxBulletJNI.ContactCache_director_connect(this, swigCPtr, swigCMemOwn, true);
  }

  public void enable() {
    gdxBulletJNI.ContactCache_enable(swigCPtr, this);
  }

  public void disable() {
    gdxBulletJNI.ContactCache_disable(swigCPtr, this);
  }

  public boolean isEnabled() {
    return gdxBulletJNI.ContactCache_isEnabled(swigCPtr, this);
  }

  public void onContactStarted(btPersistentManifold manifold, boolean match0, boolean match1) {
    gdxBulletJNI.ContactCache_onContactStarted(swigCPtr, this, btPersistentManifold.getCPtr(manifold), manifold, match0, match1);
  }

  public void onContactEnded(btCollisionObject colObj0, boolean match0, btCollisionObject colObj1, boolean match1) {
    gdxBulletJNI.ContactCache_onContactEnded(swigCPtr, this, btCollisionObject.getCPtr(colObj0), colObj0, match0, btCollisionObject.getCPtr(colObj1), colObj1, match1);
  }

  public void clear() {
    gdxBulletJNI.ContactCache_clear(swigCPtr, this);
  }

  public void update(float delta) {
    gdxBulletJNI.ContactCache_update(swigCPtr, this, delta);
  }

}
