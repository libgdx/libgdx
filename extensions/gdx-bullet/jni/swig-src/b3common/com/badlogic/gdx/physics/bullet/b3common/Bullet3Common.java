/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.12
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package com.badlogic.gdx.physics.bullet.b3common;

import com.badlogic.gdx.physics.bullet.BulletBase;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;

public class Bullet3Common {

	/** Temporary Vector3 instance, used by native methods that return a Vector3 instance */
	public final static Vector3 staticVector3 = new Vector3();
	/** Pool of Vector3, used by native (callback) method for the arguments */
	public final static com.badlogic.gdx.utils.Pool<Vector3> poolVector3 = new com.badlogic.gdx.utils.Pool<Vector3>() {
		@Override
		protected Vector3 newObject() {
			return new Vector3();
		}
	};


	/** Temporary Quaternion instance, used by native methods that return a Quaternion instance */
	public final static Quaternion staticQuaternion = new Quaternion();
	/** Pool of Quaternion, used by native (callback) method for the arguments */
	public final static com.badlogic.gdx.utils.Pool<Quaternion> poolQuaternion = new com.badlogic.gdx.utils.Pool<Quaternion>() {
		@Override
		protected Quaternion newObject() {
			return new Quaternion();
		}
	};


	/** Temporary Matrix3 instance, used by native methods that return a Matrix3 instance */
	public final static Matrix3 staticMatrix3 = new Matrix3();
	/** Pool of Matrix3, used by native (callback) method for the arguments */
	public final static com.badlogic.gdx.utils.Pool<Matrix3> poolMatrix3 = new com.badlogic.gdx.utils.Pool<Matrix3>() {
		@Override
		protected Matrix3 newObject() {
			return new Matrix3();
		}
	};


	/** Temporary Matrix4 instance, used by native methods that return a Matrix4 instance */
	public final static Matrix4 staticMatrix4 = new Matrix4();
	/** Pool of Matrix4, used by native (callback) method for the arguments */
	public final static com.badlogic.gdx.utils.Pool<Matrix4> poolMatrix4 = new com.badlogic.gdx.utils.Pool<Matrix4>() {
		@Override
		protected Matrix4 newObject() {
			return new Matrix4();
		}
	};

  public static void b3EnterProfileZone(String name) {
    Bullet3CommonJNI.b3EnterProfileZone(name);
  }

  public static void b3LeaveProfileZone() {
    Bullet3CommonJNI.b3LeaveProfileZone();
  }

  public static void b3SetCustomPrintfFunc(SWIGTYPE_p_f_p_q_const__char__void printfFunc) {
    Bullet3CommonJNI.b3SetCustomPrintfFunc(SWIGTYPE_p_f_p_q_const__char__void.getCPtr(printfFunc));
  }

  public static void b3SetCustomWarningMessageFunc(SWIGTYPE_p_f_p_q_const__char__void warningMsgFunc) {
    Bullet3CommonJNI.b3SetCustomWarningMessageFunc(SWIGTYPE_p_f_p_q_const__char__void.getCPtr(warningMsgFunc));
  }

  public static void b3SetCustomErrorMessageFunc(SWIGTYPE_p_f_p_q_const__char__void errorMsgFunc) {
    Bullet3CommonJNI.b3SetCustomErrorMessageFunc(SWIGTYPE_p_f_p_q_const__char__void.getCPtr(errorMsgFunc));
  }

  public static void b3SetCustomEnterProfileZoneFunc(SWIGTYPE_p_f_p_q_const__char__void enterFunc) {
    Bullet3CommonJNI.b3SetCustomEnterProfileZoneFunc(SWIGTYPE_p_f_p_q_const__char__void.getCPtr(enterFunc));
  }

  public static void b3SetCustomLeaveProfileZoneFunc(SWIGTYPE_p_f___void leaveFunc) {
    Bullet3CommonJNI.b3SetCustomLeaveProfileZoneFunc(SWIGTYPE_p_f___void.getCPtr(leaveFunc));
  }

  public static void b3OutputPrintfVarArgsInternal(String str) {
    Bullet3CommonJNI.b3OutputPrintfVarArgsInternal(str);
  }

  public static void b3OutputWarningMessageVarArgsInternal(String str) {
    Bullet3CommonJNI.b3OutputWarningMessageVarArgsInternal(str);
  }

  public static void b3OutputErrorMessageVarArgsInternal(String str) {
    Bullet3CommonJNI.b3OutputErrorMessageVarArgsInternal(str);
  }

}
