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

public class SoftBodyClusterData extends BulletBase {
	private long swigCPtr;
	
	protected SoftBodyClusterData(final String className, long cPtr, boolean cMemoryOwn) {
		super(className, cPtr, cMemoryOwn);
		swigCPtr = cPtr;
	}
	
	protected SoftBodyClusterData(long cPtr, boolean cMemoryOwn) {
		this("SoftBodyClusterData", cPtr, cMemoryOwn);
		construct();
	}
	
	public static long getCPtr(SoftBodyClusterData obj) {
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
				gdxBulletJNI.delete_SoftBodyClusterData(swigCPtr);
			}
			swigCPtr = 0;
		}
		super.delete();
	}

  public void setFramexform(btTransformFloatData value) {
    gdxBulletJNI.SoftBodyClusterData_framexform_set(swigCPtr, this, btTransformFloatData.getCPtr(value), value);
  }

  public btTransformFloatData getFramexform() {
    long cPtr = gdxBulletJNI.SoftBodyClusterData_framexform_get(swigCPtr, this);
    return (cPtr == 0) ? null : new btTransformFloatData(cPtr, false);
  }

  public void setLocii(SWIGTYPE_p_btMatrix3x3FloatData value) {
    gdxBulletJNI.SoftBodyClusterData_locii_set(swigCPtr, this, SWIGTYPE_p_btMatrix3x3FloatData.getCPtr(value));
  }

  public SWIGTYPE_p_btMatrix3x3FloatData getLocii() {
    return new SWIGTYPE_p_btMatrix3x3FloatData(gdxBulletJNI.SoftBodyClusterData_locii_get(swigCPtr, this), true);
  }

  public void setInvwi(SWIGTYPE_p_btMatrix3x3FloatData value) {
    gdxBulletJNI.SoftBodyClusterData_invwi_set(swigCPtr, this, SWIGTYPE_p_btMatrix3x3FloatData.getCPtr(value));
  }

  public SWIGTYPE_p_btMatrix3x3FloatData getInvwi() {
    return new SWIGTYPE_p_btMatrix3x3FloatData(gdxBulletJNI.SoftBodyClusterData_invwi_get(swigCPtr, this), true);
  }

  public void setCom(btVector3FloatData value) {
    gdxBulletJNI.SoftBodyClusterData_com_set(swigCPtr, this, btVector3FloatData.getCPtr(value), value);
  }

  public btVector3FloatData getCom() {
    long cPtr = gdxBulletJNI.SoftBodyClusterData_com_get(swigCPtr, this);
    return (cPtr == 0) ? null : new btVector3FloatData(cPtr, false);
  }

  public void setVimpulses(btVector3FloatData value) {
    gdxBulletJNI.SoftBodyClusterData_vimpulses_set(swigCPtr, this, btVector3FloatData.getCPtr(value), value);
  }

  public btVector3FloatData getVimpulses() {
    long cPtr = gdxBulletJNI.SoftBodyClusterData_vimpulses_get(swigCPtr, this);
    return (cPtr == 0) ? null : new btVector3FloatData(cPtr, false);
  }

  public void setDimpulses(btVector3FloatData value) {
    gdxBulletJNI.SoftBodyClusterData_dimpulses_set(swigCPtr, this, btVector3FloatData.getCPtr(value), value);
  }

  public btVector3FloatData getDimpulses() {
    long cPtr = gdxBulletJNI.SoftBodyClusterData_dimpulses_get(swigCPtr, this);
    return (cPtr == 0) ? null : new btVector3FloatData(cPtr, false);
  }

  public void setLv(btVector3FloatData value) {
    gdxBulletJNI.SoftBodyClusterData_lv_set(swigCPtr, this, btVector3FloatData.getCPtr(value), value);
  }

  public btVector3FloatData getLv() {
    long cPtr = gdxBulletJNI.SoftBodyClusterData_lv_get(swigCPtr, this);
    return (cPtr == 0) ? null : new btVector3FloatData(cPtr, false);
  }

  public void setAv(btVector3FloatData value) {
    gdxBulletJNI.SoftBodyClusterData_av_set(swigCPtr, this, btVector3FloatData.getCPtr(value), value);
  }

  public btVector3FloatData getAv() {
    long cPtr = gdxBulletJNI.SoftBodyClusterData_av_get(swigCPtr, this);
    return (cPtr == 0) ? null : new btVector3FloatData(cPtr, false);
  }

  public void setFramerefs(btVector3FloatData value) {
    gdxBulletJNI.SoftBodyClusterData_framerefs_set(swigCPtr, this, btVector3FloatData.getCPtr(value), value);
  }

  public btVector3FloatData getFramerefs() {
    long cPtr = gdxBulletJNI.SoftBodyClusterData_framerefs_get(swigCPtr, this);
    return (cPtr == 0) ? null : new btVector3FloatData(cPtr, false);
  }

  public void setNodeIndices(java.nio.IntBuffer value) {
    assert value.isDirect() : "Buffer must be allocated direct.";
    {
      gdxBulletJNI.SoftBodyClusterData_nodeIndices_set(swigCPtr, this, value);
    }
  }

  public java.nio.IntBuffer getNodeIndices() {
    return gdxBulletJNI.SoftBodyClusterData_nodeIndices_get(swigCPtr, this);
}

  public void setMasses(java.nio.FloatBuffer value) {
    assert value.isDirect() : "Buffer must be allocated direct.";
    {
      gdxBulletJNI.SoftBodyClusterData_masses_set(swigCPtr, this, value);
    }
  }

  public java.nio.FloatBuffer getMasses() {
    return gdxBulletJNI.SoftBodyClusterData_masses_get(swigCPtr, this);
}

  public void setNumFrameRefs(int value) {
    gdxBulletJNI.SoftBodyClusterData_numFrameRefs_set(swigCPtr, this, value);
  }

  public int getNumFrameRefs() {
    return gdxBulletJNI.SoftBodyClusterData_numFrameRefs_get(swigCPtr, this);
  }

  public void setNumNodes(int value) {
    gdxBulletJNI.SoftBodyClusterData_numNodes_set(swigCPtr, this, value);
  }

  public int getNumNodes() {
    return gdxBulletJNI.SoftBodyClusterData_numNodes_get(swigCPtr, this);
  }

  public void setNumMasses(int value) {
    gdxBulletJNI.SoftBodyClusterData_numMasses_set(swigCPtr, this, value);
  }

  public int getNumMasses() {
    return gdxBulletJNI.SoftBodyClusterData_numMasses_get(swigCPtr, this);
  }

  public void setIdmass(float value) {
    gdxBulletJNI.SoftBodyClusterData_idmass_set(swigCPtr, this, value);
  }

  public float getIdmass() {
    return gdxBulletJNI.SoftBodyClusterData_idmass_get(swigCPtr, this);
  }

  public void setImass(float value) {
    gdxBulletJNI.SoftBodyClusterData_imass_set(swigCPtr, this, value);
  }

  public float getImass() {
    return gdxBulletJNI.SoftBodyClusterData_imass_get(swigCPtr, this);
  }

  public void setNvimpulses(int value) {
    gdxBulletJNI.SoftBodyClusterData_nvimpulses_set(swigCPtr, this, value);
  }

  public int getNvimpulses() {
    return gdxBulletJNI.SoftBodyClusterData_nvimpulses_get(swigCPtr, this);
  }

  public void setNdimpulses(int value) {
    gdxBulletJNI.SoftBodyClusterData_ndimpulses_set(swigCPtr, this, value);
  }

  public int getNdimpulses() {
    return gdxBulletJNI.SoftBodyClusterData_ndimpulses_get(swigCPtr, this);
  }

  public void setNdamping(float value) {
    gdxBulletJNI.SoftBodyClusterData_ndamping_set(swigCPtr, this, value);
  }

  public float getNdamping() {
    return gdxBulletJNI.SoftBodyClusterData_ndamping_get(swigCPtr, this);
  }

  public void setLdamping(float value) {
    gdxBulletJNI.SoftBodyClusterData_ldamping_set(swigCPtr, this, value);
  }

  public float getLdamping() {
    return gdxBulletJNI.SoftBodyClusterData_ldamping_get(swigCPtr, this);
  }

  public void setAdamping(float value) {
    gdxBulletJNI.SoftBodyClusterData_adamping_set(swigCPtr, this, value);
  }

  public float getAdamping() {
    return gdxBulletJNI.SoftBodyClusterData_adamping_get(swigCPtr, this);
  }

  public void setMatching(float value) {
    gdxBulletJNI.SoftBodyClusterData_matching_set(swigCPtr, this, value);
  }

  public float getMatching() {
    return gdxBulletJNI.SoftBodyClusterData_matching_get(swigCPtr, this);
  }

  public void setMaxSelfCollisionImpulse(float value) {
    gdxBulletJNI.SoftBodyClusterData_maxSelfCollisionImpulse_set(swigCPtr, this, value);
  }

  public float getMaxSelfCollisionImpulse() {
    return gdxBulletJNI.SoftBodyClusterData_maxSelfCollisionImpulse_get(swigCPtr, this);
  }

  public void setSelfCollisionImpulseFactor(float value) {
    gdxBulletJNI.SoftBodyClusterData_selfCollisionImpulseFactor_set(swigCPtr, this, value);
  }

  public float getSelfCollisionImpulseFactor() {
    return gdxBulletJNI.SoftBodyClusterData_selfCollisionImpulseFactor_get(swigCPtr, this);
  }

  public void setContainsAnchor(int value) {
    gdxBulletJNI.SoftBodyClusterData_containsAnchor_set(swigCPtr, this, value);
  }

  public int getContainsAnchor() {
    return gdxBulletJNI.SoftBodyClusterData_containsAnchor_get(swigCPtr, this);
  }

  public void setCollide(int value) {
    gdxBulletJNI.SoftBodyClusterData_collide_set(swigCPtr, this, value);
  }

  public int getCollide() {
    return gdxBulletJNI.SoftBodyClusterData_collide_get(swigCPtr, this);
  }

  public void setClusterIndex(int value) {
    gdxBulletJNI.SoftBodyClusterData_clusterIndex_set(swigCPtr, this, value);
  }

  public int getClusterIndex() {
    return gdxBulletJNI.SoftBodyClusterData_clusterIndex_get(swigCPtr, this);
  }

  public SoftBodyClusterData() {
    this(gdxBulletJNI.new_SoftBodyClusterData(), true);
  }

}
