/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.7
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package com.badlogic.gdx.physics.bullet.linearmath;

import com.badlogic.gdx.physics.bullet.BulletBase;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;

public class btConvexHullComputer extends BulletBase {
	private long swigCPtr;
	
	protected btConvexHullComputer(final String className, long cPtr, boolean cMemoryOwn) {
		super(className, cPtr, cMemoryOwn);
		swigCPtr = cPtr;
	}
	
	/** Construct a new btConvexHullComputer, normally you should not need this constructor it's intended for low-level usage. */ 
	public btConvexHullComputer(long cPtr, boolean cMemoryOwn) {
		this("btConvexHullComputer", cPtr, cMemoryOwn);
		construct();
	}
	
	@Override
	protected void reset(long cPtr, boolean cMemoryOwn) {
		if (!destroyed)
			destroy();
		super.reset(swigCPtr = cPtr, cMemoryOwn);
	}
	
	public static long getCPtr(btConvexHullComputer obj) {
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
				LinearMathJNI.delete_btConvexHullComputer(swigCPtr);
			}
			swigCPtr = 0;
		}
		super.delete();
	}

  static public class Edge extends BulletBase {
  	private long swigCPtr;
  	
  	protected Edge(final String className, long cPtr, boolean cMemoryOwn) {
  		super(className, cPtr, cMemoryOwn);
  		swigCPtr = cPtr;
  	}
  	
  	/** Construct a new Edge, normally you should not need this constructor it's intended for low-level usage. */ 
  	public Edge(long cPtr, boolean cMemoryOwn) {
  		this("Edge", cPtr, cMemoryOwn);
  		construct();
  	}
  	
  	@Override
  	protected void reset(long cPtr, boolean cMemoryOwn) {
  		if (!destroyed)
  			destroy();
  		super.reset(swigCPtr = cPtr, cMemoryOwn);
  	}
  	
  	public static long getCPtr(Edge obj) {
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
  				LinearMathJNI.delete_btConvexHullComputer_Edge(swigCPtr);
  			}
  			swigCPtr = 0;
  		}
  		super.delete();
  	}
  
    public int getSourceVertex() {
      return LinearMathJNI.btConvexHullComputer_Edge_getSourceVertex(swigCPtr, this);
    }
  
    public int getTargetVertex() {
      return LinearMathJNI.btConvexHullComputer_Edge_getTargetVertex(swigCPtr, this);
    }
  
    public btConvexHullComputer.Edge getNextEdgeOfVertex() {
      long cPtr = LinearMathJNI.btConvexHullComputer_Edge_getNextEdgeOfVertex(swigCPtr, this);
      return (cPtr == 0) ? null : new btConvexHullComputer.Edge(cPtr, false);
    }
  
    public btConvexHullComputer.Edge getNextEdgeOfFace() {
      long cPtr = LinearMathJNI.btConvexHullComputer_Edge_getNextEdgeOfFace(swigCPtr, this);
      return (cPtr == 0) ? null : new btConvexHullComputer.Edge(cPtr, false);
    }
  
    public btConvexHullComputer.Edge getReverseEdge() {
      long cPtr = LinearMathJNI.btConvexHullComputer_Edge_getReverseEdge(swigCPtr, this);
      return (cPtr == 0) ? null : new btConvexHullComputer.Edge(cPtr, false);
    }
  
    public Edge() {
      this(LinearMathJNI.new_btConvexHullComputer_Edge(), true);
    }
  
  }

  public void setVertices(btVector3Array value) {
    LinearMathJNI.btConvexHullComputer_vertices_set(swigCPtr, this, btVector3Array.getCPtr(value), value);
  }

  public btVector3Array getVertices() {
    long cPtr = LinearMathJNI.btConvexHullComputer_vertices_get(swigCPtr, this);
    return (cPtr == 0) ? null : new btVector3Array(cPtr, false);
  }

  public void setEdges(SWIGTYPE_p_btAlignedObjectArrayT_btConvexHullComputer__Edge_t value) {
    LinearMathJNI.btConvexHullComputer_edges_set(swigCPtr, this, SWIGTYPE_p_btAlignedObjectArrayT_btConvexHullComputer__Edge_t.getCPtr(value));
  }

  public SWIGTYPE_p_btAlignedObjectArrayT_btConvexHullComputer__Edge_t getEdges() {
    long cPtr = LinearMathJNI.btConvexHullComputer_edges_get(swigCPtr, this);
    return (cPtr == 0) ? null : new SWIGTYPE_p_btAlignedObjectArrayT_btConvexHullComputer__Edge_t(cPtr, false);
  }

  public void setFaces(SWIGTYPE_p_btAlignedObjectArrayT_int_t value) {
    LinearMathJNI.btConvexHullComputer_faces_set(swigCPtr, this, SWIGTYPE_p_btAlignedObjectArrayT_int_t.getCPtr(value));
  }

  public SWIGTYPE_p_btAlignedObjectArrayT_int_t getFaces() {
    long cPtr = LinearMathJNI.btConvexHullComputer_faces_get(swigCPtr, this);
    return (cPtr == 0) ? null : new SWIGTYPE_p_btAlignedObjectArrayT_int_t(cPtr, false);
  }

  public float compute(java.nio.FloatBuffer coords, int stride, int count, float shrink, float shrinkClamp) {
    assert coords.isDirect() : "Buffer must be allocated direct.";
    {
      return LinearMathJNI.btConvexHullComputer_compute__SWIG_0(swigCPtr, this, coords, stride, count, shrink, shrinkClamp);
    }
  }

  public float compute(java.nio.DoubleBuffer coords, int stride, int count, float shrink, float shrinkClamp) {
    assert coords.isDirect() : "Buffer must be allocated direct.";
    {
      return LinearMathJNI.btConvexHullComputer_compute__SWIG_1(swigCPtr, this, coords, stride, count, shrink, shrinkClamp);
    }
  }

  public btConvexHullComputer() {
    this(LinearMathJNI.new_btConvexHullComputer(), true);
  }

}
