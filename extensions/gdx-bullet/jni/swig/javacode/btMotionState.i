/*
 *	Extra Java methods.
 */
 
%module btMotionState

/* newDerivedObject() required for down cast support (gdxDownCast.i). */

%typemap(javacode) btMotionState %{

  public static btMotionState newDerivedObject(long swigCPtr, boolean owner) {
    if (swigCPtr == 0) {
      return null;
    }
    
    // There's currently just one implementation.
    return new btDefaultMotionState(swigCPtr, owner);
  }
%}