/*
 *	Extra Java methods.
 */
 
%module btTransform

%include "../gdxDisableBuffers.i"
%include "../gdxEnableArrays.i"

%{
#include <LinearMath/btTransform.h>
%}
%include "LinearMath/btTransform.h"

%typemap(javacode) btTransform %{

  /**
   * Sets the values in this transform from the other.
   */
  public void set(btTransform other) {
  	setOrigin(other.getOrigin());
    setBasis(other.getBasis());
  }
  
  @Override
  public String toString() {
    return getOrigin() + "\n" + getBasis();
  }
%}
%include "../gdxDisableArrays.i"
%include "../gdxEnableBuffers.i"